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
package com.mgmtp.a12.dataservices.document.persistence.internal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.internal.entity.DocumentUniqueConstraintEntity;

public interface DocumentUniqueConstraintJpaRepository extends JpaRepository<DocumentUniqueConstraintEntity, Long> {

	/**
	 * Returns all unique constraint entries associated with the given document reference.
	 *
	 * @param documentReference the document reference identifying the document; must not be `null`.
	 * @return the list of entries for the given document reference; never `null`, may be empty.
	 */
	List<DocumentUniqueConstraintEntity> findByDocumentReference(DocumentReference documentReference);

	/**
	 * Returns all unique constraint entries matching the given model name, constraint name, and field values hash.
	 *
	 * Used for point-in-time uniqueness checks. For authoritative enforcement use `save()` with a
	 * database-level unique index instead.
	 *
	 * @param modelName        the root model name in the hierarchy.
	 * @param constraintName   the name of the unique constraint.
	 * @param valuesHash  the SHA-256 hash of the constrained field values.
	 * @return list of matching entries; empty if the combination does not yet exist.
	 */
	List<DocumentUniqueConstraintEntity> findByModelNameAndConstraintNameAndFieldValuesHash(
		String modelName, String constraintName, String valuesHash);

	/**
	 * Deletes all unique constraint entries associated with the given document reference.
	 *
	 * @param documentReference the document reference identifying the document; must not be `null`.
	 */
	void deleteByDocumentReference(DocumentReference documentReference);

	/**
	 * Deletes all unique constraint entries for the given root model name.
	 *
	 * Called when a topmost Document Model is deleted to remove orphaned tracking rows.
	 *
	 * @param modelName the root model name; must not be `null`.
	 */
	void deleteByModelName(String modelName);

}
