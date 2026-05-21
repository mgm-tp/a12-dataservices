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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.relationship.RelationshipRole;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;

import lombok.Getter;

/**
 * This exception and its descendants represent errors when the document of relationship role not found.
 */
public class RelationshipRoleMismatchException extends RelationshipValidationException {

	@Getter private final String linkId;
	@Getter private final transient Map<String, ? extends RelationshipRole> roles;
	@Getter private final RelationshipRoleSpec role;

	/**
	 * Creates an exception indicating that requested roles do not match the expected roles for a link.
	 *
	 * @param linkId the identifier of the link under validation.
	 * @param expectedRole the expected roles map keyed by role name.
	 * @param requestRole the requested role specifications received from the client.
	 * @param role the specific role that triggered the mismatch message composition.
	 */
	public RelationshipRoleMismatchException(String linkId, Map<String, ? extends RelationshipRole> expectedRole, List<RelationshipRoleSpec> requestRole,
		RelationshipRoleSpec role) {
		super(new RelationshipValidationException.Builder(ExceptionCodes.RELATIONSHIP_ROLE_MISMATCH_EXCEPTION_CODE,
			String.format(ExceptionKeys.RELATIONSHIP_VALIDATION_BAD_ENTITY_ERROR_KEY, role.getRole()), "Wrong Entity",
			String.format("Requested link [%s] has role:docRef %s, but expected is %s", linkId, requestRole.stream()
				.map(e -> String.format("[%s:%s]", e.getRole(), e.getDocRef()))
				.sorted()
				.collect(Collectors.joining(" and ")), expectedRole.values().stream()
				.map(e -> String.format("[%s:%s]", e.getName(), e.getDocRef()))
				.sorted()
				.collect(Collectors.joining(" and ")))));
		this.linkId = linkId;
		this.roles = expectedRole;
		this.role = role;
	}

	private RelationshipRoleMismatchException(Builder builder) {
		super(new RelationshipValidationException.Builder(builder.code, builder.key, builder.message, builder.title)
			.exception(builder.exception));
		this.linkId = builder.linkId;
		this.roles = builder.roles;
		this.role = builder.role;
	}

	/**
	 * Builder for {@link RelationshipRoleMismatchException}.
	 */
	protected static class Builder {
		private final int code;
		private final String key;
		private final String message;
		private final String title;
		private final String linkId;
		private final Map<String, ? extends RelationshipRole> roles;
		private final RelationshipRoleSpec role;
		private  Exception exception;

		/**
		 * Creates a new builder.
		 *
		 * @param code the application-specific error code.
		 * @param key the error key used for localization.
		 * @param message the detailed message explaining the validation issue.
		 * @param linkId the identifier of the link under validation.
		 * @param roles the expected roles map keyed by role name.
		 * @param role the specific role that triggered the mismatch message composition.
		 * @param title a short title for the error, typically used in UI.
		 */
		public Builder(int code, String key, String message, String linkId, Map<String, ? extends RelationshipRole> roles, RelationshipRoleSpec role, String title) {
			this.code = code;
			this.key = key;
			this.message = message;
			this.linkId = linkId;
			this.roles = roles;
			this.role = role;
			this.title = title;
		}

		/**
		 * Sets a cause exception to be attached to the built {@link RelationshipRoleMismatchException}.
		 *
		 * @param exception the underlying cause; may be null if none is available.
		 * @return this builder for method chaining.
		 */
		public Builder exception(Exception exception) {
			this.exception = exception;
			return this;
		}

		/**
		 * Builds a new {@link RelationshipRoleMismatchException} instance using the configured properties.
		 *
		 * @return a configured RelationshipRoleMismatchException.
		 */
		public RelationshipRoleMismatchException build() {
			return new RelationshipRoleMismatchException(this);
		}
	}
}
