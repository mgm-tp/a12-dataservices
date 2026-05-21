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
package com.mgmtp.a12.examples.attachment.mime;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.examples.attachment.AttachmentContentValidator;
import com.mgmtp.a12.examples.util.MediaTypeUtils;

import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ATTACHMENT_INVALID_TYPE_ERROR_KEY;

/**
 * Mime Type Validator class.
 *
 */
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.attachments.mime-types.custom", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Order(value = Ordered.HIGHEST_PRECEDENCE)
@Component public class MimeTypeValidator implements AttachmentContentValidator {
	private final MediaTypeUtils mediaTypeUtils;

	/**
	 * Validates the MIME type of the provided content bytes and rejects download streams.
	 * Throws an {@link com.mgmtp.a12.dataservices.common.exception.InvalidInputException} if the type is invalid.
	 *
	 * @param bytes raw content to inspect; must not be null.
	 */
	@Override public void validate(byte[] bytes) {
		if (mediaTypeUtils.mediaTypeEquals(bytes, MediaTypeUtils.APPLICATION_DOWNLOAD)) {
			throw new InvalidInputException(ATTACHMENT_INVALID_TYPE_ERROR_KEY, "MimeType is not valid");
		}
	}
}
