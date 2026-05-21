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
package com.mgmtp.a12.dataservices.rpc;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.operation.validate.DocumentValidationError;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;

public class RpcValidateDocumentOperationIT extends AbstractSpringContextIT {

	private static final String VALIDATION_DOCUMENT_FOLDER = PathConstants.RPC_PATH + "validate/";
	private static final String VALIDATION_REQUEST_TEMPLATE = PathConstants.RPC_PATH + "templates/document_validate_template.json";

	private static final String VALIDATION_VALID_FULL_DOCUMENT = VALIDATION_DOCUMENT_FOLDER + "/valid_document.json";
	private static final String VALIDATION_INVALID_FULL_DOCUMENT = VALIDATION_DOCUMENT_FOLDER + "/invalid_document.json";
	private static final String VALIDATION_VALID_PARTIAL_DOCUMENT = VALIDATION_DOCUMENT_FOLDER + "/valid_partial_document.json";
	private static final String VALIDATION_INVALID_PARTIAL_DOCUMENT = VALIDATION_DOCUMENT_FOLDER + "/invalid_partial_document.json";

	@BeforeClass
	public void setUp() throws Exception {
		setUserTo(UserConstants.ADMIN_USER);
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
	}

	@Test
	public void testValidFullDocument() throws IOException {
		JSONArray jsonArray = initializeRpcRequest(false, VALIDATION_VALID_FULL_DOCUMENT);
		List<DocumentValidationError> validationErrors = dispatchAndAssertRequest(jsonArray.toString());
		Assert.assertTrue(validationErrors.isEmpty());
	}

	@Test
	public void testInvalidFullDocument() throws IOException {
		JSONArray jsonArray = initializeRpcRequest(false, VALIDATION_INVALID_FULL_DOCUMENT);
		List<DocumentValidationError> validationErrors = dispatchAndAssertRequest(jsonArray.toString());
		Assert.assertEquals(validationErrors.size(), 2);

		DocumentValidationError firstMessage = validationErrors.get(0);
		Assert.assertEquals(firstMessage.getReferencedFields().size(), 8);
		Assert.assertEquals(firstMessage.getMessageType(), IMessage.MessageType.OMISSION_ERROR.name());

		DocumentValidationError secondMessage = validationErrors.get(1);
		Assert.assertEquals(secondMessage.getReferencedFields().size(), 8);
		Assert.assertEquals(secondMessage.getMessageType(), IMessage.MessageType.OMISSION_ERROR.name());
	}

	@Test
	public void testInvalidFullDocumentWithFlag() throws IOException {
		JSONObject documentObject = new JSONObject(loadResourceFromClasspathAsString(VALIDATION_INVALID_FULL_DOCUMENT));
		documentObject.getJSONObject("BusinessPartnerRoot").getJSONArray("Attachment").getJSONObject(0).remove("attachment_id");

		JSONArray jsonArray = initializeRpcRequest(false, documentObject);
		List<DocumentValidationError> validationErrors = dispatchAndAssertRequest(jsonArray.toString());
		Assert.assertEquals(validationErrors.size(), 2);

		DocumentValidationError firstMessage = validationErrors.get(0);
		Assert.assertEquals(firstMessage.getReferencedFields().size(), 8);
		Assert.assertEquals(firstMessage.getMessageType(), IMessage.MessageType.OMISSION_ERROR.name());

		DocumentValidationError secondMessage = validationErrors.get(1);
		Assert.assertEquals(secondMessage.getReferencedFields().size(), 8);
		Assert.assertEquals(secondMessage.getMessageType(), IMessage.MessageType.OMISSION_ERROR.name());
	}

	@Test
	public void testEmptyFullDocument() throws IOException {
		JSONArray jsonArray = initializeRpcRequest(false, new JSONObject());
		List<DocumentValidationError> validationErrors = dispatchAndAssertRequest(jsonArray.toString());
		Assert.assertEquals(validationErrors.size(), 3);

		validationErrors.forEach(validationError -> {
			Assert.assertEquals(validationError.getReferencedFields().size(), 1);
			Assert.assertEquals(validationError.getMessageType(), IMessage.MessageType.OMISSION_ERROR.name());

			String base = "/BusinessPartnerRoot[1]";
			Assert.assertListContainsObject(List.of(base + "/Name[1]", base + "/Industry[1]", base + "/CustomerDiscount[1]"), validationError.getReferencedFields().get(0), "Field is not containing any of expected value");
		});
	}

	@Test
	public void testEmptyPartialDocument() throws IOException {
		JSONArray jsonArray = initializeRpcRequest(true, new JSONObject());
		List<DocumentValidationError> validationErrors = dispatchAndAssertRequest(jsonArray.toString());
		Assert.assertTrue(validationErrors.isEmpty());
	}

	@Test
	public void testValidPartialDocument() throws IOException {
		JSONArray requestObject = initializeRpcRequest(true, VALIDATION_VALID_PARTIAL_DOCUMENT);
		List<DocumentValidationError> validationErrors = dispatchAndAssertRequest(requestObject.toString());
		Assert.assertTrue(validationErrors.isEmpty());
	}

	@Test
	public void testInvalidPartialDocument() throws IOException {
		JSONArray requestObject = initializeRpcRequest(true, VALIDATION_INVALID_PARTIAL_DOCUMENT);
		List<DocumentValidationError> validationErrors = dispatchAndAssertRequest(requestObject.toString());
		Assert.assertEquals(validationErrors.size(), 1);

		DocumentValidationError firstMessage = validationErrors.get(0);
		Assert.assertEquals(firstMessage.getReferencedFields().size(), 8);
		Assert.assertEquals(firstMessage.getMessageType(), IMessage.MessageType.OMISSION_ERROR.name());
	}

	private JSONObject getRequestParameters(JSONObject requestObject) {
		return requestObject.getJSONObject("params");
	}

	private JSONArray initializeRpcRequest(Boolean isPartial, String documentPath) {
		JSONArray requestArray = new JSONArray(loadResourceFromClasspathAsString(VALIDATION_REQUEST_TEMPLATE));
		JSONObject requestObject = requestArray.getJSONObject(0);
		getRequestParameters(requestObject).put("documentModelName", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);
		getRequestParameters(requestObject).put("partial", isPartial);
		getRequestParameters(requestObject).put("document", new JSONObject(loadResourceFromClasspathAsString(documentPath)));
		return requestArray;
	}

	private JSONArray initializeRpcRequest(Boolean isPartial, JSONObject documentObject) {
		JSONArray requestArray = new JSONArray(loadResourceFromClasspathAsString(VALIDATION_REQUEST_TEMPLATE));
		JSONObject requestObject = requestArray.getJSONObject(0);
		getRequestParameters(requestObject).put("documentModelName", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);
		getRequestParameters(requestObject).put("partial", isPartial);
		getRequestParameters(requestObject).put("document", documentObject);
		return requestArray;
	}

	private List<DocumentValidationError> dispatchAndAssertRequest(String request) throws IOException {
		List<JsonRpc2Response> results = sendRpcRequest(request);
		Assert.assertNotNull(results);
		Assert.assertEquals(results.size(), 1);
		return Arrays.asList(objectMapper.treeToValue(results.get(0).getResult(), DocumentValidationError[].class));
	}
}
