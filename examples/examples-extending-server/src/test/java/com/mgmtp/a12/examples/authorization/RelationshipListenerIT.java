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
package com.mgmtp.a12.examples.authorization;

import com.mgmtp.a12.dataservices.JsonFunctions;
import com.mgmtp.a12.dataservices.LinksFunctions;
import com.mgmtp.a12.dataservices.ResourceFunctions;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.LinkPosition;
import com.mgmtp.a12.examples.AbstractITBase;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.fail;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.COINSURED_ADDITIONAL_FIELDS_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.CONTRACT_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.COINSURED_ADDITIONAL_PARTNER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL;

/**
 * Integration tests for relationship link authorization.
 *
 * Tests verify that the authorization listeners correctly enforce permissions for:
 * - Link creation - requires `LinkWrite` role
 * - Link modification (update) - requires `LinkWrite` role
 * - Link relinking - requires `LinkWrite` role
 * - Link deletion - requires `LinkWrite` role
 * - Link loading (read) - requires `LinkRead` or `LinkWrite` role
 *
 * Test users:
 * - `admin` - Full access (all operations allowed)
 * - `LinkWriter` - Write operations only
 * - `LinkReader` - Read operations only
 * - `guest` - Read operations only
 * - `ActuatorUser` - No access (all operations denied)
 *
 * @see RelationshipLinkService
 */
@SpringBootTest(properties = {
		"mgmtp.a12.uaa.authentication.principal.access-rights-resource=classpath:authorization/examplesTestUserRoles.yaml",
		"mgmtp.a12.uaa.authentication.principal.local-config.user-resources=" +
				"classpath:authorization/users/admin.yaml," +
				"classpath:authorization/users/guest.yaml," +
				"classpath:authorization/users/ActuatorUser.yaml," +
				"classpath:authorization/users/tester.yaml," +
				"classpath:authorization/users/TestAdmin.yaml," +
				"classpath:authorization/users/LinkReader.yaml," +
				"classpath:authorization/users/LinkWriter.yaml"
})
public class RelationshipListenerIT extends AbstractITBase {

	@Autowired LinksFunctions linksFunctions;
	@Autowired ResourceFunctions resourceFunctions;
	@Autowired private RelationshipLinkService relationshipLinkService;

	private static final String LINK_WRITER_USER = "LinkWriter";
	private static final String LINK_READER_USER = "LinkReader";
	private static final String LINK_WRITE_ROLE = "LinkWrite";
	private static final String LINK_READ_ROLE = "LinkRead";
	private static final String CUSTOM_ROLES_FOR_TEST = String.format("%s,%s,%s,%s",
			UserConstants.ADMIN_USER,
			UserConstants.GUEST_USER,
			LINK_WRITE_ROLE,
			LINK_READ_ROLE
	);
	private static final String CONTRACT_ROLE_NAME = "Contract";
	private static final String PARTNER_ROLE_NAME = "Partner";

	private LinkDescriptor linkDescriptor;

