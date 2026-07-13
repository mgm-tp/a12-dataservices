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
package com.mgmtp.a12.dataservices.rpc.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.AttachmentTestFunctions;
import com.mgmtp.a12.dataservices.attachment.ThumbnailType;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.operation.internal.CopyDocumentOperation;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;

import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

public class CopyDocumentOperationIT extends AbstractSpringContextIT {

	@Autowired private CopyDocumentOperation copyDocumentOperation;
	@Autowired private DocumentService documentService;

	private AttachmentTestFunctions.PreparedDocument preparedDocument;
	private String preparedImageAttachmentId;

	@Override protected void initializeWithSecurityBypass() throws Exception {
		attachmentTestFunctions.prepareDocumentModel();
		preparedDocument = attachmentTestFunctions.prepareDocumentWith2AttachmentsV2();
		preparedImageAttachmentId = preparedDocument.getImageAttachment().getAttachmentId();
	}

	@Test public void testCopyDocumentOperation() {

		DocumentReference resultDocumentReference = copyDocumentOperation.rpc(preparedDocument.getDataServicesDocument().getMetadata().getDocRef().toString(), null).docRef();

		assertNotNull(resultDocumentReference);
		assertNotEquals(resultDocumentReference, preparedDocument.getDataServicesDocument().getMetadata().getDocRef());

		DataServicesDocument resultDocument = documentService.load(resultDocumentReference)
			.orElseThrow(
				() -> new NotFoundException(ExceptionKeys.DOCUMENT_NOT_FOUND_ERROR_KEY, "Document [%s] not found".formatted(resultDocumentReference)));

		assertNotNull(resultDocument);
		assertNotNull(resultDocument.getMetadata().getDocRef());
		assertNotEquals(resultDocument.getMetadata().getDocRef(), preparedDocument.getDataServicesDocument().getMetadata().getDocRef());
		assertNotNull(resultDocument.getKernelDocument());

		assertAttachments(preparedDocument.getDataServicesDocument().getMetadata().getDocRef());
		assertAttachments(preparedDocument.getDataServicesDocument().getMetadata().getDocRef());

	}

	private void assertAttachments(DocumentReference docRef) {
		Assert.assertTrue(attachmentService.findAttachmentUrl(preparedImageAttachmentId, docRef).isPresent());
		Assert.assertTrue(attachmentService.findThumbnailUrl(preparedImageAttachmentId, ThumbnailType.BIG).isPresent());
		Assert.assertTrue(attachmentService.findThumbnailUrl(preparedImageAttachmentId, ThumbnailType.SMALL).isPresent());
	}
}
