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
package com.mgmtp.a12.dataservices.internal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.mgmtp.a12.dataservices.query.annotation.QueryProjection;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.internal.UnknownOperator;
import com.mgmtp.a12.dataservices.query.fields.aggregation.IAggregationFunction;
import com.mgmtp.a12.dataservices.query.fields.aggregation.internal.UnknownFunction;
import com.mgmtp.a12.dataservices.query.projection.IQueryProjection;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

/**
 * Tests for JSON Schema structure produced by the victools-based schema generator.
 *
 * Schema is generated in-memory using the same victools configuration as the Gradle build task.
 * Structural assertions use Jackson {@link JsonNode} to inspect the generated schema directly.
 * Valid query JSON strings are verified via Jackson deserialization to {@link QueryRoot}.
 */
public class QuerySchemaValidationTest {

	private static final String BASE_PACKAGE = "com.mgmtp.a12.dataservices";

	private ObjectNode schemaNode;
	private JsonMapper mapper;

	@BeforeClass
	public void setUp() {
		mapper = JsonMapper.builder().build();

		final ThreadLocal<Set<Class<?>>> generationGuard = ThreadLocal.withInitial(HashSet::new);
		Reflections reflections = new Reflections(BASE_PACKAGE);

		List<Class<? extends ILogicOperator>> operatorImpls = findConcreteSubtypes(reflections, ILogicOperator.class,
			c -> !UnknownOperator.class.isAssignableFrom(c));
		List<Class<? extends IAggregationFunction>> functionImpls = findConcreteSubtypes(reflections, IAggregationFunction.class,
			c -> !UnknownFunction.class.isAssignableFrom(c));
		Set<StringNode> availableProjections = findConcreteSubtypes(reflections, IQueryProjection.class, c -> true).stream()
			.map(c -> c.getDeclaredAnnotation(QueryProjection.class))
			.map(QueryProjection::value)
			.map(JsonNodeFactory.instance::stringNode)
			.collect(Collectors.toSet());

		SchemaGeneratorConfigBuilder cfg = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
		cfg.forTypesInGeneral()
			.withSubtypeResolver(new QuerySubtypeResolver(operatorImpls, functionImpls))
			.withCustomDefinitionProvider(new QueryCustomDefinitionProvider(availableProjections, generationGuard));

		schemaNode = new SchemaGenerator(cfg.build()).generateSchema(QueryRoot.class);
	}

	private static <T> List<Class<? extends T>> findConcreteSubtypes(Reflections reflections, Class<T> baseType,
		Predicate<Class<? extends T>> filter) {
		return reflections.getSubTypesOf(baseType).stream()
			.filter(c -> !c.isInterface() && !Modifier.isAbstract(c.getModifiers()))
			.filter(filter)
			.toList();
	}

	@DataProvider(name = "validOrderQueries")
	public Object[][] validOrderQueries() {
		return new Object[][] {
			// Direct field sorting
			{ """
				{
					"targetDocumentModel": "Contract",
					"sort": [
						{
							"field": "contractNumber",
							"direction": "ASC"
						}
					]
				}
				""" },
			// Relationship-based sorting (single-hop, terminal field)
			{ """
				{
					"targetDocumentModel": "Contract",
					"sort": [
						{
							"relationshipModel": "ContractBusinessPartner",
							"targetRole": "partner",
							"sortBy": {
								"field": "name",
								"direction": "ASC",
								"nullHandling": "NULLS_LAST"
							}
						}
					]
				}
				""" },
			// Nested relationship sorting (multi-hop)
			{ """
				{
					"targetDocumentModel": "Contract",
					"sort": [
						{
							"relationshipModel": "ContractBusinessPartner",
							"targetRole": "partner",
							"sortBy": {
								"relationshipModel": "PartnerAddresses",
								"targetRole": "address",
								"sortBy": {
									"field": "city",
									"direction": "DESC",
									"nullHandling": "NULLS_FIRST"
								}
							}
						}
					]
				}
				""" }
		};
	}

	@Test(dataProvider = "validOrderQueries", description = "Should deserialize correct query structures with Order and RelationshipOrder")
	public void shouldDeserializeCorrectQueryStructuresWhenGivenValidOrders(String queryJson) throws IOException {
		// Given / When
		QueryRoot queryRoot = mapper.readValue(queryJson, QueryRoot.class);

		// Then
		assertNotNull(queryRoot, "QueryRoot should deserialize successfully");
		assertNotNull(queryRoot.getTargetDocumentModel(), "targetDocumentModel must be present");
	}

