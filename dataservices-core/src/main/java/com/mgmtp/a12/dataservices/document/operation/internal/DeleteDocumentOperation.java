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
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterDeleteEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterRepositoryLoadEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeDeleteEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentAfterRepositoryDeleteEvent;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Delete an existing document. If the document doesn't exist, the operation silently finishes without error to achieve idempotent behavior.
 *
 * All relationship links in which the document participates will be deleted before the deletion of the document itself.
 *
 * The operation fires the following events during document deletion: <<events,`DocumentBeforeDeleteEvent, DocumentAfterDeleteEvent, DocumentAfterRepositoryLoadEvent`>>.
 */
@Slf4j
@RemoteOperation(name = CoreOperationConstants.DELETE_DOCUMENT_OPERATION, group = CoreOperationConstants.DOCUMENT_OPERATIONS_GROUP)
public class DeleteDocumentOperation extends AbstractDocumentOperation {

	public DeleteDocumentOperation(DocumentService documentService, Anonymizer anonymizer) {
		super(documentService, anonymizer);
	}

	/**
	 * @param docRef Reference to the document that should be updated in format `DocumentModel/DocumentId`.
	 * @param locale The locale against which the document will be validated.
	 * @event {@link DocumentBeforeDeleteEvent}
	 * @event {@link DocumentAfterDeleteEvent}
	 * @event {@link DocumentAfterRepositoryDeleteEvent}
	 * @event {@link DocumentAfterRepositoryLoadEvent}
	 */
	@Transactional
	public void rpc(@NonNull @JsonRpcParam("docRef") DocumentReference docRef, @JsonRpcParam("locale") Locale locale) {
		log.debug("{} called with parameters [docRef={}, locale={}]",
			CoreOperationConstants.DELETE_DOCUMENT_OPERATION,
			anonymizer.apply(docRef.toString()),
			anonymizer.apply(locale != null ? locale.toString() : "")
		);

		documentService.delete(docRef);
	}

}
