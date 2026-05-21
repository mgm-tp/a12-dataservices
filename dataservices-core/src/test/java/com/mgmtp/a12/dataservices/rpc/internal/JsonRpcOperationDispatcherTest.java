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

import java.util.Set;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipLinkValidationListener;
import com.mgmtp.a12.dataservices.rpc.RpcException;
import com.mgmtp.a12.dataservices.utils.internal.JsonUtils;

@Listeners(MockitoTestNGListener.class)
public class JsonRpcOperationDispatcherTest {

	@Mock private RelationshipLinkValidationListener linkValidator;
	@Mock private ApplicationEventPublisher applicationEventPublisher;
	@Spy private ObjectMapper objectMapper = new ObjectMapper();
	@Mock private DataServicesCoreProperties dataServicesCoreProperties;
	@Mock private DataServicesCoreProperties.JsonRpc jsonRpcProperties;
	@Mock private JsonUtils jsonUtils;

	@BeforeTest
	public void setUp() {
		MockitoAnnotations.openMocks(this);

		Mockito.when(dataServicesCoreProperties.getJsonRpc()).thenReturn(jsonRpcProperties);
	}

	@Test public void testPreHandleJson_correctJson() {
		Mockito.when(dataServicesCoreProperties.getJsonRpc()).thenReturn(jsonRpcProperties);
		Mockito.when(jsonRpcProperties.getMaxMethodCallsPerRequest()).thenReturn(10);
		ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
		arrayNode.add(TextNode.valueOf("element1"));
		arrayNode.add(TextNode.valueOf("element2"));
		arrayNode.add(TextNode.valueOf("element3"));
		arrayNode.add(TextNode.valueOf("element4"));

		prepareJsonRpcOperationDispatcher().preHandleJson(arrayNode);
	}

	@Test(expectedExceptions = RpcException.class) public void testPreHandleJson_maxNumberOfOperationExceeded() {
		Mockito.when(dataServicesCoreProperties.getJsonRpc()).thenReturn(jsonRpcProperties);
		Mockito.when(jsonRpcProperties.getMaxMethodCallsPerRequest()).thenReturn(1);
		ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
		arrayNode.add(TextNode.valueOf("element1"));
		arrayNode.add(TextNode.valueOf("element2"));
		arrayNode.add(TextNode.valueOf("element3"));
		arrayNode.add(TextNode.valueOf("element4"));

		prepareJsonRpcOperationDispatcher().preHandleJson(arrayNode);
	}

	private JsonRpcOperationDispatcher prepareJsonRpcOperationDispatcher() {
		return new JsonRpcOperationDispatcher(
			Set.of(DataServicesCoreProperties.MATCH_ALL),
			linkValidator,
			applicationEventPublisher,
			objectMapper,
			dataServicesCoreProperties,
			false,
			jsonUtils,
			false
		);

	}
}
