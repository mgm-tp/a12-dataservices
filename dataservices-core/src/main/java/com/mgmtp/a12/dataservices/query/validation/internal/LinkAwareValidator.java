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
package com.mgmtp.a12.dataservices.query.validation.internal;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.query.LinkAware;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.validation.ValidationItem;
import com.mgmtp.a12.dataservices.model.internal.DefaultModelTypeService;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipUtils;
import com.mgmtp.a12.dataservices.relationship.model.EntityCharacteristics;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;

import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component public class LinkAwareValidator {
	private final DefaultModelTypeService modelTypeService;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final RelationshipUtils relationshipUtils;
	public static final String LINK_DOCUMENT_CONSTRAINT_FIELD_NAME = "linkDocumentConstraint";
	public static final String MAX_DEPTH_FIELD_NAME = "maxDepth";

	/**
	 * For LinkAware members, the existence of the relationship model must be tested, and also the validity of
	 * provided roles and source document model (comes from parent). QueryLinkValidator and
	 * LinkAwareOperatorValidator are responsible for this.
	 */
	public void validate(LinkAware linkAware, String[] path, QueryContext context, ValidationResult result, boolean validationEnabled) {
		if (validationEnabled) {
			String relationshipModelName = linkAware.getRelationshipModel();
			if (relationshipModelName == null) {
				result.addResult(ValidationItem.invalid(ArrayUtils.add(path, LINK_DOCUMENT_CONSTRAINT_FIELD_NAME), "RM is missing."));
			} else {
				RelationshipModel rm = context.getRelationshipModel(relationshipModelName);
				if (rm == null) {
					result.addResult(
						ValidationItem.invalid(ArrayUtils.add(path, LINK_DOCUMENT_CONSTRAINT_FIELD_NAME), "RM %s is not available.".formatted(relationshipModelName)));
				} else {
					validateLinkConstraints(linkAware, rm, path, result);
					result.addResult(validateRole(linkAware.getTargetRole(), context.getEnrichments().getTargetDocumentModel(linkAware), rm, path));
					result.addResult(validateMaxDepth(linkAware.getMaxDepth(), rm, path));
				}
			}
		}
	}

	private ValidationItem validateMaxDepth(Integer maxDepth, RelationshipModel rm, String[] path) {
		if (relationshipUtils.isRecursive(rm)) {
			if (maxDepth == null) {
				return ValidationItem.invalid(ArrayUtils.add(path, MAX_DEPTH_FIELD_NAME), "Max depth must be provided for recursive relationships.");
			}
			int hardLimit = dataServicesCoreProperties.getQuery().getMaxQueryDepth();
			if (maxDepth > hardLimit) {
				return ValidationItem.invalid(ArrayUtils.add(path, MAX_DEPTH_FIELD_NAME),
					"Max depth cannot be greater than hard limit of %d.".formatted(hardLimit));
			}
		}
		return ValidationItem.valid(ArrayUtils.add(path, MAX_DEPTH_FIELD_NAME), "Validation passed for max depth");
	}

	private ValidationItem validateRole(String role, String documentModel, RelationshipModel rm, String[] path) {
		EntityCharacteristics entityCharacteristics = rm.getContent().getEntityCharacteristics().stream()
			.filter(e -> Objects.equals(e.getRole(), role))
			.findFirst()
			.orElse(null);
		if (entityCharacteristics == null) {
			return ValidationItem.invalid(path, "No such role %s in %s".formatted(role, rm.getHeader().getId()));
		} else {
			Set<String> allSubtypes = modelTypeService.findModelNameAndAllSubtypes(entityCharacteristics.getDocumentModel());
			if (documentModel != null && !allSubtypes.contains(documentModel)) {
				return ValidationItem.invalid(path,
					"DocumentModel %s can not be in role %s of %s. Only %s are allowed.".formatted(documentModel, role, rm.getHeader().getId(),
						String.join(", ", allSubtypes)));
			} else {
				return ValidationItem.valid(path, "Validation passed for roles of relationship %s".formatted(rm.getHeader().getId()));
			}
		}
	}

	/**
	 * If the relationship model has no link document, but linkDocumentConstraint is present, validation should
	 * catch this. QueryLinkValidator is responsible for this.
	 */
	private static void validateLinkConstraints(LinkAware linkAware, RelationshipModel relationshipModel, String[] path, ValidationResult result) {
		if (linkAware.getLinkDocumentConstraint() != null) {
			if (relationshipModel.getContent().getLinkDocumentModel() == null) {
				result.addResult(
					ValidationItem.invalid(ArrayUtils.add(path, LINK_DOCUMENT_CONSTRAINT_FIELD_NAME),
						"RM %s has no link document, so you can not query for this.".formatted(relationshipModel.getHeader().getId())));
			} else {
				result.addResult(ValidationItem.valid(path,
					"Validation of link constraints passed for relationship model %s".formatted(relationshipModel.getHeader().getId())));
			}
		}
	}

}
