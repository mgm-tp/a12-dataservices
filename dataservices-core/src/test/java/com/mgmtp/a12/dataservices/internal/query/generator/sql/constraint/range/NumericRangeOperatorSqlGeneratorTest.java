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

import com.mgmtp.a12.dataservices.internal.query.generator.sql.constraint.AbstractSqlGeneratorTest;
import com.mgmtp.a12.dataservices.query.constraint.range.DoubleRangeOperator;
import com.mgmtp.a12.dataservices.query.enrichement.Enrichments;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.constraint.range.internal.NumericRangeOperatorSqlGenerator;
import com.mgmtp.a12.dataservices.query.internal.DefaultQueryContext;

import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

public class NumericRangeOperatorSqlGeneratorTest extends AbstractSqlGeneratorTest {

	private final NumericRangeOperatorSqlGenerator<DoubleRangeOperator, Double> generator = spy(new NumericRangeOperatorSqlGenerator<>());

	@DataProvider public static Object[][] operatorProvider() {
		return new Object[][] {
			new Object[] { "Two value range", DoubleRangeOperator.builder()
				.field("/ContractRoot/ChangeLog/Number")
				.from(24.0)
				.to(42.0)
				.build(),
				"EXISTS (SELECT * FROM document_fields AS document_fields WHERE document_fields.field_name = :p0 AND target_document.doc_ref = document_fields.doc_ref AND document_fields.source = :p1 AND (document_fields.number_value >= :p2 :: NUMERIC AND document_fields.number_value <= :p3 :: NUMERIC))",
				Map.of("p0", "/ContractRoot/ChangeLog/Number",
					"p1", "core",
					"p2", "24.0",
					"p3", "42.0"),
			},
			new Object[] { "Lower bound range", DoubleRangeOperator.builder()
				.field("/ContractRoot/ChangeLog/Number")
				.from(42.0)
				.build(),
				"EXISTS (SELECT * FROM document_fields AS document_fields WHERE document_fields.field_name = :p0 AND target_document.doc_ref = document_fields.doc_ref AND document_fields.source = :p1 AND (document_fields.number_value >= :p2 :: NUMERIC))",
				Map.of("p0", "/ContractRoot/ChangeLog/Number",
					"p1", "core",
					"p2", "42.0"),
			},
			new Object[] { "Upper bound range range", DoubleRangeOperator.builder()
				.field("/ContractRoot/ChangeLog/Number")
				.to(42.0)
				.build(),
				"EXISTS (SELECT * FROM document_fields AS document_fields WHERE document_fields.field_name = :p0 AND target_document.doc_ref = document_fields.doc_ref AND document_fields.source = :p1 AND (document_fields.number_value <= :p2 :: NUMERIC))",
				Map.of("p0", "/ContractRoot/ChangeLog/Number",
					"p1", "core",
					"p2", "42.0"),
			},
		};
	}

	@Test(dataProvider = "operatorProvider")
	public void testRenderCondition(String description, DoubleRangeOperator op, String expectation, Map<String, Object> expectedParams) {

		DefaultQueryContext queryContext = newQueryContext();
		queryContext.getEnrichments().getOperatorEnrichment(op).put(Enrichments.FROM_PROPERTY, op.getFrom());
		queryContext.getEnrichments().getOperatorEnrichment(op).put(Enrichments.TO_PROPERTY, op.getTo());
		QueryGeneratorContext generatorContext = queryGeneratorContextFactory.createContext(queryContext);
		assertEquals(generator.renderCondition(new StringBuilder(), op, generatorContext).toString(), expectation);
		assertEquals(generatorContext.getParamHolder(), expectedParams);
	}
}
