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
package com.mgmtp.a12.dataservices.document.support;

import java.io.Reader;
import java.io.Writer;
import java.util.Locale;

import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentSpec;
import com.mgmtp.a12.dataservices.utils.internal.DataServicesDocumentProblemReporterException;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import tools.jackson.databind.JsonNode;

/**
 * Convenient methods for dealing with documents.
 */
@OnlyForUsage public interface DocumentSupport {

	/**
	 * Convert the given JSON document to {@link DocumentV2} type.
	 *
	 * @param documentModelName The document model name.
	 * @param jsonDocument The JSON document to be converted.
	 * @return The converted {@link DocumentV2} instance.
	 * @throws DataServicesDocumentProblemReporterException if the document cannot be deserialized.
	 */
	DocumentV2 convertJSONToDocument(String documentModelName, Reader jsonDocument) throws DataServicesDocumentProblemReporterException;

	/**
	 * Provides improved log message by including document reference in the message. For more details, see {@link #convertJSONToDocument(String, Reader)}.
	 *
	 * @param documentModelName The document model name.
	 * @param jsonDocument The JSON document to be converted.
	 * @return The converted {@link DocumentV2} instance.
	 * @throws DataServicesDocumentProblemReporterException if the document cannot be deserialized.
	 */
	DocumentV2 convertJSONToDocument(String documentModelName, Reader jsonDocument, DocumentReference documentReference);

	/**
	 * Convert the given {@link DocumentV2} instance to JSON.
	 *
	 * @param document The {@link DocumentV2} instance.
	 * @param writer The target `Writer`.
	 */
	void convertDocumentToJSON(DocumentV2 document, Writer writer);

	/**
	 * Convert the {@link DataServicesDocument} to the {@link DocumentSpec} type.
	 *
	 * @param dataServicesDocument The {@link DataServicesDocument} instance to be converted.
	 * @return The converted {@link DocumentSpec} instance.
	 */
	DocumentSpec convertToDocumentSpec(DataServicesDocument dataServicesDocument);

	/**
	 * Resolve the {@link Locale} configured in the document.
	 *
	 * @param document The given {@link DocumentV2} to be resolved.
	 * @param preferredLocale The preferred locale.
	 * @param skipNonExisting if `true`, skip the `preferredLocale` if it does not exist.
	 * @return The first found locale if `preferredLocale` is null.
	 */
	Locale resolveLocale(DocumentV2 document, Locale preferredLocale, boolean skipNonExisting);

	/**
	 * Deserialize the given document to {@link DocumentV2} type.
	 *
	 * @param documentModelName The document model name.
	 * @param reader The document to be converted.
	 * @param deserializationConfig The deserialization configuration.
	 * @return The converted {@link DocumentV2} instance.
	 * @throws DataServicesDocumentProblemReporterException if the document cannot be deserialized.
	 */
	DocumentV2 deserialize(String documentModelName, Reader reader, DocumentDeserializationConfig deserializationConfig) throws
		DataServicesDocumentProblemReporterException;

	/**
	 * Serialize the given {@link DocumentV2} instance to string.
	 *
	 * @param document The {@link DocumentV2} instance.
	 * @param writer The target `Writer`.
	 * @param serializationConfig The serialization configuration.
	 */
	void serialize(DocumentV2 document, Writer writer, DocumentSerializationConfig serializationConfig);

	/**
	 * Convert the given JSON document to {@link DocumentV2} type.
	 *
	 * This method accepts a `JsonNode` directly, centralizing the JSON-to-Document conversion
	 * in one place. Internally, the JsonNode is converted to a String and deserialized using
	 * the standard Reader-based deserialization path.
	 *
	 * @param documentModelName The document model name.
	 * @param jsonNode The JSON document as a Jackson `JsonNode`.
	 * @return The converted {@link DocumentV2} instance.
	 * @throws DataServicesDocumentProblemReporterException if the document cannot be deserialized.
	 */
	DocumentV2 convertJSONToDocument(String documentModelName, JsonNode jsonNode) throws DataServicesDocumentProblemReporterException;

	/**
	 * Convert the given JSON document to {@link DocumentV2} type with document reference for logging.
	 *
	 * Provides improved log messages by including document reference in error messages.
	 * This method accepts a `JsonNode` directly, centralizing the JSON-to-Document conversion
	 * in one place. Internally, the JsonNode is converted to a String and deserialized using
	 * the standard Reader-based deserialization path.
	 *
	 * @param documentModelName The document model name.
	 * @param jsonNode The JSON document as a Jackson `JsonNode`.
	 * @param documentReference The document reference for improved error messages.
	 * @return The converted {@link DocumentV2} instance.
	 * @throws DataServicesDocumentProblemReporterException if the document cannot be deserialized.
	 */
	DocumentV2 convertJSONToDocument(String documentModelName, JsonNode jsonNode, DocumentReference documentReference);
}
