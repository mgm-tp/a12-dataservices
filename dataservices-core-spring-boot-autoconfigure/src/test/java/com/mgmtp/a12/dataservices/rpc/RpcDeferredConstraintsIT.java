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

import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.LinkTestUtils;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.OffsetBasedPageRequest;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipSortConstants;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.relationship.persistence.RelationshipLinkRepository;

public class RpcDeferredConstraintsIT extends AbstractSpringContextIT {

	@Autowired private RelationshipLinkRepository relationshipLinkRepository;

	private static final String BUSINESS_PARTNER_ADDRESS_RM = "PartnerAddresses";
	private static final String BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT = "PartnerPostalAddress";

	private DocumentReference businessPartner1DocRef;
	private DocumentReference businessPartner2DocRef;
	private DocumentReference businessPartner3DocRef;

	private DocumentReference address1DocRef;
	private DocumentReference address2DocRef;
	private DocumentReference address3DocRef;

	@BeforeMethod
	public void setUp() throws Exception {
		cleanUpTestEnvironment();
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.ADDRESS_DOCUMENT_MODEL_PATH);

		createModel(resourceFunctions.loadResource(PathConstants.RELATIONSHIP_MODEL_ROOT_DIR + BUSINESS_PARTNER_ADDRESS_RM + ".json"));
		createModel(resourceFunctions.loadResource(PathConstants.RELATIONSHIP_MODEL_ROOT_DIR + BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT + ".json"));

		businessPartner1DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(
			DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.RPC_DOCUMENTS_PATH + "BusinessPartner-1.json");
		businessPartner2DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(
			DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.RPC_DOCUMENTS_PATH + "BusinessPartner-1.json");
		businessPartner3DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(
			DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.RPC_DOCUMENTS_PATH + "BusinessPartner-1.json");

