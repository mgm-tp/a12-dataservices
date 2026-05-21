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

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.query.annotation.QueryOperatorGenerator;
import com.mgmtp.a12.dataservices.query.constraint.matching.SimpleSearchOperator;
import com.mgmtp.a12.dataservices.query.generator.sql.ILogicOperatorGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.SqlGeneratorHelpers;
import com.mgmtp.a12.dataservices.query.generator.sql.internal.SqlGeneratorHelpersInternal;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_VALIDATION;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.INVALID_QUERY_ERROR_KEY;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.AND_OPERATOR;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.CLOSING_BRACKET;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.COMMA;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.ENUMERATION_ORIGINAL_VALUE_KEY;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.FIELD_NAME_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.LOCALE_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.LOCALIZED_FULLTEXT_STRING_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.MODEL_NAME_COLUMN_ALIAS;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.MODEL_NAME_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.ORIGINAL_VALUE_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.DOT_JOINER;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.EMPTY_VALUE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.EQUALS_OPERATOR;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FORWARD_SLASH;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FROM_KEYWORD;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.ENUMERATION_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ILIKE_OPERATOR;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.IN_OPERATOR;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.OPENING_BRACKET;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.OR_OPERATOR;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.SELECT_KEYWORD;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.SPACE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TEXT_QUOTE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames.LOCALIZED_FIELDS_TABLE_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.WHERE_KEYWORD;

@QueryOperatorGenerator({ SimpleSearchOperator.class })
@RequiredArgsConstructor
@Slf4j
@Component public class SimpleSearchOperatorSqlGenerator implements ILogicOperatorGenerator<SimpleSearchOperator> {

	private final EntityManager entityManager;
	private final DataServicesCoreProperties dataServicesCoreProperties;

	// TODO [A12S-5896]: This code needs to be conditionally executed based on new config if RegexSearch is disabled
	private static final SimpleSearchOperator.TermJoinType JOIN_TYPE = SimpleSearchOperator.TermJoinType.AND;

	@Override
	public StringBuilder renderCondition(StringBuilder sb, SimpleSearchOperator operator, QueryGeneratorContext generatorContext) {

		if (isLongerThanMaxLength(dataServicesCoreProperties.getQuery().getSimpleSearch().getMaxInputValueLength(), operator)) {
			throw new QueryInvalidInputException(QUERY_VALIDATION, INVALID_QUERY_ERROR_KEY, null)
				.withAnonymityMessage("Please reduce the input value length to a value lower than %s for the %s operator.".formatted(
					dataServicesCoreProperties.getQuery().getSimpleSearch().getMaxInputValueLength(),
					operator.getOperator()));
		}

		// Apply check for minimum number of characters in search value.
		// We cannot do this in validation phase because it is a security requirement, and validation may be skipped in production.
		int minSearchableTokenSize = dataServicesCoreProperties.getQuery().getSimpleSearch().getMinSearchableTokenSize();
		if ((StringUtils.isNotBlank(operator.getValue()) && operator.getValue().length() < minSearchableTokenSize)
			|| (CollectionUtils.isNotEmpty(operator.getValues()) && valuesContainInvalidSearchTerm(operator.getValues(), minSearchableTokenSize))) {
			log.error("Query could not be executed since the length of at least one search term in [{}] is below the allowed minimum value of {}",
				!StringUtils.isBlank(operator.getValue()) ?
					operator.getValue() :
					String.join(", ", operator.getValues()),
				minSearchableTokenSize);
			throw new QueryInvalidInputException(ExceptionKeys.ExecutionPhase.QUERY_SQL_GENERATION, INVALID_QUERY_ERROR_KEY, "Query could not be executed due to invalid search data");
		}

		return RegexSearchHelper.appendSimpleSearchCondition(sb, operator, generatorContext.getCurrentDocumentTableAlias(), false, false,
			dataServicesCoreProperties.getQuery().getSimpleSearch().getExcludingMetadata().isEnabled(), generatorContext);

		// TODO [A12S-5896]: This code needs to be conditionally executed based on new config if RegexSearch is disabled
		/*List<String> searchTerms = documentFieldsJpaRepository.convertInputToSearchTerms(operator.getValue()).stream()
			.filter(term -> term.length() >= dataServicesCoreProperties.getQuery().getSimpleSearch().getMinSearchableTokenSize())
			.toList();

		if (searchTerms.isEmpty()) {
			// condition which should never be satisfied
			return sb.append("1 = 0");
		}

		String joinedSearchTerms = buildFulltextSearchTerms(searchTerms, operator.getTermJoinType().getValue(),
			generatorContext.getCurrentDocumentTableAlias(), FULLTEXT_STRING_COLUMN_NAME);

		if (!queryConfigHelper.isSearchTableEnabled()) {
			if (!operator.getFields().isEmpty()) {
				return renderFieldsJsonbCondition(operator, generatorContext, sb, searchTerms);
			}

			return renderJsonbCondition(sb, joinedSearchTerms);
		}

		if (CollectionUtils.isNotEmpty(operator.getFields())) {
			appendFieldCondition(sb, operator, generatorContext.getCurrentDocumentTableAlias());
		}

		if (operator.getEnrichedLocale() != null) {
			String localizedSearchClause = buildLocalizedSearchClause(searchTerms, operator, generatorContext);
			String inClauseValues = fetchInClauseValues(localizedSearchClause);

			if (!inClauseValues.isEmpty()) {
				appendInClause(sb, inClauseValues);
			}

			sb.append(OPENING_BRACKET)
				.append(generatorContext.getCurrentDocumentTableAlias())
				.append(DOT_JOINER)
				.append(FIELD_TYPE_COLUMN_NAME)
				.append(NOT_EQUALS_OPERATOR)
				.append(TEXT_QUOTE)
				.append(ENUMERATION_FIELD_TYPE)
				.append(TEXT_QUOTE)
				.append(AND_OPERATOR);
		}

		sb.append(OPENING_BRACKET).append(joinedSearchTerms).append(CLOSING_BRACKET);

		if (operator.getEnrichedLocale() != null ) {
			sb.append(CLOSING_BRACKET);
		}

		return sb;*/
	}

