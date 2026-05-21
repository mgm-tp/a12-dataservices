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
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;

import lombok.Getter;

/**
 * This exception and its descendants represent errors when (de)serializing link document.
 *
 * @deprecated Not used anymore, will be removed in the next breaking release.
 */
@Deprecated(since = "38.1.0", forRemoval = true)
public class RelationshipLinkDocumentSerializationException extends RelationshipValidationException {

	@Getter private final String linkDocument;
	@Getter private final String linkDocumentModelName;
	@Getter private final transient IProblemReporter problems;

	/**
	 * Creates a deprecated exception indicating that a link document cannot be deserialized.
	 *
	 * @param linkDocument the serialized link document value.
	 * @param linkDocumentModelName the model name of the link document.
	 * @param problems a reporter containing details about deserialization problems. TODO A12S-6443: Clarify contract (uncertain behavior).
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	public RelationshipLinkDocumentSerializationException(String linkDocument, String linkDocumentModelName, IProblemReporter problems) {
		super(new RelationshipValidationException.Builder(ExceptionCodes.RELATIONSHIP_LINK_DOCUMENT_SERIALIZATION_EXCEPTION_CODE,
			ExceptionKeys.RELATIONSHIP_LINK_DOCUMENT_SERIALIZATION_ERROR_KEY, "Deserialization Error",
			String.format("Link document [%s] of model [%s] could not be de-serialized, because of [%s]", linkDocument, linkDocumentModelName, problems)));
		this.linkDocumentModelName = linkDocumentModelName;
		this.problems = problems;
		this.linkDocument = linkDocument;
	}

	private RelationshipLinkDocumentSerializationException(Builder builder) {
		super(new RelationshipValidationException.Builder(builder.code, builder.key, builder.message, builder.title)
			.exception(builder.exception));
		this.linkDocument = builder.linkDocument;
		this.linkDocumentModelName = builder.linkDocumentModelName;
		this.problems = builder.problems;
	}

	/**
	 * Builder for {@link RelationshipLinkDocumentSerializationException}.
	 */
	protected static class Builder {
		private final int code;
		private final String key;
		private final String message;
		private final String title;
		private final String linkDocument;
		private final String linkDocumentModelName;
		private final IProblemReporter problems;
		private  Exception exception;

		/**
		 * Creates a new builder.
		 *
		 * @param code the application-specific error code.
		 * @param key the error key used for localization.
		 * @param message the detailed message explaining the validation issue.
		 * @param linkDocument the serialized link document value.
		 * @param linkDocumentModelName the model name of the link document.
		 * @param problems a reporter containing details about deserialization problems.
		 * @param title a short title for the error, typically used in UI.
		 */
		public Builder(int code, String key, String message, String linkDocument, String linkDocumentModelName, IProblemReporter problems, String title) {
			this.code = code;
			this.key = key;
			this.message = message;
			this.linkDocument = linkDocument;
			this.linkDocumentModelName = linkDocumentModelName;
			this.problems = problems;
			this.title = title;
		}

		/**
		 * Sets a cause exception to be attached to the built {@link RelationshipLinkDocumentSerializationException}.
		 *
		 * @param exception the underlying cause; may be null if none is available.
		 * @return this builder for method chaining.
		 */
		public Builder exception(Exception exception) {
			this.exception = exception;
			return this;
		}

		/**
		 * Builds a new {@link RelationshipLinkDocumentSerializationException} instance using the configured properties.
		 *
		 * @return a configured RelationshipLinkDocumentSerializationException.
		 */
		public RelationshipLinkDocumentSerializationException build() {
			return new RelationshipLinkDocumentSerializationException(this);
		}
	}
}
