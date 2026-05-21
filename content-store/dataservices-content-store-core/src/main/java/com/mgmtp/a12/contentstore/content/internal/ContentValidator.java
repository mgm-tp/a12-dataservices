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
package com.mgmtp.a12.contentstore.content.internal;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.contentstore.configuration.ContentStoreProperties;
import com.mgmtp.a12.contentstore.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.common.exception.InvalidSizeException;
import com.mgmtp.a12.contentstore.utils.Constants;
import com.mgmtp.a12.dataservices.common.content.ContentTypeDetector;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.common.quantity.internal.QuantityParsers;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component public class ContentValidator {

	private final ContentTypeDetector contentTypeDetector;
	private final long limitSize;
	private final String limitSizeString;

	public ContentValidator(ContentTypeDetector contentTypeDetector, ContentStoreProperties contentStoreProperties) {
		this.contentTypeDetector = contentTypeDetector;
		limitSizeString = contentStoreProperties.getLimitSize();
		limitSize = QuantityParsers.parseDataQuantity(limitSizeString);
	}

	public long getSizeAndValidate(byte[] bytes) {
		try {
			long length = contentTypeDetector.getContentLength(bytes);
			if (length > limitSize) {
				throw new InvalidSizeException(ExceptionKeys.INVALID_CONTENT_SIZE_ERROR_KEY,
					Constants.CONTENT_SIZE_CANNOT_EXCEED_LIMIT_PATTERN.formatted(limitSizeString));
			} else {
				return length;
			}
		} catch (IOException e) {
			throw new UnexpectedException(ExceptionKeys.UNEXPECTED_ERROR_KEY, Constants.ERROR_WHILE_TRYING_TO_CHECK_INPUT_STREAM_SIZE);
		}
	}
}
