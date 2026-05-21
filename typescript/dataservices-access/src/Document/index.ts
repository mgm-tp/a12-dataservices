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
/** @module Document/api */
import { isObject } from "../common/TypeGuardUtils.js";
import { JsonRpc2Request, JsonRpc2Response } from "../json-rpc/index.js";

export interface DocumentSpec {
	readonly docRef: string;
	readonly documentModelName: string;
	readonly document: object;
}

export namespace DocumentSpec {
	export function isInstance(obj: DocumentSpec | object): obj is DocumentSpec {
		return (
			"docRef" in obj &&
			"documentModelName" in obj &&
			"document" in obj &&
			typeof obj.document === "object"
		);
	}
}

export type DocumentJsonRpc2Request =
	| DocumentJsonRpc2Request.AddJsonRpc2Request
	| DocumentJsonRpc2Request.CopyJsonRpc2Request
	| DocumentJsonRpc2Request.ModifyJsonRpc2Request
	| DocumentJsonRpc2Request.PartialModifyJsonRpc2Request
	| DocumentJsonRpc2Request.DeleteJsonRpc2Request
	| DocumentJsonRpc2Request.MultiDeleteJsonRpc2Request
	| DocumentJsonRpc2Request.ValidateJsonRpc2Request
	| DocumentJsonRpc2Request.GetDocumentJsonRpc2Request;

export namespace DocumentJsonRpc2Request {
	export function isInstance(obj: unknown): obj is DocumentJsonRpc2Request {
		const document_methods = [
			"ADD_DOCUMENT",
			"COPY_DOCUMENT",
			"MODIFY_DOCUMENT",
			"PARTIAL_MODIFY_DOCUMENT",
			"DELETE_DOCUMENT",
			"MULTI_DELETE_DOCUMENTS",
			"VALIDATE_DOCUMENT",
			"GET_DOCUMENT"
		];
		return JsonRpc2Request.isInstance(obj) && document_methods.some(op => op === obj.method);
	}

	export interface AddJsonRpc2Request extends JsonRpc2Request {
		readonly method: "ADD_DOCUMENT";
		readonly params: {
			document: object;
			documentModelName: string;
			locale: string;
		};
	}

	export namespace AddJsonRpc2Request {
		export function isInstance(obj: unknown): obj is AddJsonRpc2Request {
			return (
				DocumentJsonRpc2Request.isInstance(obj) &&
				"document" in obj.params &&
				typeof obj.params.document === "object" &&
				"documentModelName" in obj.params &&
				typeof obj.params.documentModelName === "string" &&
				"locale" in obj.params &&
				typeof obj.params.locale === "string"
			);
		}
	}

	export interface CopyJsonRpc2Request extends JsonRpc2Request {
		readonly method: "COPY_DOCUMENT";
		readonly params: {
			docRef: string;
			locale: string;
		};
	}

	export namespace CopyJsonRpc2Request {
		export function isInstance(obj: unknown): obj is CopyJsonRpc2Request {
			return (
				DocumentJsonRpc2Request.isInstance(obj) &&
				"docRef" in obj.params &&
				typeof obj.params.docRef === "string" &&
				"locale" in obj.params &&
				typeof obj.params.locale === "string"
			);
		}
	}

	export interface ModifyJsonRpc2Request extends JsonRpc2Request {
		readonly method: "MODIFY_DOCUMENT";
		readonly params: {
			docRef: string;
			document: object;
			locale: string;
		};
	}

	export namespace ModifyJsonRpc2Request {
		export function isInstance(obj: unknown): obj is ModifyJsonRpc2Request {
			return (
				DocumentJsonRpc2Request.isInstance(obj) &&
				"document" in obj.params &&
				typeof obj.params.document === "object" &&
				"docRef" in obj.params &&
				typeof obj.params.docRef === "string" &&
				"locale" in obj.params &&
				typeof obj.params.locale === "string"
			);
		}
	}

	export interface PartialModifyJsonRpc2Request extends JsonRpc2Request {
		readonly method: "PARTIAL_MODIFY_DOCUMENT";
		readonly params: {
			docRef: string;
			documentPart: DocumentPart[];
			locale: string;
		};
	}

	export namespace PartialModifyJsonRpc2Request {
		export function isInstance(obj: unknown): obj is PartialModifyJsonRpc2Request {
			return (
				DocumentJsonRpc2Request.isInstance(obj) &&
				"documentPart" in obj.params &&
				"docRef" in obj.params &&
				typeof obj.params.docRef === "string" &&
				"locale" in obj.params &&
				typeof obj.params.locale === "string"
			);
		}
	}

	export interface DocumentPart {
		readonly path: string;
		readonly value: object;
		readonly repetitions: number[];
	}

