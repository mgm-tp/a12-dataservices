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
package com.mgmtp.a12.dataservices.query;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSerializationTest;
import com.mgmtp.a12.dataservices.exception.query.QueryValidationException;

import lombok.SneakyThrows;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link RelationshipOrder} and {@link DirectFieldOrder}.
 */
public class RelationshipOrderTest extends AbstractSerializationTest {

	@Test(description = "Should create direct field order with field")
	public void shouldCreateDirectFieldOrderWithField() {
		// Given
		DirectFieldOrder order = new DirectFieldOrder(
			"name", DirectFieldOrder.Direction.ASC, false, DirectFieldOrder.NullHandling.NATIVE);

		// When / Then
		assertEquals(order.field(), "name");
		assertEquals(order.direction(), DirectFieldOrder.Direction.ASC);
	}

	@Test(description = "Should create relationship order with nested direct field")
	public void shouldCreateRelationshipOrderWithNestedDirectField() {
		// Given
		DirectFieldOrder terminal = new DirectFieldOrder(
			"city", DirectFieldOrder.Direction.ASC, false, DirectFieldOrder.NullHandling.NATIVE);
		RelationshipOrder order = new RelationshipOrder(
			"ContractBusinessPartner", "PartnerRole", terminal);

		// When / Then
		assertTrue(order.sortBy() instanceof DirectFieldOrder);
		assertEquals(order.relationshipModel(), "ContractBusinessPartner");
	}

	@Test(description = "Should have depth 1 for direct field order")
	public void shouldCalculateDepthAs1ForDirectFieldOrder() {
		// Given
		DirectFieldOrder order = new DirectFieldOrder(
			"name", DirectFieldOrder.Direction.ASC, false, DirectFieldOrder.NullHandling.NATIVE);

		// When
		int depth = computeDepth(order);

		// Then
		assertEquals(depth, 1);
	}

	@Test(description = "Should have depth 2 for single-level nested relationship")
	public void shouldCalculateDepthAs2ForSingleLevelNestedOrder() {
		// Given
		DirectFieldOrder terminal = new DirectFieldOrder(
			"city", DirectFieldOrder.Direction.ASC, false, DirectFieldOrder.NullHandling.NATIVE);
		RelationshipOrder order = new RelationshipOrder(
			"ContractBusinessPartner", "PartnerRole", terminal);

		// When
		int depth = computeDepth(order);

		// Then
		assertEquals(depth, 2);
	}

	@Test(description = "Should have depth 3 for two-level nested relationship")
	public void shouldCalculateDepthAs3ForTwoLevelNestedOrder() {
		// Given
		DirectFieldOrder level3 = new DirectFieldOrder(
			"name", DirectFieldOrder.Direction.ASC, false, DirectFieldOrder.NullHandling.NATIVE);
		RelationshipOrder level2 = new RelationshipOrder(
			"PartnerAddresses", "AddressRole", level3);
		RelationshipOrder order = new RelationshipOrder(
			"ContractBusinessPartner", "PartnerRole", level2);

		// When
		int depth = computeDepth(order);

		// Then
		assertEquals(depth, 3);
	}

	@Test(description = "Should return terminal field for a direct order")
	public void shouldReturnFieldNameForDirectFieldOrder() {
		// Given
		DirectFieldOrder order = new DirectFieldOrder(
			"name", DirectFieldOrder.Direction.ASC, false, DirectFieldOrder.NullHandling.NATIVE);

		// When
		String field = order.field();

		// Then
		assertEquals(field, "name");
	}

	@Test(description = "Should return terminal field for nested relationship")
	public void shouldReturnTerminalFieldForNestedOrder() {
		// Given
		DirectFieldOrder nested = new DirectFieldOrder(
			"city", DirectFieldOrder.Direction.ASC, false, DirectFieldOrder.NullHandling.NATIVE);
		RelationshipOrder order = new RelationshipOrder(
			"ContractBusinessPartner", "PartnerRole", nested);

		// When
		String terminalField = getTerminalField(order);

		// Then
		assertEquals(terminalField, "city");
	}

	@Test(description = "Should return terminal field for multi-level nested relationship")
	public void shouldReturnTerminalFieldForMultiLevelNestedOrder() {
		// Given
		DirectFieldOrder level3 = new DirectFieldOrder(
			"isoCode", DirectFieldOrder.Direction.ASC, false, DirectFieldOrder.NullHandling.NATIVE);
		RelationshipOrder level2 = new RelationshipOrder(
			"PartnerAddresses", "AddressRole", level3);
		RelationshipOrder order = new RelationshipOrder(
			"ContractBusinessPartner", "PartnerRole", level2);

		// When
		String terminalField = getTerminalField(order);

		// Then
		assertEquals(terminalField, "isoCode");
	}

