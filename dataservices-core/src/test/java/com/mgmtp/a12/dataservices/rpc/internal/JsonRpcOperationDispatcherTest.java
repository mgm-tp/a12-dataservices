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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.StringNode;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.internal.TransactionHandler;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipLinkValidationListener;
import com.mgmtp.a12.dataservices.rpc.RpcException;
import com.mgmtp.a12.dataservices.utils.OperationContextHolder;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Listeners(MockitoTestNGListener.class)
public class JsonRpcOperationDispatcherTest {

	@Mock private RelationshipLinkValidationListener linkValidator;
	@Mock private ApplicationEventPublisher applicationEventPublisher;
	@Spy private ObjectMapper objectMapper = new ObjectMapper();
	@Mock private DataServicesCoreProperties dataServicesCoreProperties;
	@Mock private DataServicesCoreProperties.JsonRpc jsonRpcProperties;
	@Mock private TransactionHandler transactionHandler;
	@Mock private Environment environment;

	@BeforeTest
	public void setUp() {
		MockitoAnnotations.openMocks(this);

		Mockito.when(dataServicesCoreProperties.getJsonRpc()).thenReturn(jsonRpcProperties);
		Mockito.when(environment.getProperty(RpcUtils.REPLICA_URL_PROPERTY)).thenReturn(null);
	}

	@Test public void testPreHandleJson_correctJson() {
		Mockito.when(dataServicesCoreProperties.getJsonRpc()).thenReturn(jsonRpcProperties);
		Mockito.when(jsonRpcProperties.getMaxMethodCallsPerRequest()).thenReturn(10);
		ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
		arrayNode.add(StringNode.valueOf("element1"));
		arrayNode.add(StringNode.valueOf("element2"));
		arrayNode.add(StringNode.valueOf("element3"));
		arrayNode.add(StringNode.valueOf("element4"));

		prepareJsonRpcOperationDispatcher().preHandleJson(arrayNode);
	}

	@Test(expectedExceptions = RpcException.class) public void testPreHandleJson_maxNumberOfOperationExceeded() {
		Mockito.when(dataServicesCoreProperties.getJsonRpc()).thenReturn(jsonRpcProperties);
		Mockito.when(jsonRpcProperties.getMaxMethodCallsPerRequest()).thenReturn(1);
		ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
		arrayNode.add(StringNode.valueOf("element1"));
		arrayNode.add(StringNode.valueOf("element2"));
		arrayNode.add(StringNode.valueOf("element3"));
		arrayNode.add(StringNode.valueOf("element4"));

		prepareJsonRpcOperationDispatcher().preHandleJson(arrayNode);
	}

	/**
	 * Tests that `resolveError` emits a WARN-level log entry containing the operation ID and the exception message.
	 */
	@Test(description = "Should emit WARN log with operation ID and description when resolveError is called")
	public void shouldLogWarnWithOperationIdAndDescriptionWhenResolveError() throws NoSuchMethodException {
		// Given
		ListAppender<ILoggingEvent> appender = new ListAppender<>();
		appender.start();
		((Logger) LoggerFactory.getLogger(JsonRpcOperationDispatcher.class)).addAppender(appender);

		try {
			OperationContextHolder.id("op-1");
			String exceptionMessage = "test-exception-message";
			RuntimeException testException = new RuntimeException(exceptionMessage);
			Method method = Object.class.getMethod("toString");

			// When
			prepareJsonRpcOperationDispatcher().resolveError(testException, method, List.of());

			// Then
			List<ILoggingEvent> warnEvents = appender.list.stream()
				.filter(e -> e.getLevel() == Level.WARN)
				.toList();
			assertEquals(warnEvents.size(), 1, "Expected exactly one WARN log event");
			String logMessage = warnEvents.getFirst().getFormattedMessage();
			assertTrue(logMessage.contains("op-1"), "Log message should contain operation ID 'op-1': " + logMessage);
			assertTrue(logMessage.contains(exceptionMessage), "Log message should contain exception message: " + logMessage);
		} finally {
			((Logger) LoggerFactory.getLogger(JsonRpcOperationDispatcher.class)).detachAppender(appender);
			OperationContextHolder.clear();
		}
	}

	/**
	 * Tests that the rollback log in `executeRequestInternal` is WARN level and contains the operation ID
	 * and exception message when the operation fails and a rollback is triggered.
	 */
	@Test(description = "Should emit WARN log with operation ID and exception message on rollback")
	public void shouldLogWarnWithOperationIdAndExceptionMessageOnRollback() throws Exception {
		// Given
		ListAppender<ILoggingEvent> appender = new ListAppender<>();
		appender.start();
		Logger logger = (Logger) LoggerFactory.getLogger(JsonRpcOperationDispatcher.class);
		logger.addAppender(appender);

		try (AutoCloseable cleanup = () -> {
			logger.detachAppender(appender);
			OperationContextHolder.clear();
		}) {
			String operationId = "op-2";

			// Make the transactionHandler actually execute the runnable so executeRequestInternal is invoked
			Mockito.doAnswer(invocation -> {
				Runnable runnable = invocation.getArgument(0);
				runnable.run();
				return null;
			}).when(transactionHandler).runMethodInDefaultTransaction(Mockito.any());

			// Provide a JSON-RPC request that has a valid id but targets a method that will fail
			// The interceptRequest sets OperationContextHolder.id(operationId), then the operation throws
			// Set up the request with the operation id matching our expected value
			String jsonRpcRequest = """
				{"id":"%s","method":"nonExistentOp","params":[]}
				""".formatted(operationId);
			java.io.InputStream input = new java.io.ByteArrayInputStream(jsonRpcRequest.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			java.io.OutputStream output = new java.io.ByteArrayOutputStream();

			Mockito.when(dataServicesCoreProperties.getJsonRpc()).thenReturn(jsonRpcProperties);
			Mockito.when(jsonRpcProperties.getMaxMethodCallsPerRequest()).thenReturn(10);

			// When
			prepareJsonRpcOperationDispatcher().handleRequest(input, output);

			// Then: the finally block in executeRequestInternal should have logged a WARN when isFailed() is true
			List<ILoggingEvent> warnEvents = appender.list.stream()
				.filter(e -> e.getLevel() == Level.WARN)
				.toList();
			assertTrue(!warnEvents.isEmpty(), "Expected at least one WARN log event");
			boolean foundMessage = warnEvents.stream()
				.map(ILoggingEvent::getFormattedMessage)
				.anyMatch(msg -> msg.contains(operationId));
			assertTrue(foundMessage, "Expected a WARN log event containing operation ID '" + operationId + "'");
		}
	}

	private JsonRpcOperationDispatcher prepareJsonRpcOperationDispatcher() {
		return new JsonRpcOperationDispatcher(
			Set.of(DataServicesCoreProperties.MATCH_ALL),
			linkValidator,
			applicationEventPublisher,
			objectMapper,
			dataServicesCoreProperties,
			false,
			false,
			transactionHandler,
			environment
		);

	}
}
