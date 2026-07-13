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
package com.mgmtp.a12.dataservices.internal.query.generator.sql;

import java.util.List;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.internal.query.generator.sql.constraint.AbstractSqlGeneratorTest;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder.Direction;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder.NullHandling;
import com.mgmtp.a12.dataservices.query.RelationshipOrder;
import com.mgmtp.a12.dataservices.query.generator.sql.internal.RootCteGenerator;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link RootCteGenerator} verifying the corrected SQL structure when
 * relationship-based sort orders are present.
 *
 * The generator emits a `ROW_NUMBER() OVER (ORDER BY ...)` column inside the CTE SELECT
 * and references it via `ORDER BY root._sort_rank` in the outer SELECT. This guarantees
 * ordering propagates from the CTE to the outer query regardless of the PostgreSQL
 * execution plan.
 */
public class RootCteGeneratorRelationshipSortTest extends AbstractSqlGeneratorTest {

	/**
	 * Tests that a `ROW_NUMBER() OVER (ORDER BY ...)` expression is inserted into the CTE SELECT
	 * when a relationship order is present.
	 */
	@Test(description = "Should add ROW_NUMBER() to CTE SELECT when relationship order present")
	public void shouldAddRowNumberToCteSelectorWhenRelationshipOrderPresent() {
		// Given
		RelationshipOrder order = new RelationshipOrder(
			"ContractBusinessPartner", "Partner",
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.ASC, false, NullHandling.NULLS_LAST)
		);

		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel("Contract")
			.sort(List.of(order))
			.build();

		RootCteGenerator generator = RootCteGenerator.builder()
			.query(query)
			.generatorContext(queryGeneratorContextFactory.createContext(newQueryContext()))
			.build();

		// When
		StringBuilder sb = new StringBuilder();
		generator.render(sb);
		String sql = sb.toString();

