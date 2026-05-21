/*
 * SPDX-License-Identifier: EUPL-1.2 OR LicenseRef-commercial
 *
 * Copyright (c) 2012-2026 mgm technology partners GmbH
 *
 * Dual License
 * ------------
 * This source file is part of the mgm A12 Platform and available under
 * a choice of two different licenses:
 *
 * 1. Open-Source License – EUPL v1.2
 *    You may redistribute and/or modify this file under the terms of the
 *    European Union Public License, version 1.2 - see https://eupl.eu/.
 *
 * 2. Commercial License
 *    Alternatively, you may obtain a commercial license from
 *    mgm technology partners GmbH, that permits use of this software
 *    under different terms (including support and maintenance services).
 *
 *    Please contact a12-license@mgm-tp.com for more information.
 *
 * You must select and comply with exactly one of the above license options.
 *
 * Warranty Disclaimer (applies to either option)
 * ----------------------------------------------
 * THIS SOFTWARE IS PROVIDED “AS IS” AND WITHOUT WARRANTY OF ANY KIND,
 * WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT, EXCEPT WHERE SUCH DISCLAIMERS ARE HELD TO BE
 * LEGALLY INVALID. SEE THE RESPECTIVE LICENSE TEXT FOR DETAILS.
 */
package com.mgmtp.a12.dataservices.model.document.internal;

import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.model.document.IValidationCodeProvider;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig;

import org.springframework.cache.annotation.Cacheable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.zip.ZipInputStream;

import lombok.AllArgsConstructor;

import static com.mgmtp.a12.dataservices.model.internal.ModelCacheManager.VALIDATION_CACHE_CACHE;

/**
 * Generates validation codes for document models.
 *
 * This class is responsible for generating validation codes based on the provided document model and configuration.
 * It checks the consistency of the document model and handles any issues that arise during the generation process.
 */
@AllArgsConstructor
public class ValidationCodeGenerator implements IValidationCodeProvider {

	private IDocumentModelService iDocumentModelService;
	private IValidationCodeGeneratorConfig validationCodeGeneratorConfig;
	private IModelLoader<IDocumentModel> documentModelLoader;

	@Cacheable(value = VALIDATION_CACHE_CACHE, key = "#documentModelId")
	public String getValidationCode(String documentModelId, ListIProblemReporter pr) {
		return generateValidationCode(documentModelLoader.loadModel(documentModelId), pr);
	}

	public String generateValidationCode(IDocumentModel documentModel, ListIProblemReporter pr) {
		iDocumentModelService.checkConsistency(documentModel, pr);
		pr.validate(ExceptionCodes.VALIDATION_CODES_GENERATION_EXCEPTION_CODE, ExceptionKeys.VALIDATION_CODES_GENERATION_ERROR_KEY, "Error while generating validation codes");
		return convertToString(iDocumentModelService.generateValidationCode(documentModel, validationCodeGeneratorConfig, null, pr));
	}

	private static String convertToString(final byte[] zipContent) {
		if (zipContent == null) {
			return null;
		}
		final StringBuilder sb = new StringBuilder();
		final ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipContent));
		try {
			while (zis.getNextEntry() != null) {
				final Scanner sc = new Scanner(zis, StandardCharsets.UTF_8);
				while (sc.hasNextLine()) {
					sb.append(sc.nextLine()).append(System.lineSeparator());
				}
			}
		} catch (final IOException e) {
			throw new UnexpectedException("Unable to unpack validation code form ZIP(ed) data.");
		}
		return sb.toString();
	}
}
