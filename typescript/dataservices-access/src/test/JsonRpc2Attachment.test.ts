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
	LoadThumbnailUrlsJsonRpc2,
	LoadAttachmentHeaderJsonRpc2,
	LoadThumbnailUrlJsonRpc2
} from "../Attachment/attachment.js";
import { JsonRpc2Request } from "../json-rpc/index.js";

import { loadResource } from "./utils/ObjectUtils.js";
import {
	loadAttachmentHeaderRequest,
	loadAttachmentUrlRequest,
	loadThumbnailUrlRequest,
	loadThumbnailUrlsRequest
} from "./resources/jsonrpc/request/attachment_operation_requests.js";

suite("JSON-RPC Attachment Tests", () => {
	suite("Load Attachment Header", () => {
		test("Request", () => {
			strictEqual(JsonRpc2Request.isInstance(loadAttachmentHeaderRequest), true);
		});

		test("Response", () => {
			const rpcResponse = loadResource(
				"./src/test/resources/jsonrpc/response/jsonrpc_load_attachment_header_response.json"
			);
			strictEqual(LoadAttachmentHeaderJsonRpc2.Response.isInstance(rpcResponse), true);

			strictEqual(rpcResponse.id, "loadAttachmentHeader");
			strictEqual(rpcResponse.result.mimeType, "image/jpeg");
			strictEqual(rpcResponse.result.filename, "AttachmentImage.jpg");
			strictEqual(rpcResponse.result.attachmentId, "3a5ecc46-a6e7-46d7-9be2-07ca5420dcec");
		});
	});

	suite("Load Attachment URL", () => {
		test("Request", () => {
			strictEqual(JsonRpc2Request.isInstance(loadAttachmentUrlRequest), true);
		});
	});

	suite("Load Thumbnail URL", () => {
		test("Request", () => {
			strictEqual(JsonRpc2Request.isInstance(loadThumbnailUrlRequest), true);
		});

		test("Response", () => {
			const rpcResponse = loadResource(
				"./src/test/resources/jsonrpc/response/jsonrpc_load_thumbnail_url_response.json"
			);
			strictEqual(
				LoadThumbnailUrlJsonRpc2.Response.isInstance(rpcResponse),
				true,
				"LoadThumbnailUrl response was not recognized"
			);

			const getDocumentResult = rpcResponse as LoadThumbnailUrlJsonRpc2.Response;
			strictEqual(getDocumentResult.id, "loadThumbnailUrl");

			const result = getDocumentResult.result;
			strictEqual(result.smallThumbnailUrl, "smallImageUrl");
			strictEqual(result.bigThumbnailUrl, "bigImageUrl");
		});
	});

	suite("Load Thumbnail URLs", () => {
		test("Request", () => {
			strictEqual(JsonRpc2Request.isInstance(loadThumbnailUrlsRequest), true);
		});

		test("Response", () => {
			const rpcResponse = loadResource(
				"./src/test/resources/jsonrpc/response/jsonrpc_load_thumbnail_urls_response.json"
			);
			strictEqual(LoadThumbnailUrlsJsonRpc2.Response.isInstance(rpcResponse), true);

			const loadThumbnailUrlsResult = rpcResponse;
			strictEqual(loadThumbnailUrlsResult.id, "LoadThumbnailUrlsInternal");
			const result = loadThumbnailUrlsResult.result;
			strictEqual(
				result["BusinessPartner/1"]["c78b0238-7f9a-4998-90e2-f93f73086006"].smallThumbnailUrl,
				"http://localhost:8080/cs/download/93ebef0f-b034-4547-afb9-2ab51ab314ba"
			);
			strictEqual(
				result["BusinessPartner/1"]["c78b0238-7f9a-4998-90e2-f93f73086006"].bigThumbnailUrl,
				"http://localhost:8080/cs/download/36df7c80-b8ba-4a01-b0c2-4cad674cd221"
			);
		});
	});
});
