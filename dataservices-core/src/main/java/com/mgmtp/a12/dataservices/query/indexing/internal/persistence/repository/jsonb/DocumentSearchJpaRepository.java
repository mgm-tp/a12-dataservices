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
package com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.jsonb;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.jsonb.DocumentSearchEntity;

public interface DocumentSearchJpaRepository extends JpaRepository<DocumentSearchEntity, Long> {

	void deleteByDocRef(String docRef);

	void deleteDocumentSearchEntitiesByDocRefIn(Collection<String> docRefs);

	// Wen can't pass an array of DocumentReference as hibernate can not serialize that.
	// Hibernate only passes a native Postgres array if the input is a String array, a List won't be handled correctly.
	// Using EXCEPT is substantially faster than using NOT IN or <> ALL()
	@Query(value =
		"""
			select doc_ref
			from (
			  select t.doc_ref
			  from unnest(:docRefs) as t(doc_ref)
			  except
			  select doc_ref
			  from document_search
			  where model_name in (:modelNames)
			) m
			""", nativeQuery = true)
	List<String> findMissingDocRefs(@Param("modelNames") Collection<String> modelName, @Param("docRefs") String[] docRefs);

	@Modifying
	@Query(value = "TRUNCATE TABLE " + QueryGeneratorConstants.TableNames.DOCUMENT_SEARCH_TABLE_NAME, nativeQuery = true)
	void truncateTable();

}
