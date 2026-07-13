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
 * Sorting specification for a direct field.
 *
 * Used for sorting by a field directly on the queried document (root level)
 * or as a terminal specification nested within relationship traversals.
 *
 * @param field The field path to sort by.
 * @param direction The sort direction.
 * @param ignoreCase Whether sorting is case-insensitive (`true`) or case-sensitive (`false`).
 * May be `null` if not specified (treated as case-sensitive).
 * @param nullHandling The null handling strategy. May be `null` if not specified.
 */
@JsonDeserialize
public record DirectFieldOrder(@JsonPropertyDescription("The field path used for sorting") String field, Direction direction, Boolean ignoreCase,
	NullHandling nullHandling) implements Order {

	/**
	 * Enumeration for sort directions.
	 */
	public enum Direction {
		ASC, DESC
	}

	/**
	 * Enumeration for null handling hints.
	 */
	public enum NullHandling {

		/**
		 * Lets the data store decide what to do with null values.
		 */
		NATIVE,

		/**
		 * A hint to order entries with null values before non-null entries.
		 */
		NULLS_FIRST,

		/**
		 * A hint to order entries with null values after non-null entries.
		 */
		NULLS_LAST
	}

	/**
	 * Creates a direct field order with all settings specified.
	 *
	 * @param direction the sort direction
	 * @param field the field path to sort by
	 * @param ignoreCase `true` for case-insensitive sorting; may be `null`
	 * @param nullHandling the null handling strategy; may be `null`
	 */
	@JsonCreator
	public DirectFieldOrder(
		@JsonProperty("field") @JsonPropertyDescription("The field path used for sorting") String field,
		@JsonProperty("direction") Direction direction,
		@JsonProperty("ignoreCase") @JsonPropertyDescription("Whether sorting should be case-insensitive") Boolean ignoreCase,
		@JsonProperty("nullHandling") @JsonPropertyDescription("Selects the mode of handling null values in sorting") NullHandling nullHandling) {
		this.direction = direction;
		this.ignoreCase = ignoreCase;
		this.nullHandling = nullHandling;
		this.field = field;
	}

	/**
	 * Creates a direct field order with ascending direction and default settings.
	 *
	 * @param field the field path to sort by
	 */
	public DirectFieldOrder(String field) {
		this(field, Direction.ASC, false, NullHandling.NATIVE);
	}

	/**
	 * Creates a direct field order with the specified direction and default settings.
	 *
	 * @param field the field path to sort by
	 * @param direction the sort direction
	 */
	public DirectFieldOrder(String field, Direction direction) {
		this(field, direction, false, NullHandling.NATIVE);
	}

	/**
	 * Creates a direct field order with direction and case sensitivity.
	 *
	 * @param field the field path to sort by
	 * @param direction the sort direction
	 * @param ignoreCase whether to ignore case in sorting
	 */
	public DirectFieldOrder(String field, Direction direction, boolean ignoreCase) {
		this(field, direction, ignoreCase, NullHandling.NATIVE);
	}

	/**
	 * Creates a direct field order with direction and null handling.
	 *
	 * @param field the field path to sort by
	 * @param direction the sort direction
	 * @param nullHandling the null handling strategy
	 */
	public DirectFieldOrder(String field, Direction direction, NullHandling nullHandling) {
		this(field, direction, false, nullHandling);
	}
}
