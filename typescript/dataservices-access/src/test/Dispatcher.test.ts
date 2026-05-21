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
import { deepEqual, rejects } from "node:assert/strict";
import { mock, type Mock } from "node:test";

import { expectTypeOf } from "expect-type";

import { ConnectorLocator, type ServerConnector } from "@com.mgmtp.a12.utils/utils-connector";

import type { LoadAttachmentUrlJsonRpc2 } from "../Attachment/attachment.js";
import { Dispatcher } from "../dispatch/index.js";
import type { AddDocumentJsonRpc2Response, DocumentJsonRpc2Request } from "../Document/index.js";
import type { JsonRpc2Response, JsonRpc2ResponseError } from "../json-rpc/index.js";
import type { RelationshipJsonRpc2response } from "../Relationship/index.js";
import { type RelationshipJsonRpc2request } from "../Relationship/index.js";

/* eslint-disable @typescript-eslint/no-unused-vars */
suite("Dispatcher", () => {
	let fetchDataMock: Mock<ServerConnector["fetchData"]>;

	suiteSetup(() => {
		fetchDataMock = mock.fn<ServerConnector["fetchData"]>(async () => Response.json([]));
		ConnectorLocator.createInstance({
			fetchData: fetchDataMock
		});
	});

	suite("rest", () => {
		const expected = { myType: true };

		function isMyTypeGuard(value: unknown): value is typeof expected {
			return typeof value === "object" && value !== null && "myType" in value;
		}

		test("should return data when responseChecker passes", async () => {
			fetchDataMock.mock.mockImplementationOnce(async () => Response.json(expected));

			const actual = await Dispatcher.rest({ method: "", relativeUrl: "" }, isMyTypeGuard);

			deepEqual(actual, expected);
		});

		test("should throw when the responseChecker fails", async () => {
			fetchDataMock.mock.mockImplementationOnce(async () => Response.json({ other: true }));
			await rejects(() => Dispatcher.rest({ method: "", relativeUrl: "" }, isMyTypeGuard));
		});

		test("should throw when the response is not ok", async () => {
			fetchDataMock.mock.mockImplementationOnce(async () => Response.error());
			await rejects(() => Dispatcher.rest({ method: "", relativeUrl: "" }, isMyTypeGuard));
		});
	});

	suite("rpc", () => {
		function mockRequest(id: number): LoadAttachmentUrlJsonRpc2.Request {
			return {
				method: "LOAD_ATTACHMENT_URL",
				params: { attachmentId: "1", docRef: "1" },
				jsonrpc: "2.0",
				id
			};
		}

		function mockResponse(id: number): LoadAttachmentUrlJsonRpc2.Response {
			return { jsonrpc: "2.0", id, result: { location: `url${id}` } };
		}

		test("returns data for successful requests", async () => {
			const expected = [mockResponse(1), mockResponse(2)] satisfies JsonRpc2Response[];
			fetchDataMock.mock.mockImplementationOnce(async () => Response.json(expected));

			const actual = await Dispatcher.rpc("", [mockRequest(1), mockRequest(2)]);

			deepEqual(actual, expected);
		});

		test("returns undefined for undefined requests", async () => {
			const expected = mockResponse(1);
			fetchDataMock.mock.mockImplementationOnce(async () =>
				Response.json([expected] satisfies JsonRpc2Response[])
			);

			const actual = await Dispatcher.rpc("", [undefined, mockRequest(1), undefined]);

			deepEqual(actual, [undefined, expected, undefined]);
		});

		test("throws rpc errors if the response includes them", async () => {
			fetchDataMock.mock.mockImplementationOnce(async () =>
				Response.json([
					{ jsonrpc: "2.0", id: 1, error: { code: 123, message: "err", data: {} } }
				] satisfies JsonRpc2Response[])
			);
			await rejects(
				() => Dispatcher.rpc("", [mockRequest(1)]),
				(e: JsonRpc2ResponseError[]) => e.at(0)?.error.code === 123
			);
		});

		test("throws rpc errors for single error response", async () => {
			fetchDataMock.mock.mockImplementationOnce(async () =>
				Response.json({ jsonrpc: "2.0", id: 1, error: { code: 123, message: "err", data: {} } })
			);
			await rejects(
				() => Dispatcher.rpc("", [mockRequest(1)]),
				(e: JsonRpc2ResponseError) => e.error.code === 123
			);
		});

		test("throws if a response is missing for a request", async () => {
			fetchDataMock.mock.mockImplementationOnce(async () =>
				Response.json([mockResponse(1)] satisfies JsonRpc2Response[])
			);
			await rejects(
				() => Dispatcher.rpc("", [mockRequest(1), mockRequest(2)]),
				/No response found for request 2/
			);
		});

		test("throws if a typeguard fails for a response", async () => {
			fetchDataMock.mock.mockImplementationOnce(async () =>
				Response.json([
					mockResponse(1),
					{ jsonrpc: "2.0", id: 2, result: { wrong: true } }
				] satisfies JsonRpc2Response[])
			);
			await rejects(
				() => Dispatcher.rpc("", [mockRequest(1), mockRequest(2)]),
				/Expect a response of type/
			);
		});
	});

	suite("rpc (type-only)", () => {
		type ReqA = DocumentJsonRpc2Request.AddJsonRpc2Request;
		type ReqB = RelationshipJsonRpc2request.AddLinkJsonRpc2request;
		type ReqC = LoadAttachmentUrlJsonRpc2.Request;
		type RespA = AddDocumentJsonRpc2Response;
		type RespB = RelationshipJsonRpc2response.AddLinkJsonRpc2Response;
		type RespC = LoadAttachmentUrlJsonRpc2.Response;

		function asType<T>(): T {
			return undefined as T;
		}

		test("should return the correct response for a single request", async () => {
			const request = asType<ReqA>();

			const responses = await Dispatcher.rpc("", [request]);

			expectTypeOf<typeof responses>().toEqualTypeOf<[RespA]>();
		});

		test("should return the correct response or undefined for a single, optional request", async () => {
			const request = asType<ReqA | undefined>();

			const responses = await Dispatcher.rpc("", [request]);

			expectTypeOf<typeof responses>().toEqualTypeOf<[RespA | undefined]>();
		});

		test("should return the correct union of responses for a single union of requests", async () => {
			const unionReq = asType<ReqA | ReqB>();

			const responses = await Dispatcher.rpc("", [unionReq]);

			expectTypeOf<typeof responses>().toEqualTypeOf<[RespA | RespB]>();
		});

		test("should return the correct responses for multiple requests in order", async () => {
			const requestA = asType<ReqA>();
			const requestB = asType<ReqB>();
			const requestC = asType<ReqC>();

			const responses = await Dispatcher.rpc("", [requestA, requestB, requestC]);

			expectTypeOf<typeof responses>().toEqualTypeOf<[RespA, RespB, RespC]>();
		});

		test("should return the correct responses or undefined for multiple, optional requests in order", async () => {
			const requestA = asType<ReqA | undefined>();
			const requestB = asType<ReqB | undefined>();
			const requestC = asType<ReqC | undefined>();

			const responses = await Dispatcher.rpc("", [requestA, requestB, requestC]);

			expectTypeOf<typeof responses>().toEqualTypeOf<
				[RespA | undefined, RespB | undefined, RespC | undefined]
			>();
		});

		test("should return the correct union responses for multiple unions of requests in order", async () => {
			const unionReqA = asType<ReqA | ReqB>();
			const unionReqB = asType<ReqB | ReqC>();
			const unionReqC = asType<ReqC | ReqA>();

			const responses = await Dispatcher.rpc("", [unionReqA, unionReqB, unionReqC]);

			expectTypeOf<typeof responses>().toEqualTypeOf<
				[RespA | RespB, RespB | RespC, RespC | RespA]
			>();
		});
	});
});