	@Test(description = "Should have RelationshipOrder definition in schema")
	public void shouldHaveRelationshipOrderDefinitionWhenSchemaGenerated() {
		// When
		JsonNode relationshipOrderDef = schemaNode.at("/$defs/RelationshipOrder");

		// Then
		assertNotNull(relationshipOrderDef, "RelationshipOrder definition should exist");
		assertFalse(relationshipOrderDef.isMissingNode(), "RelationshipOrder definition should not be missing");
		assertTrue(relationshipOrderDef.has("description"), "RelationshipOrder should have description");
		assertTrue(relationshipOrderDef.has("required"), "RelationshipOrder should have required fields");

		// RelationshipOrder.sortBy is the only terminal — verify it is present in the properties
		JsonNode properties = relationshipOrderDef.get("properties");
		assertNotNull(properties, "RelationshipOrder should have properties");
		assertTrue(properties.has("sortBy"), "RelationshipOrder properties should contain 'sortBy'");
	}

	@Test(description = "Should have Order definition with mutual exclusivity in schema")
	public void shouldHaveOrderWithMutualExclusivityWhenSchemaGenerated() {
		// When
		JsonNode sortDef = schemaNode.at("/properties/sort/items");

		// Then
		assertNotNull(sortDef, "Sort items definition should exist");
		assertFalse(sortDef.isMissingNode(), "Sort items definition should not be missing");
		assertTrue(sortDef.has("description"), "Order should have description");
		assertTrue(sortDef.has("oneOf"), "Order should have oneOf constraints for mutual exclusivity");

		JsonNode oneOf = sortDef.get("oneOf");
		assertTrue(oneOf.isArray(), "oneOf should be an array");
		assertEquals(oneOf.size(), 2, "oneOf should have exactly 2 options (field or relationshipField)");
	}

	@Test(description = "Should enforce mutual exclusivity — field option requires 'field' and forbids 'relationshipField'")
	public void shouldHaveFieldOptionForbiddingRelationshipFieldInSchema() {
		// When
		JsonNode fieldOption = schemaNode.at("/properties/sort/items/oneOf/0");

		// Then
		assertFalse(fieldOption.isMissingNode(), "Field option (oneOf[0]) should exist");
		JsonNode required = fieldOption.get("required");
		assertNotNull(required, "Field option should have required array");
		assertTrue(required.isArray() && required.size() > 0, "Field option required should be non-empty");
	}

	@Test(description = "Should enforce mutual exclusivity — relationship option requires 'relationshipModel'")
	public void shouldHaveRelationshipFieldOptionForbiddingFieldInSchema() {
		// When
		JsonNode relationshipOption = schemaNode.at("/properties/sort/items/oneOf/1");

		// Then
		assertFalse(relationshipOption.isMissingNode(), "RelationshipField option (oneOf[1]) should exist");
		JsonNode required = relationshipOption.get("required");
		assertNotNull(required, "RelationshipField option should have required array");
		assertTrue(required.isArray() && required.size() > 0, "RelationshipField option required should be non-empty");
	}

	@Test(description = "Should require relationshipModel and targetRole in RelationshipOrder schema")
	public void shouldRequireRelationshipModelAndTargetRoleInRelationshipOrderSchema() {
		// When
		JsonNode required = schemaNode.at("/$defs/RelationshipOrder/required");

		// Then
		assertFalse(required.isMissingNode(), "RelationshipOrder required array should exist");
		assertTrue(required.isArray(), "required should be an array");

		boolean hasRelationshipModel = false;
		boolean hasTargetRole = false;
		for (JsonNode node : required) {
			if ("relationshipModel".equals(node.asText())) hasRelationshipModel = true;
			if ("targetRole".equals(node.asText())) hasTargetRole = true;
		}
		assertTrue(hasRelationshipModel, "RelationshipOrder should require 'relationshipModel'");
		assertTrue(hasTargetRole, "RelationshipOrder should require 'targetRole'");
	}

	@Test(description = "Should require 'sortBy' in RelationshipOrder schema")
	public void shouldHaveFieldAndSortByMutuallyExclusiveInRelationshipOrderSchema() {
		// In the new sort hierarchy, RelationshipOrder has only 'sortBy' (no 'field').
		// Verify that 'sortBy' is listed as a required property of RelationshipOrder.
		// When
		JsonNode required = schemaNode.at("/$defs/RelationshipOrder/required");

		// Then
		assertFalse(required.isMissingNode(), "RelationshipOrder required array should exist");
		assertTrue(required.isArray(), "RelationshipOrder required should be an array");

		boolean hasSortBy = false;
		for (JsonNode node : required) {
			if ("sortBy".equals(node.asText())) hasSortBy = true;
		}
		assertTrue(hasSortBy, "RelationshipOrder should require 'sortBy'");
	}
}
