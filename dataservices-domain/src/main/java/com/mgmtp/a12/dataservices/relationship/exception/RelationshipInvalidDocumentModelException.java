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

import lombok.Getter;

/**
 * This exception and its descendants represent errors when there are invalid model names in relationship models or in existing links.
 *
 * @deprecated Not used anymore, will be removed in the next breaking release.
 */
@Deprecated(since = "38.1.0", forRemoval = true)
public class RelationshipInvalidDocumentModelException extends RelationshipValidationException {

	@Getter private final String changelog;

	/**
	 * Creates a deprecated exception indicating invalid model names encountered in relationships.
	 *
	 * @param message the detailed message describing invalid model names.
	 * @param changelog a changelog or hint describing detected model names; may help remediation.
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	public RelationshipInvalidDocumentModelException(String message, String changelog) {
		super(new RelationshipValidationException.Builder(ExceptionCodes.RELATIONSHIP_INVALID_DOCUMENT_MODEL_EXCEPTION_CODE,
			ExceptionKeys.RELATIONSHIP_INVALID_MODEL_NAME_ERROR_KEY, "Invalid model name(s) in relationships", message));
		this.changelog = changelog;
	}

	private RelationshipInvalidDocumentModelException(Builder builder) {
		super(new RelationshipValidationException.Builder(builder.code, builder.key, builder.message, builder.title)
			.exception(builder.exception));
		this.changelog = builder.changelog;
	}

	/**
	 * Builder for {@link RelationshipInvalidDocumentModelException}.
	 */
	protected static class Builder {
		private final int code;
		private final String key;
		private final String message;
		private final String title;
		private final String changelog;
		private  Exception exception;

		/**
		 * Creates a new builder.
		 *
		 * @param code the application-specific error code.
		 * @param key the error key used for localization.
		 * @param message the detailed message explaining the validation issue.
		 * @param title a short title for the error, typically used in UI.
		 * @param changelog a changelog or hint describing detected model names.
		 */
		public Builder(int code, String key, String message, String title, String changelog) {
			this.code = code;
			this.key = key;
			this.message = message;
			this.title = title;
			this.changelog = changelog;
		}

		/**
		 * Sets a cause exception to be attached to the built {@link RelationshipInvalidDocumentModelException}.
		 *
		 * @param exception the underlying cause; may be null if none is available.
		 * @return this builder for method chaining.
		 */
		public Builder exception(Exception exception) {
			this.exception = exception;
			return this;
		}

		/**
		 * Builds a new {@link RelationshipInvalidDocumentModelException} instance using the configured properties.
		 *
		 * @return a configured RelationshipInvalidDocumentModelException.
		 */
		public RelationshipInvalidDocumentModelException build() {
			return new RelationshipInvalidDocumentModelException(this);
		}
	}
}
