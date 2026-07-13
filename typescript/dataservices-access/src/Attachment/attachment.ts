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
import type { RestRequestPayload } from "@com.mgmtp.a12.utils/utils-connector";

import {
	isArray,
	isNull,
	isNumber,
	isObject,
	isOptionalFieldOfType,
	isString
} from "../common/TypeGuardUtils.js";
import { JsonRpc2Response, type JsonRpc2Request } from "../json-rpc/index.js";

export interface Attachment {
	readonly internal_filename?: string | null;
	readonly original_filename?: string | null;
	readonly mime_type?: string | null;
	readonly category?: string | null;
	readonly description?: string | null;
	readonly attachment_id?: string | null;
	/** Most likely base64 encoded image. */
	readonly content?: string | null;
	readonly size?: number | null;
}

export namespace Attachment {
	export function isInstance(obj: unknown): obj is Attachment {
		if (!isObject(obj)) {
			return false;
		}

		const hasAttachmentId = "attachment_id" in obj && isString(obj.attachment_id);
		const hasContent = "content" in obj && isString(obj.content);

		if ((!hasAttachmentId && !hasContent) || (hasAttachmentId && hasContent)) {
			return false;
		}

		return (
			isOptionalFieldOfType(obj, "internal_filename", value => isNull(value) || isString(value)) &&
			isOptionalFieldOfType(obj, "original_filename", value => isNull(value) || isString(value)) &&
			isOptionalFieldOfType(obj, "mime_type", value => isNull(value) || isString(value)) &&
			isOptionalFieldOfType(obj, "category", value => isNull(value) || isString(value)) &&
			isOptionalFieldOfType(obj, "description", value => isNull(value) || isString(value)) &&
			isOptionalFieldOfType(obj, "size", value => isNull(value) || isNumber(value))
		);
	}
}

export interface ThumbnailUrl {
	readonly smallThumbnailUrl?: string;
	readonly bigThumbnailUrl?: string;
}

export namespace ThumbnailUrl {
	export function isInstance(obj: unknown): obj is ThumbnailUrl {
		return (
			isObject(obj) &&
			isOptionalFieldOfType(obj, "smallThumbnailUrl", isString) &&
			isOptionalFieldOfType(obj, "bigThumbnailUrl", isString)
		);
	}
}

export interface AttachmentHeader {
	readonly attachmentId: string;
	readonly filename?: string;
	readonly smallThumbnailUrl?: string;
	readonly bigThumbnailUrl?: string;
	readonly mimeType?: string;
	readonly size?: number;
	readonly annotations?: AttachmentAnnotation[];
}

export namespace AttachmentHeader {
	export function isInstance(obj: unknown): obj is AttachmentHeader {
		return (
			isObject(obj) &&
			"attachmentId" in obj &&
			isString(obj.attachmentId) &&
			isOptionalFieldOfType(obj, "filename", isString) &&
			isOptionalFieldOfType(obj, "smallThumbnailUrl", isString) &&
			isOptionalFieldOfType(obj, "bigThumbnailUrl", isString) &&
			isOptionalFieldOfType(obj, "mimeType", isString) &&
			isOptionalFieldOfType(obj, "size", isNumber) &&
			isOptionalFieldOfType(obj, "annotations", value =>
				isArray(value, AttachmentAnnotation.isInstance)
			)
		);
	}
}

export interface AttachmentAnnotation {
	readonly name: string;
	readonly value: string;
}

export namespace AttachmentAnnotation {
	export function isInstance(obj: unknown): obj is AttachmentAnnotation {
		return (
			isObject(obj) && "name" in obj && isString(obj.name) && "value" in obj && isString(obj.value)
		);
	}
}

export namespace AttachmentUpload {
	export interface Request {
		readonly attachment: File;
	}

	export interface Response {
		readonly attachmentId: string;
		readonly uploadedFile: string;
		readonly thumbnailSmall: string;
		readonly thumbnailBig: string;
	}

	export namespace Request {
		export function build(request: Request): RestRequestPayload {
			const formData = new FormData();
			formData.append("file", request.attachment, request.attachment.name);
			return {
				method: "POST",
				relativeUrl: "/attachment/upload",
				body: formData,
				customHeaders: [["Accept", "application/json"]]
			};
		}
	}

	export namespace Response {
		export function isInstance(obj: unknown): obj is Response {
			return (
				isObject(obj) &&
				"attachmentId" in obj &&
				isString(obj.attachmentId) &&
				"uploadedFile" in obj &&
				isString(obj.uploadedFile) &&
				"thumbnailSmall" in obj &&
				isString(obj.thumbnailSmall) &&
				"thumbnailBig" in obj &&
				isString(obj.thumbnailBig)
			);
		}
	}
}

export namespace AttachmentUploadV2 {
	export interface Request {
		readonly fileName: string;
		readonly documentModelName: string;
		readonly pathToField: string;
		readonly content: ArrayBuffer;
	}

