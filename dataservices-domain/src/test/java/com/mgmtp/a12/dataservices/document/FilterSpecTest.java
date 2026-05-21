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
package com.mgmtp.a12.dataservices.document;

import java.util.List;

import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.rpc.query.FilterSpec;

import static org.testng.Assert.assertEquals;

public class FilterSpecTest {

	public static final String FILTER1_TEXT = "BusinessPartnerRoot.Name:Alan Parker";
	public static final String FILTER2_TEXT = "BusinessPartnerRoot.Name:Lolina Bolina";

	@Test public void testFilterByBuilder() {
		FilterSpec result = FilterSpec.builder()
			.filter(FILTER1_TEXT)
			.build();
		assertFilterSpec(result, List.of(FILTER1_TEXT));
	}

	@Test public void testByBuilder() {
		FilterSpec result = FilterSpec.builder()
			.build();
		assertFilterSpec(result, List.of());
	}

	@Test public void testFiltersByBuilder() {
		FilterSpec result = FilterSpec.builder()
			.filters(List.of(FILTER1_TEXT))
			.build();
		assertFilterSpec(result, List.of(FILTER1_TEXT));
	}

	@Test public void testFiltersByBuilderAnsSetter() {
		FilterSpec result = FilterSpec.builder()
			.filters(List.of(FILTER1_TEXT))
			.build();
		result.setFilters(List.of(FILTER2_TEXT));
		assertFilterSpec(result, List.of(FILTER2_TEXT));
	}

	@Test public void testFilterByBuilderAnsSetter() {
		FilterSpec result = FilterSpec.builder()
			.filter(FILTER1_TEXT)
			.build();
		result.addFilter(FILTER2_TEXT);
		assertFilterSpec(result, List.of(FILTER1_TEXT, FILTER2_TEXT));
	}

	private static void assertFilterSpec(FilterSpec result, List<String> expectedFilters) {
		assertEquals(result.getFilters(), expectedFilters);
		assertEquals(result, FilterSpec.builder().filters(expectedFilters).build());
		assertEquals(result.toString(), "FilterSpec(fulltext=null, filters=[" + String.join(", ", expectedFilters) + "], lang=null)");
	}
}
