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

import { JsonRpc2Request, JsonRpc2Response } from "../json-rpc/index.js";

import { addDocumentRequest } from "./resources/jsonrpc/request/document_operation_requests.js";

suite("JsonRpc2Test", () => {
	const positiveResponse = {
		jsonrpc: "2.0",
		id: "PassingOperation",
		result: {
			success: true
		}
	};

	const negativeResponse = {
		jsonrpc: "2.0",
		id: "ErrorOperation",
		error: {
			code: -999999,
			message: "Some error message.",
			data: {
				level: "ERROR",
				title: {
					key: "error.some.title",
					priority: null,
					default: "Some error title."
				},
				description: {
					key: "error.some.description",
					priority: null,
					default: "Some error description."
				},
				details: {
					code: "-999999",
					subsystem: "GENERIC",
					time: "2012-12-12T12:12:12"
				}
			}
		}
	};

	suite("JSON-RPC Request Tests", () => {
		test("JSON-RPC Request - Passing", () => {
			strictEqual(JsonRpc2Request.isInstance(addDocumentRequest), true);
		});

		test("JSON-RPC Request - JsonRpc Field", () => {
			const { jsonrpc, ...requestBody } = { ...addDocumentRequest };
			strictEqual(JsonRpc2Request.isInstance(requestBody), false, "Check omitted field");

			strictEqual(
				JsonRpc2Request.isInstance({ ...addDocumentRequest, jsonrpc: null }),
				false,
				"Check with 'null' value"
			);
		});

		test("JSON-RPC Request - Method Field", () => {
			const { method, ...requestBody } = { ...addDocumentRequest };
			strictEqual(JsonRpc2Request.isInstance(requestBody), false, "Check omitted field");

			strictEqual(
				JsonRpc2Request.isInstance({ ...addDocumentRequest, method: null }),
				false,
				"Check with 'null' value"
			);
		});

		// TODO A12S-6532: Make 'params' field mandatory in method
		test("JSON-RPC Request - Id Field", () => {
			const { id, ...requestBody } = { ...addDocumentRequest };
			strictEqual(JsonRpc2Request.isInstance(requestBody), true, "Check omitted field");

			strictEqual(
				JsonRpc2Request.isInstance({ ...addDocumentRequest, id: null }),
				true,
				"Check with 'null' value"
			);
		});

		// TODO A12S-6532: Make 'params' field mandatory in method
		test("JSON-RPC Request - Params Field", () => {
			const { params, ...requestBody } = { ...addDocumentRequest };
			strictEqual(JsonRpc2Request.isInstance(requestBody), true, "Check omitted field");

			strictEqual(
				JsonRpc2Request.isInstance({ ...addDocumentRequest, params: null }),
				true,
				"Check with 'null' value"
			);
		});
	});

	suite("JSON-RPC Response Tests", () => {
		suite("Error Type", () => {
			suite("Error Type", () => {
				test("Check Error Type", () => {
					strictEqual(JsonRpc2Response.error.isInstance(negativeResponse), true);
					strictEqual(JsonRpc2Response.hasError(negativeResponse as JsonRpc2Response), true);
				});

				// Is it correct that 'error.data.title' is undefined when checking 'JsonRpc2Response.error'?
				test("Check Error Type - Nested Data Validation", () => {
					const testResponse = {
						...negativeResponse,
						error: {
							...negativeResponse.error,
							data: { ...negativeResponse.error.data, title: undefined }
						}
					};
					strictEqual(JsonRpc2Response.error.isInstance(testResponse), true);
					strictEqual(JsonRpc2Response.hasError(testResponse as JsonRpc2Response), true);
				});

				test("Check Error Type - Omitted Error", () => {
					const { error, ...separatedResponse } = negativeResponse;
					strictEqual(JsonRpc2Response.error.isInstance(separatedResponse), false);
				});

				test("Check Error Type - Undefined Error", () => {
					strictEqual(
						JsonRpc2Response.error.isInstance({ ...negativeResponse, error: undefined }),
						false
					);
				});

				test("Check Error Type", () => {
					strictEqual(JsonRpc2Response.JsonRpc2Error.isInstance(negativeResponse.error), true);
				});

				test("Check Error Type - WithOmittedCode", () => {
					const { code: _, ...testCaseError } = { ...negativeResponse.error };
					strictEqual(JsonRpc2Response.JsonRpc2Error.isInstance(testCaseError), false);
				});

				test("Check Error Type - WithUndefinedCode", () => {
					strictEqual(
						JsonRpc2Response.JsonRpc2Error.isInstance({
							...negativeResponse.error,
							code: undefined
						}),
						false
					);
				});

				test("Check Error Type - WithOmittedMessage", () => {
					const { message: _, ...testCaseError } = { ...negativeResponse.error };
					strictEqual(JsonRpc2Response.JsonRpc2Error.isInstance(testCaseError), false);
				});

				test("Check Error Type - WithUndefinedMessage", () => {
					strictEqual(
						JsonRpc2Response.JsonRpc2Error.isInstance({
							...negativeResponse.error,
							message: undefined
						}),
						false
					);
				});

				test("Check Error Type - WithOmittedData", () => {
					const { data: _, ...testCaseError } = { ...negativeResponse.error };
					strictEqual(JsonRpc2Response.JsonRpc2Error.isInstance(testCaseError), true);
				});

				test("Check Error Type - WithUndefinedData", () => {
					strictEqual(
						JsonRpc2Response.JsonRpc2Error.isInstance({
							...negativeResponse.error,
							data: undefined
						}),
						true
					);
				});

				test("Check Error Type - WithStringData", () => {
					strictEqual(
						JsonRpc2Response.JsonRpc2Error.isInstance({
							...negativeResponse.error,
							data: "Hello world"
						}),
						true
					);
				});

				test("Check Error Type - WithEmptyData", () => {
					strictEqual(
						JsonRpc2Response.JsonRpc2Error.isInstance({ ...negativeResponse.error, data: {} }),
						true
					);
				});

				// TODO A12S-6532: Both ok and error types are detected if both are present
				test("Check Result And Error Case", () => {
					const responseWithBoth = { ...negativeResponse, result: { success: false } };
					strictEqual(JsonRpc2Response.error.isInstance(responseWithBoth), true);
					strictEqual(JsonRpc2Response.ok.isInstance(responseWithBoth), true);
					strictEqual(JsonRpc2Response.hasError(responseWithBoth as JsonRpc2Response), true);
				});
			});

			suite("Exception Type", () => {
				const errorData = negativeResponse.error.data;

				test("Check Exception Type", () => {
					strictEqual(JsonRpc2Response.Exception.isInstance(errorData), true);
				});

				test("Check Exception Type - Undefined Title", () => {
					const { title, ...separatedErrorData } = errorData;
					strictEqual(JsonRpc2Response.Exception.isInstance(separatedErrorData), false);
				});

				test("Check Exception Type - String Title", () => {
					strictEqual(
						JsonRpc2Response.Exception.isInstance({ ...errorData, title: "Hello world" }),
						false
					);
				});

				test("Check Exception Type - Undefined Description", () => {
					const { title, ...separatedErrorData } = errorData;
					strictEqual(JsonRpc2Response.Exception.isInstance(separatedErrorData), false);
				});

				test("Check Exception Type - String Description", () => {
					strictEqual(
						JsonRpc2Response.Exception.isInstance({ ...errorData, description: "Hello world" }),
						false
					);
				});

				// TODO A12S-6532: Fix this test so that it checks for 'details' being mandatory in 'Exception' type
				test("Check Exception Type - Undefined Details", () => {
					const { details, ...separatedErrorData } = errorData;
					strictEqual(JsonRpc2Response.Exception.isInstance(separatedErrorData), true);
				});

				// TODO A12S-6532: Fix this test so that it checks for 'details' being mandatory in 'Exception' type
				test("Check Exception Type - String Details", () => {
					strictEqual(
						JsonRpc2Response.Exception.isInstance({ ...errorData, details: "Hello world" }),
						true
					);
				});

				suite("Localizable Message", () => {
					const localizedMessage = errorData.title;

					// TODO A12S-6532: Fix this test so that it checks for 'priority' being part of 'LocalizedMessage' type
					test("Check Localizable Message", () => {
						strictEqual(JsonRpc2Response.LocalizableMessage.isInstance(localizedMessage), true);
					});

					test("Check Localizable Message - Omitted Key", () => {
						const { key: _, ...separatedErrorData } = { ...localizedMessage };
						strictEqual(JsonRpc2Response.LocalizableMessage.isInstance(separatedErrorData), false);
					});

					test("Check Localizable Message - Undefined Key", () => {
						strictEqual(
							JsonRpc2Response.LocalizableMessage.isInstance({
								...localizedMessage,
								key: undefined
							}),
							false
						);
					});

					test("Check Localizable Message - Omitted Default", () => {
						const { default: _, ...separatedErrorData } = { ...localizedMessage };
						strictEqual(JsonRpc2Response.LocalizableMessage.isInstance(separatedErrorData), false);
					});

					test("Check Localizable Message - Omitted Key", () => {
						strictEqual(
							JsonRpc2Response.LocalizableMessage.isInstance({
								...localizedMessage,
								default: undefined
							}),
							false
						);
					});

					// TODO A12S-6532: Fix this test so that it checks for 'priority' being part of 'LocalizedMessage' type
					test("Check Localizable Message - Omitted Priority", () => {
						const { priority: _, ...separatedErrorData } = { ...localizedMessage };
						strictEqual(JsonRpc2Response.LocalizableMessage.isInstance(separatedErrorData), true);
					});

					// TODO A12S-6532: Fix this test so that it checks for 'priority' being part of 'LocalizedMessage' type
					test("Check Localizable Message - Undefined Priority", () => {
						strictEqual(
							JsonRpc2Response.LocalizableMessage.isInstance({
								...localizedMessage,
								priority: undefined
							}),
							true
						);
					});
				});
			});
		});

		suite("OK Response", () => {
			test("Check OK response", () => {
				strictEqual(JsonRpc2Response.ok.isInstance(positiveResponse), true);
				strictEqual(JsonRpc2Response.hasError(positiveResponse as JsonRpc2Response), false);
			});

			test("Check OK response - Null Result", () => {
				const testResponse = { ...positiveResponse, result: null };
				strictEqual(JsonRpc2Response.ok.isInstance(testResponse), true);
				strictEqual(JsonRpc2Response.hasError(testResponse as JsonRpc2Response), false);
			});

			test("Check OK response - Undefined Result", () => {
				const testResponse = { ...positiveResponse, result: undefined };
				strictEqual(JsonRpc2Response.ok.isInstance(testResponse), true);
				strictEqual(JsonRpc2Response.hasError(testResponse as JsonRpc2Response), false);
			});

			test("Check OK response - Omitted Result", () => {
				const { result, ...positiveRes } = positiveResponse;
				strictEqual(JsonRpc2Response.ok.isInstance(positiveRes), false);
				strictEqual(JsonRpc2Response.hasError(positiveRes as JsonRpc2Response), false);
			});

			// TODO A12S-6532: Both ok and error types are detected if both are present and result is undefined
			test("Check OK response - Error Attribute", () => {
				const responseWithError = {
					...positiveResponse,
					result: undefined,
					error: negativeResponse.error
				};
				strictEqual(JsonRpc2Response.ok.isInstance(responseWithError), true);
				strictEqual(JsonRpc2Response.hasError(responseWithError as JsonRpc2Response), true);
			});
		});

		suite("Multiple Responses", () => {
			test("Multiple Responses - One error", () => {
				strictEqual(
					JsonRpc2Response.hasErrors([positiveResponse, negativeResponse] as JsonRpc2Response[]),
					true
				);
			});

			test("Multiple Responses - No errors", () => {
				strictEqual(
					JsonRpc2Response.hasErrors([positiveResponse, positiveResponse] as JsonRpc2Response[]),
					false
				);
			});

			test("Multiple Responses - All errors", () => {
				strictEqual(
					JsonRpc2Response.hasErrors([negativeResponse, negativeResponse] as JsonRpc2Response[]),
					true
				);
			});
		});
	});
});
