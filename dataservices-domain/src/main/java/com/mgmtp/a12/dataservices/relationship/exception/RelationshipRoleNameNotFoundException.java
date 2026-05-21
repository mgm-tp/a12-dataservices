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
 * This exception and its descendants represent errors when relationship role is not found.
 */
public class RelationshipRoleNameNotFoundException extends RelationshipValidationException {

	@Getter private final String relationshipModelName;
	@Getter private final String roleName;

	/**
	 * Creates an exception indicating the requested role is not defined in the relationship model.
	 *
	 * @param relationshipModelName the name of the relationship model.
	 * @param roleName the missing role name within the relationship model.
	 */
	public RelationshipRoleNameNotFoundException(String relationshipModelName, String roleName) {
		super(new RelationshipValidationException.Builder(ExceptionCodes.RELATIONSHIP_ROLE_NAME_NOT_FOUND_EXCEPTION_CODE,
			ExceptionKeys.RELATIONSHIP_LINK_ROLE_MISSING_ERROR_KEY, "Missing Role in Model",
			String.format("Requested role [%s] has not been found in the model [%s].", roleName, relationshipModelName)));
		this.relationshipModelName = relationshipModelName;
		this.roleName = roleName;
	}

	private RelationshipRoleNameNotFoundException(Builder builder) {
		super(new RelationshipValidationException.Builder(builder.code, builder.key, builder.message, builder.title)
			.exception(builder.exception));
		this.relationshipModelName = builder.relationshipModelName;
		this.roleName = builder.roleName;
	}

	/**
	 * Builder for {@link RelationshipRoleNameNotFoundException}.
	 */
	protected static class Builder {
		private final int code;
		private final String key;
		private final String message;
		private final String title;
		private final String relationshipModelName;
		private final String roleName;
		private  Exception exception;

		/**
		 * Creates a new builder.
		 *
		 * @param code the application-specific error code.
		 * @param key the error key used for localization.
		 * @param message the detailed message explaining the validation issue.
		 * @param relationshipModelName the relationship model name.
		 * @param roleName the missing role name.
		 * @param title a short title for the error, typically used in UI.
		 */
		public Builder(int code, String key, String message, String relationshipModelName, String roleName, String title) {
			this.code = code;
			this.key = key;
			this.message = message;
			this.relationshipModelName = relationshipModelName;
			this.roleName = roleName;
			this.title = title;
		}

		/**
		 * Sets a cause exception to be attached to the built {@link RelationshipRoleNameNotFoundException}.
		 *
		 * @param exception the underlying cause; may be null if none is available.
		 * @return this builder for method chaining.
		 */
		public Builder exception(Exception exception) {
			this.exception = exception;
			return this;
		}

		/**
		 * Builds a new {@link RelationshipRoleNameNotFoundException} instance using the configured properties.
		 *
		 * @return a configured RelationshipRoleNameNotFoundException.
		 */
		public RelationshipRoleNameNotFoundException build() {
			return new RelationshipRoleNameNotFoundException(this);
		}
	}
}
