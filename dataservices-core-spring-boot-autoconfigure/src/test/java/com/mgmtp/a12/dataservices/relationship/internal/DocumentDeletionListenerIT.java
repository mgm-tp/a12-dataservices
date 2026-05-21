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
package com.mgmtp.a12.dataservices.relationship.internal;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.exception.IntegrityException;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;
import com.mgmtp.a12.dataservices.rpc.links.AbstractLinkIT;

public class DocumentDeletionListenerIT extends AbstractLinkIT {

	@Autowired
	private DocumentService documentService;

	@Autowired
	private RelationshipLinkService relationshipLinkService;

	@Test(expectedExceptions = IntegrityException.class)
	public void onDeleteDocument_documentIsUsedAsLinkDocument() {
		// given
		DataServicesDocument coInsuredAdditionalFields = createDocument(DocumentModelConstants.COINSURED_ADDITIONAL_FIELDS_MODEL, PathConstants.DOCUMENTS_PATH + "CoInsuredAdditionalFields-1.json");
		createPartnerContractLinkWithLinkDocRef(contract1DocRef, partner1DocRef, coInsuredAdditionalFields.getMetadata().getDocRef());

		// then
		documentService.delete(coInsuredAdditionalFields.getMetadata().getDocRef());
	}

	@Test(expectedExceptions = IntegrityException.class)
	public void onDeleteMultiDocuments_documentIsUsedAsLinkDocument() {
		// given
		DataServicesDocument coInsuredAdditionalFields1 = createDocument(DocumentModelConstants.COINSURED_ADDITIONAL_FIELDS_MODEL, PathConstants.DOCUMENTS_PATH + "CoInsuredAdditionalFields-1.json");
		DataServicesDocument coInsuredAdditionalFields2 = createDocument(DocumentModelConstants.COINSURED_ADDITIONAL_FIELDS_MODEL, PathConstants.DOCUMENTS_PATH + "CoInsuredAdditionalFields-1.json");
		createPartnerContractLinkWithLinkDocRef(contract1DocRef, partner1DocRef, coInsuredAdditionalFields1.getMetadata().getDocRef());

		// then
		documentService.deleteAll(List.of(coInsuredAdditionalFields1.getMetadata().getDocRef(), coInsuredAdditionalFields2.getMetadata().getDocRef()));
	}

	private void createPartnerContractLinkWithLinkDocRef(
		DocumentReference contractDocRef,
		DocumentReference partnerDocRef,
		DocumentReference linkDocRef
	) {
		LinkDescriptor linkDescriptor = new LinkDescriptor();
		linkDescriptor.setRelationshipModel(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL);

		RelationshipRoleSpec roleSpec1 = new RelationshipRoleSpec();
		roleSpec1.setRole(RelationshipModelConstants.RoleConstants.CONTRACT_ROLE);
		roleSpec1.setModelName(contractDocRef.getDocumentModelName());
		roleSpec1.setDocRef(contractDocRef);

		RelationshipRoleSpec roleSpec2 = new RelationshipRoleSpec();
		roleSpec2.setRole(RelationshipModelConstants.RoleConstants.PARTNER_ROLE);
		roleSpec2.setModelName(partnerDocRef.getDocumentModelName());
		roleSpec2.setDocRef(partnerDocRef);
		linkDescriptor.setEntities(List.of(roleSpec1, roleSpec2));

		relationshipLinkService.create(linkDescriptor, linkDocRef);
	}
}
