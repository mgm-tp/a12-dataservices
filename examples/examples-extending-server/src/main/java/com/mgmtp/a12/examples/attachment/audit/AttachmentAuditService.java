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
package com.mgmtp.a12.examples.attachment.audit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.attachment.events.AttachmentBeforeCreateEvent;
import com.mgmtp.a12.examples.extra.ExtraEntity;
import com.mgmtp.a12.examples.extra.ExtraEntityRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Example on how to use the {@link AttachmentBeforeCreateEvent} to store additional information when an attachment is created.
 * In this case we store the attachment id and filename in an extra table.
 *
 */
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.attachments.audit", name = "enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
@Component public class AttachmentAuditService {

	private final ExtraEntityRepository extraEntityRepository;

	/**
	 * Persists additional audit information when an attachment is created.
	 * Stores the attachment ID and file name in an extra table via {@link ExtraEntityRepository}.
	 *
	 * @param event event containing the attachment metadata; never null.
	 */
	@EventListener
	public void attachmentBeforeCreateListener(AttachmentBeforeCreateEvent event) {
		extraEntityRepository.save(ExtraEntity.builder()
			.id(event.getAttachment().getHeader().getAttachmentId())
			.text(event.getAttachment().getHeader().getFilename())
			.build());
	}

}
