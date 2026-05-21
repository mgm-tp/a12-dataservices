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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DataServicesDocumentMetadata;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.persistence.internal.AggregatedDocumentRepository;
import com.mgmtp.a12.dataservices.model.internal.DefaultModelTypeService;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipInvalidLinkException;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipLinkDocumentMissingException;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipLinkDocumentModelMismatchException;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipLinkDocumentNotAllowedException;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipLinkEntityInvalidException;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipModelMismatchException;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipRoleDocumentNotFoundException;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipRoleMismatchException;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipRoleNameNotFoundException;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipVersionValidationException;
import com.mgmtp.a12.dataservices.relationship.model.EntityCharacteristics;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;

import lombok.RequiredArgsConstructor;

/**
 * Providing support for validating relationship-related data.
 */
@RequiredArgsConstructor
public class RelationshipValidationSupport {
	private final IModelLoader<RelationshipModel> modelLoader;
	private final AggregatedDocumentRepository aggregatedDocumentRepository;
	private final DefaultModelTypeService modelTypeService;

	public static void validateRelationshipModelVersion(RelationshipModel relationshipModel) {
		String modelVersion = relationshipModel.getHeader().getModelVersion();
		if (!RelationshipModel.VERSION.equals(modelVersion)) {
			throw new RelationshipVersionValidationException(
				String.format("Validation of relationship model [%s] failed because it contains invalid version [%s]. Currently valid version is [%s]",
					relationshipModel.getHeader().getId(),
					modelVersion,
					RelationshipModel.VERSION));
		}
	}

	public static void validateLinkCharacteristic(LinkDescriptor linkDescriptor) {
		int size = linkDescriptor.getEntities().size();
		if (size != 2) {
			throw new RelationshipInvalidLinkException(size).withAnonymityMessage("Relationship model is not valid.");
		}
		linkDescriptor.getEntities()
			.forEach(RelationshipValidationSupport::validateRole);
	}

	private static void validateRole(RelationshipRoleSpec relationshipRole) {
		String role = relationshipRole.getRole();
		if (StringUtils.isEmpty(role)) {
			throw new RelationshipLinkEntityInvalidException();
		}
		if (relationshipRole.getDocRef() == null) {
			throw new RelationshipLinkEntityInvalidException(role).withAnonymityMessage("Role is not valid.");
		}
	}

	public static void validateLinkDocument(String linkDocument, RelationshipModel model) {
		if (StringUtils.isBlank(model.getContent().getLinkDocumentModel()) && (StringUtils.isNotBlank(linkDocument))) {
			throw new RelationshipLinkDocumentNotAllowedException(linkDocument).withAnonymityMessage("Link document is not valid.");
		}

		if (StringUtils.isNotBlank(model.getContent().getLinkDocumentModel()) && (StringUtils.isBlank(linkDocument))) {
			throw new RelationshipLinkDocumentMissingException(model.getContent().getLinkDocumentModel());
		}
	}

	public static void validateLink(RelationshipLink relationshipLink, LinkDescriptor linkDescriptor) {
		List<RelationshipRoleSpec> roles = linkDescriptor.getEntities();
		String linkId = relationshipLink.getId();
		boolean relationshipModelNotMatch = Optional.of(relationshipLink)
			.map(RelationshipLink::getRelationshipModel)
			.filter(e -> e.equals(linkDescriptor.getRelationshipModel()))
			.isEmpty();

		if (relationshipModelNotMatch) {

			throw new RelationshipModelMismatchException(linkId, relationshipLink.getRelationshipModel(),
				linkDescriptor.getRelationshipModel());
		}

		for (RelationshipRoleSpec roleSpec : roles) {
			relationshipLink.getRoles().values().stream()
				.filter(en -> Objects.equals(en.getName(), roleSpec.getRole()))
				.filter(en -> Objects.equals(en.getDocRef(), roleSpec.getDocRef()))
				.findAny()
				.orElseThrow(() -> new RelationshipRoleMismatchException(linkId, relationshipLink.getRoles(), roles, roleSpec).withAnonymityMessage("Validation of links failed."));
		}

	}

	public void validateLink(LinkDescriptor linkDescriptor, DocumentReference linkDocRef) {
		RelationshipModel model = modelLoader.loadModel(linkDescriptor.getRelationshipModel());
		validateLinkCharacteristic(linkDescriptor);
		validateRoles(linkDescriptor, model);
		validateLinkDocument(linkDocRef, model);
	}

	private void validateRoles(LinkDescriptor linkDescriptor, RelationshipModel relationshipModel) {
		linkDescriptor.getEntities()
			.forEach(r -> {
				String relationshipModelName = relationshipModel.getHeader().getId();

				// Validate if document reference of role exists
				DocumentReference docRef = aggregatedDocumentRepository.getByDocumentReference(r.getDocRef())
					.map(DataServicesDocument::getMetadata)
					.map(DataServicesDocumentMetadata::getDocRef)
					.orElseThrow(() -> new RelationshipRoleDocumentNotFoundException(relationshipModelName, r).withAnonymityMessage("Validation of roles failed."));

				// Validate if document model of the role document.
				Set<String> modelNameAndSubtypes = modelTypeService.findModelNameAndAllSubtypes(
					relationshipModel.getContent().getEntityCharacteristics().stream()
						.filter(characteristic -> Objects.equals(characteristic.getRole(), r.getRole()))
						.map(EntityCharacteristics::getDocumentModel)
						.findFirst()
						.orElseThrow(() -> new RelationshipRoleNameNotFoundException(relationshipModelName, r.getRole()).withAnonymityMessage("Validation of roles failed."))
				);

				if (!modelNameAndSubtypes.contains(docRef.getDocumentModelName())) {
					throw new RelationshipLinkDocumentModelMismatchException(relationshipModelName, docRef, modelNameAndSubtypes);
				}
			});
	}

	private void validateLinkDocument(DocumentReference linkDocRef, RelationshipModel model) {
		if (StringUtils.isBlank(model.getContent().getLinkDocumentModel()) && (StringUtils.isNotBlank(linkDocRef))) {
			throw new RelationshipLinkDocumentNotAllowedException(linkDocRef.getDocumentModelName());
		}

		if (StringUtils.isNotBlank(model.getContent().getLinkDocumentModel()) && (StringUtils.isBlank(linkDocRef))) {
			throw new RelationshipLinkDocumentMissingException(model.getContent().getLinkDocumentModel());
		}

		if (StringUtils.isNotBlank(linkDocRef)) {
			aggregatedDocumentRepository.getByDocumentReference(linkDocRef)
				.map(DataServicesDocument::getMetadata)
				.map(DataServicesDocumentMetadata::getDocRef)
				.orElseThrow(() -> new NotFoundException("Link documentation " + linkDocRef + " not found"));
		}
	}
}
