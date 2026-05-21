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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.internal.verification.Times;
import org.testng.annotations.Test;

import com.jayway.jsonpath.JsonPath;
import com.mgmtp.a12.dataservices.AbstractQueryContextAwareTest;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.authorization.internal.DefaultDocumentModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants;
import com.mgmtp.a12.dataservices.model.internal.DefaultModelTypeService;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.logical.AndOperator;
import com.mgmtp.a12.dataservices.query.constraint.logical.NotOperator;
import com.mgmtp.a12.dataservices.query.constraint.logical.OrOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.HasOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.DoubleRangeOperator;
import com.mgmtp.a12.dataservices.query.internal.DefaultQueryContext;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.query.validation.ValidationItem;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipUtils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertFalse;

@Slf4j
public class QueryValidatorTest extends AbstractQueryContextAwareTest {

	private final DataServicesCoreProperties dataServicesCoreProperties = spy(new DataServicesCoreProperties());
	@Mock private final @NonNull ModelPermissionEvaluator<IDocumentModel> documentModelPermissionEvaluator =
		mock(DefaultDocumentModelPermissionEvaluator.class);
	@Mock private final DefaultModelTypeService modelTypeService = mock(DefaultModelTypeService.class);
	@Mock private final DefaultQueryContext queryContext = mock(DefaultQueryContext.class);
	@Spy private final @NonNull RelationshipUtils relationshipUtils = new RelationshipUtils(modelTypeService);
	@Spy private final @NonNull LinkAwareValidator linkAwareValidator = new LinkAwareValidator(modelTypeService, dataServicesCoreProperties, relationshipUtils);
	@Spy private final @NonNull QueryValidator queryValidator = new QueryValidator(dataServicesCoreProperties, documentModelPermissionEvaluator,
		linkAwareValidator, relationshipModelLoader, mock(FieldsValidator.class));
	@Spy private final @NonNull VariadicOperatorValidator variadicOperatorValidator = new VariadicOperatorValidator(dataServicesCoreProperties);

