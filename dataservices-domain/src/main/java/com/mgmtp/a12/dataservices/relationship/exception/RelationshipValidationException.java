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
package com.mgmtp.a12.dataservices.relationship.exception;

import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;

import lombok.Getter;

/**
 * This exception and its descendants represent errors when validating relationship link.
 */
public class RelationshipValidationException extends BaseException {

	/**
	 * Title used for validation messages when the link model is missing.
	 * This value is used in UI contexts to present a concise error headline.
	 */
	public static final String MISSING_LINK_MODEL_WARN = "Missing Link Model";

	@Getter private final String keyPrefix;
	@Getter private final String title;

	/**
	 * Creates a validation exception for relationship link processing.
	 *
	 * @param keyPrefix the error key used for localization, typically a prefix grouping related errors.
	 * @param title a short title for the error, typically used in UI.
	 * @param message a detailed message describing the validation problem.
	 */
	public RelationshipValidationException(String keyPrefix, String title, String message) {
		super(ExceptionCodes.RELATIONSHIP_VALIDATION_EXCEPTION_CODE, keyPrefix, message);
		this.keyPrefix = keyPrefix;
		this.title = title;
	}

	/**
	 * Creates a validation exception using the provided builder configuration.
	 *
	 * @param builder the builder carrying error code, key, title, message, and optional cause.
	 */
	public RelationshipValidationException(Builder builder) {
		super(builder.code, builder.key, builder.message, builder.exception);
		this.keyPrefix = builder.key;
		this.title = builder.title;
	}

	/**
	 * Builder for {@link RelationshipValidationException}.
	 */
	public static class Builder {
		int code;
		String key;
		String message;
		Exception exception;
		String title;

		/**
		 * Creates a new builder.
		 *
		 * @param code the application-specific error code.
		 * @param key the error key used for localization.
		 * @param title a short title for the error, typically used in UI.
		 * @param message a detailed message describing the validation problem.
		 */
		public Builder(int code, String key, String title, String message) {
			this.code = code;
			this.key = key;
			this.message = message;
			this.title = title;
		}

		/**
		 * Sets a cause exception to be attached to the built {@link RelationshipValidationException}.
		 *
		 * @param exception the underlying cause; may be null if none is available.
		 * @return this builder for method chaining.
		 */
		public Builder exception(Exception exception) {
			this.exception = exception;
			return this;
		}

		/**
		 * Builds a new {@link RelationshipValidationException} instance using the configured properties.
		 *
		 * @return a configured RelationshipValidationException.
		 */
		public RelationshipValidationException build() {
			return new RelationshipValidationException(this);
		}
	}
}
