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
import java.util.Locale;

import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterRepositoryLoadEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeRepositorySaveEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentAfterRepositoryUpdateEvent;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.rpc.RpcExceptionSupport;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Update the content of the document.
 *
 * The operation fires the following events during document modification: <<events,`DocumentBeforeUpdateEvent, DocumentAfterUpdateEvent, DocumentAfterRepositoryLoadEvent`>>.
 */
@Slf4j
@RemoteOperation(name = CoreOperationConstants.MODIFY_DOCUMENT_OPERATION, group = CoreOperationConstants.DOCUMENT_OPERATIONS_GROUP)
public class ModifyDocumentOperation extends AbstractDocumentOperation {

	private final DocumentSupport documentSupport;

	public ModifyDocumentOperation(DocumentService documentService, Anonymizer anonymizer, DocumentSupport documentSupport) {
		super(documentService, anonymizer);
		this.documentSupport = documentSupport;
	}

	/**
	 * @param documentReference The reference to the document that should be updated in the format `DocumentModel/DocumentId`.
	 * @param documentContent A document in JSON format.
	 * @param locale The locale against which the document will be validated (language of the locale must be present in
	 * the language definition of the document model).
	 * @event {@link DocumentBeforeRepositorySaveEvent}
	 * @event {@link DocumentAfterRepositoryUpdateEvent}
	 * @event {@link DocumentAfterRepositoryLoadEvent}
	 */
	@Transactional
	public void rpc(@NonNull @JsonRpcParam("docRef") DocumentReference documentReference,
		@NonNull @JsonRpcParam("document") JsonNode documentContent, @JsonRpcParam("locale") Locale locale) {
		log.debug("{} called with parameters [docRef={}, locale={}]",
			CoreOperationConstants.MODIFY_DOCUMENT_OPERATION,
			anonymizer.apply(documentReference.toString()),
			anonymizer.apply(locale != null ? locale.toString() : "")
		);
		try (StringReader reader = new StringReader(documentContent.toString())) {
			DocumentV2 document = documentSupport.convertJSONToDocument(documentReference.getDocumentModelName(), reader, documentReference);

			documentService.update(documentReference, document, locale);
		} catch (NotFoundException notFoundEx) {
			throw RpcExceptionSupport.createException(notFoundEx.getCode(), ExceptionKeys.MODIFY_DOCUMENT_NOT_FOUND_ERROR_KEY,
				String.format("Document [%s] was not found", documentReference),
				notFoundEx.getMessage(), RemoteOperation.RemoteOperationHelper.getOperationId(this.getClass()), notFoundEx);
		}
	}

}
