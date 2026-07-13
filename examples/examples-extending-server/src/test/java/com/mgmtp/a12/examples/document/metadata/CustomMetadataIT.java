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
package com.mgmtp.a12.examples.document.metadata;

import tools.jackson.databind.JsonNode;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.operation.internal.AddLinkOperation;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;
import com.mgmtp.a12.examples.AbstractITBase;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import java.io.IOException;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

@TestPropertySource(locations = "classpath:config/application-dataservices-example-documents_metadata.properties")
public class CustomMetadataIT extends AbstractITBase {

	@Autowired private AddLinkOperation addLinkOperation;

	DocumentReference contractDocRef;
	DocumentReference businessPartnerDocRef;

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

		businessPartnerDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(BUSINESS_PARTNER_DOCUMENT_MODEL_NAME, "link/BusinessPartnerSuper.json");
		contractDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(CONTRACT_DOCUMENT_MODEL_NAME, "link/Contract.json");
	}


	@Test(description = "Sets and updates metadata version on document")
	void metadataVersionIsSetOnDocument() throws IOException {
		DocumentReference docRef = documentFunctions.createDocumentFromFileAndGetDocRef(BUSINESS_PARTNER_DOCUMENT_MODEL_NAME, "link/BusinessPartnerSuper.json");
		DocumentV2 document = documentRepository.findByDocumentReference(docRef).get().getKernelDocument();
		DocumentV2 updated = document.withFieldValue("/businessPartner/name", "Updated Name");
		DocumentV2 afterUpdate = documentService.update(docRef, updated, null).getKernelDocument();

		assertEquals(document.fieldValue("/__meta/extensions/metadataVersion"), ("1.0.0"));
		assertEquals(afterUpdate.fieldValue("/__meta/extensions/metadataVersion"), ("2.0.0"));
		assertNull(document.fieldValue("/__meta/creator"));
		assertNull(document.fieldValue("/__meta/createdAt"));
		assertNull(document.fieldValue("/__meta/modifier"));
		assertNull(document.fieldValue("/__meta/modifiedAt"));
		assertNull(document.fieldValue("/__meta/modelVersion"));
	}


	@Test(description = "Sets metadataVersion on both documents when a link is added")
	void metadataVersionIsSetOnAddLink() {
		// Documents are already created during init
		DataServicesDocument createdContract = documentService.load(contractDocRef).orElseThrow(() -> new NotFoundException("Contract Document Not Found"));
		DataServicesDocument createdBusinessPartner = documentService.load(businessPartnerDocRef).orElseThrow(() -> new NotFoundException("BusinessPartner Document Not Found"));
		assertNotNull(createdContract);
		assertNotNull(createdBusinessPartner);
		assertEquals(createdContract.getKernelDocument().fieldValue("/__meta/extensions/metadataVersion"), ("1.0.0"));
		assertEquals(createdBusinessPartner.getKernelDocument().fieldValue("/__meta/extensions/metadataVersion"), ("1.0.0"));

		LinkDescriptor
			linkDescriptor = createLinkDescriptor(CO_INSURER_RELATIONSHIP_MODEL_NAME,
			CONTRACT_ROLE,
			contractDocRef,
			BUSINESS_PARTNER_ROLE,
			businessPartnerDocRef
		);
		// By assigning a link we should create a new version of metadataVersion for both sides of relationship
		addLinkOperation.rpc(linkDescriptor, createLinkDocument());

		createdContract = documentService.load(contractDocRef).orElseThrow(() -> new NotFoundException("Contract Document Not Found"));
		createdBusinessPartner = documentService.load(businessPartnerDocRef).orElseThrow(() -> new NotFoundException("BusinessPartner Document Not found"));

		assertEquals(createdBusinessPartner.getKernelDocument().fieldValue("/__meta/extensions/linkAssignment"), ("3.0.0"));
		assertEquals(createdContract.getKernelDocument().fieldValue("/__meta/extensions/linkAssignment"), ("3.0.0"));
		assertNull(createdContract.getKernelDocument().fieldValue("/__meta/creator"));
		assertNull(createdContract.getKernelDocument().fieldValue("/__meta/createdAt"));
		assertNull(createdContract.getKernelDocument().fieldValue("/__meta/modifier"));
		assertNull(createdContract.getKernelDocument().fieldValue("/__meta/modifiedAt"));
		assertNull(createdContract.getKernelDocument().fieldValue("/__meta/modelVersion"));

		assertNull(createdBusinessPartner.getKernelDocument().fieldValue("/__meta/creator"));
		assertNull(createdBusinessPartner.getKernelDocument().fieldValue("/__meta/createdAt"));
		assertNull(createdBusinessPartner.getKernelDocument().fieldValue("/__meta/modifier"));
		assertNull(createdBusinessPartner.getKernelDocument().fieldValue("/__meta/modifiedAt"));
		assertNull(createdBusinessPartner.getKernelDocument().fieldValue("/__meta/modelVersion"));
	}


	protected LinkDescriptor createLinkDescriptor(String relationship, String role1, DocumentReference docRef1, String role2, DocumentReference docRef2) {
		LinkDescriptor linkDescriptor = new LinkDescriptor();
		linkDescriptor.setRelationshipModel(relationship);
		RelationshipRoleSpec relationshipRoleSpec1 = new RelationshipRoleSpec(role1, docRef1);
		RelationshipRoleSpec relationshipRoleSpec2 = new RelationshipRoleSpec(role2, docRef2);
		linkDescriptor.setEntities(Arrays.asList(relationshipRoleSpec1, relationshipRoleSpec2));
		return linkDescriptor;
	}

	protected JsonNode createLinkDocument() {
		JsonNode rootGroup = JACKSON_2_OBJECT_MAPPER.createObjectNode().put("since", "2025-05-12");
		return JACKSON_2_OBJECT_MAPPER.createObjectNode().set("additionalFields", rootGroup);
	}
}
