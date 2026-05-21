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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.client.rpc.internal.JsonRpc2RequestBuilder;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.test.TestConfiguration;

@ContextConfiguration(classes = { TestConfiguration.class })
@Test public class JsonRpc2RequestBuilderTest extends AbstractTestNGSpringContextTests {

	private static final String DOCUMENT_PROJECTION = "document";
	private static final String QUERY_PARAM = "query";

	@Autowired RequestBuilderFactory rpcRequestBuilderFactory;
	@Autowired private ObjectMapper objectMapper;

	@Test
	public void testBuilder() throws IOException {
		JsonRpc2RequestBuilder rpcRequestBuilder = rpcRequestBuilderFactory.newJsonRpc2RequestBuilder();
		QueryRoot query = QueryRoot.builder()
			.projectionName(DOCUMENT_PROJECTION)
			.targetDocumentModel("testModel")
			.paging(Paging.builder()
				.pageNumber(10)
				.pageSize(100)
				.build())
			.build();

		rpcRequestBuilder.addMethodCall(CoreOperationConstants.QUERY_OPERATION)
			.id("listDocumentsOperation1")
			.putParameter(QUERY_PARAM,
				objectMapper.valueToTree(query));

		rpcRequestBuilder.addMethodCall(CoreOperationConstants.ADD_DOCUMENT_OPERATION)
			.id("addDocOperation1")
			.putParameter("documentModelName", "testModel")
			.putParameter("document", objectMapper.createObjectNode()
				.put("id", "145"))
			.putParameter("locale", Locale.GERMAN);

		JSONAssert.assertEquals(IOUtils.toString(getClass().getResourceAsStream("/rpc-request.json"), StandardCharsets.UTF_8),
			objectMapper.writeValueAsString(rpcRequestBuilder.build()), false);
	}

}
