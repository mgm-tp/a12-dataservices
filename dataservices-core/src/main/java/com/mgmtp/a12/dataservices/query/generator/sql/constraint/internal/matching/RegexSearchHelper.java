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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.SimpleSearchOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.UndefinedMatchOperator;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.SqlGeneratorHelpers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.CLOSING_BRACKET;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.SEARCH_DATA_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.DOT_JOINER;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.ENUMERATION_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.OPENING_BRACKET;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.REGEX_CASE_INSENSITIVE_SEARCH_OPERATOR;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.REGEX_CASE_SENSITIVE_SEARCH_OPERATOR;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.SEARCH_DATA_VALUE_DELIMITER;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegexSearchHelper {

	private static final Set<Character> REGEX_SPECIAL_CHARS = new HashSet<>(Set.of('\\', '[', ']', '(', ')', '{', '}', '.', '*', '+', '?', '$', '^', '|'));
	private static final String NON_CAPTURING_GROUP = "(?:%s)";

	/**
	 * Start of the string or delimiter: `(?:^|~)`
	 */
	private static final String BEGINNING_OF_THE_FIELD = NON_CAPTURING_GROUP.formatted(String.join("|",
		"^", SEARCH_DATA_VALUE_DELIMITER));
	private static final String REGEX_EXCLUDE_METADATA = BEGINNING_OF_THE_FIELD + "(?!/__meta)" + "[^" + SEARCH_DATA_VALUE_DELIMITER + "]+";
	private static final String WILDCARD_INSIDE_DELIMITERS = "[^" + SEARCH_DATA_VALUE_DELIMITER + "]*";

	/**
	 * Appends an exact match expression to the given {@link StringBuilder}. The expression is built using regular expressions to ensure precise matching.
	 *
	 * @param sb the {@link StringBuilder} to append the expression to
	 * @param operator the {@link ExactMatchOperator} containing the field and value for the match
	 * @param tableAlias the alias of the table in the SQL query
	 * @param generatorContext the context for query generation, providing necessary metadata and configurations
	 * @param matchAlsoEnumerationValue if true, matches will also consider enumeration values without locale suffixes
	 * @return the updated {@link StringBuilder} with the appended exact match expression
	 */
	public static StringBuilder appendExactMatchCondition(StringBuilder sb, ExactMatchOperator<?> operator, String tableAlias,
		QueryGeneratorContext generatorContext, boolean matchAlsoEnumerationValue) {

		String value = operator.getValue().toString();

		Map<String, String> fieldTypes = Map.of(operator.getField(),
			StringUtils.firstNonBlank(getFieldType(operator, generatorContext), "IStringType"));

		return appendMatchCondition(sb,
			prepareFieldsPatternForMatch(List.of(operator.getField()), fieldTypes, getModelLocale(generatorContext), matchAlsoEnumerationValue),
			prepareValuePattern(List.of(value)),
			tableAlias, true, operator.isCaseSensitive(), false, generatorContext
		);
	}

	/**
	 * Appends a simple search condition to the given {@link StringBuilder}. The condition is built using regular expressions to match the specified fields and values.
	 *
	 * @param sb the {@link StringBuilder} to append the condition to
	 * @param operator the {@link SimpleSearchOperator} containing the fields and values for the search
	 * @param tableAlias the alias of the table in the SQL query
	 * @param exactMatch if true, performs an exact match; otherwise, allows partial matches
	 * @param caseSensitive if true, the search is case-sensitive; otherwise, it is case-insensitive
	 * @param excludingMetadata if true, excludes metadata fields from the search
	 * @param generatorContext the context for query generation, providing necessary metadata and configurations
	 * @return the updated {@link StringBuilder} with the appended simple search condition
	 */
	public static StringBuilder appendSimpleSearchCondition(StringBuilder sb, SimpleSearchOperator operator, String tableAlias,
		boolean exactMatch, boolean caseSensitive, boolean excludingMetadata, QueryGeneratorContext generatorContext) {

		List<String> searchValues = CollectionUtils.isEmpty(operator.getValues()) ? List.of(operator.getValue()) : operator.getValues();
		return appendMatchCondition(sb,
			prepareFieldsPatternForMatch(operator.getFields(), operator.getFieldsTypes(), getModelLocale(generatorContext), false),
			prepareValuePattern(searchValues),
			tableAlias, exactMatch, caseSensitive, excludingMetadata, generatorContext
		);
	}

	/**
	 * Appends a match field expression to the given {@link StringBuilder}. The expression is built using regular expressions to match the specified field.
	 *
	 * @param sb the {@link StringBuilder} to append the expression to
	 * @param operator the {@link UndefinedMatchOperator} containing the field for the match
	 * @param tableAlias the alias of the table in the SQL query
	 * @param generatorContext the context for query generation, providing necessary metadata and configurations
	 */
	public static void appendMatchFieldCondition(StringBuilder sb, UndefinedMatchOperator operator, String tableAlias,
		QueryGeneratorContext generatorContext) {

		appendSearchDataColumn(sb, tableAlias)
			.append(REGEX_CASE_SENSITIVE_SEARCH_OPERATOR)
			.append(
				SqlGeneratorHelpers.addParam("%s%s".formatted(wrapFieldWithDelimiters(operator.getField()), SEARCH_DATA_VALUE_DELIMITER), generatorContext));
	}

	private static String getModelLocale(QueryGeneratorContext generatorContext) {
		return generatorContext.getEnrichments().getModelLocale(generatorContext.getTargetDocumentModel());
	}

	private static String getFieldType(ExactMatchOperator<?> operator, QueryGeneratorContext generatorContext) {
		return generatorContext.getEnrichments().getFieldDescriptor(operator.getField()).getFieldType();
	}

	private static StringBuilder appendMatchCondition(StringBuilder sb, CharSequence fieldPattern, CharSequence valuePattern, String tableAlias,
		boolean exactMatch, boolean caseSensitive, boolean excludingMetadata, QueryGeneratorContext generatorContext) {

		sb.append(OPENING_BRACKET);

		boolean fieldIsBlank = StringUtils.isBlank(fieldPattern);
		if (!fieldIsBlank) {
			appendMatchCondition(sb, tableAlias, exactMatch, caseSensitive, valuePattern, "", generatorContext)
				.append(QueryGeneratorConstants.AND_OPERATOR);
		}
		CharSequence effectiveFieldPattern = !fieldIsBlank ? fieldPattern : excludingMetadata ? REGEX_EXCLUDE_METADATA : "";
		appendMatchCondition(sb, tableAlias, exactMatch, caseSensitive, valuePattern, effectiveFieldPattern, generatorContext)
			.append(CLOSING_BRACKET);
		return sb;
	}

	private static StringBuilder appendMatchCondition(StringBuilder sb, String tableAlias, boolean exactMatch, boolean caseSensitive,
		CharSequence valuePattern,
		@NotNull CharSequence fieldPattern, QueryGeneratorContext generatorContext) {

		return appendSearchDataColumn(sb, tableAlias)
			.append(caseSensitive ? REGEX_CASE_SENSITIVE_SEARCH_OPERATOR : REGEX_CASE_INSENSITIVE_SEARCH_OPERATOR)
			.append(SqlGeneratorHelpers.addParam(prepareFieldAndValueRegexp(fieldPattern, valuePattern, exactMatch, generatorContext), generatorContext));
	}

	@NotNull private static StringBuilder appendSearchDataColumn(StringBuilder sb, String tableAlias) {
		return sb.append(tableAlias).append(DOT_JOINER).append(SEARCH_DATA_COLUMN_NAME);
	}

	@NotNull private static String prepareFieldAndValueRegexp(@NotNull CharSequence fieldPattern, CharSequence valuePattern, boolean exactMatch,
		QueryGeneratorContext generatorContext) {

		StringBuilder expressionSb = new StringBuilder();
		expressionSb.append(fieldPattern);
		expressionSb.append(SEARCH_DATA_VALUE_DELIMITER);
		if (!exactMatch) {
			expressionSb.append(WILDCARD_INSIDE_DELIMITERS);
		}
		expressionSb.append(valuePattern);
		if (!exactMatch) {
			expressionSb.append(WILDCARD_INSIDE_DELIMITERS);
		}
		expressionSb.append(SEARCH_DATA_VALUE_DELIMITER)
			.append("/");
		return expressionSb.toString();
	}

	private static @Nullable CharSequence prepareFieldsPatternForMatch(List<String> fields, Map<String, String> fieldsTypes, String enrichedLocale,
		boolean matchAlsoEnumerationValue) {

		if (CollectionUtils.isEmpty(fields)) {
			return null;
		} else {
			List<String> stringStream = fields.stream()
				.flatMap(f -> Stream.concat(
						Stream.of(getLocalizedField(fieldsTypes, enrichedLocale, f)),
						Stream.of(getLocalizedField(fieldsTypes, null, f))
							.filter(any -> StringUtils.isNotBlank(enrichedLocale) && matchAlsoEnumerationValue)
					)
				)
				.map(RegexSearchHelper::wrapFieldWithDelimiters)
				.toList();
			return constructRegexpOr(stringStream);
		}
	}

	private static String getLocalizedField(Map<String, String> fieldsTypes, String enrichedLocale, String field) {
		return enrichedLocale != null && ENUMERATION_FIELD_TYPE.equals(fieldsTypes.get(field)) ? field + "\\[" + enrichedLocale + "\\]" : field;
	}

	private static CharSequence prepareValuePattern(List<String> values) {
		List<CharSequence> searchExpressions = values.stream().filter(StringUtils::isNotBlank)
			.map(RegexSearchHelper::cleanupSearchValue)
			.map(RegexSearchHelper::escapeRegex)
			.distinct()
			.toList();

		return constructRegexpOr(searchExpressions);
	}

	@Nullable private static CharSequence constructRegexpOr(List<? extends CharSequence> searchExpressions) {
		if (CollectionUtils.isEmpty(searchExpressions)) {
			return null;
		} else if (searchExpressions.size() == 1) {
			return searchExpressions.getFirst();
		} else {
			return NON_CAPTURING_GROUP.formatted(String.join("|", searchExpressions));
		}
	}

	@NotNull private static String wrapFieldWithDelimiters(String field) {
		return "%s%s".formatted(BEGINNING_OF_THE_FIELD, field);
	}

	private static String cleanupSearchValue(String input) {
		if (StringUtils.isBlank(input)) {
			return null;
		}
		return input.replace(SEARCH_DATA_VALUE_DELIMITER, "");
	}

	private static CharSequence escapeRegex(String input) {
		if (StringUtils.isBlank(input)) {
			return null;
		} else {
			return input.chars()
				.mapToObj(RegexSearchHelper::escapeRegexpChar)
				.collect(Collectors.joining());
		}
	}

	private static CharSequence escapeRegexpChar(int i) {
		char c = (char) i;
		return "%s%s".formatted(REGEX_SPECIAL_CHARS.contains(c) ? '\\' : "", c);
	}
}
