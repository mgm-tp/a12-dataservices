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

import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSerializationTest;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder.Direction;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder.NullHandling;

import lombok.SneakyThrows;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link DirectFieldOrder} and {@link RelationshipOrder}.
 */
public class OrderTest extends AbstractSerializationTest {

	// Tests for DirectFieldOrder convenience constructors

	@Test(description = "Should create direct field order with default settings using single-argument constructor")
	public void shouldCreateDirectFieldOrderWithDefaultSettingsUsingSingleArgumentConstructor() {
		// Given / When
		DirectFieldOrder order = new DirectFieldOrder("fieldName");

		// Then
		assertEquals(order.direction(), Direction.ASC);
		assertEquals(order.field(), "fieldName");
		assertFalse(order.ignoreCase());
		assertEquals(order.nullHandling(), NullHandling.NATIVE);
	}

	@Test(description = "Should create direct field order with direction using two-argument constructor")
	public void shouldCreateDirectFieldOrderWithDirectionUsingTwoArgumentConstructor() {
		// Given / When
		DirectFieldOrder order = new DirectFieldOrder("fieldName", Direction.DESC);

		// Then
		assertEquals(order.direction(), Direction.DESC);
		assertEquals(order.field(), "fieldName");
		assertFalse(order.ignoreCase());
		assertEquals(order.nullHandling(), NullHandling.NATIVE);
	}

	@Test(description = "Should create direct field order with direction and ignoreCase")
	public void shouldCreateDirectFieldOrderWithDirectionAndIgnoreCase() {
		// Given / When
		DirectFieldOrder order = new DirectFieldOrder("fieldName", Direction.ASC, true);

		// Then
		assertEquals(order.direction(), Direction.ASC);
		assertEquals(order.field(), "fieldName");
		assertTrue(order.ignoreCase());
		assertEquals(order.nullHandling(), NullHandling.NATIVE);
	}

	@Test(description = "Should create direct field order with direction and null handling")
	public void shouldCreateDirectFieldOrderWithDirectionAndNullHandling() {
		// Given / When
		DirectFieldOrder order = new DirectFieldOrder("fieldName", Direction.DESC, NullHandling.NULLS_FIRST);

		// Then
		assertEquals(order.direction(), Direction.DESC);
		assertEquals(order.field(), "fieldName");
		assertFalse(order.ignoreCase());
		assertEquals(order.nullHandling(), NullHandling.NULLS_FIRST);
	}

	// Tests for top-level RelationshipOrder constructors

	@Test(description = "Should create top-level relationship order with direct field target")
	public void shouldCreateTopLevelRelationshipOrderWithDirectFieldTarget() {
		// Given / When
		DirectFieldOrder terminal = new DirectFieldOrder("name", Direction.ASC, true, NullHandling.NULLS_LAST);
		RelationshipOrder order = new RelationshipOrder(
			"ContractBusinessPartner", "PartnerRole",
			terminal);

		// Then
		assertEquals(order.relationshipModel(), "ContractBusinessPartner");
		assertEquals(order.targetRole(), "PartnerRole");
		assertEquals(order.sortBy(), terminal);
		assertEquals(terminal.direction(), Direction.ASC);
		assertTrue(terminal.ignoreCase());
		assertEquals(terminal.nullHandling(), NullHandling.NULLS_LAST);
	}

	@Test(description = "Should create top-level relationship order with nested relationship target")
	public void shouldCreateTopLevelRelationshipOrderWithNestedRelationshipTarget() {
		// Given / When
		DirectFieldOrder innerTarget = new DirectFieldOrder("city", Direction.DESC, NullHandling.NULLS_FIRST);
		RelationshipOrder nestedRelationship = new RelationshipOrder(
			"PartnerAddresses", "AddressRole",
			innerTarget);
		RelationshipOrder order = new RelationshipOrder(
			"ContractBusinessPartner", "PartnerRole",
			nestedRelationship);

		// Then
		assertEquals(order.relationshipModel(), "ContractBusinessPartner");
		assertEquals(order.targetRole(), "PartnerRole");
		assertEquals(order.sortBy(), nestedRelationship);
		assertEquals(nestedRelationship.relationshipModel(), "PartnerAddresses");
		assertEquals(nestedRelationship.targetRole(), "AddressRole");
		assertEquals(nestedRelationship.sortBy(), innerTarget);
	}

	// JSON serialization tests for DirectFieldOrder

