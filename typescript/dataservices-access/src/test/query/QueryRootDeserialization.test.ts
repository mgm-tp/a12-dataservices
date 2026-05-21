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
import { deepStrictEqual } from "node:assert/strict";

import type { JsonRpc2Request } from "../../json-rpc/index.js";
import { Query, type QueryJsonRpc2Request } from "../../query/index.js";

import { loadResource } from "../utils/ObjectUtils.js";

import OPERATORS = Query.OPERATORS;

suite("QueryRoot Serialization", () => {
	test("QueryRoot2Request::OK", () => {
		const queryRoot: Query.QueryRoot = {
			projectionName: "document",
			targetDocumentModel: "Contract",
			constraint: {
				operator: OPERATORS.AND_OPERATOR,
				operands: [
					{
						operator: OPERATORS.NOT_OPERATOR,
						operand: {
							operator: OPERATORS.HAS_OPERATOR,
							relationshipModel: "ContractBusinessPartner",
							targetRole: "partner"
						}
					},
					{
						operator: OPERATORS.HAS_OPERATOR,
						relationshipModel: "ContractCoInsuredPartner",
						targetRole: "contract",
						constraint: {
							operator: OPERATORS.AND_OPERATOR,
							operands: [
								{
									operator: OPERATORS.EXACT_MATCH_OPERATOR,
									field: "/ContractRoot/ContractValue",
									value: 100
								},
								{
									operator: OPERATORS.EXACT_MATCH_OPERATOR,
									field: "/ContractRoot/CostToCustomer",
									value: 80
								}
							]
						}
					},
					{
						operator: OPERATORS.DATE_FRAGMENT_RANGE_OPERATOR,
						from: "2024-08",
						to: "2024-10",
						field: "__meta.createdAt"
					},
					{
						operator: OPERATORS.DATE_RANGE_OPERATOR,
						from: "2024-08-01",
						to: "2024-08-31",
						field: "__meta.createdAt"
					},
					{
						operator: OPERATORS.DOUBLE_RANGE_OPERATOR,
						from: 10,
						to: 20,
						field: "LengthOfContract"
					},
					{
						operator: OPERATORS.OR_OPERATOR,
						operands: [
							{
								operator: OPERATORS.EXACT_MATCH_OPERATOR,
								value: "super user",
								field: "__meta.createdBy"
							},
							{
								operator: OPERATORS.UNDEFINED_MATCH_OPERATOR,
								field: "SomeUndefinedField"
							},
							{
								operator: OPERATORS.SIMPLE_SEARCH_OPERATOR,
								value: "John",
								fields: ["__meta.modifier", "/ContractRoot/Name"]
							}
						]
					}
				]
			},
			sort: [
				{
					field: "__meta.docRef",
					ignoreCase: true,
					direction: Query.Direction.DESC,
					nullHandling: Query.NullHandling.NULLS_LAST
				}
			],
			links: [
				{
					relationshipModel: "ContractBusinessPartner",
					targetRole: "Partner",
					backReference: "/ContractBusinessPartner"
				}
			],
			paging: {
				pageNumber: 0,
				pageSize: 10
			}
		};
		const jsonRpcRequest: JsonRpc2Request[] = loadResource(
			"./src/test/resources/jsonrpc/request/jsonrpc_query_request.json"
		);
		const queryRequest = jsonRpcRequest[0] as QueryJsonRpc2Request;
		const query = queryRequest.params.query;
		deepStrictEqual(queryRoot, query);
	});
});
