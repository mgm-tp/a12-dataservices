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
package com.mgmtp.a12.dataservices.query.generator.sql.fields.aggregation.internal;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.query.annotation.QueryAggregationFunctionGenerator;
import com.mgmtp.a12.dataservices.query.fields.aggregation.IAggregationFunction;
import com.mgmtp.a12.dataservices.query.fields.aggregation.internal.UnknownFunction;
import com.mgmtp.a12.dataservices.query.generator.sql.IAggregationFunctionGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_SQL_GENERATION;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.QUERY_INVALID_INPUT_ERROR_KEY;

@QueryAggregationFunctionGenerator({ UnknownFunction.class })
@Component public class UnknownAggregationFunctionGenerator implements IAggregationFunctionGenerator<IAggregationFunction> {
	@Override public void renderFunction(StringBuilder sb, IAggregationFunction function,boolean isSearchTable, QueryGeneratorContext queryGeneratorContext) {
		throw new QueryInvalidInputException(QUERY_SQL_GENERATION, QUERY_INVALID_INPUT_ERROR_KEY, null)
			.withAnonymityMessage("Unknown aggregation function");
	}
}