		address1DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL,
			PathConstants.RPC_DOCUMENTS_PATH + "Address-1.json");
		address2DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL,
			PathConstants.RPC_DOCUMENTS_PATH + "Address-2.json");
		address3DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL,
			PathConstants.RPC_DOCUMENTS_PATH + "Address-3.json");
	}

	/*
	 * Postal address has upper limit = 1 and unbounded = false
	 */
	@Test
	public void addLinkBelowUpperLimit() throws Exception {
		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "deferred_constraints/add_link_PartnerAddress_1-address.json");
		request = request.formatted(
			BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, businessPartner1DocRef, address1DocRef
		);

		List<RelationshipLinkSpec> addResponseObject = sendRpcRequest(request).stream()
			.map(e -> convertResponse(e.getResult().toString(), RelationshipLinkSpec.class))
			.toList();

		Assert.assertEquals(addResponseObject.size(), 1);
		LinkTestUtils.assertPartnerAddressLinkRef(BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, addResponseObject.getFirst());

		checkEntityRelation(BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, businessPartner1DocRef, RelationshipModelConstants.RoleConstants.PARTNER_ROLE, 1);
		checkEntityRelation(BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, address1DocRef, RelationshipModelConstants.RoleConstants.ADDRESS_ROLE, 1);
	}

	/*
	 * Postal address has upper limit = 1 and unbounded = false
	 */
	@Test
	public void add2LinksAddressUpperLimitViolation() throws Exception {
		try {
			String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "deferred_constraints/add_link_PartnerAddress_2-addresses.json");
			request = request.formatted(
				BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, businessPartner1DocRef, address1DocRef,
				BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, businessPartner1DocRef, address2DocRef
			);
			sendRpcRequest(request);
			Assert.fail("Exception should be thrown!");
		} catch (RpcException e) {
			OperationError operationError = e.getOperationError();
			Assert.assertEquals(operationError.getShortMessage().getDefaultMessage(), "Upper Limit Reached");
			Assert.assertTrue(
				operationError.getLongMessage().getDefaultMessage()
					.contains("Upper limit reached for role [%s] in relationship model [%s]. Document ["
					.formatted(RelationshipModelConstants.RoleConstants.ADDRESS_ROLE, BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT)));

			checkEntityRelation(BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, businessPartner1DocRef, RelationshipModelConstants.RoleConstants.PARTNER_ROLE, 0);
			checkEntityRelation(BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, address1DocRef, RelationshipModelConstants.RoleConstants.ADDRESS_ROLE, 0);
			checkEntityRelation(BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, address2DocRef, RelationshipModelConstants.RoleConstants.ADDRESS_ROLE, 0);
		}
	}

	/*
	 * Partners for postal address have upper limit = 10 and unbounded = false
	 */
	@Test
	public void add11LinksPartnerUpperLimitViolation() throws Exception {
		int upperLimit = 10;
		try {
			String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "deferred_constraints/add_link_PartnerAddress_1-address.json");
			for (int i = 0; i < upperLimit + 1; i++) {
				String req = request.formatted(
					BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT,
					documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
						PathConstants.RPC_DOCUMENTS_PATH + "BusinessPartner-1.json"), address1DocRef
				);
				sendRpcRequest(req);
			}
			Assert.fail("Exception should be thrown!");
		} catch (RpcException e) {
			OperationError operationError = e.getOperationError();
			Assert.assertEquals(operationError.getShortMessage().getDefaultMessage(), "Upper Limit Reached");
			Assert.assertTrue(
				operationError.getLongMessage().getDefaultMessage()
					.contains("Upper limit reached for role [%s] in relationship model [%s]. Document ["
						.formatted(RelationshipModelConstants.RoleConstants.PARTNER_ROLE,BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT)));

			checkEntityRelation(BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, address1DocRef, RelationshipModelConstants.RoleConstants.ADDRESS_ROLE, upperLimit);
		}
	}

	/*
	 * Addresses have upper limit = 2 but unbounded = true
	 */
	@Test
	public void add3LinksOverUpperLimitWithUnbounded() throws Exception {
		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "deferred_constraints/add_link_PartnerAddress_3-addresses.json");
		request = request.formatted(
			BUSINESS_PARTNER_ADDRESS_RM, businessPartner1DocRef, address1DocRef,
			BUSINESS_PARTNER_ADDRESS_RM, businessPartner1DocRef, address2DocRef,
			BUSINESS_PARTNER_ADDRESS_RM, businessPartner1DocRef, address3DocRef
		);

		List<RelationshipLinkSpec> addResponseObject = sendRpcRequest(request).stream()
			.map(e -> convertResponse(e.getResult().toString(), RelationshipLinkSpec.class))
			.toList();

		Assert.assertEquals(addResponseObject.size(), 3);
		LinkTestUtils.assertPartnerAddressLinkRef(BUSINESS_PARTNER_ADDRESS_RM, addResponseObject.getFirst());
		LinkTestUtils.assertPartnerAddressLinkRef(BUSINESS_PARTNER_ADDRESS_RM, addResponseObject.get(1));
		LinkTestUtils.assertPartnerAddressLinkRef(BUSINESS_PARTNER_ADDRESS_RM, addResponseObject.get(2));

		checkEntityRelation(BUSINESS_PARTNER_ADDRESS_RM, businessPartner1DocRef, RelationshipModelConstants.RoleConstants.PARTNER_ROLE, 3);
		checkEntityRelation(BUSINESS_PARTNER_ADDRESS_RM, address1DocRef, RelationshipModelConstants.RoleConstants.ADDRESS_ROLE, 1);
		checkEntityRelation(BUSINESS_PARTNER_ADDRESS_RM, address2DocRef, RelationshipModelConstants.RoleConstants.ADDRESS_ROLE, 1);
		checkEntityRelation(BUSINESS_PARTNER_ADDRESS_RM, address3DocRef, RelationshipModelConstants.RoleConstants.ADDRESS_ROLE, 1);
	}

	/*
	 * Constraints are checked after all requests are executed
	 * => add links above limit is possible if supernumerous links are removed within same request
	 */
	@Transactional
	@Test public void deferredConstraintsCheckAfterAddLink() throws Exception {
		// SETUP -> limits are not broken but additional add would violate constraints
		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "deferred_constraints/add_link_PartnerAddress_1-address.json");
		request = request.formatted(
			BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, businessPartner1DocRef, address1DocRef
		);
		sendRpcRequest(request);

		request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "deferred_constraints/add_link_PartnerAddress_1-address.json");
		request = request.formatted(
			BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, businessPartner2DocRef, address1DocRef
		);
		sendRpcRequest(request);

		List<? extends RelationshipLink> existingLinkIds = relationshipLinkRepository.findByRelationshipModelNameAndSource(
			BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT,
			RelationshipModelConstants.RoleConstants.ADDRESS_ROLE,
			address1DocRef,
			OffsetBasedPageRequest.unpaged()
		).getContent();

		RelationshipLink relationshipLinkEntity = existingLinkIds.getFirst();
		RelationshipLink relationshipLinkEntity1 = existingLinkIds.get(1);
		DocumentReference partner1 = relationshipLinkEntity.getRoles().get(RelationshipModelConstants.RoleConstants.PARTNER_ROLE).getDocRef();
		DocumentReference partner2 = relationshipLinkEntity1.getRoles().get(RelationshipModelConstants.RoleConstants.PARTNER_ROLE).getDocRef();

		request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "deferred_constraints/add_1_addresses_remove_1_address_exchange_1_address.json");
		request = request.formatted(
			BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, partner2, address2DocRef,
			BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, businessPartner3DocRef, address2DocRef,
			BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, partner1, address1DocRef, relationshipLinkEntity.getId(),
			BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, partner2, address1DocRef, relationshipLinkEntity1.getId()
		);

		List<JsonRpc2Response> successfulResponses = sendRpcRequest(request).stream()
			.filter(JsonRpc2Response::isSuccess)
			.toList();

		List<RelationshipLinkSpec> addResponseObject = successfulResponses.stream()
			.filter(e -> e.getResult() != null)
			.map(e -> convertResponse(e.getResult().toString(), RelationshipLinkSpec.class))
			.toList();

		Assert.assertEquals(successfulResponses.size(), 4);
		Assert.assertEquals(addResponseObject.size(), 4);
		LinkTestUtils.assertPartnerAddressLinkRef(BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, addResponseObject.getFirst());
		LinkTestUtils.assertPartnerAddressLinkRef(BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, addResponseObject.get(1));

		// Partner 1 left its address, address of partner 2 has been changed, address 2 is assigned to partner 2 and partner 3
		checkEntityRelation(BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, partner1, RelationshipModelConstants.RoleConstants.PARTNER_ROLE, 0);
		checkEntityRelation(BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, partner2, RelationshipModelConstants.RoleConstants.PARTNER_ROLE, 1);
		checkEntityRelation(BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, businessPartner3DocRef, RelationshipModelConstants.RoleConstants.PARTNER_ROLE, 1);
		checkEntityRelation(BUSINESS_PARTNER_ADDRESS_RM_UPPER_LIMIT, address2DocRef, RelationshipModelConstants.RoleConstants.ADDRESS_ROLE, 2);
	}

	/**
	 * BusinessPartnerAddress -> duplicates not allowed
	 */
	@Test
	public void addDuplicatedLink() throws Exception {
		try {
			String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "deferred_constraints/add_link_PartnerAddress_2-addresses.json");
			request = request.formatted(
				BUSINESS_PARTNER_ADDRESS_RM, businessPartner1DocRef, address1DocRef,
				BUSINESS_PARTNER_ADDRESS_RM, businessPartner1DocRef, address1DocRef
			);
			sendRpcRequest(request);
			Assert.fail("Exception should be thrown!");
		} catch (RpcException e) {
			OperationError operationError = e.getOperationError();
			Assert.assertEquals(operationError.getShortMessage().getDefaultMessage(), "Duplicated link constraint violated");
			Assert.assertEquals(operationError.getLongMessage().getDefaultMessage(),
				"Creation of the link of model [%s] between [%s/%s] and [%s/%s] violates duplication constraint".formatted(BUSINESS_PARTNER_ADDRESS_RM,
					RelationshipModelConstants.RoleConstants.PARTNER_ROLE, businessPartner1DocRef, RelationshipModelConstants.RoleConstants.ADDRESS_ROLE,
					address1DocRef));
			checkEntityRelation(BUSINESS_PARTNER_ADDRESS_RM, businessPartner1DocRef, RelationshipModelConstants.RoleConstants.PARTNER_ROLE, 0);
			checkEntityRelation(BUSINESS_PARTNER_ADDRESS_RM, address1DocRef, RelationshipModelConstants.RoleConstants.ADDRESS_ROLE, 0);
		}
	}

	private void checkEntityRelation(String relationshipModelName, DocumentReference sourceEntityDocRef, String role, int count) {
		List<? extends RelationshipLink> relationshipModelAndDocRef =
			relationshipLinkRepository.findByRelationshipModelNameAndSource(
				relationshipModelName,
				role,
				sourceEntityDocRef,
				OffsetBasedPageRequest.unpaged(
					Sort.by(RelationshipSortConstants.ID_FIELD_NAME)
				)
			).getContent();
		MatcherAssert.assertThat(relationshipModelAndDocRef.size(), Matchers.is(count));
	}
}
