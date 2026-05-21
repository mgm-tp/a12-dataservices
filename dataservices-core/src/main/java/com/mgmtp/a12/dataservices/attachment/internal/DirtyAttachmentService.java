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
package com.mgmtp.a12.dataservices.attachment.internal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.DirtyAttachment;

import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.DirtyAttachmentEntity;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.repository.DirtyAttachmentJpaRepository;
import com.mgmtp.a12.dataservices.authorization.internal.UaaConnector;
import com.mgmtp.a12.dataservices.common.quantity.internal.QuantityParsers;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DirtyAttachmentService {

	private final DirtyAttachmentJpaRepository dirtyAttachmentJpaRepo;
	private final DirtyAttachmentMapper dirtyAttachmentMapper;
	private final DataServicesCoreProperties dataServicesCoreProperties;

	/**
	 * Marks the attachment with passed `attachmentId` as dirty (e.g. by writing the id into a table).
	 * Attachments marked as dirty will be deleted by a scheduled job.
	 *
	 * @param attachmentId The attachment id.
	 */
	public void markAttachmentAsDirty(@NonNull String attachmentId) {
		dirtyAttachmentJpaRepo.save(DirtyAttachmentEntity.builder()
			.attachmentId(attachmentId)
			.createdAt(Instant.now())
			.createdBy(UaaConnector.getCurrentUserName()).build());
	}

	protected void removeFromDirtyList(@NonNull AttachmentHeader header) {
		dirtyAttachmentJpaRepo.deleteById(header.getAttachmentId());
		log.info("Removed attachment with id = {} from dirty list", header.getAttachmentId());
	}

	protected DirtyAttachment beforeProcessing(@NonNull String attachmentId) {
		DirtyAttachmentEntity entity = dirtyAttachmentJpaRepo.getReferenceById(attachmentId);
		entity.setLastTry(Instant.now());
		entity.setExecCount(entity.getExecCount() + 1);
		return dirtyAttachmentMapper.toDirtyAttachment(dirtyAttachmentJpaRepo.save(entity));
	}

	public Stream<DirtyAttachment> getDirtyAttachmentsForDeletion() {
		Instant nowMinusDelay = Instant.now()
			.minus(QuantityParsers.parseTimeQuantity(dataServicesCoreProperties.getAttachments().getCleanup().getRetry().getDelay()), ChronoUnit.SECONDS);
		return dirtyAttachmentJpaRepo.findByLastTryBeforeOrLastTryIsNull(nowMinusDelay).stream()
			.map(dirtyAttachmentMapper::toDirtyAttachment);
	}
}
