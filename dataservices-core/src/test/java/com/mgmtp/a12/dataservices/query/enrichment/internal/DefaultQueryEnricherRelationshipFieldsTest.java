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

import java.util.List;

import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractQueryContextAwareTest;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder.Direction;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder.NullHandling;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.RelationshipOrder;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for `DefaultQueryEnricher` verifying that relationship-based sort orders cause
 * field type enrichment to be performed on the relationship target document models.
 *
 * `DefaultQueryEnricher.enrichQuery()` invokes `enrichRelationshipSortFields()` which walks
 * relationship sort orders and calls `enrichFieldTypes()` for each target model, enabling
 * correct collation for string fields and typed sorting for dates, numbers, and enumerations.
 */
public class DefaultQueryEnricherRelationshipFieldsTest extends AbstractQueryContextAwareTest {

	/**
	 * Tests that `enrichRelationshipSortFields()` enriches field types on the
	 * relationship target model when the query contains a relationship-based sort order.
	 *
	 * Given a `Contract` query with a relationship-based sort via `ContractBusinessPartner`
	 * targeting the `Partner` role (document model: `BusinessPartner`), after calling
	 * `enrichQuery()`, the enrichments must contain the field type for a field on the
	 * `BusinessPartner` model (e.g., `/BusinessPartnerRoot/Name` is `STRING`).
	 */
	@Test(description = "Should enrich target model field types when relationship sort is present")
	public void shouldEnrichRelationshipTargetModelFieldTypesWhenRelationshipSortPresent() {
		// Given: a query root that sorts by a relationship field (Contract → Partner.Name)
		DirectFieldOrder terminal = new DirectFieldOrder(
			"/BusinessPartnerRoot/Name",
			Direction.ASC,
			false,
			NullHandling.NULLS_LAST
		);
		RelationshipOrder order = new RelationshipOrder(
			RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL,
			RoleConstants.PARTNER_ROLE,
			terminal
		);

		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.sort(List.of(order))
			.build();

		QueryContext context = newQueryContext();

		// Precondition: BusinessPartner field type not yet enriched
		assertNull(
			context.getEnrichments().getFieldDescriptor("/BusinessPartnerRoot/Name").getFieldType(),
			"Precondition: field type for BusinessPartner.Name should be null before enrichment"
		);

		// When
		queryEnricher.enrichQuery(queryRoot, context);

		// Then: field types for the BusinessPartner target model must have been enriched
		// This verifies that enrichRelationshipSortFields() called enrichFieldTypes() for "BusinessPartner"
		assertEquals(
			context.getEnrichments().getFieldDescriptor("/BusinessPartnerRoot/Name").getFieldType(),
			QueryGeneratorConstants.FieldTypes.STRING_FIELD_TYPE,
			"Field type for /BusinessPartnerRoot/Name must be STRING after relationship sort enrichment"
		);
	}

	/**
	 * Tests that `enrichRelationshipSortFields()` enriches field types for nested
	 * (chained) relationship sort orders at all levels of nesting.
	 *
	 * Given a `Contract` query with a nested relationship sort:
	 * `ContractBusinessPartner → PartnerAddresses.Address./AddressRoot/City`,
	 * after calling `enrichQuery()`, the enrichments must contain field types for fields
	 * on both `BusinessPartner` (level 1 target) and `Address` (level 2 target).
	 */
	@Test(description = "Should enrich target model field types for nested relationship orders")
	public void shouldEnrichNestedRelationshipTargetModelFieldTypes() {
		// Given: a nested relationship sort order (Contract → BusinessPartner → Address.City)
		// Level 2 (inner terminal): PartnerAddresses targeting Address role, field = /AddressRoot/City
		DirectFieldOrder innerTerminal = new DirectFieldOrder(
			"/AddressRoot/City",
			Direction.ASC,
			false,
			NullHandling.NULLS_LAST
		);

		// Level 1 (middle traversal): ContractBusinessPartner targeting Partner role, nested sortBy = innerTraversal
		RelationshipOrder innerTraversal = new RelationshipOrder(
			RelationshipModelConstants.PARTNER_ADDRESSES_MODEL,
			RoleConstants.ADDRESS_ROLE,
			innerTerminal
		);

		// Top-level root order: ContractBusinessPartner targeting Partner role, sortBy is the inner traversal
		RelationshipOrder order = new RelationshipOrder(
			RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL,
			RoleConstants.PARTNER_ROLE,
			innerTraversal
		);

		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.sort(List.of(order))
			.build();

		QueryContext context = newQueryContext();

		// Preconditions: neither BusinessPartner nor Address field types enriched yet
		assertNull(
			context.getEnrichments().getFieldDescriptor("/BusinessPartnerRoot/Name").getFieldType(),
			"Precondition: field type for BusinessPartner.Name should be null before enrichment"
		);
		assertNull(
			context.getEnrichments().getFieldDescriptor("/AddressRoot/City").getFieldType(),
			"Precondition: field type for Address.City should be null before enrichment"
		);

		// When
		queryEnricher.enrichQuery(queryRoot, context);

		// Then: field types for BOTH levels of the relationship chain must be enriched
		// Level 1 target (BusinessPartner) — verified via a known BusinessPartner field
		assertEquals(
			context.getEnrichments().getFieldDescriptor("/BusinessPartnerRoot/Name").getFieldType(),
			QueryGeneratorConstants.FieldTypes.STRING_FIELD_TYPE,
			"Field type for /BusinessPartnerRoot/Name must be STRING after nested relationship sort enrichment"
		);

		// Level 2 target (Address) — verified via a known Address field
		assertEquals(
			context.getEnrichments().getFieldDescriptor("/AddressRoot/City").getFieldType(),
			QueryGeneratorConstants.FieldTypes.STRING_FIELD_TYPE,
			"Field type for /AddressRoot/City must be STRING after nested relationship sort enrichment"
		);
	}

	/**
	 * Tests that no additional enrichment is performed on relationship target models when
	 * the query has no relationship-based sort orders.
	 *
	 * Given a `Contract` query with only a direct field sort (not a relationship sort),
	 * after calling `enrichQuery()`, the enrichments must NOT contain field types for
	 * fields on the `BusinessPartner` model, since no relationship target model enrichment
	 * should have been triggered.
	 */
	@Test(description = "Should not enrich relationship target models when no relationship sort orders present")
	public void shouldNotEnrichRelationshipTargetModelsWhenNoRelationshipSort() {
		// Given: a query root that sorts only by a direct field (not a relationship order)
		DirectFieldOrder directOrder = new DirectFieldOrder("/ContractRoot/ContractName", Direction.ASC);

		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.sort(List.of(directOrder))
			.build();

		QueryContext context = newQueryContext();

		// When
		queryEnricher.enrichQuery(queryRoot, context);

		// Then: no field types for BusinessPartner should be enriched
		// (only Contract model fields should be enriched, not BusinessPartner)
		assertNull(
			context.getEnrichments().getFieldDescriptor("/BusinessPartnerRoot/Name").getFieldType(),
			"Field type for /BusinessPartnerRoot/Name must remain null when no relationship sort orders present"
		);
	}
}
