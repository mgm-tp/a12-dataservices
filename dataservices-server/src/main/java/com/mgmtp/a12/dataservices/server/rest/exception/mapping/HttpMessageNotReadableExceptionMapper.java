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
package com.mgmtp.a12.dataservices.server.rest.exception.mapping;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;

import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.common.exception.BaseError;
import com.mgmtp.a12.dataservices.common.exception.ErrorDetail;
import com.mgmtp.a12.dataservices.common.exception.mapping.GenericThrowableMapper;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;

/**
 * Mapper provides default error messages when the HTTP message could not be read.
 */
public class HttpMessageNotReadableExceptionMapper extends GenericThrowableMapper<HttpMessageNotReadableException> {

	/**
	 * Default English message used when something went wrong in reading the HTTP request.
	 */
	public static final String HTTP_MESSAGE_NOT_READABLE_DEFAULT = "The HTTP message could not be processed. Possibly the request body is empty or malformed.";

	/**
	 * {@inheritDoc}
	 *
	 * Builds a localized {@link BaseError} for empty or unreadable request bodies.
	 *
	 * @param exception the thrown {@link HttpMessageNotReadableException}; never null.
	 */
	@Override public Object getEntity(HttpMessageNotReadableException exception) {
		return new BaseError() {
			@Override public LocalizedEntry getShortMessage() {
				return new LocalizedEntry(ExceptionKeys.HTTP_MESSAGE_NOT_READABLE_ERROR_KEY, HTTP_MESSAGE_NOT_READABLE_DEFAULT);
			}

			@Override public LocalizedEntry getLongMessage() {
				return new LocalizedEntry(ExceptionKeys.HTTP_MESSAGE_NOT_READABLE_ERROR_KEY, HTTP_MESSAGE_NOT_READABLE_DEFAULT);
			}

			@Override public ErrorDetail getErrorDetail() {
				return null;
			}
		};
	}
	/**
	 * {@inheritDoc}
	 *
	 * Maps empty/unreadable bodies to {@link HttpStatus#BAD_REQUEST}.
	 *
	 * @param exception the thrown {@link HttpMessageNotReadableException}; never null.
	 * @return client error status indicating an invalid upload or body.
	 */
	@Override public HttpStatus getHttpStatus(HttpMessageNotReadableException exception) {
		return HttpStatus.BAD_REQUEST;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Suppresses stack trace logging for expected client input errors.
	 *
	 * @param exception the thrown {@link HttpMessageNotReadableException}; never null.
	 * @return `false` to keep logs concise.
	 */
	@Override public boolean shouldLogStackTrace(HttpMessageNotReadableException exception) {
		return false;
	}
}
