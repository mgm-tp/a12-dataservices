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

import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.internal.query.generator.sql.constraint.AbstractSqlGeneratorTest;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.enrichement.Enrichments;
import com.mgmtp.a12.dataservices.query.enrichement.FieldDescriptor;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.constraint.internal.matching.ExactMatchOperatorSqlGenerator;
import com.mgmtp.a12.dataservices.query.internal.DefaultQueryContext;

import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.DATE_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.DATE_FRAGMENT_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.ENUMERATION_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.NUMBER_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.STRING_FIELD_TYPE;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

public class ExactMatchOperatorSqlGeneratorTest extends AbstractSqlGeneratorTest {

	private final ExactMatchOperatorSqlGenerator generator = spy(new ExactMatchOperatorSqlGenerator(dataServicesCoreProperties));

	@DataProvider public static Object[][] operatorProvider() {
		return new Object[][] {
			new Object[] { "Enum with language",
				ExactMatchOperator.<String>builder()
					.field("/ContractRoot/Type")
					.value("IT")
					.build(),
				ENUMERATION_FIELD_TYPE,
				"IT",
				"en"
				, "(target_document.search_data ~ :p0 AND target_document.search_data ~ :p1)",
				Map.of("p0", "~IT~/",
					"p1", "(?:^|~)/ContractRoot/Type\\[en\\]~IT~/"),
			},
			new Object[] { "Enum without language",
				ExactMatchOperator.<String>builder()
					.field("/ContractRoot/Type")
					.value("IT")
					.build(),
				ENUMERATION_FIELD_TYPE,
				"IT",
				null
				, "(target_document.search_data ~ :p0 AND target_document.search_data ~ :p1)",
				Map.of("p0", "~IT~/",
					"p1", "(?:^|~)/ContractRoot/Type~IT~/"),
			},
			new Object[] { "String",
				ExactMatchOperator.<String>builder()
					.field("/ContractRoot/Name")
					.value("Insurance")
					.build(),
				STRING_FIELD_TYPE,
				"Insurance",
				null
				, "(target_document.original_value @> :p0 :: JSONB)",
				Map.of("p0", "{ \"ContractRoot\" : { \"Name\" : \"Insurance\"}}"),
			},
			new Object[] { "Quoted String",
				ExactMatchOperator.<String>builder()
					.field("/ContractRoot/Name")
					.value("\"*\"")
					.build(),
				STRING_FIELD_TYPE,
				"\"*\"",
				null
				, "(target_document.original_value @> :p0 :: JSONB)",
				Map.of("p0", "{ \"ContractRoot\" : { \"Name\" : \"\\\"*\\\"\"}}"),
			},
			new Object[] { "Repeatable String",
				ExactMatchOperator.<String>builder()
					.field("/ContractRoot/ChangeLog/User")
					.value("Arthur Dent")
					.build(),
				STRING_FIELD_TYPE,
				"Arthur Dent",
				null
				, "(target_document.search_data ~ :p0 AND target_document.search_data ~ :p1)",
				Map.of("p0", "~Arthur Dent~/",
					"p1", "(?:^|~)/ContractRoot/ChangeLog/User~Arthur Dent~/"),
			},
			new Object[] { "Number",
				ExactMatchOperator.<String>builder()
					.field("/ContractRoot/Price")
					.value("123.56")
					.build(),
				NUMBER_FIELD_TYPE,
				"123.56",
				null
				, "(target_document.search_data ~ :p0 AND target_document.search_data ~ :p1)",
				Map.of("p0", "~123\\.56~/",
					"p1", "(?:^|~)/ContractRoot/Price~123\\.56~/"),
			},
			new Object[] { "Date",
				ExactMatchOperator.<String>builder()
					.field("/ContractRoot/Date")
					.value("25.1.2025 16:45:28")
					.build(),
				DATE_FIELD_TYPE,
				"2025-01-25T16:45:28.000Z",
				null
				, "(target_document.original_value @> :p0 :: JSONB)",
				Map.of("p0", "{ \"ContractRoot\" : { \"Date\" : \"25.1.2025 16:45:28\"}}"),
			},
			new Object[] { "DateFragment",
				ExactMatchOperator.<String>builder()
					.field("/ContractRoot/DateFragment")
					.value("01-25")
					.build(),
				DATE_FRAGMENT_FIELD_TYPE,
				"2000-01-25T00:00:00.000Z",
				null,
				"(target_document.original_value @> :p0 :: JSONB)",
				Map.of("p0", "{ \"ContractRoot\" : { \"DateFragment\" : \"01-25\"}}"),
			}
		};
	}

	@Test(dataProvider = "operatorProvider")
	public <T> void testName(String description, ExactMatchOperator<T> op, String type, String value, String lang, String expectation,
		Map<String, Object> expectedParams) {

		new DefaultQueryContext(documentModelResolver, relationshipModelLoader, queryMethod, documentModelServiceFactory, queryContextHelper, indexedModelFieldCache, null, null);
		QueryGeneratorContext generatorContext = spy(queryGeneratorContextFactory.createContext(newQueryContext()));

		generatorContext.getEnrichments().setFieldDescriptor("/ContractRoot/ChangeLog/User", fd(STRING_FIELD_TYPE, true));
		generatorContext.getEnrichments().setFieldDescriptor("/ContractRoot/Type", fd(ENUMERATION_FIELD_TYPE, false));
		generatorContext.getEnrichments().setFieldDescriptor("/ContractRoot/Name", fd(STRING_FIELD_TYPE, false));
		generatorContext.getEnrichments().setFieldDescriptor("/ContractRoot/Price", fd(NUMBER_FIELD_TYPE, false));
		generatorContext.getEnrichments().setFieldDescriptor("/ContractRoot/Date", fd(DATE_FIELD_TYPE, false));
		generatorContext.getEnrichments().setFieldDescriptor("/ContractRoot/DateFragment", fd(DATE_FRAGMENT_FIELD_TYPE, false));
		generatorContext.getEnrichments().getOperatorEnrichment(op).put(Enrichments.VALUE_PROPERTY, value);
		generatorContext.getEnrichments().computeModelLocale(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, k -> lang);
		generatorContext.setTargetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL);

		assertEquals(generator.renderCondition(new StringBuilder(), op, generatorContext).toString(), expectation);
		assertEquals(generatorContext.getParamHolder(), expectedParams);
	}

	@Test(expectedExceptions = QueryInvalidInputException.class, expectedExceptionsMessageRegExp = "Please reduce the input value length to a value lower than 10 for the exact_match operator.")
	void testRenderCondition_throwErrorWhenReachMaxLength() {
		dataServicesCoreProperties.getQuery().getExactMatch().setMaxInputValueLength(10);
		StringBuilder sb = new StringBuilder();
		QueryGeneratorContext queryGeneratorContext = Mockito.mock(QueryGeneratorContext.class);
		ExactMatchOperator exactMatchOperator = ExactMatchOperator.builder()
			.value("valueLongLongLong")
			.build();
		generator.renderCondition(sb, exactMatchOperator, queryGeneratorContext);
		dataServicesCoreProperties.getQuery().getExactMatch().setMaxInputValueLength(100);
	}

	private static FieldDescriptor fd(String type, boolean repeatable) {
		return FieldDescriptor.builder().fieldType(type).repeatable(repeatable).build();
	}
}
