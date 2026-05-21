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
package com.mgmtp.a12.contentstore.server.rest.exception.mapping;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.common.exception.InvalidSizeException;
import com.mgmtp.a12.contentstore.exception.InvalidUuidFormatException;
import com.mgmtp.a12.contentstore.exception.TicketNotFoundException;
import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.common.exception.mapping.BaseExceptionMapper;
import com.mgmtp.a12.dataservices.common.exception.mapping.GenericThrowableMapper;
import com.mgmtp.a12.dataservices.common.exception.mapping.InvalidInputExceptionMapper;
import com.mgmtp.a12.dataservices.common.exception.mapping.InvalidSizeExceptionMapper;
import com.mgmtp.a12.dataservices.common.exception.mapping.NotFoundExceptionMapper;
import com.mgmtp.a12.dataservices.common.exception.mapping.UnexpectedExceptionMapper;

import static com.mgmtp.a12.dataservices.common.exception.mapping.GenericThrowableMapper.EXCEPTION_KEY;

/**
 * Controller advice for Content Store server.
 */
@ControllerAdvice(basePackages = { "com.mgmtp.a12.contentstore" })
public class ContentStoreExceptionsHandler extends ResponseEntityExceptionHandler {

	/**
	 * Handles {@link BaseException} instances raised within Content Store controllers.
	 *
	 * @param ex the thrown {@link BaseException}.
	 * @param request the current web request context; never null.
	 * @return a JSON {@link ResponseEntity} mapped according to the base exception contract.
	 */
	@ExceptionHandler(value = { BaseException.class })
	public ResponseEntity<Object> handleBaseServicesException(BaseException ex, WebRequest request) {
		return handle(new BaseExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles resource-related {@link NotFoundException}s.
	 *
	 * @param ex the thrown {@link NotFoundException}.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with HTTP 404 status and structured error payload.
	 */
	@ExceptionHandler(value = { NotFoundException.class })
	protected ResponseEntity<Object> handleNotFoundException(NotFoundException ex, WebRequest request) {
		return handle(new NotFoundExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles {@link TicketNotFoundException} thrown when a download ticket does not exist.
	 *
	 * @param ex the thrown {@link TicketNotFoundException}.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with HTTP 404 status and error details for the missing ticket.
	 */
	@ExceptionHandler(value = { TicketNotFoundException.class })
	protected ResponseEntity<Object> handleTicketNotFoundException(TicketNotFoundException ex, WebRequest request) {
		return handle(new TicketNotFoundExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles {@link InvalidUuidFormatException} when identifiers do not conform to UUID format.
	 *
	 * @param ex the thrown {@link InvalidUuidFormatException}.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with HTTP 400 status describing the invalid identifier.
	 */
	@ExceptionHandler(value = { InvalidUuidFormatException.class })
	protected ResponseEntity<Object> handleInvalidUuidFormatException(InvalidUuidFormatException ex, WebRequest request) {
		return handle(new InvalidUuidFormatExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles {@link InvalidSizeException} for invalid payload or content size constraints.
	 *
	 * @param ex the thrown {@link InvalidSizeException}.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with HTTP 400 status indicating the size violation.
	 */
	@ExceptionHandler(value = { InvalidSizeException.class })
	protected ResponseEntity<Object> handleInvalidSizeException(InvalidSizeException ex, WebRequest request) {
		return handle(new InvalidSizeExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles {@link InvalidInputException} when request parameters or payload are malformed.
	 *
	 * @param ex the thrown {@link InvalidInputException}.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with HTTP 400 status and structured validation error details.
	 */
	@ExceptionHandler(value = { InvalidInputException.class })
	protected ResponseEntity<Object> handleInvalidInputException(InvalidInputException ex, WebRequest request) {
		return handle(new InvalidInputExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles unexpected runtime errors wrapped by {@link UnexpectedException}.
	 *
	 * @param ex the thrown {@link UnexpectedException}.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with HTTP 500 status and generalized error payload.
	 */
	@ExceptionHandler(value = { UnexpectedException.class })
	protected ResponseEntity<Object> handleUnexpectedException(UnexpectedException ex, WebRequest request) {
		return handle(new UnexpectedExceptionMapper<>(), ex, request);
	}

	/**
	 * Maps an exception using the provided {@link GenericThrowableMapper} and builds a response.
	 *
	 * @param <E> the type of {@link Exception} being handled.
	 * @param mapper the mapper responsible for logging and building the response payload; never null.
	 * @param ex the exception to handle; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with headers and status determined by the mapper.
	 */
	protected <E extends Exception> ResponseEntity<Object> handle(GenericThrowableMapper<E> mapper, E ex, WebRequest request) {
		mapper.log(ex);
		HttpHeaders headers = new HttpHeaders();
		if (mapper.isShouldAddExceptionToHeader()) {
			headers.add(EXCEPTION_KEY, mapper.getExceptionKey(ex));
		}
		headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		return handleExceptionInternal(ex, mapper.getEntity(ex), headers, mapper.getHttpStatus(ex), request);
	}
}

