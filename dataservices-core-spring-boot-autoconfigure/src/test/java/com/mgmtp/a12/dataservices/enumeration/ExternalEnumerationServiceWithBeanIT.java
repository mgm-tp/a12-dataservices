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
package com.mgmtp.a12.dataservices.enumeration;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.enumeration.external.ExternalEnumeration;
import com.mgmtp.a12.dataservices.enumeration.external.ExternalEnumerationService;

@SpringBootTest(properties = { "mgmtp.a12.dataservices.enumeration.pageLimit=2"})
public class ExternalEnumerationServiceWithBeanIT extends AbstractSpringContextIT {

	@Autowired private ExternalEnumerationService externalEnumerationService;

	@BeforeClass
	public void setup() throws IOException {
		super.cleanUpTestEnvironment();
		setUserTo(UserConstants.ADMIN_USER);
		modelsFunctions.createModel(PathConstants.CONTRACT_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
	}

	@Test
	public void loadExternalEnumerationForModel() throws IOException {
		documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "Contract-1.json");
		documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "Contract-1.json");
		List<ExternalEnumeration> externalEnumerations = externalEnumerationService.loadExternalEnumerationForModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL);
		Assert.assertNotNull(externalEnumerations);
		Assert.assertEquals(externalEnumerations.size(), 2);
	}

	@Test
	public void loadExternalEnumerationForModelMaxPageSizeApplied() throws IOException {
		documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "Contract-1.json");
		documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "Contract-1.json");
		documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "Contract-1.json");
		List<ExternalEnumeration> externalEnumerations = externalEnumerationService.loadExternalEnumerationForModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL);
		Assert.assertNotNull(externalEnumerations);
		Assert.assertEquals(externalEnumerations.size(), 2);
	}

	@Test
	public void loadExternalEnumerationForWrongModel() throws IOException {
		documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "BusinessPartner-1.json");
		List<ExternalEnumeration> externalEnumerations = externalEnumerationService.loadExternalEnumerationForModel(
			DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);
		Assert.assertNotNull(externalEnumerations);
		Assert.assertTrue(CollectionUtils.isEmpty(externalEnumerations));
	}
}
