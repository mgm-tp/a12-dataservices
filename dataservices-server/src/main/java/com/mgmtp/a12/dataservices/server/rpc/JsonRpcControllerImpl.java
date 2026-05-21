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
package com.mgmtp.a12.dataservices.server.rpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.googlecode.jsonrpc4j.JsonRpcBasicServer;
import com.mgmtp.a12.dataservices.exception.FunctionalityDisabledException;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2ResponseError;
import com.mgmtp.a12.dataservices.rpc.RpcException;
import com.mgmtp.a12.dataservices.rpc.internal.JsonRpcOperationDispatcher;
import com.mgmtp.a12.dataservices.rpc.internal.RequestIdManager;
import com.mgmtp.a12.dataservices.rpc.internal.RequestIdService;
import com.mgmtp.a12.dataservices.server.uaa.SecuredController;

import static com.mgmtp.a12.dataservices.rpc.JsonRpc2Request.REQUEST_ID_HEADER;

/**
 * In this controller we need to handle general exception caused by validating links in
 * {@link JsonRpcOperationDispatcher#handleRequest(java.io.InputStream, java.io.OutputStream)}.
 * As it operates on raw streams, we need to deserialize the response to json array, add error message to end of it
 * and serialize it again in case of general error.
 * Per operation errors are handled by {@link JsonRpcBasicServer} and resolved to error responses there, so not handled
 * here.
 */
@RequestMapping("#{@dataServicesCoreProperties.server.contextPath}/v2/rpc")
@SecuredController
@RestController public class JsonRpcControllerImpl {

	@Autowired private JsonRpcBasicServer jsonRpcServer;
	@Autowired private ObjectMapper objectMapper;
	@Autowired private RequestIdService requestIdFactory;
	@Autowired private JsonRpcOperationDispatcher jsonRpcOperationDispatcher;

	/**
	 * Handles JSON-RPC requests, ensuring request ID lifecycle management and graceful error aggregation.
	 *
	 * When an unhandled exception occurs, the controller appends an error entry to the JSON-RPC response array.
	 *
	 * @param request the input stream containing the JSON-RPC request payload; never null.
	 * @param requestId optional request ID header used for tracking; may be `null`.
	 * @return a response containing the JSON-RPC result array (with appended error entry if needed).
	 * @throws IOException if reading or writing the request/response stream fails.
	 */
	@PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<ByteArrayResource> jsonRpc(InputStream request, @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId)
		throws IOException {
		RequestIdManager requestIdManager = requestIdFactory.newRequestIdManager(requestId);
		try (ByteArrayOutputStream operationOutput = new ByteArrayOutputStream()) {
			try {
				jsonRpcServer.handleRequest(request, operationOutput);
				finalizeRequest(requestIdManager, operationOutput);
				return ResponseEntity.ok(new ByteArrayResource(operationOutput.toByteArray()));
			} catch (FunctionalityDisabledException e) {
				throw e;
			} catch (Exception e) {
				if (requestIdManager != null) {
					requestIdManager.finalizeRequestError();
				}
				try (ByteArrayOutputStream outputWithError = new ByteArrayOutputStream()) {
					addExceptionToRpc(operationOutput, e, outputWithError);
					return ResponseEntity.ok(new ByteArrayResource(outputWithError.toByteArray()));
				}
			}
		}
	}

	private void finalizeRequest(RequestIdManager requestIdManager, ByteArrayOutputStream operationOutput) throws IOException {
		if (StreamSupport.stream(getJsonArray(operationOutput).spliterator(), false)
			.anyMatch(n -> n.has("error"))) {
			if (requestIdManager != null) {
				requestIdManager.finalizeRequestError();
			}
		} else {
			if (requestIdManager != null) {
				requestIdManager.finalizeRequestSuccess();
			}
		}
	}

	private void addExceptionToRpc(ByteArrayOutputStream operationOutput, Throwable t, ByteArrayOutputStream outputWithError) throws IOException {
		JsonNode errorResponse = objectMapper.valueToTree(createErrorResponse(t));
		ArrayNode jsonOperationOutput = getJsonArray(operationOutput);
		JsonNode resultNode = jsonOperationOutput == null
			? errorResponse
			: jsonOperationOutput.add(errorResponse);

		try (JsonGenerator jsonGenerator = objectMapper.createGenerator(outputWithError)) {
			jsonGenerator.writeTree(resultNode);
		}
	}

	private JsonRpc2Response createErrorResponse(Throwable t) {
		return new JsonRpc2Response(
			t instanceof RpcException rpcException

				? new JsonRpc2ResponseError(
				rpcException.getOperationError().getCode(),
				t.getMessage(),
				objectMapper.valueToTree(jsonRpcOperationDispatcher.createExceptionDetail(rpcException.getOperationError(), t)))

				: new JsonRpc2ResponseError(-1, t.getMessage(), null)
		);
	}

	private ArrayNode getJsonArray(ByteArrayOutputStream operationOutput) throws IOException {
		JsonNode resultJson = objectMapper.readTree(operationOutput.toByteArray());
		if (resultJson instanceof MissingNode) {
			return null;
		}

		if (resultJson.isArray()) {
			return (ArrayNode) resultJson;
		} else {
			return objectMapper.createArrayNode().add(resultJson);
		}
	}
}
