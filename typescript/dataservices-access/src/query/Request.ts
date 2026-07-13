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
import { JsonRpc2Request } from "../json-rpc/index.js";

import type { Query } from "./Query.js";

/**
 * @module Query/api
 * @description This module contains interfaces for Query-related JSON-RPC 2.0 requests and responses.
 */

/**
 * Represents a JSON-RPC 2.0 request for querying.
 * @extends JsonRpc2Request
 */
export interface QueryJsonRpc2Request<
	Q extends Query.QueryRoot = Query.QueryRoot
> extends JsonRpc2Request {
	/**
	 * The query operation name for the query request.
	 * @readonly
	 */
	readonly method: "QUERY";

	/**
	 * The parameters for the query request.
	 * @readonly
	 */
	readonly params: {
		/**
		 * The root query.
		 * @readonly
		 */
		readonly query: Q;
	};
}

export namespace QueryJsonRpc2Request {
	/**
	 * Type guard to check if an object is an instance of QueryJsonRpc2Request.
	 * @param obj The object to check.
	 * @returns True if the object is an instance of QueryJsonRpc2Request, false otherwise.
	 */
	export function isInstance(obj: unknown): obj is QueryJsonRpc2Request {
		return JsonRpc2Request.isInstance(obj) && obj.method === "QUERY" && "query" in obj.params;
	}
}
