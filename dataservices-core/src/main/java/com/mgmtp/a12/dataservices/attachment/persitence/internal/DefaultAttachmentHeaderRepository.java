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
package com.mgmtp.a12.dataservices.attachment.persitence.internal;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentReference;
import com.mgmtp.a12.dataservices.attachment.AttachmentReferenceType;
import com.mgmtp.a12.dataservices.attachment.TypeOfTheContent;
import com.mgmtp.a12.dataservices.attachment.internal.AttachmentMapper;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.AttachmentHeaderEntity;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.AttachmentReferenceEntity;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.repository.AttachmentHeaderJpaRepository;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.repository.AttachmentReferenceJpaRepository;
import com.mgmtp.a12.dataservices.attachment.persitence.AttachmentHeaderRepository;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.internal.DocumentationDiagram;
import com.mgmtp.a12.dataservices.reference.GenericReference;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@DocumentationDiagram
@RequiredArgsConstructor @Slf4j
public class DefaultAttachmentHeaderRepository implements AttachmentHeaderRepository {

	private final AttachmentHeaderJpaRepository attachmentHeaderJpaRepository;
	private final AttachmentReferenceJpaRepository attachmentReferenceJpaRepository;
	private final AttachmentMapper attachmentMapper;

	@Transactional
	@Override public void create(@NonNull AttachmentHeader header) {
		StopWatch stopWatch = StopWatch.createStarted();
		if (header.getTypeOfTheContent() == null) {
			header.setTypeOfTheContent(TypeOfTheContent.ATTACHMENT_SECURED);
		}
		attachmentHeaderJpaRepository.save(attachmentMapper.toAttachmentHeaderEntity(header));
		log.trace("Attachment header [{}] saved to database in [{}] ms", header.getAttachmentId(), stopWatch.getTime());
	}

	@Transactional(readOnly = true)
	@Override public Optional<AttachmentHeader> findById(@NonNull String attachmentId) {
		StopWatch stopWatch = StopWatch.createStarted();
		Optional<AttachmentHeader> attachmentHeader = attachmentHeaderJpaRepository.findById(attachmentId)
			.map(attachmentMapper::toAttachmentHeader);
		log.trace("Attachment header [{}] loaded from database in [{}] ms", attachmentId, stopWatch.getTime());
		return attachmentHeader;
	}

	@Transactional(readOnly = true)
	@Override public List<AttachmentHeader> findUnassignedAttachmentsOlderThan(Instant threshold) {
		StopWatch stopWatch = StopWatch.createStarted();
		List<AttachmentHeader> attachmentHeaders = attachmentHeaderJpaRepository.findUnassignedAndNotDirtyAttachmentsOlderThan(threshold)
			.stream()
			.map(attachmentMapper::toAttachmentHeader)
			.toList();
		log.trace("Attachment headers for unassigned attachments older than [{}] loaded from database in [{}] ms", threshold, stopWatch.getTime());
		return attachmentHeaders;
	}

	@Transactional
	@Override public void delete(@NonNull String attachmentId) {
		StopWatch stopWatch = StopWatch.createStarted();
		attachmentHeaderJpaRepository.findById(attachmentId)
			.ifPresent(entity -> {
				attachmentReferenceJpaRepository.deleteAllInBatch(entity.removeAllReferences());
				attachmentHeaderJpaRepository.delete(entity);
			});
		log.trace("Attachment header of attachment [{}] deleted from database in [{}] ms", attachmentId, stopWatch.getTime());
	}

	@Transactional
	@Override public void addReference(@NonNull AttachmentHeader header, @NonNull AttachmentReference<?> reference) {
		StopWatch stopWatch = StopWatch.createStarted();
		AttachmentHeaderEntity headerEntity = attachmentHeaderJpaRepository.findById(header.getAttachmentId())
			.orElseThrow(() -> new NotFoundException(ExceptionKeys.ATTACHMENT_GENERAL_ERROR_KEY,
			"Unable to assign attachment %s to final destination.".formatted(header.getAttachmentId())));
		attachmentReferenceJpaRepository.save(headerEntity.addReference(attachmentMapper.buildAttachmentReferenceEntity(reference)));
		log.trace("Attachment reference for attachment header [{}] stored in database in [{}] ms", header.getAttachmentId(), stopWatch.getTime());
	}

	@Transactional
	@Override public void removeReference(@NonNull AttachmentHeader header, @NonNull AttachmentReference<? extends GenericReference> reference) {
		StopWatch stopWatch = StopWatch.createStarted();
		attachmentReferenceJpaRepository.findByAttachmentHeaderIdAndReference(header.getAttachmentId(), reference.getReference().toString())
			.ifPresent(attachmentReferenceEntity -> {
				AttachmentHeaderEntity attachmentHeaderEntity = attachmentReferenceEntity.getAttachmentHeader();
				AttachmentReferenceEntity entity = attachmentHeaderEntity.removeReference(attachmentReferenceEntity);
				attachmentReferenceJpaRepository.delete(entity);
			});
		log.trace("Attachment reference for attachment header [{}] removed from database in [{}] ms", header.getAttachmentId(), stopWatch.getTime());
	}

	@Override public List<AttachmentReferenceEntity> findAndRemoveReferencesFor(@NonNull Collection<DocumentReference> documentReferences) {
		List<AttachmentReferenceEntity> attachmentReferences = attachmentReferenceJpaRepository.findAllByTypeAndReferenceIn(AttachmentReferenceType.DOCUMENT, documentReferences.stream().map(DocumentReference::toString).toList());
		attachmentReferenceJpaRepository.deleteAllInBatch(attachmentReferences);
		return attachmentReferences;
	}

	@Override public boolean referenceExists(@NonNull AttachmentHeader header, @NonNull AttachmentReference<? extends GenericReference> reference) {
		StopWatch stopWatch = StopWatch.createStarted();
		boolean result = attachmentReferenceJpaRepository.existsByAttachmentHeaderIdAndReference(header.getAttachmentId(), reference.getReference().toString());
		log.trace("Reference existence query executed for the attachment [{}] in [{}] ms", header.getAttachmentId(), stopWatch.getTime());
		return result;
	}
}
