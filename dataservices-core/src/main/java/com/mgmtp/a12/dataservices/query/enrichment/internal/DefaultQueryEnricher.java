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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.query.LinkAware;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.TargetDocumentModelAware;
import com.mgmtp.a12.dataservices.query.constraint.FieldAwareOperator;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.NestingOperator;
import com.mgmtp.a12.dataservices.query.constraint.VariadicOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.HasOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.SimpleSearchOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.DateRangeOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.RangeOperator;
import com.mgmtp.a12.dataservices.query.enrichement.Enrichments;
import com.mgmtp.a12.dataservices.query.enrichement.FieldDescriptor;
import com.mgmtp.a12.dataservices.query.enrichment.QueryEnricher;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.internal.EnrichmentHelper;
import com.mgmtp.a12.dataservices.query.internal.QueryTopologyHelper;
import com.mgmtp.a12.dataservices.query.internal.aggregation.RepeatableAggField;
import com.mgmtp.a12.dataservices.query.security.QueryAuthorizationService;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.query.topology.QueryTopology;
import com.mgmtp.a12.dataservices.relationship.model.EntityCharacteristics;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateFragmentType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_ENRICHMENT;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.QUERY_UNSUPPORTED_LOCALE_ERROR_KEY;
import static com.mgmtp.a12.dataservices.model.ModelConstants.FIELD_SEPARATOR;
import static com.mgmtp.a12.dataservices.query.internal.QueryTopologyHelper.fieldTypeAsString;
import static com.mgmtp.a12.dataservices.query.internal.QueryTopologyHelper.getEffectiveFieldType;

@Slf4j
@RequiredArgsConstructor
public class DefaultQueryEnricher implements QueryEnricher {

	private final ModelTypeService modelTypeService;
	private final DocumentModelUtils documentModelUtils;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final QueryAuthorizationService queryAuthorizationService;
	private final DocumentModelServiceFactory documentModelServiceFactory;

	/**
	 * Enriches the provided query topology node by adding computable data such as model subtypes,
	 * link opposite sides, and other related information.
	 *
	 * This method processes the logic constraints of the topology node and,
	 * depending on the type of the node (e.g., `QueryRoot` or `QueryLink`),
	 * enriches it with additional data. It also recursively enriches any linked topology nodes.
	 *
	 * @param queryRoot The topology node to be enriched.
	 * @param context Locale to use for localized queries and responses.
	 */
	@Override public void enrichQuery(QueryRoot queryRoot, QueryContext context) {
		StopWatch stopWatch = StopWatch.createStarted();
		enrichHeterogeneity(queryRoot, context);
		enrichFieldTypes(context, context.getEnrichments().getTargetDocumentModel(queryRoot));
		queryRoot.setConstraint(enrichConstraint(queryRoot.getConstraint(), context.getEnrichments().getTargetDocumentModel(queryRoot), context));

		context.getEnrichments().computeModelLocale(queryRoot.getTargetDocumentModel(), k -> determineEnrichedLocale(context, k));

		Collection<QueryLink> links = queryRoot.getLinks();
		if (links != null) {
			links.forEach(link -> enrichQueryLink(link, context));
		}
		if (queryRoot.isAggregated()) {
			queryRoot.getAggregation().getGroup()
				.forEach(group -> enrichRepeatability(group.getField(), queryRoot.getTargetDocumentModel(), context, true));
			queryRoot.getAggregation().getAggregations()
				.forEach(agg -> enrichRepeatability(agg.getField(), queryRoot.getTargetDocumentModel(), context, true));
		}
		log.atDebug().log("Query enrichment took in {} ms", stopWatch.getDuration().toMillis());
	}

	void enrichFieldTypes(QueryContext context, String targetDocumentModel) {
		// Target document model and all its subtypes are used for enrichment of fields because operators might use fields
		// from any of these models due to heterogeneity
		Set<String> allApplicableModelNames = new LinkedHashSet<>(modelTypeService.findAllSubtypes(targetDocumentModel));
		allApplicableModelNames.add(targetDocumentModel);

		allApplicableModelNames
			.forEach(modelName -> {
				IDocumentModel documentModel = context.getDocumentModel(modelName);
				IDocumentModelSearchService documentModelSearchService = documentModelServiceFactory.createDocumentModelSearchService(documentModel);
				documentModelUtils.getAllFieldPaths(documentModel)
					.forEach(path-> {
						FieldDescriptor fieldDescriptor = context.getEnrichments().getFieldDescriptor(path);
						if (fieldDescriptor.getFieldType() == null) {
							Optional<String> computedType = extractFieldType(documentModelSearchService, path);
							computedType.ifPresent( type ->{
								fieldDescriptor.setFieldType(type);
								fieldDescriptor.setEnumerationType(QueryGeneratorConstants.FieldTypes.ENUMERATION_FIELD_TYPE.equals(type));
							});
						}
					}
				);
			});
	}