	@SneakyThrows
	@Test(description = "Should serialize direct field order to JSON")
	public void shouldSerializeDirectFieldOrderToJson() {
		// Given
		DirectFieldOrder order = new DirectFieldOrder(
			"name", DirectFieldOrder.Direction.ASC, false, DirectFieldOrder.NullHandling.NATIVE);

		// When
		String json = objectMapper.writeValueAsString(order);

		// Then
		assertTrue(json.contains("\"field\":\"name\""));
		assertTrue(json.contains("\"direction\":\"ASC\""));
	}

	@SneakyThrows
	@Test(description = "Should deserialize direct field order from JSON")
	public void shouldDeserializeDirectFieldOrderFromJson() {
		// Given
		String json = "{\"field\":\"name\",\"direction\":\"ASC\",\"ignoreCase\":false,\"nullHandling\":\"NATIVE\"}";

		// When
		Order order = objectMapper.readValue(json, Order.class);

		// Then
		assertTrue(order instanceof DirectFieldOrder);
		DirectFieldOrder direct = (DirectFieldOrder) order;
		assertEquals(direct.field(), "name");
		assertEquals(direct.direction(), DirectFieldOrder.Direction.ASC);
	}

	@SneakyThrows
	@Test(description = "Should serialize relationship order with nested direct field to JSON")
	public void shouldSerializeRelationshipOrderWithDirectFieldToJson() {
		// Given
		DirectFieldOrder nested = new DirectFieldOrder(
			"city", DirectFieldOrder.Direction.ASC, false, DirectFieldOrder.NullHandling.NATIVE);
		RelationshipOrder order = new RelationshipOrder(
			"ContractBusinessPartner", "PartnerRole", nested);

		// When
		String json = objectMapper.writeValueAsString(order);

		// Then
		assertTrue(json.contains("\"relationshipModel\":\"ContractBusinessPartner\""));
		assertTrue(json.contains("\"targetRole\":\"PartnerRole\""));
		assertTrue(json.contains("\"sortBy\":{"));
		assertTrue(json.contains("\"field\":\"city\""));
		assertTrue(json.contains("\"direction\":\"ASC\""));
	}

	@SneakyThrows
	@Test(description = "Should deserialize relationship order with nested direct field from JSON")
	public void shouldDeserializeRelationshipOrderWithDirectFieldFromJson() {
		// Given
		String json = "{\"relationshipModel\":\"ContractBusinessPartner\",\"targetRole\":\"PartnerRole\"," +
			"\"sortBy\":{\"field\":\"city\",\"direction\":\"ASC\",\"ignoreCase\":false,\"nullHandling\":\"NATIVE\"}}";

		// When
		Order order = objectMapper.readValue(json, Order.class);

		// Then
		assertTrue(order instanceof RelationshipOrder);
		RelationshipOrder traversal = (RelationshipOrder) order;
		assertEquals(traversal.relationshipModel(), "ContractBusinessPartner");
		assertEquals(traversal.targetRole(), "PartnerRole");
		assertTrue(traversal.sortBy() instanceof DirectFieldOrder);
		DirectFieldOrder terminal = (DirectFieldOrder) traversal.sortBy();
		assertEquals(terminal.field(), "city");
		assertEquals(terminal.direction(), DirectFieldOrder.Direction.ASC);
	}

	@SneakyThrows
	@Test(description = "Should serialize multi-level nested relationships to JSON")
	public void shouldSerializeMultiLevelRelationshipOrderToJson() {
		// Given
		DirectFieldOrder level3 = new DirectFieldOrder(
			"isoCode", DirectFieldOrder.Direction.DESC, false, DirectFieldOrder.NullHandling.NULLS_LAST);
		RelationshipOrder level2 = new RelationshipOrder(
			"PartnerAddresses", "AddressRole", level3);
		RelationshipOrder order = new RelationshipOrder(
			"ContractBusinessPartner", "PartnerRole", level2);

		// When
		String json = objectMapper.writeValueAsString(order);

		// Then
		assertTrue(json.contains("\"relationshipModel\":\"ContractBusinessPartner\""));
		assertTrue(json.contains("\"targetRole\":\"PartnerRole\""));
		assertTrue(json.contains("\"relationshipModel\":\"PartnerAddresses\""));
		assertTrue(json.contains("\"targetRole\":\"AddressRole\""));
		assertTrue(json.contains("\"field\":\"isoCode\""));
		assertTrue(json.contains("\"direction\":\"DESC\""));
	}

