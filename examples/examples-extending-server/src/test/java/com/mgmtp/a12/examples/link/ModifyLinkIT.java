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
package com.mgmtp.a12.examples.link;

import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.repository.RelationshipLinkJpaRepository;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import tools.jackson.databind.JsonNode;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.operation.internal.AddLinkOperation;
import com.mgmtp.a12.dataservices.relationship.operation.internal.ModifyLinkOperation;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;
import com.mgmtp.a12.examples.AbstractITBase;
import com.mgmtp.a12.examples.authorization.SalesUserConfiguration;

@ActiveProfiles({ SalesUserConfiguration.DATASERVICES_EXAMPLE_AUTHORIZATION_ENV, "dataservices-example-sme_workspace" })
public class ModifyLinkIT extends AbstractITBase {

	@Autowired private AddLinkOperation addLinkOperation;
	@Autowired private ModifyLinkOperation modifyLinkOperation;
	@Autowired private RelationshipLinkJpaRepository relationshipLinkJpaRepository;
	@Autowired private DocumentSupport documentSupport;

	protected DocumentReference contractDocRef;
	protected DocumentReference businessPartnerDocRef;
	protected DocumentReference businessPartnerDocRef2;

	@BeforeClass
	void init() throws IOException {
		setUserTo(UserConstants.ADMIN_USER);

		// Document models
		modelsFunctions.createModels(
			"/model/document/" + BUSINESS_PARTNER_DOCUMENT_MODEL_NAME + ".json",
			"/model/document/" + CONTRACT_DOCUMENT_MODEL_NAME + ".json",
			"/model/document/" + CO_INSURER_ADDITIONAL_FIELDS_MODEL_NAME + ".json",
			"/model/relationship/" + CO_INSURER_RELATIONSHIP_MODEL_NAME + ".json"
		);

		contractDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(CONTRACT_DOCUMENT_MODEL_NAME, "link/Contract.json");
		businessPartnerDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(BUSINESS_PARTNER_DOCUMENT_MODEL_NAME, "link/BusinessPartnerSuper.json");
		businessPartnerDocRef2 = documentFunctions.createDocumentFromFileAndGetDocRef(BUSINESS_PARTNER_DOCUMENT_MODEL_NAME, "link/BusinessPartnerSuper.json");

	}

	@DataProvider(name = "provideLinkDataForTest")
	public Object[][] provideModifyDataForSuccessTest() {
		return new Object[][] {
			{ "With link document docRef", true, contractDocRef, businessPartnerDocRef, "2025-04-15", "2025-07-28", "2025-07-28T00:00:00Z" },
			{ "Without docRef", false, contractDocRef, businessPartnerDocRef2, "2025-05-12", "2025-06-28", "2025-06-28T00:00:00Z" }
		};
	}

	@Test(dataProvider = "provideLinkDataForTest")
	void modifyLink(String name, boolean withLinkDocRef, DocumentReference contractDocRef, DocumentReference partnerDocRef, String value, String updatedValue,
		String expectedValue) {
		LinkDescriptor
			linkDescriptor = createLinkDescriptor(CO_INSURER_RELATIONSHIP_MODEL_NAME,
			CONTRACT_ROLE,
			contractDocRef,
			BUSINESS_PARTNER_ROLE,
			partnerDocRef
		);
		RelationshipLinkSpec original = addLinkOperation.rpc(linkDescriptor, createLinkDocument(value));

		RelationshipLink link = relationshipLinkJpaRepository.findById(original.getId()).get();
		JsonNode updatedLinkDoc = createLinkDocument(updatedValue);

		if (withLinkDocRef) {
			DocumentV2 kernelDocument = documentService.load(link.getLinkDocumentDocRef()).get()
				.getKernelDocument()
				.withFieldValue("/additionalFields/since", updatedValue);
			StringWriter writer = new StringWriter();
			documentSupport.convertDocumentToJSON(kernelDocument, writer);
			updatedLinkDoc = JACKSON_2_OBJECT_MAPPER.readTree(writer.toString());
		}

		modifyLinkOperation.rpc(
			RelationshipLinkSpec.builder().linkDescriptor(linkDescriptor).id(original.getId()).build(),
			updatedLinkDoc
		);

		DocumentReference updatedLinkDocRef = relationshipLinkJpaRepository.findById(original.getId()).get().getLinkDocumentDocRef();

		Assert.assertNotEquals(
			link.getLinkDocumentDocRef(),
			updatedLinkDocRef
		);

		Assert.assertEquals(
			documentService.load(updatedLinkDocRef).get()
				.getKernelDocument()
				.fieldValue("/additionalFields/since").toString(),
			expectedValue
		);
	}

	protected LinkDescriptor createLinkDescriptor(String relationship, String role1, DocumentReference docRef1, String role2, DocumentReference docRef2) {
		LinkDescriptor linkDescriptor = new LinkDescriptor();
		linkDescriptor.setRelationshipModel(relationship);
		RelationshipRoleSpec relationshipRoleSpec1 = new RelationshipRoleSpec(role1, docRef1);
		RelationshipRoleSpec relationshipRoleSpec2 = new RelationshipRoleSpec(role2, docRef2);
		linkDescriptor.setEntities(Arrays.asList(relationshipRoleSpec1, relationshipRoleSpec2));
		return linkDescriptor;
	}

	protected JsonNode createLinkDocument(String value) {
		JsonNode rootGroup = JACKSON_2_OBJECT_MAPPER.createObjectNode().put("since", value);
		return JACKSON_2_OBJECT_MAPPER.createObjectNode().set("additionalFields", rootGroup);
	}
}
