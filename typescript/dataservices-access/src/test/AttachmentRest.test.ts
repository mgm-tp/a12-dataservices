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
import { notStrictEqual, strictEqual } from "node:assert/strict";

import {
	AttachmentDownloadRequest,
	AttachmentThumbnailDownloadRequest,
	AttachmentUpload,
	AttachmentUploadV2
} from "../Attachment/attachment.js";

import { loadResource } from "./utils/ObjectUtils.js";

suite("Attachment Rest Tests", () => {
	suite("Attachment Upload", () => {
		test("Attachment Upload Request Builder", () => {
			const file = new File(["dummy content"], "test.txt", { type: "text/plain" });
			const requestPayload = AttachmentUpload.Request.build({ attachment: file });

			strictEqual(requestPayload.method, "POST");
			strictEqual(requestPayload.relativeUrl, "/attachment/upload");
			strictEqual(requestPayload.body instanceof FormData, true);
			strictEqual(requestPayload.customHeaders?.[0][0], "Accept");
			strictEqual(requestPayload.customHeaders?.[0][1], "application/json");
		});

		test("Attachment Upload Response", () => {
			const attachmentUploadResponse = loadResource(
				"./src/test/resources/attachments/attachment_upload_response.json"
			);
			notStrictEqual(attachmentUploadResponse, null);
			strictEqual(AttachmentUpload.Response.isInstance(attachmentUploadResponse), true);
			strictEqual(attachmentUploadResponse.attachmentId, "c5a1e746-d799-40b1-bcdc-92de6c93f1a1");
			strictEqual(
				attachmentUploadResponse.uploadedFile,
				"c5a1e746-d799-40b1-bcdc-92de6c93f1a1.jpg"
			);
			strictEqual(
				attachmentUploadResponse.thumbnailSmall,
				"c5a1e746-d799-40b1-bcdc-92de6c93f1a1_thumbnail_small.jpg"
			);
			strictEqual(
				attachmentUploadResponse.thumbnailBig,
				"c5a1e746-d799-40b1-bcdc-92de6c93f1a1_thumbnail_big.jpg"
			);
		});
	});

	suite("Attachment Upload V2", () => {
		test("AttachmentUploadV2::Request::build", () => {
			const requestPayload = AttachmentUploadV2.Request.build({
				fileName: "test.txt",
				documentModelName: "TestModel",
				pathToField: "/field/path",
				content: new ArrayBuffer(8)
			});

			strictEqual(requestPayload.method, "POST");
			strictEqual(
				requestPayload.relativeUrl,
				"/v2/attachment?filename=test.txt&documentModelName=TestModel&pathToField=%2Ffield%2Fpath"
			);
			strictEqual(requestPayload.body instanceof ArrayBuffer, true);
			strictEqual(requestPayload.customHeaders?.[0][0], "Accept");
			strictEqual(requestPayload.customHeaders?.[0][1], "application/json");
		});
	});

	suite("Attachment Download Request", () => {
		test("Attachment DownloadRequest Builder", () => {
			const requestPayload = AttachmentDownloadRequest.build({
				docRef: "doc123",
				attachmentId: "attach456"
			});

			strictEqual(requestPayload.method, "GET");
			strictEqual(requestPayload.relativeUrl, "/attachment/download/attach456?docRef=doc123");
		});
	});

	suite("Attachment Thumbnail Download Request", () => {
		test("Attachment Thumbnail Download Request Builder", () => {
			const requestPayload = AttachmentThumbnailDownloadRequest.build({
				attachmentId: "attach789",
				type: "thumb_small"
			});

			strictEqual(requestPayload.method, "GET");
			strictEqual(requestPayload.relativeUrl, "/attachment/thumbnail/attach789/thumb_small");
		});
	});
});
