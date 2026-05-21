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
package com.mgmtp.a12.contentstore.client.exception;

import java.io.IOException;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.contentstore.client.localization.LocalizedEntry;

import lombok.extern.slf4j.Slf4j;

/**
 * Convert exceptions from server into client exceptions. Read server-side exception message from header +exception+.
 */
@Slf4j
public class ContentStoreErrorHandler extends DefaultResponseErrorHandler {

	private static final String HEADER_EXCEPTION = "exception";

	private final ObjectMapper objectMapper;

	/**
	 * Creates a {@link ContentStoreErrorHandler} that maps HTTP errors to typed client exceptions.
	 *
	 * @param objectMapper Object mapper used to parse server error payloads; must not be null.
	 */
	public ContentStoreErrorHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Maps HTTP status codes to typed client exceptions and extracts optional localized messages from the response body.
	 *
	 * @param response HTTP response to analyze.
	 * @throws IOException if reading the response fails.
	 */
	@Override public void handleError(ClientHttpResponse response) throws IOException {
		HttpStatus statusCode = (HttpStatus) response.getStatusCode();
		String body = new String(getResponseBody(response));
		LocalizedEntry shortMessage = null;
		LocalizedEntry longMessage = null;
		ErrorLevel errorLevel = null;
		try {
			ServerErrorException clientException = objectMapper.readValue(body, ServerErrorException.class);
			shortMessage = clientException.getShortMessage();
			longMessage = clientException.getLongMessage();
			errorLevel = clientException.getLevel();

		} catch (IOException e) {
			log.info("Can not read error response from server");
		}

		switch (statusCode) {
		case NOT_FOUND:
			throw new MissingDataException(
				getExceptionHeader(response, "Data not found"),
				makeErrorDetail(statusCode, body),
				longMessage,
				shortMessage,
				errorLevel
			);
		case FORBIDDEN:
			throw new MissingAccessRightException(
				getExceptionHeader(response, "Forbidden"),
				makeErrorDetail(statusCode, body),
				longMessage,
				shortMessage,
				errorLevel
			);
		case BAD_REQUEST:
			throw new BadRequestException(
				getExceptionHeader(response, "BadRequest"),
				makeErrorDetail(statusCode, body),
				longMessage,
				shortMessage,
				errorLevel
			);
		case UNAUTHORIZED:
			throw new UnauthorizedException(
				getExceptionHeader(response, "Unauthorized"),
				makeErrorDetail(statusCode, body),
				longMessage,
				shortMessage,
				errorLevel
			);
		default:
			throw new UnexpectedException(
				getExceptionHeader(response, "UnexpectedException"),
				makeErrorDetail(statusCode, body),
				longMessage,
				shortMessage,
				errorLevel
			);
		}
	}

	private static String getExceptionHeader(ClientHttpResponse response, String fallback) {
		return Optional.ofNullable(response.getHeaders().getFirst(HEADER_EXCEPTION))
			.orElse(fallback);
	}

	private static RestErrorDetail makeErrorDetail(HttpStatusCode statusCode, String body) {
		RestErrorDetail restErrorDetail = new RestErrorDetail(statusCode.value(), body);
		log.debug("Request failed: {}", restErrorDetail);
		return restErrorDetail;
	}

}
