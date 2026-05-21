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
package com.mgmtp.a12.dataservices.query.generator.sql;

import com.mgmtp.a12.dataservices.query.fields.aggregation.IAggregationFunction;

/**
 * Interface for generating SQL code for aggregation functions. Classes implementing this
 * interface are expected to render SQL for specific aggregation functions during query generation.
 *
 * @param <T> The type of aggregation function this generator supports. Must extend {@link IAggregationFunction}.
 */
public interface IAggregationFunctionGenerator<T extends IAggregationFunction> {

	/**
	 * Renders the SQL representation of a given aggregation function.
	 *
	 * @param sb The `StringBuilder` to append the rendered SQL to.
	 * @param operator The aggregation function operator to render. This should be of a type extending {@link IAggregationFunction}.
	 * @param isSearchTable A boolean flag indicating whether the function is being rendered in the context of a search table.
	 * @param queryGeneratorContext The current query generation context providing necessary details about the SQL rendering process.
	 */
	void renderFunction(StringBuilder sb, T operator, boolean isSearchTable, QueryGeneratorContext queryGeneratorContext);
}
