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

import java.util.Deque;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.query.annotation.QueryAggregationFunctionGenerator;
import com.mgmtp.a12.dataservices.query.fields.aggregation.IAggregationFunction;
import com.mgmtp.a12.dataservices.query.fields.aggregation.function.Count;
import com.mgmtp.a12.dataservices.query.generator.sql.IAggregationFunctionGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.SqlGeneratorHelpers;
import com.mgmtp.a12.dataservices.query.generator.sql.internal.SqlGeneratorHelpersInternal;
import com.mgmtp.a12.dataservices.query.internal.aggregation.RepeatableAggField;

@QueryAggregationFunctionGenerator({ Count.class })
@Component public class DefaultAggregationFunctionGenerator implements IAggregationFunctionGenerator<IAggregationFunction> {
	@Override public void renderFunction(StringBuilder sb, IAggregationFunction function, boolean isSearchTable, QueryGeneratorContext generatorContext) {

		sb.append(function.getFunction())
			.append(QueryGeneratorConstants.OPENING_BRACKET);

		if (StringUtils.isNotBlank(function.getType())) {
			renderCastedField(sb, function, isSearchTable, generatorContext);
		} else {
			if (generatorContext.isGroupingAgg()) {
				sb.append(QueryGeneratorConstants.COALESCE_FUNCTION)
					.append(QueryGeneratorConstants.OPENING_BRACKET);
				renderField(sb, function, isSearchTable, generatorContext);
				sb.append(QueryGeneratorConstants.COMMA)
					.append(QueryGeneratorConstants.EMPTY_VALUE)
					.append(QueryGeneratorConstants.CLOSING_BRACKET);
			} else {
				renderField(sb, function, isSearchTable, generatorContext);
			}
		}
		sb.append(QueryGeneratorConstants.CLOSING_BRACKET);
	}

	private static void renderCastedField(StringBuilder sb, IAggregationFunction function, boolean isSearchTable,
		QueryGeneratorContext queryGeneratorContext) {
		sb.append(QueryGeneratorConstants.OPENING_BRACKET);
		renderField(sb, function, isSearchTable, queryGeneratorContext);
		sb.append(QueryGeneratorConstants.CLOSING_BRACKET)
			.append(QueryGeneratorConstants.CAST_OPERATOR)
			.append(function.getType());
	}

	private static void renderField(StringBuilder sb, IAggregationFunction function, boolean isSearchTable, QueryGeneratorContext generatorContext) {
		if (isSearchTable) {
			String field = function.getField();
			// roots.content ->> '/ContractRoot/ContractName'
			if (SqlGeneratorHelpersInternal.isDocumentField(field)) {
				sb
					.append(generatorContext.getJsonColumnName())
					.append(QueryGeneratorConstants.JSON_FIELD_TEXT_VALUE_SELECTION_OPERATOR);
			}
			sb.append(SqlGeneratorHelpers.addParam(field, generatorContext));
		} else {
			if (generatorContext.getEnrichments().getFieldDescriptor(function.getField()).getRepeatable()) {
				Deque<RepeatableAggField> repeatableAggFields = generatorContext.getEnrichments().getRepeatableAggFields(function.getField());
				Optional<RepeatableAggField> repeatableAggField = Optional.ofNullable(repeatableAggFields.peekLast());
				if (repeatableAggField.isPresent()) {
					SqlGeneratorHelpersInternal.renderAggRepeatableField(sb, repeatableAggField);
				}
			} else {
				SqlGeneratorHelpersInternal.renderJsonbField(sb, function.getField(), generatorContext);
			}
		}
	}
}
