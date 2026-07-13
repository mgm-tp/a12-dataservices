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
import { isArray, isNumber, isObject, isString } from "../common/TypeGuardUtils.js";
import { JsonRpc2Request, JsonRpc2Response } from "../json-rpc/index.js";

export interface DocumentSpec {
	readonly docRef: string;
	readonly documentModelName: string;
	readonly document: object;
}

export namespace DocumentSpec {
	export function isInstance(obj: unknown): obj is DocumentSpec {
		return (
			isObject(obj) &&
			"docRef" in obj &&
			isString(obj.docRef) &&
			"documentModelName" in obj &&
			isString(obj.documentModelName) &&
			"document" in obj &&
			isObject(obj.document)
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
	| DocumentJsonRpc2Request.GetDocumentJsonRpc2Request
	| DocumentJsonRpc2Request.CheckUniquenessJsonRpc2Request;

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
			"GET_DOCUMENT",
			"CHECK_UNIQUENESS"
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
				isObject(obj.params.document) &&
				"documentModelName" in obj.params &&
				isString(obj.params.documentModelName) &&
				"locale" in obj.params &&
				isString(obj.params.locale)
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
				isString(obj.params.docRef) &&
				"locale" in obj.params &&
				isString(obj.params.locale)
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
				isObject(obj.params.document) &&
				"docRef" in obj.params &&
				isString(obj.params.docRef) &&
				"locale" in obj.params &&
				isString(obj.params.locale)
			);
		}
	}

	/**
	 * A request to partially modify a document by applying one or more {@link DocumentPart} patches.
	 *
	 * Each `DocumentPart` in `documentPart` may address either a field or a group. Group semantics
	 * are controlled by the `repetitions` array — see {@link DocumentPart} for details.
	 */
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
				isString(obj.params.docRef) &&
				"locale" in obj.params &&
				isString(obj.params.locale)
			);
		}
	}

	/**
	 * A single patch instruction for the `PARTIAL_MODIFY_DOCUMENT` operation.
	 *
	 * `path` must be the slash-prefixed model path of the target element (e.g.
	 * `/BusinessPartnerRoot/Attachment`).
	 *
	 * `value` carries the replacement content:
	 * - For a **field**, `value` is a primitive wrapped in an object.
	 * - For a **group**, `value` is a plain object whose keys correspond to the group's fields and
	 *   nested groups as defined by the document model.
	 *
	 * `repetitions` selects the target position within repeatable ancestors:
	 * - **Concrete indices** (e.g. `[1, 2]`) — the addressed group is replaced if it already exists,
	 *   or inserted at the given position if it does not. Missing ancestors are created automatically.
	 * - **Trailing wildcard `0`** (e.g. `[1, 0]`) — the supplied group is appended as a new
	 *   repetition of the addressed repeatable group; it becomes the first entry when none exist yet.
	 *   A null value combined with a trailing `0` is invalid.
	 * - **Null / empty** — treated as concrete (replace/insert).
	 *
	 * Setting `value` to `null` (and omitting the trailing wildcard) removes the addressed field or
	 * group.
	 */
	export interface DocumentPart {
		readonly path: string;
		readonly value?: object;
		readonly repetitions: number[];
	}

	export namespace DocumentPart {
		export function isInstance(obj: unknown): obj is DocumentPart {
			return (
				isObject(obj) &&
				"path" in obj &&
				isString(obj.path) &&
				"value" in obj &&
				isObject(obj.value) &&
				"repetitions" in obj &&
				Array.isArray(obj.repetitions) &&
				obj.repetitions.every(element => isNumber(element))
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
				isString(obj.params.docRef) &&
				"locale" in obj.params &&
				isString(obj.params.locale)
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
				isString(obj.params.documentModelName) &&
				"locale" in obj.params &&
				isString(obj.params.locale)
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
				isString(obj.params.docRef)
			);
		}
	}

	export interface CheckUniquenessJsonRpc2Request extends JsonRpc2Request {
		readonly method: "CHECK_UNIQUENESS";
		readonly params: {
			readonly documentModelName: string;
			readonly docRef?: string;
			readonly document: object;
		};
	}

	export namespace CheckUniquenessJsonRpc2Request {
		export function isInstance(obj: unknown): obj is CheckUniquenessJsonRpc2Request {
			return (
				DocumentJsonRpc2Request.isInstance(obj) &&
				"documentModelName" in obj.params &&
				isString(obj.params.documentModelName) &&
				"document" in obj.params &&
				isObject(obj.params.document)
			);
		}
	}
}

