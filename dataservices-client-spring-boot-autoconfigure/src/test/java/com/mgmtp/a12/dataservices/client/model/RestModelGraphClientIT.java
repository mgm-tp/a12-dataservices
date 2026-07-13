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
package com.mgmtp.a12.dataservices.client.model;

import java.util.ArrayList;
import java.util.List;

import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.client.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.relationship.ModelGraphDocumentModelElement;
import com.mgmtp.a12.dataservices.relationship.ModelGraphRoot;

public class RestModelGraphClientIT extends AbstractSpringContextIT {

	@BeforeClass
	public void setUp() {
		createModelFromFile(CONTRACT_MODEL_FILE);
		createModelFromFile(ADDRESS_MODEL_FILE);
		createModelFromFile(BUSINESS_PARTNER_MODEL_FILE);
		createModelFromFile(BUSINESS_PARTNER_SUPER_MODEL_FILE);
		createModelFromFile(CO_INSURED_ADDITIONAL_FIELD_MODEL_FILE);
		createModelFromFile(CONTRACT_CO_INSURED_PARTNER_MODEL_FILE);
	}

	@AfterClass
	public void cleanUp() {
		cleanupRelationshipModel();
		cleanUpByDocumentModel(CONTRACT_MODEL_NAME);
		cleanUpByDocumentModel(ADDRESS_MODEL_NAME);
		cleanUpByDocumentModel(BUSINESS_PARTNER_MODEL_NAME);
		cleanUpByDocumentModel(BUSINESS_PARTNER_SUPER_MODEL_NAME);
		cleanUpByDocumentModel(CO_INSURED_ADDITIONAL_FIELD_MODEL_NAME);
	}

	@Test
	public void testModelGraphRoot() {
		ModelGraphRoot modelGraphRoot = relationshipClient.getModelGraph();

		List<String> relationshipModels = new ArrayList<>(modelGraphRoot.getRelationshipModels());

		List<String> documentModelNames = new ArrayList<>(modelGraphRoot.getDocumentModels()).stream()
				.map(ModelGraphDocumentModelElement::getModelId)
				.toList();
		List<String> relationshipModelNames = relationshipModels.stream()
				.map(e -> createHeader(e).getId())
				.toList();

		Assert.assertEquals(documentModelNames.size(), 5);
		Assert.assertEquals(relationshipModelNames.size(), 1);

		Assert.assertTrue(documentModelNames.contains(CONTRACT_MODEL_NAME));
		Assert.assertTrue(documentModelNames.contains(ADDRESS_MODEL_NAME));
		Assert.assertTrue(documentModelNames.contains(BUSINESS_PARTNER_MODEL_NAME));
		Assert.assertTrue(documentModelNames.contains(BUSINESS_PARTNER_SUPER_MODEL_NAME));
		Assert.assertTrue(documentModelNames.contains(CO_INSURED_ADDITIONAL_FIELD_MODEL_NAME));

		JSONAssert.assertEquals(relationshipModels.getFirst(), readFile(CONTRACT_CO_INSURED_PARTNER_RELATIONSHIP_MODEL_VALID_JSON), false);
	}

}
