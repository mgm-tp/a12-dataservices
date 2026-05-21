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
package com.mgmtp.a12.dataservices.document.support.internal;

import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.time.StopWatch;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentSpec;
import com.mgmtp.a12.dataservices.document.exception.DataServicesDocumentSerializationException;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.utils.internal.DataServicesDocumentProblemReporterException;
import com.mgmtp.a12.dataservices.utils.internal.DataServicesIOUtils;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationException;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.model.header.Header;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.DOCUMENT_LOCALES_INVALID_ERROR_KEY;

@Slf4j
@RequiredArgsConstructor
public class DefaultDocumentSupport implements DocumentSupport {

	private final DocumentDeserializationConfig documentJsonDeserializationConfig;
	private final DocumentSerializationConfig documentJsonSerializationConfig;
	private final IDocumentModelResolver documentModelResolver;
	private final IDocumentV2Serializer documentV2Serializer;

	@Override public DocumentV2 deserialize(String documentModelName, Reader reader, DocumentDeserializationConfig deserializationConfig)
		throws DataServicesDocumentProblemReporterException {
		return deserializeDocument(reader, documentModelName, deserializationConfig, null);
	}

	@Override public DocumentV2 convertJSONToDocument(String documentModelName, Reader jsonDocument) throws DataServicesDocumentProblemReporterException {
		return deserializeDocument(jsonDocument, documentModelName, documentJsonDeserializationConfig, null);
	}

	@Override public DocumentV2 convertJSONToDocument(String documentModelName, Reader jsonDocument, DocumentReference documentReference) {
		return deserializeDocument(jsonDocument, documentModelName, documentJsonDeserializationConfig, documentReference);
	}

	@Override
	public void serialize(DocumentV2 document, Writer writer, DocumentSerializationConfig serializationConfig) throws
		DataServicesDocumentProblemReporterException {
		documentV2Serializer.serializeV2(document, writer, serializationConfig);
	}

	@Override public void convertDocumentToJSON(DocumentV2 document, Writer writer) {
		documentV2Serializer.serializeV2(document, writer, documentJsonSerializationConfig);
	}

	@Override public DocumentSpec convertToDocumentSpec(DataServicesDocument dataServicesDocument) {
		String document = DataServicesIOUtils.writeString(sw -> convertDocumentToJSON(dataServicesDocument.getKernelDocument(), sw));
		return new DocumentSpec(dataServicesDocument.getMetadata().getDocRef(), document);
	}

	@Override public Locale resolveLocale(DocumentV2 document, Locale preferredLocale, boolean skipNonExisting) {
		List<Locale> localesInModel = Optional.ofNullable(document)
			.map(DocumentV2::getDocumentModelId)
			.map(documentModelResolver::getDocumentModelById)
			.map(IDocumentModel::getHeader)
			.map(Header::getLocales)
			.stream()
			.flatMap(Collection::stream)
			.toList();

		boolean hasPreferredLocaleInModel = preferredLocale != null &&
			localesInModel.stream()
				.anyMatch(locale -> locale.getLanguage().equals(preferredLocale.getLanguage()));

		if (hasPreferredLocaleInModel) {
			return preferredLocale;
		}

		Locale firstLocale = localesInModel.stream()
			.findFirst()
			.orElseThrow(() -> new InvalidInputException(DOCUMENT_LOCALES_INVALID_ERROR_KEY, "Unable to lookup locale. Define language(s) in document model."));

		return !skipNonExisting && preferredLocale != null ? preferredLocale : firstLocale;
	}

	private DocumentV2 deserializeDocument(Reader reader, String documentModelId, @NonNull DocumentDeserializationConfig deserializationConfig,
		DocumentReference documentReference) {
		try {
			StopWatch stopWatch = StopWatch.createStarted();
			ListIProblemReporter pr = new ListIProblemReporter();
			DocumentV2 document = documentV2Serializer.deserializeV2(reader, documentModelId, deserializationConfig, pr);
			if (pr.hasProblems()) {
				log.warn(String.format("Deserialization of document has failed. Reason: %s", pr));
				throw new DataServicesDocumentProblemReporterException(
					ExceptionCodes.DOCUMENT_MODEL_DE_SERIALIZATION_EXCEPTION_CODE,
					ExceptionKeys.DOCUMENT_CONVERSION_ERROR_KEY,
					pr,
					String.format("The validation of document %s",
						Optional.ofNullable(documentReference)
							.filter(dr -> !documentReference.getDocumentId().isEmpty())
							.map(dr -> String.format("with document reference '%s' failed", dr))
							.orElse(String.format("of document model '%s' failed", documentModelId))));
			}
			log.trace(String.format("Deserialization of document finished successfully in [%s]", stopWatch.getTime()));
			return document;
		} catch (DocumentSerializationException exception) {
			/**
			 * In case of serialization exception, we must include document reference (if available) in the exception message to improve log messages.
			 * Original stacktrace must be still kept as cause of the new exception.
			 **/
			throw new DataServicesDocumentSerializationException(
				ExceptionCodes.DOCUMENT_MODEL_DE_SERIALIZATION_EXCEPTION_CODE,
				ExceptionKeys.DOCUMENT_CONVERSION_ERROR_KEY,
				String.format("The deserialization of document %s failed. The document will not be available for any data retrieval API", documentReference), exception);
		} catch (UnsupportedOperationException exception) {
			// This should not happen, but we need to handle it anyway in a non-breaking way.
			throw new DataServicesDocumentSerializationException(exception.getMessage()).withAnonymityMessage("Document deserialization failed.");
		}
	}
}
