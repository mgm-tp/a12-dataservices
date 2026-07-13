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
package com.mgmtp.a12.dataservices.internal.service.importer;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.attachment.AttachmentAnnotation;
import com.mgmtp.a12.dataservices.attachment.internal.DefaultAttachmentService;
import com.mgmtp.a12.dataservices.internal.model.AttachmentMetadataInfo;
import com.mgmtp.a12.dataservices.internal.model.SmeWorkspaceMetadata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component public class AttachmentImporter {

	private final Optional<DefaultAttachmentService> defaultAttachmentService;

	/**
	 * Imports a single attachment from in-memory content.
	 *
	 * @param entityId the attachment entity ID
	 * @param content the raw attachment file content
	 * @param smeWorkspaceMetadata the workspace metadata containing attachment filename and annotations
	 */
	public void importAttachment(String entityId, byte[] content, SmeWorkspaceMetadata smeWorkspaceMetadata) {
		AttachmentMetadataInfo attachmentMetadataInfo = smeWorkspaceMetadata.getAttachments().get(entityId);
		if (attachmentMetadataInfo == null) {
			log.warn("No metadata entry found for attachment with entity ID '{}', skipping import", entityId);
			return;
		}
		String fileName = attachmentMetadataInfo.getFileName();
		List<AttachmentAnnotation> annotations = Optional.of(attachmentMetadataInfo)
			.map(AttachmentMetadataInfo::getAnnotations)
			.orElse(Collections.emptyList());
		defaultAttachmentService.ifPresentOrElse(
			service -> service.createSecuredAttachment(entityId, new ByteArrayInputStream(content), fileName, annotations),
			() -> log.warn("Attachments are disabled; skipping import of attachment with entity ID '{}'", entityId)
		);
	}

}
