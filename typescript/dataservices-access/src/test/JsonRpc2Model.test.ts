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

			const omittedModelsResponse = { ...rpcResponse, result: {} };
			strictEqual(
				ListModelsJsonRpc2Response.isInstance(omittedModelsResponse),
				false,
				"List Validation Codes response without validations was incorrectly recognized"
			);
		});
	});
});