	private Optional<String> extractFieldType(IDocumentModelSearchService documentModelSearchService, String path) {
		return DocumentModelUtils.findField(documentModelSearchService, path)
			.map(field -> getEffectiveFieldType(field, QUERY_ENRICHMENT))
			.map(QueryTopologyHelper::fieldTypeAsString);
	}

	private void enrichQueryLink(QueryLink queryLink, QueryContext context) {
		enrichEntityCharacteristics(queryLink, context);
		enrichFieldTypes(context, context.getEnrichments().getTargetDocumentModel(queryLink));
		enrichHeterogeneity(queryLink, context);
		queryLink.setConstraint(enrichConstraint(queryLink.getConstraint(), context.getEnrichments().getTargetDocumentModel(queryLink), context));
		enrichLogicOperator(queryLink.getLinkDocumentConstraint(), getLinkDocumentModel(queryLink.getRelationshipModel(), context), context);

		if (queryLink instanceof TargetDocumentModelAware targetDocumentModelAware) {
			context.getEnrichments().computeModelLocale(targetDocumentModelAware.getTargetDocumentModel(), k -> determineEnrichedLocale(context, k));
		}

		Collection<QueryLink> links = queryLink.getLinks();
		// We set max links size to configured value plus 1 because we want to throw an InvalidInputException if this limit is exceeded
		queryLink.setMaxLinksSize(dataServicesCoreProperties.getQuery().getMaxLinksSize() + 1);
		if (links != null) {
			links.forEach(link -> enrichQueryLink(link, context));
		}
	}

	private void enrichEntityCharacteristics(QueryLink queryLink, QueryContext context) {
		Enrichments enrichments = context.getEnrichments();
		EntityCharacteristics targetEntityCharacteristics = getTargetRoleCharacteristics(queryLink, context);
		EntityCharacteristics sourceEntityCharacteristics = getSourceRoleCharacteristics(queryLink, context);
		String targetDocumentModel = targetEntityCharacteristics.getDocumentModel();
		enrichments.setTargetDocumentModel(queryLink, targetDocumentModel);
		enrichments.computeModelSubtypes(targetDocumentModel, modelTypeService::findAllSubtypes);
		queryLink.setOrdered(Boolean.TRUE.equals(sourceEntityCharacteristics.getOrdered()));
		enrichments.setSourceRole(queryLink, sourceEntityCharacteristics.getRole());
	}

	private void enrichHeterogeneity(QueryTopology queryRoot, QueryContext context) {
		Enrichments enrichments = context.getEnrichments();
		enrichments.computeModelSubtypes(enrichments.getTargetDocumentModel(queryRoot), modelTypeService::findAllSubtypes);
	}

	private void enrichLogicOperator(ILogicOperator constraint, String targetDocumentModel, QueryContext context) {
		if (constraint instanceof FieldAwareOperator fieldAwareOperator) {
			enrichFieldAwareOperator(fieldAwareOperator, targetDocumentModel, context);
		}
		if (constraint instanceof TargetDocumentModelAware targetDocumentModelAware) {
			context.getEnrichments().computeModelLocale(targetDocumentModelAware.getTargetDocumentModel(), k -> determineEnrichedLocale(context, k));
		}
		if (constraint instanceof HasOperator hasOperator) {
			enrichHasOperator(hasOperator, context);
		}

		if (constraint instanceof VariadicOperator variadicOperator) {
			Optional.of(variadicOperator)
				.map(VariadicOperator::getOperands)
				.stream()
				.flatMap(Collection::stream)
				.forEach(c -> enrichLogicOperator(c, targetDocumentModel, context));
		}

		if (constraint instanceof NestingOperator nestingOperator) {
			enrichLogicOperator(nestingOperator.getOperand(), targetDocumentModel, context);
		}

		if (constraint instanceof SimpleSearchOperator simpleSearchOperator) {
			enrichSimpleSearchOperator(simpleSearchOperator, context);
		}
	}

