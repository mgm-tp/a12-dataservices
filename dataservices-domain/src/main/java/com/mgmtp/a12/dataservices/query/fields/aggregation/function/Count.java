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
package com.mgmtp.a12.dataservices.query.fields.aggregation.function;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mgmtp.a12.dataservices.query.annotation.QueryAggregationFunction;
import com.mgmtp.a12.dataservices.query.fields.aggregation.internal.AbstractAggregationFunction;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Aggregation function that counts the number of entries matching the current projection and grouping.
 *
 * Count operates independently of the field type and is applicable universally.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonClassDescription("This is the count aggregation function. It counts the number of documents that match the specified criteria.")
@Data @NoArgsConstructor @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true) @SuperBuilder(toBuilder = true)
@QueryAggregationFunction("count") public class Count extends AbstractAggregationFunction {

	/**
	 * Count is independent of field type and always applicable.
	 *
	 * @param fieldType the logical field type; may be null.
	 * @return true, because count does not depend on the field type.
	 */
	@Override public boolean isSuitableForFieldType(String fieldType) {
		return true;
	}
}
