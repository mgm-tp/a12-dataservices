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

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.DocumentPart;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterRepositoryLoadEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeRepositorySaveEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentAfterRepositoryUpdateEvent;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.rpc.RpcExceptionSupport;
import com.mgmtp.a12.dataservices.rpc.internal.RpcDocRefParser;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Modify an existing document by specifying the parts that will be added, modified, or deleted.
 *
 * Possible changes are:
 *
 * * Altering the value of an existing field, including setting it to null (which results in deletion).
 * * Adding a new field within an existing group.
 * * Adding a new field within a non-existent group, which implicitly creates all missing groups.
 * * Deleting a group or field.
 *
 * The operation fires the following events during modification of the document: <<events,`DocumentBeforeUpdateEvent, DocumentAfterUpdateEvent,
 * DocumentAfterRepositoryLoadEvent`>>.
 */
@Slf4j
@RemoteOperation(name = CoreOperationConstants.PARTIAL_MODIFY_DOCUMENT_OPERATION, group = CoreOperationConstants.DOCUMENT_OPERATIONS_GROUP)
public class PartialModifyDocumentOperation extends AbstractDocumentOperation {

	public PartialModifyDocumentOperation(DocumentService documentService, Anonymizer anonymizer) {
		super(documentService, anonymizer);
	}

	/**
	 * @param documentReference The reference to the document that should be updated in the format `DocumentModel/DocumentId`.
	 * @param documentPart A {@link Set} of {@link DocumentPart} describing the entity instances to be changed.
	 * A DocumentPart encapsulates information about a specific segment or attribute within a document, defining how it should be altered.
	 *
	 *  It consists of:
	 *
	 * * path: The path to the segment or attribute within the document structure.
	 * * value: The new value to be assigned to the specified segment or attribute. This can be null, indicating deletion or removal.
	 * * repetitions: An optional array specifying the repetition indices for multivalued attributes.
	 *
	 * Example:
	 * [source, json]
	 * ----
	 * "documentPart": [
	 *     {
	 *         "path": "/Person/PersonalData/Nationality",
	 *         "value": "German",
	 *         "repetitions": [1,1,1]
	 *     }
	 * ]
	 * ----
	 *
	 * @param locale The locale against which the document will be validated (language of the locale must be present in
	 * the language definition of the document model).
	 * @event {@link DocumentBeforeRepositorySaveEvent}
	 * @event {@link DocumentAfterRepositoryUpdateEvent}
	 * @event {@link DocumentAfterRepositoryLoadEvent}
	 */
	@Transactional
	public void rpc(@JsonRpcParam("docRef") String docRef,
		@NonNull @JsonRpcParam("documentPart") List<DocumentPart> documentPart, @JsonRpcParam("locale") Locale locale) {
		DocumentReference documentReference = RpcDocRefParser.parseDocRef(docRef);
		log.debug("{} called with parameters [docRef={}, documentPart={}, locale={}]",
			CoreOperationConstants.PARTIAL_MODIFY_DOCUMENT_OPERATION,
			anonymizer.apply(documentReference.toString()),
			anonymizer.apply(documentPart.toString()),
			anonymizer.apply(locale != null ? locale.toString() : "")
		);
		try {
			documentService.update(documentReference, documentPart, locale);
		} catch (NotFoundException notFoundEx) {
			throw RpcExceptionSupport.createException(notFoundEx.getCode(), ExceptionKeys.MODIFY_DOCUMENT_NOT_FOUND_ERROR_KEY,
				"Document [%s] was not found".formatted(documentReference),
				notFoundEx.getMessage(), RemoteOperation.RemoteOperationHelper.getOperationId(this.getClass()));
		}
	}

}
