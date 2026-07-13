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
package com.mgmtp.a12.dataservices.client.rpc.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import tools.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.client.rpc.RequestBuilderFactory;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Request;

/**
 * Builder for list of {@link JsonRpc2Request}s. Obtain it from {@link RequestBuilderFactory} bean by calling {@link RequestBuilderFactory#newJsonRpc2RequestBuilder()} .
 */
public class JsonRpc2RequestBuilder {

	protected final List<RpcMethodCallBuilder> builders = new LinkedList<>();
	private final ObjectMapper objectMapper;

	public JsonRpc2RequestBuilder(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * Constructs new list of {@link JsonRpc2Request}s.
	 *
	 * @return new list of {@link JsonRpc2Request}s
	 */
	public List<JsonRpc2Request> build() {
		return builders.stream()
			.map(RpcMethodCallBuilder::build)
			.toList();
	}

	protected Collector<JsonRpc2Request, ?, List<JsonRpc2Request>> collector() {
		return Collectors.toList();
	}

	/**
	 * Add new operation to the {@link RpcMethodCallBuilder} instance.
	 *
	 * @param methodName
	 * @return {@link RpcMethodCallBuilder} for newly added method call.
	 */
	public RpcMethodCallBuilder addMethodCall(String methodName) {
		RpcMethodCallBuilder methodCall = new RpcMethodCallBuilder(methodName, this);
		builders.add(methodCall);
		return methodCall;
	}

	/**
	 * Builder for {@link RpcMethodCallBuilder}. Obtain it from {@link RequestBuilderFactory} bean by calling {@link RequestBuilderFactory#newJsonRpc2RequestBuilder()} .
	 */
	public class RpcMethodCallBuilder implements CoreOperationConstants {

		protected final JsonRpc2Request operationRequest = new JsonRpc2Request();
		private final JsonRpc2RequestBuilder upstream;
		private Map<String, Object> namedParams;
		private List<Object> positionalParams;

		public RpcMethodCallBuilder(String methodName, JsonRpc2RequestBuilder jsonRpc2RequestBuilder) {
			this.upstream = jsonRpc2RequestBuilder;
			operationRequest.setMethod(methodName);
		}

		/**
		 * Adds ID to operation.
		 *
		 * @param id ID of operation
		 * @return self
		 */
		public RpcMethodCallBuilder id(String id) {
			operationRequest.setId(id);
			return this;
		}

		/**
		 * Sets parameters for operation.
		 *
		 * @param param object serializable to JSON containing operation parameters.
		 * @return self
		 */
		public RpcMethodCallBuilder addParameter(Object param) {
			if (namedParams != null) {
				throw new IllegalStateException("You can not mix positional and named parameter together");
			}
			if (positionalParams == null) {
				positionalParams = new ArrayList<>();
			}
			positionalParams.add(param);
			return this;
		}

		/**
		 * Sets parameters for operation.
		 *
		 * @param name object serialized as JSON containing operation parameters.
		 * @return self
		 */
		public RpcMethodCallBuilder putParameter(String name, Object value) {
			if (positionalParams != null) {
				throw new IllegalStateException("You can not mix positional and named parameter together");
			}
			if (namedParams == null) {
				namedParams = new HashMap<>();
			}
			namedParams.put(name, value);
			return this;
		}

		/**
		 * Constructs new {@link JsonRpc2Request} instance.
		 *
		 * @return new {@link JsonRpc2Request}
		 */
		public JsonRpc2Request build() {
			if (StringUtils.isBlank(operationRequest.getMethod())) {
				throw new IllegalStateException("Method must be provided");
			}
			if (namedParams != null) {
				operationRequest.setParams(objectMapper.valueToTree(namedParams));
			} else if (positionalParams != null) {
				operationRequest.setParams(objectMapper.valueToTree(positionalParams));
			}
			return operationRequest;
		}

		public JsonRpc2RequestBuilder back() {
			return upstream;
		}
	}
}
