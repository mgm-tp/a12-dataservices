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

import { Attachment, AttachmentHeader } from "../Attachment/index.js";
import { AttachmentAnnotation, ThumbnailUrl } from "../Attachment/index.js";

import { replaceAtPath } from "./utils/ObjectUtils.js";

suite("Attachments suite", () => {
	suite("Attachment", () => {
		const attachmentTemplate = {
			internal_filename: "",
			original_filename: "",
			mime_type: "",
			category: "",
			description: "",
			attachment_id: "dummy id",
			content: "dummy content",
			size: 1
		};

		test("content and attachment_id are missing", () => {
			const { content, attachment_id, ...attachment } = attachmentTemplate;

			strictEqual(Attachment.isInstance(attachment), false);
		});

		test("content is provided and attachment_id is missing", () => {
			const { attachment_id, ...attachment } = attachmentTemplate;

			strictEqual(Attachment.isInstance(attachment), true);
		});

		test("content is missing and attachment_id is provided", () => {
			const { content, ...attachment } = attachmentTemplate;

			strictEqual(Attachment.isInstance(attachment), true);
		});

		test("content and attachment_id are provided", () => {
			strictEqual(Attachment.isInstance(attachmentTemplate), false);
		});

		test("content is null and attachment_id is provided", () => {
			const attachment = { ...attachmentTemplate, content: null };

			strictEqual(Attachment.isInstance(attachment), true);
		});

		test("content is provided and attachment_id is null", () => {
			const attachment = { ...attachmentTemplate, attachment_id: null };

			strictEqual(Attachment.isInstance(attachment), true);
		});
	});

	suite("Attachment header", () => {
		const attachmentHeaderBase = {
			attachmentId: "e4bf88aa-e184-4fd7-be5e-ab74cbde4bcb",
			filename: "Attachment",
			mimeType: "image/webp",
			smallThumbnailUrl: "http://localhost:8080/cs/download/325e2c61-a135-43f2-b3cc-8d98855b3d7c",
			bigThumbnailUrl: "http://localhost:8080/cs/download/4e3d334e-ccf0-46fe-85a7-c1214c06d1be",
			size: 48288,
			annotations: [
				{
					name: "foo",
					value: "bar"
				}
			]
		};

		test("Attachment Header", () => {
			strictEqual(AttachmentHeader.isInstance(attachmentHeaderBase), true);
		});

		test("Attachment Header - Core properties", () => {
			const attachmentHeader = {
				attachmentId: attachmentHeaderBase.attachmentId
			};

			strictEqual(AttachmentHeader.isInstance(attachmentHeader), true);
		});

		test("Attachment Header - AttachmentId Types", () => {
			strictEqual(
				AttachmentHeader.isInstance({ ...attachmentHeaderBase, attachmentId: null }),
				false,
				"Check with null type"
			);

			strictEqual(
				AttachmentHeader.isInstance({ ...attachmentHeaderBase, attachmentId: 123456 }),
				false,
				"Check with number type"
			);
		});

		test("Attachment Header - Missing AttachmentId", () => {
			const { attachmentId, ...attachment } = attachmentHeaderBase;

			strictEqual(AttachmentHeader.isInstance(attachment), false);
		});

		test("Attachment Header - Annotations", () => {
			const annotationWithoutName = replaceAtPath(attachmentHeaderBase, ["annotations", 1], {
				value: "foo"
			});
			strictEqual(
				AttachmentHeader.isInstance(annotationWithoutName),
				false,
				"Check with annotation without value"
			);

			const annotationWithoutValue = replaceAtPath(attachmentHeaderBase, ["annotations", 1], {
				name: "foo"
			});
			strictEqual(
				AttachmentHeader.isInstance(annotationWithoutValue),
				false,
				"Check with annotation without value"
			);
		});

		test("Attachment Header - Missing filename", () => {
			const { filename, ...attachment } = attachmentHeaderBase;
			strictEqual(
				AttachmentHeader.isInstance(attachment),
				true,
				"Check with missing filename (filename is optional and can be null)"
			);

			const attachmentWithNullFilename = { ...attachmentHeaderBase, filename: null };
			strictEqual(
				AttachmentHeader.isInstance(attachmentWithNullFilename),
				false,
				"Check with null filename (filename is optional and can be null)"
			);

			const attachmentWithNumberFilename = { ...attachmentHeaderBase, filename: 123456 };
			strictEqual(
				AttachmentHeader.isInstance(attachmentWithNumberFilename),
				false,
				"Check with number type filename (filename should be string or null)"
			);
		});
	});

	suite("Attachment annotation", () => {
		const attachmentAnnotationBase = {
			name: "foo",
			value: "bar"
		};

		test("Attachment Annotation", () => {
			strictEqual(AttachmentAnnotation.isInstance(attachmentAnnotationBase), true);
		});

		test("Attachment Annotation - Missing Name", () => {
			const { name, ...attachmentAnnotation } = attachmentAnnotationBase;
			strictEqual(AttachmentAnnotation.isInstance(attachmentAnnotation), false);
		});

		test("Attachment Annotation - Missing Value", () => {
			const { value, ...attachmentAnnotation } = attachmentAnnotationBase;
			strictEqual(AttachmentAnnotation.isInstance(attachmentAnnotation), false);
		});
	});

	suite("Thumbnail URL", () => {
		const thumbnailUrlBase = {
			smallThumbnailUrl: "http://localhost:8080/cs/download/small-thumbnail",
			bigThumbnailUrl: "http://localhost:8080/cs/download/big-thumbnail"
		};

		test("Thumbnail URL", () => {
			strictEqual(ThumbnailUrl.isInstance(thumbnailUrlBase), true);
		});

		test("Thumbnail URL - Small Thumbnail URL only", () => {
			const thumbnailUrl = {
				smallThumbnailUrl: thumbnailUrlBase.smallThumbnailUrl
			};

			strictEqual(ThumbnailUrl.isInstance(thumbnailUrl), true);
		});

		test("Thumbnail URL - Big Thumbnail URL only", () => {
			const thumbnailUrl = {
				bigThumbnailUrl: thumbnailUrlBase.bigThumbnailUrl
			};

			strictEqual(ThumbnailUrl.isInstance(thumbnailUrl), true);
		});
	});
});
