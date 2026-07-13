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

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;

public class RpcDeleteDocumentOperationIT extends AbstractSpringContextIT {

	private DocumentReference businessPartner1DocRef;
	private DocumentReference businessPartner2DocRef;
	private DocumentReference businessPartner3DocRef;

	@BeforeMethod
	public void setUp() throws Exception {
		cleanUpTestEnvironment();
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.ADDRESS_DOCUMENT_MODEL_PATH);

		businessPartner1DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(
			DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.RPC_DOCUMENTS_PATH + "BusinessPartner-1.json");
		businessPartner2DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(
			DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.RPC_DOCUMENTS_PATH + "BusinessPartner-1.json");
		businessPartner3DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(
			DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.RPC_DOCUMENTS_PATH + "BusinessPartner-1.json");

		DocumentReference address1DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL,
			PathConstants.RPC_DOCUMENTS_PATH + "Address-1.json");
		DocumentReference address2DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL,
			PathConstants.RPC_DOCUMENTS_PATH + "Address-1.json");

		createPartnerAddressLink(businessPartner1DocRef, address1DocRef);
		createPartnerAddressLink(businessPartner2DocRef, address1DocRef);
		createPartnerAddressLink(businessPartner2DocRef, address2DocRef);
	}

	@Test
	public void deleteDocumentWithoutLinks() throws IOException {
		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "delete/delete_document_request.json");
		request = request.formatted(businessPartner3DocRef);
		Assert.assertTrue(sendRpcRequest(request).getFirst().isSuccess());
		Optional<DataServicesDocument> deletedDocument = documentRepository.findByDocumentReference(businessPartner3DocRef);
		Assert.assertFalse(deletedDocument.isPresent());
	}

	@Test
	public void deleteDocumentWithLinks() throws IOException {
		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "delete/delete_document_request.json");
		request = request.formatted(businessPartner2DocRef);
		Assert.assertTrue(sendRpcRequest(request).getFirst().isSuccess());
		Optional<DataServicesDocument> deletedDocument = documentRepository.findByDocumentReference(businessPartner2DocRef);
		Assert.assertFalse(deletedDocument.isPresent());
	}

	@Test
	public void deleteAllDocumentsInOneRequest() throws IOException {
		//multiple_deletes_request.json
		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "delete/delete_3_documents_request.json");
		request = request.formatted(
			businessPartner1DocRef,
			businessPartner2DocRef,
			businessPartner3DocRef
		);
		List<JsonRpc2Response> response = sendRpcRequest(request);
		response.forEach(e -> Assert.assertTrue(e.isSuccess()));
		Stream.of(businessPartner1DocRef, businessPartner2DocRef, businessPartner3DocRef)
			.map(documentReference -> documentRepository.findByDocumentReference(documentReference))
			.filter(Optional::isPresent)
			.forEach(document -> Assert.fail("Document " + document + " should have been deleted"));
	}

	private void createPartnerAddressLink(DocumentReference partner, DocumentReference address) throws IOException {
		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "deferred_constraints/add_link_PartnerAddress_1-address.json");
		request = request.formatted(
			RelationshipModelConstants.PRODUCT_CAMPAIGN_RM, partner, address
		);
		sendRpcRequest(request);
	}
}
