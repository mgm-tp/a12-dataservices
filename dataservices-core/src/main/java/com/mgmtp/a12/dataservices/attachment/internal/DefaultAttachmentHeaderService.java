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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.time.StopWatch;

import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentReference;
import com.mgmtp.a12.dataservices.attachment.header.AttachmentHeaderService;
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
@Slf4j
@RequiredArgsConstructor
public class DefaultAttachmentHeaderService implements AttachmentHeaderService {

	private final AttachmentHeaderRepository attachmentHeaderRepository;
	private final DirtyAttachmentService dirtyAttachmentService;

	public AttachmentHeader create(AttachmentHeader header) {
		attachmentHeaderRepository.create(header);
		return header;
	}

	public void delete(@NonNull String attachmentId) {
		attachmentHeaderRepository.delete(attachmentId);
	}

	public Optional<AttachmentHeader> load(@NonNull String attachmentId) {
		StopWatch stopWatch = StopWatch.createStarted();
		Optional<AttachmentHeader> attachmentHeader = attachmentHeaderRepository.findById(attachmentId);
		log.debug("Attachment header [{}] loaded in [{}] ms", attachmentId, stopWatch.getTime());
		return attachmentHeader;
	}

	public List<AttachmentHeader> loadUnassignedAttachmentsOlderThan(int tmpAttachmentExpireHours) {
		return attachmentHeaderRepository.findUnassignedAttachmentsOlderThan(Instant.now().minusSeconds(tmpAttachmentExpireHours * 3600L));
	}

	public void assignAttachment(@NonNull AttachmentHeader header, @NonNull AttachmentReference<? extends GenericReference> reference) {
		if (attachmentHeaderRepository.referenceExists(header, reference)) {
			log.info("Did not assign reference [{}] to attachment [{}] as it already exists", reference.getReference().toString(), header.getAttachmentId());
		} else {
			attachmentHeaderRepository.addReference(header, reference);
		}
	}

	public void unAssignAttachment(@NonNull AttachmentHeader header, @NonNull AttachmentReference<? extends GenericReference> reference) {
		boolean attachmentNotExist = load(header.getAttachmentId()).isEmpty();

		if (attachmentNotExist) {
			throw new NotFoundException(ExceptionKeys.ATTACHMENT_GENERAL_ERROR_KEY,
				String.format("Unable to unassign attachment %s.", header.getAttachmentId()));
		}

		attachmentHeaderRepository.removeReference(header, reference);
		dirtyAttachmentService.markAttachmentAsDirty(header.getAttachmentId());
	}

	public void unAssignAttachments(@NonNull Collection<DocumentReference> documentReferences) {
		attachmentHeaderRepository.findAndRemoveReferencesFor(documentReferences)
			.forEach(attachmentReference -> dirtyAttachmentService.markAttachmentAsDirty(attachmentReference.getAttachmentHeader().getId()));
	}
}
