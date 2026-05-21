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

import java.io.StringWriter;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants.FieldConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.operation.internal.ModifyLinkOperation;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.RelationshipLinkRepository;
import com.mgmtp.a12.dataservices.relationship.OffsetBasedPageRequest;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;

public class ModifyLinkOperationIT extends AbstractListITBase {

	@Autowired private ModifyLinkOperation modifyLinkOperation;
	@Autowired private RelationshipLinkRepository relationshipLinkRepository;
	@Autowired private DocumentService documentService;

	@DataProvider(name = "provideModifyDataForSuccessTest")
	public Object[][] provideModifyDataForSuccessTest() {
		return new Object[][] {
			{ contractDocRef2, partnerDocRef1, "Constantin" },
			{ contractDocRef2, partnerDocRef2, "Michael" }
		};
	}

	@Test(dataProvider = "provideModifyDataForSuccessTest")
	public void successfulModifyRelationshipLinkTest(DocumentReference contractDocRef, DocumentReference docRef2, String coInsuredName) {
		JsonNode linkDocument = createLinkDocument(coInsuredName);
		modifyLinkOperation.rpc(createModifyLinkParameter(contractDocRef, docRef2), linkDocument);
		checkIfEntityWasUpdated(contractDocRef, docRef2, coInsuredName);
	}

	protected RelationshipLinkSpec createModifyLinkParameter(DocumentReference docRef1, DocumentReference docRef2) {
		List<? extends RelationshipLink> byEntities =
			relationshipLinkRepository.findByRelationshipModelNameAndSourceAndTarget(
				RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL,
				RoleConstants.CONTRACT_ROLE,
				docRef1,
				RoleConstants.PARTNER_ROLE,
				docRef2,
				OffsetBasedPageRequest.unpaged()
			).getContent();
		LinkDescriptor
			linkDescriptor = createLinkDescriptor(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, RoleConstants.CONTRACT_ROLE, docRef1, RoleConstants.PARTNER_ROLE, docRef2);
		return new RelationshipLinkSpec(linkDescriptor, byEntities.get(0).getId().toString());
	}

	private void checkIfEntityWasUpdated(DocumentReference docRef1, DocumentReference docRef2, String expectedValue) {
		List<? extends RelationshipLink> entities =
			relationshipLinkRepository.findByRelationshipModelNameAndSourceAndTarget(
				RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL,
				RoleConstants.CONTRACT_ROLE,
				docRef1,
				RoleConstants.PARTNER_ROLE,
				docRef2,
				OffsetBasedPageRequest.unpaged()
			).getContent();
		StringWriter sw = new StringWriter();
		DataServicesDocument document = documentService.load(entities.get(0).getLinkDocumentDocRef())
			.orElseThrow(() -> new NotFoundException(ExceptionKeys.DOCUMENT_NOT_FOUND_ERROR_KEY, String.format("Document [%s] not found", entities.get(0).getLinkDocumentDocRef())));
		documentSupport.convertDocumentToJSON(document.getKernelDocument(), sw);

		JSONObject coInsuredRoot = new JSONObject(sw.toString()).getJSONObject(FieldConstants.CO_INSURED_ADDITIONAL_FIELDS_ROOT);
		Assert.assertEquals(coInsuredRoot.getString("Name"), expectedValue);
		Assert.assertEquals(coInsuredRoot.getString("ID"), "0000-0000-0000-0000");
	}

}