		// Then - the CTE SELECT must include a ROW_NUMBER() OVER (ORDER BY ...) expression
		assertTrue(sql.contains("ROW_NUMBER()"), "CTE SELECT should contain ROW_NUMBER() window function");
		assertTrue(sql.contains("OVER"), "ROW_NUMBER() must have an OVER clause");
		assertTrue(sql.contains("ORDER BY"), "OVER clause must contain ORDER BY");
	}

	/**
	 * Tests that `ORDER BY root._sort_rank` is appended to the outer SELECT
	 * when a relationship order is present.
	 */
	@Test(description = "Should add ORDER BY _sort_rank to outer SELECT when relationship order present")
	public void shouldAddOuterOrderByWhenRelationshipOrderPresent() {
		// Given
		RelationshipOrder order = new RelationshipOrder(
			"ContractBusinessPartner", "Partner",
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.ASC, false, NullHandling.NULLS_LAST)
		);

		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel("Contract")
			.sort(List.of(order))
			.build();

		RootCteGenerator generator = RootCteGenerator.builder()
			.query(query)
			.generatorContext(queryGeneratorContextFactory.createContext(newQueryContext()))
			.build();

		// When
		StringBuilder sb = new StringBuilder();
		generator.render(sb);
		String sql = sb.toString();

		// Then - the outer SELECT must order by the _sort_rank column computed in the CTE
		assertTrue(sql.contains("_sort_rank"), "Outer SELECT should reference the _sort_rank column");
	}

	/**
	 * Tests that neither `ROW_NUMBER()` nor `_sort_rank` is added when no relationship orders exist.
	 *
	 * When all sort orders are direct field orders (not relationship-based), the CTE should remain
	 * unchanged and the outer SELECT should not add any `_sort_rank` reference.
	 */
	@Test(description = "Should not add ROW_NUMBER() when no relationship orders present")
	public void shouldNotAddRowNumberWhenNoRelationshipOrders() {
		// Given - sort only by a direct field (not a relationship order)
		DirectFieldOrder directOrder = new DirectFieldOrder("/contractNumber", Direction.ASC);

		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel("Contract")
			.sort(List.of(directOrder))
			.build();

		RootCteGenerator generator = RootCteGenerator.builder()
			.query(query)
			.generatorContext(queryGeneratorContextFactory.createContext(newQueryContext()))
			.build();

		// When
		StringBuilder sb = new StringBuilder();
		generator.render(sb);
		String sql = sb.toString();

		// Then - no ROW_NUMBER() should appear for non-relationship orders
		assertFalse(sql.contains("ROW_NUMBER()"), "CTE SELECT should NOT contain ROW_NUMBER() when no relationship orders");
		assertFalse(sql.contains("_sort_rank"), "Outer SELECT should NOT reference _sort_rank when no relationship orders");
	}

	/**
	 * Tests that the `NULLS FIRST` directive is correctly embedded in the `ROW_NUMBER()` window
	 * function ORDER BY clause when the order specifies `NullHandling.NULLS_FIRST`.
	 *
	 * The window function must mirror the original sort expression so that the rank preserves
	 * null placement.
	 */
	@Test(description = "Should embed NULLS FIRST in the ROW_NUMBER() window function")
	public void shouldEmbedNullsFirstInRowNumber() {
		// Given - relationship order with NULLS_FIRST null handling
		RelationshipOrder order = new RelationshipOrder(
			"ContractBusinessPartner", "Partner",
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.ASC, false, NullHandling.NULLS_FIRST)
		);

		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel("Contract")
			.sort(List.of(order))
			.build();

		RootCteGenerator generator = RootCteGenerator.builder()
			.query(query)
			.generatorContext(queryGeneratorContextFactory.createContext(newQueryContext()))
			.build();

		// When
		StringBuilder sb = new StringBuilder();
		generator.render(sb);
		String sql = sb.toString();

		// Then - NULLS FIRST must appear inside the ROW_NUMBER() OVER (ORDER BY ...) expression
		assertTrue(sql.contains("ROW_NUMBER()"), "CTE SELECT should contain ROW_NUMBER()");
		assertTrue(sql.contains("NULLS FIRST"), "ROW_NUMBER() window ORDER BY should specify NULLS FIRST");
	}

	/**
	 * Tests that the `NULLS LAST` directive is correctly embedded in the `ROW_NUMBER()` window
	 * function ORDER BY clause when the order specifies `NullHandling.NULLS_LAST`.
	 *
	 * The window function must mirror the original sort expression so that the rank preserves
	 * null placement.
	 */
	@Test(description = "Should embed NULLS LAST in the ROW_NUMBER() window function")
	public void shouldEmbedNullsLastInRowNumber() {
		// Given - relationship order with NULLS_LAST null handling
		RelationshipOrder order = new RelationshipOrder(
			"ContractBusinessPartner", "Partner",
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.ASC, false, NullHandling.NULLS_LAST)
		);

		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel("Contract")
			.sort(List.of(order))
			.build();

		RootCteGenerator generator = RootCteGenerator.builder()
			.query(query)
			.generatorContext(queryGeneratorContextFactory.createContext(newQueryContext()))
			.build();

		// When
		StringBuilder sb = new StringBuilder();
		generator.render(sb);
		String sql = sb.toString();

		// Then - NULLS LAST must appear inside the ROW_NUMBER() OVER (ORDER BY ...) expression
		assertTrue(sql.contains("ROW_NUMBER()"), "CTE SELECT should contain ROW_NUMBER()");
		assertTrue(sql.contains("NULLS LAST"), "ROW_NUMBER() window ORDER BY should specify NULLS LAST");
	}

	/**
	 * When a query combines a relationship-based sort with `links`, the outer `ORDER BY roots._sort_rank`
	 * is followed by `UNION ALL` link selects. PostgreSQL requires the ordered SELECT to be parenthesized
	 * when followed by `UNION ALL`; otherwise the statement is rejected as a syntax error.
	 */
	@Test(description = "Should wrap root SELECT in parentheses when relationship sort and links are both present")
	public void shouldWrapRootSelectInParenthesesWhenRelationshipSortAndLinksPresent() {
		// Given - relationship sort combined with a link
		RelationshipOrder order = new RelationshipOrder(
			"ContractBusinessPartner", "Partner",
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.ASC, false, NullHandling.NULLS_LAST)
		);

		QueryLink link = QueryLink.builder()
			.relationshipModel("ContractBusinessPartner")
			.targetRole("Partner")
			.fields(List.of("/BusinessPartnerRoot/Name"))
			.build();

		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel("Contract")
			.sort(List.of(order))
			.links(List.of(link))
			.build();

		RootCteGenerator generator = RootCteGenerator.builder()
			.query(query)
			.generatorContext(queryGeneratorContextFactory.createContext(newQueryContext()))
			.build();

		// When
		StringBuilder sb = new StringBuilder();
		generator.render(sb);
		String sql = sb.toString();

		// Then - links produce a UNION ALL
		assertTrue(sql.contains("UNION ALL"), "Expected links to produce UNION ALL: " + sql);

		// Then - the outer ORDER BY clause must NOT directly precede UNION ALL without a closing parenthesis
		assertFalse(
			Pattern.compile("_sort_rank\\s+UNION\\s+ALL", Pattern.CASE_INSENSITIVE).matcher(sql).find(),
			"ORDER BY roots._sort_rank must be enclosed in parentheses before UNION ALL — produces invalid PostgreSQL syntax: " + sql
		);

		// Then - the outer ORDER BY must be followed by a closing parenthesis before UNION ALL
		assertTrue(
			Pattern.compile("_sort_rank\\s*\\)\\s*UNION\\s+ALL", Pattern.CASE_INSENSITIVE).matcher(sql).find(),
			"Expected pattern '_sort_rank ) UNION ALL' indicating the root SELECT is parenthesized: " + sql
		);
	}

	/**
	 * Sanity check for the parenthesization contract: a query without any links must NOT produce
	 * spurious parentheses around the root SELECT, since no UNION ALL follows.
	 */
	@Test(description = "Should not wrap root SELECT in parentheses when no links present")
	public void shouldNotWrapRootSelectWhenNoLinks() {
		// Given - relationship sort only, no links
		RelationshipOrder order = new RelationshipOrder(
			"ContractBusinessPartner", "Partner",
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.ASC, false, NullHandling.NULLS_LAST)
		);

		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel("Contract")
			.sort(List.of(order))
			.build();

		RootCteGenerator generator = RootCteGenerator.builder()
			.query(query)
			.generatorContext(queryGeneratorContextFactory.createContext(newQueryContext()))
			.build();

		// When
		StringBuilder sb = new StringBuilder();
		generator.render(sb);
		String sql = sb.toString();

		// Then - no UNION ALL since no links
		assertFalse(sql.contains("UNION ALL"), "No links should produce no UNION ALL: " + sql);
		assertTrue(sql.contains("_sort_rank"), "Expected outer SELECT to reference _sort_rank");
	}
}