	export namespace DocumentPart {
		export function isInstance(obj: unknown): obj is DocumentPart {
			return (
				isObject(obj) &&
				"path" in obj &&
				typeof obj.path === "string" &&
				"value" in obj &&
				typeof obj.value === "object" &&
				"repetitions" in obj &&
				Array.isArray(obj.repetitions) &&
				obj.repetitions.every(element => typeof element === "number")
			);
		}
	}

	export interface DeleteJsonRpc2Request extends JsonRpc2Request {
		readonly method: "DELETE_DOCUMENT";
		readonly params: {
			docRef: string;
			locale: string;
		};
	}

	export namespace DeleteJsonRpc2Request {
		export function isInstance(obj: unknown): obj is DeleteJsonRpc2Request {
			return (
				DocumentJsonRpc2Request.isInstance(obj) &&
				"docRef" in obj.params &&
				typeof obj.params.docRef === "string" &&
				"locale" in obj.params &&
				typeof obj.params.locale === "string"
			);
		}
	}

	export interface MultiDeleteJsonRpc2Request extends JsonRpc2Request {
		readonly method: "MULTI_DELETE_DOCUMENTS";
		readonly params: {
			docRefs: string[];
		};
	}

	export namespace MultiDeleteJsonRpc2Request {
		export function isInstance(obj: unknown): obj is MultiDeleteJsonRpc2Request {
			return DocumentJsonRpc2Request.isInstance(obj) && "docRefs" in obj.params;
		}
	}

	export interface ValidateJsonRpc2Request extends JsonRpc2Request {
		readonly method: "VALIDATE_DOCUMENT";
		readonly params: {
			document: object;
			documentModelName: string;
			locale: string;
			partial?: boolean;
		};
	}

	export namespace ValidateJsonRpc2Request {
		export function isInstance(obj: unknown): obj is ValidateJsonRpc2Request {
			return (
				DocumentJsonRpc2Request.isInstance(obj) &&
				"document" in obj.params &&
				"documentModelName" in obj.params &&
				typeof obj.params.documentModelName === "string" &&
				"locale" in obj.params &&
				typeof obj.params.locale === "string"
			);
		}
	}

	export interface GetDocumentJsonRpc2Request extends JsonRpc2Request {
		readonly method: "GET_DOCUMENT";
		readonly params: {
			readonly docRef: string;
		};
	}

	export namespace GetDocumentJsonRpc2Request {
		export function isInstance(obj: unknown): obj is GetDocumentJsonRpc2Request {
			return (
				DocumentJsonRpc2Request.isInstance(obj) &&
				"docRef" in obj.params &&
				typeof obj.params.docRef === "string"
			);
		}
	}
}

export interface AddDocumentJsonRpc2Response extends JsonRpc2Response {
	readonly result: {
		readonly docRef: string;
	};
}

export namespace AddDocumentJsonRpc2Response {
	export function isInstance(obj: unknown): obj is AddDocumentJsonRpc2Response {
		return (
			JsonRpc2Response.ok.isInstance(obj) &&
			"result" in obj &&
			(typeof obj.id === "string" || typeof obj.id === "number" || obj.id === null)
		);
	}
}

export interface ModifyJsonRpc2Response extends JsonRpc2Response {
	readonly result: never;
}

export namespace ModifyJsonRpc2Response {
	export function isInstance(value: unknown): value is ModifyJsonRpc2Response {
		return JsonRpc2Response.ok.isInstance(value);
	}
}

export interface DocumentValidationError {
	readonly errorText: string;
	readonly errorCode: string;
	readonly messageType: string;
	readonly rulePath: string;
	readonly referencedFields: string[];
	readonly severityType: string;
}

export interface ValidateDocumentJsonRpc2Response extends JsonRpc2Response {
	readonly result: {
		readonly validationErrors: DocumentValidationError[];
	};
}

export namespace ValidateDocumentJsonRpc2Response {
	export function isInstance(obj: unknown): obj is ValidateDocumentJsonRpc2Response {
		return (
			JsonRpc2Response.ok.isInstance(obj) &&
			"result" in obj &&
			"validationErrors" in obj.result &&
			typeof obj.result.validationErrors === "object"
		);
	}
}

export interface GetDocumentJsonRpc2Response extends JsonRpc2Response {
	readonly result: DocumentSpec;
}

export namespace GetDocumentJsonRpc2Response {
	export function isInstance(obj: unknown): obj is GetDocumentJsonRpc2Response {
		return (
			JsonRpc2Response.ok.isInstance(obj) &&
			"result" in obj &&
			"docRef" in obj.result &&
			"documentModelName" in obj.result &&
			"document" in obj.result
		);
	}
}
