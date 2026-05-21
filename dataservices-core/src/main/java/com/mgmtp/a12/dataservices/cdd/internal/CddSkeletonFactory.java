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
package com.mgmtp.a12.dataservices.cdd.internal;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.springframework.data.domain.Sort;

import com.mgmtp.a12.dataservices.cdd.domain.internal.CddSkeleton;
import com.mgmtp.a12.dataservices.cdd.domain.internal.CddSkeletonGroup;
import com.mgmtp.a12.dataservices.cdd.jms.internal.ComposeDocumentModel;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.relationship.OffsetBasedPageRequest;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkSpecification;
import com.mgmtp.a12.dataservices.relationship.model.EntityCharacteristics;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.utils.internal.ModelUtils;
import com.mgmtp.a12.kernel.md.model.api.IGroup;

import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_RELATIONSHIP_ANNOTATION;
import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_SOURCE_ROLE_ANNOTATION;
import static com.mgmtp.a12.dataservices.relationship.internal.RelationshipSortConstants.ID_FIELD_NAME;
import static com.mgmtp.a12.dataservices.relationship.internal.RelationshipSortConstants.ORDER_BY_ROLES_SORT_COLUMNS;

@RequiredArgsConstructor
public class CddSkeletonFactory {

	private final RelationshipLinkService relationshipLinkService;
	private final IModelLoader<RelationshipModel> relationshipModelLoader;

	/**
	 * Construct skeleton from CDM. Basically, just go through all CDM fields and map them to CddGroup
	 *
	 * @param cdm CDM to convert to skeleton
	 * @return Skeleton constructed from provided CDM
	 */
	public static CddSkeleton constructSkeletonFromCdm(ComposeDocumentModel cdm) {
		return CddSkeleton.builder()
			.cdm(cdm)
			// Construct skeleton branch for each root group and connect them together to obtain complete skeleton
			.children(CddSupport.getDirectChildGroups(cdm.getContent().getDocumentModelRoot())
				.map(rootGroup -> constructSkeletonBranchFromGroup(rootGroup, null))
				.toList())
			.build();
	}

	/**
	 * Generates skeleton from root and fills it with links
	 *
	 * @param documentReference crd at top of the tree. Can be null for generating skeleton just from CDM
	 * @param parent            parent skeleton group. Null for top
	 * @param group             document model group related to skeleton group
	 * @return stream of root groups
	 */
	public Stream<CddSkeletonGroup> constructSkeletonFromRootGroup(DocumentReference documentReference, CddSkeletonGroup parent, IGroup group) {
		String relationshipModelName = ModelUtils.getAnnotationValue(group, CDM_RELATIONSHIP_ANNOTATION)
			.orElse(null);
		String sourceRole = ModelUtils.getAnnotationValue(group, CDM_SOURCE_ROLE_ANNOTATION)
			.orElse(null);
		CddSkeletonGroup cddGroupTemplate = CddSkeletonGroup.builder()
			.group(group)
			.parent(parent)
			.build();

		List<CddSkeletonGroup> resultGroups;
		// check if group contains links and is not a root group
		if (relationshipModelName == null || sourceRole == null || documentReference == null || parent == null) {
			resultGroups = List.of(cddGroupTemplate);
		} else {
			AtomicInteger counter = new AtomicInteger();
			RelationshipModel relationshipModel = relationshipModelLoader.loadModel(relationshipModelName);
			EntityCharacteristics sourceCharacteristics = relationshipModel.getEntityCharacteristicsFromRole(sourceRole);

			// create new group for each link with corresponding position value
			resultGroups = relationshipLinkService.load(
					RelationshipLinkSpecification.forRelationshipModel(relationshipModelName)
						.withSourceFilter(sourceRole, documentReference)
						.build(),
					OffsetBasedPageRequest.unpaged(
						Boolean.TRUE.equals(sourceCharacteristics.getOrdered()) ? Sort.by(ORDER_BY_ROLES_SORT_COLUMNS) : Sort.by(ID_FIELD_NAME)
					)
				).stream()
				.map(link -> cddGroupTemplate.toBuilder().link(link).position(counter.incrementAndGet()).build())
				.toList();
		}

		// recursive call to traverse all children nodes
		resultGroups.forEach(g ->
			g.setChildren(getChildrenCddSkeletonGroups(group, g, Optional.ofNullable(g.getTargetDocRef()).orElse(documentReference))));

		return resultGroups.stream();
	}

	/**
	 * Goes through the descendants of provided group and maps each IGroup instance to the CddSkeletonGroup
	 *
	 * @param parent parent skeleton group. Null for root group
	 * @param group  group for which to construct skeleton branch
	 * @return skeleton branch
	 */
	private static CddSkeletonGroup constructSkeletonBranchFromGroup(IGroup group, CddSkeletonGroup parent) {
		CddSkeletonGroup cddSkeletonGroup = CddSkeletonGroup.builder()
			.group(group)
			.parent(parent)
			.build();

		cddSkeletonGroup.setChildren(CddSupport.getDirectChildGroups(group)
			.map(g -> constructSkeletonBranchFromGroup(g, cddSkeletonGroup))
			.toList());
		return cddSkeletonGroup;
	}

	private List<CddSkeletonGroup> getChildrenCddSkeletonGroups(IGroup group, CddSkeletonGroup cddSkeletonGroup, DocumentReference sourceDocument) {
		return CddSupport.getDirectChildGroups(group)
			.flatMap(g -> constructSkeletonFromRootGroup(sourceDocument, cddSkeletonGroup, g))
			.toList();
	}
}
