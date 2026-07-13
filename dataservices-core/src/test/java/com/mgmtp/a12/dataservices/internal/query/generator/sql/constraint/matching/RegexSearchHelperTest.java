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
package com.mgmtp.a12.dataservices.internal.query.generator.sql.constraint.matching;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.internal.query.generator.sql.constraint.AbstractSqlGeneratorTest;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.SimpleSearchOperator;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.constraint.internal.matching.RegexSearchHelper;

import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;

import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.DATE_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.ENUMERATION_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.STRING_FIELD_TYPE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

public class RegexSearchHelperTest extends AbstractSqlGeneratorTest {

	@DataProvider public static Object[][] simpleSearchProvider() {
		return new Object[][] {
			new Object[] {
				"Simple field condition",
				SimpleSearchOperator.builder()
					.value("foobar")
					.fields(List.of("/BusinessPartnerRoot/SubtypeGroup/Company")).build(),
				null,
				"(st.search_data ~* :p0 AND st.search_data ~* :p1)",
				Map.of("p0", "~[^~]*foobar[^~]*~/",
					"p1", "(?:^|~)/BusinessPartnerRoot/SubtypeGroup/Company~[^~]*foobar[^~]*~/")
			},
			new Object[] {
				"Search all fields with special character",
				SimpleSearchOperator.builder().value("something^'").build(),
				null,
				"(st.search_data ~* :p0)",
				Map.of("p0", "(?:^|~)/[^~\\[]*~[^~]*something\\^'[^~]*~/")
			},
			new Object[] {
				"Global search, no locale, matches keys/values but excludes localized labels",
				SimpleSearchOperator.builder().value("technology").build(),
				null,
				"(st.search_data ~* :p0)",
				Map.of("p0", "(?:^|~)/[^~\\[]*~[^~]*technology[^~]*~/")
			},
			new Object[] {
				"Global search, with locale, also matches the matching-locale label value",
				SimpleSearchOperator.builder().value("technology").build(),
				"en_US",
				"(st.search_data ~* :p0)",
				Map.of("p0", "(?:^|~)/[^~\\[]*(?:\\[en_US\\])?~[^~]*technology[^~]*~/")
			},
			new Object[] {
				"Regex escapes",
				SimpleSearchOperator.builder()
					.value("$[foo.*bar]'^")
					.fields(List.of("/BusinessPartnerRoot/SubtypeGroup/Company")).build(),
				null,
				"(st.search_data ~* :p0 AND st.search_data ~* :p1)",
				Map.of("p0", "~[^~]*\\$\\[foo\\.\\*bar\\]'\\^[^~]*~/",
					"p1", "(?:^|~)/BusinessPartnerRoot/SubtypeGroup/Company~[^~]*\\$\\[foo\\.\\*bar\\]'\\^[^~]*~/")
			},
			new Object[] {
				"Multiple values",
				SimpleSearchOperator.builder()
					.values(List.of("foo", "bar"))
					.fields(List.of("/BusinessPartnerRoot/SubtypeGroup/Company")).build(),
				null,
				"(st.search_data ~* :p0 AND st.search_data ~* :p1)",
				Map.of("p0", "~[^~]*(?:foo|bar)[^~]*~/",
					"p1", "(?:^|~)/BusinessPartnerRoot/SubtypeGroup/Company~[^~]*(?:foo|bar)[^~]*~/")
			},
			new Object[] {
				"Multiple values, multiple fields",
				SimpleSearchOperator.builder()
					.values(List.of("foo", "bar"))
					.fields(List.of("/BusinessPartnerRoot/SubtypeGroup/Company", "/ContractRoot/ContractDescription")).build(),
				null,
				"(st.search_data ~* :p0 AND st.search_data ~* :p1)",
				Map.of("p0", "~[^~]*(?:foo|bar)[^~]*~/",
					"p1", "(?:(?:^|~)/BusinessPartnerRoot/SubtypeGroup/Company|(?:^|~)/ContractRoot/ContractDescription)~[^~]*(?:foo|bar)[^~]*~/")
			},
			new Object[] {
				"Multiple values, special character",
				SimpleSearchOperator.builder()
					.values(List.of("foo", "$bar"))
					.fields(List.of("/BusinessPartnerRoot/SubtypeGroup/Company")).build(),
				null,
				"(st.search_data ~* :p0 AND st.search_data ~* :p1)",
				Map.of("p0", "~[^~]*(?:foo|\\$bar)[^~]*~/",
					"p1", "(?:^|~)/BusinessPartnerRoot/SubtypeGroup/Company~[^~]*(?:foo|\\$bar)[^~]*~/")
			},
			new Object[] {
				"Localized field condition",
				SimpleSearchOperator.builder()
					.value("technology")
					.fields(List.of("/BusinessPartnerRoot/Industry"))
					.fieldsTypes(Map.of("/BusinessPartnerRoot/Industry", ENUMERATION_FIELD_TYPE))
					.build(),
				"en_US",
				"(st.search_data ~* :p0 AND st.search_data ~* :p1)",
				Map.of("p0", "~[^~]*technology[^~]*~/",
					"p1", "(?:^|~)/BusinessPartnerRoot/Industry\\[en_US\\]~[^~]*technology[^~]*~/")
			},
			new Object[] {
				"Multiple fields",
				SimpleSearchOperator.builder()
					.value("foobar")
					.fields(List.of("/BusinessPartnerRoot/SubtypeGroup/Company", "/BusinessPartnerRoot/Name")).build(),
				null,
				"(st.search_data ~* :p0 AND st.search_data ~* :p1)",
				Map.of("p0", "~[^~]*foobar[^~]*~/",
					"p1", "(?:(?:^|~)/BusinessPartnerRoot/SubtypeGroup/Company|(?:^|~)/BusinessPartnerRoot/Name)~[^~]*foobar[^~]*~/")
			},
			new Object[] {
				"Multiple fields, localized",
				SimpleSearchOperator.builder()
					.value("technology")
					.fields(List.of("/BusinessPartnerRoot/Industry", "/BusinessPartnerRoot/SubtypeGroup/Company"))
					.fieldsTypes(Map.of("/BusinessPartnerRoot/Industry", ENUMERATION_FIELD_TYPE))
					.build(),
				"en_US",
				"(st.search_data ~* :p0 AND st.search_data ~* :p1)",
				Map.of("p0", "~[^~]*technology[^~]*~/",
					"p1", "(?:(?:^|~)/BusinessPartnerRoot/Industry\\[en_US\\]|(?:^|~)/BusinessPartnerRoot/SubtypeGroup/Company)~[^~]*technology[^~]*~/")
			}
		};
	}

