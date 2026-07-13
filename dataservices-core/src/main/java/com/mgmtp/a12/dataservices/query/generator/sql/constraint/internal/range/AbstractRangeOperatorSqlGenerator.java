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
package com.mgmtp.a12.dataservices.query.generator.sql.constraint.internal.range;

import java.util.function.BiConsumer;

import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.query.constraint.FieldAwareOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.RangeOperator;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.internal.SqlGeneratorHelpersInternal;

import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.AND_OPERATOR;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ASTERISK;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.AS_KEYWORD;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.CLOSING_BRACKET;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.DOC_REF_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.FIELD_NAME_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.DOT_JOINER;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.EQUALS_OPERATOR;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.EXISTS_OPERATOR;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FROM_KEYWORD;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.OPENING_BRACKET;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.SELECT_KEYWORD;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames.DOCUMENT_FIELDS_TABLE_ALIAS;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames.DOCUMENT_FIELDS_TABLE_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.WHERE_KEYWORD;

@RequiredArgsConstructor
public abstract class AbstractRangeOperatorSqlGenerator<O extends RangeOperator<T>, T> {

	public StringBuilder renderCondition(StringBuilder sb, O operator, QueryGeneratorContext queryGeneratorContext) {
		if (SqlGeneratorHelpersInternal.isDocumentField(operator.getField())) {
			return queryDocumentFieldsForRanges(sb, operator, queryGeneratorContext,
				(fieldOperator, stringBuilder) -> renderCondition(fieldOperator, stringBuilder, queryGeneratorContext));
		} else {
			throw new QueryInvalidInputException(ExceptionKeys.ExecutionPhase.QUERY_EXECUTION,
				ExceptionCodes.QUERY_INVALID_INPUT_ERROR_CODE, "Field on path [%s] is not a Document Field".formatted(operator.getField()))
				.withAnonymityMessage("Field on path is not a document field.");
		}
	}

	protected abstract void renderCondition(O operator, StringBuilder sb, QueryGeneratorContext queryGeneratorContext);

	protected <O extends FieldAwareOperator> StringBuilder queryDocumentFieldsForRanges(StringBuilder sb, O fieldAwareOperator,
		QueryGeneratorContext generatorContext, BiConsumer<O, StringBuilder> renderCondition) {

		sb.append(EXISTS_OPERATOR).append(OPENING_BRACKET)
			.append(SELECT_KEYWORD).append(ASTERISK)
			.append(FROM_KEYWORD)
			.append(DOCUMENT_FIELDS_TABLE_NAME)
			.append(AS_KEYWORD)
			.append(DOCUMENT_FIELDS_TABLE_ALIAS)
			.append(WHERE_KEYWORD)
			.append(DOCUMENT_FIELDS_TABLE_ALIAS)
			.append(DOT_JOINER)
			.append(FIELD_NAME_COLUMN_NAME)
			.append(EQUALS_OPERATOR)
			.append(SqlGeneratorHelpersInternal.addParam(fieldAwareOperator.getField(), generatorContext))
			.append(AND_OPERATOR)
			.append(generatorContext.getCurrentDocumentTableAlias())
			.append(DOT_JOINER)
			.append(DOC_REF_COLUMN_NAME)
			.append(EQUALS_OPERATOR)
			.append(DOCUMENT_FIELDS_TABLE_ALIAS)
			.append(DOT_JOINER)
			.append(DOC_REF_COLUMN_NAME)
			.append(AND_OPERATOR);

		renderCondition.accept(fieldAwareOperator, sb);

		return sb.append(CLOSING_BRACKET);
	}

	/**
	 * Renders a range condition in the form (column >= fromValue AND column <= toValue).
	 *
	 * The values must be a valid SQL literals, no quoting will take place.
	 *
	 * If only one value is provided the expression will be rendered without the other value
	 * (e.g. column <= toValue if the "fromValue" is null)
	 *
	 * @param sb the buffer to append the condition
	 * @param columnName the column name from the document_fields table
	 * @param fromValue the lower value of the range
	 * @param toValue the upper value of the range.
	 * @param generatorContext
	 */
	protected void renderDocumentFieldsTableRangeCondition(StringBuilder sb, String columnName, String fromValue, String toValue,
		QueryGeneratorContext generatorContext) {

		if (fromValue == null && toValue == null) {
			throw new QueryInvalidInputException(ExceptionKeys.ExecutionPhase.QUERY_EXECUTION,
				ExceptionCodes.QUERY_INVALID_INPUT_ERROR_CODE,
				"For a range operator you must specify at least one value, either 'from' or 'to' or both.");
		}

		sb.append(QueryGeneratorConstants.OPENING_BRACKET);
		if (fromValue != null) {
			sb.append(DOCUMENT_FIELDS_TABLE_ALIAS)
				.append(DOT_JOINER)
				.append(columnName)
				.append(QueryGeneratorConstants.GREATER_THAN_OR_EQUAL_OPERATOR) // fromValue is also included
				.append(valueLiteral(fromValue, generatorContext));
		}

		if (toValue != null) {
			if (fromValue != null) {
				sb.append(AND_OPERATOR);
			}
			sb.append(DOCUMENT_FIELDS_TABLE_ALIAS)
				.append(DOT_JOINER)
				.append(columnName)
				.append(QueryGeneratorConstants.LESS_THAN_OR_EQUAL_OPERATOR) // toValue is also included
				.append(valueLiteral(toValue, generatorContext));
		}
		sb.append(QueryGeneratorConstants.CLOSING_BRACKET);

	}

	protected String valueLiteral(String input, QueryGeneratorContext generatorContext) {
		return SqlGeneratorHelpersInternal.addParam(input, generatorContext);
	}
}
