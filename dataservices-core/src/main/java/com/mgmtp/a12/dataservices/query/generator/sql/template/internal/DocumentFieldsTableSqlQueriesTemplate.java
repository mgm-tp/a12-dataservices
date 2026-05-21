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

import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.VALUE_COLUMN_NAME;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DocumentFieldsTableSqlQueriesTemplate {

	public static String generateInsensitiveEqualityCheckForValueColumn(String rightValue, QueryGeneratorContext queryGeneratorContext) {
		return generateInsensitiveEqualityCheckForColumn(VALUE_COLUMN_NAME, rightValue, queryGeneratorContext);
	}

	public static String generateEqualityCheckForValueColumn(String rightValue, QueryGeneratorContext queryGeneratorContext) {
		return generateEqualityCheckForColumn(VALUE_COLUMN_NAME, rightValue, queryGeneratorContext);
	}

	public static String generateEqualityCheckForColumn(String columnName, String rightValue, QueryGeneratorContext queryGeneratorContext) {
		return CoreSqlQueriesTemplate.generateEqualityCheck(generatePathToColumn(columnName, queryGeneratorContext), rightValue);
	}

	public static String generateInsensitiveEqualityCheckForColumn(String columnName, String rightValue, QueryGeneratorContext queryGeneratorContext) {
		return CoreSqlQueriesTemplate.generateInsensitiveEqualityCheck(generatePathToColumn(columnName, queryGeneratorContext), rightValue);
	}

	private static String generatePathToColumn(String columnName, QueryGeneratorContext queryGeneratorContext) {
		return new StringBuilder()
			.append(queryGeneratorContext.getCurrentDocumentTableAlias())
			.append(QueryGeneratorConstants.DOT_JOINER)
			.append(columnName)
			.toString();
	}
}
