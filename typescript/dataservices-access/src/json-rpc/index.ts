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
	isNull,
	isNumber,
	isObject,
	isOptionalFieldOfType,
	isString
} from "../common/TypeGuardUtils.js";

/** @module json-rpc */
export interface JsonRpc2Message {
	readonly jsonrpc: "2.0";
	readonly id: string | number | null;
}

export namespace JsonRpc2Message {
	export function isInstance(obj: unknown): obj is JsonRpc2Message {
		return (
			isObject(obj) &&
			"jsonrpc" in obj &&
			obj.jsonrpc === "2.0" &&
			"id" in obj &&
			(isString(obj.id) || isNumber(obj.id) || isNull(obj.id))
		);
	}
}

export interface JsonRpc2Request extends JsonRpc2Message {
	readonly method: string;
	readonly params: Map<string, object> | object;
}

export namespace JsonRpc2Request {
	export function build(request: JsonRpc2Request[]): RestRequestPayload {
		return {
			method: "POST",
			relativeUrl: "/v2/rpc",
			body: JSON.stringify(request),
			customHeaders: [
				["Accept", "application/json"],
				["Content-Type", "application/json;charset=utf8"]
			]
		};
	}

	export function isInstance(obj: unknown): obj is JsonRpc2Request {
		return (
			JsonRpc2Message.isInstance(obj) && "method" in obj && isString(obj.method) && "params" in obj
		);
	}
}

export interface JsonRpc2Response extends JsonRpc2Message {
	readonly result?: any;
	readonly error?: JsonRpc2Response.JsonRpc2Error;
}

/**
 * The Required<...> is here to force typescript to differentiate between JsonRpc2ResponseError
 * and the 'supertype' JsonRpc2Response when dealing with type-guards. Same for JsonRpc2ResponseOK
 */
export type JsonRpc2ResponseError = Required<Omit<JsonRpc2Response, "result">>;
export type JsonRpc2ResponseOK = Required<Omit<JsonRpc2Response, "error">>;

/**
 * A JSON-RPC error response narrowed to a unique-constraint error, where
 * {@link JsonRpc2Response.JsonRpc2Error.data} is known to be a {@link JsonRpc2Response.Exception}.
 * Use {@link JsonRpc2Response.uniqueConstraintViolationError.isInstance} (error code -32060)
 * to narrow a response to this type.
 */
export type JsonRpc2UniqueConstraintErrorResponse = Omit<JsonRpc2ResponseError, "error"> & {
	readonly error: Omit<JsonRpc2Response.JsonRpc2Error, "data"> & {
		readonly data: JsonRpc2Response.Exception;
	};
};

export namespace JsonRpc2Response {
	export interface Exception {
		readonly level: string;
		readonly title: LocalizableMessage;
		readonly description: LocalizableMessage;
		readonly details: ExceptionDetails;
		readonly source?: string;
		readonly timestamp?: string;
		readonly logId?: string;
	}

	export namespace Exception {
		export function isInstance(error: unknown): error is Exception {
			return (
				isObject(error) &&
				"level" in error &&
				isString(error.level) &&
				"title" in error &&
				LocalizableMessage.isInstance(error.title) &&
				"description" in error &&
				LocalizableMessage.isInstance(error.description) &&
				"details" in error &&
				ExceptionDetails.isInstance(error.details) &&
				isOptionalFieldOfType(error, "source", isString) &&
				isOptionalFieldOfType(error, "timestamp", isString) &&
				isOptionalFieldOfType(error, "logId", isString)
			);
		}
	}

	export interface ExceptionDetails {
		readonly code: string;
		readonly subsystem: string;
		readonly time: string;
	}

	export namespace ExceptionDetails {
		export function isInstance(error: unknown): error is ExceptionDetails {
			return (
				isObject(error) &&
				"code" in error &&
				isString(error.code) &&
				"time" in error &&
				isString(error.time) &&
				"subsystem" in error &&
				isString(error.subsystem)
			);
		}
	}

	export interface LocalizableMessage {
		readonly default: string;
		readonly key: string;
	}

	export namespace LocalizableMessage {
		export function isInstance(error: unknown): error is LocalizableMessage {
			return (
				isObject(error) &&
				"key" in error &&
				isString(error.key) &&
				"default" in error &&
				isString(error.default)
			);
		}
	}

	export function isInstance(obj: unknown): obj is JsonRpc2Response {
		return JsonRpc2Message.isInstance(obj);
	}

	export namespace ok {
		export function isInstance(obj: unknown): obj is JsonRpc2ResponseOK {
			return JsonRpc2Response.isInstance(obj) && "result" in obj && !("error" in obj);
		}
	}

	export namespace error {
		export function isInstance(obj: unknown): obj is JsonRpc2ResponseError {
			return (
				JsonRpc2Response.isInstance(obj) &&
				!("result" in obj) &&
				"error" in obj &&
				JsonRpc2Error.isInstance(obj.error)
			);
		}
	}

	/**
	 * Type guard for a unique-constraint violation error response (error code -32060).
	 * Thrown when a document violates a named uniqueness constraint during ADD_DOCUMENT or MODIFY_DOCUMENT.
	 * Narrows {@link JsonRpc2Response.JsonRpc2Error.data} to {@link Exception}.
	 */
	export namespace uniqueConstraintViolationError {
		export function isInstance(obj: unknown): obj is JsonRpc2UniqueConstraintErrorResponse {
			return (
				JsonRpc2Response.error.isInstance(obj) &&
				obj.error.code === JsonRpc2Error.UNIQUE_CONSTRAINT_VIOLATION &&
				JsonRpc2Response.Exception.isInstance(obj.error.data)
			);
		}
	}

	export function hasError(response: JsonRpc2Response): boolean {
		return "error" in response;
	}

	export function hasErrors(response: JsonRpc2Response[]): boolean {
		return response.find((r: JsonRpc2Response) => "error" in r) !== undefined;
	}

	export interface JsonRpc2Error {
		readonly code: number;
		readonly message: string;
		readonly data: Exception | unknown;
	}

	export namespace JsonRpc2Error {
		export const JSON_PARSE_ERROR = -32700;
		export const INVALID_REQUEST = -32600;
		export const METHOD_NOT_FOUND = -32601;
		export const INVALID_PARAMS = -32602;
		export const INTERNAL_ERROR = -32603;
		export const UNIQUE_CONSTRAINT_VIOLATION = -32060;

		export function isInstance(obj: unknown): obj is JsonRpc2Response.JsonRpc2Error {
			return (
				isObject(obj) &&
				"code" in obj &&
				isNumber(obj.code) &&
				"message" in obj &&
				isString(obj.message) &&
				"data" in obj
			);
		}
	}
}
