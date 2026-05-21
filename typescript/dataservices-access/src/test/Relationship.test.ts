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

import { isRelationshipModel } from "../Relationship/index.js";

import { loadResource } from "./utils/ObjectUtils.js";

suite("Relationship", () => {
	const relationshipModel = loadResource(
		"./src/test/resources/models/ContractBusinessPartner.json"
	);

	test("Check Relationship Model", () => {
		strictEqual(isRelationshipModel(relationshipModel), true);
	});

	test("Check Relationship Model - Missing Header", () => {
		const { header, ...relationship } = relationshipModel;
		strictEqual(isRelationshipModel(relationship), false);
	});

	test("Check Relationship Model - Missing Content", () => {
		const { content, ...relationship } = relationshipModel;
		strictEqual(isRelationshipModel(relationship), false);
	});

	test("Check Relationship Model - Other type of model", () => {
		const relationship = {
			...relationshipModel,
			header: { ...relationshipModel.header, modelType: "document" }
		};
		strictEqual(isRelationshipModel(relationship), false);
	});

	test("Check Relationship Model - Non-object value", () => {
		strictEqual(isRelationshipModel(null), false);
		strictEqual(isRelationshipModel(undefined), false);
		strictEqual(isRelationshipModel(1), false);
		strictEqual(isRelationshipModel(""), false);
		strictEqual(isRelationshipModel(/\w/), false);
	});
});
