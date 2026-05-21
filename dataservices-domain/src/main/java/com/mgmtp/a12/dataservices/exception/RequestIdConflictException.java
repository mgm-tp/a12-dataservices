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
package com.mgmtp.a12.dataservices.exception;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mgmtp.a12.dataservices.RequestIdState;
import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.common.exception.ErrorDetail;
import com.mgmtp.a12.dataservices.common.exception.ErrorLevel;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * Reports a conflict with a previously used request identifier (idempotency violation).
 * Carries the conflicting request id and its {@link RequestIdState}.
 */
@Data @EqualsAndHashCode(callSuper = true)
public class RequestIdConflictException extends BaseException {

	@JsonProperty private final String requestId;
	@JsonProperty private final RequestIdState state;

	/**
	 * Constructs the exception for deserialization with full details.
	 *
	 * @param code numeric error code.
	 * @param requestId the conflicting request identifier.
	 * @param state idempotency state of the request.
	 * @param longMessage optional localized long message; may be null.
	 * @param shortMessage optional localized short message; may be null.
	 * @param anonymityMessage anonymity-safe message; may be null.
	 * @param errorLevel severity level to present to clients; may be null.
	 * @param errorDetail structured error detail; may be null.
	 */
	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public RequestIdConflictException(int code, String requestId, RequestIdState state, LocalizedEntry longMessage,
		LocalizedEntry shortMessage, String anonymityMessage, ErrorLevel errorLevel, ErrorDetail errorDetail) {
		super(code);
		this.requestId = requestId;
		this.state = state;
		setLongMessage(longMessage);
		setShortMessage(shortMessage);
		setAnonymityMessage(anonymityMessage);
		if (errorLevel != null) {
			setErrorLevel(errorLevel);
		}
		setErrorDetail(errorDetail);
	}

	/**
	 * Creates an exception with default code and full details.
	 *
	 * @param requestId the conflicting request identifier.
	 * @param state idempotency state of the request.
	 * @param longMessage optional localized long message; may be null.
	 * @param shortMessage optional localized short message; may be null.
	 * @param anonymityMessage anonymity-safe message; may be null.
	 * @param errorLevel severity level to present to clients; may be null.
	 * @param errorDetail structured error detail; may be null.
	 */
	public RequestIdConflictException(String requestId, RequestIdState state, LocalizedEntry longMessage,
		LocalizedEntry shortMessage, String anonymityMessage, ErrorLevel errorLevel, ErrorDetail errorDetail) {
		super(ExceptionCodes.REQUEST_ID_CONFLICT_EXCEPTION_CODE);
		this.requestId = requestId;
		this.state = state;
		setLongMessage(longMessage);
		setShortMessage(shortMessage);
		setAnonymityMessage(anonymityMessage);
		if (errorLevel != null) {
			setErrorLevel(errorLevel);
		}
		setErrorDetail(errorDetail);
	}

	/**
	 * Creates an exception with a localization key and message.
	 *
	 * @param key localization key for the error.
	 * @param message human-readable description; may be null.
	 * @param requestId the conflicting request identifier.
	 * @param state idempotency state of the request.
	 */
	public RequestIdConflictException(String key, String message, String requestId, RequestIdState state) {
		super(ExceptionCodes.REQUEST_ID_CONFLICT_EXCEPTION_CODE, key, message);
		this.requestId = requestId;
		this.state = state;
	}

	/**
	 * Creates an exception with explicit code and cause.
	 *
	 * @param code numeric error code to expose.
	 * @param key localization key for the error.
	 * @param message human-readable description; may be null.
	 * @param e underlying cause; may be null.
	 * @param requestId the conflicting request identifier.
	 * @param state idempotency state of the request.
	 */
	protected RequestIdConflictException(int code, String key, String message, Throwable e, String requestId, RequestIdState state) {
		super(code, key, message, null, e);
		this.requestId = requestId;
		this.state = state;
	}

	/**
	 * Convenience constructor using the default error key.
	 *
	 * @param message human-readable description; may be null.
	 * @param requestId the conflicting request identifier.
	 * @param state idempotency state of the request.
	 */
	public RequestIdConflictException(String message, String requestId, RequestIdState state) {
		this(ExceptionKeys.REQUEST_IDEMPOTENCY_ERROR_KEY, message, requestId, state);
	}

	/**
	 * If no explicit message is present, returns a formatted anonymity-safe string with request id and state.
	 */
	@Override public String getMessage() {
		String message = super.getMessage();
		return message == null ? String.format("Request of ID %s is in state %s", getRequestId(), getState()) : message;
	}
}