	export namespace Request {
		/**
		 * Constructs the REST request payload for uploading an attachment.
		 *
		 * This function creates a POST request where the attachment metadata
		 * (fileName, documentModelName, and pathToField) is passed as URL query parameters.
		 * Note that all parameter values are automatically URL-encoded using
		 * `encodeURIComponent()` to ensure path validity and safety.
		 *
		 * @param request The source object containing the attachment's metadata and content.
		 * @returns A `RestRequestPayload` object configured for the attachment upload via POST.
		 */
		export function build(request: Request): RestRequestPayload {
			return {
				method: "POST",
				relativeUrl: `/v2/attachment?filename=${encodeURIComponent(request.fileName)}&documentModelName=${encodeURIComponent(request.documentModelName)}&pathToField=${encodeURIComponent(request.pathToField)}`,
				body: request.content,
				customHeaders: [["Accept", "application/json"]],
				needUrlEncoded: false
			};
		}
	}
}

export namespace AttachmentDownloadRequest {
	export interface Request {
		readonly docRef: string;
		readonly attachmentId: string;
	}

	/**
	 * Constructs the REST request payload for downloading a specific attachment.
	 *
	 * @param request The source object containing the attachment's ID and document reference.
	 * @returns A `RestRequestPayload` object configured for the attachment download via GET.
	 */
	export function build(request: Request): RestRequestPayload {
		const url = "/attachment/download/" + request.attachmentId + "?docRef=" + request.docRef;
		return {
			method: "GET",
			relativeUrl: url
		};
	}
}

export namespace AttachmentThumbnailDownloadRequest {
	export interface AttachmentThumbnailDownloadRequest {
		readonly attachmentId: string;
		readonly type: "thumb_big" | "thumb_small";
	}

	/**
	 * Constructs the REST request payload for downloading an attachment thumbnail.
	 *
	 * @param request The source object containing the attachment's ID and desired thumbnail type.
	 * @returns A `RestRequestPayload` object configured for the thumbnail download via GET.
	 */
	export function build(request: AttachmentThumbnailDownloadRequest): RestRequestPayload {
		const url = "/attachment/thumbnail/" + request.attachmentId + "/" + request.type;
		return {
			method: "GET",
			relativeUrl: url
		};
	}
}

export namespace LoadAttachmentHeaderJsonRpc2 {
	export interface Request extends JsonRpc2Request {
		readonly method: "LOAD_ATTACHMENT_HEADER";
		readonly params: {
			attachmentId: string;
			docRef: string;
		};
	}

	export interface Response extends JsonRpc2Response {
		readonly result: AttachmentHeader;
	}

	export namespace Response {
		export function isInstance(obj: unknown): obj is Response {
			return JsonRpc2Response.ok.isInstance(obj) && AttachmentHeader.isInstance(obj.result);
		}
	}
}

export namespace LoadThumbnailUrlJsonRpc2 {
	export interface Request extends JsonRpc2Request {
		readonly method: "LOAD_THUMBNAIL_URL";
		readonly params: {
			attachmentId: string;
		};
	}

	export interface Response extends JsonRpc2Response {
		readonly result: ThumbnailUrl;
	}

	export namespace Response {
		export function isInstance(obj: unknown): obj is Response {
			return JsonRpc2Response.ok.isInstance(obj) && ThumbnailUrl.isInstance(obj.result);
		}
	}
}

export namespace LoadThumbnailUrlsJsonRpc2 {
	export interface Request extends JsonRpc2Request {
		readonly method: "LOAD_THUMBNAIL_URLS_INTERNAL";
	}

	export interface Response extends JsonRpc2Response {
		readonly result: Record<string, Record<string, ThumbnailUrl>>;
	}

	export namespace Response {
		export function isInstance(obj: unknown): obj is Response {
			return (
				JsonRpc2Response.ok.isInstance(obj) &&
				isObject(obj.result) &&
				Object.keys(obj.result).every(
					key =>
						isObject(obj.result[key]) &&
						Object.keys(obj.result[key]).every(innerKey =>
							ThumbnailUrl.isInstance(obj.result[key][innerKey])
						)
				)
			);
		}
	}
}

export namespace LoadAttachmentUrlJsonRpc2 {
	export interface Request extends JsonRpc2Request {
		readonly method: "LOAD_ATTACHMENT_URL";
		readonly params: {
			readonly attachmentId: string;
			readonly docRef: string;
		};
	}

	export interface Response extends JsonRpc2Response {
		readonly result: {
			readonly location: string;
		};
	}

	export namespace Response {
		export function isInstance(obj: unknown): obj is Response {
			return (
				JsonRpc2Response.ok.isInstance(obj) &&
				"location" in obj.result &&
				isString(obj.result.location)
			);
		}
	}
}
