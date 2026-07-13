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
package com.mgmtp.a12.dataservices.client.exception;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpMessage;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;

import com.mgmtp.a12.connector.rest.ResponseErrorHandler;
import com.mgmtp.a12.dataservices.exception.RequestIdConflictException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Convert exceptions from server into client exceptions. And read server side exception message from header `+exception+`
 */
@RequiredArgsConstructor
@Slf4j
public class DataServicesErrorHandler implements ResponseErrorHandler {

	private final ObjectMapper objectMapper;
	private static final String HEADER_EXCEPTION = "exception";

	@Override public void handleError(HttpRequest httpRequest, ClientHttpResponse clientHttpResponse) throws IOException {
		@NonNull HttpStatusCode statusCode = clientHttpResponse.getStatusCode();
		switch (statusCode) {
		case NOT_FOUND:
			throw new MissingDataException(getExceptionHeader(clientHttpResponse, "Data not found"), makeErrorDetail(statusCode, clientHttpResponse));
		case FORBIDDEN:
			throw new MissingAccessRightException(getExceptionHeader(clientHttpResponse, "Forbidden"), makeErrorDetail(statusCode, clientHttpResponse));
		case BAD_REQUEST:
			throw new BadRequest(getExceptionHeader(clientHttpResponse, "BadRequest"), makeErrorDetail(statusCode, clientHttpResponse));
		case UNAUTHORIZED:
			throw new MissingAccessRightException(getExceptionHeader(clientHttpResponse, "Unauthorized"), makeErrorDetail(statusCode, clientHttpResponse));
		case CONFLICT:
			RestErrorDetail errorDetail = makeErrorDetail(statusCode, clientHttpResponse);
			try {
				throw objectMapper.readValue(errorDetail.getResponse(), RequestIdConflictException.class);
			} catch (JacksonException e) {
				throw new ConflictException(getExceptionHeader(clientHttpResponse, "Conflict"), errorDetail);
			}
		default:
			throw new GenericErrorException(
				getExceptionHeader(clientHttpResponse, "Server error"),
				makeErrorDetail(statusCode, clientHttpResponse));
		}
	}

	private static String getExceptionHeader(@NonNull ClientHttpResponse response, String fallback) {
		return Optional.of(response)
			.map(HttpMessage::getHeaders)
			.map(h -> h.getFirst(HEADER_EXCEPTION))
			.orElse(fallback);
	}

	private RestErrorDetail makeErrorDetail(HttpStatusCode statusCode, ClientHttpResponse response) throws IOException {
		InputStream body = response.getBody();
		String message = body == null
			? response.getStatusText()
			: IOUtils.toString(body, StandardCharsets.UTF_8);
		RestErrorDetail restErrorDetail = new RestErrorDetail(statusCode.value(), message);
		log.debug("Request failed: {}", restErrorDetail);
		return restErrorDetail;
	}
}
