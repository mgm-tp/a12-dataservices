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

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.JsonNode;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.common.exception.BaseException.MessagePriority;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.exception.DataServicesDocumentSerializationException;
import com.mgmtp.a12.dataservices.utils.internal.DataServicesDocumentProblemReporterException;
import com.mgmtp.a12.dataservices.document.DocumentReferenceResult;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeRepositorySaveEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentAfterRepositoryCreateEvent;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.rpc.RpcExceptionSupport;
import com.mgmtp.a12.dataservices.utils.OperationContextHolder;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.exception.ExceptionCodes.ACCESS_DENIED_EXCEPTION_CODE;
import static com.mgmtp.a12.dataservices.exception.ExceptionCodes.RPC_ERROR_EXCEPTION_CODE;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ADD_DOCUMENT_ERROR_KEY;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.SECURITY_NOT_AUTHORIZED_ERROR_KEY;

/**
 * Create a new document of particular model. The handling of the document could vary depending on the model, where
 * the {@link com.mgmtp.a12.dataservices.document.DocumentService} will find all implementations of
 * the {@link com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository} and take the first one which supports the document
 * model of the persisted document.
 *
 * The operation fires the following events during document creation: <<events,`DocumentBeforeCreateEvent,DocumentAfterCreateEvent`>>.
 *
 * @important This operation is now used to create a document using JSON format
 * instead of formerly used `/docs/:DOCUMENT_MODEL` endpoint.
 */
@Slf4j
@RemoteOperation(name = CoreOperationConstants.ADD_DOCUMENT_OPERATION, group = CoreOperationConstants.DOCUMENT_OPERATIONS_GROUP)
public class AddDocumentOperation extends AbstractDocumentOperation {

	private static final String COULD_NOT_CREATE_DOCUMENT_MESSAGE = "Could not create document";
	private static final String ADD_DOCUMENT_OPERATION_FAILED = "ADD_DOCUMENT operation failed with the following exception";

	public AddDocumentOperation(DocumentService documentService, Anonymizer anonymizer) {
		super(documentService, anonymizer);
	}

	/**
	 * @param documentModelName Model of the document.
	 * @param documentContent Content of the document.
	 * @param locale The locale for document validations and computations.
	 * @return In case no error occurs the response will contain the docRef of the newly created document.
	 * @event {@link DocumentBeforeCreateEvent}
	 * @event {@link DocumentAfterCreateEvent}
	 * @event {@link DocumentBeforeRepositorySaveEvent}
	 * @event {@link DocumentAfterRepositoryCreateEvent}
	 */
	@Transactional
	public DocumentReferenceResult rpc(@NonNull @JsonRpcParam("documentModelName") String documentModelName,
		@NonNull @JsonRpcParam("document") JsonNode documentContent,
		@JsonRpcParam("locale") Locale locale) {
		log.debug("{} called with parameters [documentModelName={}, locale={}]",
			CoreOperationConstants.ADD_DOCUMENT_OPERATION,
			anonymizer.apply(documentModelName),
			anonymizer.apply(locale != null ? locale.toString() : "")
		);

		try {
			DataServicesDocument result = documentService.create(documentModelName, documentContent, locale);
			OperationContextHolder.put(result);
			return new DocumentReferenceResult(result.getMetadata().getDocRef());
		} catch (DataServicesDocumentProblemReporterException | DataServicesDocumentSerializationException e) {
			// Propagate conversion/deserialization exceptions without message modification (required for batch rollback format)
			log.info(ADD_DOCUMENT_OPERATION_FAILED, e);
			throw e;
		} catch (BaseException e) {
			log.info(ADD_DOCUMENT_OPERATION_FAILED, e);
			e.updateShortMessage(ADD_DOCUMENT_ERROR_KEY, COULD_NOT_CREATE_DOCUMENT_MESSAGE, MessagePriority.LOW);
			if (e.getLongMessage() == null || StringUtils.isBlank(e.getLongMessage().getKey())) {
				e.setLongMessage(new LocalizedEntry(ADD_DOCUMENT_ERROR_KEY, COULD_NOT_CREATE_DOCUMENT_MESSAGE), MessagePriority.LOW);
			}
			throw e;
		} catch (AccessDeniedException e) {
			log.info(ADD_DOCUMENT_OPERATION_FAILED, e);
			throw RpcExceptionSupport.createException(ACCESS_DENIED_EXCEPTION_CODE, SECURITY_NOT_AUTHORIZED_ERROR_KEY,
				COULD_NOT_CREATE_DOCUMENT_MESSAGE, e.getMessage(), RemoteOperation.RemoteOperationHelper.getOperationId(this.getClass()), e);
		} catch (Exception e) {
			log.info(ADD_DOCUMENT_OPERATION_FAILED, e);
			throw RpcExceptionSupport.createException(RPC_ERROR_EXCEPTION_CODE, ADD_DOCUMENT_ERROR_KEY,
				COULD_NOT_CREATE_DOCUMENT_MESSAGE, e.getMessage(), RemoteOperation.RemoteOperationHelper.getOperationId(this.getClass()), e);
		}
	}

}
