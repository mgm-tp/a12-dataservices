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
package com.mgmtp.a12.dataservices.server;

import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.relationship.ModelGraphElement;
import com.mgmtp.a12.dataservices.relationship.ModelGraphRoot;
import com.mgmtp.a12.dataservices.relationship.internal.ModelGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_SUPER_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.COINSURED_ADDITIONAL_FIELDS_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.CONTRACT_CDM_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.CONTRACT_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.PathConstants.ADDRESS_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.COINSURED_ADDITIONAL_PARTNER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_AMENDMENT_RELATIONSHIP_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_CDM_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.CONTRACT_AMENDMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL;

public class ModelGraphAuthorizationTest extends AbstractSpringContextServerTests {

	@Autowired ModelGraphService modelGraphService;

	@BeforeClass
	public void setupAllTests() throws IOException {
		super.cleanUpTestEnvironment();
		super.changeUserInContext(UserConstants.ADMIN_USER);

		setupAllModelsWithCommonRoles();
	}

	@AfterClass
	public void cleanupAllTests() {
		super.changeUserInContext(UserConstants.ADMIN_USER);
		super.cleanUpTestEnvironment();
	}

	@Test
	public void testModelGraphRetrieval_restrictedDocumentModel() throws IOException {
		updateModelRole(BUSINESS_PARTNER_SUPER_MODEL, ADMIN_ROLE_ONLY);

		try {
			ModelGraphRoot modelGraphRoot = getModelGraphAsGuest();

			assertComposeDocumentModels(modelGraphRoot);
			assertDocumentModels(modelGraphRoot, CONTRACT_DOCUMENT_MODEL, COINSURED_ADDITIONAL_FIELDS_MODEL);
			assertRelationships(modelGraphRoot, CONTRACT_AMENDMENT_MODEL);
		} finally {
			updateModelRole(BUSINESS_PARTNER_SUPER_MODEL, COMMON_ROLES);
		}
	}

	@Test
	public void testModelGraphRetrieval_restrictedLinkDocument() throws IOException {
		updateModelRole(COINSURED_ADDITIONAL_FIELDS_MODEL, ADMIN_ROLE_ONLY);
		updateModelRole(CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, ADMIN_ROLE_ONLY);

		try {
			ModelGraphRoot modelGraphRoot = getModelGraphAsGuest();

			assertComposeDocumentModels(modelGraphRoot);
			assertDocumentModels(modelGraphRoot, BUSINESS_PARTNER_SUPER_MODEL, CONTRACT_DOCUMENT_MODEL);
			assertRelationships(modelGraphRoot, CONTRACT_AMENDMENT_MODEL);
		} finally {
			updateModelRole(COINSURED_ADDITIONAL_FIELDS_MODEL, COMMON_ROLES);
			updateModelRole(CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, COMMON_ROLES);
		}
	}

	@Test
	public void testModelGraphRetrieval_restrictedRelationship() throws IOException {
		updateModelRole(CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, ADMIN_ROLE_ONLY);

		try {
			ModelGraphRoot modelGraphRoot = getModelGraphAsGuest();

			assertComposeDocumentModels(modelGraphRoot);
			assertDocumentModels(modelGraphRoot, BUSINESS_PARTNER_SUPER_MODEL, CONTRACT_DOCUMENT_MODEL, COINSURED_ADDITIONAL_FIELDS_MODEL);
			assertRelationships(modelGraphRoot, CONTRACT_AMENDMENT_MODEL);
		} finally {
			updateModelRole(CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, COMMON_ROLES);
		}
	}

	@Test
	public void testAllModelsShouldBePresent() {
		ModelGraphRoot modelGraphRoot = getModelGraphAsGuest();

		assertComposeDocumentModels(modelGraphRoot, CONTRACT_CDM_MODEL);
		assertDocumentModels(modelGraphRoot, BUSINESS_PARTNER_SUPER_MODEL, CONTRACT_DOCUMENT_MODEL, COINSURED_ADDITIONAL_FIELDS_MODEL);
		assertRelationships(modelGraphRoot, CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, CONTRACT_AMENDMENT_MODEL);
	}

	private void setupAllModelsWithCommonRoles() throws IOException {
		createModelWithRole(ADDRESS_DOCUMENT_MODEL_PATH, COMMON_ROLES);
		createModelWithRole(BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH, COMMON_ROLES);
		createModelWithRole(CONTRACT_DOCUMENT_MODEL_PATH, COMMON_ROLES);
		createModelWithRole(COINSURED_ADDITIONAL_PARTNER_DOCUMENT_MODEL_PATH, COMMON_ROLES);
		createModelWithRole(CONTRACT_AMENDMENT_RELATIONSHIP_MODEL_PATH, COMMON_ROLES);
		createModelWithRole(CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH, COMMON_ROLES);
		createModelWithRole(CONTRACT_CDM_MODEL_PATH, COMMON_ROLES);
	}

	private ModelGraphRoot getModelGraphAsGuest() {
		changeUserInContext(UserConstants.GUEST_USER);
		return modelGraphService.constructModelGraph();
	}

	private void assertRelationships(ModelGraphRoot modelGraphRoot, String... expectedRelationships) {
		Set<String> actualRelationships = modelGraphRoot.getRelationshipModels();
		for (String expected : expectedRelationships) {
			Assert.assertTrue(
					actualRelationships.stream().anyMatch(relationship -> relationship.contains(expected)),
					"Expected relationship model not found: " + expected + ". Actual: " + actualRelationships
			);
		}
	}

	private void assertDocumentModels(ModelGraphRoot modelGraphRoot, String... expectedDocumentModels) {
		Set<String> actualModelIds = modelGraphRoot.getDocumentModels().stream()
				.map(ModelGraphElement::getModelId)
				.collect(Collectors.toSet());
		for (String expected : expectedDocumentModels) {
			Assert.assertTrue(
					actualModelIds.contains(expected),
					"Expected document model not found: " + expected + ". Actual: " + actualModelIds
			);
		}
	}

	private void assertComposeDocumentModels(ModelGraphRoot modelGraphRoot, String... expectedComposeDocumentModels) {
		Set<String> actualModelIds = modelGraphRoot.getComposeDocumentModels().stream()
				.map(ModelGraphElement::getModelId)
				.collect(Collectors.toSet());
		for (String expected : expectedComposeDocumentModels) {
			Assert.assertTrue(
					actualModelIds.contains(expected),
					"Expected compose document model not found: " + expected + ". Actual: " + actualModelIds
			);
		}
	}

}

