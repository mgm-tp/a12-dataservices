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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.client.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.client.rpc.internal.JsonRpc2RequestBuilder;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;

public class JsonRpc2ClientQueryIT extends AbstractSpringContextIT {

	private static final String DOCUMENT_PROJECTION = "document";
	private static final String QUERY_PARAM = "query";

	@BeforeClass void init() {
		createModelFromFile(BUSINESS_PARTNER_MODEL_FILE);
		createModelFromFile(BUSINESS_PARTNER_SUPER_MODEL_FILE);
		createModelFromFile(CONTRACT_MODEL_FILE);
		createModelFromFile(CONTRACT_CO_INSURED_ADDITIONAL_FIELDS_MODEL_FILE);
		createModelFromFile(CONTRACT_CO_INSURED_PARTNER_MODEL_FILE);
		createDocumentFromJson(CONTRACT_MODEL_NAME, readFile(CONTRACT_DOCUMENT));
	}

	@Test
	public void testQueryAPI() {
		JsonRpc2RequestBuilder rpcRequest = requestBuilderFactory.newJsonRpc2RequestBuilder();
		rpcRequest.addMethodCall(CoreOperationConstants.QUERY_OPERATION)
			.id("queryTest")
			.putParameter(QUERY_PARAM, QueryRoot.builder()
				.projectionName(DOCUMENT_PROJECTION)
				.targetDocumentModel(BUSINESS_PARTNER_MODEL_NAME)
				.field("/BusinessPartnerRoot/Name")
				.constraint(ExactMatchOperator.builder()
					.caseSensitive(true)
					.field("/__meta/docRef")
					.value("BusinessPartner/8e363b86-d108-439b-871c-2c55327200b0")
					.build()
				)
				.paging(Paging.builder()
					.pageNumber(0)
					.pageSize(100)
					.build())
				.build()
			);
		List<JsonRpc2Response> results = rpcOperationsClient.invoke(rpcRequest.build());
		Assert.assertEquals(results.size(), 1);
		Assert.assertNull(results.getFirst().getError());
	}

	@Test
	public void testQueryAPI_withLink() {
		JsonRpc2RequestBuilder rpcRequest = requestBuilderFactory.newJsonRpc2RequestBuilder();
		rpcRequest.addMethodCall(CoreOperationConstants.QUERY_OPERATION)
			.id("queryTest")
			.putParameter(QUERY_PARAM, QueryRoot.builder()
				.targetDocumentModel(CONTRACT_MODEL_NAME)
				.projectionName(DOCUMENT_PROJECTION)
				.fields(List.of(
						"/ContractRoot/ContractName",
						"/ContractRoot/Type"
					)
				)
				.link(QueryLink.builder()
					.relationshipModel(CONTRACT_CO_INSURED_PARTNER_MODEL_NAME)
					.targetRole(PARTNER_ROLE)
					.build()
				)
				.paging(Paging.builder()
					.pageNumber(0)
					.pageSize(100)
					.build())
				.build()
			);
		List<JsonRpc2Response> results = rpcOperationsClient.invoke(rpcRequest.build());
		Assert.assertEquals(results.size(), 1);
		Assert.assertNull(results.getFirst().getError());
	}

	@Test
	public void testQueryAPIWithoutPaging_hasError() {
		JsonRpc2RequestBuilder rpcRequest = requestBuilderFactory.newJsonRpc2RequestBuilder();
		rpcRequest.addMethodCall(CoreOperationConstants.QUERY_OPERATION)
			.id("queryTest")
			.putParameter(QUERY_PARAM, QueryRoot.builder()
				.projectionName(DOCUMENT_PROJECTION)
				.targetDocumentModel(BUSINESS_PARTNER_MODEL_NAME).build()
			);

		List<JsonRpc2Response> results = rpcOperationsClient.invoke(rpcRequest.build());
		Assert.assertEquals(results.size(), 1);
		Assert.assertNotNull(results.getFirst().getError());
	}

	@Test void testFieldsProjection_alwaysReturnDocumentFields() {
		JsonRpc2RequestBuilder rpcRequest = requestBuilderFactory.newJsonRpc2RequestBuilder();
		rpcRequest.addMethodCall(CoreOperationConstants.QUERY_OPERATION)
			.id("queryTest1")
			.putParameter(QUERY_PARAM, QueryRoot.builder()
				.projectionName(DOCUMENT_PROJECTION)
				.targetDocumentModel(CONTRACT_MODEL_NAME)
				.paging(Paging.builder()
					.pageNumber(0)
					.pageSize(100)
					.build())
				.build()
			);

		rpcRequest.addMethodCall(CoreOperationConstants.QUERY_OPERATION)
			.id("queryTest2")
			.putParameter(QUERY_PARAM, QueryRoot.builder()
				.projectionName(DOCUMENT_PROJECTION)
				.targetDocumentModel(CONTRACT_MODEL_NAME)
				.paging(Paging.builder()
					.pageNumber(0)
					.pageSize(100)
					.build())
				.fields(List.of("/ContractRoot/ContractName"))
				.build()
			);

		List<JsonRpc2Response> results = rpcOperationsClient.invoke(rpcRequest.build());
		Assert.assertEquals(results.size(), 2);
		Assert.assertNotNull(results.getFirst().getResult());
		Assert.assertTrue(results.getFirst().getResult().get("entries").get(0).path("document").asOptional().isPresent());
		Assert.assertTrue(results.getLast().getResult().get("entries").get(0).at("/document/ContractRoot/ContractName").asOptional().isPresent());
		Assert.assertEquals(results.getLast().getResult().get("entries").get(0).at("/document/ContractRoot/ContractName").textValue(), "MyContract1");
		Assert.assertFalse(results.getLast().getResult().get("entries").get(0).at("/document/ContractRoot/CostToCustomer").asOptional().isPresent());
	}
}
