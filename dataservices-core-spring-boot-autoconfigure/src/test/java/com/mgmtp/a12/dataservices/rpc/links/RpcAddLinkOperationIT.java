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
package com.mgmtp.a12.dataservices.rpc.links;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.LinkTestUtils;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.DefaultRelationshipLinkRepository;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.entity.RelationshipLinkEntity;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.entity.RelationshipRoleEntity;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;
import com.mgmtp.a12.dataservices.rpc.OperationError;
import com.mgmtp.a12.dataservices.rpc.RpcException;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.CONTRACT_DOCUMENT_MODEL;

public class RpcAddLinkOperationIT extends AbstractLinkIT {

	protected static final String CONTRACT_CO_INSURED_PARTNER_MODEL_NAME = "ContractCoInsuredPartner";

	@Autowired private DefaultRelationshipLinkRepository defaultRelationshipLinkRepository;

	@Transactional
	@Test public void checkAddOperation() throws Exception {
		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "add/add_links_request.json");
		request = request.formatted(partner1DocRef, contract1DocRef,
			partner2DocRef, contract1DocRef,
			partner3DocRef, contract2DocRef,
			partner4DocRef, contract2DocRef,
			partner5DocRef, contract1DocRef,
			partner5DocRef, contract2DocRef,
			partner1DocRef, contract3DocRef);

		List<RelationshipLinkSpec> addResponseObject = sendRpcRequest(request).stream()
			.map(e -> convertResponse(e.getResult().toString(), RelationshipLinkSpec.class))
			.toList();

		Assert.assertEquals(addResponseObject.size(), 7);
		addResponseObject.forEach(
			result -> LinkTestUtils.assertContractBusinessPartnerLinkRef(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, result));

