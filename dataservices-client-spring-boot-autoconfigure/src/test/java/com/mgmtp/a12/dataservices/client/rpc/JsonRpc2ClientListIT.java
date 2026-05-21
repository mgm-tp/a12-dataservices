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

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.springframework.util.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.dataservices.client.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.client.rpc.internal.JsonRpc2RequestBuilder;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonRpc2ClientListIT extends AbstractSpringContextIT {

	private static final String DOCUMENT_PROJECTION = "document";
	private static final String METADATA_DOCREF_PATH = "/__meta/docRef";
	private static final String PARTNER_ROLE = "Partner";
	private static final String QUERY_PARAM = "query";
	private DocumentReference contract;
	private DocumentReference businessPartner1;
	private DocumentReference businessPartner2;
	private String relationId1;
	private String relationId2;
	private LinkDescriptor linkDescriptor1;
	private LinkDescriptor linkDescriptor2;

	@BeforeClass
	public void setUp() throws Exception {
		findDocumentModelById(CONTRACT_MODEL_NAME).ifPresent(m -> cleanUpByDocumentModel(CONTRACT_MODEL_NAME));
		findDocumentModelById(CONTRACT_MODEL_NAME).ifPresent(m -> cleanUpByDocumentModel(BUSINESS_PARTNER_MODEL_NAME));
		findDocumentModelById(CONTRACT_MODEL_NAME).ifPresent(m -> cleanUpByDocumentModel(BUSINESS_PARTNER_SUPER_MODEL_NAME));
		findDocumentModelById(CONTRACT_MODEL_NAME).ifPresent(m -> cleanUpByDocumentModel(CO_INSURED_ADDITIONAL_FIELD_MODEL_NAME));
		cleanupRelationshipModel();

		createModelFromFile(CONTRACT_MODEL_FILE);
		createModelFromFile(BUSINESS_PARTNER_MODEL_FILE);
		createModelFromFile(BUSINESS_PARTNER_SUPER_MODEL_FILE);
		createModelFromFile(CO_INSURED_ADDITIONAL_FIELD_MODEL_FILE);

		contract = createDocumentFromJson(CONTRACT_MODEL_NAME, readFile(CONTRACT_DOCUMENT));
		businessPartner1 = createDocumentFromJson(BUSINESS_PARTNER_MODEL_NAME, readFile(BUSINESS_PARTNER_1_DOCUMENT));

		String documentFileContent = readFile(CONTRACT_DOCUMENT);
		createDocumentFromJson(CONTRACT_MODEL_NAME, documentFileContent);
		createDocumentFromJson(CONTRACT_MODEL_NAME, documentFileContent);
		createDocumentFromJson(CONTRACT_MODEL_NAME, documentFileContent);
		createDocumentFromJson(CONTRACT_MODEL_NAME, documentFileContent);

		createDocumentFromJson(BUSINESS_PARTNER_MODEL_NAME, readFile(BUSINESS_PARTNER_2_DOCUMENT));

		documentFileContent = readFile(BUSINESS_PARTNER_1_DOCUMENT);
		businessPartner2 = createDocumentFromJson(BUSINESS_PARTNER_MODEL_NAME, readFile(BUSINESS_PARTNER_1_DOCUMENT));
		createDocumentFromJson(BUSINESS_PARTNER_MODEL_NAME, documentFileContent);
		createDocumentFromJson(BUSINESS_PARTNER_MODEL_NAME, documentFileContent);
		createDocumentFromJson(BUSINESS_PARTNER_MODEL_NAME, documentFileContent);

		modelsClient.createModel(new StringReader(readFile(CONTRACT_CO_INSURED_PARTNER_MODEL_FILE)));

		JsonRpc2RequestBuilder rpcRequest = requestBuilderFactory.newJsonRpc2RequestBuilder();
		linkDescriptor1 = new LinkDescriptor(CONTRACT_CO_INSURED_PARTNER_MODEL_NAME, Arrays.asList(
			new RelationshipRoleSpec("Contract", contract),
			new RelationshipRoleSpec("Partner", businessPartner1)));
		rpcRequest.addMethodCall(CoreOperationConstants.ADD_LINK_OPERATION)
			.id("addPartner1ToContract")
			.putParameter("linkDescriptor", linkDescriptor1)
			.putParameter("linkDocument", objectMapper.createObjectNode().set("CoInsuredRoot", objectMapper.createObjectNode()
				.put("Name", "1234")));
		linkDescriptor2 = new LinkDescriptor(CONTRACT_CO_INSURED_PARTNER_MODEL_NAME, Arrays.asList(
			new RelationshipRoleSpec("Contract", contract),
			new RelationshipRoleSpec("Partner", businessPartner2)));
		rpcRequest.addMethodCall(CoreOperationConstants.ADD_LINK_OPERATION)
			.id("addPartner2ToContract")
			.putParameter("linkDescriptor", linkDescriptor2)
			.putParameter("linkDocument", objectMapper.createObjectNode().set("CoInsuredRoot", objectMapper.createObjectNode()
				.put("Name", "12340")));
		handleError(rpcOperationsClient.invoke(rpcRequest.build()));

	}

	@AfterMethod
	public void cleanUp() {
		//we need to clean-up to not affect all other tests

		JsonRpc2RequestBuilder rpcRequestBuilder = requestBuilderFactory.newJsonRpc2RequestBuilder();
		rpcRequestBuilder.addMethodCall(CoreOperationConstants.DELETE_LINK_OPERATION)
			.id("removeLinkMutation")
			.putParameter("linkRef", new RelationshipLinkSpec(linkDescriptor1, relationId1));
		rpcRequestBuilder.addMethodCall(CoreOperationConstants.DELETE_LINK_OPERATION)
			.id("removeLinkMutation2")
			.putParameter("linkRef", new RelationshipLinkSpec(linkDescriptor2, relationId2));
		handleError(rpcOperationsClient.invoke(rpcRequestBuilder.build()));

		cleanUpByDocumentModel(CONTRACT_MODEL_NAME);
		cleanUpByDocumentModel(BUSINESS_PARTNER_MODEL_NAME);
		cleanUpByDocumentModel(BUSINESS_PARTNER_SUPER_MODEL_NAME);
		cleanUpByDocumentModel(CO_INSURED_ADDITIONAL_FIELD_MODEL_NAME);
		cleanupRelationshipModel();
		modelsClient.deleteModel(CONTRACT_CO_INSURED_PARTNER_MODEL_NAME);
	}

	@Test
	public void checkListOperation() throws JsonProcessingException {
		JsonRpc2RequestBuilder rpcRequest = requestBuilderFactory.newJsonRpc2RequestBuilder();
		QueryRoot queryListLinks = QueryRoot.builder()
			.projectionName(DOCUMENT_PROJECTION)
			.targetDocumentModel(CONTRACT_MODEL_NAME)
			.constraint(ExactMatchOperator.builder()
				.field(METADATA_DOCREF_PATH)
				.value(contract.toString())
				.build())
			.exclude(true)
			.paging(Paging.builder()
				.pageNumber(0)
				.pageSize(100)
				.build())
			.links(List.of(QueryLink.builder()
				.relationshipModel(CONTRACT_CO_INSURED_PARTNER_MODEL_NAME)
				.targetRole(PARTNER_ROLE)
				.build()))
			.build();
		rpcRequest.addMethodCall(CoreOperationConstants.QUERY_OPERATION)
			.id("PartnerOfContractRole")
			.putParameter(QUERY_PARAM,
				objectMapper.valueToTree(queryListLinks));
		List<JsonRpc2Response> operationResults = rpcOperationsClient.invoke(rpcRequest.build());

		JsonNode tree = objectMapper.valueToTree(operationResults);
		relationId1 = tree.get(0).at("/result/links/3/linkId").asText();
		relationId2 = tree.get(0).at("/result/links/2/linkId").asText();

		List<DocumentTreeResult> links = Arrays.stream(objectMapper.readValue(tree.get(0).at("/result/links").toString(), DocumentTreeResult[].class))
			.filter(documentTreeResult -> documentTreeResult.getType() == DocumentTreeNodeType.LINK)
			.toList();

		Assert.assertNotNull(relationId1);
		Assert.assertNotNull(relationId2);
		Assert.assertFalse(CollectionUtils.isEmpty(links));
		Assert.assertEquals(links.size(), 2);
		Assert.assertTrue(links.stream().anyMatch(documentTreeResult -> relationId1.equals(documentTreeResult.getLinkId())));
		Assert.assertTrue(links.stream().anyMatch(documentTreeResult -> relationId2.equals(documentTreeResult.getLinkId())));
	}
}