	@SneakyThrows
	@Test(description = "Should deserialize multi-level nested relationships from JSON")
	public void shouldDeserializeMultiLevelRelationshipOrderFromJson() {
		// Given
		String json = "{\"relationshipModel\":\"ContractBusinessPartner\",\"targetRole\":\"PartnerRole\"," +
			"\"sortBy\":{\"relationshipModel\":\"PartnerAddresses\",\"targetRole\":\"AddressRole\"," +
			"\"sortBy\":{\"field\":\"isoCode\",\"direction\":\"DESC\",\"ignoreCase\":false,\"nullHandling\":\"NULLS_LAST\"}}}";

		// When
		Order root = objectMapper.readValue(json, Order.class);

		// Then
		assertTrue(root instanceof RelationshipOrder);
		RelationshipOrder level1 = (RelationshipOrder) root;
		assertEquals(level1.relationshipModel(), "ContractBusinessPartner");

		assertTrue(level1.sortBy() instanceof RelationshipOrder);
		RelationshipOrder level2 = (RelationshipOrder) level1.sortBy();
		assertEquals(level2.relationshipModel(), "PartnerAddresses");

		assertTrue(level2.sortBy() instanceof DirectFieldOrder);
		DirectFieldOrder terminal = (DirectFieldOrder) level2.sortBy();
		assertEquals(terminal.field(), "isoCode");
		assertEquals(terminal.direction(), DirectFieldOrder.Direction.DESC);
	}

	@Test(expectedExceptions = QueryValidationException.class,
		description = "Should throw QueryValidationException when both field and relationshipModel are present")
	public void shouldThrowWhenBothFieldAndRelationshipModelPresent() throws Exception {
		// Given
		String json = "{\"relationshipModel\":\"ContractBusinessPartner\",\"targetRole\":\"PartnerRole\"," +
			"\"field\":\"/BusinessPartnerRoot/Name\"," +
			"\"sortBy\":{\"field\":\"city\",\"direction\":\"ASC\",\"ignoreCase\":false,\"nullHandling\":\"NULLS_LAST\"}}";

		// When
		objectMapper.readValue(json, Order.class);
	}

	@DataProvider(name = "depthCalculationProvider")
	public Object[][] depthCalculationProvider() {
		return new Object[][] {
			// Direct field order
			{ new DirectFieldOrder("field1", DirectFieldOrder.Direction.ASC, false, DirectFieldOrder.NullHandling.NATIVE), 1 },
			// Single-level nested
			{ new RelationshipOrder("Rel1", "Role1",
				new DirectFieldOrder("field2", DirectFieldOrder.Direction.ASC, false, DirectFieldOrder.NullHandling.NATIVE)), 2 },
			// Two-level nested
			{ new RelationshipOrder("Rel1", "Role1",
				new RelationshipOrder("Rel2", "Role2",
					new DirectFieldOrder("field3", DirectFieldOrder.Direction.ASC, false, DirectFieldOrder.NullHandling.NATIVE))), 3 },
			// Three-level nested
			{ new RelationshipOrder("Rel1", "Role1",
				new RelationshipOrder("Rel2", "Role2",
					new RelationshipOrder("Rel3", "Role3",
						new DirectFieldOrder("field4", DirectFieldOrder.Direction.ASC, false, DirectFieldOrder.NullHandling.NATIVE)))), 4 }
		};
	}

	@Test(dataProvider = "depthCalculationProvider", description = "Should calculate correct depth for various nesting levels")
	public void shouldCalculateCorrectDepthForVariousNestingLevels(Order order, int expectedDepth) {
		// When
		int actualDepth = computeDepth(order);

		// Then
		assertEquals(actualDepth, expectedDepth);
	}

	private static int computeDepth(Order order) {
		if (order instanceof DirectFieldOrder) {
			return 1;
		} else if (order instanceof RelationshipOrder rel) {
			return 1 + computeDepth(rel.sortBy());
		}
		return 0;
	}

	private static String getTerminalField(Order order) {
		if (order instanceof DirectFieldOrder direct) {
			return direct.field();
		} else if (order instanceof RelationshipOrder rel) {
			return getTerminalField(rel.sortBy());
		}
		return null;
	}
}