	@DataProvider public static Object[][] simpleSearchExcludingMetadataProvider() {
		return new Object[][] {
			new Object[] {
				"Simple field condition",
				SimpleSearchOperator.builder()
					.value("foobar")
					.fields(List.of("/BusinessPartnerRoot/SubtypeGroup/Company")).build(),
				null,
				"(st.search_data ~* :p0 AND st.search_data ~* :p1)",
				Map.of("p0", "~[^~]*foobar[^~]*~/",
					"p1", "(?:^|~)/BusinessPartnerRoot/SubtypeGroup/Company~[^~]*foobar[^~]*~/")
			},
			new Object[] {
				"Search all fields with special character",
				SimpleSearchOperator.builder().value("something^'").build(),
				null,
				"(st.search_data ~* :p0)",
				Map.of("p0", "(?:^|~)(?!/__meta)/[^~\\[]*~[^~]*something\\^'[^~]*~/")
			},
			new Object[] {
				"Global search excluding metadata, with locale, matches matching-locale label value",
				SimpleSearchOperator.builder().value("technology").build(),
				"en_US",
				"(st.search_data ~* :p0)",
				Map.of("p0", "(?:^|~)(?!/__meta)/[^~\\[]*(?:\\[en_US\\])?~[^~]*technology[^~]*~/")
			}
		};
	}

	@DataProvider public static Object[][] exactMatchProvider() {
		return new Object[][] {
			new Object[] {
				"Simple value match",
				ExactMatchOperator.builder()
					.field("/BusinessPartnerRoot/SubtypeGroup/Company")
					.value("Technology")
					.caseSensitive(true).build(),
				STRING_FIELD_TYPE,
				null,
				"(st.search_data ~ :p0 AND st.search_data ~ :p1)",
				Map.of("p0", "~Technology~/",
					"p1", "(?:^|~)/BusinessPartnerRoot/SubtypeGroup/Company~Technology~/"
				)
			},
			new Object[] {
				"Enum value match",
				ExactMatchOperator.builder()
					.field("/BusinessPartnerRoot/Industry")
					.value("Technology")
					.caseSensitive(true).build(),
				ENUMERATION_FIELD_TYPE,
				"en_US",
				"(st.search_data ~ :p0 AND st.search_data ~ :p1)",
				Map.of("p0", "~Technology~/",
					"p1", "(?:^|~)/BusinessPartnerRoot/Industry~Technology~/")
			},
			new Object[] {
				"Date value match",
				ExactMatchOperator.builder()
					.field("/BusinessPartnerRoot/StartOfRelationShip")
					.value("2025-01-01")
					.caseSensitive(true).build(),
				DATE_FIELD_TYPE,
				null,
				"(st.search_data ~ :p0 AND st.search_data ~ :p1)",
				Map.of("p0", "~2025-01-01~/",
					"p1", "(?:^|~)/BusinessPartnerRoot/StartOfRelationShip~2025-01-01~/")
			},
			new Object[] {
				"Single character exact match",
				ExactMatchOperator.builder()
					.field("/BusinessPartnerRoot/Company/Name")
					.value("*")
					.caseSensitive(true).build(),
				STRING_FIELD_TYPE,
				null,
				"(st.search_data ~ :p0 AND st.search_data ~ :p1)",
				Map.of("p0", "~\\*~/",
					"p1", "(?:^|~)/BusinessPartnerRoot/Company/Name~\\*~/")
			},
			new Object[] {
				"Multiple values exact match",
				ExactMatchOperator.builder()
					.field("/BusinessPartnerRoot/SubtypeGroup/Company")
					.values(List.of("Technology", "Finance"))
					.caseSensitive(true).build(),
				STRING_FIELD_TYPE,
				null,
				"(st.search_data ~ :p0 AND st.search_data ~ :p1)",
				Map.of("p0", "~(?:Technology|Finance)~/",
					"p1", "(?:^|~)/BusinessPartnerRoot/SubtypeGroup/Company~(?:Technology|Finance)~/")
			},
			new Object[] {
				"Multiple values exact match with special chars",
				ExactMatchOperator.builder()
					.field("/BusinessPartnerRoot/SubtypeGroup/Company")
					.values(List.of("foo.bar", "$baz"))
					.caseSensitive(true).build(),
				STRING_FIELD_TYPE,
				null,
				"(st.search_data ~ :p0 AND st.search_data ~ :p1)",
				Map.of("p0", "~(?:foo\\.bar|\\$baz)~/",
					"p1", "(?:^|~)/BusinessPartnerRoot/SubtypeGroup/Company~(?:foo\\.bar|\\$baz)~/")
			},

		};
	}

