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
package com.mgmtp.a12.dataservices.authorization.internal;

import org.springframework.security.access.AccessDeniedException;

import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.authorization.AttachmentPermissionEvaluator;
import com.mgmtp.a12.dataservices.authorization.AuthConstants;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class DefaultAttachmentPermissionEvaluator  implements AttachmentPermissionEvaluator  {

	private final AuthorizationService authorizationService;

	/**
	 * Check if current user has Attachment Upload permission.
	 * @param header the attachment header.
	 * @throws AccessDeniedException if user does not have Attachment Upload permission.
	 */
	public void checkUploadPermission(AttachmentHeader header) {
		log.debug("Check attachment upload permission for attachment [{}]", header.getAttachmentId());
		if (authorizationService.checkPermissions(header, AuthConstants.ATTACHMENT_UPLOAD_PERMISSION).isNotPassed()) {
			throw new AccessDeniedException("Resource not safe for upload");
		}
	}
}
