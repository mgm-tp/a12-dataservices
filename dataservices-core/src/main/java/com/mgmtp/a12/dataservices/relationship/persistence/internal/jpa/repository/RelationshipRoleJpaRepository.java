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
package com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.DefaultRelationshipLinkRepository;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.entity.RelationshipRoleEntity;

public interface RelationshipRoleJpaRepository extends JpaRepository<RelationshipRoleEntity, String> {


	/**
	 * This is an internal method used by {@link DefaultRelationshipLinkRepository#findComplementaryBoundaryOrder(String, String, DocumentReference)}
	 * to find complementary boundary order.
	 * Since the follow-up ranking is calculated in ascending order, we have to sort descending here to get the largest one on top.
	 */
	@Query("SELECT e2.order "
		+ "FROM RelationshipRoleEntity e2 "
		+ "JOIN e2.relationship r "
		+ "WHERE r.relationshipModel = :relationshipModelName"
		+ "  AND e2.name = :targetRole"
		+ "  AND e2.docRef = :docRef "
		+ "ORDER BY e2.order DESC")
	List<String> findComplementaryRoleOrder(@Param(value = "relationshipModelName") String relationshipModelName,
		@Param(value = "targetRole") String targetRole, @Param(value = "docRef") DocumentReference docRef);
}
