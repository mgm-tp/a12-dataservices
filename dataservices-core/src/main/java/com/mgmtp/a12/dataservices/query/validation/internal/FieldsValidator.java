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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.constraint.FieldAwareOperator;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.NestingOperator;
import com.mgmtp.a12.dataservices.query.constraint.VariadicOperator;
import com.mgmtp.a12.dataservices.query.enrichement.internal.FieldPathValidator;
import com.mgmtp.a12.dataservices.query.fields.AliasedFieldItem;
import com.mgmtp.a12.dataservices.query.fields.ProjectionField;
import com.mgmtp.a12.dataservices.query.fields.aggregation.AggregationProjector;
import com.mgmtp.a12.dataservices.query.fields.aggregation.IAggregationFunction;
import com.mgmtp.a12.dataservices.query.internal.QueryTopologyHelper;
import com.mgmtp.a12.dataservices.query.topology.QueryTopology;
import com.mgmtp.a12.dataservices.query.validation.ValidationItem;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;

import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_VALIDATION;
import static com.mgmtp.a12.dataservices.query.internal.QueryTopologyHelper.getEffectiveFieldType;

/**
 * Validates fields, aggregations and constraints in query topologies.
 *
 * This validator handles validation for both standard queries and QueryLink operations.
 * For QueryLink operations, the targetDocumentModel may be null during initial validation
 * and will be determined later by the query validator.
 */
@RequiredArgsConstructor
@Component public class FieldsValidator {

	private final DocumentModelUtils documentModelUtils;
	private final ModelTypeService modelTypeService;

	public static final String FIELDS_FIELD_NAME = "fields";
	public static final String AGGREGATIONS_FIELD_NAME = "aggregations";
	public static final String AGGREGATION_FIELD_NAME = "aggregation";
	public static final String GROUP_FIELD_NAME = "group";
	public static final String FIELD_NOT_SEARCHABLE_MESSAGE_PATTERN = "Field %s is not searchable (or doesn't exists in model %s at all).";
	public static final String GROUPING_FIELD_NOT_SEARCHABLE_MESSAGE_PATTERN = "Grouping field %s is not searchable (or doesn't exists in model % at all).";
	public static final String AGGREGATION_FIELD_NOT_SEARCHABLE_MESSAGE_PATTERN =
		"Aggregation field %s is not searchable (or doesn't exists in model %s at all).";

	/**
	 * Validates fields and aggregations in the given query topology.
	 * @param topology The query topology to validate.
	 * @param targetDocumentModel The target document model name, maybe null for QueryLink operations.
	 * @param path The path in the query structure for error reporting.
	 * @param context The query context containing enrichments and model information.
	 * @param result The validation result to populate.
	 * @param validationEnabled Flag indicating whether validation is enabled.
	 */
	public void validateFieldsAndAggregations(QueryTopology topology, String targetDocumentModel, String[] path, QueryContext context, ValidationResult result,
		boolean validationEnabled) {
		if (validationEnabled) {
			validateAggregations(targetDocumentModel, topology, path, context, result);
			validateFields(targetDocumentModel, topology, path, context, result);
			validateConstraintFields(targetDocumentModel, topology, path, context, result);
		}
	}

	private void validateFields(String targetDocumentModel, QueryTopology topology, String[] path, QueryContext context, ValidationResult result) {
		List<String> fields = topology.getFields();
		if (fields != null) {
			if (fields.isEmpty()) {
				result.addResult(
					ValidationItem.invalid(new String[] { FIELDS_FIELD_NAME }, "Either enter a non-empty list for fields, or do not enter it at all."));
			} else {
				fields.stream()
					.filter(field -> !FieldPathValidator.isValidFieldPath(field))
					.forEach(field -> rejectInvalidFieldPath(field, new String[] { FIELDS_FIELD_NAME }, result));
				checkIsIndexed(fields.stream().filter(FieldPathValidator::isValidFieldPath), path, targetDocumentModel, FIELD_NOT_SEARCHABLE_MESSAGE_PATTERN, context, result);
			}
		}
	}

