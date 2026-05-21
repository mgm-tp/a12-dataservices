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
import { readFileSync } from "node:fs";

const omitAtPath = (obj: any, path: (string | number)[]): any => {
	if (path.length === 0) {
		return obj;
	}

	const [head, ...rest] = path;

	if (rest.length === 0) {
		const { [head]: _, ...restObj } = obj;
		return restObj;
	}

	return {
		...obj,
		[head]: omitAtPath(obj[head], rest)
	};
};

const omitMany = (obj: any, paths: (string | number)[][]) => {
	return paths.reduce((acc, path) => omitAtPath(acc, path), obj);
};

const replaceAtPath = (obj: any, path: (string | number)[], value: any): any => {
	const [head, ...rest] = path;

	if (rest.length === 0) {
		if (Array.isArray(obj)) {
			const clone = obj.slice();
			clone[head as number] = value;
			return clone;
		}
		return { ...obj, [head]: value };
	}

	const next = obj !== null ? obj[head] : undefined;

	if (Array.isArray(obj)) {
		const clone = obj.slice();
		clone[head as number] = replaceAtPath(next, rest, value);
		return clone;
	}

	return {
		...obj,
		[head]: replaceAtPath(next, rest, value)
	};
};

const replaceMany = (obj: any, entries: Array<{ path: (string | number)[]; value: any }>) => {
	return entries.reduce((acc, { path, value }) => replaceAtPath(acc, path, value), obj);
};

const loadResource = (resourcePath: string) => {
	const reqString = readFileSync(resourcePath, { encoding: "utf8" });
	return JSON.parse(reqString);
};

export { loadResource, omitMany, replaceMany, omitAtPath, replaceAtPath };
