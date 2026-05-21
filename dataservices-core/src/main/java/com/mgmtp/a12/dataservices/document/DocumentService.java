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
package com.mgmtp.a12.dataservices.document;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.mgmtp.a12.dataservices.document.persistence.DocumentComputationStrategy;
import com.mgmtp.a12.dataservices.document.persistence.DocumentValidationStrategy;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.NonNull;

/**
 * Service API for creating, updating, loading, and deleting documents supported by a persister.
 * The implementation ensures that operations run only for supported document models and handle transactional consistency.
 */
public interface DocumentService {

	/**
	 * Creation of document.
	 *
	 * This method is executed within a JPA transaction. Thus, if an error occurs during execution,
	 * all changes are rolled back in the database.
	 *
	 * @param document kernel {@link DocumentV2} representing the new document
	 * @param locale for kernel error messages
	 * @return Data Services wrapper of new document
	 * @authorizationScope Document Create
	 * @authorizationResource DataServicesDocument
	 */
	DataServicesDocument create(@NonNull DocumentV2 document, Locale locale);

	/**
	 * Creates a new document with a specified validation strategy.
	 * This method executes within a JPA transaction. If an error occurs during execution,
	 * all database changes are automatically rolled back.
	 *
	 * @param document kernel {@link DocumentV2} representing the new document
	 * @param locale for kernel error messages
	 * @param validationStrategy The strategy to apply for document validation
	 * @param computationStrategy The strategy to apply for document computation
	 * @return Data Services wrapper for the new document
	 *
	 * @authorizationScope Document Create
	 * @authorizationResource DataServicesDocument
	 */
	DataServicesDocument create(@NonNull DocumentV2 document, Locale locale, @NonNull DocumentValidationStrategy validationStrategy, @NonNull DocumentComputationStrategy computationStrategy);

	/**
	 * Secure update of a document.
	 *
	 * This method is executed within a JPA transaction. Thus, if an error occurs during execution,
	 * all changes are rolled back in the database.
	 *
	 * @param documentReference of existing document
	 * @param newDocument kernel {@link DocumentV2} representing the updated document
	 * @param locale for kernel error messages
	 * @return Data Services wrapper of updated document
	 *
	 * @authorizationScope Document Update
	 * @authorizationResource DataServicesDocument
	 */
	DataServicesDocument update(@NonNull DocumentReference documentReference, @NonNull DocumentV2 newDocument, Locale locale);

	/**
	 * Secure update of a document with a specified validation strategy.
	 * This method is executed within a specified JPA transaction. If an error occurs during execution,
	 * all database changes are automatically rolled back.
	 *
	 * @param documentReference Reference of existing document
	 * @param newDocument kernel {@link DocumentV2} representing the updated document
	 * @param locale for kernel error messages
	 * @param validationStrategy The strategy to apply for document validation
	 * @param computationStrategy The strategy to apply for document computation
	 * @return Data Services wrapper of updated document
	 *
	 * @authorizationScope Document Update
	 * @authorizationResource DataServicesDocument
	 */
	DataServicesDocument update(@NonNull DocumentReference documentReference, @NonNull DocumentV2 newDocument, Locale locale, @NonNull DocumentValidationStrategy validationStrategy, @NonNull DocumentComputationStrategy computationStrategy);

	/**
	 * Secure update of partial document.
	 *
	 * This method is executed within a JPA transaction. Thus, if an error occurs during execution,
	 * all changes are rolled back in the database.
	 *
	 * @param documentReference of existing document.
	 * @param documentParts partial document consisting of several document parts that should be updated.
	 * @param locale for kernel error messages.
	 * @return Data Services wrapper of updated document.
	 *
	 * @authorizationScope Document Partial Update
	 * @authorizationResource DataServicesDocument
	 */
	DataServicesDocument update(@NonNull DocumentReference documentReference, @NonNull List<DocumentPart> documentParts, Locale locale);

	/**
	 * Secure update of a document with a specified validation strategy.
	 * This method is executed within a specified JPA transaction. If an error occurs during execution,
	 * all database changes are automatically rolled back.
	 *
	 * @param documentReference Reference of existing document.
	 * @param documentParts partial document consisting of several document parts that should be updated.
	 * @param locale for kernel error messages.
	 * @param validationStrategy The strategy to apply for document validation.
	 * @param computationStrategy The strategy to apply for document computation.
	 * @return Data Services wrapper of updated document.
	 *
	 * @authorizationScope Document Update
	 * @authorizationResource DataServicesDocument
	 */
	DataServicesDocument update(@NonNull DocumentReference documentReference, @NonNull List<DocumentPart> documentParts, Locale locale,
		@NonNull DocumentValidationStrategy validationStrategy, @NonNull DocumentComputationStrategy computationStrategy);

	/**
	 *
	 * Secure load of a document.
	 *
	 * @param documentReference of existing document
	 * @return Data Services document
	 *
	 * @authorizationScope Query
	 * @authorizationResource DataServicesDocument
	 * @deprecated since 38.0.1 please use {@link com.mgmtp.a12.dataservices.query.QueryService#query(QueryRoot, String)} instead.
	 * Construct your query root with `exact_match` operator constraint to retrieve document by `/__meta/docRef` field.
	 */
	@Deprecated(since = "38.0.1")
	Optional<DataServicesDocument> load(@NonNull DocumentReference documentReference);

	/**
	 * Secure delete of a document.
	 *
	 * @param documentReference Data Services wrapper of a document to be deleted
	 *
	 * @authorizationScope Document Delete
	 * @authorizationResource DataServicesDocument
	 */
	void delete(@NonNull DocumentReference documentReference);

	/**
	 * Secure delete of a document references collection.
	 *
	 * This method is executed within a JPA transaction. Thus, if an error occurs during execution,
	 * all changes are rolled back in the database.
	 *
	 * @param documentReferences Document references to be deleted.
	 */
	void deleteAll(@NonNull Collection<DocumentReference> documentReferences);
}
