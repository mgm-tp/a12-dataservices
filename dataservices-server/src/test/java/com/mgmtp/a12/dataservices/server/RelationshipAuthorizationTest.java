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
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.LinkPosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_SUPER_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.COINSURED_ADDITIONAL_FIELDS_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.CONTRACT_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.COINSURED_ADDITIONAL_PARTNER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL;

@SpringBootTest
public class RelationshipAuthorizationTest extends AbstractSpringContextServerTests {

	@Autowired private RelationshipLinkService relationshipLinkService;

	@BeforeClass
	public void beforeClass() throws IOException {
		super.cleanUpTestEnvironment();
		super.changeUserInContext(UserConstants.ADMIN_USER);

		// Create all models with COMMON_ROLES by default
		createModelWithRole(BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH, COMMON_ROLES);
		createModelWithRole(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH, COMMON_ROLES);
		createModelWithRole(CONTRACT_DOCUMENT_MODEL_PATH, COMMON_ROLES);
		createModelWithRole(COINSURED_ADDITIONAL_PARTNER_DOCUMENT_MODEL_PATH, COMMON_ROLES);
		createModelWithRole(CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH, COMMON_ROLES);
	}

	@AfterClass
	public void afterClass() {
		super.changeUserInContext(UserConstants.ADMIN_USER);
		super.cleanUpTestEnvironment();
	}

	@DataProvider
	public Object[][] linkCreateDataProvider() {
		return new Object[][] {
				{ UserConstants.ADMIN_USER, true },
				{ UserConstants.ACTUATOR_USER, false },
				{ UserConstants.GUEST_USER, false },
				{ UserConstants.DOCUMENT_READ_USER, false },
				{ UserConstants.MODEL_READ_USER, false },
				{ UserConstants.MODEL_MANAGER_USER, false }
		};
	}

	@DataProvider
	public Object[][] linkModifyDataProvider() {
		return new Object[][] {
				{ UserConstants.ADMIN_USER, true },
				{ UserConstants.ACTUATOR_USER, false },
				{ UserConstants.GUEST_USER, false },
				{ UserConstants.DOCUMENT_READ_USER, false },
				{ UserConstants.MODEL_READ_USER, false },
				{ UserConstants.MODEL_MANAGER_USER, false }
		};
	}

	@DataProvider
	public Object[][] linkRelinkDocumentDataProvider() {
		return new Object[][] {
				{ UserConstants.ADMIN_USER, true },
				{ UserConstants.ACTUATOR_USER, false },
				{ UserConstants.GUEST_USER, true },
				{ UserConstants.DOCUMENT_READ_USER, false },
				{ UserConstants.MODEL_READ_USER, false },
				{ UserConstants.MODEL_MANAGER_USER, true }
		};
	}

	@DataProvider
	public Object[][] linkDeleteDataProvider() {
		return new Object[][] {
				{ UserConstants.ADMIN_USER, true },
				{ UserConstants.ACTUATOR_USER, false },
				{ UserConstants.GUEST_USER, false },
				{ UserConstants.DOCUMENT_READ_USER, false },
				{ UserConstants.MODEL_READ_USER, false },
				{ UserConstants.MODEL_MANAGER_USER, false }
		};
	}

	@DataProvider
	public Object[][] linkLoadDataProvider() {
		return new Object[][] {
				{ UserConstants.ADMIN_USER, true },
				{ UserConstants.ACTUATOR_USER, false },
				{ UserConstants.GUEST_USER, true },
				{ UserConstants.DOCUMENT_READ_USER, false },
				{ UserConstants.MODEL_READ_USER, false },
				{ UserConstants.MODEL_MANAGER_USER, true }
		};
	}

	@DataProvider
	public Object[][] modelsDataProvider() {
		return new Object[][] {
				{ BUSINESS_PARTNER_SUPER_MODEL },
				{ COINSURED_ADDITIONAL_FIELDS_MODEL }
		};
	}

