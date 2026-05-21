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

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mgmtp.a12.dataservices.client.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.client.document.SpelAwareDocumentReference;
import com.mgmtp.a12.dataservices.client.rpc.internal.JsonRpc2RequestBuilder;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;

import lombok.NonNull;

public class JsonRpc2ClientLinksIT extends AbstractSpringContextIT {

	@BeforeClass
	public void init() {
		createModelFromFile(CONTRACT_MODEL_FILE);
		createModelFromFile(BUSINESS_PARTNER_SUPER_MODEL_FILE);
		createModelFromFile(BUSINESS_PARTNER_MODEL_FILE);
		createModelFromFile(CO_INSURED_ADDITIONAL_FIELD_MODEL_FILE);

		createModelFromFile(CONTRACT_CO_INSURED_PARTNER_MODEL_FILE);
	}

	@AfterClass
	public void cleanUp() {
		cleanUpByDocumentModel(BUSINESS_PARTNER_SUPER_MODEL_NAME);
		cleanUpByDocumentModel(BUSINESS_PARTNER_MODEL_NAME);
		cleanUpByDocumentModel(CONTRACT_MODEL_NAME);
		cleanUpByDocumentModel(CO_INSURED_ADDITIONAL_FIELD_MODEL_NAME);

		cleanupRelationshipModel();
		modelsClient.deleteModel(CONTRACT_CO_INSURED_PARTNER_MODEL_NAME);
	}

	@Test
	public void createDocumentsAndLink() throws JsonProcessingException {
		JsonRpc2RequestBuilder rpcBuilder = requestBuilderFactory.newJsonRpc2RequestBuilder();

		String addContractId = "AddContractDocument";
		String addBusinessPartnerId = "AddBusinessPartnerDocument";

		rpcBuilder.addMethodCall(CoreOperationConstants.ADD_DOCUMENT_OPERATION)
			.id(addContractId)
			.putParameter("documentModelName", CONTRACT_MODEL_NAME)
			.putParameter("locale", "en")
			.putParameter("document", objectMapper.readTree(readFile(CONTRACT_DOCUMENT)));

		rpcBuilder.addMethodCall(CoreOperationConstants.ADD_DOCUMENT_OPERATION)
			.id(addBusinessPartnerId)
			.putParameter("documentModelName", BUSINESS_PARTNER_MODEL_NAME)
			.putParameter("document", objectMapper.readTree(readFile(BUSINESS_PARTNER_1_DOCUMENT)));

		rpcBuilder.addMethodCall(CoreOperationConstants.ADD_LINK_OPERATION)
			.id("AddLink")
			.putParameter("linkDescriptor", getLinkDescriptor(addContractId, addBusinessPartnerId))
			.putParameter("linkDocument", objectMapper.createObjectNode().set("CoInsuredRoot", objectMapper.createObjectNode()
				.put("Name", "CoInsuredAdditionalFields 1")));

		List<JsonRpc2Response> responseList = rpcOperationsClient.invoke(rpcBuilder.build());
		responseList.forEach(res -> {
			Assert.assertNull(res.getError(), String.format("Response [%s] should not contain error", res.getId()));
			Assert.assertNotNull(res.getResult(), String.format("Response [%s] should contain result", res.getId()));
		});
	}

	@NonNull private static LinkDescriptor getLinkDescriptor(String addContractId, String addBusinessPartnerId) {
		RelationshipRoleSpec entitySpec1 = new RelationshipRoleSpec("Contract", new SpelAwareDocumentReference(String.format("#{#%s.metadata.docRef}", addContractId)));
		entitySpec1.setModelName(String.format("#{#%s.metadata.docRef.documentModelName}", addContractId));

		RelationshipRoleSpec entitySpec2 = new RelationshipRoleSpec("Partner", new SpelAwareDocumentReference(String.format("#{#%s.metadata.docRef}",
			addBusinessPartnerId)));
		entitySpec2.setModelName(String.format("#{#%s.metadata.docRef.documentModelName}", addBusinessPartnerId));

		return new LinkDescriptor(CONTRACT_CO_INSURED_PARTNER_MODEL_NAME, Arrays.asList(entitySpec1, entitySpec2));
	}

}
