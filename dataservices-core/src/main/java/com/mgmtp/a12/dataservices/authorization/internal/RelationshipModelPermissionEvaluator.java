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
package com.mgmtp.a12.dataservices.authorization.internal;

import java.util.Collection;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipInvalidDocumentReferencesException;
import com.mgmtp.a12.dataservices.model.persistence.IModelReadRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.relationship.model.EntityCharacteristics;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModelContent;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;

@Component public class RelationshipModelPermissionEvaluator extends AbstractModelPermissionEvaluator<RelationshipModel> {

	private final ModelPermissionEvaluator<IDocumentModel> documentModelPermissionEvaluator;
	private final IModelReadRepository<RelationshipModel> relationshipModelReadRepository;

	public RelationshipModelPermissionEvaluator(AuthorizationService authorizationService, CachedPermissionEvaluator cachedPermissionEvaluator, ModelHeaderJpaRepository modelHeaderRepository,
		ModelPermissionEvaluator<IDocumentModel> documentModelPermissionEvaluator, IModelReadRepository<RelationshipModel> relationshipModelReadRepository) {
		super(authorizationService, cachedPermissionEvaluator, modelHeaderRepository);
		this.documentModelPermissionEvaluator = documentModelPermissionEvaluator;
		this.relationshipModelReadRepository = relationshipModelReadRepository;
	}

	@Override public void checkModelReadPermission(RelationshipModel model) {
		super.checkModelReadPermission(model);

		try {
			Optional<RelationshipModelContent> content = Optional.of(model)
				.map(RelationshipModel::getContent);
			checkLinkDocumentPermission(content);
			checkLinkedDocumentsPermissions(content);
		} catch (InvalidInputException | NotFoundException e) {
			throw new RelationshipInvalidDocumentReferencesException(model.getHeader().getId());
		}
	}

	@Override public void checkModelReadPermission(String modelId) {
		this.checkModelReadPermission(relationshipModelReadRepository.readModel(modelId));
	}

	@Override public boolean hasModelReadPermission(RelationshipModel model) {
		try {
			this.checkModelReadPermission(model);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void checkLinkDocumentPermission(Optional<RelationshipModelContent> content) {
		content
			.map(RelationshipModelContent::getLinkDocumentModel)
			.ifPresent(documentModelPermissionEvaluator::checkModelReadPermission);
	}

	private void checkLinkedDocumentsPermissions(Optional<RelationshipModelContent> content) {
		content
			.map(RelationshipModelContent::getEntityCharacteristics)
			.stream()
			.flatMap(Collection::stream)
			.map(EntityCharacteristics::getDocumentModel)
			.forEach(documentModelPermissionEvaluator::checkModelReadPermission);
	}
}
