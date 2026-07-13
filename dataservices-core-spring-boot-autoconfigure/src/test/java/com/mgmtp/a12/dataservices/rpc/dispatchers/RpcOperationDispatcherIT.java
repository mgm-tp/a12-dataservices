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
package com.mgmtp.a12.dataservices.rpc.dispatchers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.LinkTestUtils;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.document.operation.internal.AddDocumentOperation;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.relationship.OffsetBasedPageRequest;
import com.mgmtp.a12.dataservices.relationship.operation.internal.AddLinkOperation;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2ResponseError;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.MissingNode;

import static com.mgmtp.a12.dataservices.rpc.internal.JsonRpcOperationDispatcher.RPC_ERROR_MESSAGE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.testng.AssertJUnit.assertTrue;

public class RpcOperationDispatcherIT extends AbstractDispatcherClass {

	public static final String SPEL_ALLOWED_FIELD = "spelAllowed";
	public static final String ALLOWED_OPERATIONS_FIELD = "allowedOperations";
	public static final String OPERATIONS_FIELD = "operations";

	@Autowired private AddDocumentOperation addDocumentOperation;
	@Autowired private AddLinkOperation addLinkOperation;

	@DataProvider
	public static Object[][] docRefs() {
		return new Object[][] {
			{ "#addProductDocument.invalidValuePath" },
			{ "#invalidId.docRef" },
		};
	}

	@DataProvider
	public static Object[][] invalidCharactersInRequest() {
		return new Object[][] {
			{ "NUL char in param", "request-with-docref-containing-NUL-character.txt" },
			{ "NUL char in requestId", "request-with-id-containing-NUL-character.txt" }
		};
	}

	@DataProvider
	public static Object[][] validSpecialCharactersInRequest() {
		return new Object[][] {
			{ "Control char in param", "request-with-docref-containing-control-char-xc0x81.txt" },
			{ "Emoji in param", "request-with-docref-containing-emoji.txt" }
		};
	}

	@Test(dataProvider = "docRefs")
	public void dispatchRpcRequest(String docRef) throws IOException {
		JsonRpc2Response response = dispatchRpcRequestInternal(docRef).get(1);
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals(createOperationError(response).getShortMessage().getKey(), ExceptionKeys.RPC_OPERATION_ERROR_KEY);
		Assert.assertEquals(createOperationError(response).getShortMessage().getDefaultMessage(), RPC_ERROR_MESSAGE);
		Assert.assertEquals(createOperationError(response).getLongMessage().getDefaultMessage(), "SpEL evaluation error occurred!");
	}

	@Test
	public void validateInvalidPlaceholder() throws IOException {
		JsonRpc2Response response = dispatchRpcRequestInternal("#invalidPlaceholder").get(1);
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals(createOperationError(response).getShortMessage().getKey(), "rpc.operation.error");
		Assert.assertEquals(createOperationError(response).getShortMessage().getDefaultMessage(), "JSON-RPC Request failed and rollback was performed");
		Assert.assertEquals(createOperationError(response).getLongMessage().getKey(), "error.convert.json");
		Assert.assertEquals(createOperationError(response).getLongMessage().getDefaultMessage(), "Parameter deserialization error occurred!");
	}

	@Test(dataProvider = "invalidCharactersInRequest")
	public void notAllowedCharacters(String description, String fileName) throws IOException {
		ByteArrayOutputStream response = new ByteArrayOutputStream();
		rpcOperationDispatcher.handleRequest(FileUtils.openInputStream(new File("src/test/resources/rpc/dispatcher/" + fileName)), response);
		String output = response.toString();
		assertTrue(output != null && output.contains("NUL character is not allowed in requests"));
	}

	@Test(dataProvider = "validSpecialCharactersInRequest")
	public void allowedSpecialCharacters(String description, String fileName) throws IOException {
		ByteArrayOutputStream response = new ByteArrayOutputStream();
		rpcOperationDispatcher.handleRequest(FileUtils.openInputStream(new File("src/test/resources/rpc/dispatcher/" + fileName)), response);
		String output = response.toString();
		assertTrue(output != null && !output.contains("NUL character is not allowed in requests"));
	}

