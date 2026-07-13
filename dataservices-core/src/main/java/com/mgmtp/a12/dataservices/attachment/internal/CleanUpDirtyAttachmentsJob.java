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

import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.DirtyAttachment;
import com.mgmtp.a12.dataservices.attachment.IDirtyAttachmentCleanupCondition;
import com.mgmtp.a12.dataservices.attachment.header.AttachmentHeaderService;
import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class CleanUpDirtyAttachmentsJob implements Job {

	private final DirtyAttachmentService dirtyAttachmentService;
	private final DefaultAttachmentService defaultAttachmentService;
	private final AttachmentHeaderService attachmentHeaderService;
	private final Optional<IDirtyAttachmentCleanupCondition> attachmentCleanupCondition;
	private final DataServicesCoreProperties dataServicesCoreProperties;

	@Transactional
	@Override public void execute(JobExecutionContext context) throws JobExecutionException {
		log.debug("Delete dirty attachments job triggered");
		dirtyAttachmentService.getDirtyAttachmentsForDeletion()
			.map(DirtyAttachment::getAttachmentId)
			.map(attachmentHeaderService::load)
			.flatMap(Optional::stream)
			.filter(this::checkRulesForDeletion)
			.forEach(this::deleteDirtyAttachment);
	}

	private void deleteDirtyAttachment(AttachmentHeader header) {
		DirtyAttachment dirtyAttachment = dirtyAttachmentService.beforeProcessing(header.getAttachmentId());
		try {
			defaultAttachmentService.delete(header);
			dirtyAttachmentService.removeFromDirtyList(header);
			log.info("Physically deleted attachment with id = {}", header.getAttachmentId());
		} catch (BaseException e) {
			if (e.isRecoverable()) {
				if (dirtyAttachment.getExecCount() > dataServicesCoreProperties.getAttachments().getCleanup().getRetry().getMax()) {
					log.error("Maximum retry-count reached for dirty attachment %s. Last error is %s".formatted(header.getAttachmentId(), e.getMessage()),
						e);
					dirtyAttachmentService.removeFromDirtyList(header);
				} else {
					log.warn("Recoverable error during processing of dirty attachment {}. Will retry in {}. The error is {}",
						header.getAttachmentId(), dataServicesCoreProperties.getAttachments().getCleanup().getRetry().getDelay(), e.getMessage());
				}
			} else {
				log.error(
					"Unrecoverable error during processing of dirty attachment %s. The error is %s".formatted(header.getAttachmentId(), e.getMessage()),
					e);
				dirtyAttachmentService.removeFromDirtyList(header);
			}
		} catch (Exception e) {
			log.error("Unknown error during processing of dirty attachment %s. The error is %s".formatted(header.getAttachmentId(), e.getMessage()),
				e);
			dirtyAttachmentService.removeFromDirtyList(header);
		}
	}

	private boolean checkRulesForDeletion(AttachmentHeader header) {
		if (attachmentCouldBeDeleted(header) && noReferencesToTheAttachment(header)) {
			return true;
		} else {
			dirtyAttachmentService.removeFromDirtyList(header);
			return false;
		}
	}

	private static boolean noReferencesToTheAttachment(AttachmentHeader header) {
		return CollectionUtils.isEmpty(header.getReferences());
	}

	@NonNull private Boolean attachmentCouldBeDeleted(AttachmentHeader header) {
		return attachmentCleanupCondition
			.map(c -> c.canBeDeleted(header))
			.orElse(true);
	}

}
