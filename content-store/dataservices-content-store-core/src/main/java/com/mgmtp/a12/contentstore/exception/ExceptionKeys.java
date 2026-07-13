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
package com.mgmtp.a12.contentstore.exception;

import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * All localization keys for exception messages of the content store are defined here.
 */
@OnlyForUsage public interface ExceptionKeys {

	/**
	 * Prefix for all content store localization keys used in error messages.
	 */
	String CONTENT_STORE_ERROR_KEY_PREFIX = "error.content-store.";

	/**
	 * Key for I/O related content store errors.
	 */
	String CONTENT_IO_ERROR_KEY = CONTENT_STORE_ERROR_KEY_PREFIX + "io";

	/**
	 * Key for the error indicating that content was not found.
	 */
	String CONTENT_NOT_FOUND_ERROR_KEY = CONTENT_STORE_ERROR_KEY_PREFIX + "content.notFound";

	/**
	 * Key for the error indicating an invalid content size.
	 */
	String INVALID_CONTENT_SIZE_ERROR_KEY = CONTENT_STORE_ERROR_KEY_PREFIX + "content.invalidSize";

	/**
	 * Key for the error indicating an invalid content location.
	 */
	String INVALID_CONTENT_LOCATION_ERROR_KEY = CONTENT_STORE_ERROR_KEY_PREFIX + "content.invalidLocation";

	/**
	 * Key for the error indicating invalid input parameters.
	 */
	String INVALID_INPUT_ERROR_KEY = CONTENT_STORE_ERROR_KEY_PREFIX + "input.invalid";

	/**
	 * Key for the error indicating an invalid persistent type (public/private).
	 */
	String INVALID_PERSISTENT_TYPE_ERROR_KEY = CONTENT_STORE_ERROR_KEY_PREFIX + "persistentType.invalid";

	/**
	 * Key for unexpected content store errors.
	 */
	String UNEXPECTED_ERROR_KEY = CONTENT_STORE_ERROR_KEY_PREFIX + "unexpected";

	/**
	 * Key for the error indicating that a download ticket is unavailable or expired.
	 */
	String TICKET_UNAVAILABLE_ERROR_KEY = CONTENT_STORE_ERROR_KEY_PREFIX + "ticket.unavailable";

}
