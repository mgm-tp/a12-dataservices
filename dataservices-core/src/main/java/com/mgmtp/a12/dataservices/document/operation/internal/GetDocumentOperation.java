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

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.DocumentSpec;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterLoadEvent;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.document.operation.events.GetDocumentAfterEvent;
import com.mgmtp.a12.dataservices.document.operation.events.GetDocumentBeforeEvent;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.utils.OperationContextHolder;
import com.mgmtp.a12.dataservices.utils.internal.LoadedDocumentReferencesContextHolder;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Get {@link DocumentSpec} of the document by its {@link DocumentReference}.
 * This operation is designed especially for getting the content of a single document.
 */
@Slf4j
@RemoteOperation(name = CoreOperationConstants.GET_DOCUMENT_OPERATION, group = CoreOperationConstants.DOCUMENT_OPERATIONS_GROUP)
public class GetDocumentOperation extends AbstractDocumentOperation {

	private final DocumentSupport documentSupport;
	private final ApplicationEventPublisher eventPublisher;

	public GetDocumentOperation(DocumentService documentService, Anonymizer anonymizer, DocumentSupport documentSupport, ApplicationEventPublisher eventPublisher) {
		super(documentService, anonymizer);
		this.documentSupport = documentSupport;
		this.eventPublisher = eventPublisher;
	}

	/**
	 * @param docRef The {@link DocumentReference} of requested document.
	 * @return An object of type {@link DocumentSpec} with properties:
	 * `docRef`:: type DocumentReference,
	 * `documentModelName`:: type String,
	 * `document`:: type Document.
	 * @event {@link GetDocumentBeforeEvent}
	 * @event {@link GetDocumentAfterEvent}
	 * @event {@link DocumentAfterLoadEvent}
	 */
	@Transactional(readOnly = true)
	public DocumentSpec rpc(@NonNull @JsonRpcParam("docRef") DocumentReference docRef) {
		log.debug("{} called with parameters [docRef={}]", CoreOperationConstants.GET_DOCUMENT_OPERATION, anonymizer.anonymize(docRef));
		GetDocumentBeforeEvent beforeEvent = new GetDocumentBeforeEvent(docRef);
		eventPublisher.publishEvent(beforeEvent);
		DataServicesDocument dataServicesDocument = documentService.load(docRef)
			.orElseThrow(() -> new NotFoundException(ExceptionKeys.DOCUMENT_NOT_FOUND_ERROR_KEY, String.format("Document [%s] not found", docRef)));
		OperationContextHolder.put(dataServicesDocument);
		DocumentSpec documentSpec = documentSupport.convertToDocumentSpec(dataServicesDocument);
		LoadedDocumentReferencesContextHolder.addDocumentReference(documentSpec);
		eventPublisher.publishEvent(new GetDocumentAfterEvent(documentSpec));
		return documentSpec;
	}
}
