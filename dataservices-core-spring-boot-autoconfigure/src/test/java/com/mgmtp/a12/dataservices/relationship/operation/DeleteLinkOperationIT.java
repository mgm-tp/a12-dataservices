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
package com.mgmtp.a12.dataservices.relationship.operation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.OffsetBasedPageRequest;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.operation.internal.DeleteLinkOperation;
import com.mgmtp.a12.dataservices.relationship.persistence.RelationshipLinkRepository;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.rpc.RpcException;

public class DeleteLinkOperationIT extends AbstractListITBase {

	@Autowired private DeleteLinkOperation deleteLinkOperation;
	@Autowired private RelationshipLinkRepository relationshipLinkRepository;
	@Autowired private RelationshipLinkService relationshipLinkService;

	@DataProvider
	public Object[][] provideDataForInvalidRelationshipModelTest() {
		return new Object[][]{
				{INVALID_LINK_DM_RM},
				{INVALID_FIRST_ROLE_RM},
				{INVALID_SECOND_ROLE_RM}
		};
	}

	@Test(dataProvider = "provideDataForInvalidRelationshipModelTest")
	public void invalidRelationshipModelTest(String relationship) {
		try {
			LinkDescriptor
				linkDescriptor = createLinkDescriptor(relationship, RoleConstants.CONTRACT_ROLE, contractDocRef2, RoleConstants.PARTNER_ROLE, partnerDocRef1);
			RelationshipLinkSpec linkSpec = RelationshipLinkSpec.builder().linkDescriptor(linkDescriptor).id(contract2Partner2Link).build();
			deleteLinkOperation.rpc(linkSpec);
			Assert.fail("Exception was expected to be thrown");
		} catch (RpcException e) {
			LocalizedEntry shortMessage = e.getOperationError().getShortMessage();
			LocalizedEntry longMessage = e.getOperationError().getLongMessage();

			Assert.assertEquals(shortMessage.getKey(), "error.validation.link.badModel.title");
			Assert.assertEquals(shortMessage.getDefaultMessage(), "Wrong Relationship Model");
			Assert.assertEquals(longMessage.getKey(), "error.validation.link.badModel.description");
			Assert.assertEquals(
				longMessage.getDefaultMessage(),
				"Requested link [%s] is for relationship model [%s], but expected is [%s]".formatted(
					contract2Partner2Link,
					RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL,
					relationship
				)
			);
		}
	}

	@DataProvider(name = "DeleteDataAndResultForSuccessTest")
	public Object[][] provideDeleteDataForSuccessTest() {
		return new Object[][]{
				{contractDocRef2, partnerDocRef1},
				{contractDocRef2, partnerDocRef2},
		};
	}

	@Test(dataProvider = "DeleteDataAndResultForSuccessTest")
	public void successfulDeleteRelationshipLinkTest(DocumentReference contractDocRef, DocumentReference partnerDocRef) {
		checkIfEntityExists(contractDocRef, partnerDocRef, true);
		deleteLinkOperation.rpc(createDeleteLinkParameter(contractDocRef, partnerDocRef));
		checkIfEntityExists(contractDocRef, partnerDocRef, false);
	}

	@DataProvider(name = "ProvideDeleteDataForLimitTest")
	public Object[][] provideDeleteDataAndResultForLimitTest() {
		return new Object[][]{
				{contractDocRef2, partnerDocRef3},
				{contractDocRef2, partnerDocRef4}
		};
	}

	@Test(dataProvider = "ProvideDeleteDataForLimitTest")
	public void unboundedDeleteRelationshipLinkTest(DocumentReference contractDocRef, DocumentReference partnerDocRef) {
		//unbounded is true
		checkIfEntityExists(contractDocRef, partnerDocRef, true);
		deleteLinkOperation.rpc(createDeleteLinkParameter(contractDocRef, partnerDocRef));
		checkIfEntityExists(contractDocRef, partnerDocRef, false);
	}

	@Test
	public void InvalidDocRefDeleteRelationshipLinkTest() {
		DocumentReference invalidDocRef = new DocumentReference(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, "99999");
		LinkDescriptor linkDescriptor =
			createLinkDescriptor(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, RoleConstants.CONTRACT_ROLE, contractDocRef2,
				RoleConstants.PARTNER_ROLE, invalidDocRef);
		RelationshipLinkSpec relationshipLinkSpec = RelationshipLinkSpec.builder().linkDescriptor(linkDescriptor).id(contract2Partner2Link).build();
		try {
			deleteLinkOperation.rpc(relationshipLinkSpec);
		} catch (RpcException e) {
			LocalizedEntry shortMessage = e.getOperationError().getShortMessage();
			LocalizedEntry longMessage = e.getOperationError().getLongMessage();
			Assert.assertEquals(shortMessage.getKey(), "error.validation.link.badEntity_Partner.title");
			Assert.assertEquals(shortMessage.getDefaultMessage(), "Wrong Entity");
			Assert.assertEquals(longMessage.getKey(), "error.validation.link.badEntity_Partner.description");
			Assert.assertEquals(longMessage.getDefaultMessage(),
				"Requested link [%s] has role:docRef [Contract:%s] and [Partner:%s], but expected is [Contract:%s] and [Partner:%s]".formatted(relationshipLinkSpec.getId(), contractDocRef2,
					invalidDocRef, contractDocRef2, partnerDocRef1));
		}
	}

