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

import java.util.Collection;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;

import lombok.Getter;

/**
 * This exception and its descendants represent errors when the link document model and link document is mismatched.
 */
public class RelationshipLinkDocumentModelMismatchException extends RelationshipValidationException {

	@Getter private final String relationshipModelName;
	@Getter private final DocumentReference docRef;
	@Getter private final Collection<String> allowedModels;

	/**
	 * Creates an exception indicating a mismatch between the link document model and the actual document reference.
	 *
	 * @param relationshipModelName the name of the relationship model under validation.
	 * @param docRef the {@link DocumentReference} that does not match the expected model(s).
	 * @param allowedModels the set of allowed document model names for the relationship.
	 */
	public RelationshipLinkDocumentModelMismatchException(String relationshipModelName, DocumentReference docRef, Collection<String> allowedModels) {
		super(new RelationshipValidationException.Builder(ExceptionCodes.RELATIONSHIP_LINK_DOCUMENT_MODEL_MISMATCH_EXCEPTION_CODE,
			ExceptionKeys.RELATIONSHIP_LINK_ADD_DOCUMENT_BAD_MODEL_ERROR_KEY, "Bad Document Model",
			"Document [%s] should have been defined for models [%s], found [%s] instead".formatted(docRef, String.join(",", allowedModels),
				docRef.getDocumentModelName())));
		this.relationshipModelName = relationshipModelName;
		this.docRef = docRef;
		this.allowedModels = allowedModels;
	}

	private RelationshipLinkDocumentModelMismatchException(Builder builder) {
		super(new RelationshipValidationException.Builder(builder.code, builder.key, builder.message, builder.title)
			.exception(builder.exception));
		this.relationshipModelName = builder.relationshipModelName;
		this.docRef = builder.docRef;
		this.allowedModels = builder.allowedModels;
	}

	/**
	 * Builder for {@link RelationshipLinkDocumentModelMismatchException}.
	 */
	protected static class Builder {
		private final int code;
		private final String key;
		private final String message;
		private final String title;
		private final String relationshipModelName;
		private final DocumentReference docRef;
		private final Collection<String> allowedModels;
		private  Exception exception;

		/**
		 * Creates a new builder.
		 *
		 * @param code the application-specific error code.
		 * @param key the error key used for localization.
		 * @param message the detailed message explaining the validation issue.
		 * @param relationshipModelName the relationship model name.
		 * @param docRef the offending document reference.
		 * @param allowedModels the allowed document model names.
		 * @param title a short title for the error, typically used in UI.
		 */
		public Builder(int code, String key, String message, String relationshipModelName, DocumentReference docRef, Collection<String> allowedModels, String title) {
			this.code = code;
			this.key = key;
			this.message = message;
			this.relationshipModelName = relationshipModelName;
			this.docRef = docRef;
			this.allowedModels = allowedModels;
			this.title = title;
		}

		/**
		 * Sets a cause exception to be attached to the built {@link RelationshipLinkDocumentModelMismatchException}.
		 *
		 * @param exception the underlying cause; may be null if none is available.
		 * @return this builder for method chaining.
		 */
		public Builder exception(Exception exception) {
			this.exception = exception;
			return this;
		}

		/**
		 * Builds a new {@link RelationshipLinkDocumentModelMismatchException} instance using the configured properties.
		 *
		 * @return a configured RelationshipLinkDocumentModelMismatchException.
		 */
		public RelationshipLinkDocumentModelMismatchException build() {
			return new RelationshipLinkDocumentModelMismatchException(this);
		}
	}
}
