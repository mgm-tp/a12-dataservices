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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;

import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentPart;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.operation.internal.AddDocumentOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.CopyDocumentOperation;
import com.mgmtp.a12.dataservices.rpc.RpcException;

import lombok.SneakyThrows;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tools.jackson.databind.JsonNode;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH;

@SpringBootTest
public class DocumentAuthorizationTest extends AbstractSpringContextServerTests {

	private static final String DIRTY_DOCUMENT_MODEL = "dirtyDocumentModel";

	@Autowired private DocumentService defaultDocumentService;
	@Autowired private CopyDocumentOperation copyDocumentOperation;
	@Autowired private AddDocumentOperation addDocumentOperation;

	private static final String BUSINESS_PARTNER_DOCUMENT = "document/BusinessPartnerDocument.json";
	private static final String ALL_ROLES = "admin,guest,DocumentWrite,DocumentCreate,DocumentUpdate,DocumentPartialUpdate,DocumentDelete,DocumentRead,DocumentCopy";

	@BeforeClass
	public void initClass() throws IOException {
		super.cleanUpTestEnvironment();
		changeUserInContext(UserConstants.ADMIN_USER);
		createModelWithRole(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH, ALL_ROLES);
	}

	@AfterMethod(onlyForGroups = {DIRTY_DOCUMENT_MODEL})
	public void cleanupModels() throws IOException {
		changeUserInContext(UserConstants.ADMIN_USER);
		updateModelRole(BUSINESS_PARTNER_DOCUMENT_MODEL, ALL_ROLES);
	}

	@DataProvider
	public Object[][] documentCreateDataProvider() {
		return new Object[][]{
				{UserConstants.ADMIN_USER, true},
				{UserConstants.ACTUATOR_USER, false},
				{UserConstants.GUEST_USER, false},
				{UserConstants.DOCUMENT_WRITE_USER, true},
				{UserConstants.DOCUMENT_CREATE_USER, true},
				{UserConstants.DOCUMENT_UPDATE_USER, false},
				{UserConstants.DOCUMENT_PARTIAL_UPDATE_USER, false},
				{UserConstants.DOCUMENT_DELETE_USER, false},
				{UserConstants.DOCUMENT_READ_USER, false},
				{UserConstants.DOCUMENT_COPY_USER, true},
				{UserConstants.MODEL_MANAGER_USER, false}
		};
	}

	@DataProvider
	public Object[][] documentUpdateDataProvider() {
		return new Object[][]{
				{UserConstants.ADMIN_USER, true},
				{UserConstants.ACTUATOR_USER, false},
				{UserConstants.GUEST_USER, false},
				{UserConstants.DOCUMENT_WRITE_USER, true},
				{UserConstants.DOCUMENT_CREATE_USER, false},
				{UserConstants.DOCUMENT_UPDATE_USER, true},
				{UserConstants.DOCUMENT_PARTIAL_UPDATE_USER, false},
				{UserConstants.DOCUMENT_DELETE_USER, false},
				{UserConstants.DOCUMENT_READ_USER, false},
				{UserConstants.DOCUMENT_COPY_USER, false},
				{UserConstants.MODEL_MANAGER_USER, false}
		};
	}

	@DataProvider
	public Object[][] documentPartialUpdateDataProvider() {
		return new Object[][]{
				{UserConstants.ADMIN_USER, true},
				{UserConstants.ACTUATOR_USER, false},
				{UserConstants.GUEST_USER, false},
				{UserConstants.DOCUMENT_WRITE_USER, true},
				{UserConstants.DOCUMENT_CREATE_USER, false},
				{UserConstants.DOCUMENT_UPDATE_USER, false},
				{UserConstants.DOCUMENT_PARTIAL_UPDATE_USER, true},
				{UserConstants.DOCUMENT_DELETE_USER, false},
				{UserConstants.DOCUMENT_READ_USER, false},
				{UserConstants.DOCUMENT_COPY_USER, false},
				{UserConstants.MODEL_MANAGER_USER, false}
		};
	}

	@DataProvider
	public Object[][] documentDeleteDataProvider() {
		return new Object[][]{
				{UserConstants.ADMIN_USER, true},
				{UserConstants.ACTUATOR_USER, false},
				{UserConstants.GUEST_USER, false},
				{UserConstants.DOCUMENT_WRITE_USER, true},
				{UserConstants.DOCUMENT_CREATE_USER, false},
				{UserConstants.DOCUMENT_UPDATE_USER, false},
				{UserConstants.DOCUMENT_PARTIAL_UPDATE_USER, false},
				{UserConstants.DOCUMENT_DELETE_USER, true},
				{UserConstants.DOCUMENT_READ_USER, false},
				{UserConstants.DOCUMENT_COPY_USER, false},
				{UserConstants.MODEL_MANAGER_USER, false}
		};
	}

