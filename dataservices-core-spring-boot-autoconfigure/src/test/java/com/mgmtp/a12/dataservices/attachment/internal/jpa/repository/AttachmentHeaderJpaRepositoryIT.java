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
package com.mgmtp.a12.dataservices.attachment.internal.jpa.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeTypeUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.attachment.AttachmentReferenceType;
import com.mgmtp.a12.dataservices.attachment.TypeOfTheContent;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.AttachmentHeaderEntity;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.AttachmentReferenceEntity;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.DirtyAttachmentEntity;

public class AttachmentHeaderJpaRepositoryIT extends AbstractSpringContextIT {

	private static final String VALID_ATTACHMENT_ID = "VALID_ATTACHMENT_ID";
	private String attachmentHasIdReference;
	private static final String ATTACHMENT_ID_IS_DIRTY = "ATTACHMENT_ID_IS_DIRTY";
	private static final String ATTACHMENT_ID_HAS_REFERENCE_AND_IS_DIRTY = "ATTACHMENT_ID_HAS_REFERENCE_AND_IS_DIRTY";

	@Autowired private AttachmentReferenceJpaRepository attachmentReferenceJpaRepository;
	@Autowired private AttachmentHeaderJpaRepository attachmentHeaderJpaRepository;
	@Autowired private DirtyAttachmentJpaRepository dirtyAttachmentJpaRepository;

	@BeforeMethod
	public void setup() {
		attachmentHasIdReference = UUID.randomUUID().toString();
	}

	@Test
	public void testFindUnassignedAttachmentsOlderThan_shouldReturnListHeader_whenGivenHeaderHaveNoReferenceAndIsNotDirtyAttachment() {
		Instant threshold = Instant.now().minusSeconds(dataServicesCoreProperties.getJobs().getAttachments().getTemporary().getExpireHours() * 3600L);
		createAttachmentHeader(VALID_ATTACHMENT_ID, threshold);
		List<AttachmentHeaderEntity> attachmentHeaderEntities = attachmentHeaderJpaRepository.findUnassignedAndNotDirtyAttachmentsOlderThan(Instant.now());

		Assert.assertNotNull(attachmentHeaderEntities);
		Assert.assertFalse(CollectionUtils.isEmpty(attachmentHeaderEntities));
		Assert.assertTrue(attachmentHeaderEntities.stream().anyMatch(entity -> VALID_ATTACHMENT_ID.equals(entity.getId())));
	}

	@Test
	public void testFindUnassignedAttachmentsOlderThan_shouldNotFindHeader_whenGivenHeaderHaveReferenceButIsNotDirtyAttachment() {
		Instant threshold = Instant.now().minusSeconds(dataServicesCoreProperties.getJobs().getAttachments().getTemporary().getExpireHours() * 3600L);
		AttachmentHeaderEntity headerEntity = createAttachmentHeader(attachmentHasIdReference, threshold);
		createAttachmentReference(attachmentHasIdReference, headerEntity);
		List<AttachmentHeaderEntity> attachmentHeaderEntities = attachmentHeaderJpaRepository.findUnassignedAndNotDirtyAttachmentsOlderThan(Instant.now());

		Assert.assertNotNull(attachmentHeaderEntities);
		Assert.assertTrue(attachmentHeaderEntities.stream().noneMatch(entity -> attachmentHasIdReference.equals(entity.getId())));
	}

	@Test
	public void testFindUnassignedAttachmentsOlderThan_shouldNotFindHeader_whenGivenHeaderHaveNoReferenceButIsDirtyAttachment() {
		Instant threshold = Instant.now().minusSeconds(dataServicesCoreProperties.getJobs().getAttachments().getTemporary().getExpireHours() * 3600L);
		createAttachmentHeader(ATTACHMENT_ID_IS_DIRTY, threshold);
		createDirtyAttachment(ATTACHMENT_ID_IS_DIRTY);
		List<AttachmentHeaderEntity> attachmentHeaderEntities = attachmentHeaderJpaRepository.findUnassignedAndNotDirtyAttachmentsOlderThan(Instant.now());

		Assert.assertNotNull(attachmentHeaderEntities);
		Assert.assertTrue(attachmentHeaderEntities.stream().noneMatch(entity -> ATTACHMENT_ID_IS_DIRTY.equals(entity.getId())));
		attachmentHeaderEntities.forEach(entity -> dirtyAttachmentJpaRepository.deleteById(entity.getId()));
	}

	@Test
	public void testFindUnassignedAttachmentsOlderThan_shouldNotFindHeader_whenGivenHeaderHaveReferenceAndIsDirtyAttachment() {
		Instant threshold = Instant.now().minusSeconds(dataServicesCoreProperties.getJobs().getAttachments().getTemporary().getExpireHours() * 3600L);
		AttachmentHeaderEntity headerEntity = createAttachmentHeader(ATTACHMENT_ID_HAS_REFERENCE_AND_IS_DIRTY, threshold);
		createAttachmentReference(ATTACHMENT_ID_HAS_REFERENCE_AND_IS_DIRTY, headerEntity);
		createDirtyAttachment(ATTACHMENT_ID_HAS_REFERENCE_AND_IS_DIRTY);
		List<AttachmentHeaderEntity> attachmentHeaderEntities = attachmentHeaderJpaRepository.findUnassignedAndNotDirtyAttachmentsOlderThan(Instant.now());

		Assert.assertNotNull(attachmentHeaderEntities);
		Assert.assertTrue(attachmentHeaderEntities.stream().noneMatch(entity -> ATTACHMENT_ID_HAS_REFERENCE_AND_IS_DIRTY.equals(entity.getId())));
		attachmentHeaderEntities.forEach(entity -> dirtyAttachmentJpaRepository.deleteById(entity.getId()));
	}

	private void createAttachmentReference(String reference, AttachmentHeaderEntity attachmentHeaderEntity) {
		attachmentReferenceJpaRepository.save(AttachmentReferenceEntity.builder()
			.attachmentHeader(attachmentHeaderEntity)
			.reference(reference)
			.type(AttachmentReferenceType.DOCUMENT)
			.build());
	}

	private AttachmentHeaderEntity createAttachmentHeader(String attachmentId, Instant createdAt) {
		return attachmentHeaderJpaRepository.save(AttachmentHeaderEntity.builder()
			.id(attachmentId)
			.fileName("fileName")
			.createdBy("test")
			.createdAt(createdAt)
			.mimeType(MimeTypeUtils.IMAGE_PNG_VALUE)
			.typeOfTheContent(TypeOfTheContent.ATTACHMENT_SECURED.toString())
			.build());
	}

	private void createDirtyAttachment(String attachmentId) {
		dirtyAttachmentJpaRepository.save(DirtyAttachmentEntity.builder()
			.attachmentId(attachmentId)
			.createdBy("test")
			.createdAt(Instant.now())
			.build());
	}
}