	@SneakyThrows
	@Test(description = "Should serialize direct field order to JSON")
	public void shouldSerializeDirectFieldOrderToJson() {
		// Given
		DirectFieldOrder order = new DirectFieldOrder("fieldName", Direction.DESC, NullHandling.NULLS_LAST);

		// When
		String json = objectMapper.writeValueAsString(order);

		// Then
		assertTrue(json.contains("\"direction\":\"DESC\""));
		assertTrue(json.contains("\"field\":\"fieldName\""));
		assertTrue(json.contains("\"ignoreCase\":false"));
		assertTrue(json.contains("\"nullHandling\":\"NULLS_LAST\""));
	}

	@SneakyThrows
	@Test(description = "Should deserialize direct field order from JSON via Order interface")
	public void shouldDeserializeDirectFieldOrderFromJsonViaRootOrderInterface() {
		// Given
		String json = "{\"direction\":\"ASC\",\"field\":\"fieldName\",\"ignoreCase\":true,\"nullHandling\":\"NULLS_FIRST\"}";

		// When
		Order order = objectMapper.readValue(json, Order.class);

		// Then
		assertTrue(order instanceof DirectFieldOrder);
		DirectFieldOrder directOrder = (DirectFieldOrder) order;
		assertEquals(directOrder.direction(), Direction.ASC);
		assertEquals(directOrder.field(), "fieldName");
		assertTrue(directOrder.ignoreCase());
		assertEquals(directOrder.nullHandling(), NullHandling.NULLS_FIRST);
	}

	@SneakyThrows
	@Test(description = "Should deserialize direct field order with missing optional fields")
	public void shouldDeserializeDirectFieldOrderWithMissingOptionalFields() {
		// Given
		String json = "{\"direction\":\"ASC\",\"field\":\"fieldName\"}";

		// When
		Order order = objectMapper.readValue(json, Order.class);

		// Then
		assertTrue(order instanceof DirectFieldOrder);
		DirectFieldOrder directOrder = (DirectFieldOrder) order;
		assertEquals(directOrder.direction(), Direction.ASC);
		assertEquals(directOrder.field(), "fieldName");
		assertNull(directOrder.ignoreCase());
		assertNull(directOrder.nullHandling());
	}

	// JSON serialization tests for RelationshipOrder as top-level Order

	@SneakyThrows
	@Test(description = "Should serialize top-level relationship order to JSON with direct field target")
	public void shouldSerializeTopLevelRelationshipOrderToJson() {
		// Given
		DirectFieldOrder target = new DirectFieldOrder("name", Direction.ASC, false, NullHandling.NULLS_LAST);
		RelationshipOrder order = new RelationshipOrder(
			"ContractBusinessPartner", "PartnerRole",
			target);

		// When
		String json = objectMapper.writeValueAsString(order);

		// Then
		assertTrue(json.contains("\"relationshipModel\":\"ContractBusinessPartner\""));
		assertTrue(json.contains("\"targetRole\":\"PartnerRole\""));
		assertTrue(json.contains("\"sortBy\":"));
		assertTrue(json.contains("\"direction\":\"ASC\""));
		assertTrue(json.contains("\"field\":\"name\""));
		assertTrue(json.contains("\"nullHandling\":\"NULLS_LAST\""));
	}

	@SneakyThrows
	@Test(description = "Should deserialize top-level relationship order from JSON via Order interface")
	public void shouldDeserializeTopLevelRelationshipOrderFromJsonViaRootOrderInterface() {
		// Given
		String json = "{\"relationshipModel\":\"ContractBusinessPartner\",\"targetRole\":\"PartnerRole\"," +
			"\"sortBy\":{\"direction\":\"DESC\",\"field\":\"name\",\"ignoreCase\":true,\"nullHandling\":\"NULLS_FIRST\"}}";

		// When
		Order order = objectMapper.readValue(json, Order.class);

		// Then
		assertTrue(order instanceof RelationshipOrder);
		RelationshipOrder relOrder = (RelationshipOrder) order;
		assertEquals(relOrder.relationshipModel(), "ContractBusinessPartner");
		assertEquals(relOrder.targetRole(), "PartnerRole");
		Order sortBy = relOrder.sortBy();
		assertTrue(sortBy instanceof DirectFieldOrder);
		DirectFieldOrder fieldOrder = (DirectFieldOrder) sortBy;
		assertEquals(fieldOrder.direction(), Direction.DESC);
		assertEquals(fieldOrder.field(), "name");
		assertTrue(fieldOrder.ignoreCase());
		assertEquals(fieldOrder.nullHandling(), NullHandling.NULLS_FIRST);
	}

