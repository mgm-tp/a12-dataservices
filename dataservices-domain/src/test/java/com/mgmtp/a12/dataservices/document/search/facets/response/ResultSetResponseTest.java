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
package com.mgmtp.a12.dataservices.document.search.facets.response;

import com.mgmtp.a12.dataservices.rpc.query.ResultSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResultSetResponseTest extends AbstractFacetResponseTest<ResultSet<Object>> {

	@Override protected String getResourceName() {
		return "response/result_set.json";
	}

	@Override protected ResultSet<Object> makeObject() {
		ResultSet<Object> objectResultSet = new ResultSet<>();
		objectResultSet.setEntries(new ArrayList<>());
		objectResultSet.setFullSize(1000L);
		objectResultSet.setFacets(Map.of(
			"region", new BucketedFacetResult(List.of(
				makeAuthorBucket("england"),
				makeAuthorBucket("germany"),
				makeAuthorBucket("norway")), 0, 10, 3),
			"prices", new BucketedFacetResult(List.of(
				makePriceRange(0, 99),
				makePriceRange(100, 199),
				makePriceRange(200, 299),
				makePriceRange(300, 399),
				makePriceRange(400, 499),
				makePriceRange(500, 599)), 0, 10, 6)));
		return objectResultSet;
	}

	private BucketContent makePriceRange(double start, double end) {
		return new BucketContent(String.format("%.1f -> %.1f", start, end), 100, Map.of(
			"total", new PrimitiveFacetResult(1000.46D),
			"avg", new PrimitiveFacetResult(14.6D),
			"count", new PrimitiveFacetResult(100),
			"min", new PrimitiveFacetResult(start),
			"max", new PrimitiveFacetResult(end)));
	}

	private BucketContent makeAuthorBucket(String region) {
		return new BucketContent(region, 100, Map.of(
			"count", new PrimitiveFacetResult(300),
			"author", new BucketedFacetResult(Stream.of("Higgins", "Hemingway")
				.map(val -> new BucketContent(val, 100, new HashMap<>()))
				.peek(c -> c.getFacets().put("count", new PrimitiveFacetResult(100)))
				.collect(Collectors.toList()), 0, 10, 2)));
	}
}
