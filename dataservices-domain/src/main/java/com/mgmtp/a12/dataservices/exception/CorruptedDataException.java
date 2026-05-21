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

import com.mgmtp.a12.dataservices.common.exception.BaseException;

/**
 * Represents errors caused by corrupted or unreadable data within the application.
 * Use when payloads or persisted content cannot be processed reliably.
 */
public class CorruptedDataException extends BaseException {

	/**
	 * Creates a CorruptedDataException with a localization key, message and cause.
	 *
	 * @param key localization key that identifies the error.
	 * @param message human-readable description; may be null.
	 * @param ex underlying cause; may be null.
	 */
	public CorruptedDataException(String key, String message, Exception ex) {
		super(ExceptionCodes.CORRUPTED_DATA_EXCEPTION_CODE, key, message, ex);
	}

	/**
	 * Creates a CorruptedDataException with a message.
	 *
	 * @param message human-readable description; may be null.
	 */
	public CorruptedDataException(String message) {
		super(ExceptionCodes.CORRUPTED_DATA_EXCEPTION_CODE, message);
	}

	/**
	 * Creates a CorruptedDataException with a localization key and message.
	 *
	 * @param key localization key that identifies the error.
	 * @param message human-readable description; may be null.
	 */
	public CorruptedDataException(String key, String message){
		super(ExceptionCodes.CORRUPTED_DATA_EXCEPTION_CODE, key, message);
	}

	/**
	 * Creates a CorruptedDataException with a message and cause.
	 *
	 * @param message human-readable description; may be null.
	 * @param ex underlying cause; may be null.
	 */
	public CorruptedDataException(String message, Exception ex) {
		super(ExceptionCodes.CORRUPTED_DATA_EXCEPTION_CODE, message, ex);
	}

	private CorruptedDataException(Builder builder) {
		super(builder.code, builder.key, builder.message, builder.exception);
	}

	/**
	 * Builder for {@link CorruptedDataException} instances.
	 * Provides a fluent way to set optional details before construction.
	 */
	protected static class Builder {
		int code;
		String key;
		String message;
		Exception exception;

		/**
		 * Initializes the builder with required details.
		 *
		 * @param code numeric error code to expose.
		 * @param key localization key that identifies the error.
		 * @param message human-readable description; may be null.
		 */
		public Builder(int code, String key, String message) {
			this.code = code;
			this.key = key;
			this.message = message;
		}

		/**
		 * Sets the underlying cause.
		 *
		 * @param exception root cause; may be null.
		 * @return this builder for chaining.
		 */
		public Builder exception(Exception exception) {
			this.exception = exception;
			return this;
		}

		/**
		 * Builds the configured {@link CorruptedDataException}.
		 *
		 * @return a new exception instance with the configured properties.
		 */
		public CorruptedDataException build() {
			return new CorruptedDataException(this);
		}
	}
}

