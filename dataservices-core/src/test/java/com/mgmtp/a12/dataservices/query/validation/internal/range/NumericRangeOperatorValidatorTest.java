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
package com.mgmtp.a12.dataservices.query.validation.internal.range;

import java.util.Collection;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.constraint.range.DoubleRangeOperator;
import com.mgmtp.a12.dataservices.query.validation.ValidationItem;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for NumericRangeOperatorValidator.
 * Tests validation behavior with null parentDocumentModel to prevent NullPointerException.
 */
public class NumericRangeOperatorValidatorTest {

	private NumericRangeOperatorValidator validator;
	private QueryContext queryContext;

	@BeforeMethod
	public void setUp() {
		validator = new NumericRangeOperatorValidator();
		queryContext = mock(QueryContext.class);
	}

	@Test(description = "Should return empty collection when validation is disabled")
	public void shouldReturnEmptyWhenValidationDisabled() {
		// Given
		DoubleRangeOperator operator = DoubleRangeOperator.builder()
			.field("TestField")
			.from(10.0)
			.to(20.0)
			.build();
		String[] path = new String[]{"constraint"};

		// When
		Collection<ValidationItem> result = validator.validate(operator, "TestModel", path, queryContext, false);

		// Then
		assertTrue(result.isEmpty(), "Should return empty collection when validation is disabled");
	}

	@Test(description = "Should return empty collection when operator is not NumericRangeOperator")
	public void shouldReturnEmptyWhenOperatorIsNotNumericRangeOperator() {
		// Given
		String[] path = new String[]{"constraint"};

		// When
		Collection<ValidationItem> result = validator.validate(mock(com.mgmtp.a12.dataservices.query.constraint.ILogicOperator.class),
			"TestModel", path, queryContext, true);

		// Then
		assertTrue(result.isEmpty(), "Should return empty collection for non-NumericRangeOperator");
	}

	@Test(description = "Should return validation error when both from and to are null")
	public void shouldReturnErrorWhenBothFromAndToAreNull() {
		// Given
		DoubleRangeOperator operator = DoubleRangeOperator.builder()
			.field("TestField")
			.build();
		String[] path = new String[]{"constraint"};
		when(queryContext.getOperatorName(operator)).thenReturn("double_range");

		// When
		Collection<ValidationItem> result = validator.validate(operator, "TestModel", path, queryContext, true);

		// Then
		assertEquals(result.size(), 1);
		ValidationItem item = result.iterator().next();
		assertFalse(item.valid(), "Should be invalid when both from and to are null");
		assertTrue(item.message().contains("Please provide `from` or `to` or both"),
			"Error message should indicate missing parameters");
	}

	@Test(description = "Should return validation error when from is greater than to")
	public void shouldReturnErrorWhenFromIsGreaterThanTo() {
		// Given
		DoubleRangeOperator operator = DoubleRangeOperator.builder()
			.field("TestField")
			.from(100.0)
			.to(50.0)
			.build();
		String[] path = new String[]{"constraint"};
		when(queryContext.getOperatorName(operator)).thenReturn("double_range");

		// When
		Collection<ValidationItem> result = validator.validate(operator, "TestModel", path, queryContext, true);

		// Then
		assertEquals(result.size(), 1);
		ValidationItem item = result.iterator().next();
		assertFalse(item.valid(), "Should be invalid when from is greater than to");
		assertTrue(item.message().contains("`from` cannot be bigger than `to`"),
			"Error message should indicate from/to order issue");
	}

	@Test(description = "Should return empty collection when parentDocumentModel is null")
	public void shouldReturnEmptyWhenParentDocumentModelIsNull() {
		// Given
		DoubleRangeOperator operator = DoubleRangeOperator.builder()
			.field("TestField")
			.from(10.0)
			.to(20.0)
			.build();
		String[] path = new String[]{"links", "[0]", "constraint"};

		// When
		Collection<ValidationItem> result = validator.validate(operator, null, path, queryContext, true);

		// Then
		assertTrue(result.isEmpty(),
			"Should return empty collection when parentDocumentModel is null");
	}

