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

import java.util.List;
import java.util.Map;

public class NestedFacetResultTest extends AbstractFacetResponseTest<BucketedFacetResult> {
	@Override
	protected String getResourceName() {
		return "response/nested.json";
	}

	@Override
	protected BucketedFacetResult makeObject() {
		return new BucketedFacetResult(List.of(
				new BucketContent("Risk", 1, Map.of(
						"first term", new BucketedFacetResult(List.of(new BucketContent("Natural Person", 1, Map.of("avg", new PrimitiveFacetResult(12)))), 0, 10, 1))),
				new BucketContent("Legal", 2, Map.of(
						"first term", new BucketedFacetResult(List.of(
								new BucketContent("Natural Person", 1, Map.of("avg", new PrimitiveFacetResult(12))),
								new BucketContent("Legal Entity", 1, Map.of("avg", new PrimitiveFacetResult(12)))),
								0, 10, 2))),
				new BucketContent("IT", 6, Map.of(
						"first term", new BucketedFacetResult(List.of(
								new BucketContent("Natural Person", 5, Map.of("avg", new PrimitiveFacetResult(12))),
								new BucketContent("Legal Entity", 1, Map.of("avg", new PrimitiveFacetResult(12)))),
								0, 10, 2))),
				new BucketContent("Healthcare", 1, Map.of(
						"first term", new BucketedFacetResult(List.of(
								new BucketContent("Natural Person", 1, Map.of("avg", new PrimitiveFacetResult(12)))), 0, 10, 1))),
				new BucketContent("Commerce", 1, Map.of(
						"first term", new BucketedFacetResult(List.of(
								new BucketContent("Legal Entity", 1, Map.of("avg", new PrimitiveFacetResult(12)))), 0, 10, 1)))),
				0, 999, 5);
	}
}
