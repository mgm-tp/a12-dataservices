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
package com.mgmtp.a12.dataservices;

import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

@Component public class DocumentFunctions {

	@Autowired private DocumentSupport documentSupport;
	@Autowired private ResourceFunctions resourceFunctions;
	@Autowired private DocumentService documentService;

	/**
	 * Convenient method to convert a DataServicesDocument to its JSON representation.
	 *
	 * @param document The document to convert.
	 * @return The JSON representation of the document.
	 */
	public String convertDocumentToJson(DataServicesDocument document) {
		Writer writer = new StringWriter();
		documentSupport.convertDocumentToJSON(document.getKernelDocument(), writer);
		return writer.toString();
	}

	/**
	 * Creates a DataServicesDocument from raw JSON content for a given model.
	 *
	 * @param modelName the name of the document model the JSON conforms to
	 * @param jsonContent the JSON content representing the document
	 * @return The created DataServicesDocument.
	 */
	public DataServicesDocument createDocumentFromJson(final String modelName, final String jsonContent) {
		DocumentV2 document = documentSupport.convertJSONToDocument(modelName, new StringReader(jsonContent));
		return createDocumentFromKernelDocumentInternal(document);
	}

	/**
	 * Convenient method to create a KernelDocument (aka DocumentV2) from a file.
	 *
	 * @param modelName The name of the document model.
	 * @param fileName The file name containing the document content in JSON format.
	 * @return The KernelDocument of the created DataServicesDocument.
	 * @throws IOException If an I/O error occurs while reading the file.
	 */
	public DocumentV2 getKernelDocumentFromFile(final String modelName, final String fileName) throws IOException {
		return documentSupport.convertJSONToDocument(modelName, resourceFunctions.loadResourceAsReader(fileName));
	}

	/**
	 * Creates a DataServicesDocument from a file.
	 *
	 * @param modelName The name of the document model.
	 * @param fileName The file name containing the document content in JSON format.
	 * @return The created DataServicesDocument.
	 * @throws IOException If an I/O error occurs while reading the file.
	 */
	public DataServicesDocument createDocumentFromFile(final String modelName, final String fileName) throws IOException {
		DocumentV2 document = getKernelDocumentFromFile(modelName, fileName);
		return createDocumentFromKernelDocumentInternal(document);
	}

	/**
	 * Creates a DataServicesDocument from a file and returns its DocumentReference.
	 *
	 * @param modelName The name of the document model.
	 * @param fileName The file name containing the document content in JSON format.
	 * @return The DocumentReference of the created DataServicesDocument.
	 * @throws IOException If an I/O error occurs while reading the file.
	 */
	public DocumentReference createDocumentFromFileAndGetDocRef(final String modelName, final String fileName) throws IOException {
		return createDocumentFromFile(modelName, fileName).getMetadata().getDocRef();
	}

	private DataServicesDocument createDocumentFromKernelDocumentInternal(DocumentV2 document) {
		return documentService.create(document, null);
	}
}
