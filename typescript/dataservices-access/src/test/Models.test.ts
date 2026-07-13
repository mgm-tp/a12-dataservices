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

import type { ErrorPayload } from "../common/index.js";
import type { DeleteModelsResponse } from "../model/index.js";
import {
	MODEL_UNIQUE_CONSTRAINT_VALIDATION_ERROR_CODE,
	ModelsErrorResponse,
	ModelsResponse
} from "../model/index.js";
import {
	ModelsRequest,
	ModelsResult,
	DeleteModelsRequest,
	UpdateModelsRequest,
	CreateModelsRequest
} from "../model/index.js";

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

		test("Check Model Response", () => {
			const getModelResponse = JSON.parse(getModelResponseString);
			strictEqual(ModelsResponse.isInstance(getModelResponse), true, "Check valid model response");
		});

		test("Check Model Response - ErrorPayload", () => {
			const errorResponse: ErrorPayload = {
				errorType: "PRECONDITIONS_FAILED",
				message: "Something is wrong"
			};
			strictEqual(ModelsResponse.isInstance(errorResponse), true, "Check error payload response");
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

	suite("Model Unique Constraint Validation Error", () => {
		const modelUniqueConstraintErrorResponse = {
			level: "ERROR",
			shortMessage: { key: "error.model.unique.short", default: "Model validation failed." },
			longMessage: {
				key: "error.model.unique.long",
				default: "A unique constraint definition is invalid."
			},
			errorDetail: {
				code: MODEL_UNIQUE_CONSTRAINT_VALIDATION_ERROR_CODE,
				subsystem: "DATASERVICES",
				time: "2024-01-01T00:00:00Z"
			}
		};

		test("Recognizes model unique constraint validation error", () => {
			strictEqual(
				ModelsErrorResponse.isModelUniqueConstraintValidationError(
					modelUniqueConstraintErrorResponse
				),
				true
			);
		});

		test("Rejects a successful model response", () => {
			strictEqual(ModelsErrorResponse.isModelUniqueConstraintValidationError(parsedModel), false);
		});

		test("Rejects an error with a different error code", () => {
			const otherError = {
				...modelUniqueConstraintErrorResponse,
				errorDetail: { ...modelUniqueConstraintErrorResponse.errorDetail, code: "-32060" }
			};
			strictEqual(ModelsErrorResponse.isModelUniqueConstraintValidationError(otherError), false);
		});

		test("Rejects an error without errorDetail", () => {
			const { errorDetail, ...errorWithoutDetail } = modelUniqueConstraintErrorResponse;
			strictEqual(
				ModelsErrorResponse.isModelUniqueConstraintValidationError(errorWithoutDetail),
				false
			);
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
