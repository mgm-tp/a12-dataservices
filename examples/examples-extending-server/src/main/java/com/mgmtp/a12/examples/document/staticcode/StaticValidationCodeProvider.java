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
package com.mgmtp.a12.examples.document.staticcode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.cache.annotation.Cacheable;

import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.dataservices.model.document.IValidationCodeProvider;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Loads validation code for document models from static JavaScript files.
 * The files are expected to be located in the classpath under the specified directory.
 */
@Slf4j
@AllArgsConstructor
public class StaticValidationCodeProvider implements IValidationCodeProvider {

	private final String javascriptDirectory;

	/**
	 * Loads the validation code for a document model by its ID.
	 * The validation code is expected to be in a JavaScript file named "{documentModelId}.js"
	 * located in the specified directory.
	 *
	 * @param documentModelId the ID of the document model
	 * @param pr the problem reporter to report issues
	 * @return the validation code as a string, or null if not found
	 */
	@Cacheable(value = "validationCache", key = "#documentModelId")
	@Override public String getValidationCode(String documentModelId, ListIProblemReporter pr) {
		try (InputStream stream = getResourceAsStream(javascriptDirectory + documentModelId + ".js")) {
			if (stream == null) {
				failValidation(documentModelId, pr);
				return null;
			} else {
				return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
			}
		} catch (IOException e) {
			failValidation(documentModelId, pr);
			return null;
		}
	}

	private void failValidation(String documentModelId, ListIProblemReporter pr) {
		pr.validate(ExceptionCodes.VALIDATION_CODES_GENERATION_EXCEPTION_CODE, ExceptionKeys.VALIDATION_CODES_GENERATION_ERROR_KEY, "Validation code generation failed.");
		log.error("Validation code was not loaded for document model ID: {}. No validation is possible for documents of that model", documentModelId);
	}

	/**
	 * Loads a resource as an input stream from the classpath.
	 *
	 * @param path the resource path relative to the classpath root; never null.
	 * @return the input stream for the resource, or `null` if not found.
	 */
	protected InputStream getResourceAsStream(String path) {
		return StaticValidationCodeProvider.class.getClassLoader().getResourceAsStream(path);
	}
}

