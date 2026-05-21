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
package com.mgmtp.a12.dataservices.relationship.internal.ranks;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.relationship.OffsetBasedPageRequest;
import com.mgmtp.a12.dataservices.relationship.Order;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipRole;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.DefaultRelationshipLinkRepository;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;
import com.mgmtp.a12.dataservices.rpc.RpcExceptionSupport;

import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.relationship.Order.ASC;
import static com.mgmtp.a12.dataservices.relationship.Order.DESC;
import static com.mgmtp.a12.dataservices.relationship.internal.RelationshipSortConstants.ORDER_BY_ROLES_SORT_COLUMNS;
import static com.mgmtp.a12.dataservices.relationship.spec.LinkPosition.BOTTOM;
import static com.mgmtp.a12.dataservices.relationship.spec.LinkPosition.TOP;

/**
 * Service interface for managing relationship ranks within a system.
 * This interface defines methods for calculating and updating ranks associated with
 * relationship links.
 **/
@RequiredArgsConstructor
public class RelationshipRankService {
	private final DefaultRelationshipLinkRepository relationshipLinkRepository;

	private static String extractOrder(Optional<? extends RelationshipLink> link, String role) {
		return link
			.map(RelationshipLink::getRoles)
			.map(e -> e.get(role))
			.map(RelationshipRole::getOrder)
			.orElse(null);
	}

	/**
	 * Refreshes the ranks of relationship links associated with the specified role in the document
	 * referenced by the given relationship model name and document reference.
	 *
	 * @param relationshipModelName the name of the relationship model
	 * @param role the role name
	 * @param docRef the document reference
	 * @return the number of relationship ranks refreshed
	 */
	@Transactional
	public int refreshRanks(String relationshipModelName, String role, DocumentReference docRef) {
		return relationshipLinkRepository.reorder(relationshipModelName, role, docRef, RelationshipRankComputer::spreadEqually);
	}

	/**
	 * Refreshes the ranks of relationship links associated with the specified relationship model name.
	 *
	 * @param relationshipModelName the name of the relationship model
	 * @return the number of relationship ranks refreshed
	 */
	@Transactional
	public long refreshRanks(String relationshipModelName) {
		return relationshipLinkRepository.findByRelationshipModelName(relationshipModelName, null).stream()
			.flatMap(this::entitiesToTripleWithRmModelName)
			.distinct()
			.mapToLong(t -> refreshRanks(t.getLeft(), t.getMiddle(), t.getRight()))
			.sum();
	}

	/**
	 * Computes the rank for the specified relationship link, representing the order of a relationship.
	 *
	 * @param linkDescriptor the relationship link description
	 * @return the computed rank as a string representation
	 */
	@Transactional
	public ComputedRank computeRank(LinkDescriptor linkDescriptor) {
		String orderRank = computeOrderRank(linkDescriptor);
		String complementaryOrderRank = computeComplementaryOrderRank(linkDescriptor);

		return new ComputedRank(orderRank, complementaryOrderRank);
	}

	private String computeOrderRank(LinkDescriptor linkDescriptor) {
		RelationshipRoleSpec sourceRole = linkDescriptor.getSourceRole();
		return findNextAvailableRank(linkDescriptor.getRelationshipModel(), sourceRole.getRole(), sourceRole.getDocRef(), findRangeForOrderRank(linkDescriptor));
	}

	private String computeComplementaryOrderRank(LinkDescriptor linkDescriptor) {
		String relationshipModelName = linkDescriptor.getRelationshipModel();
		RelationshipRoleSpec sourceRole = linkDescriptor.getSourceRole();
		RelationshipRoleSpec targetRole = linkDescriptor.getTargetRole();
		DocumentReference boundaryDocRef = findBoundaryRelationshipLinkEntity(relationshipModelName, sourceRole.getDocRef(), sourceRole.getRole(), DESC)
			.map(RelationshipLink::getRoles)
			.map(e -> e.get(sourceRole.getRole()))
			.map(RelationshipRole::getDocRef)
			.orElse(null);

		String boundaryComplementaryOrder =
			relationshipLinkRepository.findComplementaryBoundaryOrder(relationshipModelName, sourceRole.getRole(), targetRole.getRole(), sourceRole.getDocRef()).orElse(null);

		return findNextAvailableRank(relationshipModelName, sourceRole.getRole(), boundaryDocRef, new ImmutablePair<>(boundaryComplementaryOrder, null));
	}

