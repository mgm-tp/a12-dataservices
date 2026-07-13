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
package com.mgmtp.a12.dataservices.rpc.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.springframework.aop.TargetClassAware;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;

public class RpcUtilsTest {

	@RemoteOperation(isMutation = false, name = "READ_OP")
	private static class ReadOnlyOperation {
	}

	@RemoteOperation(isMutation = true, name = "WRITE_OP")
	private static class MutatingOperation {
	}

	private static class OperationWithoutAnnotation {
	}

	private final ObjectMapper objectMapper = new ObjectMapper();

	// --- single request ---

	@Test
	public void shouldReturnTrueWhenSingleOperationIsNonMutating() throws Exception {
		Map<String, Object> ops = Map.of("READ_OP", new ReadOnlyOperation());
		JsonNode json = parse("{\"jsonrpc\":\"2.0\",\"method\":\"READ_OP\",\"id\":\"1\"}");

		assertThat(RpcUtils.isAllOperationsNonMutating(json, ops)).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenSingleOperationIsMutating() throws Exception {
		Map<String, Object> ops = Map.of("WRITE_OP", new MutatingOperation());
		JsonNode json = parse("{\"jsonrpc\":\"2.0\",\"method\":\"WRITE_OP\",\"id\":\"1\"}");

		assertThat(RpcUtils.isAllOperationsNonMutating(json, ops)).isFalse();
	}

	// --- batch requests ---

	@Test
	public void shouldReturnTrueWhenAllBatchOperationsAreNonMutating() throws Exception {
		Map<String, Object> ops = Map.of("READ_OP", new ReadOnlyOperation());
		JsonNode json = parse("[{\"jsonrpc\":\"2.0\",\"method\":\"READ_OP\",\"id\":\"1\"},{\"jsonrpc\":\"2.0\",\"method\":\"READ_OP\",\"id\":\"2\"}]");

		assertThat(RpcUtils.isAllOperationsNonMutating(json, ops)).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenBatchContainsAtLeastOneMutatingOperation() throws Exception {
		Map<String, Object> ops = Map.of("READ_OP", new ReadOnlyOperation(), "WRITE_OP", new MutatingOperation());
		JsonNode json = parse("[{\"jsonrpc\":\"2.0\",\"method\":\"READ_OP\",\"id\":\"1\"},{\"jsonrpc\":\"2.0\",\"method\":\"WRITE_OP\",\"id\":\"2\"}]");

		assertThat(RpcUtils.isAllOperationsNonMutating(json, ops)).isFalse();
	}

	// --- unknown / missing annotation ---

	@Test
	public void shouldTreatUnknownOperationAsMutating() throws Exception {
		Map<String, Object> ops = new HashMap<>();
		JsonNode json = parse("{\"jsonrpc\":\"2.0\",\"method\":\"UNKNOWN_OP\",\"id\":\"1\"}");

		assertThat(RpcUtils.isAllOperationsNonMutating(json, ops)).isFalse();
	}

	@Test
	public void shouldTreatOperationMissingAnnotationAsMutating() throws Exception {
		Map<String, Object> ops = Map.of("NO_ANNOTATION_OP", new OperationWithoutAnnotation());
		JsonNode json = parse("{\"jsonrpc\":\"2.0\",\"method\":\"NO_ANNOTATION_OP\",\"id\":\"1\"}");

		assertThat(RpcUtils.isAllOperationsNonMutating(json, ops)).isFalse();
	}

	// --- versioned method names ---

	@DataProvider
	public Object[][] versionedMethodNames() {
		return new Object[][] {
			{ "READ_OP:1" },
			{ "READ_OP:42" },
		};
	}

	@Test(dataProvider = "versionedMethodNames")
	public void shouldStripVersionSuffixAndResolveOperation(String methodName) throws Exception {
		Map<String, Object> ops = Map.of("READ_OP", new ReadOnlyOperation());
		JsonNode json = parse("{\"jsonrpc\":\"2.0\",\"method\":\"" + methodName + "\",\"id\":\"1\"}");

		assertThat(RpcUtils.isAllOperationsNonMutating(json, ops)).isTrue();
	}

	// --- TargetClassAware proxy ---

	@Test
	public void shouldUnwrapTargetClassAwareProxy() throws Exception {
		TargetClassAware proxy = mock(TargetClassAware.class);
		when(proxy.getTargetClass()).thenReturn((Class) ReadOnlyOperation.class);

		Map<String, Object> ops = Map.of("READ_OP", proxy);
		JsonNode json = parse("{\"jsonrpc\":\"2.0\",\"method\":\"READ_OP\",\"id\":\"1\"}");

		assertThat(RpcUtils.isAllOperationsNonMutating(json, ops)).isTrue();
	}

	// --- null operations map ---

	@Test
	public void shouldTreatNullOperationsMapAsMutating() throws Exception {
		JsonNode json = parse("{\"jsonrpc\":\"2.0\",\"method\":\"READ_OP\",\"id\":\"1\"}");

		assertThat(RpcUtils.isAllOperationsNonMutating(json, null)).isFalse();
	}

	private JsonNode parse(String json) throws Exception {
		return objectMapper.readTree(json);
	}
}
