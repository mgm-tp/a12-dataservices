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

import java.util.Optional;

import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;

/**
 * Base type for all query-related exceptions.
 * Captures the {@link ExceptionKeys.ExecutionPhase} to qualify error messages and anonymity-safe text.
 */
public class QueryException extends BaseException {

	protected final ExceptionKeys.ExecutionPhase executionPhase;

	/**
	 * Creates a QueryException with explicit code and message.
	 *
	 * @param executionPhase phase of query processing when the error is detected.
	 * @param code numeric error code to expose.
	 * @param message human-readable description; may be null.
	 */
	public QueryException(ExceptionKeys.ExecutionPhase executionPhase, int code, String message) {
		super(code, message);
		this.executionPhase = executionPhase;
	}

	/**
	 * Creates a QueryException with explicit code, localization key and message.
	 *
	 * @param executionPhase phase of query processing when the error is detected.
	 * @param code numeric error code to expose.
	 * @param key localization key that identifies the error.
	 * @param message human-readable description; may be null.
	 */
	public QueryException(ExceptionKeys.ExecutionPhase executionPhase, int code, String key, String message) {
		super(code, key, message);
		this.executionPhase = executionPhase;
	}

	/**
	 * Creates a QueryException with explicit code, message and cause.
	 *
	 * @param executionPhase phase of query processing when the error is detected.
	 * @param code numeric error code to expose.
	 * @param message human-readable description; may be null.
	 * @param cause underlying cause; may be null.
	 */
	public QueryException(ExceptionKeys.ExecutionPhase executionPhase, int code, String message, Throwable cause) {
		super(code, message, cause);
		this.executionPhase = executionPhase;
	}

	/**
	 * Creates a QueryException with explicit code, key, message and cause.
	 *
	 * @param executionPhase phase of query processing when the error is detected.
	 * @param code numeric error code to expose.
	 * @param key localization key that identifies the error.
	 * @param message human-readable description; may be null.
	 * @param cause underlying cause; may be null.
	 */
	public QueryException(ExceptionKeys.ExecutionPhase executionPhase, int code, String key, String message, Throwable cause) {
		super(code, key, message, cause);
		this.executionPhase = executionPhase;
	}

	/**
	 * Creates a QueryException with explicit code, key, message, priority and cause.
	 *
	 * @param executionPhase phase of query processing when the error is detected.
	 * @param code numeric error code to expose.
	 * @param key localization key that identifies the error.
	 * @param message human-readable description; may be null.
	 * @param priority presentation priority for clients.
	 * @param cause underlying cause; may be null.
	 */
	public QueryException(ExceptionKeys.ExecutionPhase executionPhase, int code, String key, String message, MessagePriority priority, Throwable cause) {
		super(code, key, message, priority, cause);
		this.executionPhase = executionPhase;
	}

	/**
	 * Prefixes the anonymity-safe message with the query {@link ExceptionKeys.ExecutionPhase} if present.
	 * The {@link ExceptionKeys.ExecutionPhase} provides context about where in the query processing the error occurred,
	 * which may help for debugging and error analysis.
	 */
	@Override public String getAnonymityMessage() {
		return Optional.ofNullable(super.getAnonymityMessage())
			.map(this::formatMessageWithPhase)
			.orElse(null);
	}

	private String formatMessageWithPhase(String message) {
		return "%s:: %s".formatted(executionPhase, message);
	}
}

