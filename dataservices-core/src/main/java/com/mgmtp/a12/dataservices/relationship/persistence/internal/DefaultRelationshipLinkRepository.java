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
package com.mgmtp.a12.dataservices.relationship.persistence.internal;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.StreamUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.OffsetBasedPageRequest;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipRole;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.entity.RelationshipLinkEntity;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.entity.RelationshipRoleEntity;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.repository.RelationshipLinkJpaRepository;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.repository.RelationshipRoleJpaRepository;

import jakarta.persistence.EntityManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.relationship.internal.RelationshipSortConstants.ORDER_BY_ROLES_SORT_COLUMNS;

@RequiredArgsConstructor
@Repository public class DefaultRelationshipLinkRepository implements RelationshipLinkRepository {
	private final RelationshipLinkJpaRepository relationshipLinkJpaRepository;
	private final RelationshipRoleJpaRepository relationshipRoleJpaRepository;
	private final EntityManager entityManager;

	@Override public RelationshipLink create(@NonNull RelationshipLink relationshipLink) {
		return save(makeLinkEntity(relationshipLink));
	}

	@Override public RelationshipLink update(@NonNull String id, @NonNull RelationshipLink relationshipLink)
		throws NotFoundException {
		return save(makeLinkEntity(relationshipLink, id));
	}

	@Override public void delete(@NonNull String id) {
		relationshipLinkJpaRepository.deleteById(id);
	}

	@Override public void deleteAllByIds(@NonNull Set<String> ids) {
		relationshipLinkJpaRepository.deleteAllByIdInBatch(ids);
	}

	@Override public Optional<? extends RelationshipLink> findById(@NonNull String id) {
		return relationshipLinkJpaRepository.findById(id);
	}

	@Override public Page<? extends RelationshipLink> findByRelationshipModelName(@NonNull String modelName, Pageable pageable) {
		return relationshipLinkJpaRepository.findByRelationshipModel(modelName, pageable);
	}

	@Transactional
	@Override public Page<? extends RelationshipLink> findByRelationshipModelNameAndSource(
		@NonNull String relationshipModelName,
		@NonNull String sourceRole,
		@NonNull DocumentReference sourceDocRef,
		Pageable pageable
	) {
		return relationshipLinkJpaRepository.findByRelationshipModelAndRoleNameAndRoleDocRef(
			relationshipModelName,
			sourceRole,
			sourceDocRef.toString(),
			pageable
		);
	}

	@Override public Page<? extends RelationshipLink> findByRelationshipModelNameAndSourceAndTarget(
		@NonNull String relationshipModelName,
		@NonNull String sourceRole,
		@NonNull DocumentReference sourceRoleDocRef,
		@NonNull String targetRole,
		@NonNull DocumentReference targetRoleDocRef,
		Pageable pageable
	) {
		return relationshipLinkJpaRepository.findByRoles(
			relationshipModelName,
			sourceRole,
			targetRole,
			sourceRoleDocRef,
			targetRoleDocRef,
			pageable
		);
	}

	@Override public Page<? extends RelationshipLink> findByLinkDocument(@NonNull DocumentReference docRef, Pageable pageable) {
		return relationshipLinkJpaRepository.findByLinkDocumentDocRef(
			docRef,
			pageable
		);
	}

	@Override
	public Page<? extends RelationshipLink> findTerminatingNodes(@NonNull String relationshipModelName, @NonNull String terminatingRoleName,
		Pageable pageable) {
		return relationshipLinkJpaRepository.findTerminatingNodes(
			relationshipModelName,
			terminatingRoleName,
			pageable
		);
	}

	@Override public long countByRole(@NonNull String relationshipModelName, @NonNull String role, @NonNull DocumentReference docRef) {
		return relationshipLinkJpaRepository.countMultiplicityForRole(relationshipModelName, role, docRef);
	}

	@Override public long countByRoles(@NonNull String relationshipModelName, @NonNull RelationshipRole sourceRole, @NonNull RelationshipRole targetRole) {
		return relationshipLinkJpaRepository.countByRoles(relationshipModelName, sourceRole.getName(), targetRole.getName(), sourceRole.getDocRef(),
			targetRole.getDocRef());
	}

