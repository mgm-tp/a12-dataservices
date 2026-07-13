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
package com.mgmtp.a12.dataservices.internal.service.importer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component public class DocumentImporter {

	private final DocumentService documentService;
	private final DocumentSupport documentSupport;

	/**
	 * Imports a single document from in-memory content.
	 *
	 * @param modelName the document model name
	 * @param entityId the document entity ID
	 * @param content the raw JSON document content
	 */
	public void importDocument(String modelName, String entityId, byte[] content) {
		try (Reader reader = new InputStreamReader(new ByteArrayInputStream(content), StandardCharsets.UTF_8)) {
			documentService.create(documentSupport.convertJSONToDocument(modelName, reader).withId(entityId), null);
		} catch (IOException e) {
			throw new InvalidInputException(ExceptionKeys.SME_WORKSPACE_IMPORT_ERROR_KEY,
				"Cannot parse document with model %s and id %s ".formatted(modelName, entityId))
				.withAnonymityMessage("Import document failed");
		} catch (NotFoundException e) {
			throw new InvalidInputException(ExceptionKeys.SME_WORKSPACE_IMPORT_ERROR_KEY,
				"Cannot import document with error " + e.getMessage())
				.withAnonymityMessage("Cannot import document.");
		}
	}
}
