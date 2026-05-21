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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.client.rpc.internal.JsonRpc2RequestBuilder;

/**
 * Request Builder Factory class for java clients of the data services.
 *
 */
public class RequestBuilderFactory {

	private final ObjectMapper objectMapper;

	/**
	 * Creates a new factory for building JSON-RPC 2 requests with the given {@link ObjectMapper}.
	 *
	 * @param objectMapper the Jackson object mapper used for serialization; never null.
	 */
	public RequestBuilderFactory(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * @return new {@link JsonRpc2RequestBuilder} instance tied to this factory configuration.
	 */
	public JsonRpc2RequestBuilder newJsonRpc2RequestBuilder() {
		return new JsonRpc2RequestBuilder(objectMapper);
	}

}


