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
package com.mgmtp.a12.dataservices.attachment;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.AttachmentTestFunctions;
import com.mgmtp.a12.dataservices.document.DocumentReference;

import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class LoadAttachmentAndThumbnailV2IT extends AbstractSpringContextIT {

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

	@Test public void testLoadAttachmentAndThumbnailV2() throws Exception {
		preparedDocument = attachmentTestFunctions.prepareDocumentWithAttachmentV2();
		docRef = preparedDocument.getDataServicesDocument().getMetadata().getDocRef();
		attachmentId = preparedDocument.getImageAttachment().getAttachmentId();
		// load attachment
		DataServicesAttachmentURL attachmentURL = loadAttachmentUrlOperation.rpc(attachmentId, docRef);
		assertTrue(ATTACHMENT_SECURED_URL_PATTERN.test(attachmentURL.getLocation()));
		// load thumbnails
		AttachmentThumbnailUrl thumbnails = loadThumbnailUrlOperation.rpc(attachmentId);
		assertNotNull(thumbnails);
		assertTrue(PUBLIC_URL_PATTERN.test(thumbnails.getBigThumbnailUrl()));
		assertTrue(PUBLIC_URL_PATTERN.test(thumbnails.getSmallThumbnailUrl()));
		assertNotEquals(thumbnails.getBigThumbnailUrl(), thumbnails.getSmallThumbnailUrl());
	}
}
