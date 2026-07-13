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

import java.util.Arrays;

import org.testng.annotations.BeforeClass;

import tools.jackson.databind.JsonNode;
import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants.FieldConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.LinkPosition;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;

public class AbstractListITBase extends AbstractSpringContextIT {

	protected static final String INVALID_FIRST_ROLE_RM = "ContractCoinsuredPartner_InvalidFirstRole";
	protected static final String INVALID_SECOND_ROLE_RM = "ContractCoinsuredPartner_InvalidSecondRole";
	protected static final String INVALID_LINK_DM_RM = "ContractCoinsuredPartner_InvalidLinkModel";

	protected static final String WRONG_RELATIONSHIP_MODEL_MSG = "Wrong Relationship Model";

	protected static DocumentReference contractDocRef1;
	protected static DocumentReference contractDocRef2;
	protected static DocumentReference contractDocRef3;
	protected static DocumentReference contractDocRef4;
	protected static DocumentReference contractDocRef5;

	// Business Partners
	protected static DocumentReference partnerDocRef1;
	protected static DocumentReference partnerDocRef2;
	protected static DocumentReference partnerDocRef3;
	protected static DocumentReference partnerDocRef4;
	protected static DocumentReference partnerDocRef5;

	protected static DocumentReference partnerLtdDocRef1;
	protected static DocumentReference partnerLtdDocRef2;
	protected static DocumentReference partnerLtdDocRef3;
	protected static DocumentReference partnerLtdDocRef4;
	protected static DocumentReference partnerLtdDocRef5;


	protected static String contract2Partner2Link;
	protected static String contract2Partner3Link;
	protected static String contract2Partner4Link;
	protected static String contract2Partner5Link;
	protected static String contract3Partner1Link;
	protected static String contract3Partner2Link;
	protected static String contract3Partner3Link;
	protected static String contract3PartnerLtd1Link;
	protected static String contract3PartnerLtd2Link;

