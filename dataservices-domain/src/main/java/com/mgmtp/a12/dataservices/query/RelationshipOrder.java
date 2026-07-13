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
package com.mgmtp.a12.dataservices.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Relationship-based sort specification that traverses a to-1 relationship.
 *
 * Represents a hop in the relationship traversal chain, pointing to the next hop
 * via `sortBy`. The terminal specification (with actual field to sort by)
 * appears as a nested {@link DirectFieldOrder} in the chain.
 */
@JsonDeserialize
public record RelationshipOrder(@JsonProperty("relationshipModel") @JsonPropertyDescription("The relationship model name to traverse") String relationshipModel,
		@JsonProperty("targetRole") @JsonPropertyDescription("The target role name in the relationship") String targetRole,
		@JsonProperty("sortBy") @JsonPropertyDescription("Nested sort specification for the next hop or terminal field") Order sortBy)
	implements Order {

	/**
	 * Full constructor used by Jackson for deserialization.
	 *
	 * @param relationshipModel the relationship model name to traverse
	 * @param targetRole the target role name in the relationship
	 * @param sortBy the nested sort specification for the next hop
	 */
	@JsonCreator
	public RelationshipOrder(
		@JsonProperty("relationshipModel") String relationshipModel,
		@JsonProperty("targetRole") String targetRole,
		@JsonProperty("sortBy") Order sortBy) {
		this.relationshipModel = relationshipModel;
		this.targetRole = targetRole;
		this.sortBy = sortBy;
	}
}
