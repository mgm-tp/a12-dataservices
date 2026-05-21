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
package com.mgmtp.a12.dataservices.query.generator.sql.constraint.range.internal;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.query.QueryException;
import com.mgmtp.a12.dataservices.query.annotation.QueryOperatorGenerator;
import com.mgmtp.a12.dataservices.query.constraint.range.DateRangeOperator;
import com.mgmtp.a12.dataservices.query.enrichement.Enrichments;
import com.mgmtp.a12.dataservices.query.generator.sql.ILogicOperatorGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.SqlGeneratorHelpers;
import com.mgmtp.a12.dataservices.query.generator.sql.constraint.range.AbstractRangeOperatorSqlGenerator;

import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.CONTAINS_OPERATOR;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.TIMESTAMP_VALUE_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.TS_RANGE_VALUE_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.DOT_JOINER;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.IS_CONTAINED_OPERATOR;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TIMESTAMP_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames.DOCUMENT_FIELDS_TABLE_ALIAS;
import static com.mgmtp.a12.dataservices.query.indexing.internal.DocumentSearchIndexBehaviour.CORE_SOURCE;

@QueryOperatorGenerator(DateRangeOperator.class)
@Component public class DateTimeRangeOperatorSqlGenerator<O extends DateRangeOperator> extends AbstractRangeOperatorSqlGenerator<O, String>
	implements ILogicOperatorGenerator<O> {

	public DateTimeRangeOperatorSqlGenerator() {
		super(CORE_SOURCE);
	}

	@Override protected void renderCondition(O operator, StringBuilder sb, QueryGeneratorContext generatorContext) {

		if (operator.isRangeType()) {
			sb.append(QueryGeneratorConstants.OPENING_BRACKET);
			appendRangeContainment(operator, sb, generatorContext);
			sb.append(QueryGeneratorConstants.CLOSING_BRACKET);
		} else {
			renderDocumentFieldsTableRangeCondition(sb,
				TIMESTAMP_VALUE_COLUMN_NAME,
				(String) generatorContext.getEnrichments().getOperatorEnrichment(operator).get(Enrichments.FROM_PROPERTY),
				(String) generatorContext.getEnrichments().getOperatorEnrichment(operator).get(Enrichments.TO_PROPERTY),
				generatorContext);
		}
	}

	private void appendRangeContainment(O operator, StringBuilder sb, QueryGeneratorContext generatorContext) {
		String from = (String) generatorContext.getEnrichments().getOperatorEnrichment(operator).get(Enrichments.FROM_PROPERTY);
		String to = (String) generatorContext.getEnrichments().getOperatorEnrichment(operator).get(Enrichments.TO_PROPERTY);
		if (generatorContext.getEnrichments().getOperatorEnrichment(operator).get(Enrichments.VALUE_PROPERTY) != null) {

			if (from == null && to == null) {
				throw new QueryException(ExceptionKeys.ExecutionPhase.QUERY_EXECUTION,
					ExceptionCodes.QUERY_INVALID_INPUT_ERROR_CODE,
					"For a range operator you can specify either \"from\" and \"to\" or \"value\", but not both.");
			}
			if (operator.isRangeType() && operator.isReverse()) {
				throw new QueryException(ExceptionKeys.ExecutionPhase.QUERY_EXECUTION,
					ExceptionCodes.QUERY_INVALID_INPUT_ERROR_CODE,
					"By value you can search in range type only with reverse enabled.");
			}
			sb.append(valueLiteral(Objects.toString(generatorContext.getEnrichments().getOperatorEnrichment(operator).get(Enrichments.VALUE_PROPERTY)),
				generatorContext));

		} else {
			Assert.isTrue(from != null || to != null, "For a range operator you must specify at least one value of \"from\" or \"to\".");
			renderInputRange(sb, nullOrQuoted(from, generatorContext), nullOrQuoted(to, generatorContext));
		}
		sb.append(operator.isReverse() ? IS_CONTAINED_OPERATOR : CONTAINS_OPERATOR);
		sb.append(DOCUMENT_FIELDS_TABLE_ALIAS)
			.append(DOT_JOINER)
			.append(TS_RANGE_VALUE_COLUMN_NAME);
	}

	@Override protected String valueLiteral(String input, QueryGeneratorContext generatorContext) {
		return input == null ? null : SqlGeneratorHelpers.addParam(input, generatorContext) + QueryGeneratorConstants.CAST_OPERATOR + TIMESTAMP_TYPE;
	}

	@NotNull private static CharSequence nullOrQuoted(String value, QueryGeneratorContext generatorContext) {
		return value == null ? QueryGeneratorConstants.NULL_VALUE : SqlGeneratorHelpers.addParam(value, generatorContext);
	}

	private static void renderInputRange(StringBuilder sb, CharSequence from, CharSequence to) {
		sb.append(QueryGeneratorConstants.TS_RANGE_FUNCTION)
			.append(QueryGeneratorConstants.OPENING_BRACKET)
			.append(from)
			.append(QueryGeneratorConstants.CAST_OPERATOR)
			.append(QueryGeneratorConstants.TIMESTAMP_TYPE)
			.append(QueryGeneratorConstants.COMMA)
			.append(to)
			.append(QueryGeneratorConstants.CAST_OPERATOR)
			.append(QueryGeneratorConstants.TIMESTAMP_TYPE)
			.append(QueryGeneratorConstants.COMMA)
			.append(QueryGeneratorConstants.TEXT_QUOTE)
			.append(QueryGeneratorConstants.CLOSED_RANGE) // [] as third argument : boundary is also included
			.append(QueryGeneratorConstants.TEXT_QUOTE)
			.append(QueryGeneratorConstants.CLOSING_BRACKET);
	}

}
