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

import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentService;
import com.mgmtp.a12.dataservices.attachment.ThumbnailType;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH;

@SpringBootTest
public class AttachmentAuthorizationTest extends AbstractSpringContextServerTests {

	@Autowired private AttachmentService attachmentService;

	@BeforeClass
	public void initClass() {
		super.cleanUpTestEnvironment();
		changeUserInContext(UserConstants.ADMIN_USER);
		modelsFunctions.createModel(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
	}

	@DataProvider
	public Object[][] attachmentCreateDataProvider() {
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
				{ UserConstants.DOCUMENT_COPY_USER, false },
				{ UserConstants.MODEL_READ_USER, false },
				{ UserConstants.MODEL_CREATE_USER, false },
				{ UserConstants.MODEL_DELETE_USER, false },
				{ UserConstants.MODEL_MANAGER_USER, false }
		};
	}

	@DataProvider
	public Object[][] loadAttachmentUrlDataProvider() {
		return new Object[][] {
				{ UserConstants.ADMIN_USER, true },
				{ UserConstants.ACTUATOR_USER, false },
				{ UserConstants.GUEST_USER, true },
				{ UserConstants.DOCUMENT_WRITE_USER, false },
				{ UserConstants.DOCUMENT_CREATE_USER, false },
				{ UserConstants.DOCUMENT_UPDATE_USER, false },
				{ UserConstants.DOCUMENT_PARTIAL_UPDATE_USER, false },
				{ UserConstants.DOCUMENT_DELETE_USER, false },
				{ UserConstants.DOCUMENT_READ_USER, false },
				{ UserConstants.DOCUMENT_COPY_USER, false },
				{ UserConstants.MODEL_READ_USER, false },
				{ UserConstants.MODEL_CREATE_USER, false },
				{ UserConstants.MODEL_DELETE_USER, false },
				{ UserConstants.MODEL_MANAGER_USER, false }
		};
	}

	@DataProvider
	public Object[][] loadThumbnailUrlDataProvider() {
		return new Object[][] {
				{ UserConstants.ADMIN_USER, true },
				{ UserConstants.ACTUATOR_USER, true },
				{ UserConstants.GUEST_USER, true },
				{ UserConstants.DOCUMENT_WRITE_USER, true },
				{ UserConstants.DOCUMENT_CREATE_USER, true },
				{ UserConstants.DOCUMENT_UPDATE_USER, true },
				{ UserConstants.DOCUMENT_PARTIAL_UPDATE_USER, true },
				{ UserConstants.DOCUMENT_DELETE_USER, true },
				{ UserConstants.DOCUMENT_READ_USER, true },
				{ UserConstants.DOCUMENT_COPY_USER, true },
				{ UserConstants.MODEL_READ_USER, true },
				{ UserConstants.MODEL_CREATE_USER, true },
				{ UserConstants.MODEL_DELETE_USER, true },
				{ UserConstants.MODEL_MANAGER_USER, true }
		};
	}

	@Test(dataProvider = "attachmentCreateDataProvider")
	public void testAttachmentCreateAccess(String username, boolean hasPermission) {
		changeUserInContext(username);
		assertAccessPermission(() -> {
			try {
				prepareAttachment();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}, hasPermission, "Attachment creation for user: " + username);
	}

	@Test(dataProvider = "loadAttachmentUrlDataProvider")
	public void testAttachmentUrlLoadAccess(String username, boolean hasPermission) throws IOException {
		AttachmentHeader attachmentHeader = prepareAttachment();
		DataServicesDocument document = createDocumentAsSuperUser(attachmentHeader.getAttachmentId());
		changeUserInContext(username);

		assertAccessPermission(
			() -> attachmentService.findAttachmentUrl(attachmentHeader.getAttachmentId(), document.getMetadata().getDocRef()),
			hasPermission,
			"Attachment URL loading for user: " + username
		);
	}

	@Test(dataProvider = "loadThumbnailUrlDataProvider")
	public void testThumbnailUrlLoadAccess(String username, boolean hasPermission) throws IOException {
		AttachmentHeader attachmentHeader = prepareAttachment();
		changeUserInContext(username);

		assertAccessPermission(
			() -> attachmentService.findThumbnailUrl(attachmentHeader.getAttachmentId(), ThumbnailType.SMALL),
			hasPermission,
			"Thumbnail URL loading for user: " + username
		);
	}

	private AttachmentHeader prepareAttachment() throws IOException {
		return attachmentService.createAttachment(
				resourceFunctions.loadResourceAsStream("/attachment/Attachment.jpg"),
				"TestAttachment",
				BUSINESS_PARTNER_DOCUMENT_MODEL,
				"/BusinessPartnerRoot/Name",
				List.of()
		);
	}

	@SneakyThrows
	private DataServicesDocument createDocumentAsSuperUser(String attachmentIdentification) {
		String documentContent = resourceFunctions.loadResource(BUSINESS_PARTNER_DOCUMENT_FILE);
		documentContent = documentContent.formatted(attachmentIdentification);
		return documentFunctions.createDocumentFromJson(BUSINESS_PARTNER_DOCUMENT_MODEL, documentContent);
	}

}
