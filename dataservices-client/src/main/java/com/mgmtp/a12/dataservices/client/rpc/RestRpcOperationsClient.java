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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.connector.rest.RestPostConnector;
import com.mgmtp.a12.connector.rest.RestServerRequest;
import com.mgmtp.a12.connector.rest.UrlBuilderSupport;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Request;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;

import lombok.NonNull;

import static com.mgmtp.a12.dataservices.rpc.JsonRpc2Request.REQUEST_ID_HEADER;

/**
 * REST implementation of {@link RpcOperationsClient} which uses HTTP client with Basic Auth to issue HTTP request to server.
 * Use this client to perform document related operations.
 */
public class RestRpcOperationsClient implements RpcOperationsClient {

	private static final String[] CONTEXT = { "v2", "rpc" };

	private final RestPostConnector postConnector;
	private final UrlBuilderSupport urlBuilderSupport;

	/**
	 * Creates a REST-based RPC operations client.
	 *
	 * @param baseUrl the server base URL; must not be `null`.
	 * @param postConnector the POST connector used to invoke RPC operations; must not be `null`.
	 */
	public RestRpcOperationsClient(@NonNull String baseUrl, @NonNull RestPostConnector postConnector) {
		this.postConnector = postConnector;
		urlBuilderSupport = UrlBuilderSupport.withBaseUrl(baseUrl, CONTEXT);
	}

	/**
	 * Invokes RPC operations without an explicit request ID.
	 *
	 * @param request the list of JSON-RPC requests to execute; must not be `null`.
	 * @return the list of JSON-RPC responses in the same order as requests.
	 */
	@Override public List<JsonRpc2Response> invoke(List<JsonRpc2Request> request) {
		return invoke(null, request);
	}

	/**
	 * Invokes RPC operations with a given request ID for correlation.
	 * Adds the {@link REQUEST_ID_HEADER} to the outgoing request if `requestId` is not `null`.
	 *
	 * @param requestId optional correlation identifier; may be `null`.
	 * @param request the list of JSON-RPC requests to execute; must not be `null`.
	 * @return the list of JSON-RPC responses in the same order as requests.
	 */
	@Override public List<JsonRpc2Response> invoke(String requestId, List<JsonRpc2Request> request) {
		String url = urlBuilderSupport.createBuilder().toUriString();
		RestServerRequest<List<JsonRpc2Request>> restRequest = Optional.ofNullable(requestId)
			.map(rId -> RestServerRequest.withPayload(request).withHeader(REQUEST_ID_HEADER, requestId))
			.orElseGet(() -> RestServerRequest.withPayload(request));
		return postConnector.callServer(url, restRequest, ResponseList.class).getData();
	}

	private static class ResponseList extends ArrayList<JsonRpc2Response> {
	}
}