	@DataProvider
	public Object[][] documentMultiDeleteDataProvider() {
		return new Object[][]{
				{UserConstants.ADMIN_USER, true},
				{UserConstants.ACTUATOR_USER, false},
				{UserConstants.GUEST_USER, false},
				{UserConstants.DOCUMENT_WRITE_USER, false},
				{UserConstants.DOCUMENT_CREATE_USER, false},
				{UserConstants.DOCUMENT_UPDATE_USER, false},
				{UserConstants.DOCUMENT_PARTIAL_UPDATE_USER, false},
				{UserConstants.DOCUMENT_DELETE_USER, false},
				{UserConstants.DOCUMENT_MULTI_DELETE_USER, true},
				{UserConstants.DOCUMENT_READ_USER, false},
				{UserConstants.DOCUMENT_COPY_USER, false},
				{UserConstants.MODEL_MANAGER_USER, false}
		};
	}

	@DataProvider
	public Object[][] documentCopyDataProvider() {
		return new Object[][]{
				{UserConstants.ADMIN_USER, true},
				{UserConstants.ACTUATOR_USER, false},
				{UserConstants.GUEST_USER, false},
				{UserConstants.DOCUMENT_WRITE_USER, false},
				{UserConstants.DOCUMENT_CREATE_USER, false},
				{UserConstants.DOCUMENT_UPDATE_USER, false},
				{UserConstants.DOCUMENT_PARTIAL_UPDATE_USER, false},
				{UserConstants.DOCUMENT_DELETE_USER, false},
				{UserConstants.DOCUMENT_READ_USER, false},
				{UserConstants.DOCUMENT_COPY_USER, true},
				{UserConstants.MODEL_MANAGER_USER, false}
		};
	}

	@DataProvider
	public Object[][] documentReadDataProvider() {
		return new Object[][]{
				{UserConstants.ADMIN_USER, true},
				{UserConstants.ACTUATOR_USER, false},
				{UserConstants.GUEST_USER, true},
				{UserConstants.DOCUMENT_WRITE_USER, false},
				{UserConstants.DOCUMENT_CREATE_USER, false},
				{UserConstants.DOCUMENT_UPDATE_USER, false},
				{UserConstants.DOCUMENT_PARTIAL_UPDATE_USER, false},
				{UserConstants.DOCUMENT_DELETE_USER, false},
				{UserConstants.DOCUMENT_READ_USER, true},
				{UserConstants.DOCUMENT_COPY_USER, true},
				{UserConstants.MODEL_MANAGER_USER, false}
		};
	}

