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
package com.mgmtp.a12.dataservices.rpc.dispatchers;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.persistence.RelationshipLinkRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class AbstractDispatcherClass extends AbstractSpringContextIT {

	DocumentReference product2DocRef;
	DocumentReference campaign1DocRef;

	@Autowired protected RelationshipLinkRepository relationshipLinkRepository;

	@BeforeClass
	public void setUp() throws Exception {
		setUserTo(UserConstants.ADMIN_USER);
		modelsFunctions.createModel(PathConstants.DOCUMENT_MODEL_PATH + "DomainProduct.json");
		modelsFunctions.createModel(PathConstants.CAMPAIGN_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.ADDITIONAL_FIELDS_MODEL_PATH);

		//15-19
		String productDataPath = PathConstants.RPC_DOCUMENTS_PATH + "Product-Data.json";

		DocumentReference product1DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.PRODUCT_MODEL_NAME, productDataPath);
		DocumentReference product3DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.PRODUCT_MODEL_NAME, productDataPath);
		DocumentReference product4DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.PRODUCT_MODEL_NAME, productDataPath);
		DocumentReference product5DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.PRODUCT_MODEL_NAME, productDataPath);

		product2DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.PRODUCT_MODEL_NAME, productDataPath);
		campaign1DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CAMPAIGN_MODEL_NAME, PathConstants.RPC_DOCUMENTS_PATH + "Campaign-1.json");

		DocumentReference campaign2DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CAMPAIGN_MODEL_NAME, PathConstants.RPC_DOCUMENTS_PATH + "Campaign-2.json");
		DocumentReference campaign3DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CAMPAIGN_MODEL_NAME, PathConstants.RPC_DOCUMENTS_PATH + "Campaign-1.json");
		documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CAMPAIGN_MODEL_NAME, PathConstants.RPC_DOCUMENTS_PATH + "Campaign-2.json");

		createModel(resourceFunctions.loadResource(PathConstants.PRODUCT_CAMPAIGN_RM_PATH));

		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "add/add_links_request_productcampaign.json");
		request = request.formatted(product1DocRef, campaign1DocRef, product2DocRef, campaign1DocRef, product3DocRef,
			campaign2DocRef, product4DocRef, campaign2DocRef, product5DocRef, campaign1DocRef, product5DocRef,
			campaign2DocRef, product1DocRef, campaign3DocRef);

		handleErrors(sendRpcRequest(request));
	}

}
