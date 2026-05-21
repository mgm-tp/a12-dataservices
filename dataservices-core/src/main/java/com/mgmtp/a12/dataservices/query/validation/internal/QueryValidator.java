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
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.authorization.AuthConstants;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.query.ConstraintAware;
import com.mgmtp.a12.dataservices.query.LinkAware;
import com.mgmtp.a12.dataservices.query.Order;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.TargetDocumentModelAware;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.internal.QueryVisitor;
import com.mgmtp.a12.dataservices.query.internal.QueryWalker;
import com.mgmtp.a12.dataservices.query.projection.internal.ExportCddCsvProjectionImplementation;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.query.topology.QueryTopology;
import com.mgmtp.a12.dataservices.query.validation.ValidationItem;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipRoleNameNotFoundException;
import com.mgmtp.a12.dataservices.relationship.model.EntityCharacteristics;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.request.internal.QueryPagingHelper;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component public class QueryValidator {

	public static final String TARGET_DOCUMENT_MODEL_FIELD_NAME = "targetDocumentModel";
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final ModelPermissionEvaluator<IDocumentModel> documentModelPermissionEvaluator;
	private final LinkAwareValidator linkAwareValidator;
	private final IModelLoader<RelationshipModel> relationshipModelLoader;
	private final FieldsValidator fieldsValidator;

	/**
	 * Validates the query structure, paging, sorting and members (topology ones as well as operators).
	 *
	 * @param queryRoot the query root
	 * @param context the query context
	 * @param validationEnabled if false, only mandatory validations are executed (e.g. security related checks)
	 * @return the validation result
	 */
	public ValidationResult validate(QueryRoot queryRoot, QueryContext context, boolean validationEnabled) {
		StopWatch stopWatch = StopWatch.createStarted();
		ValidationResult result = new ValidationResult();
		validateStructure(queryRoot, context, result, validationEnabled);
		validatePaging(queryRoot, result, validationEnabled);
		validateSorting(queryRoot, result, validationEnabled);
		validateMembers(queryRoot, context, result, validationEnabled);
		log.debug("Query validation took {}.", stopWatch.formatTime());
		return result;
	}

	private void validateMembers(QueryRoot queryRoot, QueryContext context, ValidationResult result, boolean validationEnabled) {
		new QueryWalker(new QueryVisitor() {
			@Override public VisitState visitTopology(QueryTopology topology, String parentDocumentModel, @NonNull String[] path) {
				validateTopologyNode(topology, path, context, result, validationEnabled);
				return VisitState.DIVE;
			}

			@Override public VisitState visitOperator(ILogicOperator operator, String parentDocumentModel, @NonNull String[] path) {
				validateOperators(operator, parentDocumentModel, path, context, result, validationEnabled);
				return VisitState.DIVE;
			}
		}, constraintAware -> getTargetDocumentModel(constraintAware, context))
			.walk(queryRoot);
	}

	void validateTopologyNode(QueryTopology topology, @NonNull String[] path, QueryContext context,
		ValidationResult result, boolean validationEnabled) {
		if (topology instanceof TargetDocumentModelAware targetDocumentModelAware) {
			validateTargetDocumentAware(targetDocumentModelAware, path, result, validationEnabled);
		}
		if (topology instanceof LinkAware linkAware) {
			linkAwareValidator.validate(linkAware, path, context, result, validationEnabled);
		}
		fieldsValidator.validateFieldsAndAggregations(topology, getTargetDocumentModel(topology, context), path, context, result,
			validationEnabled);
	}

	private String getTargetDocumentModel(ConstraintAware constraintAware, QueryContext context) {
		if (constraintAware instanceof TargetDocumentModelAware targetDocumentModelAware) {
			return targetDocumentModelAware.getTargetDocumentModel();
		} else if (context.getEnrichments().getTargetDocumentModel(constraintAware) == null && constraintAware instanceof LinkAware linkAware) {
			String relationshipModelName = linkAware.getRelationshipModel();
			if (relationshipModelName != null) {
				RelationshipModel relationshipModel = relationshipModelLoader.loadModel(relationshipModelName);
				String targetRole = linkAware.getTargetRole();
				if (targetRole != null) {
					try {
						EntityCharacteristics targetCharacteristics = relationshipModel.getEntityCharacteristicsFromRole(targetRole);
						context.getEnrichments().setTargetDocumentModel(linkAware, targetCharacteristics.getDocumentModel());
					} catch (RelationshipRoleNameNotFoundException e) {
						// ignore, will be validated later
					}
				}
			}
		}
		return context.getEnrichments().getTargetDocumentModel(constraintAware);
	}

	/**
	 * The validation should check syntax correctness of the structure, using JSON schema.
	 * JSON schema must be at least partially generated to include also extra added operators. It should
	 * be enough to generate it during build time and store the static resource.
	 */
	private void validateStructure(QueryRoot queryRoot, QueryContext context, ValidationResult result, boolean validationEnabled) {
		// TODO A12S-5484: Validate JSON Schema
	}

	/**
	 * Existence of models in TargetDocumentModelAware members must be tested and also
	 * accessibility by the current user. QueryRootValidator and QueryLinkValidator are responsible for
	 * this.
	 */
	private void validateTargetDocumentAware(TargetDocumentModelAware targetDocumentModelAware, @NonNull String[] path, ValidationResult result,
		boolean validationEnabled) {
		if (validationEnabled) {
			String dm = targetDocumentModelAware.getTargetDocumentModel();
			if (dm == null) {
				if (!(targetDocumentModelAware instanceof LinkAware)) {
					result.addResult(ValidationItem.invalid(ArrayUtils.add(path, TARGET_DOCUMENT_MODEL_FIELD_NAME), "Target document model is missing."));
				} else {
					result.addResult(ValidationItem.valid(ArrayUtils.add(path, TARGET_DOCUMENT_MODEL_FIELD_NAME), "TargetDocumentModel is not required"));
				}
			} else {
				if (!documentModelPermissionEvaluator.hasModelReadPermission(dm)) {
					result.addResult(ValidationItem.invalid(ArrayUtils.add(path, TARGET_DOCUMENT_MODEL_FIELD_NAME), AuthConstants.ACCESS_DENIED));
				} else {
					result.addResult(ValidationItem.valid(ArrayUtils.add(path, TARGET_DOCUMENT_MODEL_FIELD_NAME),
						"TargetDocumentModelAware with model %s is valid".formatted(dm)));
				}
			}
		}
	}

	/**
	 * Paging must be tested to be present, including valid page number and size, all in bounds of hard
	 * limits. QueryPagingValidator is responsible for this.
	 */
	private void validatePaging(QueryRoot queryRoot, ValidationResult result, boolean validationEnabled) {
		if (validationEnabled) {
			if (ExportCddCsvProjectionImplementation.PROJECTION_NAME.equals(queryRoot.getProjectionName()))
				return;

			DataServicesCoreProperties.Query.PageRequest pageRequestProperties = dataServicesCoreProperties.getQuery().getPageRequest();
			try {
				QueryPagingHelper.validatePageRequest(queryRoot, pageRequestProperties.getPageNumberLimit(), pageRequestProperties.getPageSizeLimit());
				result.addResult(ValidationItem.valid(new String[] { "paging" }, "Paging validated"));
			} catch (InvalidInputException e) {
				result.addResult(ValidationItem.invalid(new String[] { "paging" }, e.getMessage()));
			}
		}
	}

	/**
	 * If sorting is present, we have to check for mandatory properties.
	 */
	private static void validateSorting(QueryRoot queryRoot, ValidationResult result, boolean validationEnabled) {
		if (validationEnabled && CollectionUtils.isNotEmpty(queryRoot.getSort()) && ignoreCaseIsMissing(queryRoot.getSort())) {
			result.addResult(ValidationItem.invalid(new String[] { "sorting" }, "The property ignoreCase must not be null in a sort specification"));
		}
	}

	private static boolean ignoreCaseIsMissing(List<Order> sort) {
		return sort.stream()
			.map(Order::ignoreCase)
			.anyMatch(Objects::isNull);
	}

	/**
	 * Regarding valid data in the operator, we should define IQueryOperatorValidator to provide
	 * extensible validation ability. But at least we will check that every operator in the tree exists by its
	 * name (QueryRootValidator and QueryLinkValidator are responsible for this).
	 *
	 * So, similarly to enrichment, let’s walk through the query tree and for every operator, apply
	 * IQueryOperatorValidators. There could be multiple validators applied on single operator, to
	 * combine common validation and operator-specific validation.
	 *
	 * Each IQueryOperatorValidator will decide inside the validate(operator: ILogicOperator) method
	 * whether it wants to validate this type of operator or immediately exits with null as a result.
	 *
	 * If it decides to validate the operator, then it must return valid instance of
	 * QueryOperatorValidationResult with path and operator set to the validated operator and either
	 * valid as true or with proper message describing the error.
	 *
	 * Each IQueryOperatorValidator extending FieldAwareOperator should be responsible for checking
	 * if this operator supports the field type. Also, other validations like date format, numeric range or
	 * enum value should happen here.
	 */
	void validateOperators(@NonNull ILogicOperator operator, String parentDocumentModel, @NonNull String[] path, @NonNull QueryContext context,
		@NonNull ValidationResult result, boolean validationEnabled) {

		if (validationEnabled) {
			if (dataServicesCoreProperties.getQuery().getDisabledOperators().contains(context.getOperatorName(operator))) {
				result.addResult(ValidationItem.invalid(path, "The operator [%s] is disabled by configuration.".formatted(context.getOperatorName(operator))));
			} else {
				result.addResult(ValidationItem.valid(path, "The operator [%s] is not disabled.".formatted(context.getOperatorName(operator))));
			}
		}

		if ("and".equals(context.getOperatorName(operator))) {
			if (context.addAndGetNumberOfAndOperators() > dataServicesCoreProperties.getQuery().getMaxAndOperators()) {
				result.addResult(ValidationItem.invalid(path,
					"Maximum number of `and` operators [=%d] exceeded".formatted(dataServicesCoreProperties.getQuery().getMaxAndOperators())));
			} else {
				result.addResult(ValidationItem.valid(path,
					"Maximum number of `and` operators [=%d] is not exceeded".formatted(dataServicesCoreProperties.getQuery().getMaxAndOperators())));
			}
		}

		if ("or".equals(context.getOperatorName(operator))) {
			if (context.addAndGetNumberOfOrOperators() > dataServicesCoreProperties.getQuery().getMaxAndOperators()) {
				result.addResult(ValidationItem.invalid(path,
					"Maximum number of `or` operators [=%d] exceeded".formatted(dataServicesCoreProperties.getQuery().getMaxAndOperators())));
			} else {
				result.addResult(ValidationItem.valid(path,
					"Maximum number of `or` operators [=%d] is not exceeded".formatted(dataServicesCoreProperties.getQuery().getMaxAndOperators())));
			}
		}

		if (operator instanceof LinkAware linkAware) {
			linkAwareValidator.validate(linkAware, path, context, result, validationEnabled);
		}
		if (operator instanceof TargetDocumentModelAware targetDocumentModelAware) {
			validateTargetDocumentAware(targetDocumentModelAware, path, result, validationEnabled);
		}

		context.getValidators(operator.getClass()).stream()
			.flatMap(v -> v.validate(operator, parentDocumentModel, path, context, validationEnabled).stream())
			.forEach(result::addResult);
	}
}
