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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.mockito.ArgumentMatchers;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import tools.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.internal.TransactionHandler;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipLinkValidationListener;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;

public class JsonRpcOperationDispatcherReadReplicaTest {

	private static final String REPLICA_URL = "jdbc:postgresql://replica:5432/db";

	@RemoteOperation(isMutation = false, name = "READ_OP")
	private static class ReadOnlyOperation {
	}

	@RemoteOperation(isMutation = true, name = "WRITE_OP")
	private static class MutatingOperation {
	}

	private TransactionHandler transactionHandler;
	private Environment environment;
	private RelationshipLinkValidationListener linkValidator;
	private ApplicationEventPublisher eventPublisher;
	private DataServicesCoreProperties coreProperties;
	private DataServicesCoreProperties.JsonRpc jsonRpcProperties;
	private ObjectMapper objectMapper;

	@BeforeMethod
	public void setUp() {
		transactionHandler = mock(TransactionHandler.class);
		environment = mock(Environment.class);
		linkValidator = mock(RelationshipLinkValidationListener.class);
		eventPublisher = mock(ApplicationEventPublisher.class);
		coreProperties = mock(DataServicesCoreProperties.class);
		jsonRpcProperties = mock(DataServicesCoreProperties.JsonRpc.class);
		objectMapper = new ObjectMapper();

		when(coreProperties.getJsonRpc()).thenReturn(jsonRpcProperties);
		when(jsonRpcProperties.getMaxMethodCallsPerRequest()).thenReturn(100);
	}

	@Test
	public void shouldUseReadOnlyTransactionWhenAllOperationsAreNonMutating() throws IOException {
		when(environment.getProperty(RpcUtils.REPLICA_URL_PROPERTY)).thenReturn(REPLICA_URL);

		JsonRpcOperationDispatcher dispatcher = createDispatcher();
		dispatcher.getOperations().clear();
		dispatcher.getOperations().put("READ_OP", new ReadOnlyOperation());
		dispatcher.getOperations().put("READ_OP2", new ReadOnlyOperation());

		String batch = "[{\"jsonrpc\":\"2.0\",\"method\":\"READ_OP\",\"id\":\"1\"},{\"jsonrpc\":\"2.0\",\"method\":\"READ_OP2\",\"id\":\"2\"}]";
		dispatcher.handleRequest(toInputStream(batch), new ByteArrayOutputStream());

		verify(transactionHandler).runMethodInReadOnlyTransaction(ArgumentMatchers.any());
	}

	@Test
	public void shouldUseReadWriteTransactionWhenAtLeastOneOperationIsMutating() throws IOException {
		when(environment.getProperty(RpcUtils.REPLICA_URL_PROPERTY)).thenReturn(REPLICA_URL);

		JsonRpcOperationDispatcher dispatcher = createDispatcher();
		dispatcher.getOperations().clear();
		dispatcher.getOperations().put("READ_OP", new ReadOnlyOperation());
		dispatcher.getOperations().put("WRITE_OP", new MutatingOperation());

		String batch = "[{\"jsonrpc\":\"2.0\",\"method\":\"READ_OP\",\"id\":\"1\"},{\"jsonrpc\":\"2.0\",\"method\":\"WRITE_OP\",\"id\":\"2\"}]";
		dispatcher.handleRequest(toInputStream(batch), new ByteArrayOutputStream());

		verify(transactionHandler).runMethodInDefaultTransaction(ArgumentMatchers.any());
	}

	@Test
	public void shouldUseReadOnlyTransactionForSingleNonMutatingOperation() throws IOException {
		when(environment.getProperty(RpcUtils.REPLICA_URL_PROPERTY)).thenReturn(REPLICA_URL);

		JsonRpcOperationDispatcher dispatcher = createDispatcher();
		dispatcher.getOperations().clear();
		dispatcher.getOperations().put("READ_OP", new ReadOnlyOperation());

		String single = "{\"jsonrpc\":\"2.0\",\"method\":\"READ_OP\",\"id\":\"1\"}";
		dispatcher.handleRequest(toInputStream(single), new ByteArrayOutputStream());

		verify(transactionHandler).runMethodInReadOnlyTransaction(ArgumentMatchers.any());
	}

	@Test
	public void shouldUseReadWriteTransactionForSingleMutatingOperation() throws IOException {
		when(environment.getProperty(RpcUtils.REPLICA_URL_PROPERTY)).thenReturn(REPLICA_URL);

		JsonRpcOperationDispatcher dispatcher = createDispatcher();
		dispatcher.getOperations().clear();
		dispatcher.getOperations().put("WRITE_OP", new MutatingOperation());

		String single = "{\"jsonrpc\":\"2.0\",\"method\":\"WRITE_OP\",\"id\":\"1\"}";
		dispatcher.handleRequest(toInputStream(single), new ByteArrayOutputStream());

		verify(transactionHandler).runMethodInDefaultTransaction(ArgumentMatchers.any());
	}

	@Test
	public void shouldTreatUnknownOperationAsMutating() throws IOException {
		when(environment.getProperty(RpcUtils.REPLICA_URL_PROPERTY)).thenReturn(REPLICA_URL);

		JsonRpcOperationDispatcher dispatcher = createDispatcher();

		String single = "{\"jsonrpc\":\"2.0\",\"method\":\"UNKNOWN_OP\",\"id\":\"1\"}";
		dispatcher.handleRequest(toInputStream(single), new ByteArrayOutputStream());

		verify(transactionHandler).runMethodInDefaultTransaction(ArgumentMatchers.any());
	}

	@Test
	public void shouldSkipBatchAnalysisWhenReplicaIsNotConfigured() throws IOException {
		when(environment.getProperty(RpcUtils.REPLICA_URL_PROPERTY)).thenReturn(null);

		JsonRpcOperationDispatcher dispatcher = createDispatcher();
		dispatcher.getOperations().clear();
		dispatcher.getOperations().put("READ_OP", new ReadOnlyOperation());

		String single = "{\"jsonrpc\":\"2.0\",\"method\":\"READ_OP\",\"id\":\"1\"}";
		dispatcher.handleRequest(toInputStream(single), new ByteArrayOutputStream());

		verify(transactionHandler).runMethodInDefaultTransaction(ArgumentMatchers.any());
	}

	private JsonRpcOperationDispatcher createDispatcher() {
		return new JsonRpcOperationDispatcher(
			Set.of(DataServicesCoreProperties.MATCH_ALL),
			linkValidator,
			eventPublisher,
			objectMapper,
			coreProperties,
			false,
			false,
			transactionHandler,
			environment
		);
	}

	private ByteArrayInputStream toInputStream(String json) {
		return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
	}
}
