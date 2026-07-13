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
package com.mgmtp.a12.dataservices.client.rpc;

import java.util.List;

import com.mgmtp.a12.dataservices.rpc.JsonRpc2Request;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * RpcOperationsClient interface provides access to JSON-RPC 2.0 based functionality.
 */
@OnlyForUsage public interface RpcOperationsClient {

	/**
	 * Processes an RPC request. The request consists of a list of {@link JsonRpc2Request} objects,
	 * each representing an RPC method call or — in A12 Data Services nomenclature — an operation.
	 *
	 * @param request A collection of JSON-RPC 2.0 methods (aka operations) to be executed in sequence. Must not be `null`.
	 * @return A collection of {@link JsonRpc2Response} objects corresponding to the supplied request. Never `null`.
	 */
	List<JsonRpc2Response> invoke(List<JsonRpc2Request> request);

	/**
	 * {@inheritDoc}
	 *
	 * Adds a caller-supplied request identifier used for idempotency checks or trace correlation.
	 *
	 * @param requestId The unique identifier for the request used to ensure exactly-once processing or tracing. May be `null`.
	 * @param request A collection of JSON-RPC 2.0 methods to be executed in sequence. Must not be `null`.
	 * @return A collection of {@link JsonRpc2Response} objects corresponding to the supplied request. Never `null`.
	 */
	default List<JsonRpc2Response> invoke(String requestId, List<JsonRpc2Request> request) {
		return invoke(request);
	}
}
