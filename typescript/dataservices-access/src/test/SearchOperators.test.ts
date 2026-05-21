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
import { deepStrictEqual, strictEqual } from "node:assert/strict";
import { readFileSync } from "node:fs";

import type {
	SearchOperator,
	SearchOperatorAnd,
	SearchOperatorMatch,
	SearchOperatorNot,
	SearchOperatorNull,
	SearchOperatorOr,
	SearchOperatorRange
} from "../search/filter/index.js";

suite("SearchOperators", () => {
	const expectedSerialized = readFileSync("./src/test/resources/search/filter/filters.json", {
		encoding: "utf8"
	});
	const firstMatch: SearchOperatorMatch = {
		type: "match",
		field: "field2",
		value: "value 2"
	};
	const secondMatch: SearchOperatorMatch = {
		type: "match",
		field: "field3",
		value: "value 3"
	};
	const thirdMatch: SearchOperatorMatch = {
		type: "match",
		field: "field4",
		value: "value 4"
	};
	const firstNot: SearchOperatorNot = { type: "not", filter: secondMatch };
	const firstRange: SearchOperatorRange = {
		type: "range",
		field: "field5",
		from: "5",
		to: "20"
	};
	const firstNull: SearchOperatorNull = { type: "null", field: "field6" };
	const secondNot: SearchOperatorNot = { type: "not", filter: firstNull };
	const firstAnd: SearchOperatorAnd = {
		type: "and",
		filters: [firstNot, thirdMatch, firstRange, secondNot]
	};
	const expectedStructure: SearchOperatorOr = {
		type: "or",
		filters: [firstMatch, firstAnd]
	};

	test("Serialize", () => {
		const actualSerialized = JSON.stringify(expectedStructure, undefined, "\t").trim();
		strictEqual(actualSerialized, expectedSerialized.trim());
	});

	test("Deserialize", () => {
		const actualDeserialized: SearchOperator = JSON.parse(expectedSerialized);
		deepStrictEqual(actualDeserialized, expectedStructure);
	});
});
