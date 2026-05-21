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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.common.exception.ErrorDetail;
import com.mgmtp.a12.dataservices.common.exception.ErrorLevel;

public class RpcModifyDocumentOperationIT extends AbstractSpringContextIT {
	
	private static final String NEW_PARTNER_NAME = "NEW NAME";
	private static final String NEW_ADDRESS_LOCATION = "NEW LOCATION";

	private DocumentReference businessPartner1DocRef;
	private DocumentReference businessPartner2DocRef;

	private DocumentReference address1DocRef;
	private DocumentReference address2DocRef;

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

		address1DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, PathConstants.RPC_DOCUMENTS_PATH + "Address-1.json");
		address2DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, PathConstants.RPC_DOCUMENTS_PATH + "Address-2.json");
}

	@Test
	public void modifySingleDocument() throws IOException {
		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "templates/document_modify_template.json");
		String newBusinessPartnerVersion = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "modify/doc_businesspartner_content.json");
		request = String.format(request, businessPartner1DocRef, newBusinessPartnerVersion);

		JsonRpc2Response response = sendRpcRequest(request).getFirst();
		Assert.assertTrue(response.isSuccess());
		Assert.assertTrue(response.getResult().isNull());
		Assert.assertEquals(response.getId(), "modifyDocument");

		assertModification(businessPartner1DocRef, NEW_PARTNER_NAME);
	}

	@Test
	public void modifyMultipleDocuments() throws IOException {
		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "modify/modify_multiple_document_request.json");
		String newBusinessPartnerVersion = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "modify/doc_businesspartner_content.json");
		String newAddressVersion = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "modify/doc_address_content.json");
		request =
			String.format(request, 
					businessPartner1DocRef, newBusinessPartnerVersion, 
					businessPartner2DocRef, newBusinessPartnerVersion, 
					address1DocRef, newAddressVersion, 
					address2DocRef, newAddressVersion);

		List<JsonRpc2Response> response = sendRpcRequest(request);
		Assert.assertFalse(response.stream().anyMatch(e -> !e.isSuccess()));

		assertModification(businessPartner1DocRef, NEW_PARTNER_NAME);
		assertModification(businessPartner2DocRef, NEW_PARTNER_NAME);
		assertModification(address1DocRef, NEW_ADDRESS_LOCATION);
		assertModification(address2DocRef, NEW_ADDRESS_LOCATION);
	}

	@Test
	public void modifyNonExistingDocument() throws IOException {
		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "templates/document_modify_template.json");
		String newBusinessPartnerVersion = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "modify/doc_businesspartner_content.json");
		request = String.format(request, DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL + "/155477884456", newBusinessPartnerVersion);
		JsonRpc2Response response = sendRpcRequest(request).getFirst();
		OperationError expectedError = createErrorTemplate("error.modify_document.document.notFound", "Document [BusinessPartner/155477884456] was not found",
			"Document [BusinessPartner/155477884456] not found", "modifyDocument");
		assertExceptions(expectedError, createOperationError(response));
	}

	private OperationError createErrorTemplate(String keyPrefix, String shortMessage, String longMessage, String operationId) {
		LocalizedEntry shortMessageObject = new LocalizedEntry(keyPrefix + ".title", shortMessage);
		LocalizedEntry longMessageObject =
			new LocalizedEntry(keyPrefix + ".description", longMessage);

		OperationError.OperationErrorBuilder builder = OperationError.builder()
			.operationId(operationId)
			.level(ErrorLevel.ERROR)
			.shortMessage(shortMessageObject)
			.longMessage(longMessageObject)
			.errorDetail(ErrorDetail.createGenericError());
		return builder.build();
	}

	private void assertExceptions(OperationError expectedError, OperationError actualError) {
		Assert.assertEquals(expectedError.getShortMessage().getKey(), actualError.getShortMessage().getKey());
		Assert.assertEquals(expectedError.getShortMessage().getDefaultMessage(), actualError.getShortMessage().getDefaultMessage());
		Assert.assertEquals(expectedError.getLongMessage().getKey(), actualError.getLongMessage().getKey());
		Assert.assertEquals(expectedError.getLongMessage().getDefaultMessage(), actualError.getLongMessage().getDefaultMessage());
	}
	
	private void assertModification(DocumentReference docRef, String expectedValue) {
		Optional<DataServicesDocument> modifiedDocument = documentRepository.findByDocumentReference(docRef);
		Assert.assertTrue(modifiedDocument.isPresent());
		Assert.assertTrue(documentFunctions.convertDocumentToJson(modifiedDocument.get()).contains(expectedValue));
	}
}