export interface CheckUniquenessViolation {
	readonly modelName: string;
	readonly constraintName: string;
	readonly conflictingDocRef: string;
	readonly errorMessage: Record<string, string>;
	readonly fieldFullNames: string[];
	readonly errorKey: string;
}

/**
 * The result of a CHECK_UNIQUENESS operation.
 *
 * {@link violations} is empty when all uniqueness constraints are satisfied.
 *
 * Each individual {@link CheckUniquenessViolation} carries its own {@link CheckUniquenessViolation.modelName}
 * reflecting the topmost model in the hierarchy that defines that specific constraint, which may
 * differ per violation when sub-type-only constraints are involved.
 */
export interface CheckUniquenessResponse {
	readonly violations: CheckUniquenessViolation[];
}

export namespace CheckUniquenessResponse {
	export function isInstance(obj: unknown): obj is CheckUniquenessResponse {
		return (
			isObject(obj) &&
			"violations" in obj &&
			isArray((obj as Record<string, unknown>).violations, isViolation)
		);
	}

	function isViolation(item: unknown): item is CheckUniquenessViolation {
		return (
			isObject(item) &&
			"modelName" in item &&
			isString((item as Record<string, unknown>).modelName) &&
			"constraintName" in item &&
			isString((item as Record<string, unknown>).constraintName) &&
			"conflictingDocRef" in item &&
			isString((item as Record<string, unknown>).conflictingDocRef) &&
			"errorMessage" in item &&
			isObject((item as Record<string, unknown>).errorMessage) &&
			"fieldFullNames" in item &&
			isArray((item as Record<string, unknown>).fieldFullNames, isString) &&
			"errorKey" in item &&
			isString((item as Record<string, unknown>).errorKey)
		);
	}
}

export interface CheckUniquenessJsonRpc2Response extends JsonRpc2Response {
	readonly result: CheckUniquenessResponse;
}

export namespace CheckUniquenessJsonRpc2Response {
	export function isInstance(obj: unknown): obj is CheckUniquenessJsonRpc2Response {
		return JsonRpc2Response.ok.isInstance(obj) && CheckUniquenessResponse.isInstance(obj.result);
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
			JsonRpc2Response.ok.isInstance(obj) && "docRef" in obj.result && isString(obj.result.docRef)
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

export namespace DocumentValidationError {
	export function isInstance(obj: unknown): obj is DocumentValidationError {
		return (
			isObject(obj) &&
			"errorText" in obj &&
			isString(obj.errorText) &&
			"errorCode" in obj &&
			isString(obj.errorCode) &&
			"messageType" in obj &&
			isString(obj.messageType) &&
			"rulePath" in obj &&
			isString(obj.rulePath) &&
			"referencedFields" in obj &&
			isArray(obj.referencedFields, isString) &&
			"severityType" in obj &&
			isString(obj.severityType)
		);
	}
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
			"validationErrors" in obj.result &&
			isArray(obj.result.validationErrors, DocumentValidationError.isInstance)
		);
	}
}

export interface GetDocumentJsonRpc2Response extends JsonRpc2Response {
	readonly result: DocumentSpec;
}

export namespace GetDocumentJsonRpc2Response {
	export function isInstance(obj: unknown): obj is GetDocumentJsonRpc2Response {
		return JsonRpc2Response.ok.isInstance(obj) && DocumentSpec.isInstance(obj.result);
	}
}
