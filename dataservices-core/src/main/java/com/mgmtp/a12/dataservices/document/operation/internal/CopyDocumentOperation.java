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

import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.common.exception.BaseException.MessagePriority;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentReferenceResult;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterLoadEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeRepositorySaveEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentAfterRepositoryCreateEvent;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.rpc.RpcExceptionSupport;
import com.mgmtp.a12.dataservices.rpc.internal.RpcDocRefParser;
import com.mgmtp.a12.dataservices.utils.OperationContextHolder;

import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.exception.ExceptionCodes.ACCESS_DENIED_EXCEPTION_CODE;
import static com.mgmtp.a12.dataservices.exception.ExceptionCodes.RPC_ERROR_EXCEPTION_CODE;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.COPY_DOCUMENT_ERROR_KEY;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.SECURITY_NOT_AUTHORIZED_ERROR_KEY;

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
	private static final String OPERATION_FAILED_WITH_EXCEPTION = "COPY_DOCUMENT operation failed with the following exception";

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
	public DocumentReferenceResult rpc(@JsonRpcParam("docRef") String docRef, @JsonRpcParam("locale") Locale locale) {
		DocumentReference documentReference = RpcDocRefParser.parseDocRef(docRef);
		log.debug("{} called with parameters [docRef={}, locale={}]", CoreOperationConstants.COPY_DOCUMENT_OPERATION, anonymizer.apply(documentReference.toString()),
			locale);

		try {
			DataServicesDocument result = documentService.copy(documentReference, locale);
			OperationContextHolder.put(result);
			return new DocumentReferenceResult(result.getMetadata().getDocRef());
		} catch (BaseException e) {
			log.info(OPERATION_FAILED_WITH_EXCEPTION, e);
			e.updateShortMessage(COPY_DOCUMENT_ERROR_KEY, COULD_NOT_COPY_DOCUMENT_MESSAGE, MessagePriority.LOW);
			throw e;
		} catch (AccessDeniedException e) {
			log.info(OPERATION_FAILED_WITH_EXCEPTION, e);
			throw RpcExceptionSupport.createException(ACCESS_DENIED_EXCEPTION_CODE, SECURITY_NOT_AUTHORIZED_ERROR_KEY,
				COULD_NOT_COPY_DOCUMENT_MESSAGE, e.getMessage(), RemoteOperation.RemoteOperationHelper.getOperationId(this.getClass()), e);
		} catch (Exception e) {
			log.info(OPERATION_FAILED_WITH_EXCEPTION, e);
			throw RpcExceptionSupport.createException(RPC_ERROR_EXCEPTION_CODE, COPY_DOCUMENT_ERROR_KEY, COULD_NOT_COPY_DOCUMENT_MESSAGE,
				e.getMessage(), RemoteOperation.RemoteOperationHelper.getOperationId(this.getClass()));
		}
	}
}
