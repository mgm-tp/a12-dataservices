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

import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.common.exception.BaseError;
import com.mgmtp.a12.dataservices.common.exception.ErrorDetail;
import com.mgmtp.a12.dataservices.common.exception.mapping.GenericThrowableMapper;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindException;

/**
 * Mapper provides default error messages for the failed deserialization from JSON to the objects. These exceptions are thrown when there is either corrupted
 * JSON on the REST layer or completely different input is passed to the REST endpoint.
 */
public class JacksonMappingExceptionMapper<E extends JacksonException> extends GenericThrowableMapper<E> {

	/**
	 * Localization key representing a generic invalid request.
	 */
	public static final String BAD_REQUEST_KEY = "error.invalid.request";
	/**
	 * Default English message shown when the request is not valid.
	 */
	public static final String BAD_REQUEST_DEFAULT = "The request received is not a valid request";

	/**
	 * {@inheritDoc}
	 *
	 * Builds a localized {@link BaseError} payload describing the invalid request.
	 *
	 * @param exception the thrown {@link DatabindException}; never null.
	 */
	@Override public Object getEntity(E exception) {
		return new BaseError() {
			@Override public LocalizedEntry getShortMessage() {
				return new LocalizedEntry(BAD_REQUEST_KEY, BAD_REQUEST_DEFAULT);
			}

			@Override public LocalizedEntry getLongMessage() {
				return new LocalizedEntry(BAD_REQUEST_KEY, BAD_REQUEST_DEFAULT);
			}

			@Override public ErrorDetail getErrorDetail() {
				return null;
			}
		};
	}

	/**
	 * {@inheritDoc}
	 *
	 * Suppresses stack trace logging for invalid request payloads.
	 *
	 * @param exception the thrown {@link DatabindException}; never null.
	 * @return `false` to keep logs clean for client errors.
	 */
	@Override public boolean shouldLogStackTrace(E exception) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Maps JSON mapping failures to {@link HttpStatus#BAD_REQUEST}.
	 *
	 * @param exception the thrown {@link DatabindException}; never null.
	 * @return client error status indicating invalid request content.
	 */
	@Override public HttpStatus getHttpStatus(E exception) {
		return HttpStatus.BAD_REQUEST;
	}

}
