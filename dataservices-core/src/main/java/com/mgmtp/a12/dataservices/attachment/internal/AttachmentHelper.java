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

import com.mgmtp.a12.dataservices.attachment.AttachmentAnnotation;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.TypeOfTheContent;
import com.mgmtp.a12.dataservices.authorization.internal.UaaConnector;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AttachmentHelper {
	public static AttachmentHeader prepareAttachmentHeader(String filename, List<AttachmentAnnotation> annotations) {
		return prepareAttachmentHeader(UUID.randomUUID().toString(), filename, annotations);
	}

	public static AttachmentHeader prepareAttachmentHeader(String attachmentId, String filename, List<AttachmentAnnotation> annotations) {
		Instant now = Instant.now();
		String username = Optional.ofNullable(UaaConnector.getCurrentUserName())
				.orElseThrow(() -> new UnexpectedException("Missing authorization context"));
		return AttachmentHeader.builder()
				.attachmentId(attachmentId)
				.annotations(annotations)
				.createdAt(now)
				.createdBy(username)
				.modifiedAt(now)
				.modifiedBy(username)
				.filename(filename)
				.build();
	}

	public static TypeOfTheContent classifyAttachmentType(String documentModelName, DataServicesCoreProperties dataServicesCoreProperties) {
		return dataServicesCoreProperties.getAttachments().getType().getPublicType().getModels().contains(documentModelName) ?
			TypeOfTheContent.ATTACHMENT_PUBLIC : TypeOfTheContent.ATTACHMENT_SECURED;
	}
}
