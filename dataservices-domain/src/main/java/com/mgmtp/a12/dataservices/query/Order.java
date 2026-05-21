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

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.extern.slf4j.Slf4j;

/**
 * @param direction Returns the order the property shall be sorted for.
 * @param field Returns the property to order for.
 * @param ignoreCase Returns whether the sort will be case-sensitive or case-insensitive.
 * @param nullHandling Returns the used {@link NullHandling} hint, which can but may not be respected by the used datastore.
 */
@Slf4j
public record Order(@JsonPropertyDescription("The direction of sorting") Direction direction,
					@JsonPropertyDescription("The field used for sorting") String field,
					@JsonPropertyDescription("Whether the sorting should be case-sensitive") Boolean ignoreCase,
					@JsonPropertyDescription("Selects the mode of handling NULLs in sorting") NullHandling nullHandling) {

	public Order(String field) {
		this(Direction.ASC, field, false, NullHandling.NATIVE);
	}

	public Order(String field, Direction direction) {
		this(direction, field, false, NullHandling.NATIVE);
	}

	public Order(String field, Direction direction, boolean ignoreCase) {
		this(direction, field, ignoreCase, NullHandling.NATIVE);
	}

	public Order(String field, Direction direction, NullHandling nullHandling) {
		this(direction, field, false, nullHandling);
	}

	/**
	 * Enumeration for null handling hints that can be used in {@link Order} expressions.
	 *
	 */
	public enum NullHandling {

		/**
		 * Lets the data store decide what to do with nulls.
		 */
		NATIVE,

		/**
		 * A hint to the used data store to order entries with null values before non null entries.
		 */
		NULLS_FIRST,

		/**
		 * A hint to the used data store to order entries with null values after non null entries.
		 */
		NULLS_LAST
	}

	/**
	 * Enumeration for sort directions.
	 */
	public enum Direction {

		ASC, DESC

	}
}
