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
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.quartz.Calendar;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.attachment.internal.CleanUpDirtyAttachmentsJob;
import com.mgmtp.a12.dataservices.attachment.internal.DirtyAttachmentMapper;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.DirtyAttachmentEntity;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.repository.DirtyAttachmentJpaRepository;
import com.mgmtp.a12.dataservices.attachment.persitence.IAttachmentRepository;
import com.mgmtp.a12.dataservices.common.quantity.internal.QuantityParsers;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.exception.CorruptedDataException;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.reference.GenericReference;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import static com.mgmtp.a12.dataservices.AttachmentTestFunctions.BUSINESS_PARTNER;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class CleanUpDirtyAttachmentsJobIT extends AbstractSpringContextIT {

	@Autowired private AttachmentService attachmentService;
	@Autowired private CleanUpDirtyAttachmentsJob cleanUpDirtyAttachmentsJob;
	@Autowired private DirtyAttachmentJpaRepository dirtyAttachmentJpaRepo;
	@Autowired private DataServicesCoreProperties dataServicesCoreProperties;
	@Autowired private DirtyAttachmentMapper dirtyAttachmentMapper;
	@Autowired private ModelService modelService;
	@MockitoSpyBean private IAttachmentRepository contentStoreAttachmentRepository;
	private AttachmentHeader attachmentHeaderWithoutReferences;
	private AttachmentHeader attachmentHeaderWithOneReference;
	private AttachmentHeader attachmentHeaderWithTwoReferences;

	@BeforeMethod
	public void setUp() throws Exception {
		setUserTo(UserConstants.ADMIN_USER);
		modelService.delete(BUSINESS_PARTNER);
		reset(contentStoreAttachmentRepository);

		attachmentTestFunctions.prepareDocumentModel();

		attachmentHeaderWithoutReferences = saveAttachment(attachmentTestFunctions.createTestImage(), "temp1.jpg");

		attachmentHeaderWithOneReference = saveAttachment(attachmentTestFunctions.createTestImage(), "temp2.jpg");
		attachmentHeaderService.assignAttachment(attachmentHeaderWithOneReference, makeReference("doc1", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL));

		attachmentHeaderWithTwoReferences = saveAttachment(attachmentTestFunctions.createTestImage(), "temp3.jpg");
		attachmentHeaderService.assignAttachment(attachmentHeaderWithTwoReferences, makeReference("doc1", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL));
		attachmentHeaderService.assignAttachment(attachmentHeaderWithTwoReferences, makeReference("doc2", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL));

		attachmentHeaderService.unAssignAttachment(attachmentHeaderWithOneReference, makeReference("doc1", DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL));
	}

	@SneakyThrows
	@Test public void testUnassignmentSuccess() {
		assertHeaderNotDeleted();
		assertDirtyAttachmentNotDeleted();

		cleanUpDirtyAttachmentsJob.execute(new TestJobExecutionContext());
		assertHeaderDeleted();
		assertDirtyAttachmentDeleted();
	}

	@SneakyThrows
	@Transactional
	@Test public void testUnassignmentRecoverable() {

		setMockException(true);
		assertHeaderNotDeleted();
		assertDirtyAttachmentNotDeleted();

		Instant firstReferenceTimestamp = Instant.now();
		cleanUpDirtyAttachmentsJob.execute(new TestJobExecutionContext());

		assertHeaderNotDeleted();
		assertDirtyAttachmentNotDeleted();
		assertDirtyAttachmentState(firstReferenceTimestamp, Instant.now(), 1);

		Instant secondReferenceTimestamp = Instant.now();
		cleanUpDirtyAttachmentsJob.execute(new TestJobExecutionContext());

		assertHeaderNotDeleted();
		assertDirtyAttachmentNotDeleted();
		assertDirtyAttachmentState(firstReferenceTimestamp, secondReferenceTimestamp, 1);

	}

	@SneakyThrows
	@Transactional
	@Test public void testUnassignmentRecoverableRetry() {

		setMockException(true);
		assertHeaderNotDeleted();
		assertDirtyAttachmentNotDeleted();

		Instant referenceTimestamp = Instant.now();
		cleanUpDirtyAttachmentsJob.execute(new TestJobExecutionContext());

		assertHeaderNotDeleted();
		assertDirtyAttachmentNotDeleted();
		assertDirtyAttachmentState(referenceTimestamp, Instant.now(), 1);

		referenceTimestamp = Instant.now();
		updateDirtyAttachmentLastTry(referenceTimestamp
			.minus(QuantityParsers.parseTimeQuantity(dataServicesCoreProperties.getAttachments().getCleanup().getRetry().getDelay()), ChronoUnit.SECONDS)
			.minus(1, ChronoUnit.SECONDS));

		cleanUpDirtyAttachmentsJob.execute(new TestJobExecutionContext());

		assertHeaderNotDeleted();
		assertDirtyAttachmentNotDeleted();
		assertDirtyAttachmentState(referenceTimestamp, Instant.now(), 2);

		referenceTimestamp = Instant.now();
		updateDirtyAttachmentLastTry(referenceTimestamp
			.minus(QuantityParsers.parseTimeQuantity(dataServicesCoreProperties.getAttachments().getCleanup().getRetry().getDelay()), ChronoUnit.SECONDS)
			.minus(1, ChronoUnit.SECONDS));
		updateDirtyAttachmentExecCount(dataServicesCoreProperties.getAttachments().getCleanup().getRetry().getMax());

		cleanUpDirtyAttachmentsJob.execute(new TestJobExecutionContext());

		assertHeaderNotDeleted();
		assertDirtyAttachmentDeleted();
	}

	private void updateDirtyAttachmentLastTry(Instant lastTry) {
		DirtyAttachmentEntity dirtyAttachmentEntity = dirtyAttachmentJpaRepo.getReferenceById(attachmentHeaderWithOneReference.getAttachmentId());
		dirtyAttachmentEntity.setLastTry(lastTry);
		dirtyAttachmentJpaRepo.save(dirtyAttachmentEntity);
	}

	private void updateDirtyAttachmentExecCount(int cnt) {
		DirtyAttachmentEntity dirtyAttachmentEntity = dirtyAttachmentJpaRepo.getReferenceById(attachmentHeaderWithOneReference.getAttachmentId());
		dirtyAttachmentEntity.setExecCount(cnt);
		dirtyAttachmentJpaRepo.save(dirtyAttachmentEntity);
	}

	private void assertDirtyAttachmentState(Instant before, Instant after, int execCount) {
		DirtyAttachment dirtyAttachment = getDirtyAttachment(attachmentHeaderWithOneReference);
		assertNotNull(dirtyAttachment.getLastTry());
		assertTrue(dirtyAttachment.getLastTry().isAfter(before), "Expected last try [" + dirtyAttachment.getLastTry() + "] to be after first timestamp [" + before + "]");
		assertTrue(dirtyAttachment.getLastTry().isBefore(after), "Expected last try [" + dirtyAttachment.getLastTry() + "] to be before second timestamp [" + after + "]");
		assertEquals(dirtyAttachment.getExecCount(), execCount);
	}

	@SneakyThrows
	@Test public void testUnassignmentUnRecoverable() {
		setMockException(false);
		assertHeaderNotDeleted();
		assertDirtyAttachmentNotDeleted();

		cleanUpDirtyAttachmentsJob.execute(new TestJobExecutionContext());
		assertHeaderNotDeleted();
		assertDirtyAttachmentDeleted();
	}

	private void assertDirtyAttachmentDeleted() {
		assertDirtyAttachmentNotExists(attachmentHeaderWithoutReferences);
		assertDirtyAttachmentNotExists(attachmentHeaderWithOneReference);
		assertDirtyAttachmentNotExists(attachmentHeaderWithTwoReferences);
	}

	@SneakyThrows
	@Test public void testUnassignmentUnUnknown() {
		setMockException(new RuntimeException());
		assertHeaderNotDeleted();
		assertDirtyAttachmentNotDeleted();

		cleanUpDirtyAttachmentsJob.execute(new TestJobExecutionContext());
		assertHeaderNotDeleted();
		assertDirtyAttachmentDeleted();
	}

	private void assertDirtyAttachmentNotDeleted() {
		assertDirtyAttachmentNotExists(attachmentHeaderWithoutReferences);
		assertDirtyAttachmentExists(attachmentHeaderWithOneReference);
		assertDirtyAttachmentNotExists(attachmentHeaderWithTwoReferences);
	}

	private void assertHeaderDeleted() {
		assertAttachmentHeaderExists(attachmentHeaderWithoutReferences);
		assertAttachmentHeaderNotExists(attachmentHeaderWithOneReference);
		assertAttachmentHeaderExists(attachmentHeaderWithTwoReferences);
	}

	private void assertHeaderNotDeleted() {
		assertAttachmentHeaderExists(attachmentHeaderWithoutReferences);
		assertAttachmentHeaderExists(attachmentHeaderWithOneReference);
		assertAttachmentHeaderExists(attachmentHeaderWithTwoReferences);
	}

	private void setMockException(boolean recoverable) {
		CorruptedDataException exception = new CorruptedDataException("");
		exception.setRecoverable(recoverable);
		setMockException(exception);
	}

	private void setMockException(Throwable exception) {
		doThrow(exception).when(contentStoreAttachmentRepository).delete(anyString());
	}

	private DirtyAttachment getDirtyAttachment(AttachmentHeader attachmentHeader) {
		return dirtyAttachmentMapper.toDirtyAttachment(dirtyAttachmentJpaRepo.getReferenceById(attachmentHeader.getAttachmentId()));
	}

	private void assertAttachmentHeaderExists(AttachmentHeader attachmentHeader) {
		assertTrue(attachmentHeaderService.load(attachmentHeader.getAttachmentId()).isPresent());
	}

	private void assertAttachmentHeaderNotExists(AttachmentHeader attachmentHeader) {
		assertFalse(attachmentHeaderService.load(attachmentHeader.getAttachmentId()).isPresent());
	}

	private void assertDirtyAttachmentExists(AttachmentHeader attachmentHeader) {
		assertTrue(dirtyAttachmentJpaRepo.findAll().stream().anyMatch(da -> Objects.equals(da.getAttachmentId(), attachmentHeader.getAttachmentId())));
	}

	private void assertDirtyAttachmentNotExists(AttachmentHeader attachmentHeader) {

		assertTrue(dirtyAttachmentJpaRepo.findAll().stream().noneMatch(da -> Objects.equals(da.getAttachmentId(), attachmentHeader.getAttachmentId())));
	}

	private AttachmentHeader saveAttachment(File tempFile31, String image) throws IOException {
		return attachmentService.createAttachment(FileUtils.openInputStream(tempFile31), image, BUSINESS_PARTNER, "/BusinessPartnerRoot/AttachmentGroup", Collections.emptyList());
	}

	private AttachmentReference<? extends GenericReference> makeReference(String docId, String modelName) {
		return AttachmentReference.builder()
			.type(AttachmentReferenceType.DOCUMENT)
			.reference(DocumentReference.builder()
				.documentId(docId)
				.documentModelName(modelName)
				.build())
			.build();
	}

	@Getter
	private static class TestJobExecutionContext implements JobExecutionContext {
		private Scheduler scheduler;
		private Trigger trigger;
		private Calendar calendar;
		private boolean recovering;
		private TriggerKey recoveringTriggerKey;
		private int refireCount;
		private JobDataMap mergedJobDataMap;
		private JobDetail jobDetail;
		private Job jobInstance;
		private Date fireTime;
		private Date scheduledFireTime;
		private Date previousFireTime;
		private Date nextFireTime;
		private String fireInstanceId;
		@Setter private Object result;
		private long jobRunTime;

		@Override public void put(Object key, Object value) {
			// Needed only to satisfy interface for mocking. No actual implementation is needed
		}

		@Override public Object get(Object key) {
			return null;
		}
	}
}
