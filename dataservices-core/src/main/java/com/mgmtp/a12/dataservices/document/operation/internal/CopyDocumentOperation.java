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

import java.util.Locale;

import org.springframework.transaction.annotation.Transactional;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.common.exception.BaseException.MessagePriority;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterLoadEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeRepositorySaveEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentAfterRepositoryCreateEvent;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.rpc.RpcExceptionSupport;
import com.mgmtp.a12.dataservices.utils.OperationContextHolder;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Copy the document together with its attachments to a new one with the new {@link DocumentReference}.
 * The attachment IDs stay the same for the new document, but the attachment content is duplicated to be independent of the source document.
 *
 * The operation fires the following events during document creation: <<events,`DocumentBeforeCreateEvent, DocumentAfterCreateEvent, DocumentBeforeRepositorySaveEvent, DocumentAfterLoadEvent`>>.
 */
@Slf4j
@RemoteOperation(name = CoreOperationConstants.COPY_DOCUMENT_OPERATION, group = CoreOperationConstants.DOCUMENT_OPERATIONS_GROUP)
public class CopyDocumentOperation extends AbstractDocumentOperation {

	private static final String COULD_NOT_COPY_DOCUMENT_MESSAGE = "Could not copy document";

	public CopyDocumentOperation(DocumentService documentService, Anonymizer anonymizer) {
		super(documentService, anonymizer);
	}

	/**
	 * @param docRef The {@link DocumentReference} of the source document.
	 * @param locale The locale for document validations and computations.
	 * @return In case no error occurs the response will contain the docRef of the newly created document.
	 * @event {@link DocumentBeforeCreateEvent}
	 * @event {@link DocumentAfterCreateEvent}
	 * @event {@link DocumentBeforeRepositorySaveEvent}
	 * @event {@link DocumentAfterRepositoryCreateEvent}
	 * @event {@link DocumentAfterLoadEvent}
	 */
	@Transactional
	public DocumentReference rpc(@NonNull @JsonRpcParam("docRef") DocumentReference docRef, @JsonRpcParam("locale") Locale locale) {
		log.debug("{} called with parameters [docRef={}, locale={}]", CoreOperationConstants.COPY_DOCUMENT_OPERATION, anonymizer.apply(docRef.toString()),
			locale);
		try {
			DataServicesDocument sourceDocument = documentService.load(docRef)
				.orElseThrow(() -> new NotFoundException(ExceptionKeys.DOCUMENT_NOT_FOUND_ERROR_KEY, String.format("Document [%s] not found", docRef)));
			DataServicesDocument result = documentService.create(sourceDocument.getKernelDocument().withId(null), locale);
			OperationContextHolder.put(result);
			return result.getMetadata().getDocRef();
		} catch (BaseException e) {
			log.info("{} operation failed with the following exception", CoreOperationConstants.COPY_DOCUMENT_OPERATION, e);
			e.updateShortMessage(ExceptionKeys.COPY_DOCUMENT_ERROR_KEY, COULD_NOT_COPY_DOCUMENT_MESSAGE, MessagePriority.LOW);
			throw e;
		} catch (Exception e) {
			log.info("{} operation failed with the following exception", CoreOperationConstants.COPY_DOCUMENT_OPERATION, e);
			throw RpcExceptionSupport.createException(ExceptionKeys.COPY_DOCUMENT_ERROR_KEY, COULD_NOT_COPY_DOCUMENT_MESSAGE,
				e.getMessage(), RemoteOperation.RemoteOperationHelper.getOperationId(this.getClass()));
		}
	}
}
