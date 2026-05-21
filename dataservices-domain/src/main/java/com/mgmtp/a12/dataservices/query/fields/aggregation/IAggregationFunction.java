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
package com.mgmtp.a12.dataservices.query.fields.aggregation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mgmtp.a12.dataservices.internal.DocumentationDiagram;
import com.mgmtp.a12.dataservices.query.annotation.QueryAggregationFunction;
import com.mgmtp.a12.dataservices.query.fields.AliasedFieldItem;
import com.mgmtp.a12.dataservices.query.fields.aggregation.internal.UnknownFunction;

/**
 * Represents an aggregation function in a query that is typically used in the context of data aggregation operations.
 * This interface extends the {@link AliasedFieldItem} interface and provides additional methods for
 * defining and retrieving the function name and type associated with the aggregation function.
 *
 * The function implementations are marked with the {@link QueryAggregationFunction} annotation to specify their
 * unique function name, which is primarily used during JSON deserialization. Unrecognized functions default to
 * the {@link UnknownFunction} implementation.
 *
 * Classes implementing this interface are expected to provide details regarding the data type or casting type
 * they represent using the `getType` method, and optionally supply a function name derived from the
 * {@link QueryAggregationFunction} annotation.
 *
 * JSON Serialization:
 *
 * - The function is included in serialized JSON under the property "function".
 * - Handles cases where no explicit data is assigned by including only non-empty properties in serialization.
 */
@DocumentationDiagram
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "function", defaultImpl = UnknownFunction.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public interface IAggregationFunction extends AliasedFieldItem {

	/**
	 * @return function name. By default derived from {@link QueryAggregationFunction} annotation value.
	 */
	@JsonIgnore default String getFunction() {
		return this.getClass().getAnnotation(QueryAggregationFunction.class).value();
	}

	/**
	 * Retrieves the type of the aggregation function.
	 *
	 * @return the type of the aggregation function, usually representing the data type or casting type
	 */
	@JsonIgnore String getType();

	/**
	 * Determines whether this aggregation function is applicable to the given field type.
	 *
	 * @param fieldType the logical field type identifier; may be null if unknown.
	 * @return true if the function is applicable to the specified field type; false otherwise.
	 */
	default boolean isSuitableForFieldType(String fieldType) {
		// Default implementation assumes all functions are suitable for all field types.
		// Specific implementations can override this method to provide custom logic.
		return true;
	}
}
