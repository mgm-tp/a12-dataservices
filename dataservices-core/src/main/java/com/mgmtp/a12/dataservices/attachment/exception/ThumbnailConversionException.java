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
package com.mgmtp.a12.dataservices.attachment.exception;

import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;

/**
 * Indicates that an error occurs while converting an {@link java.io.InputStream} to a {@link com.mgmtp.a12.dataservices.attachment.DataServicesThumbnail}.
 *
 * The exception wraps the original cause and carries a localized message via {@link BaseException}.
 */
public class ThumbnailConversionException extends BaseException {

	/**
	 * Creates a new exception indicating thumbnail conversion failed.
	 *
	 * @param key A localization key for the error message; must not be null.
	 * @param message A non-localized English fallback message describing the failure; may be null.
	 * @param priority The display priority of the message; must not be null.
	 * @param t The underlying cause of the failure; may be null.
	 */
	public ThumbnailConversionException(String key, String message, BaseException.MessagePriority priority, Throwable t) {
		super(ExceptionCodes.THUMBNAIL_CONVERSION_EXCEPTION_CODE, key, message, priority, t);
	}
}
