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
import { strictEqual } from "node:assert/strict";
import { readFileSync } from "node:fs";

import { Aggregation } from "../../query/Query.js";

suite("Aggregation Rest Tests", () => {
	test("Request builder", () => {
		const queryRoot = JSON.parse(
			readFileSync("./src/test/resources/query/aggregation_query.json", {
				encoding: "utf8"
			})
		);
		const request = Aggregation.Request.build({ queryRoot: queryRoot });

		strictEqual(request.method, "POST");
		strictEqual(request.relativeUrl, "/aggregation");
		strictEqual(request.customHeaders?.length, 2);
		strictEqual(request.customHeaders?.[0][0], "Accept");
		strictEqual(request.customHeaders?.[0][1], "application/json");
		strictEqual(request.customHeaders?.[1][0], "Content-Type");
		strictEqual(request.customHeaders?.[1][1], "application/json;charset=utf8");
		strictEqual(request.body, JSON.stringify(queryRoot));
	});

	test("Response", () => {
		const aggregationResponse = [
			["Health", 2],
			["Health Contract", 1]
		];

		strictEqual(
			Aggregation.Response.isInstance(aggregationResponse),
			true,
			"Aggregation response was not recognized!"
		);

		strictEqual(
			Aggregation.Response.isInstance([]),
			true,
			"Empty aggregation response was not recognized!"
		);

		strictEqual(
			Aggregation.Response.isInstance([["Health"]]),
			false,
			"Invalid aggregation response was incorrectly recognized!"
		);

		strictEqual(
			Aggregation.Response.isInstance([["Health", "Insurance", 2, 5]]),
			true,
			"Invalid aggregation response was incorrectly recognized!"
		);

		strictEqual(
			Aggregation.Response.isInstance([["basketball", "black", 4399.0, 1200.0, 1999.0]]),
			true,
			"Invalid aggregation response was incorrectly recognized!"
		);

		strictEqual(
			Aggregation.Response.isInstance([
				[null, 3, 1350000.0],
				["Household", 1, 50000.0]
			]),
			true,
			"Invalid aggregation response was incorrectly recognized!"
		);

		strictEqual(
			Aggregation.Response.isInstance([1, 2, 3]),
			false,
			"Array of numbers was incorrectly recognized as aggregation response!"
		);

		strictEqual(
			Aggregation.Response.isInstance([["Health", "Insurance"], ["Life"]]),
			false,
			"Aggregation response with invalid tuples was incorrectly recognized!"
		);

		strictEqual(
			Aggregation.Response.isInstance("not an array"),
			false,
			"String was incorrectly recognized as aggregation response!"
		);

		strictEqual(
			Aggregation.Response.isInstance({}),
			false,
			"Object was incorrectly recognized as aggregation response!"
		);
	});
});
