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

import { Query } from "../query/index.js";

suite("JSON-RPC Query Tests", () => {
	suite("Query", () => {
		const queryParams = {
			projectionName: "document",
			targetDocumentModel: "Contract",
			paging: {
				pageNumber: 1,
				pageSize: 10
			},
			sort: [
				{
					direction: "ASC",
					field: "/Contract/ContractName",
					ignoreCase: true,
					nullHandling: "NULLS_FIRST"
				}
			]
		};

		suite("Query Root", () => {
			test("Query Root", () => {
				strictEqual(Query.QueryRoot.isInstance(queryParams), true);
			});

			test("Query Params - Missing Projection", () => {
				const { projectionName, ...paramsWithoutProjection } = queryParams;
				strictEqual(Query.QueryRoot.isInstance(paramsWithoutProjection), false);
			});

			test("Query Params - Different Projection", () => {
				strictEqual(
					Query.QueryRoot.isInstance({ ...queryParams, projectionName: "other-projection" }),
					true
				);
			});

			test("Query Params - Missing Paging", () => {
				const { paging, ...paramsWithoutPaging } = queryParams;
				strictEqual(Query.QueryRoot.isInstance(paramsWithoutPaging), false);
			});

			// TODO A12S-6532: Fix that targetDocumentModel should be mandatory
			test("Query Params - Missing Target DocumentModel", () => {
				const { targetDocumentModel, ...paramsWithoutTargetDocumentModel } = queryParams;
				strictEqual(Query.QueryRoot.isInstance(paramsWithoutTargetDocumentModel), true);
			});
		});

		suite("Query Aggregations", () => {
			const aggregationObjectTemplate = {
				field: "/Fields/Number",
				function: "avg"
			};

			test("Query Aggregation - Missing Function", () => {
				const { function: _, ...aggregationWithoutFunction } = aggregationObjectTemplate;
				strictEqual(Query.AggregationFunction.isInstance(aggregationWithoutFunction), false);
			});

			test("Query Aggregation - Missing Field", () => {
				const { field: _, ...aggregationWithoutFunction } = aggregationObjectTemplate;
				strictEqual(Query.AggregationFunction.isInstance(aggregationWithoutFunction), false);
			});

			[
				{
					aggregation: "avg",
					expectedInstance: Query.AvgAggregationFunction
				},
				{
					aggregation: "min",
					expectedInstance: Query.MinAggregationFunction
				},
				{
					aggregation: "max",
					expectedInstance: Query.MaxAggregationFunction
				},
				{
					aggregation: "sum",
					expectedInstance: Query.SumAggregationFunction
				},
				{
					aggregation: "count",
					expectedInstance: Query.CountAggregationFunction
				}
			].forEach(({ aggregation, expectedInstance }) => {
				test(`Query Aggregation - Check Aggregations (${aggregation})`, () => {
					const aggregationObject = { ...aggregationObjectTemplate, function: aggregation };
					strictEqual(
						Query.AggregationFunction.isInstance(aggregationObject),
						true,
						`Check with function: ${aggregation}`
					);
					strictEqual(
						expectedInstance.isInstance(aggregationObject),
						true,
						`Check with function: ${aggregation}`
					);
					strictEqual(
						expectedInstance.isInstance({ ...aggregationObjectTemplate, function: "other" }),
						false,
						`Check [${aggregation}] against function 'other'`
					);
					strictEqual(
						expectedInstance.isInstance({ ...aggregationObject, alias: `${aggregation}_of_field` }),
						true,
						`Check [${aggregation}] with alias`
					);
				});
			});
		});

		suite("Constraints", () => {
			suite("Operators", () => {
				[
					{
						operator: "and",
						expectedInstance: Query.AndOperator
					},
					{
						operator: "or",
						expectedInstance: Query.OrOperator
					},
					{
						operator: "not",
						expectedInstance: Query.NotOperator
					},
					{
						operator: "has",
						expectedInstance: Query.HasOperator
					},
					{
						operator: "simple_search",
						expectedInstance: Query.SimpleSearchOperator
					},
					{
						operator: "exact_match",
						expectedInstance: Query.ExactMatchOperator
					},
					{
						operator: "undefined_match",
						expectedInstance: Query.UndefinedMatchOperator
					},
					{
						operator: "date_range",
						expectedInstance: Query.DateRangeOperator
					},
					{
						operator: "datefragment_range",
						expectedInstance: Query.DateFragmentRangeOperator
					},
					{
						operator: "double_range",
						expectedInstance: Query.DoubleRangeOperator
					}
				].forEach(({ operator, expectedInstance }) => {
					test(`Operators - Check ${operator}`, () => {
						strictEqual(
							expectedInstance.isInstance({ operator: operator }),
							true,
							`Check with operator '${operator}'`
						);
						strictEqual(
							expectedInstance.isInstance({ operator: "other" }),
							false,
							"Check with operator 'other'"
						);
						strictEqual(expectedInstance.isInstance({}), false, "Check without provided operator");
					});
				});
			});

			suite("Logic Operators", () => {
				[
					{
						operator: "and",
						expectedInstance: Query.AndOperator
					},
					{
						operator: "or",
						expectedInstance: Query.OrOperator
					}
				].forEach(({ operator, expectedInstance }) => {
					suite(`Logic Operator (${operator})`, () => {
						const andOperatorBase = {
							operator: operator
						};

						test(`Logic Operator (${operator}) - Operator String Operands`, () => {
							strictEqual(
								expectedInstance.isInstance({ ...andOperatorBase, operands: ["Foo", "Bar"] }),
								true,
								"And operator with string operands should be instance"
							);
						});

						// TODO A12S-6532: Fix that when missing operands it should not be instances
						test(`Logic Operator (${operator}) - Missing Operands`, () => {
							strictEqual(
								expectedInstance.isInstance(andOperatorBase),
								true,
								"Operands missing should not be instance"
							);
						});
					});
				});
			});

			suite("Exact Match Operator", () => {
				const exactMatchBase = {
					operator: "exact_match",
					field: "/some/Field",
					value: "SomeValue",
					lang: "en",
					caseSensitive: false
				};

				test("Exact Match Operator", () => {
					strictEqual(Query.ExactMatchOperator.isInstance(exactMatchBase), true);
				});

				test("Exact MatchOperator - Missing Field", () => {
					const { field, ...exactMatchWithoutField } = exactMatchBase;
					strictEqual(Query.ExactMatchOperator.isInstance(exactMatchWithoutField), true);
				});

				test("Exact Match Operator - Missing Value", () => {
					const { value, ...exactMatchWithoutValue } = exactMatchBase;
					strictEqual(Query.ExactMatchOperator.isInstance(exactMatchWithoutValue), true);
				});
			});

			suite("Undefined Match Operator", () => {
				const undefinedMatchBase = {
					operator: "undefined_match",
					field: "/some/Field"
				};

				test("Undefined Match Operator", () => {
					strictEqual(Query.UndefinedMatchOperator.isInstance(undefinedMatchBase), true);
				});

				// TODO A12S-6532: Fix that when missing field it should not be instance
				test("Undefined Match Operator - Missing Field", () => {
					const { field, ...undefinedMatchWithoutField } = undefinedMatchBase;
					strictEqual(Query.UndefinedMatchOperator.isInstance(undefinedMatchWithoutField), true);
				});
			});

			suite("Not Operator", () => {
				const notOperatorBase = {
					operator: "not",
					operand: {
						operator: "exact_match",
						field: "/some/Field",
						value: "SomeValue"
					}
				};

				test("Not Operator", () => {
					strictEqual(Query.NotOperator.isInstance(notOperatorBase), true);
				});

				// TODO A12S-6532: Fix that when missing operand it should not be instance
				test("Not Operator - Missing Operand", () => {
					const { operand, ...notOperatorWithoutOperand } = notOperatorBase;
					strictEqual(Query.NotOperator.isInstance(notOperatorWithoutOperand), true);
				});
			});

			suite("Range Operators", () => {
				[
					{
						operator: "double_range",
						expectedInstance: Query.DoubleRangeOperator,
						values: [10, 20]
					},
					{
						operator: "date_range",
						expectedInstance: Query.DateRangeOperator,
						values: ["2023-01-01", "2023-12-31"]
					},
					{
						operator: "datefragment_range",
						expectedInstance: Query.DateFragmentRangeOperator,
						values: ["2023-01", "2023-12"]
					}
				].forEach(({ operator, expectedInstance, values }) => {
					suite(`Range Operator (${operator})`, () => {
						const rangeOperatorBase = {
							operator: operator,
							field: "/some/field",
							from: values[0],
							to: values[1]
						};

						test(`Range Operator - (${operator})`, () => {
							strictEqual(
								expectedInstance.isInstance(rangeOperatorBase),
								true,
								`${operator} operator with all fields should be instance`
							);
						});

						test(`Range Operator (${operator}) - Missing Field`, () => {
							const { field, ...rangeOperatorWithoutField } = rangeOperatorBase;
							strictEqual(
								expectedInstance.isInstance(rangeOperatorWithoutField),
								true,
								`${operator} operator missing field should not be instance`
							);
						});

						test(`Range Operator (${operator}) - Missing From`, () => {
							const { from, ...rangeOperatorWithoutFrom } = rangeOperatorBase;
							strictEqual(
								expectedInstance.isInstance(rangeOperatorWithoutFrom),
								true,
								`${operator} operator missing from should not be instance`
							);
						});

						test(`Range Operator (${operator}) - Missing To`, () => {
							const { to, ...rangeOperatorWithoutTo } = rangeOperatorBase;
							strictEqual(
								expectedInstance.isInstance(rangeOperatorWithoutTo),
								true,
								`${operator} operator missing to should not be instance`
							);
						});

						// TODO A12S-6532: Make this test failing as 'from' and 'to' values are not present
						test(`Range Operator (${operator}) - Missing From & To`, () => {
							const { from, to, ...rangeOperatorWithoutFromTo } = rangeOperatorBase;
							strictEqual(
								expectedInstance.isInstance(rangeOperatorWithoutFromTo),
								true,
								`${operator} operator missing from and to should not be instance`
							);
						});
					});
				});
			});

			suite("Simple Search Operator", () => {
				const simpleSearchBase = {
					operator: "simple_search",
					value: "foo",
					values: ["foo", "bar"],
					fields: ["/some/field"]
				};

				test("Simple Search Operator", () => {
					strictEqual(Query.SimpleSearchOperator.isInstance(simpleSearchBase), true);
				});

				test("Simple Search Operator - Missing Value", () => {
					const { value, ...simpleSearchWithoutValue } = simpleSearchBase;
					strictEqual(Query.SimpleSearchOperator.isInstance(simpleSearchWithoutValue), true);
				});

				test("Simple Search Operator - Missing Values", () => {
					const { values, ...simpleSearchWithoutValue } = simpleSearchBase;
					strictEqual(Query.SimpleSearchOperator.isInstance(simpleSearchWithoutValue), true);
				});

				// TODO A12S-6532: Fix that when neither 'value' nor 'values' is present object is not instance
				test("Simple Search Operator - Missing Value & Values", () => {
					const { value, values, ...simpleSearchWithoutValue } = simpleSearchBase;
					strictEqual(Query.SimpleSearchOperator.isInstance(simpleSearchWithoutValue), true);
				});
			});
		});

		suite("Order", () => {
			const orderObject = {
				field: "/some/Field",
				direction: "ASC",
				ignoreCase: true,
				nullHandling: "NULLS_FIRST"
			};

			test("Order", () => {
				strictEqual(Query.Order.isInstance(orderObject), true);
			});

			// TODO A12S-6532: Fix that invalid direction values should not be instances
			["ASC", "DESC", "OTHER"].forEach(item => {
				test(`Order - Direction (${item})`, () => {
					const orderWithDirection = { ...orderObject, direction: item };
					const shouldBeInstance = item === "ASC" || item === "DESC" || item === "OTHER";
					strictEqual(Query.Order.isInstance(orderWithDirection), shouldBeInstance);
				});
			});

			// TODO A12S-6532: Fix that invalid nullHandling values should not be instances
			["NULLS_FIRST", "NULLS_LAST", "NONE"].forEach(item => {
				test(`Order - Null Handling (${item})`, () => {
					const orderWithNullHandling = { ...orderObject, nullHandling: item };
					const shouldBeInstance =
						item === "NULLS_FIRST" || item === "NULLS_LAST" || item === "NONE";
					strictEqual(Query.Order.isInstance(orderWithNullHandling), shouldBeInstance);
				});
			});

			test("Order - Missing Field", () => {
				const { field, ...orderWithoutField } = orderObject;
				strictEqual(Query.Order.isInstance(orderWithoutField), false);
			});

			test("Order - Missing Direction", () => {
				const { direction, ...orderWithoutDirection } = orderObject;
				strictEqual(Query.Order.isInstance(orderWithoutDirection), false);
			});

			test("Order - Missing IgnoreCase", () => {
				const { ignoreCase, ...orderWithoutIgnoreCase } = orderObject;
				strictEqual(Query.Order.isInstance(orderWithoutIgnoreCase), false);
			});

			test("Order - Missing Null Handling", () => {
				const { nullHandling, ...orderWithoutNullHandling } = orderObject;
				strictEqual(Query.Order.isInstance(orderWithoutNullHandling), false);
			});
		});

		suite("Paging", () => {
			const pagingObject = { pageNumber: 0, pageSize: 10 };

			test("Paging", () => {
				strictEqual(Query.Paging.isInstance(pagingObject), true);
			});

			test("Paging - Missing Page Size", () => {
				const { pageSize, ...pagingWithoutPageSize } = pagingObject;
				strictEqual(Query.Paging.isInstance(pagingWithoutPageSize), false);
			});

			test("Paging - Missing Page Number", () => {
				const { pageNumber, ...pagingWithoutPageNumber } = pagingObject;
				strictEqual(Query.Paging.isInstance(pagingWithoutPageNumber), false);
			});
		});

		suite("Query Link", () => {
			suite("Request", () => {
				const linkQueryObject = {
					relationshipModel: "ContractCoInsuredPartner",
					targetRole: "Partner",
					constraint: {
						operator: "exact_match",
						field: "/BusinessPartnerRoot/Name",
						value: "Nora"
					},
					linkDocumentConstraint: {
						operator: "exact_match",
						field: "/CoInsuredRoot/Role",
						value: "Barrister"
					},
					linkDocumentFields: ["/CoInsuredRoot/Role", "/CoInsuredRoot/Name"],
					maxDepth: 10,
					backReference: "foo"
				};

				test("Query Link", () => {
					strictEqual(Query.QueryLink.isInstance(linkQueryObject), true);
				});

				test("Query Link - Core", () => {
					const {
						linkDocumentFields,
						linkDocumentConstraint,
						maxDepth,
						backReference,
						constraint,
						...coreFields
					} = linkQueryObject;
					strictEqual(Query.QueryLink.isInstance(coreFields), true);
				});

				test("Query Link - Missing Core", () => {
					const { relationshipModel, ...linkWithoutRelationshipModel } = linkQueryObject;
					strictEqual(Query.QueryLink.isInstance(linkWithoutRelationshipModel), false);

					const { targetRole, ...linkWithoutTargetRole } = linkQueryObject;
					strictEqual(Query.QueryLink.isInstance(linkWithoutTargetRole), false);
				});
			});

			suite("Response", () => {
				const documentTreeResultObject = {
					type: "CHILD",
					docRef: "BusinessPartner/1",
					relationshipModel: "ContractCoInsuredPartner",
					sourceRole: "Contract",
					sourceDocRef: "Contract/2",
					targetRole: "Partner",
					targetDocRef: "BusinessPartner/3",
					document: {
						field1: "foo"
					},
					linkId: "1",
					depth: 0,
					documentModelName: "BusinessPartner"
				};

				test("Document Tree Result", () => {
					strictEqual(Query.DocumentTreeResult.isInstance(documentTreeResultObject), true);
				});

				test("Document TreeResult - Core Values", () => {
					const {
						relationshipModel,
						sourceRole,
						sourceDocRef,
						targetRole,
						targetDocRef,
						linkId,
						documentModelName,
						depth,
						...coreValues
					} = documentTreeResultObject;
					strictEqual(Query.DocumentTreeResult.isInstance(coreValues), true);
				});

				test("Document Tree Result - Missing Type", () => {
					const { type, ...documentWithoutType } = documentTreeResultObject;
					strictEqual(Query.DocumentTreeResult.isInstance(documentWithoutType), false);
				});

				test("Document Tree Result - Invalid Type", () => {
					const documentWithInvalidType = { ...documentTreeResultObject, type: "OTHER" };
					strictEqual(Query.DocumentTreeResult.isInstance(documentWithInvalidType), true);
				});

				test("Document Tree Result - Missing Document Reference", () => {
					const { docRef, ...documentWithoutDocRef } = documentTreeResultObject;
					strictEqual(Query.DocumentTreeResult.isInstance(documentWithoutDocRef), false);
				});

				test("Document Tree Result - Missing Document", () => {
					const { document, ...documentWithoutDocument } = documentTreeResultObject;
					strictEqual(Query.DocumentTreeResult.isInstance(documentWithoutDocument), false);
				});
			});
		});
	});
});
