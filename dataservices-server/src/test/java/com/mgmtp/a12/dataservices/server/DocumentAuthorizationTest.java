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
import org.springframework.security.access.AccessDeniedException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.DocumentFunctions;
import com.mgmtp.a12.dataservices.ModelsFunctions;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentPart;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.operation.internal.CopyDocumentOperation;
import com.mgmtp.a12.dataservices.rpc.RpcException;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.uaa.authentication.backend.BackendAuthenticationService;

import lombok.SneakyThrows;

@SpringBootTest
public class DocumentAuthorizationTest extends AbstractSpringContextServerTests {

	@Autowired private BackendAuthenticationService backendAuthenticationService;
	@Autowired private DocumentService defaultDocumentService;
	@Autowired private ModelsFunctions modelsFunctions;
	@Autowired private DocumentFunctions documentFunctions;
	@Autowired private CopyDocumentOperation copyDocumentOperation;

	private static final String AUTHORIZATION_MODELS_PATH = "model/document/authorizationTests/";
	private static final String AUTHORIZATION_DOCUMENTS_PATH = "document/";
	private static final String TEST_MODEL_NAME = "AuthorizationTestModel";

	@BeforeMethod public void initMethod() {
		super.cleanUpTestEnvironment();
		changeUserInContext("admin");
		modelsFunctions.createModel(AUTHORIZATION_MODELS_PATH + "DocumentAuthorizationTestModel.json");
	}

	@DataProvider
	public Object[][] documentCreateDataProvider() {
		return new Object[][] {
			{ UserConstants.ADMIN_USER, true },
			{ UserConstants.ACTUATOR_USER, false },
			{ UserConstants.GUEST_USER, false },
			{ UserConstants.DOCUMENT_WRITE_USER, true },
			{ UserConstants.DOCUMENT_CREATE_USER, true },
			{ UserConstants.DOCUMENT_UPDATE_USER, false },
			{ UserConstants.DOCUMENT_PARTIAL_UPDATE_USER, false },
			{ UserConstants.DOCUMENT_DELETE_USER, false },
			{ UserConstants.DOCUMENT_READ_USER, false },
			{ UserConstants.DOCUMENT_COPY_USER, true },
			{ UserConstants.MODEL_MANAGER_USER, false }
		};
	}

	@DataProvider
	public Object[][] documentUpdateDataProvider() {
		return new Object[][] {
			{ UserConstants.ADMIN_USER, true },
			{ UserConstants.ACTUATOR_USER, false },
			{ UserConstants.GUEST_USER, false },
			{ UserConstants.DOCUMENT_WRITE_USER, true },
			{ UserConstants.DOCUMENT_CREATE_USER, false },
			{ UserConstants.DOCUMENT_UPDATE_USER, true },
			{ UserConstants.DOCUMENT_PARTIAL_UPDATE_USER, false },
			{ UserConstants.DOCUMENT_DELETE_USER, false },
			{ UserConstants.DOCUMENT_READ_USER, false },
			{ UserConstants.DOCUMENT_COPY_USER, false },
			{ UserConstants.MODEL_MANAGER_USER, false }
		};
	}

	@DataProvider
	public Object[][] documentPartialUpdateDataProvider() {
		return new Object[][] {
			{ UserConstants.ADMIN_USER, true },
			{ UserConstants.ACTUATOR_USER, false },
			{ UserConstants.GUEST_USER, false },
			{ UserConstants.DOCUMENT_WRITE_USER, true },
			{ UserConstants.DOCUMENT_CREATE_USER, false },
			{ UserConstants.DOCUMENT_UPDATE_USER, false },
			{ UserConstants.DOCUMENT_PARTIAL_UPDATE_USER, true },
			{ UserConstants.DOCUMENT_DELETE_USER, false },
			{ UserConstants.DOCUMENT_READ_USER, false },
			{ UserConstants.DOCUMENT_COPY_USER, false },
			{ UserConstants.MODEL_MANAGER_USER, false }
		};
	}

