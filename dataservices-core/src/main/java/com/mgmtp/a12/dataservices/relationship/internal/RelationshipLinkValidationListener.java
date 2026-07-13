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
package com.mgmtp.a12.dataservices.relationship.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipRole;
import com.mgmtp.a12.dataservices.relationship.events.AbstractRelationshipLinkEvent;
import com.mgmtp.a12.dataservices.relationship.events.RelationshipLinkAfterCreateEvent;
import com.mgmtp.a12.dataservices.relationship.events.RelationshipLinkAfterDeleteEvent;
import com.mgmtp.a12.dataservices.relationship.model.EntityCharacteristics;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.rpc.RpcExceptionSupport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CommonDataServicesEventListener bean that aggregates the created and deleted links for operations of a RPC request.
 */
@Slf4j
@RequiredArgsConstructor
@Component public class RelationshipLinkValidationListener {

	private final ThreadLocal<Set<RelationshipLinkAfterCreateEvent>> addedLinks = ThreadLocal.withInitial(HashSet::new);
	private final ThreadLocal<Set<RelationshipLinkAfterDeleteEvent>> deletedLinks = ThreadLocal.withInitial(HashSet::new);

	private final DefaultRelationshipLinkService relationshipLinkService;

	@Order(100)
	@CommonDataServicesEventListener public void linkAddedEventListener(RelationshipLinkAfterCreateEvent relationshipLinkAfterCreateEvent) {
		addedLinks.get().add(relationshipLinkAfterCreateEvent);
	}

	@Order(100)
	@CommonDataServicesEventListener public void linkDeletedEventListener(RelationshipLinkAfterDeleteEvent relationshipLinkAfterDeleteEvent) {
		deletedLinks.get().add(relationshipLinkAfterDeleteEvent);
	}

	/**
	 * Validates links that has been created, and links that have been deleted.
	 */
	public void validateLinks() {
		validateDuplicatesAllowed(addedLinks.get().stream()
			.filter(e -> BooleanUtils.isNotTrue(e.getRelationshipModel().getContent().getDuplicatesAllowed()))
			.collect(Collectors.groupingBy(AbstractRelationshipLinkEvent::getRelationshipModel,
				Collectors.mapping(AbstractRelationshipLinkEvent::getLink, Collectors.toSet()))));

		validateMultiplicity(addedLinks.get().stream()
			.collect(Collectors.groupingBy(AbstractRelationshipLinkEvent::getRelationshipModel,
				Collectors.mapping(AbstractRelationshipLinkEvent::getLink, Collectors.toSet()))));
	}

	/**
	 * After the validation is done the links collections need to be cleared because the ThreadLocal member would be reused because the threads reused by pooling
	 */
	public void clearLinks() {
		addedLinks.remove();
		deletedLinks.remove();
		log.debug("Relationship link validation caches have been evicted");
	}

	private void validateMultiplicity(Map<RelationshipModel, Set<RelationshipLink>> linksByModelAndRole) {
		linksByModelAndRole.forEach((relationshipModel, links) -> links.forEach(relationshipLink -> relationshipLink.getRoles().values().stream()
			.distinct()
			.forEach((role) -> validateMultiplicity(relationshipModel, relationshipLink.getRelationshipModel(), role))));
	}

	private void validateMultiplicity(RelationshipModel relationshipModel, String linkModelName, RelationshipRole role) {
		String relationshipModelName = relationshipModel.getHeader().getId();
		EntityCharacteristics characteristicsByRole = findTargetEntityCharacteristicsByRole(relationshipModel, role.getName());

		if (BooleanUtils.isNotTrue(characteristicsByRole.getLinkConstraints().getMultiplicity().getUnbounded())) {
			Integer upperLimit = characteristicsByRole.getLinkConstraints().getMultiplicity().getUpperLimit();
			long currentCount = getNumberOfLinksPerRole(role, relationshipModelName);
			if ((upperLimit != null) && currentCount > upperLimit) {
				throw RpcExceptionSupport.createException(ExceptionCodes.RELATIONSHIP_INVALID_LINK_EXCEPTION_CODE, ExceptionKeys.RELATIONSHIP_LINK_VALIDATION_ERROR_KEY, "Upper Limit Reached",
					"Upper limit reached for role [%s] in relationship model [%s]. Document [%s] already has [%d] links; maximum is [%d]".formatted(
						characteristicsByRole.getRole(), linkModelName, role.getDocRef(), currentCount, upperLimit));
			}
		}
	}

	private EntityCharacteristics findTargetEntityCharacteristicsByRole(RelationshipModel relationshipModel, String sourceRole) {
		Optional<EntityCharacteristics> entityCharacteristics = relationshipModel.getContent().getEntityCharacteristics().stream()
			.filter(characteristic -> !characteristic.getRole().equals(sourceRole))
			.findFirst();
		return entityCharacteristics.orElseThrow(() -> RpcExceptionSupport.createException(ExceptionCodes.RELATIONSHIP_LINK_ROLE_MISSING_EXCEPTION_CODE,
			ExceptionKeys.RELATIONSHIP_LINK_VALIDATION_ROLE_MISSING_ERROR_KEY,
			"Role Characteristic",
			"Unable to find entity characteristics for role [%s]".formatted(sourceRole)));
	}

	private long getNumberOfLinksPerRole(RelationshipRole role, String relationshipModelName) {
		return relationshipLinkService.countByRole(relationshipModelName, role.getName(), role.getDocRef());
	}

	private void validateDuplicatesAllowed(Map<RelationshipModel, Set<RelationshipLink>> linksByModel) {

		linksByModel.forEach((relationshipModel, links) -> links.forEach(link -> {
			List<? extends RelationshipRole> roleList = relationshipModel.getContent().getEntityCharacteristics().stream()
				.map(EntityCharacteristics::getRole)
				.map(r -> link.getRoles().get(r))
				.toList();

			if (relationshipLinkService.countByRoles(link.getRelationshipModel(), roleList.getFirst(), roleList.get(1)) > 1) {
				throw RpcExceptionSupport.createException(ExceptionCodes.RELATIONSHIP_INVALID_LINK_EXCEPTION_CODE, ExceptionKeys.RELATIONSHIP_LINK_VALIDATION_ERROR_KEY,
					"Duplicated link constraint violated",
					"Creation of the link of model [%s] between [%s] and [%s] violates duplication constraint".formatted(
						relationshipModel.getHeader().getId(), roleToString(roleList.getFirst()), roleToString(roleList.get(1))));
			}
		}));
	}

	private static String roleToString(RelationshipRole role) {
		return "%s/%s".formatted(role.getName(), role.getDocRef());
	}
}