	private Pair<String, String> findRangeForOrderRank(LinkDescriptor linkDescriptor) {
		String relationshipModelName = linkDescriptor.getRelationshipModel();
		String predecessorLinkRef = linkDescriptor.getPredecessorLinkRef();
		RelationshipRoleSpec sourceRole = linkDescriptor.getSourceRole();
		if (StringUtils.isNotBlank(predecessorLinkRef)) {
			if (relationshipLinkRepository.countByRole(relationshipModelName, sourceRole.getRole(), sourceRole.getDocRef()) == 0) {
				// init set
				return new ImmutablePair<>(null, null);
			} else {
				Optional<Pair<? extends RelationshipLink, ? extends RelationshipLink>> linksInBetween = Optional.of(
						findLinksInBetween(
							relationshipModelName,
							sourceRole.getRole(),
							sourceRole.getDocRef(),
							predecessorLinkRef
						)
				);
				return new ImmutablePair<>(
					extractOrder(linksInBetween.map(Pair::getLeft), sourceRole.getRole()),
					extractOrder(linksInBetween.map(Pair::getRight), sourceRole.getRole()));
			}
		} else if (BOTTOM.equals(linkDescriptor.getPosition())) {
			return new ImmutablePair<>(extractOrder(findBoundaryRelationshipLinkEntity(relationshipModelName, sourceRole.getDocRef(), sourceRole.getRole(), DESC), sourceRole.getRole()), null);
		} else if (TOP.equals(linkDescriptor.getPosition())) {
			return new ImmutablePair<>(null, extractOrder(findBoundaryRelationshipLinkEntity(relationshipModelName, sourceRole.getDocRef(), sourceRole.getRole(), ASC), sourceRole.getRole()));
		} else {
			throw new IllegalStateException();
		}
	}

	private Optional<? extends RelationshipLink> findBoundaryRelationshipLinkEntity(
		String relationshipModelName,
		DocumentReference roleDocRef,
		String role,
		Order order
	) {
		Page<? extends RelationshipLink> page = relationshipLinkRepository.findByRelationshipModelNameAndSource(
			relationshipModelName,
			role,
			roleDocRef,
			OffsetBasedPageRequest.ofOffset(0, 1, Sort.by(Arrays.stream(ORDER_BY_ROLES_SORT_COLUMNS).map(order::getOrder).toList()))
		);

		return page.getTotalElements() > 0 ? Optional.of(page.getContent().get(0)) : Optional.empty();
	}

	private Pair<? extends RelationshipLink, ? extends RelationshipLink> findLinksInBetween(
		String relationshipModelName,
		String sourceRole,
		DocumentReference docRef,
		String predecessorRef
	) {
		List<? extends RelationshipLink> allLinks = relationshipLinkRepository.findByRelationshipModelNameAndSource(
			relationshipModelName,
			sourceRole,
			docRef,
			OffsetBasedPageRequest.unpaged(Sort.by(ORDER_BY_ROLES_SORT_COLUMNS))
		).getContent();

		for (int i = 0; i < allLinks.size(); i++) {
			if (allLinks.get(i).getId().equals(predecessorRef)) {
				return new ImmutablePair<>(allLinks.get(i), i < allLinks.size() - 1 ? allLinks.get(i + 1) : null);
			}
		}

		if (!allLinks.isEmpty()) {
			throw RpcExceptionSupport.createException(ExceptionCodes.RELATIONSHIP_LINK_ADD_PREDECESSOR_LINK_NOT_FOUND_EXCEPTION_CODE,
				ExceptionKeys.RELATIONSHIP_LINK_ADD_PREDECESSOR_LINK_NOT_FOUND_ERROR_KEY,
				"Missing predecessor Link", String.format("Predecessor link [%s] not found", predecessorRef), null);
		}
		return new ImmutablePair<>(null, null);
	}

	private String findNextAvailableRank(String relationshipModelName, String role, DocumentReference docId, Pair<String, String> range) {
		try {
			return RelationshipRankComputer.computeRank(range);
		} catch (OutOfRanksException e) {
			refreshRanks(relationshipModelName, role, docId);
			try {
				return RelationshipRankComputer.computeRank(range);
			} catch (OutOfRanksException outOfRanksException) {
				throw new NoAvailableRanksException(outOfRanksException);
			}
		}
	}

	private Stream<ImmutableTriple<String, String, DocumentReference>> entitiesToTripleWithRmModelName(RelationshipLink l) {
		List<? extends RelationshipRole> immutableEntities = List.copyOf(l.getRoles().values());
		return immutableEntities.stream()
			.map(e -> new ImmutableTriple<>(l.getRelationshipModel(), e.getName(), e.getDocRef()));
	}
}