	@Test(dataProvider = "documentCreateDataProvider")
	public void testDocumentCreateAccess(String username, boolean hasPermission) {
		changeUserInContext(username);

		assertAccessPermission(() -> {
			try {
				defaultDocumentService.create(documentFunctions.getKernelDocumentFromFile(BUSINESS_PARTNER_DOCUMENT_MODEL,
						BUSINESS_PARTNER_DOCUMENT), Locale.ENGLISH);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}, hasPermission, "Document creation for user: " + username);
	}

	@Test(groups = DIRTY_DOCUMENT_MODEL)
	public void testDocumentCreation_restrictedModelRoles() throws IOException {
		updateModelRole(BUSINESS_PARTNER_DOCUMENT_MODEL, COMMON_ROLES);


		changeUserInContext(UserConstants.DOCUMENT_CREATE_USER);
		assertAccessPermission(() -> {
					try {
						defaultDocumentService.create(
								documentFunctions.getKernelDocumentFromFile(BUSINESS_PARTNER_DOCUMENT_MODEL, BUSINESS_PARTNER_DOCUMENT),
								Locale.ENGLISH
						);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				},
				false,
				"Document creation for user: " + UserConstants.DOCUMENT_CREATE_USER + " with restricted model: " + BUSINESS_PARTNER_DOCUMENT_MODEL);
	}


	@Test(dataProvider = "documentUpdateDataProvider")
	public void testDocumentUpdateAccess(String username, boolean hasPermission) {
		DataServicesDocument dataServicesDocument = createDocumentAsSuperUser();
		changeUserInContext(username);

		assertAccessPermission(
				() -> defaultDocumentService.update(
						dataServicesDocument.getMetadata().getDocRef(),
						dataServicesDocument.getKernelDocument(), Locale.ENGLISH
				), hasPermission, "Document update for user: " + username
		);
	}

	@Test(groups = DIRTY_DOCUMENT_MODEL)
	public void testDocumentUpdate_restrictedModelRoles() throws IOException {
		DataServicesDocument dataServicesDocument = createDocumentAsSuperUser();
		updateModelRole(BUSINESS_PARTNER_DOCUMENT_MODEL, COMMON_ROLES);


		changeUserInContext(UserConstants.DOCUMENT_UPDATE_USER);
		assertAccessPermission(() -> defaultDocumentService.update(
						dataServicesDocument.getMetadata().getDocRef(),
						dataServicesDocument.getKernelDocument(), Locale.ENGLISH
				), false, "Document update for user: " + UserConstants.DOCUMENT_UPDATE_USER + " with restricted model: " + BUSINESS_PARTNER_DOCUMENT_MODEL
		);
	}

	@Test(dataProvider = "documentPartialUpdateDataProvider")
	public void testDocumentPartialUpdateAccess(String username, boolean hasPermission) {
		DataServicesDocument dataServicesDocument = createDocumentAsSuperUser();
		changeUserInContext(username);

		assertAccessPermission(() -> {
			DocumentPart part = createTestDocumentPart();
			defaultDocumentService.update(dataServicesDocument.getMetadata().getDocRef(), List.of(part), Locale.ENGLISH);
		}, hasPermission, "Document partial update for user: " + username);
	}

	@Test(groups = DIRTY_DOCUMENT_MODEL)
	public void testDocumentPartialUpdate_restrictedModelRoles() throws IOException {
		DataServicesDocument dataServicesDocument = createDocumentAsSuperUser();
		updateModelRole(BUSINESS_PARTNER_DOCUMENT_MODEL, COMMON_ROLES);

		changeUserInContext(UserConstants.DOCUMENT_PARTIAL_UPDATE_USER);
		assertAccessPermission(() -> {
					DocumentPart part = createTestDocumentPart();
					defaultDocumentService.update(dataServicesDocument.getMetadata().getDocRef(), List.of(part), Locale.ENGLISH);
				},
				false,
				"Document partial update for user: " + UserConstants.DOCUMENT_PARTIAL_UPDATE_USER + " with restricted model: " + BUSINESS_PARTNER_DOCUMENT_MODEL
		);
	}

	@Test(dataProvider = "documentDeleteDataProvider")
	public void testDocumentDeleteAccess(String username, boolean hasPermission) {
		DataServicesDocument dataServicesDocument = createDocumentAsSuperUser();
		changeUserInContext(username);

		assertAccessPermission(
				() -> defaultDocumentService.delete(dataServicesDocument.getMetadata().getDocRef()),
				hasPermission,
				"Document deletion for user: " + username
		);
	}

	@Test(groups = DIRTY_DOCUMENT_MODEL)
	public void testDocumentDelete_restrictedModelRoles() throws IOException {
		DataServicesDocument dataServicesDocument = createDocumentAsSuperUser();
		updateModelRole(BUSINESS_PARTNER_DOCUMENT_MODEL, COMMON_ROLES);

		changeUserInContext(UserConstants.DOCUMENT_DELETE_USER);
		assertAccessPermission(() -> defaultDocumentService.delete(dataServicesDocument.getMetadata().getDocRef()),
				false,
				"Document delete for user: " + UserConstants.DOCUMENT_DELETE_USER + " with restricted model: " + BUSINESS_PARTNER_DOCUMENT_MODEL
		);
	}

	@Test(dataProvider = "documentCopyDataProvider")
	public void testDocumentCopyAccess(String username, boolean hasPermission) {
		DataServicesDocument dataServicesDocument = createDocumentAsSuperUser();
		changeUserInContext(username);

		assertAccessPermissionWithRpcException(
				() -> copyDocumentOperation.rpc(dataServicesDocument.getMetadata().getDocRef().toString(), Locale.ENGLISH),
				hasPermission,
				"Document copy for user: " + username
		);
	}

	@Test(enabled = false, groups = DIRTY_DOCUMENT_MODEL)
	public void testDocumentCopy_restrictedModelRoles() throws IOException {
		DataServicesDocument dataServicesDocument = createDocumentAsSuperUser();
		updateModelRole(BUSINESS_PARTNER_DOCUMENT_MODEL, COMMON_ROLES);

		changeUserInContext(UserConstants.DOCUMENT_COPY_USER);
		assertAccessPermissionWithRpcException(
				() -> copyDocumentOperation.rpc(dataServicesDocument.getMetadata().getDocRef().toString(), Locale.ENGLISH),
				false,
				"Document copy for user: " + UserConstants.DOCUMENT_COPY_USER + " with restricted model: " + BUSINESS_PARTNER_DOCUMENT_MODEL
		);
	}

	@Test(dataProvider = "documentReadDataProvider")
	public void testDocumentReadAccess(String username, boolean hasPermission) {
		DataServicesDocument dataServicesDocument = createDocumentAsSuperUser();
		changeUserInContext(username);

		assertAccessPermission(
				() -> defaultDocumentService.load(dataServicesDocument.getMetadata().getDocRef()),
				hasPermission,
				"Document read for user: " + username
		);
	}

	@Test(enabled = false, groups = DIRTY_DOCUMENT_MODEL)
	public void testDocumentRead_restrictedModelRoles() throws IOException {
		DataServicesDocument dataServicesDocument = createDocumentAsSuperUser();
		updateModelRole(BUSINESS_PARTNER_DOCUMENT_MODEL, COMMON_ROLES);

		changeUserInContext(UserConstants.DOCUMENT_READ_USER);
		assertAccessPermission(() -> defaultDocumentService.load(dataServicesDocument.getMetadata().getDocRef()),
				false,
				"Document read for user: " + UserConstants.DOCUMENT_READ_USER + " with restricted model: " + BUSINESS_PARTNER_DOCUMENT_MODEL
		);
	}

	/**
	 * Helper method for operations that throw RpcException instead of AccessDeniedException
	 */
	private void assertAccessPermissionWithRpcException(Runnable operation, boolean hasPermission, String operationDescription) {
		try {
			operation.run();
			Assert.assertTrue(hasPermission,
					operationDescription + " should have been denied but was allowed");
		} catch (RpcException e) {
			Assert.assertFalse(hasPermission,
					operationDescription + " should have been allowed but was denied: " + e.getMessage());
		}
	}

	// --- Info leakage prevention tests: guest must get AccessDeniedException even for non-existing documents ---

	@Test(description = "Should deny guest when deleting non-existing document (prevents info leakage)")
	public void testDocumentDeleteAccess_nonExistingDocument_guestDenied() {
		changeUserInContext(UserConstants.GUEST_USER);
		DocumentReference nonExistingRef = new DocumentReference(BUSINESS_PARTNER_DOCUMENT_MODEL, "non-existing-id-12345");

		assertAccessPermission(
				() -> defaultDocumentService.delete(nonExistingRef),
				false, "Document deletion of non-existing document for guest user"
		);
	}

	@Test(description = "Should deny guest when updating non-existing document (prevents info leakage)")
	public void testDocumentUpdateAccess_nonExistingDocument_guestDenied() {
		DataServicesDocument existingDocument = createDocumentAsSuperUser();
		changeUserInContext(UserConstants.GUEST_USER);
		DocumentReference nonExistingRef = new DocumentReference(BUSINESS_PARTNER_DOCUMENT_MODEL, "non-existing-id-12345");

		assertAccessPermission(
				() -> defaultDocumentService.update(
						nonExistingRef,
						existingDocument.getKernelDocument(), Locale.ENGLISH
				), false, "Document update of non-existing document for guest user"
		);
	}

	@Test(description = "Should deny guest when partially updating non-existing document (prevents info leakage)")
	public void testDocumentPartialUpdateAccess_nonExistingDocument_guestDenied() {
		changeUserInContext(UserConstants.GUEST_USER);
		DocumentReference nonExistingRef = new DocumentReference(BUSINESS_PARTNER_DOCUMENT_MODEL, "non-existing-id-12345");

		assertAccessPermission(() -> {
			DocumentPart part = createTestDocumentPart();
			defaultDocumentService.update(nonExistingRef, List.of(part), Locale.ENGLISH);
		}, false, "Document partial update of non-existing document for guest user");
	}

	@Test(description = "Should deny guest when creating document with non-existing model (prevents info leakage)")
	public void testDocumentCreateAccess_nonExistingModel_guestDenied() {
		changeUserInContext(UserConstants.GUEST_USER);
		JsonNode emptyDocument = objectMapper.createObjectNode();

		assertAccessPermissionWithRpcException(
				() -> addDocumentOperation.rpc("NonExistingModel", emptyDocument, Locale.ENGLISH),
				false, "Document creation with non-existing model for guest user"
		);
	}

	@Test(description = "Should deny guest when copying non-existing document (prevents info leakage)")
	public void testDocumentCopyAccess_nonExistingDocument_guestDenied() {
		changeUserInContext(UserConstants.GUEST_USER);
		DocumentReference nonExistingRef = new DocumentReference(BUSINESS_PARTNER_DOCUMENT_MODEL, "non-existing-id-12345");

		assertAccessPermissionWithRpcException(
				() -> copyDocumentOperation.rpc(nonExistingRef.toString(), Locale.ENGLISH),
				false, "Document copy of non-existing document for guest user"
		);
	}

	@SneakyThrows
	private DataServicesDocument createDocumentAsSuperUser() {
		String documentContent = resourceFunctions.loadResource(BUSINESS_PARTNER_DOCUMENT);
		return documentFunctions.createDocumentFromJson(BUSINESS_PARTNER_DOCUMENT_MODEL, documentContent);
	}

	private DocumentPart createTestDocumentPart() {
		return DocumentPart.builder()
				.path("/BusinessPartnerRoot/Name")
				.repetitions(new int[]{1, 1})
				.value("Foo")
				.build();
	}

}