	@Override public long countByRelationshipModel(@NonNull String relationshipModelName) {
		return relationshipLinkJpaRepository.countByRelationshipModel(relationshipModelName);
	}

	@Override
	public Page<RelationshipLinkEntity> findAllByRoleDocRef(@NonNull Collection<DocumentReference> documentReferences, Pageable pageable) {
		return relationshipLinkJpaRepository.findAllByRolesDocRefIn(documentReferences, pageable);
	}

	@Override
	public long countByLinkInDocumentDocRefs(@NonNull Collection<DocumentReference> documentReferences) {
		return relationshipLinkJpaRepository.countByLinkDocumentDocRefIn(documentReferences);
	}

	public int reorder(String relationshipModelName, String role, DocumentReference roleDocRef, LongFunction<List<String>> reorderFunction) {
		List<? extends RelationshipRoleEntity> roles = relationshipLinkJpaRepository.findByRelationshipModelAndRoleNameAndRoleDocRef(
				relationshipModelName,
				role,
				roleDocRef.toString(),
				OffsetBasedPageRequest.unpaged(Sort.by(ORDER_BY_ROLES_SORT_COLUMNS))
			)
			.stream()
			.map(RelationshipLinkEntity::getRoles)
			.map(e -> e.get(role))
			.toList();

		List<String> newOrder = reorderFunction.apply(roles.size());
		Assert.isTrue(newOrder.size() == roles.size(),
			String.format("Count of links to order (%s) doesn't match count of new ranks (%s)", roles.size(), newOrder.size()));

		return relationshipRoleJpaRepository.saveAll(StreamUtils.zip(roles.stream(), newOrder.stream(), RelationshipRoleEntity::order)
			.toList()).size();
	}

	@Transactional(readOnly = true)
	public Optional<String> findComplementaryBoundaryOrder(@NonNull String relationshipModelName, @NonNull String sourceRole, @NonNull String targetRole,
		@NonNull DocumentReference targetRoleDocRef) {
		return (relationshipRoleJpaRepository.findComplementaryRoleOrder(relationshipModelName, sourceRole, targetRole, targetRoleDocRef)).stream()
			.findFirst();
	}

	private RelationshipLink save(RelationshipLinkEntity relationshipLinkEntity) {
		if (relationshipLinkEntity.getId() == null) {
			RelationshipLinkEntity result = relationshipLinkJpaRepository.save(relationshipLinkEntity);
			Collection<RelationshipRoleEntity> roleEntity = result.getRoles().values();
			roleEntity.forEach(r -> r.setRelationship(result));
			relationshipRoleJpaRepository.saveAll(roleEntity);
			return result;
		} else {
			entityManager.persist(relationshipLinkEntity);
			relationshipLinkEntity.getRoles()
				.values()
				.forEach(roleEntity -> {
					roleEntity.setRelationship(relationshipLinkEntity);
					entityManager.persist(roleEntity);
				});
			return relationshipLinkEntity;
		}
	}

	private RelationshipLinkEntity makeLinkEntity(RelationshipLink relationshipLink) {
		return makeLinkEntity(relationshipLink, null);
	}

	private RelationshipLinkEntity makeLinkEntity(RelationshipLink relationshipLink, String id) {
		if (relationshipLink instanceof RelationshipLinkEntity relationshipLinkEntity) {
			return relationshipLinkEntity;
		} else {
			return RelationshipLinkEntity.builder()
				.id(id == null ? relationshipLink.getId() : id)
				.relationshipModel(relationshipLink.getRelationshipModel())
				.createdAt(relationshipLink.getCreatedAt())
				.linkDocumentDocRef(relationshipLink.getLinkDocumentDocRef())
				.roles(relationshipLink.getRoles().values().stream()
					.map(this::makeRoleEntity)
					.collect(Collectors.toMap(RelationshipRoleEntity::getName, Function.identity())))
				.build();
		}
	}

	private RelationshipRoleEntity makeRoleEntity(RelationshipRole r) {
		return r instanceof RelationshipRoleEntity relationshipRoleEntity ? relationshipRoleEntity
			: RelationshipRoleEntity.builder()
			.name(r.getName())
			.docRef(r.getDocRef())
			.order(r.getOrder())
			.build();
	}
}
