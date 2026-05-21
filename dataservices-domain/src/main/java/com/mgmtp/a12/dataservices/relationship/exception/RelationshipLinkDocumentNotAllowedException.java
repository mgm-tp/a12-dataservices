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
 * This exception and its descendants represent errors when link document is set when link document model is not set.
 */
public class RelationshipLinkDocumentNotAllowedException extends RelationshipValidationException {

	@Getter private final String linkDocument;

	/**
	 * Creates an exception indicating a link document is provided although no link document model is set.
	 *
	 * @param linkDocument the provided link document identifier; never null.
	 */
	public RelationshipLinkDocumentNotAllowedException(String linkDocument) {
		super(new RelationshipValidationException.Builder(ExceptionCodes.RELATIONSHIP_LINK_DOCUMENT_NOT_ALLOWED_EXCEPTION_CODE,
			ExceptionKeys.RELATIONSHIP_LINK_VALIDATION_MODEL_MISSING_ERROR_KEY, MISSING_LINK_MODEL_WARN,
			String.format("Link document [%s] must be blank if link document model is not set.", linkDocument)));
		this.linkDocument = linkDocument;
	}

	private RelationshipLinkDocumentNotAllowedException(Builder builder) {
		super(new RelationshipValidationException.Builder(builder.code, builder.key, builder.message, builder.title)
			.exception(builder.exception));
		this.linkDocument = builder.linkDocumentModel;
	}

	/**
	 * Builder for {@link RelationshipLinkDocumentNotAllowedException}.
	 */
	protected static class Builder {
		private final int code;
		private final String key;
		private final String message;
		private final String title;
		private final String linkDocumentModel;
		private  Exception exception;

		/**
		 * Creates a new builder.
		 *
		 * @param code the application-specific error code.
		 * @param key the error key used for localization.
		 * @param message the detailed message explaining the validation issue.
		 * @param linkDocumentModel the link document value that caused the error.
		 * @param title a short title for the error, typically used in UI.
		 */
		public Builder(int code, String key, String message, String linkDocumentModel, String title) {
			this.code = code;
			this.key = key;
			this.message = message;
			this.linkDocumentModel = linkDocumentModel;
			this.title = title;
		}

		/**
		 * Sets a cause exception to be attached to the built {@link RelationshipLinkDocumentNotAllowedException}.
		 *
		 * @param exception the underlying cause; may be null if none is available.
		 * @return this builder for method chaining.
		 */
		public Builder exception(Exception exception) {
			this.exception = exception;
			return this;
		}

		/**
		 * Builds a new {@link RelationshipLinkDocumentNotAllowedException} instance using the configured properties.
		 *
		 * @return a configured RelationshipLinkDocumentNotAllowedException.
		 */
		public RelationshipLinkDocumentNotAllowedException build() {
			return new RelationshipLinkDocumentNotAllowedException(this);
		}
	}
}
