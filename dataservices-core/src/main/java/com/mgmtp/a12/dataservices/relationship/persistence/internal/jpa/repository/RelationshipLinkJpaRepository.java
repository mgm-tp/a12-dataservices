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

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.entity.RelationshipLinkEntity;

public interface RelationshipLinkJpaRepository extends JpaRepository<RelationshipLinkEntity, String> {

	@Query("SELECT count(r) FROM RelationshipLinkEntity r JOIN r.roles e where r.relationshipModel = :relationshipModel AND e.name = :role AND e.docRef = :docRef")
	long countMultiplicityForRole(
		@Param(value = "relationshipModel") String relationshipModel,
		@Param(value = "role") String role,
		@Param(value = "docRef") DocumentReference docRef);

	Page<RelationshipLinkEntity> findByRelationshipModel(String relationshipModelName, Pageable pageable);

	Page<RelationshipLinkEntity> findByLinkDocumentDocRef(DocumentReference linkDocRef, Pageable pageable);

	@Query("SELECT a FROM RelationshipLinkEntity a "
		+ "JOIN a.roles ea "
		+ "WHERE a.relationshipModel = :relationshipModel AND ea.name = :terminatingRoleOpposite "
		+ "  AND NOT EXISTS ( "
		+ "   SELECT 1 FROM RelationshipLinkEntity b "
		+ "   JOIN b.roles eb "
		+ "   WHERE b.relationshipModel = :relationshipModel AND ea.docRef = eb.docRef AND not(ea.name = eb.name))")
	Page<RelationshipLinkEntity> findTerminatingNodes(
		@Param(value = "relationshipModel") String relationshipModel,
		@Param(value = "terminatingRoleOpposite") String terminatingRoleOpposite,
		Pageable pageable
	);

	@Query(value =
		"      SELECT link.id, "
			+ "       link.created_at, "
			+ "       link.link_document_docref, "
			+ "       link.relationship_model,"
			+ "       o1.role_order as source_role_order,  "
			+ "       o2.role_order as target_role_order  "
			+ "FROM relationship_link link "
			+ "         left join relationship_role r1 "
			+ "                   on link.id = r1.relationship_id "
			+ "                       and r1.role_name = :roleName "
			+ "                       and r1.role_docref = :docRef "
			+ "                       and link.relationship_model = :relationshipModel "
			+ "         left join relationship_order o1 "
			+ "                    on o1.id = r1.id "
			+ "         left join relationship_role r2 "
			+ "                   on link.id = r2.relationship_id "
			+ "                       and r2.role_name != :roleName "
			+ "                       and link.relationship_model = :relationshipModel "
			+ "         left join relationship_order o2 "
			+ "                    on o2.id = r2.id "
			+ "WHERE link.relationship_model = :relationshipModel"
			+ "     and r1.role_docref = :docRef"
			+ "     and r1.role_name = :roleName",
		nativeQuery = true)
	Page<RelationshipLinkEntity> findByRelationshipModelAndRoleNameAndRoleDocRef(
		@Param(value = "relationshipModel") String relationshipModel,
		@Param(value = "roleName") String roleName,
		@Param(value = "docRef") String docRef,
		Pageable pageable
	);

	@Query(value =
		"      SELECT link.id, "
			+ "       link.created_at, "
			+ "       link.link_document_docref, "
			+ "       link.relationship_model,"
			+ "       o1.role_order as source_role_order,  "
			+ "       o2.role_order as target_role_order  "
			+ "FROM relationship_link link "
			+ "         left join relationship_role r1 "
			+ "                   on link.id = r1.relationship_id "
			+ "                       and r1.role_name = :sourceRoleName "
			+ "                       and r1.role_docref = :sourceRoleDocRef "
			+ "                       and link.relationship_model = :relationshipModel "
			+ "         left join relationship_order o1 "
			+ "                    on o1.id = r1.id "
			+ "         left join relationship_role r2 "
			+ "                   on link.id = r2.relationship_id "
			+ "                       and r2.role_name != :sourceRoleName "
			+ "                       and link.relationship_model = :relationshipModel "
			+ "         left join relationship_order o2 "
			+ "                    on o2.id = r2.id "
			+ "WHERE link.relationship_model = :relationshipModel"
			+ "     and r2.role_docref = :targetRoleDocRef"
			+ "     and r1.role_docref = :sourceRoleDocRef"
			+ "     and r1.role_name = :sourceRoleName",
		nativeQuery = true)
	Optional<RelationshipLinkEntity> findByRelationshipModelNameAndSourceRoleAndSourceRoleDocRefAndTargetRoleDocRef(
		@Param("relationshipModel") String relationshipModelName,
		@Param("sourceRoleName") String sourceRole,
		@Param("sourceRoleDocRef") String sourceRoleDocRef,
		@Param("targetRoleDocRef") String targetDocRef
	);

	@Query("SELECT r from RelationshipLinkEntity r "
		+ "JOIN r.roles e1 "
		+ "JOIN r.roles e2 "
		+ "where r.relationshipModel = :relationshipModelName"
		+ "  AND e1.name = :role1"
		+ "  AND e1.docRef = :docRef1"
		+ "  AND e2.name = :role2"
		+ "  AND e2.docRef = :docRef2")
	Page<RelationshipLinkEntity> findByRoles(
		@Param(value = "relationshipModelName") String relationshipModelName,
		@Param(value = "role1") String role1,
		@Param(value = "role2") String role2,
		@Param(value = "docRef1") DocumentReference docRef1,
		@Param(value = "docRef2") DocumentReference docRef2,
		Pageable pageable
	);

	@Query("SELECT COUNT(r) from RelationshipLinkEntity r "
		+ "JOIN r.roles e1 "
		+ "JOIN r.roles e2 "
		+ "where r.relationshipModel = :relationshipModelName"
		+ "  AND e1.name = :role1"
		+ "  AND e1.docRef = :docRef1"
		+ "  AND e2.name = :role2"
		+ "  AND e2.docRef = :docRef2")
	long countByRoles(
		@Param(value = "relationshipModelName") String relationshipModelName,
		@Param(value = "role1") String role1,
		@Param(value = "role2") String role2,
		@Param(value = "docRef1") DocumentReference docRef1,
		@Param(value = "docRef2") DocumentReference docRef2);

	long countByRelationshipModel(String relationshipModel);

	Page<RelationshipLinkEntity> findAllByRolesDocRefIn(Collection<DocumentReference> documentReferences, Pageable pageable);

	long countByLinkDocumentDocRefIn(Collection<DocumentReference> documentReferences);

	@Query("SELECT count(r) FROM RelationshipLinkEntity r JOIN r.roles e where e.docRef in (:docRefs)")
	long countNumberOfLinksForDocuments(Collection<DocumentReference> docRefs);
}
