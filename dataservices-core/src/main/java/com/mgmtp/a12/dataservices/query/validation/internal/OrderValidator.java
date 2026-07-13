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

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder;
import com.mgmtp.a12.dataservices.query.Order;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.RelationshipOrder;
import com.mgmtp.a12.dataservices.query.enrichement.internal.FieldPathValidator;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.query.validation.ValidationItem;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipRoleNameNotFoundException;
import com.mgmtp.a12.dataservices.relationship.model.EntityCharacteristics;
import com.mgmtp.a12.dataservices.relationship.model.LinkConstraints;
import com.mgmtp.a12.dataservices.relationship.model.Multiplicity;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Validates sorting specifications in query requests, including relationship-based sorting.
 *
 * This validator ensures that:
 * - `Order` at the root level is either a {@link com.mgmtp.a12.dataservices.query.DirectFieldOrder} or a {@link com.mgmtp.a12.dataservices.query.RelationshipOrder}
 * - `RelationshipOrder` has `relationshipModel`, `targetRole`, and `sortBy` fields
 * - `sortBy` on `RelationshipOrder` can be a {@link com.mgmtp.a12.dataservices.query.DirectFieldOrder} (terminal) or another `RelationshipOrder` (nested)
 * - Terminal `DirectFieldOrder` must have `direction` and `nullHandling` explicitly specified when used in relationship context
 * - Relationship models exist and are accessible by the current user
 * - Target roles exist in the relationship model
 * - Target document models are accessible by the current user
 * - Relationships have to-1 cardinality (to-many not supported)
 * - Terminal fields exist on the target document model
 * - Nesting depth does not exceed configured maximum
 * - Number of relationship orders does not exceed configured maximum
 * - Fields are not in repeatable groups
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OrderValidator {

	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final ModelPermissionEvaluator<IDocumentModel> documentModelPermissionEvaluator;
	private final ModelPermissionEvaluator<RelationshipModel> relationshipModelPermissionEvaluator;
	private final IModelLoader<RelationshipModel> relationshipModelLoader;
	private final ModelTypeService modelTypeService;
	private final DocumentModelUtils documentModelUtils;

	private static final String FIELD = "field";

	// Error message constants
	private static final String INVALID_ORDER_MSG =
		"Order must be either a direct field order (with 'field') or a relationship order (with 'relationshipModel')";
	private static final String TOO_MANY_RELATIONSHIP_ORDERS_MSG = "Number of relationship orders [%d] exceeds configured maximum [%d]";
	private static final String RELATIONSHIP_MODEL_NOT_FOUND_MSG = "Relationship model [%s] not found";
	private static final String RELATIONSHIP_MODEL_NOT_ACCESSIBLE_MSG = "User does not have permission to access relationship model [%s]";
	private static final String ROLE_NOT_FOUND_MSG = "Target role [%s] not found in relationship model [%s]";
	private static final String TARGET_MODEL_NOT_ACCESSIBLE_MSG = "User does not have permission to access target document model [%s]";
	private static final String INVALID_CARDINALITY_MSG =
		"Relationship [%s] with role [%s] has to-many cardinality. Only to-1 relationships are supported for sorting";
	private static final String FIELD_NOT_FOUND_MSG = "Field [%s] not found on document model [%s]";
	private static final String MAX_DEPTH_EXCEEDED_MSG = "Relationship order nesting depth [%d] exceeds configured maximum [%d]";
	private static final String NULL_HANDLING_REQUIRED_MSG = "Explicit nullHandling is required for relationship-based sorting";
	private static final String REPEATABLE_FIELD_NOT_SORTABLE_MSG = "Field [%s] is in a repeatable group and cannot be used for sorting";

	/**
	 * Validates sorting specifications in the given query root.
	 *
	 * @param queryRoot the query root containing sorting specifications
	 * @param targetDocumentModel the target document model name
	 * @param context the query context
	 * @param result the validation result to populate
	 */
	public void validateSorting(QueryRoot queryRoot, String targetDocumentModel, QueryContext context, ValidationResult result) {
		if (sortIsEmpty(queryRoot)) {
			return;
		}

		List<Order> sort = queryRoot.getSort();
		long relationshipOrderCount = sort.stream()
			.filter(RelationshipOrder.class::isInstance)
			.count();

		// Validate max count of relationship orders
		int maxCount = dataServicesCoreProperties.getQuery().getRelationshipOrder().getMaxCount();
		if (relationshipOrderCount > maxCount) {
			result.addResult(ValidationItem.invalid(
				new String[] { "sort" },
				TOO_MANY_RELATIONSHIP_ORDERS_MSG.formatted(relationshipOrderCount, maxCount)
			));
		}

		// Validate each order
		for (int i = 0; i < sort.size(); i++) {
			Order order = sort.get(i);
			String[] path = new String[] { "sort", String.valueOf(i) };
			validateOrder(order, targetDocumentModel, context, path, result);
		}
	}

	private static boolean sortIsEmpty(QueryRoot queryRoot) {
		return Optional.of(queryRoot)
			.map(QueryRoot::getSort)
			.filter(List::isEmpty)
			.isPresent();
	}

	/**
	 * Validates a single order specification.
	 *
	 * @param order the order to validate
	 * @param sourceDocumentModel the source document model name
	 * @param context the query context
	 * @param path the path in the query structure for error reporting
	 * @param result the validation result to populate
	 */
	private void validateOrder(Order order, String sourceDocumentModel, QueryContext context, String[] path, ValidationResult result) {
		if (order instanceof DirectFieldOrder dfo) {
			validateDirectFieldOrder(sourceDocumentModel, context, path, result, dfo);
		} else if (order instanceof RelationshipOrder relationshipOrder) {
			validateRelationshipOrder(relationshipOrder, context, path, result, 1);
		} else {
			result.addResult(ValidationItem.invalid(path, INVALID_ORDER_MSG));
		}
	}

	private void validateDirectFieldOrder(String sourceDocumentModel, QueryContext context, String[] path, ValidationResult result, DirectFieldOrder dfo) {
		if (dfo.field() == null) {
			result.addResult(ValidationItem.invalid(path, INVALID_ORDER_MSG));
			return;
		}
		if (!FieldPathValidator.isValidFieldPath(dfo.field())) {
			result.addResult(FieldPathValidationSupport.invalidFieldPathItem(dfo.field(), ArrayUtils.add(path, FIELD)));
			return;
		}
		// Check field is not in a repeatable group
		if (isFieldInRepeatableGroup(sourceDocumentModel, dfo.field(), context)) {
			result.addResult(ValidationItem.invalid(ArrayUtils.add(path, FIELD), REPEATABLE_FIELD_NOT_SORTABLE_MSG.formatted(dfo.field())));
		} else {
			result.addResult(ValidationItem.valid(ArrayUtils.add(path, FIELD), "Direct field order validated"));
		}
	}

	/**
	 * Validates a relationship order specification recursively.
	 *
	 * @param relationshipOrder the relationship order to validate
	 * @param context the query context
	 * @param path the path in the query structure for error reporting
	 * @param result the validation result to populate
	 * @param currentDepth the current nesting depth
	 */
	private void validateRelationshipOrder(RelationshipOrder relationshipOrder, QueryContext context, String[] path, ValidationResult result,
		int currentDepth) {

		// Validate nesting depth
		int maxDepth = dataServicesCoreProperties.getQuery().getRelationshipOrder().getMaxNestingDepth();
		if (currentDepth > maxDepth) {
			result.addResult(ValidationItem.invalid(path, MAX_DEPTH_EXCEEDED_MSG.formatted(currentDepth, maxDepth)));
			return;
		}

		// Validate relationship model exists and is accessible
		String relationshipModelName = relationshipOrder.relationshipModel();
		if (relationshipModelName == null) {
			result.addResult(ValidationItem.invalid(ArrayUtils.add(path, "relationshipModel"), "Relationship model name is required"));
			return;
		}

		RelationshipModel relationshipModel;
		try {
			relationshipModel = relationshipModelLoader.loadModel(relationshipModelName);
		} catch (NotFoundException e) {
			result.addResult(ValidationItem.invalid(ArrayUtils.add(path, "relationshipModel"),
				RELATIONSHIP_MODEL_NOT_FOUND_MSG.formatted(relationshipModelName)));
			return;
		}

		// Check user has permission to access relationship model
		if (!relationshipModelPermissionEvaluator.hasModelReadPermission(relationshipModel)) {
			result.addResult(ValidationItem.invalid(ArrayUtils.add(path, "relationshipModel"),
				RELATIONSHIP_MODEL_NOT_ACCESSIBLE_MSG.formatted(relationshipModelName)));
			return;
		}

		// Validate target role exists
		String targetRole = relationshipOrder.targetRole();
		if (targetRole == null) {
			result.addResult(ValidationItem.invalid(ArrayUtils.add(path, "targetRole"), "Target role is required"));
			return;
		}

		EntityCharacteristics targetCharacteristics;
		try {
			targetCharacteristics = relationshipModel.getEntityCharacteristicsFromRole(targetRole);
		} catch (RelationshipRoleNameNotFoundException e) {
			result.addResult(ValidationItem.invalid(ArrayUtils.add(path, "targetRole"),
				ROLE_NOT_FOUND_MSG.formatted(targetRole, relationshipModelName)));
			return;
		}

		// Validate target document model is accessible
		String targetDocumentModel = targetCharacteristics.getDocumentModel();
		try {
			documentModelPermissionEvaluator.checkModelReadPermission(targetDocumentModel);
		} catch (NotFoundException e) {
			result.addResult(ValidationItem.invalid(ArrayUtils.add(path, "targetRole"),
				RELATIONSHIP_MODEL_NOT_FOUND_MSG.formatted(targetDocumentModel)));
			return;
		} catch (org.springframework.security.access.AccessDeniedException e) {
			result.addResult(ValidationItem.invalid(ArrayUtils.add(path, "targetRole"),
				TARGET_MODEL_NOT_ACCESSIBLE_MSG.formatted(targetDocumentModel)));
			return;
		}

		// Validate cardinality (only to-1 relationships supported)
		if (!isToOneCardinality(targetCharacteristics)) {
			result.addResult(ValidationItem.invalid(path,
				INVALID_CARDINALITY_MSG.formatted(relationshipModelName, targetRole)));
			return;
		}

		// Validate sortBy is present and valid
		Order sortBy = relationshipOrder.sortBy();
		if (sortBy == null) {
			result.addResult(ValidationItem.invalid(ArrayUtils.add(path, "sortBy"), "sortBy is required on RelationshipOrder"));
			return;
		}

		// Recursively validate sortBy (which can be DirectFieldOrder for terminal or RelationshipOrder for nested)
		validateOrderInRelationshipContext(sortBy, targetDocumentModel, context, ArrayUtils.add(path, "sortBy"), result, currentDepth);
	}

	/**
	 * Validates an order that appears within a relationship context (via sortBy).
	 * Direction and nullHandling must be explicitly specified at the terminal field level.
	 *
	 * @param order the order to validate (can be DirectFieldOrder or RelationshipOrder)
	 * @param sourceDocumentModel the source document model name
	 * @param context the query context
	 * @param path the path in the query structure for error reporting
	 * @param result the validation result to populate
	 * @param currentDepth the current nesting depth
	 */
	private void validateOrderInRelationshipContext(Order order, String sourceDocumentModel, QueryContext context,
		String[] path, ValidationResult result, int currentDepth) {

		if (order instanceof DirectFieldOrder dfo) {
			// Terminal field - direction and nullHandling must be explicitly specified
			if (dfo.direction() == null) {
				result.addResult(ValidationItem.invalid(ArrayUtils.add(path, "direction"), "direction is required for terminal field in relationship order"));
				return;
			}
			if (dfo.nullHandling() == null || dfo.nullHandling() == DirectFieldOrder.NullHandling.NATIVE) {
				result.addResult(ValidationItem.invalid(ArrayUtils.add(path, "nullHandling"), NULL_HANDLING_REQUIRED_MSG));
				return;
			}

			// Validate the field itself
			validateTerminalField(dfo.field(), sourceDocumentModel, context, ArrayUtils.add(path, FIELD), result);
		} else if (order instanceof RelationshipOrder ro) {
			// Nested relationship - recurse
			validateRelationshipOrder(ro, context, path, result, currentDepth + 1);
		} else {
			result.addResult(ValidationItem.invalid(path, INVALID_ORDER_MSG));
		}
	}

	/**
	 * Validates a terminal field on the target document model.
	 *
	 * Note: `ignoreCase` is not validated against the field type. When set on a non-string field
	 * it is silently ignored by the SQL renderer, mirroring the behavior of root-level direct
	 * field sorting. This keeps validation consistent across both code paths and avoids failures
	 * when modeling tools default `ignoreCase=true` on sortable fields.
	 *
	 * @param fieldPath the field path to validate
	 * @param documentModelName the document model name
	 * @param context the query context
	 * @param path the path in the query structure for error reporting
	 * @param result the validation result to populate
	 */
	private void validateTerminalField(String fieldPath, String documentModelName, QueryContext context, String[] path, ValidationResult result) {
		if (fieldPath == null) {
			result.addResult(ValidationItem.invalid(path, "field is required"));
			return;
		}
		if (!FieldPathValidator.isValidFieldPath(fieldPath)) {
			result.addResult(FieldPathValidationSupport.invalidFieldPathItem(fieldPath, path));
			return;
		}
		// Search for the field in the target model, then fall back to subtypes (heterogeneous model support)
		IDocumentModel documentModel = context.getDocumentModel(documentModelName);
		Optional<IField> field = documentModelUtils.findField(documentModel, fieldPath);
		String effectiveModelName = documentModelName;

		if (field.isEmpty()) {
			for (String subtypeName : modelTypeService.findAllSubtypes(documentModelName)) {
				IDocumentModel subtypeModel = context.getDocumentModel(subtypeName);
				field = documentModelUtils.findField(subtypeModel, fieldPath);
				if (field.isPresent()) {
					effectiveModelName = subtypeName;
					break;
				}
			}
		}

		if (field.isEmpty()) {
			result.addResult(ValidationItem.invalid(path,
				FIELD_NOT_FOUND_MSG.formatted(fieldPath, documentModelName)));
			return;
		}

		// Check field is not in a repeatable group
		if (isFieldInRepeatableGroup(effectiveModelName, fieldPath, context)) {
			result.addResult(ValidationItem.invalid(path,
				REPEATABLE_FIELD_NOT_SORTABLE_MSG.formatted(fieldPath)));
			return;
		}

		result.addResult(ValidationItem.valid(path, "Terminal field validated"));
	}

	/**
	 * Checks if a relationship has to-1 cardinality.
	 *
	 * A relationship is considered to-1 if:
	 * - It has no multiplicity constraint (defaults to 0..1), OR
	 * - It has an upper limit of 1, OR
	 * - It is not unbounded and has no upper limit set (defaults to 1)
	 *
	 * @param targetCharacteristics the target entity characteristics
	 * @return `true` if the relationship is to-1, `false` otherwise
	 */
	private boolean isToOneCardinality(EntityCharacteristics targetCharacteristics) {
		LinkConstraints linkConstraints = targetCharacteristics.getLinkConstraints();
		if (linkConstraints == null) {
			return true; // No constraints means 0..1 (to-one)
		}

		Multiplicity multiplicity = linkConstraints.getMultiplicity();
		if (multiplicity == null) {
			return true; // No multiplicity means 0..1 (to-one)
		}

		// Check if unbounded (to-many)
		if (Boolean.TRUE.equals(multiplicity.getUnbounded())) {
			return false;
		}

		// Check upper limit
		Integer upperLimit = multiplicity.getUpperLimit();
		if (upperLimit == null) {
			return true; // No upper limit set and not unbounded means default 1 (to-one)
		}

		return upperLimit == 1;
	}

	/**
	 * Checks if a field is in a repeatable group by traversing the element's parent hierarchy.
	 *
	 * @param documentModelName the document model name
	 * @param fieldPath the field path
	 * @param context the query context
	 * @return `true` if the field is in a repeatable group, `false` otherwise
	 */
	private boolean isFieldInRepeatableGroup(String documentModelName, String fieldPath, QueryContext context) {
		IDocumentModelSearchService searchService = context.getDocumentModelSearchService(documentModelName);

		for (IElement element = searchService.getByPath(fieldPath).orElse(null); element != null; element = element.getParent()) {
			if (element instanceof IGroup group && group.getRepeatability() > 1) {
				return true;
			}
		}

		return false;
	}
}
