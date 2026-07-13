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
package com.mgmtp.a12.dataservices.common.exception.mapping;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.common.exception.AnonymityException;
import com.mgmtp.a12.dataservices.common.exception.BaseError;
import com.mgmtp.a12.dataservices.common.exception.ErrorDetail;
import com.mgmtp.a12.dataservices.common.exception.ErrorLevel;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This is a base class for exception mapping.
 * Maps a runtime exception to a related response entity. The response delivers an error code corresponding to a log
 * error message.
 */
@Slf4j
@Getter @Setter
public class GenericThrowableMapper<E extends Throwable> {

	/**
	 * Header key used to store the exception descriptor in the response.
	 */
	public static final String EXCEPTION_KEY = "exception";

	// TODO A12S-4148: Rename to error.unknown in breaking release
	/**
	 * Error key used when no specific mapping exists.
	 */
	public static final String UNKNOWN_ERROR_KEY = "UNKNOWN";

	private boolean shouldAddExceptionToHeader = false;

	/**
	 * Resolves the HTTP status to use for the given exception.
	 *
	 * @param exception the exception instance to map.
	 * @return the HTTP status representing this exception; defaults to {@link HttpStatus#INTERNAL_SERVER_ERROR}.
	 */
	public HttpStatus getHttpStatus(E exception) {
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	/**
	 * Computes a header-safe identifier or message for the exception.
	 *
	 * @param exception the exception instance.
	 * @return a sanitized identifier/message suitable for HTTP headers.
	 */
	public String getExceptionKey(E exception) {
		return constructSafeExceptionHeaderMessage(getExceptionMessage(exception));
	}

	/**
	 * Provides the entity to be serialized as the response body for this exception.
	 *
	 * @param exception the exception instance to map.
	 * @return a {@link BaseError} describing the exception in a client-consumable form.
	 */
	public Object getEntity(E exception) {
		return new BaseError() {
			@Override public LocalizedEntry getShortMessage() {
				return new LocalizedEntry(UNKNOWN_ERROR_KEY, getExceptionMessage(exception));
			}

			@Override public LocalizedEntry getLongMessage() {
				return new LocalizedEntry(UNKNOWN_ERROR_KEY, getExceptionMessage(exception));
			}

			@Override public ErrorDetail getErrorDetail() {
				return null;
			}
		};
	}

	/**
	 * Indicates whether the full stack trace should be logged for the exception.
	 *
	 * @param exception the exception instance.
	 * @return `true` to log stack trace; `false` to log a concise message only.
	 */
	public boolean shouldLogStackTrace(E exception) {
		return true;
	}

	/**
	 * Logs the exception using the resolved {@link ErrorLevel} and stack trace policy.
	 *
	 * @param exception the exception to log.
	 */
	public void log(E exception) {
		String commonMessage = "[ERROR-ID: %s]".formatted(getErrorCode(exception));
		if (shouldLogStackTrace(exception)) {
			log(getErrorLevel(exception), commonMessage, exception);
		} else {
			log(getErrorLevel(exception), "[Exception: %s] : %s".formatted(exception.getClass().getCanonicalName(), getSecureMessage(exception)));
			log.trace(commonMessage, exception);
		}
	}

	/**
	 * Resolves the {@link ErrorLevel} used for logging the exception.
	 *
	 * @param exception the exception instance.
	 * @return	the specific level if the exception implements {@link BaseError}; otherwise {@link ErrorLevel#ERROR}.
	 */
	public ErrorLevel getErrorLevel(E exception) {
		if (exception instanceof BaseError error) {
			return error.getLevel();
		} else {
			return ErrorLevel.ERROR;
		}
	}

	/**
	 * Returns a message safe for logging and headers.
	 * If the exception is an {@link AnonymityException}, uses the anonymized message; otherwise the raw exception message.
	 *
	 * @param exception the exception instance.
	 * @return	a secure message string.
	 */
	public String getSecureMessage(E exception) {
		return exception instanceof AnonymityException ae ? ae.getAnonymityMessage() : getExceptionMessage(exception);
	}

	/**
	 * Sanitizes an exception message for use in HTTP headers.
	 * Replaces CR/LF sequences with spaces and truncates the result to 1100 characters.
	 *
	 * @param message raw exception message; must not be `null`.
	 * @return	a sanitized and truncated message suitable for headers.
	 */
	public String constructSafeExceptionHeaderMessage(String message) {
		return StringUtils.truncate(RegExUtils.replaceAll(message, "[\r\n]+", " "), 1100);
	}

	/**
	 * Resolves the error code for the exception.
	 * This default implementation delegates to {@link #getExceptionKey(Throwable)}.
	 *
	 * @param ex the exception instance.
	 * @return	the error code string.
	 */
	public String getErrorCode(E ex) {
		return getExceptionKey(ex);
	}

	/**
	 * Extracts the message to use for downstream processing.
	 *
	 * @param exception the exception instance.
	 * @return	the raw {@link Throwable#getMessage()} by default.
	 */
	protected String getExceptionMessage(E exception) {
		return exception.getMessage();
	}

	private void log(ErrorLevel level, String message) {
		if (level == ErrorLevel.WARN) {
			log.warn(message);
		} else {
			log.error(message);
		}
	}

	private void log(ErrorLevel level, String message, E exception) {
		if (level == ErrorLevel.WARN) {
			log.warn(message, exception);
		} else {
			log.error(message, exception);
		}
	}

}