	@BeforeClass
	public void init() throws Exception {
		setUserTo(UserConstants.ADMIN_USER);

		// Document models
		modelsFunctions.createModels(
				PathConstants.ADDRESS_DOCUMENT_MODEL_PATH,
				PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH,
				PathConstants.BUSINESS_PARTNER_LTD_MODEL_PATH,
				PathConstants.BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH,
				PathConstants.COINSURED_ADDITIONAL_PARTNER_DOCUMENT_MODEL_PATH,
				PathConstants.CONTRACT_DOCUMENT_MODEL_PATH,
				PathConstants.CONTRACT_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH,
				PathConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH,
				PathConstants.PARTNER_ADDRESSES_RELATIONSHIP_MODEL_PATH,
				PathConstants.PARTNER_POSTAL_ADDRESS_RELATIONSHIP_MODEL_PATH,
				PathConstants.RELATIONSHIP_MODEL_INVALID_PATH + "ContractCoinsuredPartner_InvalidLinkModel.json",
				PathConstants.RELATIONSHIP_MODEL_INVALID_PATH + "ContractCoinsuredPartner_InvalidSecondRole.json",
				PathConstants.RELATIONSHIP_MODEL_INVALID_PATH + "ContractCoinsuredPartner_InvalidFirstRole.json"
		);

		contractDocRef1 = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, PathConstants.DOCUMENTS_SEARCH_PATH + "singleModelSort/Contract-alan.json");
		contractDocRef2 = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, PathConstants.DOCUMENTS_SEARCH_PATH + "singleModelSort/Contract-beth.json");
		contractDocRef3 = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, PathConstants.DOCUMENTS_SEARCH_PATH + "singleModelSort/Contract-karl.json");
		contractDocRef4 = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, PathConstants.DOCUMENTS_SEARCH_PATH + "singleModelSort/Contract-theo.json");
		contractDocRef5 = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, PathConstants.DOCUMENTS_SEARCH_PATH + "singleModelSort/Contract-zoltan.json");

		partnerDocRef1 = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.DOCUMENTS_SEARCH_FILTERING_PATH + "BusinessPartner-1.json");
		partnerDocRef2 = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.DOCUMENTS_SEARCH_FILTERING_PATH + "BusinessPartner-2.json");
		partnerDocRef3 = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.DOCUMENTS_SEARCH_FILTERING_PATH + "BusinessPartner-3.json");
		partnerDocRef4 = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.DOCUMENTS_SEARCH_FILTERING_PATH + "BusinessPartner-4.json");
		partnerDocRef5 = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.DOCUMENTS_SEARCH_FILTERING_PATH + "BusinessPartner-5.json");

		partnerLtdDocRef1 = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_LTD_MODEL, PathConstants.DOCUMENTS_SEARCH_FILTERING_PATH + "BusinessPartnerLTD-1.json");
		partnerLtdDocRef2 = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_LTD_MODEL, PathConstants.DOCUMENTS_SEARCH_FILTERING_PATH + "BusinessPartnerLTD-2.json");
		partnerLtdDocRef3 = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_LTD_MODEL, PathConstants.DOCUMENTS_SEARCH_FILTERING_PATH + "BusinessPartnerLTD-3.json");
		partnerLtdDocRef4 = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_LTD_MODEL, PathConstants.DOCUMENTS_SEARCH_FILTERING_PATH + "BusinessPartnerLTD-4.json");
		partnerLtdDocRef5 = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_LTD_MODEL, PathConstants.DOCUMENTS_SEARCH_FILTERING_PATH + "BusinessPartnerLTD-5.json");

		contract2Partner2Link = executeAddLinkOperation(contractDocRef2, partnerDocRef1).getId();
		contract2Partner3Link = executeAddLinkOperation(contractDocRef2, partnerDocRef2).getId();
		contract2Partner4Link = executeAddLinkOperation(contractDocRef2, partnerDocRef3).getId();
		contract2Partner5Link = executeAddLinkOperation(contractDocRef2, partnerDocRef4).getId();

		contract3Partner1Link = executeAddLinkOperation(contractDocRef3, partnerDocRef1).getId();
		contract3Partner2Link = executeAddLinkOperation(contractDocRef3, partnerDocRef2).getId();
		contract3Partner3Link = executeAddLinkOperation(contractDocRef3, partnerDocRef3).getId();

		contract3PartnerLtd1Link = executeAddLinkOperation(contractDocRef3, partnerLtdDocRef1).getId();
		contract3PartnerLtd2Link = executeAddLinkOperation(contractDocRef3, partnerLtdDocRef2).getId();
	}

	protected RelationshipLinkSpec executeAddLinkOperation(DocumentReference contractDocRef, DocumentReference partnerDocRef) {
		return executeAddLinkOperation(contractDocRef, partnerDocRef, LinkPosition.TOP);
	}

	protected RelationshipLinkSpec executeAddLinkOperation(DocumentReference contractDocRef, DocumentReference partnerDocRef, LinkPosition position) {
		RelationshipRoleSpec partnerRole = new RelationshipRoleSpec(RoleConstants.PARTNER_ROLE, partnerDocRef);
		RelationshipRoleSpec contractRole = new RelationshipRoleSpec(RoleConstants.CONTRACT_ROLE, contractDocRef);
		LinkDescriptor
			linkDescriptor = new LinkDescriptor(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, Arrays.asList(contractRole, partnerRole), position);
		return addLinkOperation.rpc(linkDescriptor, createLinkDocument());
	}

	protected JsonNode createLinkDocument() {
		return createLinkDocument("Karel");
	}

	protected JsonNode createLinkDocument(String name) {
		JsonNode rootGroup = JACKSON_2_OBJECT_MAPPER.createObjectNode().put("Name", name);
		return JACKSON_2_OBJECT_MAPPER.createObjectNode().set(FieldConstants.CO_INSURED_ADDITIONAL_FIELDS_ROOT, rootGroup);
	}

}
