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
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;

import lombok.Getter;

/**
 * This exception and its descendants represent errors when the document of relationship role not found.
 */
public class RelationshipRoleDocumentNotFoundException extends RelationshipValidationException {

	@Getter private final transient String relationshipModelName;
	@Getter private final transient RelationshipRoleSpec role;

	/**
	 * Creates an exception indicating the document of the given role is missing for the relationship model.
	 *
	 * @param relationshipModelName the relationship model name.
	 * @param role the role specification whose document reference is missing.
	 */
	public RelationshipRoleDocumentNotFoundException(String relationshipModelName, RelationshipRoleSpec role) {
		super(new RelationshipValidationException.Builder(ExceptionCodes.RELATIONSHIP_ROLE_DOCUMENT_NOT_FOUND_EXCEPTION_CODE,
			ExceptionKeys.RELATIONSHIP_LINK_ADD_DOCUMENT_NOT_FOUND_ERROR_KEY, "Missing Link Documents",
			String.format("Requested document for link %s in role %s is missing: %s", relationshipModelName, role.getRole(), role.getDocRef())));
		this.relationshipModelName = relationshipModelName;
		this.role = role;
	}

	private RelationshipRoleDocumentNotFoundException(Builder builder) {
		super(new RelationshipValidationException.Builder(builder.code, builder.key, builder.message, builder.title)
			.exception(builder.exception));
		this.relationshipModelName = builder.relationshipModelName;
		this.role = builder.role;
	}

	/**
	 * Builder for {@link RelationshipRoleDocumentNotFoundException}.
	 */
	protected static class Builder {
		private final int code;
		private final String key;
		private final String message;
		private final String title;
		private final String relationshipModelName;
		private final RelationshipRoleSpec role;
		private  Exception exception;

		/**
		 * Creates a new builder.
		 *
		 * @param code the application-specific error code.
		 * @param key the error key used for localization.
		 * @param message the detailed message explaining the validation issue.
		 * @param relationshipModelName the relationship model name.
		 * @param role the role specification whose document is missing.
		 * @param title a short title for the error, typically used in UI.
		 */
		public Builder(int code, String key, String message, String relationshipModelName, RelationshipRoleSpec role, String title) {
			this.code = code;
			this.key = key;
			this.message = message;
			this.relationshipModelName = relationshipModelName;
			this.role = role;
			this.title = title;
		}

		/**
		 * Sets a cause exception to be attached to the built {@link RelationshipRoleDocumentNotFoundException}.
		 *
		 * @param exception the underlying cause; may be null if none is available.
		 * @return this builder for method chaining.
		 */
		public Builder exception(Exception exception) {
			this.exception = exception;
			return this;
		}

		/**
		 * Builds a new {@link RelationshipRoleDocumentNotFoundException} instance using the configured properties.
		 *
		 * @return a configured RelationshipRoleDocumentNotFoundException.
		 */
		public RelationshipRoleDocumentNotFoundException build() {
			return new RelationshipRoleDocumentNotFoundException(this);
		}
	}
}
