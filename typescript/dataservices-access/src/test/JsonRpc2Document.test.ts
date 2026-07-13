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

import { LoadAttachmentUrlJsonRpc2 } from "../Attachment/index.js";
import {
	AddDocumentJsonRpc2Response,
	DocumentJsonRpc2Request,
	GetDocumentJsonRpc2Response,
	ModifyJsonRpc2Response,
	ValidateDocumentJsonRpc2Response
} from "../Document/index.js";
import { JsonRpc2Request } from "../json-rpc/index.js";

import {
	addDocumentRequest,
	copyDocumentRequest,
	deleteDocumentRequest,
	getDocumentRequest,
	modifyDocumentRequest,
	multiDeleteDocumentRequest,
	partialModifyDocumentRequest,
	validateDocumentRequest
} from "./resources/jsonrpc/request/document_operation_requests.js";
import { omitAtPath, replaceAtPath } from "./utils/ObjectUtils.js";

suite("JSON-RPC Document Tests", () => {
	suite("Add Document Operation", () => {
		test("Request", () => {
			strictEqual(DocumentJsonRpc2Request.AddJsonRpc2Request.isInstance(addDocumentRequest), true);

			strictEqual(
				DocumentJsonRpc2Request.DeleteJsonRpc2Request.isInstance({
					...addDocumentRequest,
					params: {}
				}),
				false,
				"Check with 'docRef' omitted"
			);
		});

		test("Response", () => {
			const rpcResponse = {
				jsonrpc: "2.0",
				id: "AddDocument",
				result: {
					docRef: "Contract/1"
				}
			};

			strictEqual(
				AddDocumentJsonRpc2Response.isInstance(rpcResponse),
				true,
				"Check operation response recognized"
			);

			const invalidResponse = omitAtPath(rpcResponse, ["result", "docRef"]);
			strictEqual(
				AddDocumentJsonRpc2Response.isInstance(invalidResponse),
				false,
				"Check invalid response (no docRef)"
			);

			const responseWithoutResult = { jsonrpc: "2.0", id: "AddDocument" };
			strictEqual(
				AddDocumentJsonRpc2Response.isInstance(responseWithoutResult),
				false,
				"Add Document response without result field should not be valid"
			);

			const responseWithWrongDocRefType = replaceAtPath(rpcResponse, ["result", "docRef"], 123);
			strictEqual(
				AddDocumentJsonRpc2Response.isInstance(responseWithWrongDocRefType),
				false,
				"Add Document response with wrong docRef type should not be valid"
			);

			const responseWithError = {
				...rpcResponse,
				error: { code: -1, message: "Error", data: {} }
			};
			strictEqual(
				AddDocumentJsonRpc2Response.isInstance(responseWithError),
				false,
				"Add Document response should not be valid with both result and error fields"
			);
		});
	});

	suite("Copy Document Operation", () => {
		test("Request", () => {
			strictEqual(
				DocumentJsonRpc2Request.CopyJsonRpc2Request.isInstance(copyDocumentRequest),
				true
			);

			strictEqual(
				DocumentJsonRpc2Request.DeleteJsonRpc2Request.isInstance({
					...copyDocumentRequest,
					params: {}
				}),
				false,
				"Check with 'docRef' omitted"
			);
		});
	});

	suite("ModifyDocumentRequest::OK", () => {
		test("Request", () => {
			strictEqual(
				DocumentJsonRpc2Request.ModifyJsonRpc2Request.isInstance(modifyDocumentRequest),
				true
			);

			strictEqual(
				DocumentJsonRpc2Request.DeleteJsonRpc2Request.isInstance({
					...modifyDocumentRequest,
					params: {}
				}),
				false,
				"Check with 'docRef' omitted"
			);
		});

		test("Response", () => {
			const rpcResponse = {
				jsonrpc: "2.0",
				id: "ModifyDocument",
				result: {}
			};

			strictEqual(
				ModifyJsonRpc2Response.isInstance(rpcResponse),
				true,
				"Modify Document response was not recognized!"
			);

			const responseWithoutResult = { jsonrpc: "2.0", id: "ModifyDocument" };
			strictEqual(
				ModifyJsonRpc2Response.isInstance(responseWithoutResult),
				false,
				"Modify Document response without result was incorrectly recognized!"
			);

			const responseWithError = {
				...rpcResponse,
				error: { code: -1, message: "Error", data: {} }
			};
			strictEqual(
				ModifyJsonRpc2Response.isInstance(responseWithError),
				false,
				"Modify Document response should not be valid with both result and error fields!"
			);
		});
	});

	suite("Partial Modify Document Operation", () => {
		test("Request", () => {
			strictEqual(
				DocumentJsonRpc2Request.PartialModifyJsonRpc2Request.isInstance(
					partialModifyDocumentRequest
				),
				true
			);

			strictEqual(
				DocumentJsonRpc2Request.DeleteJsonRpc2Request.isInstance({
					...partialModifyDocumentRequest,
					params: {}
				}),
				false,
				"Check with 'docRef' omitted"
			);
		});

		test("shouldAcceptGroupObjectValueAndWildcardRepetitionsInPartialModifyRequest", () => {
			const groupObjectValue = {
				original_filename: "appended.pdf",
				internal_filename: "abc123"
			};

			const documentPart = {
				path: "/BusinessPartnerRoot/Attachment",
				value: groupObjectValue,
				repetitions: [1, 0]
			};

			strictEqual(
				DocumentJsonRpc2Request.DocumentPart.isInstance(documentPart),
				true,
				"DocumentPart with object value and wildcard repetitions should be recognized"
			);

			const request = {
				jsonrpc: "2.0",
				id: "PartialModifyGroup",
				method: "PARTIAL_MODIFY_DOCUMENT",
				params: {
					docRef: "BusinessPartner/1",
					documentPart: [documentPart],
					locale: "en"
				}
			};

			strictEqual(
				DocumentJsonRpc2Request.PartialModifyJsonRpc2Request.isInstance(request),
				true,
				"PartialModifyJsonRpc2Request with group object value and wildcard repetitions should be recognized"
			);
		});
	});

	suite("Delete Document Operation", () => {
		test("Request", () => {
			strictEqual(
				DocumentJsonRpc2Request.DeleteJsonRpc2Request.isInstance(deleteDocumentRequest),
				true
			);

			strictEqual(
				DocumentJsonRpc2Request.DeleteJsonRpc2Request.isInstance({
					...deleteDocumentRequest,
					params: {}
				}),
				false,
				"Check with 'docRef' omitted"
			);
		});
	});

	suite("Multi Delete Documents Operation", () => {
		test("Request", () => {
			strictEqual(
				DocumentJsonRpc2Request.MultiDeleteJsonRpc2Request.isInstance(multiDeleteDocumentRequest),
				true,
				"Check request"
			);

			strictEqual(
				DocumentJsonRpc2Request.MultiDeleteJsonRpc2Request.isInstance({
					...multiDeleteDocumentRequest,
					params: {}
				}),
				false,
				"Check with 'docRefs' omitted"
			);
		});
	});

	suite("Validate Document Operation", () => {
		test("Request", () => {
			strictEqual(
				DocumentJsonRpc2Request.ValidateJsonRpc2Request.isInstance(validateDocumentRequest),
				true
			);

			strictEqual(
				DocumentJsonRpc2Request.DeleteJsonRpc2Request.isInstance({
					...validateDocumentRequest,
					params: {}
				}),
				false,
				"Check with 'docRef' omitted"
			);
		});

		test("Response", () => {
			const validateDocumentResponse = {
				jsonrpc: "2.0",
				id: "ValidateDocument",
				result: {
					validationErrors: [
						{
							errorText: "Field name cannot be empty",
							errorCode: "VALIDATION_ERROR",
							messageType: "ERROR",
							rulePath: "/ContractRoot/ContractName",
							referencedFields: ["/ContractRoot/ContractName"],
							severityType: "ERROR"
						}
					]
				}
			};

			strictEqual(
				ValidateDocumentJsonRpc2Response.isInstance(validateDocumentResponse),
				true,
				"Check operation response recognized"
			);

			strictEqual(
				ValidateDocumentJsonRpc2Response.isInstance({ ...validateDocumentResponse, result: {} }),
				false,
				"Check invalid response (no validationErrors)"
			);

			const responseWithoutResult = { jsonrpc: "2.0", id: "ValidateDocument" };
			strictEqual(
				ValidateDocumentJsonRpc2Response.isInstance(responseWithoutResult),
				false,
				"Validate Document response without result field should not be valid"
			);

			const responseWithWrongValidationErrorsType = {
				...validateDocumentResponse,
				result: { validationErrors: "not an array" }
			};
			strictEqual(
				ValidateDocumentJsonRpc2Response.isInstance(responseWithWrongValidationErrorsType),
				false,
				"Validate Document response with wrong validationErrors type should not be valid"
			);

			const responseWithInvalidValidationError = {
				...validateDocumentResponse,
				result: {
					validationErrors: [
						{ errorText: "Missing fields" } // Missing required fields
					]
				}
			};
			strictEqual(
				ValidateDocumentJsonRpc2Response.isInstance(responseWithInvalidValidationError),
				false,
				"Validate Document response with invalid validation error should not be valid"
			);

			const responseWithError = {
				...validateDocumentResponse,
				error: { code: -1, message: "Error", data: {} }
			};
			strictEqual(
				ValidateDocumentJsonRpc2Response.isInstance(responseWithError),
				false,
				"Validate Document response should not be valid with both result and error fields"
			);
		});
	});

	suite("Get Document Operation", () => {
		test("Request", () => {
			strictEqual(
				DocumentJsonRpc2Request.GetDocumentJsonRpc2Request.isInstance(getDocumentRequest),
				true
			);

			strictEqual(
				DocumentJsonRpc2Request.GetDocumentJsonRpc2Request.isInstance({
					...multiDeleteDocumentRequest,
					params: {}
				}),
				false,
				"Check with 'docRef' omitted"
			);

			strictEqual(
				DocumentJsonRpc2Request.GetDocumentJsonRpc2Request.isInstance({
					...multiDeleteDocumentRequest,
					params: { docRef: null }
				}),
				false,
				"Check with 'docRef' null"
			);
		});

		test("Response", () => {
			const getDocumentResponse = {
				jsonrpc: "2.0",
				id: "GetDocument",
				result: {
					docRef: "Contract/999",
					documentModelName: "Contract",
					document: {
						name0: "xyz"
					}
				}
			};

			strictEqual(
				GetDocumentJsonRpc2Response.isInstance(getDocumentResponse),
				true,
				"Check operation response recognized"
			);

			const responseWithoutDocument = omitAtPath(getDocumentResponse, ["result", "document"]);
			strictEqual(
				GetDocumentJsonRpc2Response.isInstance(responseWithoutDocument),
				false,
				"Check invalid response (no document)"
			);

			const responseWithoutDocRef = omitAtPath(getDocumentResponse, ["result", "docRef"]);
			strictEqual(
				GetDocumentJsonRpc2Response.isInstance(responseWithoutDocRef),
				false,
				"Check invalid response (no docRef)"
			);

			const responseWithoutDocumentModelName = omitAtPath(getDocumentResponse, [
				"result",
				"documentModelName"
			]);
			strictEqual(
				GetDocumentJsonRpc2Response.isInstance(responseWithoutDocumentModelName),
				false,
				"Check invalid response (no documentModelName)"
			);

			const responseWithoutResult = { jsonrpc: "2.0", id: "GetDocument" };
			strictEqual(
				GetDocumentJsonRpc2Response.isInstance(responseWithoutResult),
				false,
				"Get Document response without result field should not be valid"
			);

			const responseWithWrongDocRefType = replaceAtPath(
				getDocumentResponse,
				["result", "docRef"],
				123
			);
			strictEqual(
				GetDocumentJsonRpc2Response.isInstance(responseWithWrongDocRefType),
				false,
				"Get Document response with wrong docRef type should not be valid"
			);

			const responseWithWrongDocumentModelNameType = replaceAtPath(
				getDocumentResponse,
				["result", "documentModelName"],
				123
			);
			strictEqual(
				GetDocumentJsonRpc2Response.isInstance(responseWithWrongDocumentModelNameType),
				false,
				"Get Document response with wrong documentModelName type should not be valid"
			);

			const responseWithWrongDocumentType = replaceAtPath(
				getDocumentResponse,
				["result", "document"],
				"not an object"
			);
			strictEqual(
				GetDocumentJsonRpc2Response.isInstance(responseWithWrongDocumentType),
				false,
				"Get Document response with wrong document type should not be valid"
			);

			const responseWithError = {
				...getDocumentResponse,
				error: { code: -1, message: "Error", data: {} }
			};
			strictEqual(
				GetDocumentJsonRpc2Response.isInstance(responseWithError),
				false,
				"Get Document response should not be valid with both result and error fields"
			);
		});
	});

	suite("Load Attachment URL Operation", () => {
		test("Request", () => {
			const requestObject = {
				jsonrpc: "2.0",
				id: "LoadAttachmentUrl",
				method: "LOAD_ATTACHMENT_URL",
				params: {
					attachmentId: "Attachment1",
					docRef: "Contract/1"
				}
			};

			strictEqual(
				JsonRpc2Request.isInstance(requestObject),
				true,
				"Document operation not recognized"
			);
		});

		test("Response", () => {
			const responseObject = {
				jsonrpc: "2.0",
				id: "LoadAttachmentUrl",
				result: {
					location: "/attachment/download/attachmentId1?Contract/999"
				}
			};

			strictEqual(
				LoadAttachmentUrlJsonRpc2.Response.isInstance(responseObject),
				true,
				"Check operation response recognized"
			);

			const responseWithEmptyResult = replaceAtPath(responseObject, ["result"], {});
			strictEqual(
				LoadAttachmentUrlJsonRpc2.Response.isInstance(responseWithEmptyResult),
				false,
				"Check invalid response (no location)"
			);
		});
	});
});
