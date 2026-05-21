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
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.exception.RequestIdConflictException;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Convert exceptions from server into client exceptions. And read server side exception message from header `+exception+`
 */
@Slf4j
public class DataServicesErrorHandler extends DefaultResponseErrorHandler {

	@Autowired ObjectMapper objectMapper;
	private static final String HEADER_EXCEPTION = "exception";

	/**
	 * Maps HTTP error responses to Data Services client exceptions.
	 *
	 * Translates common HTTP statuses into domain-specific exceptions and attempts to parse 409 conflicts into {@link RequestIdConflictException}.
	 * Falls back to {@link DefaultResponseErrorHandler} for unhandled statuses.
	 *
	 * @param response the HTTP response containing the error; never `null`.
	 * @throws IOException if reading the response fails.
	 */
	@Override public void handleError(ClientHttpResponse response) throws IOException {
		HttpStatus statusCode = (HttpStatus) response.getStatusCode();
		switch (statusCode) {
		case NOT_FOUND:
			throw new MissingDataException(getExceptionHeader(response, "Data not found"), makeErrorDetail(statusCode, response));
		case FORBIDDEN:
			throw new MissingAccessRightException(getExceptionHeader(response, "Forbidden"), makeErrorDetail(statusCode, response));
		case BAD_REQUEST:
			throw new BadRequest(getExceptionHeader(response, "BadRequest"), makeErrorDetail(statusCode, response));
		case UNAUTHORIZED:
			throw new MissingAccessRightException(getExceptionHeader(response, "Unauthorized"), makeErrorDetail(statusCode, response));
		case CONFLICT:
			RestErrorDetail errorDetail = makeErrorDetail(statusCode, response);
			try {
				throw objectMapper.readValue(errorDetail.getResponse(), RequestIdConflictException.class);
			} catch (JsonProcessingException e) {
				throw new ConflictException(getExceptionHeader(response, "Conflict"), errorDetail);
			}
		default:
		}
		super.handleError(response);
	}

	private static String getExceptionHeader(@NonNull ClientHttpResponse response, String fallback) {
		return Optional.of(response)
			.map(HttpMessage::getHeaders)
			.map(h -> h.getFirst(HEADER_EXCEPTION))
			.orElse(fallback);
	}

	private RestErrorDetail makeErrorDetail(HttpStatusCode statusCode, ClientHttpResponse response) {
		RestErrorDetail restErrorDetail = new RestErrorDetail(statusCode.value(), new String(getResponseBody(response)));
		log.debug("Request failed: {}", restErrorDetail);
		return restErrorDetail;
	}
}