	private void validateConstraintFields(String targetDocumentModel, QueryTopology topology, String[] path, QueryContext context, ValidationResult result) {
		ILogicOperator operator = topology.getConstraint();
		Stream.Builder<String> fieldPathsBuilder = Stream.builder();

		if (operator instanceof FieldAwareOperator fieldAwareOperator && fieldAwareOperator.getField() != null) {
			fieldPathsBuilder.add(fieldAwareOperator.getField());
		}

		if (operator instanceof VariadicOperator variadicOperator && variadicOperator.getOperands() != null) {
			variadicOperator.getOperands().stream()
				.filter(FieldAwareOperator.class::isInstance)
				.map(FieldAwareOperator.class::cast)
				.map(FieldAwareOperator::getField)
				.filter(Objects::nonNull)
				.forEach(fieldPathsBuilder::add);
		}

		if (operator instanceof NestingOperator nestingOperator && nestingOperator.getOperand() instanceof FieldAwareOperator fieldAwareOperator) {
			fieldPathsBuilder.add(fieldAwareOperator.getField());
		}

		List<String> constraintFieldPaths = fieldPathsBuilder.build().toList();
		constraintFieldPaths.stream()
			.filter(field -> !FieldPathValidator.isValidFieldPath(field))
			.forEach(field -> rejectInvalidFieldPath(field, path, result));
		checkIsIndexed(constraintFieldPaths.stream().filter(FieldPathValidator::isValidFieldPath), path, targetDocumentModel, FIELD_NOT_SEARCHABLE_MESSAGE_PATTERN, context,
			result);
	}

	private void validateAggregations(String targetDocumentModel, QueryTopology topology, String[] path, QueryContext context, ValidationResult result) {
		AggregationProjector aggregation = topology.getAggregation();
		if (aggregation == null) {
			result.addResult(ValidationItem.valid(ArrayUtils.add(path, AGGREGATION_FIELD_NAME), "Aggregations validated"));
			return;
		}

		boolean isValid = true;
		if (CollectionUtils.isNotEmpty(topology.getLinks())) {
			result.addResult(ValidationItem.invalid(path, "Invalid aggregations: links together with aggregations are not supported."));
			isValid = false;
		}
		if (CollectionUtils.isEmpty(aggregation.getAggregations())) {
			result.addResult(ValidationItem.invalid(ArrayUtils.addAll(path, AGGREGATION_FIELD_NAME),
				"Either enter a non-empty list for aggregations, or do not enter it at all."));
			isValid = false;
		} else {
			if (topology.getFields() != null) {
				result.addResult(
					ValidationItem.invalid(path, "Properties '" + FIELDS_FIELD_NAME + "' and '" + AGGREGATION_FIELD_NAME + "' are mutually exclusive."));
				isValid = false;
			}

			checkIsIndexed(Optional.of(aggregation)
					.map(AggregationProjector::getGroup)
					.stream()
					.flatMap(Collection::stream)
					.map(ProjectionField::getField),
				path, targetDocumentModel, GROUPING_FIELD_NOT_SEARCHABLE_MESSAGE_PATTERN, context, result);

			checkIsIndexed(aggregation.getAggregations().stream().map(AliasedFieldItem::getField),
				path, targetDocumentModel, AGGREGATION_FIELD_NOT_SEARCHABLE_MESSAGE_PATTERN, context, result);

			// Note: targetDocumentModel can be null at this stage for QueryLink operations,
			// field validation will be performed when the target model is determined by the validator.
			aggregation.getAggregations().forEach(aggregationFunction ->
				checkAggregationFunction(aggregationFunction, ArrayUtils.addAll(path, AGGREGATION_FIELD_NAME, AGGREGATIONS_FIELD_NAME), targetDocumentModel,
					context, result)
			);

			if (CollectionUtils.isNotEmpty(aggregation.getGroup())) {
				aggregation.getGroup().forEach(aggregationGroup ->
					checkAggregationGroup(aggregationGroup, ArrayUtils.addAll(path, AGGREGATION_FIELD_NAME, GROUP_FIELD_NAME), result)
				);
			}
		}
		if (isValid) {
			result.addResult(ValidationItem.valid(ArrayUtils.add(path, AGGREGATION_FIELD_NAME), "Aggregations validated"));
		}
	}

