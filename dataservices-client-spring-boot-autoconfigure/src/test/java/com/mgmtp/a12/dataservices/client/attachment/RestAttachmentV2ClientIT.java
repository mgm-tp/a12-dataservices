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
package com.mgmtp.a12.dataservices.client.attachment;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.attachment.AttachmentAnnotation;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeaderSpec;
import com.mgmtp.a12.dataservices.client.AbstractSpringContextIT;

public class RestAttachmentV2ClientIT extends AbstractSpringContextIT {

	public static final String PATH_TO_FIELD = "Contract/logo";
	@Autowired AttachmentClientV2 restAttachmentClient;

	@BeforeMethod public void setUp() {
		createModelFromFile(CONTRACT_MODEL_FILE);
	}

	@AfterMethod public void cleanUp() {
		cleanUpByDocumentModel(CONTRACT_MODEL_NAME);
	}

	@DataProvider public static Object[][] annotationProvider() {
		return new Object[][] {
			{ null },
			{ List.of() },
			{ List.of(new AttachmentAnnotation("name", "value")) },
			{ List.of(new AttachmentAnnotation("name1", "value1"), new AttachmentAnnotation("name2", "value2")) }
		};
	}
	@DataProvider public static String[] filenameProvider() {
		return new String[] {
			"Attachment.png",
			"Attachment (upload).png",
			"Upload_ Image-123.png",
			"Upload#$Image-123.png",
			"Upload & Image=-123.png",
		};
	}

	@Test(dataProvider = "annotationProvider") public void attachmentCreation(Collection<AttachmentAnnotation> annotations) throws IOException {
		AttachmentHeaderSpec attachmentHeader =
			restAttachmentClient.uploadAttachment(new FileInputStream(ATTACHMENT_FILE), FILENAME, CONTRACT_MODEL_NAME, PATH_TO_FIELD, annotations);
		Assert.assertNotNull(attachmentHeader);
		Assert.assertNotNull(attachmentHeader.getAttachmentId());
		Assert.assertEquals(attachmentHeader.getFilename(), FILENAME);
		Assert.assertEquals(attachmentHeader.getMimeType(), MediaType.IMAGE_JPEG_VALUE);
		Assert.assertEquals(attachmentHeader.getSize(), 131819);
		Assert.assertEquals(attachmentHeader.getAnnotations(), annotations == null ? List.of() : annotations);
	}

	@Test(dataProvider = "filenameProvider") public void attachmentCreationWithFilename(String filename) throws IOException {
		AttachmentHeaderSpec attachmentHeader =
			restAttachmentClient.uploadAttachment(new FileInputStream(ATTACHMENT_FILE), filename, CONTRACT_MODEL_NAME, PATH_TO_FIELD, List.of());
		Assert.assertNotNull(attachmentHeader);
		Assert.assertNotNull(attachmentHeader.getAttachmentId());
		Assert.assertEquals(attachmentHeader.getFilename(), filename);
		Assert.assertEquals(attachmentHeader.getMimeType(), MediaType.IMAGE_JPEG_VALUE);
		Assert.assertEquals(attachmentHeader.getSize(), 131819);
	}
}
