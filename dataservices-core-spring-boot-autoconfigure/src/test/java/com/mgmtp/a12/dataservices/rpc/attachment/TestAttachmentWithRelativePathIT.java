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
package com.mgmtp.a12.dataservices.rpc.attachment;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.MimeTypeUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.AttachmentTestFunctions;
import com.mgmtp.a12.dataservices.InitialITConfiguration;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeaderSpec;
import com.mgmtp.a12.dataservices.attachment.DataServicesAttachmentURL;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.DocumentReference;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@SpringBootTest(classes = { InitialITConfiguration.class }, properties = { "mgmtp.a12.dataservices.contentstore.base-url=/" })
public class TestAttachmentWithRelativePathIT extends AbstractSpringContextIT {

	private AttachmentTestFunctions.PreparedDocumentV2 preparedDocument;
	private DocumentReference docRef;
	private String attachmentId;

	@BeforeMethod
	public void setUp() throws Exception {
		cleanUpTestEnvironment();
		attachmentTestFunctions.prepareDocumentModel();
		preparedDocument = attachmentTestFunctions.prepareDocumentWithAttachmentV2();
		docRef = preparedDocument.getDataServicesDocument().getMetadata().getDocRef();
		attachmentId = preparedDocument.getImageAttachment().getAttachmentId();
	}

	@Test public void testLoadAttachmentHeader() throws IOException {
		AttachmentHeaderSpec header = loadAttachmentHeaderOperation.rpc(attachmentId, docRef.toString());
		assertEquals(header.getAttachmentId(), attachmentId);
		assertEquals(header.getSize(), FileUtils.sizeOf(attachmentTestFunctions.createTestImage()));
		assertEquals(header.getMimeType(), MimeTypeUtils.IMAGE_PNG_VALUE);
		assertTrue(RELATIVE_THUMBNAIL_URL_PATTERN.test(header.getBigThumbnailUrl()));
		assertTrue(RELATIVE_THUMBNAIL_URL_PATTERN.test(header.getSmallThumbnailUrl()));
	}

	@Test public void testLoadAttachmentUrl() {
		DataServicesAttachmentURL attachmentURL = loadAttachmentUrlOperation.rpc(attachmentId, docRef.toString());
		assertTrue(RELATIVE_ATTACHMENT_URL_PATTERN.test(attachmentURL.getLocation()));
	}

	@Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "Attachment \\[.*] not found")
	public void attachmentNotFound_ShouldThrowException() {
		loadAttachmentHeaderOperation.rpc("notExistedAttachmentId", docRef.toString());
	}
}
