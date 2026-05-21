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

import java.util.List;

import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mgmtp.a12.dataservices.client.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.client.rpc.internal.JsonRpc2RequestBuilder;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;

public class JsonRpc2ClientDocumentIT extends AbstractSpringContextIT {

	@BeforeClass
	public void setUp() {
		createModelFromFile(CONTRACT_MODEL_FILE);
	}

	@AfterClass
	public void cleanUp() {
		cleanUpByDocumentModel(CONTRACT_MODEL_NAME);
	}

	@Test
	public void createDocumentTest() throws JsonProcessingException {
		String documentFileContent = readFile(CONTRACT_DOCUMENT);
		JsonRpc2RequestBuilder rpcRequest = requestBuilderFactory.newJsonRpc2RequestBuilder();

		rpcRequest
				.addMethodCall(CoreOperationConstants.ADD_DOCUMENT_OPERATION)
				.id("AddDocument")
				.putParameter("documentModelName", CONTRACT_MODEL_NAME)
				.putParameter("locale", "en")
				.putParameter("document", objectMapper.readTree(documentFileContent));

		rpcRequest
				.addMethodCall(CoreOperationConstants.GET_DOCUMENT_OPERATION)
				.id("GetDocument")
				.putParameter("docRef", "#{#AddDocument.metadata.docRef}");

		List<JsonRpc2Response> responseList = rpcOperationsClient.invoke(rpcRequest.build());

		responseList.forEach(res -> {
			Assert.assertNull(res.getError(), String.format("Response [%s] should not contain error", res.getId()));
			Assert.assertNotNull(res.getResult(), String.format("Response [%s] should contain result", res.getId()));
		});


		responseList.stream().filter(e ->  "GetDocument".equals(e.getId())).findFirst().ifPresent(e -> {
			JSONAssert.assertEquals(documentFileContent, e.getResult().get("document").toPrettyString(), false);
		});
	}
}