		checkEntityRelation(partner1DocRef, RoleConstants.PARTNER_ROLE, 2);
		checkEntityRelation(partner2DocRef, RoleConstants.PARTNER_ROLE, 1);
		checkEntityRelation(partner3DocRef, RoleConstants.PARTNER_ROLE, 1);
		checkEntityRelation(partner4DocRef, RoleConstants.PARTNER_ROLE, 1);
		checkEntityRelation(partner5DocRef, RoleConstants.PARTNER_ROLE, 2);
		checkEntityRelation(contract1DocRef, RoleConstants.CONTRACT_ROLE, 3);
		checkEntityRelation(contract2DocRef, RoleConstants.CONTRACT_ROLE, 3);
		checkEntityRelation(contract3DocRef, RoleConstants.CONTRACT_ROLE, 1);
	}

	@Transactional
	@Test public void checkSaveRelationShipWithoutGenerateId() {
		RelationshipLinkEntity entity = RelationshipLinkEntity.builder()
			.id("100")
			.relationshipModel("ContractCoInsuredPartner")
			.linkDocumentDocRef(contract1DocRef)
			.createdAt(Instant.now())
			.build();
		entity.addRole(
			RelationshipRoleEntity.builder()
				.id("200")
				.name("Partner")
				.docRef(partner1DocRef)
				.order("123445677")
				.build()
		);
		entity.addRole(
			RelationshipRoleEntity.builder()
				.id("201")
				.name("Contract")
				.docRef(contract1DocRef)
				.order("123445678")
				.build()
		);

		RelationshipLink result = defaultRelationshipLinkRepository.create(entity);
		Assert.assertEquals(entity.getId(), result.getId());
		Assert.assertEquals(entity.getCreatedAt().toEpochMilli(), result.getCreatedAt().toEpochMilli());
		Assert.assertEquals(entity.getRelationshipModel(), result.getRelationshipModel());
		Assert.assertEquals(result.getRoles().size(), 2);

		RelationshipLinkEntity entity2 = RelationshipLinkEntity.builder()
			.relationshipModel(CONTRACT_CO_INSURED_PARTNER_MODEL_NAME)
			.linkDocumentDocRef(contract1DocRef)
			.createdAt(Instant.now())
			.build();

		entity2.addRole(
			RelationshipRoleEntity.builder()
				.name(CONTRACT_DOCUMENT_MODEL)
				.docRef(contract1DocRef)
				.order("123445678")
				.build()
		);
		RelationshipLink result2 = defaultRelationshipLinkRepository.create(
			entity2
		);

		Assert.assertNotNull(result2.getId());
		Assert.assertEquals(result2.getRoles().size(), 1);
	}

	@Test
	public void checkAddOperationWithUpperLimit() throws Exception {
		try {
			String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "add/add_links_request_upper_limit.json");
			request = request.formatted(partner1DocRef, contract1DocRef,
				partner2DocRef, contract1DocRef,
				partner3DocRef, contract1DocRef,
				partner4DocRef, contract1DocRef,
				partner5DocRef, contract1DocRef,
				partner6DocRef, contract1DocRef,
				partner7DocRef, contract1DocRef,
				partner8DocRef, contract1DocRef,
				partner9DocRef, contract1DocRef);
			sendRpcRequest(request);
			Assert.fail("Exception should be thrown");
		} catch (RpcException e) {
			OperationError operationError = e.getOperationError();
			Assert.assertEquals(operationError.getShortMessage().getDefaultMessage(), "Upper Limit Reached");
			Assert.assertTrue(operationError.getLongMessage().getDefaultMessage().
					contains("Upper limit reached for role [Partner] in relationship model [ContractCoInsuredPartner]. Document "));
		}
	}

	@Test
	public void checkAddOperationBadObject() throws Exception {
		JsonRpc2Response response = dispatchBadAddLinkRequests(partner1DocRef.toString(), partner2DocRef.toString()).getFirst();
		Assert.assertFalse(response.isSuccess());
		OperationError operationError = createOperationError(response);
		OperationError expectedError = createErrorTemplate(ExceptionKeys.RELATIONSHIP_LINK_ADD_DOCUMENT_BAD_MODEL_ERROR_KEY, "Bad Document Model",
			"Document [%s] should have been defined for models [Contract], found [BusinessPartner] instead".formatted(partner2DocRef),
			"");
		assertExceptions(expectedError, operationError);
	}

	@Test
	public void checkAddOperationMissingAllObject() throws Exception {
		JsonRpc2Response response = dispatchBadAddLinkRequests("BusinessPartnerSuper/-999999", "Contract/-88888888").getFirst();
		Assert.assertFalse(response.isSuccess());
		OperationError operationError = createOperationError(response);
		OperationError expectedError = createErrorTemplate(ExceptionKeys.RELATIONSHIP_LINK_ADD_DOCUMENT_NOT_FOUND_ERROR_KEY, "Missing Link Documents",
			"Requested document for link ContractCoInsuredPartner in role Partner is missing: BusinessPartnerSuper/-999999",
			"");
		assertExceptions(expectedError, operationError);
	}

	@Test
	public void checkAddOperationBadRole() throws Exception {
		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "add/bad_add_link_request.json").replace(": \"Partner", ": \"Partner_GOGO");
		request = request.formatted(partner1DocRef, contract1DocRef);

		JsonRpc2Response response = sendRpcRequest(request).getFirst();
		Assert.assertFalse(response.isSuccess());
		OperationError operationError = createOperationError(response);
		OperationError expectedError = createErrorTemplate(ExceptionKeys.RELATIONSHIP_LINK_ROLE_MISSING_ERROR_KEY, "Missing Role in Model",
			"Requested role [Partner_GOGO] has not been found in the model [ContractCoInsuredPartner].", "");
		assertExceptions(expectedError, operationError);
	}

	@Test
	public void checkAddOperationMissingOneObject() throws Exception {
		JsonRpc2Response response = dispatchBadAddLinkRequests(partner1DocRef.toString(), "Contract/-88888888").getFirst();
		Assert.assertFalse(response.isSuccess());
		OperationError operationError = createOperationError(response);
		OperationError expectedError = createErrorTemplate(ExceptionKeys.RELATIONSHIP_LINK_ADD_DOCUMENT_NOT_FOUND_ERROR_KEY, "Missing Link Documents",
			"Requested document for link ContractCoInsuredPartner in role Contract is missing: Contract/-88888888", "");
		assertExceptions(expectedError, operationError);
	}

	private List<JsonRpc2Response> dispatchBadAddLinkRequests(String docRef, String docRefValue) throws IOException {
		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "add/bad_add_link_request.json");
		return sendRpcRequest(request.formatted(docRef, docRefValue));
	}
}
