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
import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.constraint.range.DateRangeOperator;
import com.mgmtp.a12.dataservices.query.enrichement.Enrichments;
import com.mgmtp.a12.dataservices.query.enrichement.FieldDescriptor;
import com.mgmtp.a12.dataservices.query.validation.ValidationItem;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for DateRangeOperatorValidator.
 * Tests validation behavior with null parentDocumentModel to prevent NullPointerException.
 */
public class DateRangeOperatorValidatorTest {

	private DateRangeOperatorValidator validator;
	private DocumentModelUtils documentModelUtils;
	private QueryContext queryContext;
	private Enrichments enrichments;

	@BeforeMethod
	public void setUp() {
		documentModelUtils = mock(DocumentModelUtils.class);
		queryContext = mock(QueryContext.class);
		enrichments = mock(Enrichments.class);

		validator = new DateRangeOperatorValidator(documentModelUtils);

		// Setup default mock behavior
		when(queryContext.getEnrichments()).thenReturn(enrichments);
		when(enrichments.getFieldDescriptor(anyString())).thenReturn(mock(FieldDescriptor.class));
	}

	@Test(description = "Should return empty collection when validation is disabled")
	public void shouldReturnEmptyWhenValidationDisabled() {
		// Given
		DateRangeOperator operator = DateRangeOperator.builder()
			.field("/Fields/DateRanges/YearDateRange")
			.value("2000/2020")
			.build();
		String[] path = new String[]{"constraint"};

		// When
		Collection<ValidationItem> result = validator.validate(operator, "TestModel", path, queryContext, false);

		// Then
		assertTrue(result.isEmpty(), "Should return empty collection when validation is disabled");
		verify(queryContext, never()).getDocumentModel(anyString());
	}

	@Test(description = "Should return empty collection when operator is not DateRangeOperator")
	public void shouldReturnEmptyWhenOperatorIsNotDateRangeOperator() {
		// Given
		String[] path = new String[]{"constraint"};

		// When
		Collection<ValidationItem> result = validator.validate(mock(com.mgmtp.a12.dataservices.query.constraint.ILogicOperator.class),
			"TestModel", path, queryContext, true);

		// Then
		assertTrue(result.isEmpty(), "Should return empty collection for non-DateRangeOperator");
	}

	@Test(description = "Should return validation error when from, to, and value are all null")
	public void shouldReturnErrorWhenAllParametersAreNull() {
		// Given
		DateRangeOperator operator = DateRangeOperator.builder()
			.field("/Fields/DateRanges/YearDateRange")
			.build();
		String[] path = new String[]{"constraint"};
		when(queryContext.getOperatorName(operator)).thenReturn("date_range");

		// When
		Collection<ValidationItem> result = validator.validate(operator, "TestModel", path, queryContext, true);

		// Then
		assertEquals(result.size(), 1);
		ValidationItem item = result.iterator().next();
		assertFalse(item.valid(), "Should be invalid when all parameters are null");
		assertTrue(item.message().contains("Please provide `value` or `from` or `to`"),
			"Error message should indicate missing parameters");
	}

	@Test(description = "Should return empty collection when parentDocumentModel is null")
	public void shouldReturnEmptyWhenParentDocumentModelIsNull() {
		// Given
		DateRangeOperator operator = DateRangeOperator.builder()
			.field("/Fields/DateRanges/YearDateRange")
			.value("2000/2020")
			.build();
		String[] path = new String[]{"links", "[0]", "constraint"};

		// When
		Collection<ValidationItem> result = validator.validate(operator, null, path, queryContext, true);

		// Then
		assertTrue(result.isEmpty(),
			"Should return empty collection when parentDocumentModel is null");
		verify(queryContext, never()).getDocumentModel(any());
	}