	@SneakyThrows
	@Test public void testRecursion() {
		Mockito.reset(dataServicesCoreProperties);
		QueryRoot qr = QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.constraint(AndOperator.builder()
				.operand(ExactMatchOperator.builder()
					.field("ContractRoot/Name")
					.value("Project Manhattan")
					.build())
				.operand(HasOperator.builder()
					.relationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL)
					.targetRole(RoleConstants.PARTNER_ROLE)
					.constraint(NotOperator.builder()
						.operand(DoubleRangeOperator.builder()
							.field("BusinessPartner/Revenue")
							.from(10_000d)
							.build())
						.build())
					.build())
				.build())
			.link(QueryLink.builder()
				.relationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL)
				.targetRole(RoleConstants.PARTNER_ROLE)
				.linkDocumentConstraint(ExactMatchOperator.builder()
					.field("BusinessPartner/Name")
					.value("Stark Industries")
					.build())
				.build())
			.link(QueryLink.builder()
				.relationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL)
				.targetRole(RoleConstants.PARTNER_ROLE)
				.build())
			.link(QueryLink.builder()
				.relationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL)
				.targetRole(RoleConstants.PARTNER_ROLE)
				.constraint(HasOperator.builder()
					.relationshipModel(RelationshipModelConstants.PARTNER_ADDRESSES_MODEL)
					.targetRole(RoleConstants.ADDRESS_ROLE)
					.build())
				.build())

			.build();

		List<String> topologyPaths = new ArrayList<>();
		List<String> operatorPaths = new ArrayList<>();

		doAnswer(a -> {
			topologyPaths.add(constructJsonPathDefinition(a.getArgument(1, String[].class)));
			return a.callRealMethod();
		})
			.when(queryValidator)
			.validateTopologyNode(any(), any(), any(), any(), anyBoolean());

		doAnswer(a -> {
			operatorPaths.add(constructJsonPathDefinition(a.getArgument(2, String[].class)));
			return a.callRealMethod();
		})
			.when(queryValidator)
			.validateOperators(nullable(ILogicOperator.class), nullable(String.class), any(String[].class), any(QueryContext.class),
				any(ValidationResult.class), anyBoolean());

		QueryContext queryContext = newQueryContext();
		ValidationResult vr = queryValidator.validate(qr, queryContext, true);

		String json = jsonMapper.writeValueAsString(qr);
		log.info("Query: {}", json);
		log.info("Topology paths: {}", topologyPaths);
		log.info("Operator paths: {}", operatorPaths);

		assertTrue(vr.hasErrors());
		vr.getErrors();

		verify(queryValidator, new Times(1))
			.validate(any(), eq(queryContext), anyBoolean());

		verify(queryValidator, new Times(1))
			.validateTopologyNode(eq(qr), any(String[].class), any(QueryContext.class), any(ValidationResult.class), anyBoolean());

		Map<String[], String> expectedOperators = Map.of(
			new String[] { "$", "constraint" }, "AndOperator",
			new String[] { "$", "constraint", "operands", "[0]" }, "ExactMatchOperator",
			new String[] { "$", "constraint", "operands", "[1]" }, "HasOperator",
			new String[] { "$", "constraint", "operands", "[1]", "constraint" }, "NotOperator",
			new String[] { "$", "constraint", "operands", "[1]", "constraint", "operand" }, "DoubleRangeOperator",
			new String[] { "$", "links", "[0]", "linkDocumentConstraint" }, "ExactMatchOperator",
			new String[] { "$", "links", "[2]", "constraint" }, "HasOperator"
		);

		expectedOperators.forEach((k, v) -> {
			log.info("{}: {}", k, v);
			verify(queryValidator, new Times(1))
				.validateOperators(nullable(ILogicOperator.class), nullable(String.class), eq(k), eq(queryContext), any(ValidationResult.class), anyBoolean());

			Object node = JsonPath.read(json, constructJsonPathDefinition(k));
			if (node instanceof Map<?, ?> operatorMap) {
				assertEquals(operatorMap.get("operator"), v);
			} else {
				fail();
			}

		});

		verify(queryValidator, new Times(expectedOperators.size()))
			.validateOperators(nullable(ILogicOperator.class), nullable(String.class), any(String[].class), any(QueryContext.class),
				any(ValidationResult.class), anyBoolean());
	}

	@Test
	public void testDisabledOperators() {
		DataServicesCoreProperties.Query query = new DataServicesCoreProperties.Query();
		query.setDisabledOperators(List.of("and"));
		Mockito.when(dataServicesCoreProperties.getQuery()).thenReturn(query);
		ILogicOperator andOperator = AndOperator.builder().build();
		Mockito.when(queryContext.getOperatorName(andOperator)).thenReturn("and");
		ValidationResult result = new ValidationResult();

		queryValidator.validateOperators(andOperator, "someDocumentModel", new String[] { "path" }, queryContext, result, true);

		assertTrue(result.hasErrors());
		assertNotNull(result.getErrors().stream()
			.map(ValidationItem::message)
			.filter(m -> m.equals("The operator [and] is disabled by configuration."))
			.findFirst()
			.orElse(null));
	}

	@Test
	public void testDisabledOperatorsWithValidationDisabled() {
		DataServicesCoreProperties.Query query = new DataServicesCoreProperties.Query();
		query.setDisabledOperators(List.of("and"));
		Mockito.when(dataServicesCoreProperties.getQuery()).thenReturn(query);
		ILogicOperator andOperator = AndOperator.builder().build();
		Mockito.when(queryContext.getOperatorName(andOperator)).thenReturn("and");
		ValidationResult result = new ValidationResult();

		queryValidator.validateOperators(andOperator, "someDocumentModel", new String[] { "path" }, queryContext, result, false);

		assertFalse(result.hasErrors());
	}

	@Test
	public void testMaxAndOperatorsWithValidationDisabled() {
		DataServicesCoreProperties.Query query = new DataServicesCoreProperties.Query();
		query.setDisabledOperators(Collections.emptyList());
		Mockito.when(dataServicesCoreProperties.getQuery()).thenReturn(query);
		ILogicOperator andOperator = AndOperator.builder().build();
		Mockito.when(queryContext.getOperatorName(andOperator)).thenReturn("and");
		Mockito.when(queryContext.addAndGetNumberOfAndOperators()).thenReturn(1050);
		ValidationResult result = new ValidationResult();

		queryValidator.validateOperators(andOperator, "someDocumentModel", new String[] { "path" }, queryContext, result, false);

		assertTrue(result.hasErrors());
		assertNotNull(result.getErrors().stream()
			.map(ValidationItem::message)
			.filter(m -> m.equals("Maximum number of `and` operators [=1000] exceeded"))
			.findFirst()
			.orElse(null));
	}

	@Test
	public void testMaxOrOperatorsWithValidationDisabled() {
		DataServicesCoreProperties.Query query = new DataServicesCoreProperties.Query();
		query.setDisabledOperators(Collections.emptyList());
		Mockito.when(dataServicesCoreProperties.getQuery()).thenReturn(query);
		ILogicOperator orOperator = OrOperator.builder().build();
		Mockito.when(queryContext.getOperatorName(orOperator)).thenReturn("or");
		Mockito.when(queryContext.addAndGetNumberOfOrOperators()).thenReturn(1050);
		ValidationResult result = new ValidationResult();

		queryValidator.validateOperators(orOperator, "someDocumentModel", new String[] { "path" }, queryContext, result, false);

		assertTrue(result.hasErrors());
		assertNotNull(result.getErrors().stream()
			.map(ValidationItem::message)
			.filter(m -> m.equals("Maximum number of `or` operators [=1000] exceeded"))
			.findFirst()
			.orElse(null));
	}

	@Test
	public void testMaxAndOperandsWithValidationDisabled() {
		DataServicesCoreProperties.Query query = new DataServicesCoreProperties.Query();
		query.setDisabledOperators(List.of("and"));
		query.setMaxAndOperands(5);
		Mockito.when(dataServicesCoreProperties.getQuery()).thenReturn(query);
		ILogicOperator andOperator = AndOperator.builder()
			.operand(null)
			.operand(null)
			.operand(null)
			.operand(null)
			.operand(null)
			.operand(null)
			.build();
		Mockito.when(queryContext.getOperatorName(andOperator)).thenReturn("and");

		Collection<ValidationItem> validationItems =
			variadicOperatorValidator.validate(andOperator, "someDocumentModel", new String[] { "path" }, queryContext, false);

		assertNotNull(validationItems);
		assertFalse(validationItems.isEmpty());
		assertNotNull(validationItems.stream()
			.map(ValidationItem::message)
			.filter(m -> m.equals("Only 5 operands are allowed for an `and` operator."))
			.findFirst()
			.orElse(null));
	}

	@Test
	public void testMaxOrOperandsWithValidationDisabled() {
		DataServicesCoreProperties.Query query = new DataServicesCoreProperties.Query();
		query.setDisabledOperators(List.of("or"));
		query.setMaxOrOperands(5);
		Mockito.when(dataServicesCoreProperties.getQuery()).thenReturn(query);
		OrOperator.OrOperatorBuilder<?, ?> builder = OrOperator.builder();
		for (int i = 0; i < 1050; i++) {
			builder.operand(null);
		}
		OrOperator orOperator = builder.build();
		Mockito.when(queryContext.getOperatorName(orOperator)).thenReturn("or");

		Collection<ValidationItem> validationItems =
			variadicOperatorValidator.validate(orOperator, "someDocumentModel", new String[] { "path" }, queryContext, false);

		assertNotNull(validationItems);
		assertFalse(validationItems.isEmpty());
		assertNotNull(validationItems.stream()
			.map(ValidationItem::message)
			.filter(m -> m.equals("Only 5 operands are allowed for an `or` operator."))
			.findFirst()
			.orElse(null));
	}

	@NotNull private static String constructJsonPathDefinition(@NonNull String[] argument) {
		return String.join(".", argument).replaceAll("\\.\\[", "[");
	}
}
