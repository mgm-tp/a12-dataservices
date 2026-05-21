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

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.quartz.JobExecutionException;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;

import lombok.NonNull;

public class CleanUpDirtyAttachmentsJobTT extends AbstractAttachmentTT {

	private AttachmentHeader attachmentHeaderWithoutReferences;
	private AttachmentHeader attachmentHeaderWithOneReference;
	private AttachmentHeader attachmentHeaderWithTwoReferences;

	public CleanUpDirtyAttachmentsJobTT(String storageType) {
		super(storageType);
	}

	@Override
	protected void initializeWithSecurityBypass() throws Exception {

		attachmentTestFunctions.prepareDocumentModel();

		File tempFile1 = attachmentTestFunctions.createTestImage();
		attachmentHeaderWithoutReferences = attachmentService.createAttachment(
			FileUtils.openInputStream(tempFile1), "temp1.jpg", "", null, null);

		File tempFile2 = attachmentTestFunctions.createTestImage();
		attachmentHeaderWithOneReference = attachmentService.createAttachment(
			FileUtils.openInputStream(tempFile2), "temp2.jpg", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, null, null);
		attachmentHeaderService.assignAttachment(attachmentHeaderWithOneReference, makeReference("doc1", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL));

		File tempFile3 = attachmentTestFunctions.createTestImage();
		attachmentHeaderWithTwoReferences = attachmentService.createAttachment(
			FileUtils.openInputStream(tempFile3), "temp3.jpg", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, null, null);
		attachmentHeaderService.assignAttachment(attachmentHeaderWithTwoReferences, makeReference("doc1", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL));
		attachmentHeaderService.assignAttachment(attachmentHeaderWithTwoReferences, makeReference("doc2", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL));
	}

	@Transactional
	@Test public void deleteCorrectly() throws JobExecutionException {
		// Attachments (and thumbnails) exist before
		assertUnassignedAttachment(attachmentHeaderWithoutReferences);
		assertAttachment(attachmentHeaderWithOneReference.getAttachmentId(), makeReference("doc1", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL), true);
		assertThumbnail(attachmentHeaderWithOneReference.getAttachmentId(), ThumbnailType.SMALL, true);
		assertThumbnail(attachmentHeaderWithOneReference.getAttachmentId(), ThumbnailType.BIG, true);
		assertAttachment(attachmentHeaderWithTwoReferences.getAttachmentId(), makeReference("doc1", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL), true);

		@NonNull AttachmentReference<?> attachmentReference1 = makeReference("doc1", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);
		attachmentHeaderService.load(attachmentHeaderWithOneReference.getAttachmentId()).ifPresent(
			h1 -> attachmentHeaderService.unAssignAttachment(h1, attachmentReference1));
		@NonNull AttachmentReference<?> attachmentReference = makeReference("doc1", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);
		attachmentHeaderService.load(
			attachmentHeaderWithTwoReferences.getAttachmentId()).ifPresent(h -> attachmentHeaderService.unAssignAttachment(h, attachmentReference));

		cleanUpDirtyAttachmentsJob.execute(null);

		// Attachment without references was not deleted because it was not set to dirty
		assertUnassignedAttachment(attachmentHeaderWithoutReferences);

		// Attachment with one references was deleted (together with thumbnails and header)
		assertAttachment(attachmentHeaderWithOneReference.getAttachmentId(), makeReference("doc1", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL), false);
		assertThumbnail(attachmentHeaderWithOneReference.getAttachmentId(), ThumbnailType.SMALL, false);
		assertThumbnail(attachmentHeaderWithOneReference.getAttachmentId(), ThumbnailType.BIG, false);
		assertHeader(attachmentHeaderWithOneReference.getAttachmentId(), false);

		// Attachment with two references was not deleted because there is still one reference (doc2)
		assertAttachment(attachmentHeaderWithTwoReferences.getAttachmentId(), makeReference("doc2", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL), true);
	}

	private AttachmentReference<DocumentReference> makeReference(String docId, String modelName) {
		return AttachmentReference.<DocumentReference>builder()
			.type(AttachmentReferenceType.DOCUMENT)
			.reference(DocumentReference.builder()
				.documentId(docId)
				.documentModelName(modelName)
				.build())
			.build();
	}

}