	@Test(description = "Should validate successfully when valid parentDocumentModel and operator are provided")
	public void shouldValidateSuccessfullyWhenValidInputsProvided() {
		// Given
		DoubleRangeOperator operator = DoubleRangeOperator.builder()
			.field("TestField")
			.from(10.0)
			.to(20.0)
			.build();
		String[] path = new String[]{"constraint"};
		when(queryContext.getOperatorName(operator)).thenReturn("double_range");

		// When
		Collection<ValidationItem> result = validator.validate(operator, "TestModel", path, queryContext, true);

		// Then
		assertEquals(result.size(), 1);
		ValidationItem item = result.iterator().next();
		assertTrue(item.valid(), "Should be valid for correct operator and model");
		assertTrue(item.message().contains("Validation passed"), "Should contain success message");
	}

	@Test(description = "Should validate successfully with only from parameter")
	public void shouldValidateSuccessfullyWithOnlyFromParameter() {
		// Given
		DoubleRangeOperator operator = DoubleRangeOperator.builder()
			.field("TestField")
			.from(10.0)
			.build();
		String[] path = new String[]{"constraint"};
		when(queryContext.getOperatorName(operator)).thenReturn("double_range");

		// When
		Collection<ValidationItem> result = validator.validate(operator, "TestModel", path, queryContext, true);

		// Then
		assertEquals(result.size(), 1);
		ValidationItem item = result.iterator().next();
		assertTrue(item.valid(), "Should be valid with only from parameter");
	}

	@Test(description = "Should validate successfully with only to parameter")
	public void shouldValidateSuccessfullyWithOnlyToParameter() {
		// Given
		DoubleRangeOperator operator = DoubleRangeOperator.builder()
			.field("TestField")
			.to(20.0)
			.build();
		String[] path = new String[]{"constraint"};
		when(queryContext.getOperatorName(operator)).thenReturn("double_range");

		// When
		Collection<ValidationItem> result = validator.validate(operator, "TestModel", path, queryContext, true);

		// Then
		assertEquals(result.size(), 1);
		ValidationItem item = result.iterator().next();
		assertTrue(item.valid(), "Should be valid with only to parameter");
	}

	@Test(description = "Should handle DoubleRangeOperator with null parentDocumentModel and decimal values")
	public void shouldHandleDoubleRangeOperatorWithNullParentDocumentModelAndDecimalValues() {
		// Given
		DoubleRangeOperator operator = DoubleRangeOperator.builder()
			.field("TestField")
			.from(10.5)
			.to(20.5)
			.build();
		String[] path = new String[]{"links", "[0]", "constraint"};

		// When
		Collection<ValidationItem> result = validator.validate(operator, null, path, queryContext, true);

		// Then
		assertTrue(result.isEmpty(),
			"Should return empty collection for DoubleRangeOperator with null parentDocumentModel");
	}

	@Test(description = "Should handle decimal comparison correctly for DoubleRangeOperator")
	public void shouldHandleDecimalComparisonCorrectlyForDoubleRangeOperator() {
		// Given
		DoubleRangeOperator operator = DoubleRangeOperator.builder()
			.field("TestField")
			.from(10.999)
			.to(10.001)
			.build();
		String[] path = new String[]{"constraint"};
		when(queryContext.getOperatorName(operator)).thenReturn("double_range");

		// When
		Collection<ValidationItem> result = validator.validate(operator, "TestModel", path, queryContext, true);

		// Then
		assertEquals(result.size(), 1);
		ValidationItem item = result.iterator().next();
		assertFalse(item.valid(), "Should be invalid when from is greater than to (decimal)");
	}

	@Test(description = "Should return empty with null model even when from and to are null")
	public void shouldReturnEmptyWithNullModelEvenWhenParametersAreNull() {
		// Given
		DoubleRangeOperator operator = DoubleRangeOperator.builder()
			.field("TestField")
			.build();
		String[] path = new String[]{"links", "[0]", "constraint"};

		// When
		Collection<ValidationItem> result = validator.validate(operator, null, path, queryContext, true);

		// Then
		assertTrue(result.isEmpty(),
			"Should return empty collection when parentDocumentModel is null, even if parameters are missing");
	}

	@Test(description = "Should return empty with null model even when from is greater than to")
	public void shouldReturnEmptyWithNullModelEvenWhenFromIsGreaterThanTo() {
		// Given
		DoubleRangeOperator operator = DoubleRangeOperator.builder()
			.field("TestField")
			.from(100.0)
			.to(50.0)
			.build();
		String[] path = new String[]{"links", "[0]", "constraint"};

		// When
		Collection<ValidationItem> result = validator.validate(operator, null, path, queryContext, true);

		// Then
		assertTrue(result.isEmpty(),
			"Should return empty collection when parentDocumentModel is null, even if from > to");
	}
}
