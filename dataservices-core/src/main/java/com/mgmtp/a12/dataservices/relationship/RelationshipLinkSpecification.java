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
package com.mgmtp.a12.dataservices.relationship;

import com.mgmtp.a12.dataservices.document.DocumentReference;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Specification for a relationship link, including optional filters for source and target roles for loading purposes without the knowledge of the link id.
 */
@Data
@RequiredArgsConstructor
public class RelationshipLinkSpecification {
	@NonNull private final String relationshipModelName;

	private RelationshipRoleSpecification sourceFilter;
	private RelationshipRoleSpecification targetFilter;

	/**
	 * Starts building a specification for the given relationship model.
	 *
	 * @param modelName The relationship model identifier; must not be null.
	 * @return A new {@link Builder} preconfigured with the model name.
	 */
	public static Builder forRelationshipModel(String modelName) {
		return new Builder(modelName);
	}

	/**
	 * Filter specification identifying one side of a relationship by role and document reference.
	 *
	 * @param name Role name used on the corresponding side; must not be null.
	 * @param docRef Document reference of the participating entity; must not be null.
	 */
	public record RelationshipRoleSpecification(@NonNull String name, @NonNull DocumentReference docRef) {}

	/**
	 * Builder for {@link RelationshipLinkSpecification}.
	 *
	 * Provides fluent methods to set optional source and target filters.
	 */
	public static class Builder {

		private final RelationshipLinkSpecification specification;

		/**
		 * Creates a builder for the specified relationship model.
		 *
		 * @param relationshipModelName Relationship model identifier; must not be null.
		 */
		public Builder(@NonNull String relationshipModelName) {
			this.specification = new RelationshipLinkSpecification(relationshipModelName);
		}

		/**
		 * Adds a filter that constrains the source side by role and document reference.
		 *
		 * @param sourceRole Role name of the source side; must not be null.
		 * @param docRef Document reference of the source entity; must not be null.
		 * @return This builder for method chaining.
		 */
		public Builder withSourceFilter(@NonNull String sourceRole, @NonNull DocumentReference docRef) {
			this.specification.setSourceFilter(new RelationshipRoleSpecification(sourceRole, docRef));
			return this;
		}

		/**
		 * Adds a filter that constrains the target side by role and document reference.
		 *
		 * @param targetRole Role name of the target side; must not be null.
		 * @param docRef Document reference of the target entity; must not be null.
		 * @return This builder for method chaining.
		 */
		public Builder withTargetFilter(@NonNull  String targetRole, @NonNull  DocumentReference docRef) {
			this.specification.setTargetFilter(new RelationshipRoleSpecification(targetRole, docRef));
			return this;
		}

		/**
		 * Builds the relationship specification.
		 *
		 * @return The configured {@link RelationshipLinkSpecification}.
		 */
		public RelationshipLinkSpecification build() {
			return this.specification;
		}
	}
}
