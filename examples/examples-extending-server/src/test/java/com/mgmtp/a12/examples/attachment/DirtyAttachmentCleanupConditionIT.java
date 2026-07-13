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
package com.mgmtp.a12.examples.attachment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.attachment.AttachmentAnnotation;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentReference;
import com.mgmtp.a12.dataservices.attachment.AttachmentReferenceType;
import com.mgmtp.a12.dataservices.attachment.AttachmentService;
import com.mgmtp.a12.dataservices.attachment.header.AttachmentHeaderService;
import com.mgmtp.a12.dataservices.attachment.internal.CleanUpDirtyAttachmentsJob;
import com.mgmtp.a12.dataservices.attachment.internal.DirtyAttachmentService;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.reference.GenericReference;
import com.mgmtp.a12.examples.AbstractITBase;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.examples.attachment.ReusableAttachmentCleanupCondition.REUSABLE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ActiveProfiles({ "dataservices-example-attachments_env" })
@Test public class DirtyAttachmentCleanupConditionIT extends AbstractITBase {

	public static final String ATTACHMENT_PATH = "attachment/";
	public static final String ATTACHMENT_DOCUMENT_PATH = ATTACHMENT_PATH + DOCUMENT_PATH;

	@Autowired private AttachmentService attachmentService;
	@Autowired private DirtyAttachmentService dirtyAttachmentService;
	@Autowired private CleanUpDirtyAttachmentsJob cleanUpDirtyAttachmentsJob;
	@Autowired private AttachmentHeaderService attachmentHeaderService;

	@DataProvider public static Object[][] attachmentProvider() {
		return new Object[][] {
			new Object[] { "reusable", List.of(AttachmentAnnotation.builder().name(REUSABLE).value("true").build()), true },
			new Object[] { "nonReusable", List.of(), false },
		};
	}

	@BeforeClass
	public void beforeClass() {
		setUserTo(UserConstants.ADMIN_USER);
		Optional.ofNullable(modelService.load(BUSINESS_PARTNER_DOCUMENT_MODEL))
				.ifPresent(model -> modelService.delete(model.getHeader().getId()));
		modelsFunctions.createModel(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
	}

	@Test(dataProvider = "attachmentProvider")
	public void testAttachmentCleanup(String name, List<AttachmentAnnotation> annotations, boolean presentAfterUnassignment) throws JobExecutionException, IOException {
		AttachmentHeader savedAttachmentHeader = attachmentService.createAttachment(new ByteArrayInputStream("attachment content".getBytes()),
			name + ".txt", BUSINESS_PARTNER_DOCUMENT_MODEL, "/BusinessPartnerRoot/Attachment", annotations);
		String attachmentId = savedAttachmentHeader.getAttachmentId();

		DocumentReference createdBusinessPartner = documentFunctions.createDocumentFromFileAndGetDocRef(BUSINESS_PARTNER_DOCUMENT_MODEL, PARTNER_DOCUMENT_FILE);

		AttachmentReference<GenericReference> attachmentReference = AttachmentReference.builder()
			.reference(createdBusinessPartner)
			.type(AttachmentReferenceType.DOCUMENT)
			.build();

		Optional<AttachmentHeader> loadedHeader = attachmentHeaderService.load(attachmentId);
		assertTrue(loadedHeader.isPresent());

		attachmentHeaderService.assignAttachment(loadedHeader.get(), attachmentReference);

		dirtyAttachmentService.markAttachmentAsDirty(attachmentId);
		cleanUpDirtyAttachmentsJob.execute(null);

		loadedHeader = attachmentHeaderService.load(attachmentId);
		assertTrue(loadedHeader.isPresent());

		attachmentHeaderService.unAssignAttachment(loadedHeader.get(), attachmentReference);
		cleanUpDirtyAttachmentsJob.execute(null);

		loadedHeader = attachmentHeaderService.load(attachmentId);
		assertEquals(loadedHeader.isPresent(), presentAfterUnassignment);
	}

}
