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
package com.mgmtp.a12.dataservices.document.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.NonNull;

/**
 * Lowest-level persistence API for loading and managing documents.
 * Enables swapping the persistence layer without affecting higher layers.
 */
public interface IDocumentRepository {

	/**
	 * Checks whether the given document is supported by this repository.
	 *
	 * @param document the document to be evaluated; must not be null
	 * @return `true` if the document is supported; `false` otherwise
	 */
	boolean supports(@NonNull DocumentV2 document);

	/**
	 * Checks whether the specified model with the given modelName is supported by the current repository.
	 * This method is less flexible and powerful than {@link com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository#supports(DocumentV2)},
	 * but it is necessary for cases where document loading is avoided due to performance considerations,
	 * as seen in {@link com.mgmtp.a12.dataservices.document.operation.internal.MultiDeleteDocumentsOperation}.
	 *
	 * @param modelName The name of the model to be checked for repository support.
	 * @param metadata Optional metadata associated with the model.
	 * @return true if documents of the provided model are supported; false otherwise.
	 */
	boolean supports(@NonNull String modelName, @NonNull Optional<String> metadata);

	/**
	 * This API provides loading of all documents of a certain document path
	 *
	 * @param documentReference of document to be loaded
	 * @return document if present
	 */
	Optional<DataServicesDocument> findByDocumentReference(@NonNull DocumentReference documentReference);

	/**
	 * This API provides loading of all documents of a certain model
	 * Without paging this might cause performance issues
	 *
	 * @param modelId for which the documents should be loaded
	 * @return list of all documents of modelId model
	 */
	@NonNull List<DocumentReference> findAllDocRefsForModel(@NonNull String modelId);

	/**
	 * This API is intended to provide paged loading of all documents of a certain model.
	 * Default implementation should be overridden if functionality is needed.
	 *
	 * @param modelId for which the documents should be loaded
	 * @param pageable the page requested
	 * @return Paged list of all documents of modelId model or un-paged list if not overridden
	 */
	// TODO A12S-4116: Remove this insecure default implementation
	@NonNull default List<DocumentReference> findAllDocRefsForModel(@NonNull String modelId, Pageable pageable) {
		return findAllDocRefsForModel(modelId);
	}

	/**
	 * This API provides possible optimizations for loading documents one by one with {@link #findByDocumentReference(DocumentReference)}
	 *
	 * @param docRefs Document references to be loaded
	 * @return List of documents
	 */
	@NonNull List<DataServicesDocument> findDocumentsByDocRefs(@NonNull List<DocumentReference> docRefs);

	/**
	 * Repository level create of a document
	 *
	 * @param dataServicesDocument new document to be created
	 */
	void create(@NonNull DataServicesDocument dataServicesDocument);

	/**
	 * Repository level update of a document
	 *
	 * @param dataServicesDocument new version of a document
	 */
	void update(@NonNull DataServicesDocument dataServicesDocument);

	/**
	 * Repository level delete of a document
	 *
	 * @param documentReference of a document that should be deleted
	 */
	void delete(@NonNull DocumentReference documentReference);

	/**
	 * Delete all documents with associated document references.
	 *
	 * @param modelName Model name of documents to be deleted.
	 * @param documentReferences Document references to be deleted.
	 */
	void deleteAll(@NonNull String modelName, @NonNull Collection<DocumentReference> documentReferences);
}