	@DataProvider
	public Object[][] documentDeleteDataProvider() {
		return new Object[][] {
			{ UserConstants.ADMIN_USER, true },
			{ UserConstants.ACTUATOR_USER, false },
			{ UserConstants.GUEST_USER, false },
			{ UserConstants.DOCUMENT_WRITE_USER, true },
			{ UserConstants.DOCUMENT_CREATE_USER, false },
			{ UserConstants.DOCUMENT_UPDATE_USER, false },
			{ UserConstants.DOCUMENT_PARTIAL_UPDATE_USER, false },
			{ UserConstants.DOCUMENT_DELETE_USER, true },
			{ UserConstants.DOCUMENT_READ_USER, false },
			{ UserConstants.DOCUMENT_COPY_USER, false },
			{ UserConstants.MODEL_MANAGER_USER, false }
		};
	}

	@DataProvider
	public Object[][] documentMultiDeleteDataProvider() {
		return new Object[][] {
			{ UserConstants.ADMIN_USER, true },
			{ UserConstants.ACTUATOR_USER, false },
			{ UserConstants.GUEST_USER, false },
			{ UserConstants.DOCUMENT_WRITE_USER, false },
			{ UserConstants.DOCUMENT_CREATE_USER, false },
			{ UserConstants.DOCUMENT_UPDATE_USER, false },
			{ UserConstants.DOCUMENT_PARTIAL_UPDATE_USER, false },
			{ UserConstants.DOCUMENT_DELETE_USER, false },
			{ UserConstants.DOCUMENT_MULTI_DELETE_USER, true },
			{ UserConstants.DOCUMENT_READ_USER, false },
			{ UserConstants.DOCUMENT_COPY_USER, false },
			{ UserConstants.MODEL_MANAGER_USER, false }
		};
	}

	@DataProvider
	public Object[][] documentCopyDataProvider() {
		return new Object[][] {
			{ UserConstants.ADMIN_USER, true },
			{ UserConstants.ACTUATOR_USER, false },
			{ UserConstants.GUEST_USER, false },
			{ UserConstants.DOCUMENT_WRITE_USER, false },
			{ UserConstants.DOCUMENT_CREATE_USER, false },
			{ UserConstants.DOCUMENT_UPDATE_USER, false },
			{ UserConstants.DOCUMENT_PARTIAL_UPDATE_USER, false },
			{ UserConstants.DOCUMENT_DELETE_USER, false },
			{ UserConstants.DOCUMENT_READ_USER, false },
			{ UserConstants.DOCUMENT_COPY_USER, true },
			{ UserConstants.MODEL_MANAGER_USER, false }
		};
	}

	@DataProvider
	public Object[][] documentReadDataProvider() {
		return new Object[][] {
			{ UserConstants.ADMIN_USER, true },
			{ UserConstants.ACTUATOR_USER, false },
			{ UserConstants.GUEST_USER, true },
			{ UserConstants.DOCUMENT_WRITE_USER, false },
			{ UserConstants.DOCUMENT_CREATE_USER, false },
			{ UserConstants.DOCUMENT_UPDATE_USER, false },
			{ UserConstants.DOCUMENT_PARTIAL_UPDATE_USER, false },
			{ UserConstants.DOCUMENT_DELETE_USER, false },
			{ "document_read_user", true },
			{ "document_copy_user", true },
			{ "model_manager_user", false }
		};
	}

	@Test(dataProvider = "documentCreateDataProvider")
	public void testDocumentCreateAccess(String username, boolean hasPermission) {
		changeUserInContext(username);
		assertDocumentCreateAccess(hasPermission);
	}

	@Test(dataProvider = "documentUpdateDataProvider")
	public void testDocumentUpdateAccess(String username, boolean hasPermission) {
		DataServicesDocument dataServicesDocument = createDocumentAsSuperUser();
		changeUserInContext(username);
		assertDocumentUpdateAccess(dataServicesDocument.getMetadata().getDocRef(), dataServicesDocument.getKernelDocument(), hasPermission);
	}

	@Test(dataProvider = "documentPartialUpdateDataProvider")
	public void testDocumentPartialUpdateAccess(String username, boolean hasPermission) {
		DataServicesDocument dataServicesDocument = createDocumentAsSuperUser();
		changeUserInContext(username);
		assertDocumentPartialUpdateAccess(dataServicesDocument.getMetadata().getDocRef(), hasPermission);
	}

