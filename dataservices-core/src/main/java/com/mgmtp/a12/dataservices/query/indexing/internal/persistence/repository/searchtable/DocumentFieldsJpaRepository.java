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
package com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.searchtable;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.searchtable.DocumentFieldEntity;

public interface DocumentFieldsJpaRepository extends JpaRepository<DocumentFieldEntity, Long> {

	/**
	 * = Delete Document Fields by Document Reference
	 *
	 * Deletes all `DocumentFieldEntity` records associated with the given document reference.
	 *
	 * == Purpose
	 *
	 * This explicit `@Query`-based deletion is used instead of a derived query
	 * (`deleteDocumentFieldEntitiesByDocRef`) to avoid Hibernate's default behavior of
	 * performing a *select-before-delete* operation.
	 *
	 * == Important
	 *
	 * *Never use the derived delete query version in concurrent transaction scenarios.*
	 *
	 * The derived version triggers a `SELECT` before each `DELETE`, which can cause
	 * `javax.persistence.OptimisticLockException` under high concurrency when multiple
	 * transactions delete the same records simultaneously.
	 *
	 * This native JPQL `DELETE` executes in a single SQL statement, preventing that issue.
	 *
	 * @param docRef the document reference whose associated field records should be deleted
	 */
	@Modifying
	@Query("DELETE FROM DocumentFieldEntity WHERE docRef = :docRef")
	void deleteDocumentFieldEntitiesByDocRef(@Param("docRef") String docRef);

	@Modifying
	@Query("DELETE FROM DocumentFieldEntity WHERE docRef IN :docRef")
	void deleteDocumentFieldEntitiesByDocRefIn(@Param("docRef") Collection<String> docRefs);

	@Query("select distinct docRef from DocumentFieldEntity where docRef in :docRefs")
	List<String> findDocRefsIn(@Param("docRefs") List<String> docRefs);

	@NativeQuery("SELECT '%' || element || '%' FROM unnest(tsvector_to_array(to_tsvector('simple', ?1))) AS element")
	List<String> convertInputToSearchTerms(String input);

	@Modifying
	@NativeQuery("TRUNCATE TABLE document_fields")
	void truncateTable();

}
