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
package com.mgmtp.a12.dataservices.query.validation.internal.matching;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.validation.ValidationItem;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for `ExactMatchOperatorValidator`.
 */
public class ExactMatchOperatorValidatorTest {

	private ExactMatchOperatorValidator validator;
	private QueryContext queryContext;
	private DataServicesCoreProperties dataServicesCoreProperties;

	@BeforeMethod
	public void setUp() {
		dataServicesCoreProperties = new DataServicesCoreProperties();
		validator = new ExactMatchOperatorValidator(dataServicesCoreProperties);
		queryContext = mock(QueryContext.class);
	}

	@Test(description = "Should return empty collection when validation is disabled")
	public void shouldReturnEmptyWhenValidationDisabled() {
		// Given
		ExactMatchOperator<String> operator = ExactMatchOperator.<String>builder()
			.field("/Fields/sport")
			.value("Football")
			.build();
		String[] path = new String[]{"constraint"};

		// When
		Collection<ValidationItem> result = validator.validate(operator, "TestModel", path, queryContext, false);

		// Then
		assertTrue(result.isEmpty(), "Should return empty collection when validation is disabled");
	}

	@Test(description = "Should return error when neither value nor values are provided")
	public void shouldReturnErrorWhenNeitherValueNorValuesProvided() {
		// Given
		ExactMatchOperator<String> operator = ExactMatchOperator.<String>builder()
			.field("/Fields/sport")
			.build();
		String[] path = new String[]{"constraint"};
		when(queryContext.getOperatorName(operator)).thenReturn("exact_match");

		// When
		Collection<ValidationItem> result = validator.validate(operator, "TestModel", path, queryContext, true);

		// Then
		assertEquals(result.size(), 1);
		ValidationItem item = result.iterator().next();
		assertFalse(item.valid(), "Should be invalid when no value or values are provided");
		assertTrue(item.message().contains("Please provide value or values for exact_match operator."),
			"Error message should indicate missing value or values");
	}

	@Test(description = "Should return error when both value and values are provided")
	public void shouldReturnErrorWhenBothValueAndValuesProvided() {
		// Given
		ExactMatchOperator<String> operator = ExactMatchOperator.<String>builder()
			.field("/Fields/sport")
			.value("Football")
			.values(List.of("Basketball", "Tennis"))
			.build();
		String[] path = new String[]{"constraint"};
		when(queryContext.getOperatorName(operator)).thenReturn("exact_match");

		// When
		Collection<ValidationItem> result = validator.validate(operator, "TestModel", path, queryContext, true);

		// Then
		assertEquals(result.size(), 1);
		ValidationItem item = result.iterator().next();
		assertFalse(item.valid(), "Should be invalid when both value and values are provided");
		assertTrue(item.message().contains("Please provide either value or values, not both, for exact_match operator."),
			"Error message should indicate mutual exclusivity violation");
	}

	@Test(description = "Should return error when values list contains a null entry")
	public void shouldReturnErrorWhenValuesContainsNullEntry() {
		// Given
		ExactMatchOperator<String> operator = ExactMatchOperator.<String>builder()
			.field("/Fields/sport")
			.values(Arrays.asList("Football", null, "Tennis"))
			.build();
		String[] path = new String[]{"constraint"};
		when(queryContext.getOperatorName(operator)).thenReturn("exact_match");

		// When
		Collection<ValidationItem> result = validator.validate(operator, "TestModel", path, queryContext, true);

		// Then
		assertEquals(result.size(), 1);
		ValidationItem item = result.iterator().next();
		assertFalse(item.valid(), "Should be invalid when values list contains null");
		assertTrue(item.message().contains("Please ensure none of the values are null for exact_match operator."),
			"Error message should indicate null entry in values");
	}

	@Test(description = "Should return error when values list exceeds maxValuesCount")
	public void shouldReturnErrorWhenValuesCountExceedsMaxValuesCount() {
		// Given
		dataServicesCoreProperties.getQuery().getExactMatch().setMaxValuesCount(2);
		ExactMatchOperator<String> operator = ExactMatchOperator.<String>builder()
			.field("/Fields/sport")
			.values(List.of("Football", "Basketball", "Tennis"))
			.build();
		String[] path = new String[]{"constraint"};
		when(queryContext.getOperatorName(operator)).thenReturn("exact_match");

		// When
		Collection<ValidationItem> result = validator.validate(operator, "TestModel", path, queryContext, true);

		// Then
		assertEquals(result.size(), 1);
		ValidationItem item = result.iterator().next();
		assertFalse(item.valid(), "Should be invalid when values count exceeds maxValuesCount");
		assertTrue(item.message().contains("Please reduce the number of values to a value lower than or equal to 2 for the exact_match operator."),
			"Error message should indicate values count exceeds limit");
	}

	@Test(description = "Should pass validation when a single value is provided")
	public void shouldPassValidationWhenSingleValueProvided() {
		// Given
		ExactMatchOperator<String> operator = ExactMatchOperator.<String>builder()
			.field("/Fields/sport")
			.value("Football")
			.build();
		String[] path = new String[]{"constraint"};
		when(queryContext.getOperatorName(operator)).thenReturn("exact_match");

		// When
		Collection<ValidationItem> result = validator.validate(operator, "TestModel", path, queryContext, true);

		// Then
		assertEquals(result.size(), 1);
		ValidationItem item = result.iterator().next();
		assertTrue(item.valid(), "Should be valid when a single value is provided");
		assertTrue(item.message().contains("Validation passed for operator exact_match"),
			"Should contain success message");
	}

	@Test(description = "Should pass validation when a valid values list is provided")
	public void shouldPassValidationWhenValidValuesListProvided() {
		// Given
		ExactMatchOperator<String> operator = ExactMatchOperator.<String>builder()
			.field("/Fields/sport")
			.values(List.of("Football", "Basketball", "Tennis"))
			.build();
		String[] path = new String[]{"constraint"};
		when(queryContext.getOperatorName(operator)).thenReturn("exact_match");

		// When
		Collection<ValidationItem> result = validator.validate(operator, "TestModel", path, queryContext, true);

		// Then
		assertEquals(result.size(), 1);
		ValidationItem item = result.iterator().next();
		assertTrue(item.valid(), "Should be valid when a non-empty values list with no nulls is provided");
		assertTrue(item.message().contains("Validation passed for operator exact_match"),
			"Should contain success message");
	}
}