	private static boolean valuesContainInvalidSearchTerm(List<String> values, int minSearchableTokenSize) {
		return values.stream()
			.anyMatch(v -> v.length() < minSearchableTokenSize);
	}

	private static StringBuilder renderFieldsJsonbCondition(SimpleSearchOperator operator, QueryGeneratorContext generatorContext, StringBuilder sb,
		List<String> searchTerms) {
		boolean isFirstField = true;

		for (String field : operator.getFields()) {
			if (!isFirstField) {
				sb.append(OR_OPERATOR);
			} else {
				isFirstField = false;
			}

			String fieldReference = resolveFieldReference(operator, field, generatorContext);

			sb.append(OPENING_BRACKET)
				.append(buildFulltextSearchTerms(
					searchTerms,
					JOIN_TYPE.getValue(),
					generatorContext.getCurrentDocumentTableAlias(),
					ORIGINAL_VALUE_COLUMN_NAME + SqlGeneratorHelpersInternal.makeJsonFieldValueReferenceAndGetAsText(fieldReference, generatorContext), generatorContext
				))
				.append(CLOSING_BRACKET);
		}

		return sb;
	}

	private static String resolveFieldReference(SimpleSearchOperator operator, String field, QueryGeneratorContext generatorContext) {
		if (ENUMERATION_FIELD_TYPE.equals(operator.getFieldsTypes().get(field))) {
			String locale = generatorContext.getEnrichments().getModelLocale(generatorContext.getTargetDocumentModel());
			return locale == null
				? field + FORWARD_SLASH + ENUMERATION_ORIGINAL_VALUE_KEY
				: field + FORWARD_SLASH + locale;
		}

		return field;
	}

	private static StringBuilder renderJsonbCondition(StringBuilder sb, String joinedSearchTerms) {
		return sb.append(OPENING_BRACKET).append(joinedSearchTerms).append(CLOSING_BRACKET);
	}

	private static String buildFulltextSearchTerms(List<String> searchTerms, String joinType, String tableAlias, String columnName,
		QueryGeneratorContext generatorContext) {

		return searchTerms.stream()
			.map(term -> tableAlias + DOT_JOINER + columnName + ILIKE_OPERATOR + SqlGeneratorHelpers.addParam(term, generatorContext))
			.collect(Collectors.joining(SPACE + joinType + SPACE));
	}

