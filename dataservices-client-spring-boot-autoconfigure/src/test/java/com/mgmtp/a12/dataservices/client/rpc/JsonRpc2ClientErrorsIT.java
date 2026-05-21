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

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.client.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.client.rpc.internal.JsonRpc2RequestBuilder;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.exception.RequestIdConflictException;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2ResponseError;

import static com.mgmtp.a12.dataservices.rpc.JsonRpc2ResponseError.INVALID_PARAMS;
import static com.mgmtp.a12.dataservices.rpc.JsonRpc2ResponseError.METHOD_NOT_FOUND;
import static com.mgmtp.a12.dataservices.rpc.JsonRpc2ResponseError.PARSE_ERROR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;


public class JsonRpc2ClientErrorsIT extends AbstractSpringContextIT {

	@Autowired private ObjectMapper objectMapper;

	@BeforeClass public void setUp() throws JsonProcessingException {
		createModelFromFile(CONTRACT_MODEL_FILE);
		createModelFromFile(BUSINESS_PARTNER_MODEL_FILE);
		createModelFromFile(CO_INSURED_ADDITIONAL_FIELD_MODEL_FILE);

		String documentFileContent = readFile(CONTRACT_DOCUMENT);
		createDocumentFromJson(CONTRACT_MODEL_NAME, documentFileContent);
		createDocumentFromJson(CONTRACT_MODEL_NAME, documentFileContent);
		createDocumentFromJson(CONTRACT_MODEL_NAME, documentFileContent);
		createDocumentFromJson(CONTRACT_MODEL_NAME, documentFileContent);

		createModelFromFile(CONTRACT_CO_INSURED_PARTNER_MODEL_FILE);
	}

	@AfterClass public void cleanUp() {
		//we need to clean-up to not affect all other tests
		cleanUpByDocumentModel(BUSINESS_PARTNER_MODEL_NAME);
		cleanUpByDocumentModel(CONTRACT_MODEL_NAME);
		cleanUpByDocumentModel(CO_INSURED_ADDITIONAL_FIELD_MODEL_NAME);

		cleanupRelationshipModel();
		modelsClient.deleteModel(CONTRACT_CO_INSURED_PARTNER_MODEL_NAME);
	}

	@Test(expectedExceptions = RequestIdConflictException.class, expectedExceptionsMessageRegExp = "Request of ID ID1 is in state SUCCESS")
	public void testReuseRequestIdSuccess() throws Exception {
		JsonRpc2RequestBuilder requestBuilder = requestBuilderFactory.newJsonRpc2RequestBuilder();
		String documentFileContent = readFile(CONTRACT_DOCUMENT);
		requestBuilder
			.addMethodCall(CoreOperationConstants.ADD_DOCUMENT_OPERATION)
			.id("AddDocument")
			.putParameter("documentModelName", CONTRACT_MODEL_NAME)
			.putParameter("locale", "en")
			.putParameter("document", objectMapper.readTree(documentFileContent));
		List<JsonRpc2Response> responseList = rpcOperationsClient.invoke("ID1", requestBuilder.build());

		responseList.forEach(res -> {
			Assert.assertNull(res.getError(), String.format("Response [%s] should not contain error", res.getId()));
			Assert.assertNotNull(res.getResult(), String.format("Response [%s] should contain result", res.getId()));
		});

		rpcOperationsClient.invoke("ID1", requestBuilder.build());
	}

	@Test(expectedExceptions = RequestIdConflictException.class, expectedExceptionsMessageRegExp = "Request of ID ID2 is in state FAILED")
	public void testReuseRequestIdFailed() {
		JsonRpc2RequestBuilder requestBuilder = requestBuilderFactory.newJsonRpc2RequestBuilder();
		requestBuilder.addMethodCall("NOT_EXISTING_METHOD")
			.id("NotExistingMethodCall")
			.addParameter("xyz");
		List<JsonRpc2Response> results = rpcOperationsClient.invoke("ID2", requestBuilder.build());

		assertEquals(results.size(), 1);
		JsonRpc2Response response = results.get(0);
		assertFalse(response.isSuccess());

		rpcOperationsClient.invoke("ID2", requestBuilder.build());
	}

	@Test public void testNotExistingMethod() {
		JsonRpc2RequestBuilder requestBuilder = requestBuilderFactory.newJsonRpc2RequestBuilder();
		requestBuilder.addMethodCall("NOT_EXISTING_METHOD")
			.id("id4")
			.addParameter("xyz");
		List<JsonRpc2Response> results = rpcOperationsClient.invoke(requestBuilder.build());

		assertEquals(results.size(), 1);
		JsonRpc2Response response = results.get(0);
		assertFalse(response.isSuccess());
		JsonRpc2ResponseError error = response.getError();
		assertEquals(error.getCode(), METHOD_NOT_FOUND);
		assertEquals(error.getMessage(), "method not found");
	}

	@Test public void testJsonErrorMethodNotFound() throws Exception {
		JsonRpc2RequestBuilder rpcRequest = requestBuilderFactory.newJsonRpc2RequestBuilder();
		rpcRequest.addMethodCall(CoreOperationConstants.DELETE_LINK_OPERATION)
			.id("removeLinkMutation")
			.putParameter("linkAA", objectMapper.readTree("{" +
				"\"linkRefA\": {" +
				"\"linkDescriptorXY\": {\"relationshipModel\": \"ContractCoInsuredPartner\"," +
				"\"entities\": [{\"role\": \"Contract\",\"docRef\": \"%s\"},{\"role\": \"Partner\",\"docRef\": \"%s\"}]" +
				"},\"id\": \"-44\"}}"));
		List<JsonRpc2Response> results = rpcOperationsClient.invoke(rpcRequest.build());

		assertEquals(results.size(), 1);
		JsonRpc2Response response = results.get(0);
		assertFalse(response.isSuccess());
		JsonRpc2ResponseError error = response.getError();
		assertEquals(error.getCode(), INVALID_PARAMS);
		assertEquals(error.getMessage(), "method parameters invalid");
	}

	@Test public void testJsonErrorInvalidParams() {
		JsonRpc2RequestBuilder rpcRequest = requestBuilderFactory.newJsonRpc2RequestBuilder();
		rpcRequest.addMethodCall(CoreOperationConstants.DELETE_LINK_OPERATION)
			.id("removeLinkMutation")
			.addParameter(3);
		List<JsonRpc2Response> results = rpcOperationsClient.invoke(rpcRequest.build());
		assertEquals(results.size(), 1);
		JsonRpc2Response response = results.get(0);
		assertFalse(response.isSuccess());
		JsonRpc2ResponseError error = response.getError();
		assertEquals(error.getCode(), PARSE_ERROR);
		assertEquals(error.getMessage(), "JSON parse error");
	}
}
