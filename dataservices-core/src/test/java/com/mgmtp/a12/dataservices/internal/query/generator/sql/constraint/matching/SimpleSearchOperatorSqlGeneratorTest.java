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

import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.internal.query.generator.sql.constraint.AbstractSqlGeneratorTest;
import com.mgmtp.a12.dataservices.query.constraint.matching.SimpleSearchOperator;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.constraint.internal.matching.RegexSearchHelper;
import com.mgmtp.a12.dataservices.query.generator.sql.constraint.internal.matching.SimpleSearchOperatorSqlGenerator;

public class SimpleSearchOperatorSqlGeneratorTest extends AbstractSqlGeneratorTest {

	@InjectMocks private SimpleSearchOperatorSqlGenerator simpleSearchOperatorSqlGenerator;

	@BeforeClass
	void beforeClassSetup() {
		dataServicesCoreProperties.getQuery().getSimpleSearch().setMaxInputValueLength(10);
	}

	@AfterClass
	void afterClassSetUp() {
		dataServicesCoreProperties.getQuery().getSimpleSearch().setMaxInputValueLength(100);
	}

	@Test(expectedExceptions = QueryInvalidInputException.class, expectedExceptionsMessageRegExp = "Please reduce the input value length to a value lower than 10 for the simple_search operator.")
	void testRenderCondition_throwErrorWhenReachMaxLength() {
		StringBuilder sb = new StringBuilder();
		QueryGeneratorContext queryGeneratorContext = Mockito.mock(QueryGeneratorContext.class);
		SimpleSearchOperator simpleSearchOperator = SimpleSearchOperator.builder()
			.value("valueLongLongLong")
			.build();
		simpleSearchOperatorSqlGenerator.renderCondition(sb, simpleSearchOperator, queryGeneratorContext);
	}

	@Test(expectedExceptions = QueryInvalidInputException.class, expectedExceptionsMessageRegExp = "Please reduce the input value length to a value lower than 10 for the simple_search operator.")
	void testRenderCondition_throwErrorWhenValuesReachMaxLength() {
		StringBuilder sb = new StringBuilder();
		QueryGeneratorContext queryGeneratorContext = Mockito.mock(QueryGeneratorContext.class);
		SimpleSearchOperator simpleSearchOperator = SimpleSearchOperator.builder()
			.value("value")
			.values(List.of("valueLongLongLong"))
			.build();
		simpleSearchOperatorSqlGenerator.renderCondition(sb, simpleSearchOperator, queryGeneratorContext);
	}

	@Test void testRenderCondition_success_shouldCallRegexGenerator() {
		StringBuilder sb = new StringBuilder();
		QueryGeneratorContext queryGeneratorContext = Mockito.spy(QueryGeneratorContext.class);
		SimpleSearchOperator simpleSearchOperator = SimpleSearchOperator.builder()
			.value("value").build();

		try (MockedStatic<RegexSearchHelper> mockedRegexSearchHelper = Mockito.mockStatic(RegexSearchHelper.class)) {
			simpleSearchOperatorSqlGenerator.renderCondition(sb, simpleSearchOperator, queryGeneratorContext);
			mockedRegexSearchHelper.verify(() -> RegexSearchHelper.appendSimpleSearchCondition(
				Mockito.eq(sb),
				Mockito.eq(simpleSearchOperator),
				Mockito.any(),
				Mockito.eq(false),
				Mockito.eq(false),
				Mockito.eq(false),
				Mockito.eq(queryGeneratorContext)
			));
		}
	}
}
