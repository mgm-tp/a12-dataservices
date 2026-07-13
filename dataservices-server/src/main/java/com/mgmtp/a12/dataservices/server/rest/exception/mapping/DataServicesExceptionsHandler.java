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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.mgmtp.a12.contentstore.exception.TicketNotFoundException;
import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.common.exception.InvalidSizeException;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.common.exception.mapping.BaseExceptionMapper;
import com.mgmtp.a12.dataservices.common.exception.mapping.GenericThrowableMapper;
import com.mgmtp.a12.dataservices.common.exception.mapping.InvalidInputExceptionMapper;
import com.mgmtp.a12.dataservices.common.exception.mapping.NotFoundExceptionMapper;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.document.exception.DocumentImportException;
import com.mgmtp.a12.dataservices.exception.ContentStoreClientException;
import com.mgmtp.a12.dataservices.exception.CorruptedDataException;
import com.mgmtp.a12.dataservices.exception.FunctionalityDisabledException;
import com.mgmtp.a12.dataservices.exception.IntegrityException;
import com.mgmtp.a12.dataservices.exception.RequestIdConflictException;
import com.mgmtp.a12.dataservices.exception.SecurityException;
import com.mgmtp.a12.dataservices.exception.query.QueryException;
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.exception.query.QueryNotFoundException;
import com.mgmtp.a12.dataservices.model.exception.SerializationException;
import com.mgmtp.a12.dataservices.rpc.internal.JsonRpcOperationDispatcher.PreviousOperationFailedException;
import com.mgmtp.a12.dataservices.server.rest.exception.internal.mapping.ContentStoreClientExceptionMapper;

import lombok.NonNull;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.exc.MismatchedInputException;

import static com.mgmtp.a12.dataservices.common.exception.mapping.GenericThrowableMapper.EXCEPTION_KEY;

/**
 * Spring `@ControllerAdvice` that handles exceptions for REST controllers and provides structured error responses.
 * Key features:
 *
 * * Maps exceptions to HTTP status codes and response entities
 * * Uses `AnonymityException.getAnonymityMessage()` for privacy-safe logging
 * * Delegates to specialized mapper classes for type-specific handling
 * 
 * @see AnonymityException for privacy protection guidelines
 */
@ControllerAdvice(basePackages = { DataServicesCoreProperties.DS_PACKAGE_PREFIX })
public class DataServicesExceptionsHandler extends ResponseEntityExceptionHandler {

