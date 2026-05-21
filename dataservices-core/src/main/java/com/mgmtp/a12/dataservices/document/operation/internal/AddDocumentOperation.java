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

import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.common.exception.BaseException.MessagePriority;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeRepositorySaveEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentAfterRepositoryCreateEvent;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.rpc.RpcExceptionSupport;
import com.mgmtp.a12.dataservices.utils.OperationContextHolder;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Create a new document of particular model. The handling of the document could vary depending on the model, where
 * the {@link com.mgmtp.a12.dataservices.document.DocumentService} will find all implementations of
 * the {@link com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository} and take the first one which  supports the document
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
	private final DocumentSupport documentSupport;

	public AddDocumentOperation(DocumentService documentService, Anonymizer anonymizer, DocumentSupport documentSupport) {
		super(documentService, anonymizer);
		this.documentSupport = documentSupport;
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
	public DocumentReference rpc(@NonNull @JsonRpcParam("documentModelName") String documentModelName,
		@NonNull @JsonRpcParam("document") JsonNode documentContent,
		@JsonRpcParam("locale") Locale locale) {
		log.debug("{} called with parameters [documentModelName={}, locale={}]",
			CoreOperationConstants.ADD_DOCUMENT_OPERATION,
			anonymizer.apply(documentModelName),
			anonymizer.apply(locale != null ? locale.toString() : "")
		);

		try (StringReader jsonDocument = new StringReader(documentContent.toString())) {
			DocumentV2 document = documentSupport.convertJSONToDocument(documentModelName, jsonDocument);
			try {
				DataServicesDocument result = documentService.create(document, locale);
				OperationContextHolder.put(result);
				return result.getMetadata().getDocRef();
			} catch (BaseException e) {
				log.info("ADD_DOCUMENT operation failed with the following exception", e);
				e.updateShortMessage(ExceptionKeys.ADD_DOCUMENT_ERROR_KEY, COULD_NOT_CREATE_DOCUMENT_MESSAGE, MessagePriority.LOW);
				if (e.getLongMessage() == null || StringUtils.isBlank(e.getLongMessage().getKey())) {
					e.setLongMessage(new LocalizedEntry(ExceptionKeys.ADD_DOCUMENT_ERROR_KEY, COULD_NOT_CREATE_DOCUMENT_MESSAGE), MessagePriority.LOW);
				}
				throw e;
			} catch (Exception e) {
				log.info("ADD_DOCUMENT operation failed with the following exception", e);
				throw RpcExceptionSupport.createException(ExceptionCodes.RPC_ERROR_EXCEPTION_CODE, ExceptionKeys.ADD_DOCUMENT_ERROR_KEY,
					COULD_NOT_CREATE_DOCUMENT_MESSAGE, e.getMessage(), RemoteOperation.RemoteOperationHelper.getOperationId(this.getClass()), e);
			}

		}
	}

}
