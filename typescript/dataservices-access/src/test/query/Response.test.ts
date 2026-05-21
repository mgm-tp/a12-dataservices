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
import { expectTypeOf } from "expect-type";

import { type QueryJsonRpc2Response, type QueryJsonRpc2Request, Query } from "../../query/index.js";

/* eslint-disable @typescript-eslint/no-unused-vars */
suite("Response", () => {
	const targetDocumentModel = "SearchingTestModel";
	const paging: Query.Paging = {
		pageNumber: 0,
		pageSize: 10
	};

	function create<Q extends Query.QueryRoot>(query: Q): QueryJsonRpc2Request<Q> {
		return { method: "QUERY", id: "req", jsonrpc: "2.0", params: { query } };
	}

	function fetch<Q extends Query.QueryRoot>(
		request: QueryJsonRpc2Request<Q>
	): QueryJsonRpc2Response.FromQuery<Q> {
		return {} as QueryJsonRpc2Response.FromQuery<Q>;
	}

	suite("Document Projection", () => {
		suite("IsGetSingleDocumentQuery", () => {
			test("should return true for exact_match with docRef field constraint", () => {
				const request = create({
					projectionName: "document",
					targetDocumentModel,
					paging,
					constraint: {
						operator: Query.OPERATORS.EXACT_MATCH_OPERATOR,
						field: "/__meta/docRef",
						value: "abc"
					}
				});

				expectTypeOf<
					QueryJsonRpc2Response.IsQuerySingleDocumentConstraint<typeof request.params.query>
				>().toEqualTypeOf<true>();
			});

			test("should return never for none constraint", () => {
				const request = create({
					projectionName: "document",
					targetDocumentModel,
					paging
				});

				expectTypeOf<
					QueryJsonRpc2Response.IsQuerySingleDocumentConstraint<typeof request.params.query>
				>().toEqualTypeOf<false>();
			});

			test("should return never for exact_match with non-docRef field constraint", () => {
				const request = create({
					projectionName: "document",
					targetDocumentModel,
					paging,
					constraint: {
						operator: Query.OPERATORS.EXACT_MATCH_OPERATOR,
						field: "/teamName",
						value: "abc"
					}
				});

				expectTypeOf<
					QueryJsonRpc2Response.IsQuerySingleDocumentConstraint<typeof request.params.query>
				>().toEqualTypeOf<false>();
			});
		});

		suite("given simple query", () => {
			test("should return list of documents, empty link and other results", () => {
				const request = create({
					projectionName: "document",
					targetDocumentModel,
					paging
				});

				const response = fetch(request);

				expectTypeOf<typeof response.result.entries>().toEqualTypeOf<
					QueryJsonRpc2Response.DocumentEntry[]
				>();
				expectTypeOf<typeof response.result.links>().toEqualTypeOf<[]>();
			});
		});

		suite("given GetDocument constraint query", () => {
			test("should return zero or one document, empty link and other results", () => {
				const request = create({
					projectionName: "document",
					targetDocumentModel,
					paging,
					constraint: {
						operator: Query.OPERATORS.EXACT_MATCH_OPERATOR,
						field: "/__meta/docRef",
						value: "abc"
					}
				});

				const response = fetch(request);

				expectTypeOf<typeof response.result.entries>().toEqualTypeOf<
					[QueryJsonRpc2Response.DocumentEntry?]
				>();
				expectTypeOf<typeof response.result.links>().toEqualTypeOf<[]>();
			});
		});

		suite("given other constraint query", () => {
			test("should return a list of documents, empty link and other results", () => {
				const request = create({
					projectionName: "document",
					targetDocumentModel,
					paging,
					constraint: {
						operator: Query.OPERATORS.EXACT_MATCH_OPERATOR,
						field: "/Team/Name",
						value: "abc"
					}
				});

				const response = fetch(request);

				expectTypeOf<typeof response.result.entries>().toEqualTypeOf<
					QueryJsonRpc2Response.DocumentEntry[]
				>();
				expectTypeOf<typeof response.result.links>().toEqualTypeOf<[]>();
			});
		});

		suite("given other constraint query with aggregation", () => {
			test("should return a list of documents, empty link and other results", () => {
				const request = create({
					projectionName: "document",
					targetDocumentModel,
					paging,
					constraint: {
						operator: Query.OPERATORS.EXACT_MATCH_OPERATOR,
						field: "/Team/Name",
						value: "abc"
					},
					aggregation: {
						aggregations: [{ field: "/Fields/price", function: "sum", alias: "sum_of_price" }],
						group: [{ field: "/__meta/creator", alias: "creator" }]
					}
				});

				const response = fetch(request);

				expectTypeOf<typeof response.result.entries>().toEqualTypeOf<
					QueryJsonRpc2Response.AggregationEntry[]
				>();
				expectTypeOf<typeof response.result.links>().toEqualTypeOf<[]>();
			});
		});

		suite("given links query", () => {
			test("should return a list of documents, a list of links and empty other results", () => {
				const request = create({
					projectionName: "document",
					targetDocumentModel,
					paging,
					links: [
						{
							relationshipModel: "ContractCoInsuredPartner",
							targetRole: "Partner"
						}
					]
				});

				const response = fetch(request);

				expectTypeOf<typeof response.result.entries>().toEqualTypeOf<
					QueryJsonRpc2Response.DocumentEntry[]
				>();
				expectTypeOf<typeof response.result.links>().toEqualTypeOf<QueryJsonRpc2Response.Link[]>();
			});
		});

		suite("given aggregation query", () => {
			test("should return correct document type", () => {
				const request = create({
					projectionName: "document",
					targetDocumentModel,
					paging,
					aggregation: {
						aggregations: [{ field: "/Fields/price", function: "sum", alias: "sum_of_price" }],
						group: [{ field: "/__meta/creator", alias: "creator" }]
					}
				});

				const response = fetch(request);

				expectTypeOf<typeof response.result.entries>().toEqualTypeOf<
					QueryJsonRpc2Response.AggregationEntry[]
				>();
				expectTypeOf<
					(typeof response.result.entries)[number]["document"]
				>().toEqualTypeOf<QueryJsonRpc2Response.AggregationResult>();

				expectTypeOf<typeof response.result.links>().toEqualTypeOf<[]>();
			});
		});
	});

	suite("Document graph projection", () => {
		suite("Given exact_match operator on docRef", () => {
			test("should return zero or one document, a list of links and empty other results", () => {
				const request = create({
					projectionName: "document-graph",
					targetDocumentModel,
					paging,
					constraint: {
						operator: Query.OPERATORS.EXACT_MATCH_OPERATOR,
						field: "/__meta/docRef",
						value: "Contract/8410aaeb"
					}
				});

				const response = fetch(request);

				expectTypeOf<typeof response.result.entries>().toEqualTypeOf<
					[QueryJsonRpc2Response.DocumentEntry?]
				>();
				expectTypeOf<typeof response.result.links>().toEqualTypeOf<QueryJsonRpc2Response.Link[]>();
			});
		});
	});

	suite("CDD projection", () => {
		suite("Given exact_match operator on docRef", () => {
			test("should return zero or one document, empty links and empty other results", () => {
				const request = create({
					projectionName: "cdd",
					targetDocumentModel,
					paging,
					constraint: {
						operator: Query.OPERATORS.EXACT_MATCH_OPERATOR,
						field: "/__meta/docRef",
						value: "Contract/8410aaeb"
					}
				});

				const response = fetch(request);

				expectTypeOf<typeof response.result.entries>().toEqualTypeOf<
					[QueryJsonRpc2Response.DocumentEntry?]
				>();
				expectTypeOf<typeof response.result.links>().toEqualTypeOf<[]>();
			});
		});

		suite("Given other operator", () => {
			test("should return a list of documents, empty links and empty other results", () => {
				const request = create({
					projectionName: "cdd",
					targetDocumentModel,
					paging,
					constraint: {
						operator: Query.OPERATORS.EXACT_MATCH_OPERATOR,
						field: "/Contract/Name",
						value: "Insurance"
					}
				});

				const response = fetch(request);

				expectTypeOf<typeof response.result.entries>().toEqualTypeOf<
					QueryJsonRpc2Response.DocumentEntry[]
				>();
				expectTypeOf<typeof response.result.links>().toEqualTypeOf<[]>();
			});
		});

		suite("given other constraint query with aggregation", () => {
			test("should return a list of documents, empty link and other results", () => {
				const request = create({
					projectionName: "cdd",
					targetDocumentModel,
					paging,
					constraint: {
						operator: Query.OPERATORS.EXACT_MATCH_OPERATOR,
						field: "/Contract/Name",
						value: "Insurance"
					},
					aggregation: {
						aggregations: [{ field: "/Fields/price", function: "sum", alias: "sum_of_price" }],
						group: [{ field: "/__meta/creator", alias: "creator" }]
					}
				});

				const response = fetch(request);

				expectTypeOf<typeof response.result.entries>().toEqualTypeOf<
					QueryJsonRpc2Response.AggregationEntry[]
				>();
				expectTypeOf<typeof response.result.links>().toEqualTypeOf<[]>();
			});
		});
	});

	suite("Export Cdd Csv projection", () => {
		test("should return zero or one document, empty links and empty other results", () => {
			const request = create({
				projectionName: "exportCddCsv",
				targetDocumentModel,
				paging
			});

			const response = fetch(request);

			expectTypeOf<typeof response.result.entries>().toEqualTypeOf<undefined>();
			expectTypeOf<typeof response.result.links>().toEqualTypeOf<undefined>();
			expectTypeOf<typeof response.result.otherResults>().toEqualTypeOf<{ downloadUrl: string }>();
		});
	});
});