	private void checkAggregationFunction(IAggregationFunction aggregationFunction, String[] path, String targetDocumentModel, QueryContext context,
		ValidationResult result) {
		// Check for existing function attribute in each aggregation function.
		if (aggregationFunction.getFunction() == null) {
			result.addResult(ValidationItem.invalid(path,
				"Aggregation function '%s' must have a function specified.".formatted(aggregationFunction.getField())));
		} else {
			result.addResult(ValidationItem.valid(path, "Aggregation function %s has a function specified".formatted(aggregationFunction.getFunction())));
			// Check that the function is registered.
			if (!QueryTopologyHelper.isAggregationFunctionRegistered(aggregationFunction.getFunction())) {
				result.addResult(ValidationItem.invalid(path,
					"Aggregation function is missing or not registered."));
				return;
			} else {
				result.addResult(ValidationItem.valid(path, "Aggregation function %s is registered".formatted(aggregationFunction.getFunction())));
			}
		}
		// Check for existing field attribute in each aggregation function.
		if (aggregationFunction.getField() == null) {
			result.addResult(ValidationItem.invalid(path,
				"Aggregation function '%s' must have a field specified.".formatted(aggregationFunction.getFunction())));
		} else if (!FieldPathValidator.isValidFieldPath(aggregationFunction.getField())) {
			rejectInvalidFieldPath(aggregationFunction.getField(), path, result);
		} else {
			result.addResult(ValidationItem.valid(path, "Aggregation function '%s' has field specified.".formatted(
				aggregationFunction.getFunction())));			// Check for correct field types for aggregation functions.
			extractFieldType(context, targetDocumentModel, aggregationFunction.getField())
				.ifPresentOrElse(fieldType -> {
					context.getEnrichments().getFieldDescriptor(aggregationFunction.getField()).setFieldType(fieldType);
					if (!aggregationFunction.isSuitableForFieldType(fieldType)) {
						result.addResult(ValidationItem.invalid(path,
							"Aggregation function '%s' is not suitable for field type '%s'.".formatted(aggregationFunction.getFunction(), fieldType)));
					} else {
						result.addResult(ValidationItem.valid(path,
							"Aggregation function '%s' is suitable for field type '%s'.".formatted(aggregationFunction.getFunction(), fieldType)));
					}
				}, () -> result.addResult(ValidationItem.invalid(path,
					"Field type could not be determined for field '%s'.".formatted(aggregationFunction.getField()))));
		}
	}

	private void checkAggregationGroup(ProjectionField aggregationGroup, String[] path, ValidationResult result) {
		// Check for existing field in the aggregation group
		if (aggregationGroup.getField() == null) {
			result.addResult(ValidationItem.invalid(path,
				"Aggregation group must have a field specified."));
		} else if (!FieldPathValidator.isValidFieldPath(aggregationGroup.getField())) {
			rejectInvalidFieldPath(aggregationGroup.getField(), path, result);
		} else {
			result.addResult(ValidationItem.valid(path, "Group has field %s specified.".formatted(aggregationGroup.getField())));
		}
	}

	/**
	 * Extracts the field type from the document model.
	 *
	 * @param context the query context containing document models
	 * @param documentModelName the document model name, may be null
	 * @param path the field path to extract type for
	 * @return Optional containing field type, or empty if model is null or field not found.
	 */
	private Optional<String> extractFieldType(QueryContext context, String documentModelName, String path) {
		context.getEnrichments().computeModelSubtypes(documentModelName, modelTypeService::findAllSubtypes);

		if (documentModelName == null) {
			return Optional.empty();
		}
		Set<String> modelSubtypes = context.getEnrichments().getModelSubtypes(documentModelName);
		modelSubtypes.add(documentModelName);
		return modelSubtypes.stream()
			.map(context::getDocumentModel)
			.flatMap(iDocumentModel -> documentModelUtils.findField(iDocumentModel, path).stream())
			.map(field -> getEffectiveFieldType(field, QUERY_VALIDATION))
			.map(QueryTopologyHelper::fieldTypeAsString)
			.findAny();
	}

	private static void rejectInvalidFieldPath(String field, String[] path, ValidationResult result) {
		result.addResult(FieldPathValidationSupport.invalidFieldPathItem(field, path));
	}

	/**
	 * Checks if the given field paths are indexed in the target document model. If the target document model is null,
	 * the check is skipped.
	 *
	 * @param fieldPaths The stream of field paths to check.
	 * @param path The path in the query structure for error reporting.
	 * @param targetDocumentModel The target document model name, may be null.
	 * @param message The message pattern for invalid fields.
	 * @param context The query context containing enrichments and model information.
	 * @param result The validation result to populate.
	 */
	private void checkIsIndexed(Stream<String> fieldPaths, String[] path, String targetDocumentModel, String message, QueryContext context,
		ValidationResult result) {

		fieldPaths
			.filter(fp -> Objects.nonNull(fp) && StringUtils.isNotBlank(targetDocumentModel))
			.forEach(fieldPath -> {
				try {
					context.getEnrichments().computeModelSubtypes(targetDocumentModel, modelTypeService::findAllSubtypes);
					Set<String> allModels = SetUtils.union(Set.of(targetDocumentModel), context.getEnrichments().getModelSubtypes(targetDocumentModel));

					if (context.isIndexedField(allModels, fieldPath)) {
						result.addResult(ValidationItem.valid(ArrayUtils.add(path, FIELDS_FIELD_NAME), "Field %s is indexed".formatted(fieldPath)));
					} else {
						result.addResult(ValidationItem.invalid(ArrayUtils.add(path, FIELDS_FIELD_NAME),
							"Field %s not found.".formatted(fieldPath)));
					}
				} catch (Exception e) {
					result.addResult(ValidationItem.invalid(path, message.formatted(fieldPath, targetDocumentModel)));
				}
			});
	}
}
