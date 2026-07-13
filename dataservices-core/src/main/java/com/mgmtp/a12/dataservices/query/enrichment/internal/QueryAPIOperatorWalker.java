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
package com.mgmtp.a12.dataservices.query.enrichment.internal;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.query.ConstraintAware;
import com.mgmtp.a12.dataservices.query.LinkAware;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.TargetDocumentModelAware;
import com.mgmtp.a12.dataservices.query.constraint.FieldAwareOperator;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.NestingOperator;
import com.mgmtp.a12.dataservices.query.constraint.VariadicOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.SimpleSearchOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.DateRangeOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.RangeOperator;
import com.mgmtp.a12.dataservices.query.enrichement.Enrichments;
import com.mgmtp.a12.dataservices.query.enrichement.FieldDescriptor;
import com.mgmtp.a12.dataservices.query.enrichement.internal.FieldPathValidator;
import com.mgmtp.a12.dataservices.query.internal.EnrichmentHelper;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateFragmentType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_ENRICHMENT;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.QUERY_INVALID_INPUT_ERROR_KEY;
import static com.mgmtp.a12.dataservices.query.internal.QueryTopologyHelper.fieldTypeAsString;
import static com.mgmtp.a12.dataservices.query.internal.QueryTopologyHelper.getEffectiveFieldType;

/**
 * Traverses the constraint tree of a query, calling all registered `IQueryAPIOperatorEnricher`
 * implementations at each node.
 *
 * The walker handles traversal of all operator types: `FieldAwareOperator`, `VariadicOperator`,
 * `NestingOperator`, `SimpleSearchOperator`, `ConstraintAware`, and `LinkAware`. For each node,
 * all enrichers are called in registration order regardless of the return value of previous enrichers.
 */
@Slf4j
@RequiredArgsConstructor
public class QueryAPIOperatorWalker {

	private final List<IQueryAPIOperatorEnricher> enrichers;
	private final DocumentModelUtils documentModelUtils;
	private final ModelTypeService modelTypeService;

	/**
	 * Walks the constraint tree rooted at `constraint`, enriching each operator using all registered
	 * `IQueryAPIOperatorEnricher` implementations. Returns immediately if `constraint` is `null`.
	 *
	 * @param constraint the root of the constraint sub-tree to walk; may be `null`.
	 * @param targetDocumentModel the document model in scope at this level of the tree.
	 * @param context the query context providing models, enrichments, and caches; never `null`.
	 */
	public void walkConstraint(ILogicOperator constraint, String targetDocumentModel, QueryContext context) {
		if (constraint == null) {
			return;
		}

		if (constraint instanceof FieldAwareOperator fieldAwareOperator) {
			enrichFieldAwareOperator(fieldAwareOperator, targetDocumentModel, context);
		}

		if (constraint instanceof TargetDocumentModelAware targetDocumentModelAware) {
			context.getEnrichments().computeModelLocale(targetDocumentModelAware.getTargetDocumentModel(),
				k -> LocaleEnrichmentHelper.determineEnrichedLocale(context, k));
		}

		if (constraint instanceof SimpleSearchOperator simpleSearchOperator) {
			enrichSimpleSearchOperator(simpleSearchOperator, targetDocumentModel, context);
		}

		enrichers.forEach(e -> e.enrich(constraint, context));

		if (constraint instanceof VariadicOperator variadicOperator) {
			Optional.of(variadicOperator)
				.map(VariadicOperator::getOperands)
				.stream()
				.flatMap(Collection::stream)
				.forEach(c -> walkConstraint(c, targetDocumentModel, context));
		}

		if (constraint instanceof NestingOperator nestingOperator) {
			walkConstraint(nestingOperator.getOperand(), targetDocumentModel, context);
		}

		if (constraint instanceof ConstraintAware constraintAware) {
			String childTargetDocumentModel = context.getEnrichments().getTargetDocumentModel(constraintAware);
			if (childTargetDocumentModel != null) {
				context.getEnrichments().computeModelSubtypes(childTargetDocumentModel, modelTypeService::findAllSubtypes);
				walkConstraint(constraintAware.getConstraint(), childTargetDocumentModel, context);
				if (constraintAware instanceof LinkAware linkAware) {
					String linkDocumentModel = context.getRelationshipModel(linkAware.getRelationshipModel())
						.getContent()
						.getLinkDocumentModel();
					walkConstraint(linkAware.getLinkDocumentConstraint(), linkDocumentModel, context);
				}
			}
		}
	}