	@Test
	public void addDocumentAndReferenceItToDelete() throws IOException {
		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "request_add_and_delete_document.json");
		List<JsonRpc2Response> responses = sendRpcRequest(request);
		JsonNode json = objectMapper.valueToTree(responses);
		assertTrue(json.isArray());
		assertThat(json.get(0).at("/error"), is(MissingNode.getInstance()));
		assertThat(json.get(0).at("/result/docRef").toString(), matchesPattern("\"Product/\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}\""));
		assertThat(json.get(1).at("/error"), is(MissingNode.getInstance()));
	}

	@Transactional
	@Test public void checkAddLinkAndReferencedDocument() throws IOException {
		List<JsonRpc2Response> dispatcherResults = dispatchRpcRequestInternal("#addProductDocument.metadata.docRef");
		RelationshipLinkSpec link = objectMapper.treeToValue(dispatcherResults.get(1).getResult(), RelationshipLinkSpec.class);

		LinkTestUtils.assertProductCampaignLinkRef(RelationshipModelConstants.PRODUCT_CAMPAIGN_RM, link);
		Assert.assertEquals(link.getLinkDescriptor().getEntities().get(1).getDocRef(), campaign1DocRef);

		DocumentReference resultAddDocument =
			new DocumentReference(new JSONObject(dispatcherResults.getFirst().getResult().toString()).get("docRef").toString());
		List<? extends RelationshipLink> linksOfNewlyCreatedDocument = relationshipLinkRepository.findAllByRoleDocRef(
			List.of(resultAddDocument),
			OffsetBasedPageRequest.unpaged()
		).getContent();

		Assert.assertEquals(linksOfNewlyCreatedDocument.size(), 1);

		Assert.assertEquals(linksOfNewlyCreatedDocument.getFirst().getRoles().get(RelationshipModelConstants.RoleConstants.PRODUCT_ROLE).getDocRef(),
			resultAddDocument);
		Assert.assertEquals(linksOfNewlyCreatedDocument.getFirst().getRoles().get(RelationshipModelConstants.RoleConstants.CAMPAIGN_ROLE).getDocRef(),
			campaign1DocRef);
	}

	private List<JsonRpc2Response> dispatchRpcRequestInternal(String replacedValue) throws IOException {
		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "request_add_link_and_referenced_document.json");
		request = request.formatted("addProductDocument", replacedValue, campaign1DocRef);
		return sendRpcRequest(request);
	}

	@Test
	public void checkSpecificErrorHandling() throws Exception {
		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "request_bad.json");
		request = request.formatted(product2DocRef.getDocumentId(), campaign1DocRef.getDocumentId(), product2DocRef.getDocumentId());
		JsonRpc2Response response = sendRpcRequest(request).getFirst();
		Assert.assertEquals(response.getError().getCode(), JsonRpc2ResponseError.METHOD_NOT_FOUND);
		Assert.assertEquals(response.getError().getMessage(), "method not found");
	}

	@Test
	public void assertSpelNotFiring() throws IOException {
		runWithFieldOverwritten(SPEL_ALLOWED_FIELD, false, rpcOperationDispatcher, () -> {
			JsonRpc2Response response = dispatchRpcRequestInternal("#addProductDocument.metadata.docRef").get(1);
			Assert.assertFalse(response.isSuccess());
			Assert.assertNotNull(response.getError());
		});
	}

	@Test
	public void assertOperationValidationFires() throws IOException {
		runWithFieldOverwritten(ALLOWED_OPERATIONS_FIELD, Collections.emptySet(), rpcOperationDispatcher, () ->
			runWithFieldOverwritten(OPERATIONS_FIELD, Collections.emptyMap(), rpcOperationDispatcher, () -> {
				List<JsonRpc2Response> responses = dispatchRpcRequestInternal("#addProductDocument.metadata.docRef");
				responses.forEach(e -> {
					Assert.assertFalse(e.isSuccess());
					Assert.assertEquals(e.getError().getMessage(), "method not found");
					Assert.assertEquals(e.getError().getCode(), JsonRpc2ResponseError.METHOD_NOT_FOUND);
				});
			}));
	}

	@Test
	public void assertAllowedOperationGroupAccessDenied() throws IOException {
		runWithFieldOverwritten(ALLOWED_OPERATIONS_FIELD, Set.of(CoreOperationConstants.A12_INTERNAL_OPERATIONS_GROUP), rpcOperationDispatcher,
			() -> runWithFieldOverwritten(OPERATIONS_FIELD, Collections.emptyMap(),
				rpcOperationDispatcher,
				() -> {
					List<JsonRpc2Response> responses = dispatchRpcRequestInternal("#addProductDocument.metadata.docRef");
					responses.forEach(e -> Assert.assertFalse(e.isSuccess()));
				}));
	}

	@Test
	public void assertAllowedOperationGroupAccessAllowed() throws IOException {
		runWithFieldOverwritten(ALLOWED_OPERATIONS_FIELD,
			Set.of(
				CoreOperationConstants.A12_INTERNAL_OPERATIONS_GROUP,
				CoreOperationConstants.DOCUMENT_OPERATIONS_GROUP,
				CoreOperationConstants.LINK_OPERATIONS_GROUP
			),
			rpcOperationDispatcher,
			() -> runWithFieldOverwritten(OPERATIONS_FIELD,
				Map.of(
					CoreOperationConstants.ADD_DOCUMENT_OPERATION, addDocumentOperation,
					CoreOperationConstants.ADD_LINK_OPERATION, addLinkOperation
				),
				rpcOperationDispatcher,
				() -> {
					List<JsonRpc2Response> responses = dispatchRpcRequestInternal("#addProductDocument.metadata.docRef");
					responses.forEach(e -> Assert.assertTrue(e.isSuccess()));
				}));
	}
}
