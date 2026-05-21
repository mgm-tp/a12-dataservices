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
package com.mgmtp.a12.dataservices.common.exception.mapping;

import org.springframework.http.HttpStatus;

import com.mgmtp.a12.dataservices.common.exception.InvalidSizeException;

/**
 * Maps {@link InvalidSizeException} and its subtypes to  response.
 *
 * @param <E> Type of exception to handle.
 */
public class InvalidSizeExceptionMapper<E extends InvalidSizeException> extends BaseExceptionMapper<E> {

	/**
	 * {@inheritDoc}
	 *
	 * Maps invalid size problems to {@link HttpStatus#BAD_REQUEST}.
	 *
	 * @param exception the exception instance.
	 * @return the HTTP status for invalid size errors.
	 */
	@Override public HttpStatus getHttpStatus(E exception) {
		return HttpStatus.BAD_REQUEST;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Size validation errors are expected and do not require stack traces in logs.
	 *
	 * @param exception the exception instance.
	 * @return `false` to suppress stack trace logging.
	 */
	@Override public boolean shouldLogStackTrace(E exception) {
		return false;
	}
}
