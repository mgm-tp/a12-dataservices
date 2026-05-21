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
package com.mgmtp.a12.dataservices.query.constraint.range;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.mgmtp.a12.dataservices.query.annotation.QueryOperator;
import com.mgmtp.a12.dataservices.query.constraint.ValueAware;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Date range operator that filters results by matching values within the specified date boundaries.
 * Supports both direct date matching and range evaluation depending on configuration.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonClassDescription("This is the date range operator. It allows to specify a range of dates for filtering results.")
@Data @NoArgsConstructor @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true) @SuperBuilder
@QueryOperator("date_range") public class DateRangeOperator extends RangeOperator<String> implements ValueAware<String> {

	private String value;

	/**
	 * If reverse is true,
	 * then the type in the DB must be date-range, and then we search that value or range defined by this operator is within the range stored in the DB.
	 * Otherwise, (reverse = false) DB value is searched to be in the range specified by the operator.
	 */
	@JsonPropertyDescription("If true, the operator checks if the specified date range is within the range stored in the database. " +
		"If false, it checks if the database range is within the specified date range.")
	private Boolean reverse;
	@JsonIgnore
	private boolean rangeType;

	/**
	 * Indicates whether the range evaluation is reversed (DB range contains the operator range).
	 *
	 * @return true if reverse evaluation is enabled; false otherwise.
	 */
	@JsonIgnore
	public boolean isReverse() {
		return getReverse() != null && getReverse();
	}
}