	@Test(description = "Should skip validation when field is not found in document model")
	public void shouldSkipValidationWhenFieldNotFound() {
		// Given
		String modelName = "TestModel";
		DateRangeOperator operator = DateRangeOperator.builder()
			.field("/Fields/DateRanges/YearDateRange")
			.from("2000-01-01")
			.to("2020-12-31")
			.build();
		String[] path = new String[]{"constraint"};

		IDocumentModel documentModel = mock(IDocumentModel.class);

		when(queryContext.getDocumentModel(modelName)).thenReturn(documentModel);
		when(documentModelUtils.findField(documentModel, "/Fields/DateRanges/YearDateRange"))
			.thenReturn(Optional.empty());
		when(queryContext.getOperatorName(operator)).thenReturn("date_range");

		// When
		Collection<ValidationItem> result = validator.validate(operator, modelName, path, queryContext, true);

		// Then
		assertEquals(result.size(), 1);
		ValidationItem item = result.iterator().next();
		assertTrue(item.valid(), "Should be valid when field is not found (field validation handled elsewhere)");
		assertTrue(item.message().contains("Validation passed"), "Should contain success message");
	}

	@Test(description = "Should handle from parameter without throwing exception with null parentDocumentModel")
	public void shouldHandleFromParameterWithNullParentDocumentModel() {
		// Given
		DateRangeOperator operator = DateRangeOperator.builder()
			.field("/Fields/DateRanges/YearDateRange")
			.from("2000-01-01")
			.build();
		String[] path = new String[]{"links", "[0]", "constraint"};

		// When
		Collection<ValidationItem> result = validator.validate(operator, null, path, queryContext, true);

		// Then
		assertTrue(result.isEmpty(),
			"Should return empty collection without exception when parentDocumentModel is null");
		verify(queryContext, never()).getDocumentModel(any());
	}

	@Test(description = "Should handle to parameter without throwing exception with null parentDocumentModel")
	public void shouldHandleToParameterWithNullParentDocumentModel() {
		// Given
		DateRangeOperator operator = DateRangeOperator.builder()
			.field("/Fields/DateRanges/YearDateRange")
			.to("2020-12-31")
			.build();
		String[] path = new String[]{"links", "[0]", "constraint"};

		// When
		Collection<ValidationItem> result = validator.validate(operator, null, path, queryContext, true);

		// Then
		assertTrue(result.isEmpty(),
			"Should return empty collection without exception when parentDocumentModel is null");
		verify(queryContext, never()).getDocumentModel(any());
	}

	@Test(description = "Should handle both from and to parameters without throwing exception with null parentDocumentModel")
	public void shouldHandleBothParametersWithNullParentDocumentModel() {
		// Given
		DateRangeOperator operator = DateRangeOperator.builder()
			.field("/Fields/DateRanges/YearDateRange")
			.from("2000-01-01")
			.to("2020-12-31")
			.build();
		String[] path = new String[]{"links", "[0]", "constraint"};

		// When
		Collection<ValidationItem> result = validator.validate(operator, null, path, queryContext, true);

		// Then
		assertTrue(result.isEmpty(),
			"Should return empty collection without exception when parentDocumentModel is null");
		verify(queryContext, never()).getDocumentModel(any());
	}

	@Test(description = "Should pass validation when document model is valid even with complex field paths")
	public void shouldPassValidationWithComplexFieldPaths() {
		// Given
		String modelName = "TestModel";
		DateRangeOperator operator = DateRangeOperator.builder()
			.field("/Fields/Complex/Path/DateField")
			.from("2000-01-01")
			.build();
		String[] path = new String[]{"constraint"};

		IDocumentModel documentModel = mock(IDocumentModel.class);

		when(queryContext.getDocumentModel(modelName)).thenReturn(documentModel);
		when(documentModelUtils.findField(documentModel, "/Fields/Complex/Path/DateField"))
			.thenReturn(Optional.empty());
		when(queryContext.getOperatorName(operator)).thenReturn("date_range");

		// When
		Collection<ValidationItem> result = validator.validate(operator, modelName, path, queryContext, true);

		// Then
		assertEquals(result.size(), 1);
		ValidationItem item = result.iterator().next();
		assertTrue(item.valid(), "Should be valid when field is not found (field validation handled elsewhere)");
	}
}
