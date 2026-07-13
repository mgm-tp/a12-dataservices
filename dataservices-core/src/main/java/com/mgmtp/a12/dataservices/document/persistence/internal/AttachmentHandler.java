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
package com.mgmtp.a12.dataservices.document.persistence.internal;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentReference;
import com.mgmtp.a12.dataservices.attachment.AttachmentReferenceType;
import com.mgmtp.a12.dataservices.attachment.header.AttachmentHeaderService;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.internal.attachment.AttachmentSupport;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.reference.GenericReference;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AttachmentHandler {

	private final AttachmentHeaderService attachmentHeaderService;
	private final AttachmentSupport attachmentSupport;

	@Transactional public void deleteAttachmentsForDocument(DocumentV2 document, DocumentReference docRef) {
		try {
			StopWatch stopWatch = StopWatch.createStarted();
			attachmentSupport.collectAttachmentIDs(document).forEach(attachmentId -> unAssignAttachment(docRefToAttRef(docRef), attachmentId));
			log.debug("Attachments deleted for document [{}] in [{}] ms", docRef, stopWatch.getTime());
		} catch (Exception ex) {
			log.warn("Exception occurred during deletion of attachments for document [{}/{}]", document.getDocumentModelId(), docRef, ex);
		}
	}

	@Transactional public void deleteAttachmentsForDocuments(Collection<DocumentReference> documentReferences) {
		attachmentHeaderService.unAssignAttachments(documentReferences);
	}

	@Transactional public void synchronizeAttachments(List<String> uploadedAttachmentsIds, List<String> existingAttachmentIds, DocumentReference docRef) {
		//only new one
		AttachmentReference<GenericReference> attachmentReference = docRefToAttRef(docRef);
		uploadedAttachmentsIds.stream()
			.filter(StringUtils::isNotEmpty)
			.filter(attachmentId -> !existingAttachmentIds.contains(attachmentId))
			.forEach(attachmentId -> assignAttachment(attachmentReference, attachmentId));
		//exiting which needs to be deleted.
		existingAttachmentIds.stream()
			.filter(existingId -> !uploadedAttachmentsIds.contains(existingId))
			.forEach(attachmentId -> unAssignAttachment(attachmentReference, attachmentId));
	}

	private void unAssignAttachment(AttachmentReference<GenericReference> attachmentReference, String attachmentId) {
		attachmentHeaderService.load(attachmentId).ifPresent(h -> attachmentHeaderService.unAssignAttachment(h,
			attachmentReference));
	}

	private static AttachmentReference<GenericReference> docRefToAttRef(DocumentReference docRef) {
		return AttachmentReference.builder()
			.type(AttachmentReferenceType.DOCUMENT)
			.reference(docRef)
			.build();
	}

	private void assignAttachment(AttachmentReference<GenericReference> attachmentReference, String attachmentId) {
		AttachmentHeader attachmentHeader = attachmentHeaderService.load(attachmentId).orElseThrow(
			() -> new NotFoundException(ExceptionKeys.ATTACHMENT_GENERAL_ERROR_KEY, "Unable to assign attachment %s to final destination.".formatted(attachmentId)));
		attachmentHeaderService.assignAttachment(attachmentHeader, attachmentReference);
	}
}
