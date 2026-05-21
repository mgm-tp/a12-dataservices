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
package com.mgmtp.a12.dataservices.exception.query;

import com.mgmtp.a12.dataservices.exception.ExceptionKeys;

import static com.mgmtp.a12.dataservices.exception.ExceptionCodes.QUERY_INVALID_INPUT_ERROR_CODE;

/**
 * Indicates that a query cannot be executed due to invalid user input (structure or values).
 * Use for client-side validation failures detected by the server.
 */
public class QueryInvalidInputException extends QueryException {

	/**
	 * Creates a QueryInvalidInputException with a message.
	 *
	 * @param executionPhase phase of query processing when the error is detected.
	 * @param message human-readable description; may be null.
	 */
	public QueryInvalidInputException(ExceptionKeys.ExecutionPhase executionPhase, String message) {
		super(executionPhase, QUERY_INVALID_INPUT_ERROR_CODE, message);
	}

	/**
	 * Creates a QueryInvalidInputException with a localization key and message.
	 *
	 * @param executionPhase phase of query processing when the error is detected.
	 * @param key localization key that identifies the error.
	 * @param message human-readable description; may be null.
	 */
	public QueryInvalidInputException(ExceptionKeys.ExecutionPhase executionPhase, String key, String message) {
		super(executionPhase, QUERY_INVALID_INPUT_ERROR_CODE, key, message);
	}

	/**
	 * Creates a QueryInvalidInputException with key, message and cause.
	 *
	 * @param executionPhase phase of query processing when the error is detected.
	 * @param key localization key that identifies the error.
	 * @param message human-readable description; may be null.
	 * @param cause underlying cause; may be null.
	 */
	public QueryInvalidInputException(ExceptionKeys.ExecutionPhase executionPhase, String key, String message, Throwable cause) {
		super(executionPhase, QUERY_INVALID_INPUT_ERROR_CODE, key, message, cause);
	}

	/**
	 * Creates a QueryInvalidInputException with key, priority and cause.
	 *
	 * @param executionPhase phase of query processing when the error is detected.
	 * @param key localization key that identifies the error.
	 * @param message human-readable description; may be null.
	 * @param priority presentation priority for clients.
	 * @param cause underlying cause; may be null.
	 */
	public QueryInvalidInputException(ExceptionKeys.ExecutionPhase executionPhase, String key, String message, MessagePriority priority,
		Throwable cause) {
		super(executionPhase, QUERY_INVALID_INPUT_ERROR_CODE, key, message, priority, cause);
	}

	/**
	 * Creates a QueryInvalidInputException with explicit code and message.
	 *
	 * @param executionPhase phase of query processing when the error is detected.
	 * @param code numeric error code to expose.
	 * @param message human-readable description; may be null.
	 */
	public QueryInvalidInputException(ExceptionKeys.ExecutionPhase executionPhase, int code, String message) {
		super(executionPhase, code, message);
	}

	/**
	 * Creates a QueryInvalidInputException with explicit code, message and cause.
	 *
	 * @param executionPhase phase of query processing when the error is detected.
	 * @param code numeric error code to expose.
	 * @param message human-readable description; may be null.
	 * @param cause underlying cause; may be null.
	 */
	public QueryInvalidInputException(ExceptionKeys.ExecutionPhase executionPhase, int code, String message, Throwable cause) {
		super(executionPhase, code, message, cause);
	}

	/**
	 * Creates a QueryInvalidInputException with explicit code, key and message.
	 *
	 * @param executionPhase phase of query processing when the error is detected.
	 * @param code numeric error code to expose.
	 * @param key localization key that identifies the error.
	 * @param message human-readable description; may be null.
	 */
	public QueryInvalidInputException(ExceptionKeys.ExecutionPhase executionPhase, int code, String key, String message) {
		super(executionPhase, code, key, message);
	}

	/**
	 * Creates a QueryInvalidInputException with explicit code, key, message and cause.
	 *
	 * @param executionPhase phase of query processing when the error is detected.
	 * @param code numeric error code to expose.
	 * @param key localization key that identifies the error.
	 * @param message human-readable description; may be null.
	 * @param cause underlying cause; may be null.
	 */
	public QueryInvalidInputException(ExceptionKeys.ExecutionPhase executionPhase, int code, String key, String message, Throwable cause) {
		super(executionPhase, code, key, message, cause);
	}

	/**
	 * Creates a QueryInvalidInputException with explicit code, key, message, priority and cause.
	 *
	 * @param executionPhase phase of query processing when the error is detected.
	 * @param code numeric error code to expose.
	 * @param key localization key that identifies the error.
	 * @param message human-readable description; may be null.
	 * @param priority presentation priority for clients.
	 * @param cause underlying cause; may be null.
	 */
	public QueryInvalidInputException(ExceptionKeys.ExecutionPhase executionPhase, int code, String key, String message, MessagePriority priority,
		Throwable cause) {
		super(executionPhase, code, key, message, priority, cause);
	}
}

