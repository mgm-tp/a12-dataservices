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
package com.mgmtp.a12.dataservices.query.generator.sql.constraint.internal.matching;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.dataservices.query.annotation.QueryOperatorGenerator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.enrichement.FieldDescriptor;
import com.mgmtp.a12.dataservices.query.generator.sql.ILogicOperatorGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.SqlGeneratorHelpers;
import com.mgmtp.a12.dataservices.query.generator.sql.template.internal.CoreSqlQueriesTemplate;

import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_VALIDATION;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.INVALID_QUERY_ERROR_KEY;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.CAST_OPERATOR;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.CLOSING_BRACKET;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.CLOSING_CURLED_BRACKET;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.COLON;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.CONTAINS_OPERATOR;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.DOC_REF_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.MODEL_NAME_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.ORIGINAL_VALUE_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.DOT_JOINER;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FORWARD_SLASH;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.BOOLEN_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.CONFIRM_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.ENUMERATION_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.NUMBER_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.JSONB_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.OPENING_BRACKET;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.OPENING_CURLED_BRACKET;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.SPACE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TEXT_DOUBLE_QUOTE;

@QueryOperatorGenerator({ ExactMatchOperator.class })
@RequiredArgsConstructor
public class ExactMatchOperatorSqlGenerator implements ILogicOperatorGenerator<ExactMatchOperator<?>> {

	private static final Set<String> TYPES_WITHOUT_QUOTES = Set.of(BOOLEN_FIELD_TYPE, CONFIRM_FIELD_TYPE);
	private final DataServicesCoreProperties dataServicesCoreProperties;

	@Override public StringBuilder renderCondition(StringBuilder sb, ExactMatchOperator<?> operator, QueryGeneratorContext generatorContext) {

		if (dataServicesCoreProperties.getQuery().getExactMatch().getMaxInputValueLength() < StringUtils.length(operator.getValue().toString())) {
			throw new QueryInvalidInputException(QUERY_VALIDATION, INVALID_QUERY_ERROR_KEY, null)
				.withAnonymityMessage("Please reduce the input value length to a value lower than %s for the %s operator.".formatted(
					dataServicesCoreProperties.getQuery().getExactMatch().getMaxInputValueLength(),
					operator.getOperator()));
		}

		String field = operator.getField();
		if (DocumentMetadataConstants.DOCREF_METADATA_PATH.equalsIgnoreCase(field)) {
			return sb.append(
				CoreSqlQueriesTemplate.generateEqualityCheck(DOC_REF_COLUMN_NAME,
					SqlGeneratorHelpers.addParam(operator.getValue().toString(), generatorContext)));
		} else if (DocumentMetadataConstants.MODEL_REFERENCE_PATH.equalsIgnoreCase(field)) {
			return sb.append(CoreSqlQueriesTemplate.generateEqualityCheck(MODEL_NAME_COLUMN_NAME,
				SqlGeneratorHelpers.addParam(String.valueOf(operator.getValue()), generatorContext)));
		} else if (useJsonbContains(operator, generatorContext)) {
			return appendJsonbContainsCondition(sb, operator, generatorContext);
		} else {
			return RegexSearchHelper.appendExactMatchCondition(sb, operator, generatorContext.getCurrentDocumentTableAlias(), generatorContext, dataServicesCoreProperties.getQuery().getExactMatch().getEnumerationValueMatch().isEnabled());
		}
	}

	private static StringBuilder appendJsonbContainsCondition(StringBuilder sb, ExactMatchOperator<?> operator, QueryGeneratorContext generatorContext) {
		sb.append(OPENING_BRACKET)
			.append(generatorContext.getCurrentDocumentTableAlias()).append(DOT_JOINER).append(ORIGINAL_VALUE_COLUMN_NAME)
			.append(CONTAINS_OPERATOR);
		sb.append(SqlGeneratorHelpers.addParam(
				createContainsExpression(operator, generatorContext.getEnrichments().getFieldDescriptor(operator.getField()).getFieldType()),
				generatorContext))
			.append(CAST_OPERATOR)
			.append(JSONB_TYPE);
		return sb.append(CLOSING_BRACKET);
	}

	private static String createContainsExpression(ExactMatchOperator<?> operator, String fieldType) {
		String[] elements = operator.getField().replaceFirst(FORWARD_SLASH, "").split(FORWARD_SLASH);
		// this generates a JSON object like {"Group":{"Field":"Value"}} from the field path /Group/Field plus the value
		StringBuilder sb = new StringBuilder();
		for (String name : elements) {
			sb
				.append(OPENING_CURLED_BRACKET)
				.append(SPACE)
				.append(TEXT_DOUBLE_QUOTE)
				.append(name)
				.append(TEXT_DOUBLE_QUOTE)
				.append(SPACE)
				.append(COLON)
				.append(SPACE);
		}
		sb.append(getJsonLiteral(operator, fieldType))
			.append(StringUtils.repeat(CLOSING_CURLED_BRACKET, elements.length));
		return sb.toString();
	}

	private static String getJsonLiteral(ExactMatchOperator<?> operator, String fieldType) {
		String value = StringEscapeUtils.escapeJson(operator.getValue().toString());
		return TYPES_WITHOUT_QUOTES.contains(fieldType) ? StringUtils.strip(value, TEXT_DOUBLE_QUOTE) : StringUtils.wrap(value, TEXT_DOUBLE_QUOTE);
	}

	private static boolean useJsonbContains(ExactMatchOperator<?> operator, QueryGeneratorContext context) {
		FieldDescriptor descriptor = context.getEnrichments().getFieldDescriptor(operator.getField());
		if (descriptor.getFieldType() == null) {
			// This happens for constraints on links. While we could get the field type from the operator,
			// we can't get the information about repeatability in this situation.
			// se we can not decide if it's possible to use the JSONB operator.
			return false;
		}
		// We can't use the @> operator for numbers as we don't know at this point if the number allows leading zeros or not
		// We can't use it for enumerations because we don't know if the passed value is the label value or the "internal" value.
		return operator.isCaseSensitive()
			&& !descriptor.getRepeatable()
			&& !NUMBER_FIELD_TYPE.equals(descriptor.getFieldType())
			&& !ENUMERATION_FIELD_TYPE.equals(descriptor.getFieldType());
	}
}