	@BeforeClass
	public void initMethod() throws IOException {
		testEnvironmentCleaner.cleanUpTestEnvironment(dataSource, cacheManager);
		changeUserInContext("admin");

		createModelWithCustomRoles(BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH);
		createModelWithCustomRoles(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
		createModelWithCustomRoles(CONTRACT_DOCUMENT_MODEL_PATH);
		createModelWithCustomRoles(COINSURED_ADDITIONAL_PARTNER_DOCUMENT_MODEL_PATH);
		createModelWithCustomRoles(CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH);
	}

	@BeforeMethod
	public void setupTestData() throws IOException {
		changeUserInContext("admin");

		// Create fresh test data for each test method
		DocumentReference contract = documentFunctions.createDocumentFromFileAndGetDocRef(
				CONTRACT_DOCUMENT_MODEL, CONTRACT_DOCUMENT_FILE);
		DocumentReference businessPartner = documentFunctions.createDocumentFromFileAndGetDocRef(
				BUSINESS_PARTNER_DOCUMENT_MODEL, PARTNER_DOCUMENT_FILE);
		DocumentReference additionalFields = documentFunctions.createDocumentFromFileAndGetDocRef(
				COINSURED_ADDITIONAL_FIELDS_MODEL, COINSURED_DOCUMENT_FILE);

		linkDescriptor = linksFunctions.createLinkDescriptor(
				CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL,
				CONTRACT_ROLE_NAME,
				contract,
				PARTNER_ROLE_NAME,
				businessPartner,
				LinkPosition.TOP
		);
		linkDescriptor.setLinkDocumentDocRef(additionalFields);
	}

	@AfterClass
	public void cleanup() {
		changeUserInContext("admin");
		testEnvironmentCleaner.cleanUpTestEnvironment(dataSource, cacheManager);
	}

	@DataProvider
	public Object[][] linkWriteOperationsDataProvider() {
		return new Object[][] {
				{ UserConstants.ADMIN_USER, true },
				{ UserConstants.ACTUATOR_USER, false },
				{ UserConstants.GUEST_USER, false },
				{ LINK_READER_USER, false },
				{ LINK_WRITER_USER, true }
		};
	}

	@DataProvider
	public Object[][] linkReadOperationsDataProvider() {
		return new Object[][] {
				{ UserConstants.ADMIN_USER, true },
				{ UserConstants.ACTUATOR_USER, false },
				{ UserConstants.GUEST_USER, true },
				{ LINK_READER_USER, true },
				{ LINK_WRITER_USER, true }
		};
	}

	@Test(dataProvider = "linkWriteOperationsDataProvider",
			description = "Should enforce authorization for link creation")
	public void shouldCheckLinkCreateAccess(String username, boolean hasPermission) {
		testLinkOperation(username, hasPermission, "link creation", () ->
				relationshipLinkService.create(linkDescriptor, linkDescriptor.getLinkDocumentDocRef())
		);
	}

	@Test(dataProvider = "linkWriteOperationsDataProvider",
			description = "Should enforce authorization for link modification")
	public void shouldCheckLinkModifyAccess(String username, boolean hasPermission) throws IOException {
		String linkId = createTestLinkAsAdmin();
		String linkDocumentContent = resourceFunctions.loadResource(COINSURED_DOCUMENT_FILE);

		testLinkOperation(username, hasPermission, "link modification", () ->
				relationshipLinkService.update(linkId, linkDescriptor, linkDocumentContent)
		);
	}

	@Test(dataProvider = "linkWriteOperationsDataProvider",
			description = "Should enforce authorization for link relinking")
	public void shouldCheckRelinkAccess(String username, boolean hasPermission) {
		String linkId = createTestLinkAsAdmin();

		testLinkOperation(username, hasPermission, "relinking", () ->
				relationshipLinkService.relink(linkDescriptor, linkId)
		);
	}

	@Test(dataProvider = "linkWriteOperationsDataProvider",
			description = "Should enforce authorization for link deletion")
	public void shouldCheckDeleteLinkAccess(String username, boolean hasPermission) {
		String linkId = createTestLinkAsAdmin();

		testLinkOperation(username, hasPermission, "link deletion", () ->
				relationshipLinkService.delete(linkId)
		);
	}

	@Test(dataProvider = "linkReadOperationsDataProvider",
			description = "Should enforce authorization for link loading")
	public void shouldCheckListLinksAccess(String username, boolean hasPermission) {
		String linkId = createTestLinkAsAdmin();

		testLinkOperation(username, hasPermission, "link loading", () ->
				relationshipLinkService.load(linkId)
		);
	}

	/**
	 * Generic helper to test authorization for relationship link operations.
	 *
	 * @param username the user to test
	 * @param hasPermission whether the user should have permission
	 * @param operationName name of operation for error messages
	 * @param operation the operation to execute (as CheckedRunnable)
	 */
	private void testLinkOperation(
			String username, boolean hasPermission,
			String operationName, CheckedRunnable operation
	) {
		changeUserInContext(username);

		if (hasPermission) {
			try {
				operation.run();
			} catch (AccessDeniedException e) {
				fail(String.format("User %s should have permission for %s, but got AccessDeniedException: %s",
						username, operationName, e.getMessage()));
			} catch (Exception e) {
				fail(String.format("Unexpected exception for user %s during %s: %s",
						username, operationName, e.getMessage()));
			}
		} else {
			try {
				operation.run();
				fail(String.format("User %s should be denied %s, but operation succeeded",
						username, operationName));
			} catch (AccessDeniedException e) {
				// Expected - test passes
			} catch (Exception e) {
				fail(String.format("Expected AccessDeniedException for user %s during %s, but got: %s",
						username, operationName, e.getClass().getSimpleName()));
			}
		}
	}

	/**
	 * Creates a test link as admin user and returns its ID.
	 * Use this for operations that require an existing link (modify, delete, load, relink).
	 *
	 * @return the ID of the created link
	 */
	private String createTestLinkAsAdmin() {
		changeUserInContext("admin");
		RelationshipLink testLink = relationshipLinkService.create(
				linkDescriptor,
				linkDescriptor.getLinkDocumentDocRef()
		);
		return testLink.getId();
	}

	/**
	 * Functional interface for operations that may throw checked exceptions.
	 */
	@FunctionalInterface
	private interface CheckedRunnable {
		void run() throws Exception;
	}

	private void createModelWithCustomRoles(String modelFilePath) throws IOException {
		String modelContent = resourceFunctions.loadResource(modelFilePath);
		modelContent = JsonFunctions.replaceValue(modelContent, "/header/annotations/0/value",
				CUSTOM_ROLES_FOR_TEST, true);
		modelsFunctions.createModelFromJson(modelContent);
	}

}