	/**
	 * Tests link creation access control for different user roles.
	 * Verifies that only authorized users can create relationship links.
	 */
	@Test(dataProvider = "linkCreateDataProvider")
	public void testLinkCreateAccess(String username, boolean hasPermission) {
		changeUserInContext(username);

		assertAccessPermission(() -> {
			try {
				createRelationshipLink();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}, hasPermission, "Link creation for user: " + username);
	}

	/**
	 * Tests link creation when one of the models is restricted to admin only.
	 * Verifies that access is denied when guest user tries to create links with restricted models.
	 */
	@Test(dataProvider = "modelsDataProvider")
	public void testLinkCreation_restrictedModels(String modelName) throws IOException {
		updateModelRole(modelName, ADMIN_ROLE_ONLY);

		try {
			changeUserInContext(UserConstants.GUEST_USER);
			assertAccessPermission(() -> {
				try {
					createRelationshipLink();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}, false, "Link creation for user: " + UserConstants.GUEST_USER + " with restricted model: " + modelName);
		} finally {
			updateModelRole(modelName, COMMON_ROLES);
		}
	}

	/**
	 * Tests link modification access control for different user roles.
	 * Verifies that only authorized users can update existing relationship links.
	 */
	@Test(dataProvider = "linkModifyDataProvider")
	public void testLinkModifyAccess(String username, boolean hasPermission) throws IOException {
		LinkDescriptor linkDescriptor = createLinkDescriptor();
		RelationshipLink relationshipLink = createRelationshipLink(linkDescriptor);
		changeUserInContext(username);
		String linkDocumentContent = resourceFunctions.loadResource(COINSURED_DOCUMENT_FILE);

		assertAccessPermission(
			() -> relationshipLinkService.update(relationshipLink.getId(), linkDescriptor, linkDocumentContent),
			hasPermission,
			"Link modification for user: " + username
		);
	}

	/**
	 * Tests relink access control for different user roles.
	 * Verifies that only authorized users can relink relationship documents.
	 */
	@Test(dataProvider = "linkRelinkDocumentDataProvider")
	public void testRelinkAccess(String username, boolean hasPermission) throws IOException {
		LinkDescriptor linkDescriptor = createLinkDescriptor();
		RelationshipLink relationshipLink = createRelationshipLink(linkDescriptor);
		changeUserInContext(username);

		assertAccessPermission(
			() -> relationshipLinkService.relink(linkDescriptor, relationshipLink.getId()),
			hasPermission,
			"Relinking for user: " + username
		);
	}

	/**
	 * Tests link deletion access control for different user roles.
	 * Verifies that only authorized users can delete relationship links.
	 */
	@Test(dataProvider = "linkDeleteDataProvider")
	public void testDeleteLinkAccess(String username, boolean hasPermission) throws IOException {
		RelationshipLink relationshipLink = createRelationshipLink();
		changeUserInContext(username);

		assertAccessPermission(
			() -> relationshipLinkService.delete(relationshipLink.getId()),
			hasPermission,
			"Link deletion for user: " + username
		);
	}

	/**
	 * Tests link loading access control for different user roles.
	 * Verifies that only authorized users can load relationship links.
	 */
	@Test(dataProvider = "linkLoadDataProvider")
	public void testLoadLinkAccess(String username, boolean hasPermission) throws IOException {
		RelationshipLink relationshipLink = createRelationshipLink();
		changeUserInContext(username);

		assertAccessPermission(
			() -> relationshipLinkService.load(relationshipLink.getId()),
			hasPermission,
			"Link loading for user: " + username
		);
	}

	/**
	 * Tests link loading when link document model is restricted to admin only.
	 * Verifies that guest users can still load the link even when the link document model is restricted.
	 */
	@Test(dataProvider = "modelsDataProvider")
	public void testLoadLinkAccess_restrictedLinkDocument(String modelName) throws IOException {
		RelationshipLink relationshipLink = createRelationshipLink();
		updateModelRole(modelName, ADMIN_ROLE_ONLY);

		try {
			changeUserInContext(UserConstants.GUEST_USER);
			assertAccessPermission(
					() -> relationshipLinkService.load(relationshipLink.getId()),
					false,
					"Link loading for user: " + UserConstants.GUEST_USER + " with restricted model: " + modelName
			);
		} finally {
			updateModelRole(modelName, COMMON_ROLES);
		}
	}

	private LinkDescriptor createLinkDescriptor() throws IOException {
		DocumentReference contract = documentFunctions.createDocumentFromFileAndGetDocRef(CONTRACT_DOCUMENT_MODEL, CONTRACT_DOCUMENT_FILE);
		DocumentReference businessPartner = documentFunctions.createDocumentFromFileAndGetDocRef(BUSINESS_PARTNER_DOCUMENT_MODEL, PARTNER_DOCUMENT_FILE);
		DocumentReference additionalFields = documentFunctions.createDocumentFromFileAndGetDocRef(COINSURED_ADDITIONAL_FIELDS_MODEL, COINSURED_DOCUMENT_FILE);
		LinkDescriptor linkDescriptor = linksFunctions.createLinkDescriptor(
				CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL,
				"Contract",
				contract,
				"Partner",
				businessPartner,
				LinkPosition.TOP
		);
		linkDescriptor.setLinkDocumentDocRef(additionalFields);
		return linkDescriptor;
	}

	private RelationshipLink createRelationshipLink() throws IOException {
		LinkDescriptor linkDescriptor = createLinkDescriptor();
		return createRelationshipLink(linkDescriptor);
	}

	private RelationshipLink createRelationshipLink(LinkDescriptor linkDescriptor) {
		return relationshipLinkService.create(linkDescriptor, linkDescriptor.getLinkDocumentDocRef());
	}

}
