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
package com.mgmtp.a12.dataservices.document.operation.internal;

import java.io.StringReader;
import java.util.List;
import java.util.Locale;

import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.JsonNode;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.DocumentValidationResult;
import com.mgmtp.a12.dataservices.document.internal.kernel.KernelDocumentService;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.document.operation.validate.ValidateDocumentResult;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.utils.OperationContextHolder;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Validate the JSON document.
 *
 * @note This operation results in the Data Services JSON version of the Kernel validation result
 * {@link IDocumentValidationResult} from artifact `kernel-md-runtime-api`.
 * This is done so that the result of the operation will not be directly dependent on the Kernel API.
 * This means that any change of the kernel interfaces will not lead to breaking changes in the result of the operation.
 * @note It is possible to also include a custom condition to the validation by implementing
 * `com.mgmtp.a12.kernel.md.rt.api.ICustomConditionFactory` interface as a bean. Spring will discover all beans of `ICustomConditionFactory`
 * interface and inject them to the Kernel validation engine. For more information about Custom conditions please see
 * Kernel documentation.
 *
 */
@Slf4j
@RemoteOperation(name = CoreOperationConstants.VALIDATE_DOCUMENT_OPERATION, group = CoreOperationConstants.DOCUMENT_OPERATIONS_GROUP, isMutation = false)
public class ValidateDocumentOperation extends AbstractDocumentOperation {

	private final KernelDocumentService kernelDocumentService;
	private final IDocumentV2Serializer documentV2Serializer;
	private final DocumentDeserializationConfig documentJsonDeserializationConfig;

	public ValidateDocumentOperation(DocumentService documentService, Anonymizer anonymizer, KernelDocumentService kernelDocumentService,
		DocumentDeserializationConfig documentJsonDeserializationConfig,
		IDocumentV2Serializer documentV2Serializer) {
		super(documentService, anonymizer);
		this.kernelDocumentService = kernelDocumentService;
		this.documentJsonDeserializationConfig = documentJsonDeserializationConfig;
		this.documentV2Serializer = documentV2Serializer;
	}

	/**
	 * @param documentModelName The document model name of the document to validate.
	 * @param documentContent A document in JSON format.
	 * @param partial Non-mandatory boolean flag indicating that the document has been provided partially, which is supposed
	 * to be considered during validation. By default, full document validation will be executed.
	 * @param locale The locale against which the document will be validated (language of the locale must be present in
	 * the language definition of the document model).
	 * @return The result is a list of `DocumentValidationError` which contains:
	 *
	 * `errorText`:: a string mapped from Kernel error text,
	 * `errorCode`:: a string mapped from Kernel error code,
	 * `messageType`:: a string mapped from Kernel message type,
	 * `rulePath`:: a string mapped from Kernel rule path,
	 * `referencedFields`:: a list of referenced field.
	 */
	@Transactional(readOnly = true)
	public List<DocumentValidationResult> rpc(@NonNull @JsonRpcParam("documentModelName") String documentModelName,
		@NonNull @JsonRpcParam("document") JsonNode documentContent, @JsonRpcParam("partial") Boolean partial,
		@JsonRpcParam("locale") Locale locale) {
		log.debug("{} called with parameters [documentModelName={}, locale={}, partial={}]",
			CoreOperationConstants.VALIDATE_DOCUMENT_OPERATION,
			anonymizer.apply(documentModelName),
			anonymizer.apply(locale != null ? locale.toString() : ""),
			anonymizer.apply(partial != null ? partial.toString() : "")
		);
		StringReader reader = new StringReader(documentContent.toString());
		ListIProblemReporter pr = new ListIProblemReporter();
		DocumentV2 document = documentV2Serializer.deserializeV2(reader, documentModelName, documentJsonDeserializationConfig, pr);

		pr.validate(ExceptionCodes.DOCUMENT_CONVERSION_EXCEPTION_CODE, ExceptionKeys.DOCUMENT_CONVERSION_ERROR_KEY, "Error while document deserialization");

		IDocumentValidationResult documentValidationResult = Boolean.TRUE.equals(partial)
			? kernelDocumentService.validatePartially(document, locale)
			: kernelDocumentService.validateFull(document, locale);

		OperationContextHolder.put(document);
		return new ValidateDocumentResult(documentValidationResult).getValidationErrors();
	}
}
