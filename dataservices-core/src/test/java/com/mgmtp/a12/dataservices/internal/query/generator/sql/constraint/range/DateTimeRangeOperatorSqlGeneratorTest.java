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
package com.mgmtp.a12.dataservices.internal.query.generator.sql.constraint.range;

import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.exception.query.QueryException;
import com.mgmtp.a12.dataservices.internal.query.generator.sql.constraint.AbstractSqlGeneratorTest;
import com.mgmtp.a12.dataservices.query.constraint.range.DateRangeOperator;
import com.mgmtp.a12.dataservices.query.enrichement.Enrichments;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.constraint.range.internal.DateTimeRangeOperatorSqlGenerator;
import com.mgmtp.a12.dataservices.query.internal.DefaultQueryContext;

import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.DATE_RANGE_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.DATE_TIME_FIELD_TYPE;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

public class DateTimeRangeOperatorSqlGeneratorTest extends AbstractSqlGeneratorTest {

	private final DateTimeRangeOperatorSqlGenerator generator = spy(new DateTimeRangeOperatorSqlGenerator());

	@DataProvider public static Object[][] operatorProvider() {
		return new Object[][] {
			new Object[] { "From -> To", DateRangeOperator.builder()
				.field("/__meta/createdAt")
				.from("2025-01-01T13:52:43")
				.to("2025-04-30T16:32:00")
				.build(),
				DATE_TIME_FIELD_TYPE,
				"en",
				"EXISTS (SELECT * FROM document_fields AS document_fields WHERE document_fields.field_name = :p0 AND target_document.doc_ref = document_fields.doc_ref AND document_fields.source = :p1 AND (document_fields.timestamp_value >= :p2 :: timestamp AND document_fields.timestamp_value <= :p3 :: timestamp))",
				Map.of("p0", "/__meta/createdAt",
					"p1", "core",
					"p2", "2025-01-01T13:52:43",
					"p3", "2025-04-30T16:32:00"),
			},

			new Object[] { "Only from value", DateRangeOperator.builder()
				.field("/__meta/createdAt")
				.from("2025-01-01T13:52:43")
				.build(),
				DATE_TIME_FIELD_TYPE,
				"en",
				"EXISTS (SELECT * FROM document_fields AS document_fields WHERE document_fields.field_name = :p0 AND target_document.doc_ref = document_fields.doc_ref AND document_fields.source = :p1 AND (document_fields.timestamp_value >= :p2 :: timestamp))",
				Map.of("p0", "/__meta/createdAt",
					"p1", "core",
					"p2", "2025-01-01T13:52:43"),
			},

			new Object[] { "Range containment", DateRangeOperator.builder().rangeType(true)
				.field("/__meta/createdAt")
				.from("2025-01-01T13:52:43")
				.to("2025-04-25T16:32:00")
				.build(),
				DATE_RANGE_FIELD_TYPE,
				"en",
				"EXISTS (SELECT * FROM document_fields AS document_fields WHERE document_fields.field_name = :p0 AND target_document.doc_ref = document_fields.doc_ref AND document_fields.source = :p1 AND (tsrange(:p2 :: timestamp, :p3 :: timestamp, '[]') @> document_fields.ts_range_value))",
				Map.of("p0", "/__meta/createdAt",
					"p1", "core",
					"p2", "2025-01-01T13:52:43",
					"p3", "2025-04-25T16:32:00"),
			},

			new Object[] { "Range containment", DateRangeOperator.builder().rangeType(true)
				.field("/__meta/createdAt")
				.value("2025-01-01T13:52:43")
				.reverse(true)
				.build(),
				DATE_RANGE_FIELD_TYPE,
				"en",
				"EXISTS (SELECT * FROM document_fields AS document_fields WHERE document_fields.field_name = :p0 AND target_document.doc_ref = document_fields.doc_ref AND document_fields.source = :p1 AND (:p2 :: timestamp <@ document_fields.ts_range_value))",
				Map.of("p0", "/__meta/createdAt",
					"p1", "core",
					"p2", "2025-01-01T13:52:43"),
			},
		};
	}

	@Test(dataProvider = "operatorProvider")
	public void testRenderCondition(String description, DateRangeOperator op, String type, String locale, String expectation,
		Map<String, Object> expectedParams) {

		DefaultQueryContext queryContext = newQueryContext();
		queryContext.getEnrichments().computeModelLocale(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, k -> locale);
		queryContext.getEnrichments().getFieldDescriptor(op.getField()).setFieldType(type);
		queryContext.getEnrichments().getOperatorEnrichment(op).put(Enrichments.FROM_PROPERTY, op.getFrom());
		queryContext.getEnrichments().getOperatorEnrichment(op).put(Enrichments.TO_PROPERTY, op.getTo());
		queryContext.getEnrichments().getOperatorEnrichment(op).put(Enrichments.VALUE_PROPERTY, op.getValue());

		QueryGeneratorContext generatorContext = queryGeneratorContextFactory.createContext(queryContext);
		assertEquals(generator.renderCondition(new StringBuilder(), op, generatorContext).toString(), expectation);
		assertEquals(generatorContext.getParamHolder(), expectedParams);
	}

	@Test(expectedExceptions = QueryException.class, expectedExceptionsMessageRegExp = "For a range operator you can specify either \"from\" and \"to\" or \"value\", but not both.")
	public void shouldThrowQueryExceptionWhenMixingValueWithFromTo() {
		DateRangeOperator op = DateRangeOperator.builder().rangeType(true)
			.field("/__meta/createdAt")
			.from("2025-01-01T13:52:43")
			.to("2025-04-25T16:32:00")
			.value("2025-01-01T13:52:43")
			.reverse(true)
			.build();
		DefaultQueryContext queryContext = newQueryContext();
		queryContext.getEnrichments().getOperatorEnrichment(op).put(Enrichments.FROM_PROPERTY, op.getFrom());
		queryContext.getEnrichments().getOperatorEnrichment(op).put(Enrichments.TO_PROPERTY, op.getTo());
		queryContext.getEnrichments().getOperatorEnrichment(op).put(Enrichments.VALUE_PROPERTY, op.getValue());

		QueryGeneratorContext generatorContext = queryGeneratorContextFactory.createContext(queryContext);
		generator.renderCondition(new StringBuilder(), op, generatorContext);
	}

	@Test(expectedExceptions = QueryException.class, expectedExceptionsMessageRegExp = "By value you can search in range type only with reverse enabled.")
	public void shouldThrowQueryExceptionWhenIsRangeTypeAndIsNotReverse() {
		DateRangeOperator op = DateRangeOperator.builder().rangeType(true)
			.field("/__meta/createdAt")
			.value("2025-01-01T13:52:43")
			.build();
		DefaultQueryContext queryContext = newQueryContext();
		queryContext.getEnrichments().getOperatorEnrichment(op).put(Enrichments.VALUE_PROPERTY, op.getValue());

		QueryGeneratorContext generatorContext = queryGeneratorContextFactory.createContext(queryContext);
		generator.renderCondition(new StringBuilder(), op, generatorContext);
	}
}
