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

import {
	ListValidationsJsonRpc2Request,
	ListValidationsJsonRpc2Response,
	ListModelsJsonRpc2Response,
	ModelJsonRpc2Request
} from "../model/index.js";

import {
	listModelsRequest,
	listValidationCodesRequest
} from "./resources/jsonrpc/request/model_operation_requests.js";
import { loadResource } from "./utils/ObjectUtils.js";

suite("JSON-RPC Model Tests", () => {
	suite("List Models Operation", () => {
		test("Request", () => {
			strictEqual(
				ModelJsonRpc2Request.ListModelsJsonRpc2Request.isInstance(listModelsRequest),
				true,
				"Check valid request"
			);

			const omittedParamsRequest = { ...listModelsRequest, params: {} };
			strictEqual(
				ModelJsonRpc2Request.ListModelsJsonRpc2Request.isInstance(omittedParamsRequest),
				false,
				"Check invalid request (no model names)"
			);
		});

		test("Response", () => {
			const rpcResponse = loadResource(
				"./src/test/resources/jsonrpc/response/jsonrpc_list_models_response.json"
			);
			strictEqual(
				ListModelsJsonRpc2Response.isInstance(rpcResponse),
				true,
				"List Models response was not recognized"
			);

			const omittedModelsResponse = { ...rpcResponse, result: {} };
			strictEqual(
				ListModelsJsonRpc2Response.isInstance(omittedModelsResponse),
				false,
				"List Models response without models was incorrectly recognized"
			);

			const responseWithoutResult = { jsonrpc: "2.0", id: "ListModels" };
			strictEqual(
				ListModelsJsonRpc2Response.isInstance(responseWithoutResult),
				false,
				"List Models response without result field should not be valid"
			);

			const responseWithWrongModelsType = {
				...rpcResponse,
				result: { models: "not an object" }
			};
			strictEqual(
				ListModelsJsonRpc2Response.isInstance(responseWithWrongModelsType),
				true,
				"List Models response only checks for presence of models field"
			);

			const responseWithError = {
				...rpcResponse,
				error: { code: -1, message: "Error", data: {} }
			};
			strictEqual(
				ListModelsJsonRpc2Response.isInstance(responseWithError),
				false,
				"List Models response should not be valid with both result and error fields"
			);
		});
	});

	suite("List Validation Codes Operation", () => {
		test("Request", () => {
			strictEqual(
				ListValidationsJsonRpc2Request.isInstance(listValidationCodesRequest),
				true,
				"Check valid request"
			);

			const omittedParamsRequest = { ...listValidationCodesRequest, params: {} };
			strictEqual(
				ListValidationsJsonRpc2Request.isInstance(omittedParamsRequest),
				false,
				"Check invalid request (no model names)"
			);
		});

		test("Response", () => {
			const rpcResponse = loadResource(
				"./src/test/resources/jsonrpc/response/jsonrpc_list_validation_codes_response.json"
			);
			strictEqual(
				ListValidationsJsonRpc2Response.isInstance(rpcResponse),
				true,
				"List Validation Codes response was not recognized"
			);

			const omittedValidationsResponse = { ...rpcResponse, result: {} };
			strictEqual(
				ListValidationsJsonRpc2Response.isInstance(omittedValidationsResponse),
				false,
				"List Validation Codes response without documentValidationCodes was incorrectly recognized"
			);

			const { documentValidationCodes, ...resultWithoutValidations } = rpcResponse.result;
			const responseWithoutValidations = { ...rpcResponse, result: resultWithoutValidations };
			strictEqual(
				ListValidationsJsonRpc2Response.isInstance(responseWithoutValidations),
				false,
				"List Validation Codes response without documentValidationCodes field was incorrectly recognized"
			);

			const responseWithoutResult = { jsonrpc: "2.0", id: "ListValidationCodes" };
			strictEqual(
				ListValidationsJsonRpc2Response.isInstance(responseWithoutResult),
				false,
				"List Validation Codes response without result field should not be valid"
			);

			const responseWithError = {
				...rpcResponse,
				error: { code: -1, message: "Error", data: {} }
			};
			strictEqual(
				ListValidationsJsonRpc2Response.isInstance(responseWithError),
				false,
				"List Validation Codes response should not be valid with both result and error fields"
			);
		});
	});
});
