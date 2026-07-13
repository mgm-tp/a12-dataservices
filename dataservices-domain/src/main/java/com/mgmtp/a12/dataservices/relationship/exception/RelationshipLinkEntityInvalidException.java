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

import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;

/**
 * This exception and its descendants represent errors when the link entity is invalid.
 */
public class RelationshipLinkEntityInvalidException extends RelationshipValidationException {

	/**
	 * Creates an exception indicating at least one link entity lacks a valid role.
	 * The error is initialized with code {@link ExceptionCodes#RELATIONSHIP_LINK_ENTITY_INVALID_EXCEPTION_CODE}
	 * and key {@link ExceptionKeys#RELATIONSHIP_LINK_VALIDATION_ERROR_KEY}.
	 */
	public RelationshipLinkEntityInvalidException() {
		super(new RelationshipValidationException.Builder(ExceptionCodes.RELATIONSHIP_LINK_ENTITY_INVALID_EXCEPTION_CODE,
			ExceptionKeys.RELATIONSHIP_LINK_VALIDATION_ERROR_KEY, "Invalid Link Entity Role", "One of the entities has no valid role!"));
	}

	/**
	 * Creates an exception for an invalid link entity document reference in a given role.
	 *
	 * @param role the role name for which the document reference is invalid; never null.
	 */
	public RelationshipLinkEntityInvalidException(String role) {
		super(new RelationshipValidationException.Builder(ExceptionCodes.RELATIONSHIP_LINK_ENTITY_INVALID_EXCEPTION_CODE,
			ExceptionKeys.RELATIONSHIP_LINK_VALIDATION_ERROR_KEY, "Invalid Link Entity DocRef",
			"Role [%s] has invalid docRef".formatted(role)));
	}

	private RelationshipLinkEntityInvalidException(Builder builder) {
		super(new RelationshipValidationException.Builder(builder.code, builder.key, builder.message, builder.title)
			.exception(builder.exception));
	}

	/**
	 * Builder for {@link RelationshipLinkEntityInvalidException}.
	 * Provides a fluent API to set optional context like the underlying exception.
	 */
	protected static class Builder {
		private final int code;
		private final String key;
		private final String message;
		private final String title;
		private  Exception exception;

		/**
		 * Creates a new builder.
		 *
		 * @param code the application-specific error code.
		 * @param key the error key used for localization.
		 * @param message the detailed message explaining the validation issue.
		 * @param title a short title for the error, typically used in UI.
		 */
		public Builder(int code, String key, String message, String title) {
			this.code = code;
			this.key = key;
			this.message = message;
			this.title = title;
		}

		/**
		 * Sets a cause exception to be attached to the built {@link RelationshipLinkEntityInvalidException}.
		 *
		 * @param exception the underlying cause; may be null if none is available.
		 * @return this builder for method chaining.
		 */
		public Builder exception(Exception exception) {
			this.exception = exception;
			return this;
		}

		/**
		 * Builds a new {@link RelationshipLinkEntityInvalidException} instance using the configured properties.
		 *
		 * @return a configured RelationshipLinkEntityInvalidException.
		 */
		public RelationshipLinkEntityInvalidException build() {
			return new RelationshipLinkEntityInvalidException(this);
		}
	}
}