	@Test(dataProvider = "documentDeleteDataProvider")
	public void testDocumentDeleteAccess(String username, boolean hasPermission) {
		DataServicesDocument dataServicesDocument = createDocumentAsSuperUser();
		changeUserInContext(username);
		assertDocumentDeleteAccess(dataServicesDocument.getMetadata().getDocRef(), hasPermission);
	}

	@Test(dataProvider = "documentCopyDataProvider")
	public void testDocumentCopyAccess(String username, boolean hasPermission) {
		DataServicesDocument dataServicesDocument = createDocumentAsSuperUser();
		changeUserInContext(username);
		assertDocumentCopyAccess(dataServicesDocument.getMetadata().getDocRef(), hasPermission);
	}

	@Test(dataProvider = "documentReadDataProvider")
	public void testDocumentReadAccess(String username, boolean hasPermission) {
		DataServicesDocument dataServicesDocument = createDocumentAsSuperUser();
		changeUserInContext(username);
		assertDocumentReadAccess(dataServicesDocument.getMetadata().getDocRef(), hasPermission);
	}

	@SneakyThrows
	private DataServicesDocument createDocumentAsSuperUser() {
		DocumentV2 document = documentFunctions.getKernelDocumentFromFile(TEST_MODEL_NAME,
			AUTHORIZATION_DOCUMENTS_PATH + "DocumentAuthorizationTest.json");
		return backendAuthenticationService.executeWithBackendAuthentication("superUser", () -> defaultDocumentService.create(document, Locale.ENGLISH));
	}

	private void assertDocumentCreateAccess(boolean isAllowed) {
		try {
			defaultDocumentService.create(documentFunctions.getKernelDocumentFromFile(TEST_MODEL_NAME,
				AUTHORIZATION_DOCUMENTS_PATH + "DocumentAuthorizationTest.json"), Locale.ENGLISH);
			Assert.assertTrue(isAllowed);
		} catch (AccessDeniedException e) {
			Assert.assertFalse(isAllowed);
		} catch (IOException ioe) {
			Assert.fail("Caught IOException unexpectedly, with message: " + ioe.getMessage());
		}
	}

	private void assertDocumentUpdateAccess(DocumentReference docRef, DocumentV2 document, boolean isAllowed) {
		try {
			defaultDocumentService.update(docRef, document, Locale.ENGLISH);
			Assert.assertTrue(isAllowed);
		} catch (AccessDeniedException e) {
			Assert.assertFalse(isAllowed);
		}
	}

	private void assertDocumentPartialUpdateAccess(DocumentReference docRef, boolean isAllowed) {
		try {
			DocumentPart part = DocumentPart.builder()
				.path("/AuthorizationTestModelGroup/Name")
				.repetitions(new int[] { 1, 1 })
				.value("new")
				.build();
			defaultDocumentService.update(docRef, List.of(part), Locale.ENGLISH);
			Assert.assertTrue(isAllowed);
		} catch (AccessDeniedException e) {
			Assert.assertFalse(isAllowed);
		}
	}

	private void assertDocumentDeleteAccess(DocumentReference docRef, boolean isAllowed) {
		try {
			defaultDocumentService.delete(docRef);
			Assert.assertTrue(isAllowed);
		} catch (AccessDeniedException e) {
			Assert.assertFalse(isAllowed);
		}
	}

	private void assertDocumentCopyAccess(DocumentReference docRef, boolean isAllowed) {
		try {
			copyDocumentOperation.rpc(docRef, Locale.ENGLISH);
			Assert.assertTrue(isAllowed);
		} catch (RpcException e) {
			Assert.assertFalse(isAllowed);
		}
	}

	private void assertDocumentReadAccess(DocumentReference docRef, boolean isAllowed) {
		try {
			defaultDocumentService.load(docRef);
			Assert.assertTrue(isAllowed);
		} catch (AccessDeniedException e) {
			Assert.assertFalse(isAllowed);
		}
	}

}