	@SneakyThrows
	@Test(description = "Should serialize nested relationship order to JSON with sortBy")
	public void shouldSerializeNestedRelationshipOrderToJson() {
		// Given
		DirectFieldOrder innerTarget = new DirectFieldOrder("city", Direction.ASC, false, NullHandling.NULLS_LAST);
		RelationshipOrder nestedOrder = new RelationshipOrder(
			"PartnerAddresses", "AddressRole",
			innerTarget);
		RelationshipOrder order = new RelationshipOrder(
			"ContractBusinessPartner", "PartnerRole",
			nestedOrder);

		// When
		String json = objectMapper.writeValueAsString(order);

		// Then
		assertTrue(json.contains("\"relationshipModel\":\"ContractBusinessPartner\""));
		assertTrue(json.contains("\"targetRole\":\"PartnerRole\""));
		assertTrue(json.contains("\"sortBy\":{"));
		assertTrue(json.contains("\"relationshipModel\":\"PartnerAddresses\""));
		assertTrue(json.contains("\"targetRole\":\"AddressRole\""));
		assertTrue(json.contains("\"field\":\"city\""));
	}

	@SneakyThrows
	@Test(description = "Should deserialize nested relationship order from JSON via Order interface")
	public void shouldDeserializeNestedRelationshipOrderFromJsonViaRootOrderInterface() {
		// Given
		String json = "{\"relationshipModel\":\"ContractBusinessPartner\",\"targetRole\":\"PartnerRole\"," +
			"\"sortBy\":{\"relationshipModel\":\"PartnerAddresses\",\"targetRole\":\"AddressRole\"," +
			"\"sortBy\":{\"direction\":\"ASC\",\"field\":\"city\",\"ignoreCase\":false,\"nullHandling\":\"NULLS_LAST\"}}}";

		// When
		Order order = objectMapper.readValue(json, Order.class);

		// Then
		assertTrue(order instanceof RelationshipOrder);
		RelationshipOrder relOrder = (RelationshipOrder) order;
		assertEquals(relOrder.relationshipModel(), "ContractBusinessPartner");
		assertEquals(relOrder.targetRole(), "PartnerRole");
		Order sortBy = relOrder.sortBy();
		assertTrue(sortBy instanceof RelationshipOrder);
		RelationshipOrder nestedOrder = (RelationshipOrder) sortBy;
		assertEquals(nestedOrder.relationshipModel(), "PartnerAddresses");
		assertEquals(nestedOrder.targetRole(), "AddressRole");
		Order innerSortBy = nestedOrder.sortBy();
		assertTrue(innerSortBy instanceof DirectFieldOrder);
		DirectFieldOrder fieldOrder = (DirectFieldOrder) innerSortBy;
		assertEquals(fieldOrder.field(), "city");
		assertEquals(fieldOrder.direction(), Direction.ASC);
		assertFalse(fieldOrder.ignoreCase());
		assertEquals(fieldOrder.nullHandling(), NullHandling.NULLS_LAST);
	}

	// Tests for canonical DirectFieldOrder constructor

	@Test(description = "Should create direct field order with 4-parameter canonical constructor")
	public void shouldCreateDirectFieldOrderWith4ParameterCanonicalConstructor() {
		// Given / When
		DirectFieldOrder order = new DirectFieldOrder("fieldName", Direction.DESC, true, NullHandling.NULLS_LAST);

		// Then
		assertEquals(order.direction(), Direction.DESC);
		assertEquals(order.field(), "fieldName");
		assertTrue(order.ignoreCase());
		assertEquals(order.nullHandling(), NullHandling.NULLS_LAST);
	}

	// Edge cases

	@Test(description = "Should handle direct field order with null ignoreCase")
	public void shouldHandleDirectFieldOrderWithNullIgnoreCase() {
		// Given / When
		DirectFieldOrder order = new DirectFieldOrder("fieldName", Direction.ASC, null, NullHandling.NATIVE);

		// Then
		assertEquals(order.direction(), Direction.ASC);
		assertEquals(order.field(), "fieldName");
		assertNull(order.ignoreCase());
		assertEquals(order.nullHandling(), NullHandling.NATIVE);
	}
}