	@Test(dataProvider = "exactMatchProvider")
	public void testExactMatch(String description, ExactMatchOperator<?> operator, String fieldType, String locale, String expectedExpression,
		Map<String, String> expectedParams) {

		QueryGeneratorContext generatorContext = prepareContext(locale);
		generatorContext.getEnrichments().getFieldDescriptor(operator.getField()).setFieldType(fieldType);

		StringBuilder sb = new StringBuilder();
		RegexSearchHelper.appendExactMatchCondition(sb, operator, "st", generatorContext);
		assertEquals(sb.toString(), expectedExpression);
		assertEquals(generatorContext.getParamHolder(), expectedParams);
	}

	@Test(dataProvider = "simpleSearchProvider")
	public void testAppendSearchExpression(String description, SimpleSearchOperator operator, String locale, String expectedExpression,
		Map<String, Object> expectedParams) {

		QueryGeneratorContext generatorContext = prepareContext(locale);

		StringBuilder sb = new StringBuilder();
		RegexSearchHelper.appendSimpleSearchCondition(sb, operator, "st", false, false, false, generatorContext);
		assertEquals(sb.toString(), expectedExpression);
		assertEquals(generatorContext.getParamHolder(), expectedParams);
	}

	@Test(dataProvider = "simpleSearchExcludingMetadataProvider")
	public void testSimpleSearchExcludingMetadataExpression(String description, SimpleSearchOperator operator, String locale, String expectedExpression,
		Map<String, Object> expectedParams) {
		QueryGeneratorContext generatorContext = prepareContext(locale);
		StringBuilder sb = new StringBuilder();
		RegexSearchHelper.appendSimpleSearchCondition(sb, operator, "st", false, false, true, generatorContext);

		assertEquals(sb.toString(), expectedExpression);
		assertEquals(generatorContext.getParamHolder(), expectedParams);
	}

	@Test(description = "ExactMatch must throw QueryInvalidInputException when field path has no leading slash")
	public void shouldThrowExceptionForExactMatchWhenFieldPathHasNoLeadingSlash() {
		ExactMatchOperator<?> withoutSlash = ExactMatchOperator.builder()
			.field(DocumentModelConstants.STATUS_FIELD_PATH)
			.value("draft")
			.build();
		QueryGeneratorContext context = prepareContext(null);
		StringBuilder sb = new StringBuilder();

		assertThrows(QueryInvalidInputException.class,
			() -> RegexSearchHelper.appendExactMatchCondition(sb, withoutSlash, "tableAlias", context));
	}

	@Test(description = "SimpleSearch must throw QueryInvalidInputException when field path has no leading slash")
	public void shouldThrowExceptionForSimpleSearchWhenFieldPathHasNoLeadingSlash() {
		SimpleSearchOperator withoutSlash = SimpleSearchOperator.builder()
			.value("draft")
			.fields(List.of(DocumentModelConstants.STATUS_FIELD_PATH))
			.build();
		QueryGeneratorContext context = prepareContext(null);
		StringBuilder sb = new StringBuilder();

		assertThrows(QueryInvalidInputException.class,
			() -> RegexSearchHelper.appendSimpleSearchCondition(sb, withoutSlash, "tableAlias", false, false, false, context));
	}

	@NotNull private QueryGeneratorContext prepareContext(String locale) {
		QueryContext queryContext = newQueryContext();
		queryContext.getEnrichments().computeModelLocale(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, k -> locale);
		QueryGeneratorContext generatorContext = queryGeneratorContextFactory.createContext(queryContext);
		generatorContext.setTargetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL);
		return generatorContext;
	}
}