	/**
	 * Handles access-denied scenarios and maps them to a forbidden response.
	 *
	 * @param ex the {@link AccessDeniedException} thrown by Spring Security; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} containing the error payload and HTTP 403 status.
	 */
	@ExceptionHandler(value = { AccessDeniedException.class })
	public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
		return handle(new AccessDeniedExceptionMapper(), ex, request);
	}

	/**
	 * Handles {@link BaseException} instances and delegates mapping to {@link BaseExceptionMapper}.
	 *
	 * @param ex a domain {@link BaseException}; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} configured according to the exception details.
	 */
	@ExceptionHandler(value = { BaseException.class })
	public ResponseEntity<Object> handleBaseServicesException(BaseException ex, WebRequest request) {
		return handle(new BaseExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles document import failures.
	 *
	 * @param ex a {@link DocumentImportException} describing import issues; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with a warning-level operation error payload.
	 */
	@ExceptionHandler(value = { DocumentImportException.class })
	public ResponseEntity<Object> handleDocumentImportException(DocumentImportException ex, WebRequest request) {
		return handle(new DocumentImportExceptionMapper(), ex, request);
	}

	/**
	 * Handles failures of previously executed operations in a batch/RPC context.
	 *
	 * @param ex a {@link PreviousOperationFailedException} signaling a prior failure; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} representing the mapped error.
	 */
	@ExceptionHandler(value = { PreviousOperationFailedException.class })
	public ResponseEntity<Object> handlePreviousOperationFailedException(PreviousOperationFailedException ex, WebRequest request) {
		return handle(new PreviousOperationFailedExceptionMapper(), ex, request);
	}

	/**
	 * Handles any uncaught exception with a generic mapper.
	 *
	 * @param ex the thrown {@link Exception}; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with a generic error representation.
	 */
	@ExceptionHandler(value = { Exception.class })
	public ResponseEntity<Object> handleGenericException(Exception ex, WebRequest request) {
		return handle(new GenericThrowableMapper<>(), ex, request);
	}

	/**
	 * Handles integrity violations (e.g., unique constraints).
	 *
	 * @param ex an {@link IntegrityException}; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with conflict status.
	 */
	@ExceptionHandler(value = { IntegrityException.class })
	protected ResponseEntity<Object> handleIntegrityException(IntegrityException ex, WebRequest request) {
		return handle(new IntegrityExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles request ID conflicts.
	 *
	 * @param ex a {@link RequestIdConflictException}; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} indicating a conflict.
	 */
	@ExceptionHandler(value = { RequestIdConflictException.class })
	protected ResponseEntity<Object> handleRequestIdConflictException(RequestIdConflictException ex, WebRequest request) {
		return handle(new RequestIdConflictExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles domain-specific invalid input errors.
	 *
	 * @param ex an {@link InvalidInputException}; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with BAD_REQUEST status.
	 */
	@ExceptionHandler(value = { InvalidInputException.class })
	protected ResponseEntity<Object> handleInvalidInputException(InvalidInputException ex, WebRequest request) {
		return handle(new InvalidInputExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles old Jackson v2 JSON mapping failures when deserializing request payloads.
	 *
	 * @param ex a {@link DatabindException}; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with BAD_REQUEST status and localized message.
	 */
	@ExceptionHandler(value = { DatabindException.class })
	protected ResponseEntity<Object> handleJsonMappingException(DatabindException ex, WebRequest request) {
		return handle(new JsonMappingExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles JSON mapping failures when deserializing request payloads.
	 *
	 * @param ex a {@link JacksonException}; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with BAD_REQUEST status and localized message.
	 */
	@ExceptionHandler(value = { JacksonException.class })
	protected ResponseEntity<Object> handleJsonMappingException(JacksonException ex, WebRequest request) {
		return handle(new JacksonMappingExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles JSON input type mismatches for request payloads.
	 *
	 * @param ex a {@link MismatchedInputException}; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with BAD_REQUEST status and localized message.
	 */
	@ExceptionHandler(value = { MismatchedInputException.class })
	protected ResponseEntity<Object> handleMismatchedInputException(MismatchedInputException ex, WebRequest request) {
		return handle(new MismatchedInputExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles domain not-found exceptions.
	 *
	 * @param ex a {@link NotFoundException}; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with NOT_FOUND status.
	 */
	@ExceptionHandler(value = { NotFoundException.class })
	protected ResponseEntity<Object> handleNotFoundException(NotFoundException ex, WebRequest request) {
		return handle(new NotFoundExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles security-related violations.
	 *
	 * @param ex a {@link SecurityException}; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with FORBIDDEN status.
	 */
	@ExceptionHandler(value = { SecurityException.class })
	protected ResponseEntity<Object> handleSecurityException(SecurityException ex, WebRequest request) {
		return handle(new SecurityExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles serialization errors for model content.
	 *
	 * @param ex a {@link SerializationException}; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with BAD_REQUEST status where appropriate.
	 */
	@ExceptionHandler(value = { SerializationException.class })
	protected ResponseEntity<Object> handleSerializationException(SerializationException ex, WebRequest request) {
		return handle(new SerializationExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles cases where functionality is intentionally disabled.
	 *
	 * @param ex a {@link FunctionalityDisabledException}; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with FORBIDDEN status.
	 */
	@ExceptionHandler(value = { FunctionalityDisabledException.class })
	protected ResponseEntity<Object> handleFunctionalityDisabledException(FunctionalityDisabledException ex, WebRequest request) {
		return handle(new FunctionalityDisabledExceptionMapper(), ex, request);
	}

	/**
	 * Handles data corruption scenarios.
	 *
	 * @param ex a {@link CorruptedDataException}; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with INTERNAL_SERVER_ERROR status.
	 */
	@ExceptionHandler(value = { CorruptedDataException.class })
	protected ResponseEntity<Object> handleCorruptedDataException(CorruptedDataException ex, WebRequest request) {
		return handle(new CorruptedDataExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles missing ticket errors originating from the content store.
	 *
	 * @param ex a {@link TicketNotFoundException}; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with NOT_FOUND status.
	 */
	@ExceptionHandler(value = { TicketNotFoundException.class })
	protected ResponseEntity<Object> handleTicketNotFoundException(TicketNotFoundException ex, WebRequest request) {
		return handle(new TicketNotFoundExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles invalid size violations for uploads or payloads.
	 *
	 * @param ex an {@link InvalidSizeException}; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with BAD_REQUEST status.
	 */
	@ExceptionHandler(value = { InvalidSizeException.class })
	protected ResponseEntity<Object> handleInvalidSizeException(InvalidSizeException ex, WebRequest request) {
		return handle(new InvalidSizeExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles exceptions returned by the content store client.
	 *
	 * @param ex a {@link ContentStoreClientException}; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with a mapped client error.
	 */
	@ExceptionHandler(value = { ContentStoreClientException.class })
	protected ResponseEntity<Object> handleContentStoreClientException(ContentStoreClientException ex, WebRequest request) {
		return handle(new ContentStoreClientExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles invalid input for queries.
	 *
	 * @param ex a {@link QueryInvalidInputException}; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with BAD_REQUEST status.
	 */
	@ExceptionHandler(value = { QueryInvalidInputException.class })
	public ResponseEntity<Object> handleQueryInvalidInputException(QueryInvalidInputException ex, WebRequest request) {
		return handle(new QueryInvalidInputExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles not-found errors for queries (e.g., missing resources).
	 *
	 * @param ex a {@link QueryNotFoundException}; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} with NOT_FOUND status.
	 */
	@ExceptionHandler(value = { QueryNotFoundException.class })
	public ResponseEntity<Object> handleQueryNotFoundException(QueryNotFoundException ex, WebRequest request) {
		return handle(new QueryNotFoundExceptionMapper<>(), ex, request);
	}

	/**
	 * Handles general query-related exceptions.
	 *
	 * @param ex a {@link QueryException}; never null.
	 * @param request the current web request context; never null.
	 * @return a {@link ResponseEntity} mapped via {@link QueryExceptionMapper}.
	 */
	@ExceptionHandler(value = { QueryException.class })
	public ResponseEntity<Object> handleQueryException(QueryException ex, WebRequest request) {
		return handle(new QueryExceptionMapper<>(), ex, request);
	}

	/**
	 * Maps empty or unreadable request bodies to a BAD_REQUEST response with a localized message.
	 */
	@Override protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException ex, @NonNull HttpHeaders headers,
		@NonNull HttpStatusCode status, @NonNull WebRequest request) {
		return handle(new HttpMessageNotReadableExceptionMapper(), ex, request);
	}

	/**
	 * Central exception processing method that delegates to mappers.
	 *
	 * Coordinates exception-to-response transformation:
	 *
	 * * Calls `mapper.log(ex)` for anonymized logging when `AnonymityException` is implemented
	 * * Adds exception headers when configured  
	 * * Builds final `ResponseEntity` with mapper-provided content and HTTP status
	 * 
	 * @param <E> the exception type handled by the mapper
	 * @param mapper the mapper for exception-to-response transformation; never null
	 * @param ex the thrown exception to process; never null
	 * @param request the current web request context; never null
	 * @return `ResponseEntity` with headers, body, and HTTP status from the mapper
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