	private static void appendFieldCondition(StringBuilder sb, SimpleSearchOperator operator, String tableName, QueryGeneratorContext generatorContext) {
		String fieldConditions = operator.getFields().stream()
			.map(input -> SqlGeneratorHelpers.addParam(input, generatorContext))
			.collect(Collectors.joining(COMMA));

		sb.append(tableName)
			.append(DOT_JOINER)
			.append(FIELD_NAME_COLUMN_NAME)
			.append(IN_OPERATOR)
			.append(OPENING_BRACKET)
			.append(fieldConditions)
			.append(CLOSING_BRACKET)
			.append(AND_OPERATOR);
	}

	private static String buildLocalizedSearchClause(List<String> searchTerms, SimpleSearchOperator operator, QueryGeneratorContext generatorContext) {
		String joinedTerms = searchTerms.stream()
			.map(term -> LOCALIZED_FULLTEXT_STRING_COLUMN_NAME + ILIKE_OPERATOR + SqlGeneratorHelpers.addParam(term, generatorContext))
			.collect(Collectors.joining(SPACE + JOIN_TYPE.getValue() + SPACE));

		StringBuilder sb = new StringBuilder()
			.append(SELECT_KEYWORD)
			.append(ORIGINAL_VALUE_COLUMN_NAME)
			.append(COMMA)
			.append(FIELD_NAME_COLUMN_NAME)
			.append(COMMA)
			.append(MODEL_NAME_COLUMN_ALIAS)
			.append(FROM_KEYWORD)
			.append(LOCALIZED_FIELDS_TABLE_NAME)
			.append(WHERE_KEYWORD);

		if (CollectionUtils.isNotEmpty(operator.getFields())) {
			appendFieldCondition(sb, operator, LOCALIZED_FIELDS_TABLE_NAME, generatorContext);
		}

		String targetDocumentModel = generatorContext.getTargetDocumentModel();
		sb.append(LOCALE_COLUMN_NAME)
			.append(EQUALS_OPERATOR)
			.append(SqlGeneratorHelpers.addParam(generatorContext.getEnrichments().getModelLocale(targetDocumentModel), generatorContext))
			.append(AND_OPERATOR)
			.append(MODEL_NAME_COLUMN_NAME)
			.append(EQUALS_OPERATOR)
			.append(SqlGeneratorHelpers.addParam(targetDocumentModel, generatorContext))
			.append(AND_OPERATOR)
			.append(OPENING_BRACKET).append(joinedTerms).append(CLOSING_BRACKET);

		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	private String fetchInClauseValues(String localizedSearchClause, QueryGeneratorContext generatorContext) {
		Query query = entityManager.createNativeQuery(localizedSearchClause);
		List<String> results = query.getResultStream()
			.map(row -> {
				Object[] columns = (Object[]) row;
				String originalValue = columns[0].toString().replace(TEXT_QUOTE, EMPTY_VALUE);
				String fieldName = columns[1].toString().replace(TEXT_QUOTE, EMPTY_VALUE);
				String modelName = columns[2].toString().replace(TEXT_QUOTE, EMPTY_VALUE);
				return OPENING_BRACKET
					+ SqlGeneratorHelpers.addParam(originalValue, generatorContext) + COMMA
					+ SqlGeneratorHelpers.addParam(fieldName, generatorContext) + COMMA
					+ SqlGeneratorHelpers.addParam(modelName, generatorContext)
					+ CLOSING_BRACKET;
			})
			.toList();

		return String.join(COMMA, results);
	}

	private static void appendInClause(StringBuilder sb, String inClauseValues) {
		sb.append(OPENING_BRACKET)
			.append(ORIGINAL_VALUE_COLUMN_NAME)
			.append(COMMA)
			.append(FIELD_NAME_COLUMN_NAME)
			.append(COMMA)
			.append(MODEL_NAME_COLUMN_ALIAS)
			.append(CLOSING_BRACKET)
			.append(IN_OPERATOR)
			.append(OPENING_BRACKET)
			.append(inClauseValues)
			.append(CLOSING_BRACKET)
			.append(OR_OPERATOR);
	}

	private static boolean isLongerThanMaxLength(int maxLength, SimpleSearchOperator operator) {
		if (maxLength < StringUtils.length(operator.getValue())) {
			return true;
		}
		return operator.getValues() != null &&  operator.getValues().stream().anyMatch(v -> maxLength < StringUtils.length(v));
	}
}
