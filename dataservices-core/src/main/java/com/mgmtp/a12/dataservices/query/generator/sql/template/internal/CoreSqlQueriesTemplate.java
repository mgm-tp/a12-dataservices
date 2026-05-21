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
package com.mgmtp.a12.dataservices.query.generator.sql.template.internal;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CoreSqlQueriesTemplate {

	public static String generateExistsQuery(String selectStatement) {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder
			.append(QueryGeneratorConstants.EXISTS_OPERATOR)
			.append(QueryGeneratorConstants.OPENING_BRACKET)
			.append(selectStatement)
			.append(QueryGeneratorConstants.CLOSING_BRACKET);

		return stringBuilder.toString();
	}

	public static String generateEqualityCheck(String leftValue, String rightValue) {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder
			.append(leftValue)
			.append(QueryGeneratorConstants.EQUALS_OPERATOR)
			.append(rightValue);

		return stringBuilder.toString();
	}

	public static String generateInsensitiveEqualityCheck(String leftValue, String rightValue) {
		return generateEqualityCheck(CoreSqlQueriesTemplate.applyLowerFunction(leftValue), CoreSqlQueriesTemplate.applyLowerFunction(rightValue));
	}

	public static String applyLowerFunction(String value) {
		return QueryGeneratorConstants.LOWER_FUNCTION + QueryGeneratorConstants.OPENING_BRACKET + value + QueryGeneratorConstants.CLOSING_BRACKET;
	}

	public static String generateSelectQuery(List<String> columns, String fromTable, String alias, String whereCondition) {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder
			.append(QueryGeneratorConstants.SELECT_KEYWORD)
			.append(StringUtils.join(columns, ", "))
			.append(QueryGeneratorConstants.FROM_KEYWORD)
			.append(fromTable);

		if (alias != null) {
			stringBuilder
				.append(QueryGeneratorConstants.AS_KEYWORD)
				.append(alias);
		}

		stringBuilder
			.append(QueryGeneratorConstants.WHERE_KEYWORD)
			.append(whereCondition);

		return stringBuilder.toString();
	}
}