	private void enrichSimpleSearchOperator(SimpleSearchOperator simpleSearchOperator, QueryContext context) {
		Map<String, String> fieldTypeMap = simpleSearchOperator.getFields().stream()
			.collect(Collectors.toMap(
				field -> field,
				field -> context.getEnrichments().getFieldDescriptor(field).getFieldType())
			);

		simpleSearchOperator.setFieldsTypes(fieldTypeMap);
	}

	private void enrichFieldAwareOperator(FieldAwareOperator operator, String targetDocumentModel, QueryContext context) {

		enrichRepeatability(operator.getField(), targetDocumentModel, context, false);

		IDocumentModel documentModel = context.getDocumentModel(targetDocumentModel);
		context.getEnrichments().computeModelLocale(targetDocumentModel, k -> determineEnrichedLocale(context, k));

		Collection<IDocumentModel> models = Stream.concat(
			Stream.of(documentModel),
			Optional.ofNullable(context.getEnrichments().getModelSubtypes(targetDocumentModel)).stream()
				.flatMap(Collection::stream)
				.map(context::getDocumentModel))
			.toList();

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

	private static void enrichRepeatability(String field, String targetDocumentModel, QueryContext context, boolean isAggregated) {
		FieldDescriptor fieldDescriptor = context.getEnrichments().getFieldDescriptor(field);
		if (fieldDescriptor.getRepeatable() != null && !isAggregated) {
			// nothing to do, already set
			return;
		}

		boolean repeatable = false;
		IElement element = context.getDocumentModelSearchService(targetDocumentModel).getByPath(field).orElse(null);
		Deque<String> paths = new ArrayDeque<>();
		String currentField = field;
		while (element != null && element.getParent() != null) {
			currentField = currentField.substring(0, currentField.lastIndexOf(FIELD_SEPARATOR));
			// A field is repeatable if it has at least one parent group that is repeatable
			if (element instanceof IGroup group && group.getRepeatability() > 1) {
				repeatable = true;
				if (isAggregated) {
					// in case of aggregation processing, we're traversing the whole repeatable group for aggregation, therefore will not break the loop
					paths = enrichRepeatableAggField(field, context, element, currentField, paths);
				} else {
					// as soon as we find such a group we can terminate the loop
					break;
				}
			} else {
				paths.push(element.getName());
			}
			element = element.getParent();
		}
		if (isAggregated && repeatable) {
			RepeatableAggField repeatableAggField = new RepeatableAggField("", paths);
			context.getEnrichments().addRepeatableAggField(field, repeatableAggField);
		}

		fieldDescriptor.setRepeatable(repeatable);
	}

	private String determineEnrichedLocale(QueryContext context, String documentModelName) {
		return getEnrichedLocale(context, context.getDocumentModel(documentModelName));
	}

	private static String getEnrichedLocale(QueryContext context, IDocumentModel documentModel) {
		String locale = context.getLocale();
		if (!StringUtils.isBlank(locale) &&
			documentModel.getHeader().getLocales().stream()
				.map(Locale::toString)
				.noneMatch(locale::equalsIgnoreCase)) {
			throw new QueryInvalidInputException(QUERY_ENRICHMENT, QUERY_UNSUPPORTED_LOCALE_ERROR_KEY,
				String.format("Unable to construct query for unsupported locale: %s", locale));
		}
		return StringUtils.isBlank(locale) ? null : locale;
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
					EnrichmentHelper.enrichDate(Enrichments.VALUE_PROPERTY, documentModel, matchOperator, field, stringValue, dateFragmentType.getFormatOfFragment(), false,
						true,
						timeZone, context, QUERY_ENRICHMENT
					);
				}
			} else {
				throw new InvalidInputException(
					"Date operators must have formatted string value. %s is not supported.".formatted(matchOperator.getValue().getClass().getSimpleName()));
			}
		} catch (NotFoundException e) {
			// Depending on the decision how to treat missing models during validation this should be reworked.
			log.warn("Caught NotFoundException [{}] but ignored it", e.getAnonymityMessage());
		}
	}

	private void enrichHasOperator(HasOperator hasOperator, QueryContext context) {

		String targetDocumentModel = getTargetRoleCharacteristics(hasOperator, context).getDocumentModel();
		enrichFieldTypes(context, targetDocumentModel);
		context.getEnrichments().setTargetDocumentModel(hasOperator, targetDocumentModel);
		context.getEnrichments().computeModelSubtypes(targetDocumentModel, modelTypeService::findAllSubtypes);
		context.getEnrichments().setSourceRole(hasOperator, getSourceRoleCharacteristics(hasOperator, context).getRole());

		hasOperator.setConstraint(enrichConstraint(hasOperator.getConstraint(), targetDocumentModel, context));
		enrichLogicOperator(hasOperator.getLinkDocumentConstraint(), getLinkDocumentModel(hasOperator.getRelationshipModel(), context), context);
	}

	private static EntityCharacteristics getTargetRoleCharacteristics(LinkAware linkAware, QueryContext context) {
		RelationshipModel relationshipModel = context.getRelationshipModel(linkAware.getRelationshipModel());
		return relationshipModel.getEntityCharacteristicsFromRole(linkAware.getTargetRole());
	}

	private static EntityCharacteristics getSourceRoleCharacteristics(LinkAware linkAware, QueryContext context) {
		RelationshipModel relationshipModel = context.getRelationshipModel(linkAware.getRelationshipModel());
		// Strictly speaking, this method returns the entity characteristic of the opposite side of the passed role (as specified in the RelationshipModel).
		return relationshipModel.getTargetEntityCharacteristicsFromSourceRole(linkAware.getTargetRole());
	}

	private String getLinkDocumentModel(String relationshipModelName, QueryContext context) {
		String linkDocumentModel = context.getRelationshipModel(relationshipModelName)
			.getContent()
			.getLinkDocumentModel();
		if (linkDocumentModel != null) {
			context.getEnrichments().computeModelSubtypes(linkDocumentModel, modelTypeService::findAllSubtypes);
		}
		return linkDocumentModel;
	}

	private ILogicOperator enrichConstraint(ILogicOperator constraint, String targetDocumentModel, QueryContext context) {
		constraint = queryAuthorizationService.addAbacRules(constraint, targetDocumentModel);
		enrichLogicOperator(constraint, targetDocumentModel, context);
		return constraint;
	}

	/**
	 * Enriches a repeatable aggregate field by generating a unique alias and storing its path.
	 * This method ensures that each repeatable aggregate field gets a unique alias within the
	 * {@link QueryContext} to prevent naming conflicts, especially when dealing with multiple
	 * occurrences of the same field in different aggregation groups.
	 * For an example of conflicting field alias name:
	 *
	 * - We have 2 fields with same element name but different field paths:
	 *
	 * - /EmployeeRoot/Position/Title/Name
	 *
	 * - /EmployeeRoot/Department/Title/Name
	 *
	 * - In this case, `title` will be chosen as the alias for both fields. But they are actually different path,
	 * the enrichment method will try to add `1` to the alias until it finds a unique one.
	 *
	 * - The alias result for each field path will be: `title` for `/EmployeeRoot/Position/Title` path and `title1` for `/EmployeeRoot/Department/Title` path.
	 *
	 * @param field The original field name for which the repeatable aggregate field is being enriched.
	 * @param context The {@link QueryContext} containing enrichment data, including repeatable aggregate group aliases.
	 * @param element The {@link IElement} representing the current element, used to derive the initial alias.
	 * @param currentField The current field name being processed, used to check for existing aliases.
	 * @param paths A {@link Deque} of strings representing the current path of the field, which will be
	 *              associated with the generated alias.
	 * @return A {@link Deque} of strings containing the newly generated alias, pushed onto the deque.
	 */
	private static Deque<String> enrichRepeatableAggField(String field, QueryContext context, IElement element, String currentField, Deque<String> paths) {
		StringBuilder tempAlias = new StringBuilder(element.getName());
		Deque<String> resultPaths = new ArrayDeque<>();
		while (context.getEnrichments().getRepeatableAggGroupAliases().containsKey(tempAlias.toString())
				&& !context.getEnrichments().getRepeatableAggGroupAliases().get(tempAlias.toString()).equals(
			currentField)) {
			tempAlias.append(QueryGeneratorConstants.ONE_VALUE);
		}
		if (!context.getEnrichments().getRepeatableAggGroupAliases().containsKey(tempAlias.toString())) {
			context.getEnrichments().getRepeatableAggGroupAliases().put(tempAlias.toString(), currentField);
		}
		RepeatableAggField repeatableAggField = new RepeatableAggField(tempAlias.toString(), paths);
		context.getEnrichments().addRepeatableAggField(field, repeatableAggField);
		resultPaths.push(tempAlias.toString());
		return resultPaths;
	}
}
