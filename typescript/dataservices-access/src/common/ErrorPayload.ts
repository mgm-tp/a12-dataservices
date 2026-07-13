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
import { isObject, isOptionalFieldOfType, isString } from "./TypeGuardUtils.js";

/** @module common */

export interface ErrorPayload {
	// Following error types are allowed:
	// RESOURCE_NOT_FOUND_ERROR_TYPE, FORBIDDEN_ERROR_TYPE, PRECONDITIONS_FAILED_ERROR_TYPE
	errorType: string;
	// Non-localized English text that provides additional information
	message: string;
}

export namespace ErrorPayload {
	// Server responds with error if there is no model with specified model in the persistence store
	export const RESOURCE_NOT_FOUND_ERROR_TYPE = "RESOURCE_NOT_FOUND";
	// Server responds with error if user is not allowed to access existing model
	export const FORBIDDEN_ERROR_TYPE = "FORBIDDEN";
	// Server responds with error if stored model is not serializable to JSON
	export const PRECONDITIONS_FAILED_ERROR_TYPE = "PRECONDITIONS_FAILED";
	// Server responds with error if provided password, token or email is empty in case of password change/reset
	export const UNKNOWN = "UNKNOWN";

	export function isInstance(obj: unknown): obj is ErrorPayload {
		return (
			isObject(obj) &&
			"errorType" in obj &&
			isString(obj.errorType) &&
			"message" in obj &&
			isString(obj.message)
		);
	}
}

export interface ErrorResponse {
	level: string;
	operationId?: string;
	shortMessage?: ErrorResponse.LocalizedEntry;
	longMessage?: ErrorResponse.LocalizedEntry;
	errorDetail?: ErrorResponse.ErrorDetail;
}

export namespace ErrorResponse {
	export const WARN = "WARN";

	export const ERROR = "ERROR";

	export interface LocalizedEntry {
		readonly key: string;
		readonly default: string;
	}

	export namespace LocalizedEntry {
		export function isInstance(obj: unknown): obj is LocalizedEntry {
			return (
				isObject(obj) &&
				"key" in obj &&
				isString(obj.key) &&
				"default" in obj &&
				isString(obj.default)
			);
		}
	}

	export interface ErrorDetail {
		readonly code: string;
		readonly subsystem: string;
		readonly time: string;
	}

	export namespace ErrorDetail {
		export function isInstance(obj: unknown): obj is ErrorDetail {
			return (
				isObject(obj) &&
				"code" in obj &&
				isString(obj.code) &&
				"subsystem" in obj &&
				isString(obj.subsystem) &&
				"time" in obj &&
				isString(obj.time)
			);
		}
	}

	export function isInstance(obj: unknown): obj is ErrorResponse {
		return (
			isObject(obj) &&
			"level" in obj &&
			(obj.level === WARN || obj.level === ERROR) &&
			isOptionalFieldOfType(obj, "operationId", isString) &&
			isOptionalFieldOfType(obj, "shortMessage", ErrorResponse.LocalizedEntry.isInstance) &&
			isOptionalFieldOfType(obj, "longMessage", ErrorResponse.LocalizedEntry.isInstance) &&
			isOptionalFieldOfType(obj, "errorDetail", ErrorResponse.ErrorDetail.isInstance)
		);
	}
}
