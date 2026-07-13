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
package com.mgmtp.a12.dataservices.configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import tools.jackson.databind.JsonNode;
import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.AttachmentTestFunctions;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService;
import com.mgmtp.a12.dataservices.exception.IntegrityException;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.LinkPosition;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;

public class DocumentCascadeDeleteTest  extends AbstractSpringContextIT {

	@Autowired private DefaultDocumentService documentService;
	@Autowired private RelationshipLinkService relationshipLinkService;
	private DocumentReference docRefAddress;
	private DocumentReference docRefBusinessPartner;
	private static final String CONFIGURATION_FIELD = "dataServicesCoreProperties";

	@BeforeMethod public void init() throws Exception {
		super.cleanUpTestEnvironment();
		modelsFunctions.createModels(
			PathConstants.COINSURED_ADDITIONAL_PARTNER_DOCUMENT_MODEL_PATH,
			PathConstants.BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH,
			PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH,
			PathConstants.CONTRACT_DOCUMENT_MODEL_PATH,
			PathConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH,
			PathConstants.ADDRESS_DOCUMENT_MODEL_PATH
		);

		docRefAddress = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "Address.json");

		docRefBusinessPartner = attachmentTestFunctions.prepareDocumentWith2AttachmentsV2()
			.getDataServicesDocument().getMetadata().getDocRef();
	}

	private DataServicesCoreProperties createConfig(List<String> assetModel) {
		dataServicesCoreProperties.getDocuments().getDelete().getCascadeLinks().setDisabledForModels(assetModel);
		return dataServicesCoreProperties;
	}

	@Test(expectedExceptions = IntegrityException.class)
	public void testCascadeDeleteWithSingleModels() throws IOException {
		DataServicesCoreProperties configuration = createConfig(List.of(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL));
		DocumentReference contractDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL,
			PathConstants.DOCUMENTS_SEARCH_PATH + "singleModelSort/Contract-beth.json");
		createPartnerContractLink(docRefBusinessPartner, contractDocRef);
		runWithFieldOverwritten(CONFIGURATION_FIELD, configuration, documentService, () -> documentService.delete(docRefBusinessPartner));
	}

	@Test(expectedExceptions = IntegrityException.class)
	public void testCascadeDeleteWithAllModels() throws IOException {
		DocumentReference contractDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL,
			PathConstants.DOCUMENTS_SEARCH_PATH + "singleModelSort/Contract-beth.json");
		createPartnerContractLink(docRefBusinessPartner, contractDocRef);
		DataServicesCoreProperties configuration = createConfig(List.of(DataServicesCoreProperties.MATCH_ALL));
		runWithFieldOverwritten(CONFIGURATION_FIELD, configuration, documentService, () -> documentService.delete(docRefBusinessPartner));
	}

	@Test(expectedExceptions = IntegrityException.class)
	public void testCascadeDeleteWithAllModelsAndSingleModel() throws IOException {
		DocumentReference contractDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL,
			PathConstants.DOCUMENTS_SEARCH_PATH + "singleModelSort/Contract-beth.json");
		createPartnerContractLink(docRefBusinessPartner, contractDocRef);
		DataServicesCoreProperties configuration = createConfig(Arrays.asList(DataServicesCoreProperties.MATCH_ALL, DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL));
		runWithFieldOverwritten(CONFIGURATION_FIELD, configuration, documentService, () -> documentService.delete(docRefBusinessPartner));
	}

	@Test
	public void testCascadeDeleteWithAllModelsAndAnotherModel() throws IOException {
		DocumentReference contractDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL,
			PathConstants.DOCUMENTS_SEARCH_PATH + "singleModelSort/Contract-beth.json");
		createPartnerContractLink(docRefBusinessPartner, contractDocRef);
		DataServicesCoreProperties configuration = createConfig(Arrays.asList(DataServicesCoreProperties.MATCH_ALL, DocumentModelConstants.ADDRESS_DOCUMENT_MODEL));
		runWithFieldOverwritten(CONFIGURATION_FIELD, configuration, documentService, () -> {
			documentService.delete(docRefBusinessPartner);
			Assert.assertFalse(documentService.load(docRefBusinessPartner).isPresent());
		});
	}

	@Test
	public void testCascadeDeleteWithNoRelationshipLinksAndAllModels() throws IOException {
		DocumentReference contractDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL,
			PathConstants.DOCUMENTS_SEARCH_PATH + "singleModelSort/Contract-beth.json");
		DataServicesCoreProperties configuration = createConfig(List.of(DataServicesCoreProperties.MATCH_ALL));
		runWithFieldOverwritten(CONFIGURATION_FIELD, configuration, documentService, () -> {
			documentService.delete(contractDocRef);
			Assert.assertFalse(documentService.load(contractDocRef).isPresent());
		});
	}


	@Test
	public void testCascadeDeleteAllWithNoRelationshipLinksAndSingleModels() throws IOException {
		DocumentReference contractDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL,
			PathConstants.DOCUMENTS_SEARCH_PATH + "singleModelSort/Contract-beth.json");
		DocumentReference partnerDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
			PathConstants.DOCUMENTS_SEARCH_FILTERING_PATH + "BusinessPartner-1.json");
		createPartnerContractLink(partnerDocRef, contractDocRef);
		DataServicesCoreProperties configuration = createConfig(List.of(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL));
		List<DocumentReference> uploadedDocumentReferences =
			List.of(contractDocRef, partnerDocRef, docRefAddress);
		runWithFieldOverwritten(CONFIGURATION_FIELD, configuration, documentService, () -> {
			documentService.deleteAll(uploadedDocumentReferences);
			Assert.assertFalse(documentService.load(contractDocRef).isPresent());
		});
	}

	@Test(expectedExceptions = IntegrityException.class)
	public void testCascadeDeleteAllWithSingleModels() throws Exception {
		AttachmentTestFunctions.PreparedDocument preparedDocument1 = attachmentTestFunctions.prepareDocumentWith2AttachmentsV2();
		DocumentReference contractDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL,
			PathConstants.DOCUMENTS_SEARCH_PATH + "singleModelSort/Contract-beth.json");
		createPartnerContractLink(docRefBusinessPartner, contractDocRef);
		List<DocumentReference> uploadedDocumentReferences =
			List.of(preparedDocument1.getDataServicesDocument().getMetadata().getDocRef(), docRefBusinessPartner);
		DataServicesCoreProperties configuration = createConfig(List.of(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL));
		runWithFieldOverwritten(CONFIGURATION_FIELD, configuration, documentService, () -> documentService.deleteAll(uploadedDocumentReferences));
	}

	private RelationshipLink createPartnerContractLink(DocumentReference partnerDocRef, DocumentReference contractDocRef) {
		RelationshipRoleSpec partnerRole = new RelationshipRoleSpec(RelationshipModelConstants.RoleConstants.PARTNER_ROLE, partnerDocRef);
		RelationshipRoleSpec contractRole = new RelationshipRoleSpec(RelationshipModelConstants.RoleConstants.CONTRACT_ROLE, contractDocRef);
		LinkDescriptor linkDescriptor =
			new LinkDescriptor(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, Arrays.asList(contractRole, partnerRole),
				LinkPosition.TOP);

		JsonNode rootGroup = JACKSON_2_OBJECT_MAPPER.createObjectNode().put("Name", "TestName");
		rootGroup = JACKSON_2_OBJECT_MAPPER.createObjectNode().set(DocumentModelConstants.FieldConstants.CO_INSURED_ADDITIONAL_FIELDS_ROOT, rootGroup);
		RelationshipLinkSpec relationshipLinkSpec = linksFunctions.addLink(linkDescriptor, rootGroup);
		return relationshipLinkService.load(relationshipLinkSpec.getId());
	}
}
