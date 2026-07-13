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
/** @module common */

/** Checks if value is a plain object (not null, not array). */
export function isObject(value: unknown): value is Record<string, unknown> {
	return typeof value === "object" && value !== null && !Array.isArray(value);
}

/** Checks if value is a string. */
export function isString(value: unknown): value is string {
	return typeof value === "string";
}

/** Checks if value is a finite number (excludes NaN and Infinity). */
export function isNumber(value: unknown): value is number {
	return typeof value === "number" && !Number.isNaN(value) && Number.isFinite(value);
}

/** Checks if value a boolean. */
export function isBoolean(value: unknown): value is boolean {
	return typeof value === "boolean";
}

/** Checks if value is null. */
export function isNull(value: unknown): value is null {
	return value === null;
}

/** Checks if value is an array and all its elements satisfy the given type check. */
export function isArray<T>(
	value: unknown,
	typeCheck: (value: unknown) => value is T
): value is T[] {
	return Array.isArray(value) && value.every(typeCheck);
}

/**
 * Checks if field is not present in value or satisfies the given type check.
 *
 * Note: a field that is present but explicitly set to `undefined` (e.g. `{ field: undefined }`)
 * is treated differently from an absent field — it will be passed to `typeCheck`, which typically
 * returns `false` for `undefined`. Use this intentionally; spreading objects that may carry
 * `undefined` values will cause `isInstance` checks to fail.
 */
export function isOptionalFieldOfType<T>(
	value: Record<string, unknown>,
	field: string,
	typeCheck: (value: unknown) => value is T
): boolean {
	return !(field in value) || typeCheck(value[field]);
}
