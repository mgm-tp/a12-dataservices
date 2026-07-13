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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.googlecode.jsonrpc4j.JsonRpcBasicServer;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.rpc.ExceptionDetail;
import com.mgmtp.a12.dataservices.rpc.RpcException;
import com.mgmtp.a12.dataservices.rpc.RpcExceptionSupport;
import com.mgmtp.a12.dataservices.rpc.internal.JsonRpcOperationDispatcher;
import com.mgmtp.a12.dataservices.rpc.internal.RequestIdService;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class JsonRpcControllerImplTest {

	private JsonRpcBasicServer jsonRpcServer;
	private ObjectMapper objectMapper;
	private RequestIdService requestIdFactory;
	private JsonRpcOperationDispatcher jsonRpcOperationDispatcher;
	private JsonRpcControllerImpl controller;

	@BeforeMethod
	public void setUp() {
		jsonRpcServer = mock(JsonRpcBasicServer.class);
		objectMapper = new ObjectMapper();
		requestIdFactory = mock(RequestIdService.class);
		jsonRpcOperationDispatcher = mock(JsonRpcOperationDispatcher.class);
		controller = new JsonRpcControllerImpl(jsonRpcServer, objectMapper, requestIdFactory, jsonRpcOperationDispatcher);
	}

	@Test(description = "Should include id:null in error response when exception escapes handleRequest and no id was set")
	public void shouldIncludeNullIdInErrorResponseWhenExceptionEscapesHandleRequest() throws IOException {
		// Given
		RpcException rpcException = RpcExceptionSupport.createException(
			ExceptionCodes.RELATIONSHIP_INVALID_LINK_EXCEPTION_CODE,
			ExceptionKeys.RELATIONSHIP_LINK_VALIDATION_ERROR_KEY,
			"Duplicate link not allowed",
			"A duplicate relationship link already exists"
		);
		doThrow(rpcException).when(jsonRpcServer).handleRequest(any(InputStream.class), any(OutputStream.class));
		when(jsonRpcOperationDispatcher.createExceptionDetail(any(), any())).thenReturn(new ExceptionDetail());

		InputStream request = new ByteArrayInputStream(
			"{\"jsonrpc\":\"2.0\",\"method\":\"ADD_LINK\",\"id\":\"op-1\"}".getBytes(StandardCharsets.UTF_8)
		);

		// When
		ResponseEntity<ByteArrayResource> response = controller.jsonRpc(request, null);

		// Then
		assertNotNull(response.getBody());
		String body = new String(response.getBody().getByteArray(), StandardCharsets.UTF_8);
		JsonNode responseNode = objectMapper.readTree(body);

		assertFalse(responseNode.isArray(), "Single request with no prior output yields a single response object");
		assertTrue(responseNode.has("id"), "Response must contain 'id' field so TypeScript JsonRpc2Message.isInstance type guard passes");
		assertTrue(responseNode.get("id").isNull(), "id must be null per JSON-RPC 2.0 spec when request id cannot be determined");
	}

	@Test(description = "Should include well-formed JSON-RPC envelope alongside id:null")
	public void shouldIncludeWellFormedJsonRpcEnvelopeWhenExceptionEscapesHandleRequest() throws IOException {
		// Given
		RpcException rpcException = RpcExceptionSupport.createException(
			ExceptionCodes.RELATIONSHIP_INVALID_LINK_EXCEPTION_CODE,
			ExceptionKeys.RELATIONSHIP_LINK_VALIDATION_ERROR_KEY,
			"Upper Limit Reached",
			"Multiplicity upper limit exceeded"
		);
		doThrow(rpcException).when(jsonRpcServer).handleRequest(any(InputStream.class), any(OutputStream.class));
		when(jsonRpcOperationDispatcher.createExceptionDetail(any(), any())).thenReturn(new ExceptionDetail());

		InputStream request = new ByteArrayInputStream(
			"{\"jsonrpc\":\"2.0\",\"method\":\"ADD_LINK\",\"id\":\"op-2\"}".getBytes(StandardCharsets.UTF_8)
		);

		// When
		ResponseEntity<ByteArrayResource> response = controller.jsonRpc(request, null);

		// Then
		assertNotNull(response.getBody());
		String body = new String(response.getBody().getByteArray(), StandardCharsets.UTF_8);
		JsonNode responseNode = objectMapper.readTree(body);

		assertFalse(responseNode.isArray(), "Single request with no prior output yields a single response object");
		assertTrue(responseNode.has("jsonrpc"), "Response must contain 'jsonrpc' field");
		assertTrue(responseNode.has("error"), "Response must contain 'error' field");
		assertTrue(responseNode.has("id"), "Response must contain 'id' field");
		assertTrue(responseNode.get("id").isNull(), "id must be null");
		assertTrue(responseNode.get("error").has("code"), "error must contain 'code'");
	}
}

