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

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;

import com.mgmtp.a12.connector.rest.ResponseErrorHandler;
import com.mgmtp.a12.contentstore.client.localization.LocalizedEntry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Convert exceptions from server into client exceptions. Read server-side exception message from header +exception+.
 */
@Slf4j @RequiredArgsConstructor
public class ContentStoreErrorHandler implements ResponseErrorHandler {

	private static final String HEADER_EXCEPTION = "exception";

	private final ObjectMapper objectMapper;

	@Override public void handleError(HttpRequest request, ClientHttpResponse response) throws IOException {

		HttpStatusCode statusCode = response.getStatusCode();
		String body = IOUtils.toString(response.getBody());

		ServerErrorException clientException = objectMapper.readValue(body, ServerErrorException.class);
		LocalizedEntry shortMessage = clientException.getShortMessage();
		LocalizedEntry longMessage = clientException.getLongMessage();
		ErrorLevel errorLevel = clientException.getLevel();

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
