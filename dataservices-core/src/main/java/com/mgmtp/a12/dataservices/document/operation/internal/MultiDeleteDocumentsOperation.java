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

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Efficiently deletes multiple documents in a single operation. It is designed to optimize multiple calls to {@link com.mgmtp.a12.dataservices.document.operation.internal.DeleteDocumentOperation}.
 * This optimization is achieved by avoiding the retrieval of documents from the database, eliminating the associated performance overhead.
 *
 * If the document is absent, the operation gracefully concludes without triggering any errors, maintaining an idempotent process.
 * Before removing the document itself, all associated relationship links are deleted. However, it's important to note that if the document is used as a link document within a relationship, the operation will encounter a failure.
 *
 * It's important to be aware that this approach comes with trade-offs. Firstly, it bypasses the standard document Attribute-Based Access Control (ABAC) checks, and secondly, it does
 * not provide fine-grained control for selecting the appropriate document repository through the {@link com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository#supports(DocumentV2)} method. Instead, it is required
 * to use {@link com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository#supports(String, Optional)}.
 *
 *
 *
 * @see com.mgmtp.a12.dataservices.document.operation.internal.DeleteDocumentOperation
 * @see com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository
 */
@Slf4j
@RemoteOperation(name = CoreOperationConstants.MULTI_DELETE_DOCUMENTS_OPERATION, group = CoreOperationConstants.DOCUMENT_OPERATIONS_GROUP)
public class MultiDeleteDocumentsOperation extends AbstractDocumentOperation {

	public MultiDeleteDocumentsOperation(DocumentService documentService, Anonymizer anonymizer) {
		super(documentService, anonymizer);
	}

	/**
	 *
	 * @param documentReferences A collection of document references to be deleted.
	 */
	@Transactional public void rpc(@NonNull @JsonRpcParam("docRefs") Collection<DocumentReference> documentReferences) {
		log.debug("{} called with parameters [docRefs={}]",
			CoreOperationConstants.MULTI_DELETE_DOCUMENTS_OPERATION,
			anonymizer.apply(documentReferences.stream()
				.map(Object::toString)
				.collect(Collectors.joining(","))));

		documentService.deleteAll(documentReferences);
	}
}
