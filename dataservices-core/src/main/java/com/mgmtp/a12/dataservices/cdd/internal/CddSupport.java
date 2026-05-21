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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.data.domain.Sort;

import com.mgmtp.a12.dataservices.cdd.domain.internal.CddSkeleton;
import com.mgmtp.a12.dataservices.cdd.domain.internal.CddSkeletonGroup;
import com.mgmtp.a12.dataservices.cdd.jms.internal.ComposeDocumentModel;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.model.ModelConstants;
import com.mgmtp.a12.dataservices.model.document.persistence.DocumentModelReadRepository;
import com.mgmtp.a12.dataservices.model.internal.DefaultModelTypeService;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.relationship.OffsetBasedPageRequest;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkSpecification;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipSortConstants;
import com.mgmtp.a12.dataservices.utils.internal.ComposeDocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CddSupport {

	public static final String CDD_REFERENCES = "CDD_REFERENCES";
	public static final String CDD_ROOT_DOCUMENT_REFERENCE = "CDD_ROOT_DOCUMENT_REFERENCE";
	public static final String FIELD_PATH_SEPARATOR = "/";

	private final RelationshipLinkService relationshipLinkService;
	private final ModelHeaderJpaRepository modelHeaderJpaRepository;
	private final DefaultModelTypeService modelTypeService;
	private final DocumentModelReadRepository documentModelReadRepository;

	public static Stream<IGroup> getDirectChildGroups(IGroup group) {
		return group.getElements().stream()
			.filter(IGroup.class::isInstance)
			.map(IGroup.class::cast);
	}

	public static Stream<IField> getDirectChildFields(IGroup group) {
		return group.getElements().stream()
			.filter(IField.class::isInstance)
			.map(IField.class::cast);
	}

	public static Stream<CddSkeletonGroup> findAllChildGroups(CddSkeletonGroup node) {
		return Stream.concat(Stream.of(node), node.getChildren().stream().flatMap(CddSupport::findAllChildGroups));
	}

	public static Stream<CddSkeletonGroup> findAllChildGroups(CddSkeleton node) {
		return node.getChildren().stream().flatMap(CddSupport::findAllChildGroups);
	}

	/**
	 * Apply consumer to all nodes of skeleton group tree.
	 *
	 * @param skeleton skeleton group to start from (the top level one)
	 * @param consumer function which process current skeleton group and if returns true, then also children are processed
	 */
	public static void walkSkeleton(List<CddSkeletonGroup> skeleton, Predicate<CddSkeletonGroup> consumer) {
		skeleton.forEach(g -> {
			if (consumer.test(g)) {
				Optional.of(g)
					.map(CddSkeletonGroup::getChildren)
					.ifPresent(c -> walkSkeleton(c, consumer));
			}
		});
	}

	/**
	 * Find all skeletons where query root is equal to document model associated with provided document reference
	 *
	 * @param documentReference document reference of dirty document
	 * @return Stream of skeletons where query root is equal to document model associated with provided document reference
	 */
	public Stream<CddSkeleton> findSkeletonsForCrd(DocumentReference documentReference) {
		return findAllCdm()
			.filter(cdm -> isSubtype(ComposeDocumentModelUtils.getCrdModelName(cdm), documentReference.getDocumentModelName()))
			.map(cdm -> CddSkeleton.builder()
				.cdm(cdm)
				.targetDocRef(documentReference)
				.build());
	}

	private Optional<CddSkeleton> findSkeletonsForCrd(ComposeDocumentModel composeDocumentModel, DocumentReference documentReference) {
		if (isSubtype(ComposeDocumentModelUtils.getCrdModelName(composeDocumentModel), documentReference.getDocumentModelName())) {
			return Optional.of(CddSkeleton.builder()
				.cdm(composeDocumentModel)
				.targetDocRef(documentReference)
				.build());
		}

		return Optional.empty();
	}

	/**
	 * Find skeletons which are affected by provided documentReference meaning that CDDs for them needs to be rebuilt
	 *
	 * @param documentReference Document reference of a dirty document
	 * @return stream of affected skeletons
	 */
	public Stream<CddSkeleton> findMatchingSkeletons(DocumentReference documentReference) {
		return findAllCdm()
			.flatMap(model -> Stream.concat(
				findSkeletonsForCrd(model, documentReference).stream(),
				findSkeletonsWhereDocumentIsNotCrd(model, documentReference).stream())
			);
	}

	/**
	 * Find skeletons of provided cdm which are affected by document reference.
	 *
	 * @param cdm               CDM to check
	 * @param documentReference Document reference of dirty document
	 * @return Skeletons which were affected and should be updated. Even though CDD skeleton is basically a CDM, it has targetDocRef field which binds it to specific CDD instance.
	 * That is the reason why list of CDDs can be returned for one CDM.
	 */
	private List<CddSkeleton> findSkeletonsWhereDocumentIsNotCrd(ComposeDocumentModel cdm, DocumentReference documentReference) {
		return Stream.of(CddSkeletonFactory.constructSkeletonFromCdm(cdm))
			.flatMap(skeleton -> skeleton.getChildren().stream()
				.flatMap(rootGroup -> findMatchingGroups(rootGroup, documentReference.getDocumentModelName()))
				.flatMap(matchingGroup -> findSkeletonRootFromGroup(matchingGroup, documentReference, skeleton.toBuilder()))
				.filter(Objects::nonNull))
			.filter(filterSkeletonRootsForTargetDocRef())
			.toList();
	}

	/**
	 * Goes through the provided group and all its descendants to find groups related to the document model name
	 *
	 * @param group             group to traverse
	 * @param documentModelName name of model to match
	 * @return stream of all matching groups for subtree
	 */
	private Stream<CddSkeletonGroup> findMatchingGroups(CddSkeletonGroup group, String documentModelName) {
		return Stream.concat(
			group.getChildren().stream().flatMap(g -> findMatchingGroups(g, documentModelName)),
			Stream.of(group).filter(g -> isSubtype(g.getTargetDocumentModel(), documentModelName))
		);
	}

	/**
	 * Method accepts matched skeleton groups and finds the root group for them
	 *
	 * @param group              node for which to find the topmost parent
	 * @param documentReference  document reference of dirty document
	 * @param cddSkeletonBuilder skeleton to modify
	 * @return Stream of skeletons to modify
	 */
	private Stream<CddSkeleton> findSkeletonRootFromGroup(CddSkeletonGroup group, DocumentReference documentReference,
		CddSkeleton.CddSkeletonBuilder cddSkeletonBuilder) {
		CddSkeletonGroup parent = group.getParent();

		// If matched group is resolved via relationship, it means that provided document reference is not a query root, and skeleton targetDocRef field needs to be updated
		if (group.isRelationship()) {
			String relationshipModelName = group.getRelationshipModelName();
			var links = relationshipLinkService.load(
				RelationshipLinkSpecification.forRelationshipModel(relationshipModelName)
					.withSourceFilter(group.getTargetRole(), documentReference)
					.build(),
				OffsetBasedPageRequest.unpaged(
					Sort.by(RelationshipSortConstants.ID_FIELD_NAME)
				)
			);

			if (parent == null) {
				// we reached the top of the tree so result skeletons can be returned. One skeleton is returned for each link because each skeleton is bind to specific document reference
				return links.stream()
					.map(link -> cddSkeletonBuilder.targetDocRef(link.getRoles().get(group.getSourceRole()).getDocRef()).build());
			} else {
				// this is not a root, so we continue going upwards with updated document reference
				return links.stream()
					.flatMap(link -> findSkeletonRootFromGroup(parent, link.getRoles().get(group.getSourceRole()).getDocRef(), cddSkeletonBuilder));
			}
		} else if (parent == null) {
			// we reached the top of the tree so result skeleton can be returned
			return Stream.of(cddSkeletonBuilder.targetDocRef(documentReference).build());
		} else {
			// this is not a root so we continue going upwards
			return findSkeletonRootFromGroup(parent, documentReference, cddSkeletonBuilder);
		}
	}

	public Stream<ComposeDocumentModel> findAllCdm() {
		return modelHeaderJpaRepository.findIdsByModelType(ModelConstants.DOCUMENT_MODEL_TYPE).stream()
			.map(documentModelReadRepository::readModel)
			.filter(model -> ComposeDocumentModelUtils.isComposeDocumentModel(model.getHeader()))
			.map(ComposeDocumentModel::new);
	}

	private Predicate<CddSkeleton> filterSkeletonRootsForTargetDocRef() {
		return cddSkeleton -> isSubtype(ComposeDocumentModelUtils.getCrdModelName(cddSkeleton.getCdm()),
			cddSkeleton.getTargetDocRef().getDocumentModelName());
	}

	@NonNull private Boolean isSubtype(String parentModel, String testedModel) {
		return Optional.ofNullable(parentModel)
			.map(parent -> modelTypeService.isSubtype(parent, testedModel))
			.orElse(false);
	}
}
