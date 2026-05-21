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
import { rest, rpc } from "./dispatchServerRequest.js";

export * from "./TypeGuards.js";
export type * from "./ResponseTypings.js";

/**
 * @experimental
 *
 * Provides a type-safe way to dispatch dataservices requests and access their responses.
 */
export const Dispatcher = {
	/**
	 * Dispatches one or more given requests using JSON-RPC and returns their corresponding responses.
	 * This only works for the default requests provided by Data Services. Trying to pass any other requests
	 * will lead to compile and runtime errors.
	 *
	 * The response typings are inferred from their corresponding requests.
	 *
	 * NOTE: A configured `ServerConnector` is required.
	 */
	rpc,
	/**
	 * Dispatches the given rest request and returns the response.
	 * `responseChecker` can be used to type-guard the response.
	 *
	 * NOTE: A configured `ServerConnector` is required.
	 */
	rest
};
