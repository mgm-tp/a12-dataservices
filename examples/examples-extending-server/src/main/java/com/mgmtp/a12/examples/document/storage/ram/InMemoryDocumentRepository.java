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
package com.mgmtp.a12.examples.document.storage.ram;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterDeleteEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterUpdateEvent;
import com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple in-memory {@link com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository} for examples and tests.
 * Stores documents of model {@link #MODEL_NAME} and supports rollback handlers for transactional failures.
 */
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.documents.storage.ram", name = "enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
@Component public class InMemoryDocumentRepository implements IDocumentRepository {

	/**
	 * Supported document model name for this repository.
	 */
	public static final String MODEL_NAME = "BusinessPartner";

	private final Map<DocumentReference, DataServicesDocument> store = new HashMap<>();

	/**
	 * Removes the created document from the store on transaction rollback.
	 *
	 * @param documentAfterCreateEvent the event containing the created document; never null.
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
	public void rollBackOrder(DocumentAfterCreateEvent documentAfterCreateEvent) {
		if (MODEL_NAME.equals(documentAfterCreateEvent.getDataServicesDocument().getMetadata().getDocRef().getDocumentModelName())) {
			store.remove(documentAfterCreateEvent.getDataServicesDocument().getMetadata().getDocRef());
		}
	}

	/**
	 * Restores the previous document state in the store on transaction rollback.
	 *
	 * @param documentAfterCreateEvent the event containing the old and new document; never null.
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
	public void rollBackOrder(DocumentAfterUpdateEvent documentAfterCreateEvent) {
		if (MODEL_NAME.equals(documentAfterCreateEvent.getOldDocument().getMetadata().getDocRef().getDocumentModelName())) {
			store.put(documentAfterCreateEvent.getOldDocument().getMetadata().getDocRef(), documentAfterCreateEvent.getOldDocument());
		}
	}

	/**
	 * Restores the deleted document in the store on transaction rollback.
	 *
	 * @param documentAfterDeleteEvent the event with the deleted document; never null.
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
	public void rollBackOrder(DocumentAfterDeleteEvent documentAfterDeleteEvent) {
		if (MODEL_NAME.equals(documentAfterDeleteEvent.getDataServicesDocument().getMetadata().getDocRef().getDocumentModelName())) {
			store.put(documentAfterDeleteEvent.getDataServicesDocument().getMetadata().getDocRef(), documentAfterDeleteEvent.getDataServicesDocument());
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param document the document to check; never null.
	 * @return `true` if {@link DocumentV2#getDocumentModelId()} equals {@link #MODEL_NAME}.
	 */
	@Override public boolean supports(@NonNull DocumentV2 document) {
		return MODEL_NAME.equals(document.getDocumentModelId());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param modelName the model name to check; never null.
	 * @param metadata optional metadata influencing support; ignored here.
	 * @return `true` if the model name equals {@link #MODEL_NAME}.
	 */
	@Override public boolean supports(@NonNull String modelName, @NonNull Optional<String> metadata) {
		return MODEL_NAME.equals(modelName);
	}

	/**
	 * Stores a new document in memory.
	 *
	 * @param dataServicesDocument the document to store; never null.
	 */
	@Override public void create(@NonNull DataServicesDocument dataServicesDocument) {
		store.put(dataServicesDocument.getMetadata().getDocRef(), dataServicesDocument);
	}

	/**
	 * Updates (overwrites) an existing document in memory.
	 *
	 * @param dataServicesDocument the document to update; never null.
	 */
	@Override public void update(@NonNull DataServicesDocument dataServicesDocument) {
		store.put(dataServicesDocument.getMetadata().getDocRef(), dataServicesDocument);
	}

	/**
	 * Deletes a document from memory.
	 *
	 * @param documentReference the reference of the document to delete; never null.
	 */
	@Override public void delete(@NonNull DocumentReference documentReference) {
		store.remove(documentReference);
	}

	/**
	 * Deletes multiple documents from memory.
	 *
	 * @param modelName the model name; ignored here.
	 * @param documentReferences the references of documents to delete; never null.
	 */
	@Override public void deleteAll(@NonNull String modelName, @NonNull Collection<DocumentReference> documentReferences) {
		documentReferences.forEach(store::remove);
	}

	/**
	 * Finds a document by its reference.
	 *
	 * @param documentReference the reference to look up; never null.
	 * @return an {@link java.util.Optional} with the document if present; otherwise empty.
	 */
	@Override public Optional<DataServicesDocument> findByDocumentReference(@NonNull DocumentReference documentReference) {
		return Optional.ofNullable(store.get(documentReference));
	}

	/**
	 * Lists all document references stored for a given model.
	 *
	 * @param modelId the model identifier to filter by; never null.
	 * @return a list of stored document references for the model; never null.
	 */
	@Override public @NonNull List<DocumentReference> findAllDocRefsForModel(@NonNull String modelId) {
		return store.keySet().stream()
			.filter(e -> Objects.equals(e.getDocumentModelName(), modelId))
			.toList();
	}

	/**
	 * Resolves documents for the given list of references.
	 *
	 * @param docRefs the references to resolve; never null.
	 * @return a list of resolved documents (non-null entries only); never null.
	 */
	@Override public @NonNull List<DataServicesDocument> findDocumentsByDocRefs(@NonNull List<DocumentReference> docRefs) {
		return docRefs.stream()
			.map(store::get)
			.filter(Objects::nonNull)
			.toList();
	}
}
