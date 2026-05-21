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
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import com.mgmtp.a12.dataservices.attachment.internal.CleanUpStaleAttachmentsJob;
import org.apache.commons.io.FileUtils;
import org.quartz.Calendar;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.AttachmentHeaderEntity;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.repository.AttachmentHeaderJpaRepository;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.exception.CorruptedDataException;

import static com.mgmtp.a12.dataservices.AttachmentTestFunctions.BUSINESS_PARTNER;
import static com.mgmtp.a12.dataservices.attachment.internal.CleanUpStaleAttachmentsJob.ATTACHMENT_EXPIRE_HOURS;
import static org.testng.Assert.assertTrue;

public class CleanUpStaleAttachmentsJobTT extends AbstractAttachmentTT {

	// UUIDs never contain 'x'
	private AttachmentHeader attachmentHeaderWithoutReferencesOneYearOld;
	private AttachmentHeader attachmentHeaderWithoutReferences10SecondsOld;
	private AttachmentHeader attachmentHeaderWithOneReference;

	@Autowired CleanUpStaleAttachmentsJob cleanUpStaleAttachmentsJob;
	@Autowired AttachmentHeaderJpaRepository attachmentHeaderJpaRepo;

	public CleanUpStaleAttachmentsJobTT(String storageType) {
		super(storageType);
	}

	@Override
	protected void initializeWithSecurityBypass() throws Exception {

		attachmentTestFunctions.prepareDocumentModel();

		File tempFile1 = attachmentTestFunctions.createTestImage();
		attachmentHeaderWithoutReferencesOneYearOld = attachmentService.createAttachment(
			FileUtils.openInputStream(tempFile1), "temp1.jpg", BUSINESS_PARTNER, null, null);
		Optional<AttachmentHeaderEntity> headerEntity = attachmentHeaderJpaRepo.findById(attachmentHeaderWithoutReferencesOneYearOld.getAttachmentId());
		assertTrue(headerEntity.isPresent());
		AttachmentHeaderEntity entity = headerEntity.get();
		entity.setCreatedAt(Instant.now().minusSeconds(1 * 365 * 24 * 3600));
		attachmentHeaderJpaRepo.save(entity);

		File tempFile2 = attachmentTestFunctions.createTestImage();
		attachmentHeaderWithoutReferences10SecondsOld = attachmentService.createAttachment(
			FileUtils.openInputStream(tempFile2), "temp1.jpg", BUSINESS_PARTNER, null, null);
		headerEntity = attachmentHeaderJpaRepo.findById(attachmentHeaderWithoutReferences10SecondsOld.getAttachmentId());
		assertTrue(headerEntity.isPresent());
		entity = headerEntity.get();
		entity.setCreatedAt(Instant.now().minusSeconds(10));
		attachmentHeaderJpaRepo.save(entity);

		File tempFile3 = attachmentTestFunctions.createTestImage();
		attachmentHeaderWithOneReference = attachmentService.createAttachment(
			FileUtils.openInputStream(tempFile3), "temp2.jpg", BUSINESS_PARTNER, null, null);
		attachmentHeaderService.assignAttachment(attachmentHeaderWithOneReference, getReference("doc1", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL));

	}

	@Test(expectedExceptions = { CorruptedDataException.class }, expectedExceptionsMessageRegExp = "The attachment file could not be found in storage.")
	public void deleteCorrectly() throws JobExecutionException {
		// Attachments exist before
		assertUnassignedAttachment(attachmentHeaderWithoutReferencesOneYearOld);
		assertUnassignedAttachment(attachmentHeaderWithoutReferences10SecondsOld);
		assertAttachment(attachmentHeaderWithOneReference.getAttachmentId(),
			getReference("doc1", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL),
			true);

		JobExecutionContext context = new TestCleanUpStaleAttachmentsJobExecutionContext();
		// Delete all attachments older than 5 minutes and without references
		cleanUpStaleAttachmentsJob.execute(context);

		// 10 seconds old attachment without references was not deleted because it was too young to die
		assertUnassignedAttachment(attachmentHeaderWithoutReferences10SecondsOld);

		// Attachment with one references was not deleted
		assertAttachment(attachmentHeaderWithOneReference.getAttachmentId(), getReference("doc1", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL), true);

		// One-year-old attachment without references was deleted
		assertHeader(attachmentHeaderWithoutReferencesOneYearOld.getAttachmentId(), false);
		// Should throw CorruptedDataException
		assertUnassignedAttachment(attachmentHeaderWithoutReferencesOneYearOld);
	}

	private AttachmentReference<DocumentReference> getReference(String docId, String modelName) {
		return AttachmentReference.<DocumentReference>builder()
			.type(AttachmentReferenceType.DOCUMENT)
			.reference(DocumentReference.builder()
				.documentId(docId)
				.documentModelName(modelName).build())
			.build();
	}

	public static class TestCleanUpStaleAttachmentsJobExecutionContext implements JobExecutionContext {

		@Override public Scheduler getScheduler() {
			return null;
		}

		@Override public Trigger getTrigger() {
			return null;
		}

		@Override public Calendar getCalendar() {
			return null;
		}

		@Override public boolean isRecovering() {
			return false;
		}

		@Override public TriggerKey getRecoveringTriggerKey() throws IllegalStateException {
			return null;
		}

		@Override public int getRefireCount() {
			return 0;
		}

		@Override public JobDataMap getMergedJobDataMap() {

			return new JobDataMap(Map.of(ATTACHMENT_EXPIRE_HOURS, 300));
		}

		@Override public JobDetail getJobDetail() {
			return null;
		}

		@Override public Job getJobInstance() {
			return null;
		}

		@Override public Date getFireTime() {
			return null;
		}

		@Override public Date getScheduledFireTime() {
			return null;
		}

		@Override public Date getPreviousFireTime() {
			return null;
		}

		@Override public Date getNextFireTime() {
			return null;
		}

		@Override public String getFireInstanceId() {
			return null;
		}

		@Override public Object getResult() {
			return null;
		}

		@Override public void setResult(Object result) {
			// Needed only to satisfy interface for mocking. No actual implementation is needed
		}

		@Override public long getJobRunTime() {
			return 0;
		}

		@Override public void put(Object key, Object value) {
			//Needed to satisfy the interface, but not used in the test
		}

		@Override public Object get(Object key) {
			return null;
		}
	}
}