	private void enrichFieldAwareOperator(FieldAwareOperator operator, String targetDocumentModel, QueryContext context) {
		FieldPathValidator.validateFieldPath(operator.getField());
		RepeatabilityEnrichmentHelper.enrichRepeatability(operator.getField(), targetDocumentModel, context, false);

		IDocumentModel documentModel = context.getDocumentModel(targetDocumentModel);
		context.getEnrichments().computeModelLocale(targetDocumentModel,
			k -> LocaleEnrichmentHelper.determineEnrichedLocale(context, k));

		Collection<IDocumentModel> models = collectDocumentModels(targetDocumentModel, context, documentModel);

		documentModelUtils.findFieldInModels(models, operator.getField())
			.ifPresent(field -> {
				FieldDescriptor fieldDescriptor = context.getEnrichments().getFieldDescriptor(operator.getField());
				if (fieldDescriptor.getFieldType() == null) {
					fieldDescriptor.setFieldType(fieldTypeAsString(getEffectiveFieldType(field, QUERY_ENRICHMENT)));
				}
				if (operator instanceof DateRangeOperator dateRangeOperator) {
					EnrichmentHelper.enrichDateRangeOperator(dateRangeOperator, field, documentModel, context, QUERY_ENRICHMENT);
				} else if (operator instanceof RangeOperator<?> rangeOperator) {
					context.getEnrichments().getOperatorEnrichment(rangeOperator)
						.computeIfAbsent(Enrichments.FROM_PROPERTY, k -> rangeOperator.getFrom());
					context.getEnrichments().getOperatorEnrichment(rangeOperator)
						.computeIfAbsent(Enrichments.TO_PROPERTY, k -> rangeOperator.getTo());
				} else if (operator instanceof ExactMatchOperator<?> matchOperator) {
					IFieldType effectiveFieldType = getEffectiveFieldType(field, QUERY_ENRICHMENT);
					if ((effectiveFieldType instanceof IDateType || effectiveFieldType instanceof IDateFragmentType)) {
						enrichDateMatchOperator(matchOperator, field, documentModel, context);
					} else {
						context.getEnrichments().getOperatorEnrichment(matchOperator).putIfAbsent(Enrichments.VALUE_PROPERTY, matchOperator.getValue());
					}
				}
			});
	}

	private Optional<String> resolveFieldType(String field, Collection<IDocumentModel> models, String targetDocumentModel) {
		if (targetDocumentModel == null) {
			return Optional.empty();
		}
		return documentModelUtils.findFieldInModels(models, field)
			.map(f -> fieldTypeAsString(getEffectiveFieldType(f, QUERY_ENRICHMENT)));
	}

	@NotNull private static Collection<IDocumentModel> collectDocumentModels(String targetDocumentModel, QueryContext context, IDocumentModel documentModel) {
		return Stream.concat(
				Stream.of(documentModel),
				Optional.ofNullable(context.getEnrichments().getModelSubtypes(targetDocumentModel)).stream()
					.flatMap(Collection::stream)
					.map(context::getDocumentModel))
			.toList();
	}

	private void enrichSimpleSearchOperator(SimpleSearchOperator simpleSearchOperator, String targetDocumentModel, QueryContext context) {
		if (targetDocumentModel == null) {
			// No model is in scope (e.g. a link constraint whose relationship has no link document model), so no field types can be resolved.
			// We set an empty map and skip resolution.
			simpleSearchOperator.setFieldsTypes(Map.of());
			return;
		}
		Collection<IDocumentModel> models = collectDocumentModels(targetDocumentModel, context, context.getDocumentModel(targetDocumentModel));
		Map<String, String> fieldTypeMap = simpleSearchOperator.getFields().stream()
			.flatMap(field -> resolveFieldType(field, models, targetDocumentModel)
				.map(type -> Map.entry(field, type))
				.stream())
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		simpleSearchOperator.setFieldsTypes(fieldTypeMap);
	}

	private static void enrichDateMatchOperator(ExactMatchOperator<?> matchOperator, IField field, IDocumentModel documentModel, QueryContext context) {
		try {
			if (matchOperator.getValue() instanceof String stringValue) {
				String timeZone = documentModel.getContent().getDocumentModelConfig().getTimeZone();
				IFieldType effectiveFieldType = getEffectiveFieldType(field, QUERY_ENRICHMENT);
				if (effectiveFieldType instanceof IDateType dateField) {
					EnrichmentHelper.enrichDate(Enrichments.VALUE_PROPERTY, documentModel, matchOperator, field, stringValue, dateField.getFormat(),
						dateField.arePartiallyKnownDatesAllowed(), false,
						timeZone, context, QUERY_ENRICHMENT
					);
				} else if (effectiveFieldType instanceof IDateFragmentType dateFragmentType) {
					EnrichmentHelper.enrichDate(Enrichments.VALUE_PROPERTY, documentModel, matchOperator, field, stringValue,
						dateFragmentType.getFormatOfFragment(), false, true, timeZone, context, QUERY_ENRICHMENT);
				}
			} else {
				throw new QueryInvalidInputException(QUERY_ENRICHMENT, QUERY_INVALID_INPUT_ERROR_KEY,
					"Date operators must have formatted string value. %s is not supported.".formatted(matchOperator.getValue().getClass().getSimpleName()));
			}
		} catch (NotFoundException e) {
			// Depending on the decision how to treat missing models during validation this should be reworked.
			log.warn("Caught NotFoundException [{}] but ignored it", e.getAnonymityMessage());
		}
	}
}
