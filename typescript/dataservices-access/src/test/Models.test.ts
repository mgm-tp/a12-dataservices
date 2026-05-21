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
import { deepEqual, notStrictEqual, strictEqual } from "node:assert/strict";
import { readFileSync } from "node:fs";

import { isModelInstance } from "@com.mgmtp.a12.base/base-model-api/lib/main/model/index.js";

import type { DeleteModelsResponse } from "../model/index.js";
import {
	ModelsRequest,
	ModelsResult,
	DeleteModelsRequest,
	UpdateModelsRequest,
	CreateModelsRequest
} from "../model/index.js";
import { ErrorPayload } from "../common/ErrorPayload.js";

suite("Models Tests", () => {
	const getModelResponseString = readFileSync(
		"./src/test/resources/models/ContractBusinessPartner.json",
		{
			encoding: "utf8"
		}
	);

	const parsedModel = JSON.parse(getModelResponseString);

	suite("Model Responses", () => {
		test("Check Model Instance - OK", () => {
			const getModelResponse = JSON.parse(getModelResponseString);
			strictEqual(ModelsResult.isInstance(getModelResponse), true);
		});

		test("Check Model Parsing", () => {
			const modelTest = JSON.parse(getModelResponseString);
			strictEqual(isModelInstance(modelTest) || ErrorPayload.isInstance(modelTest), true);
		});

		test("Delete Models Response - OK", () => {
			const deleteModelResponse: DeleteModelsResponse = JSON.parse(JSON.stringify(null));
			strictEqual(deleteModelResponse, null);
		});

		test("Delete Models Response - NOK", () => {
			const deleteModelResponse: DeleteModelsResponse = JSON.parse(
				JSON.stringify({
					errorType: "PRECONDITIONS_FAILED",
					message: "Something is wrong"
				})
			);
			notStrictEqual(deleteModelResponse, null);

			const expected = {
				errorType: "PRECONDITIONS_FAILED",
				message: "Something is wrong"
			};
			strictEqual(JSON.stringify(deleteModelResponse), JSON.stringify(expected));
		});
	});

	suite("Model Requests", () => {
		test("Get Model Request", () => {
			const request = ModelsRequest.build({ id: "ContractBusinessPartner" });

			strictEqual(request.method, "GET");
			strictEqual(request.relativeUrl, "/v2/models/ContractBusinessPartner");
		});

		test("Delete Model Request", () => {
			const request = DeleteModelsRequest.build({ id: "ContractBusinessPartner" });

			strictEqual(request.method, "DELETE");
			strictEqual(request.relativeUrl, "/v2/models/ContractBusinessPartner");
		});

		test("Create Model Request", () => {
			const request = CreateModelsRequest.build(parsedModel);

			strictEqual(request.method, "POST");
			strictEqual(request.relativeUrl, "/v2/models");
			deepEqual(JSON.parse(request.body as string), parsedModel);
		});

		test("Update Model Request", () => {
			const request = UpdateModelsRequest.build(parsedModel);

			strictEqual(request.method, "PUT");
			strictEqual(request.relativeUrl, "/v2/models");
			deepEqual(JSON.parse(request.body as string), parsedModel);
		});
	});
});