	@Test
	public void InvalidRoleDeleteRelationshipLinkTest() {
		LinkDescriptor linkDescriptor =
			createLinkDescriptor(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, RoleConstants.CONTRACT_ROLE, contractDocRef2,
				INVALID_SECOND_ROLE_RM, partnerDocRef1);
		RelationshipLinkSpec relationshipLinkSpec = RelationshipLinkSpec.builder().linkDescriptor(linkDescriptor).id(contract2Partner2Link).build();
		try {
			deleteLinkOperation.rpc(relationshipLinkSpec);
		} catch (RpcException e) {
			LocalizedEntry shortMessage = e.getOperationError().getShortMessage();
			LocalizedEntry longMessage = e.getOperationError().getLongMessage();
			Assert.assertEquals(shortMessage.getKey(), "error.validation.link.badEntity_ContractCoinsuredPartner_InvalidSecondRole.title");
			Assert.assertEquals(shortMessage.getDefaultMessage(), "Wrong Entity");
			Assert.assertEquals(longMessage.getKey(), "error.validation.link.badEntity_ContractCoinsuredPartner_InvalidSecondRole.description");
			Assert.assertEquals(longMessage.getDefaultMessage(),
				"Requested link [%s] has role:docRef [Contract:%s] and [%s:%s], but expected is [Contract:%s] and [Partner:%s]".formatted(relationshipLinkSpec.getId(), contractDocRef2,
					INVALID_SECOND_ROLE_RM, partnerDocRef1, contractDocRef2, partnerDocRef1));
		}
	}

	@Test
	public void InvalidRMDeleteRelationshipLinkTest() {
		LinkDescriptor linkDescriptor =
			createLinkDescriptor(INVALID_LINK_DM_RM, RoleConstants.CONTRACT_ROLE, contractDocRef2, RoleConstants.PARTNER_ROLE, partnerDocRef1);
		RelationshipLinkSpec relationshipLinkSpec = RelationshipLinkSpec.builder().linkDescriptor(linkDescriptor).id(contract2Partner2Link).build();
		try {
			deleteLinkOperation.rpc(relationshipLinkSpec);
		} catch (RpcException e) {
			LocalizedEntry shortMessage = e.getOperationError().getShortMessage();
			LocalizedEntry longMessage = e.getOperationError().getLongMessage();
			Assert.assertEquals(shortMessage.getKey(), "error.validation.link.badModel.title");
			Assert.assertEquals(shortMessage.getDefaultMessage(), "Wrong Relationship Model");
			Assert.assertEquals(longMessage.getKey(), "error.validation.link.badModel.description");
			Assert.assertEquals(
				longMessage.getDefaultMessage(),
				"Requested link [%s] is for relationship model [%s], but expected is [%s]".formatted(
					contract2Partner2Link,
					RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL,
					INVALID_LINK_DM_RM
				));
		}
	}

	protected RelationshipLinkSpec createDeleteLinkParameter(DocumentReference contractDocRef, DocumentReference partnerDocRef) {
		List<? extends RelationshipLink> byEntities =
			relationshipLinkRepository.findByRelationshipModelNameAndSourceAndTarget(
				RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL,
				RoleConstants.CONTRACT_ROLE,
				contractDocRef,
				RoleConstants.PARTNER_ROLE,
				partnerDocRef,
				OffsetBasedPageRequest.unpaged()
			).getContent();
		LinkDescriptor linkDescriptor =
			createLinkDescriptor(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, RoleConstants.CONTRACT_ROLE, contractDocRef,
				RoleConstants.PARTNER_ROLE, partnerDocRef);
		return RelationshipLinkSpec.builder().linkDescriptor(linkDescriptor).id(byEntities.getFirst().getId()).build();
	}

	private void checkIfEntityExists(DocumentReference contractDocRef, DocumentReference partnerDocRef, boolean expectExists) {
		int amountOfEntities = relationshipLinkRepository.findByRelationshipModelNameAndSourceAndTarget(
			RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL,
			RoleConstants.CONTRACT_ROLE,
			contractDocRef,
			RoleConstants.PARTNER_ROLE,
			partnerDocRef,
			OffsetBasedPageRequest.unpaged()
		).getContent().size();

		if (expectExists) {
			Assert.assertEquals(amountOfEntities, 1);
		} else {
			Assert.assertEquals(amountOfEntities, 0);
		}
	}
}
