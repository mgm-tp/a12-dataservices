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
package com.mgmtp.a12.dataservices.query.validation.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.constraint.logical.AndOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.enrichement.Enrichments;
import com.mgmtp.a12.dataservices.query.enrichement.FieldDescriptor;
import com.mgmtp.a12.dataservices.query.fields.ProjectionField;
import com.mgmtp.a12.dataservices.query.fields.aggregation.AggregationProjector;
import com.mgmtp.a12.dataservices.query.fields.aggregation.function.Count;
import com.mgmtp.a12.dataservices.query.fields.aggregation.function.Sum;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;

import lombok.extern.slf4j.Slf4j;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for FieldsValidator.
 *  * Tests validation behavior with null targetDocumentModel for QueryLink and has operator scenarios.
 *
 */
@Slf4j
public class FieldsValidatorTest {

	private FieldsValidator fieldsValidator;
	private DocumentModelUtils documentModelUtils;
	private QueryContext queryContext;
	private Enrichments queryEnrichments;
	private ValidationResult validationResult;

	@BeforeMethod
	public void setUp() {
		documentModelUtils = mock(DocumentModelUtils.class);
		ModelTypeService modelTypeService = mock(ModelTypeService.class);
		queryContext = mock(QueryContext.class);
		queryEnrichments = mock(Enrichments.class);
		validationResult = new ValidationResult();

		fieldsValidator = new FieldsValidator(documentModelUtils, modelTypeService);

		// Setup default mock behavior
		when(queryContext.getEnrichments()).thenReturn(queryEnrichments);
		// Return mutable set for getModelSubtypes
		when(queryEnrichments.getModelSubtypes(anyString())).thenAnswer(invocation -> new HashSet<>());
		when(modelTypeService.findAllSubtypes(anyString())).thenReturn(Collections.emptySet());
		when(queryEnrichments.getFieldDescriptor(anyString())).thenReturn(mock(FieldDescriptor.class));
		
		// Mock computeModelSubtypes to allow it to be called without actual computation
		doAnswer(invocation -> null).when(queryEnrichments).computeModelSubtypes(anyString(), any());
	}

	@Test(description = "Should allow null targetDocumentModel in constraint fields validation")
	public void shouldAllowNullTargetDocumentModelInConstraintFields() {
		// Given
		QueryRoot queryRoot = QueryRoot.builder()
			.constraint(AndOperator.builder()
				.operand(ExactMatchOperator.builder()
					.field("TestField")
					.value("testValue")
					.build())
				.build())
			.build();

		String[] path = new String[]{"constraint"};

		// When
		fieldsValidator.validateFieldsAndAggregations(queryRoot, null, path, queryContext, validationResult, true);

		// Then - should not throw exception and should not attempt field validation with null model
		assertTrue(validationResult.getResults().stream()
			.noneMatch(item -> item.message().contains("NullPointerException")),
			"Validation should handle null targetDocumentModel gracefully");

		// Verify that no field indexing check was attempted with null model
		verify(queryContext, never()).isIndexedField(any(), anyString());
	}

	@Test(description = "Should skip indexed check when targetDocumentModel is null")
	public void shouldSkipIndexedCheckWhenTargetDocumentModelIsNull() {
		// Given
		QueryRoot queryRoot = QueryRoot.builder()
			.fields(List.of("field1", "field2"))
			.build();

		String[] path = new String[]{"fields"};

		// When
		fieldsValidator.validateFieldsAndAggregations(queryRoot, null, path, queryContext, validationResult, true);

		// Then - indexed check should be skipped for null targetDocumentModel
		verify(queryContext, never()).isIndexedField(any(), anyString());
		verify(queryEnrichments, never()).computeModelSubtypes(any(), any());

		// Validation should complete without errors related to null model
		assertTrue(validationResult.getResults().stream()
			.noneMatch(item -> item.message().contains("NullPointerException")),
			"Should not throw NullPointerException when targetDocumentModel is null");
	}

	@Test(description = "Should return empty when extracting field type with null documentModelName")
	public void shouldReturnEmptyWhenExtractFieldTypeGivenNullDocumentModelName() {
		// Given
		QueryRoot queryRoot = QueryRoot.builder()
			.aggregation(AggregationProjector.builder()
				.aggregation(Sum.builder()
					.field("/numericField")
					.alias("totalSum")
					.build())
				.build())
			.build();

		String[] path = new String[]{"aggregation"};
		IDocumentModel documentModel = mock(IDocumentModel.class);
		IField field = mock(IField.class);

		when(queryContext.getDocumentModel(anyString())).thenReturn(documentModel);
		when(documentModelUtils.findField(any(IDocumentModel.class), anyString())).thenReturn(Optional.of(field));

		// When - call with null targetDocumentModel
		fieldsValidator.validateFieldsAndAggregations(queryRoot, null, path, queryContext, validationResult, true);

		// Then - should handle gracefully without attempting to process null documentModelName
		// The extractFieldType method should return empty for null documentModelName
		verify(queryContext, never()).getDocumentModel(null);

		// Validation result should contain error about field type not being determined
		assertTrue(validationResult.getResults().stream()
			.anyMatch(item -> item.message().contains("Field type could not be determined")),
			"Should report that field type could not be determined for null model");
	}

	@Test(description = "Should validate aggregations without early exit for null targetDocumentModel")
	public void shouldValidateAggregationsWhenTargetDocumentModelIsNull() {
		// Given
		QueryRoot queryRoot = QueryRoot.builder()
			.aggregation(AggregationProjector.builder()
				.aggregation(Count.builder()
					.field("someField")
					.alias("countAlias")
					.build())
				.build())
			.build();

		String[] path = new String[]{"aggregation"};

		// When
		fieldsValidator.validateFieldsAndAggregations(queryRoot, null, path, queryContext, validationResult, true);

		// Then - validation should proceed without early exit
		// Should not have validation error about missing target document model
		assertFalse(validationResult.getResults().stream()
			.anyMatch(item -> item.message().contains("Aggregations must have a target document model specified")),
			"Should not fail with old error message about target document model being required");

		// Should still validate aggregation structure (function, field presence)
		assertTrue(validationResult.getResults().stream()
			.anyMatch(item -> item.message().contains("function") || item.message().contains("field")),
			"Should still validate aggregation function and field requirements");
	}

	@Test(description = "Should validate fields when targetDocumentModel is provided")
	public void shouldValidateFieldsWhenTargetDocumentModelIsProvided() {
		// Given
		String targetModel = "TestModel";
		String fieldName = "/TestField";
		QueryRoot queryRoot = QueryRoot.builder()
			.fields(List.of(fieldName))
			.build();

		String[] path = new String[]{"fields"};

		when(queryContext.isIndexedField(any(), anyString())).thenReturn(true);

		// When
		fieldsValidator.validateFieldsAndAggregations(queryRoot, targetModel, path, queryContext, validationResult, true);

		// Then - should perform indexed check with valid model
		verify(queryContext).isIndexedField(any(Set.class), anyString());
		verify(queryEnrichments).computeModelSubtypes(eq(targetModel), any());

		// Should have validation result for the field
		assertTrue(validationResult.getResults().stream()
			.anyMatch(item -> item.message().contains("indexed")),
			"Should validate field when target model is provided");
	}

	@Test(description = "Should validate constraint fields when targetDocumentModel is provided")
	public void shouldValidateConstraintFieldsWhenTargetDocumentModelIsProvided() {
		// Given
		String targetModel = "TestModel";
		String fieldName = "/constraint.field";
		QueryRoot queryRoot = QueryRoot.builder()
			.constraint(ExactMatchOperator.builder()
				.field(fieldName)
				.value("testValue")
				.build())
			.build();

		String[] path = new String[]{"constraint"};

		when(queryContext.isIndexedField(any(), anyString())).thenReturn(true);

		// When
		fieldsValidator.validateFieldsAndAggregations(queryRoot, targetModel, path, queryContext, validationResult, true);

		// Then - should validate constraint field with valid model
		verify(queryContext).isIndexedField(any(Set.class), anyString());
		verify(queryEnrichments).computeModelSubtypes(eq(targetModel), any());
	}

	@Test(description = "Should handle empty fields list appropriately")
	public void shouldHandleEmptyFieldsListAppropriately() {
		// Given
		QueryRoot queryRoot = QueryRoot.builder()
			.fields(Collections.emptyList())
			.build();

		String[] path = new String[]{"fields"};

		// When
		fieldsValidator.validateFieldsAndAggregations(queryRoot, "TestModel", path, queryContext, validationResult, true);

		// Then - should have validation error for empty fields list
		assertTrue(validationResult.hasErrors(),
			"Should have errors when fields list is empty");

		assertTrue(validationResult.getResults().stream()
			.anyMatch(item -> item.message().contains("non-empty list")),
			"Should report error about empty fields list");
	}

	@Test(description = "Should skip validation when validation is disabled")
	public void shouldSkipValidationWhenValidationIsDisabled() {
		// Given
		QueryRoot queryRoot = QueryRoot.builder()
			.fields(List.of("testField"))
			.build();

		String[] path = new String[]{"fields"};

		// When
		fieldsValidator.validateFieldsAndAggregations(queryRoot, "TestModel", path, queryContext, validationResult, false);

		// Then - no validation should occur
		verify(queryContext, never()).isIndexedField(any(), anyString());
		assertTrue(validationResult.getResults().isEmpty(),
			"Should not perform any validation when disabled");
	}

	@Test(description = "Should validate aggregations with group fields when targetDocumentModel is null")
	public void shouldValidateAggregationsWithGroupFieldsWhenTargetDocumentModelIsNull() {
		// Given
		QueryRoot queryRoot = QueryRoot.builder()
			.aggregation(AggregationProjector.builder()
				.group(List.of(ProjectionField.builder()
					.field("/groupField")
					.build()))
				.aggregation(Count.builder()
					.field("/countField")
					.alias("count")
					.build())
				.build())
			.build();

		String[] path = new String[]{"aggregation"};

		// When
		fieldsValidator.validateFieldsAndAggregations(queryRoot, null, path, queryContext, validationResult, true);

		// Then - should not throw exception
		// Group field validation should be performed (checking for field presence)
		assertTrue(validationResult.getResults().stream()
			.anyMatch(item -> item.message().contains("Group has field")),
			"Should validate group field presence even with null targetDocumentModel");

		// Should skip indexed check for null model
		verify(queryContext, never()).isIndexedField(any(), anyString());
	}

	@Test(description = "Should handle multiple constraint fields with null targetDocumentModel")
	public void shouldHandleMultipleConstraintFieldsWithNullTargetDocumentModel() {
		// Given
		QueryRoot queryRoot = QueryRoot.builder()
			.constraint(AndOperator.builder()
				.operand(ExactMatchOperator.builder()
					.field("field1")
					.value("value1")
					.build())
				.operand(ExactMatchOperator.builder()
					.field("field2")
					.value("value2")
					.build())
				.operand(ExactMatchOperator.builder()
					.field("field3")
					.value("value3")
					.build())
				.build())
			.build();

		String[] path = new String[]{"constraint"};

		// When
		fieldsValidator.validateFieldsAndAggregations(queryRoot, null, path, queryContext, validationResult, true);

		// Then - should handle gracefully without exceptions
		assertTrue(validationResult.getResults().stream()
			.noneMatch(item -> item.message().contains("NullPointerException")),
			"Should handle multiple fields with null model without errors");

		// Should not attempt indexed check with null model
		verify(queryContext, never()).isIndexedField(any(), anyString());
	}

	@DataProvider(name = "nullAndBlankDocumentModels")
	public Object[][] nullAndBlankDocumentModels() {
		return new Object[][] {
			{null, "null targetDocumentModel"},
			{"", "empty targetDocumentModel"},
			{"   ", "blank targetDocumentModel"}
		};
	}

	@Test(dataProvider = "nullAndBlankDocumentModels",
		description = "Should skip field validation for null or blank targetDocumentModel")
	public void shouldSkipFieldValidationForNullOrBlankTargetDocumentModel(String targetDocumentModel, String description) {
		// Given
		QueryRoot queryRoot = QueryRoot.builder()
			.fields(List.of("field1", "field2"))
			.constraint(ExactMatchOperator.builder()
				.field("constraintField")
				.value("value")
				.build())
			.build();

		String[] path = new String[]{"root"};

		// When
		fieldsValidator.validateFieldsAndAggregations(queryRoot, targetDocumentModel, path, queryContext, validationResult, true);

		// Then - should skip indexed check for null/blank model
		verify(queryContext, never()).isIndexedField(any(), anyString());

		log.info("Test passed for {}: validation skipped indexed check", description);
	}

	@Test(description = "Should validate that fields and aggregations are mutually exclusive")
	public void shouldValidateThatFieldsAndAggregationsAreMutuallyExclusive() {
		// Given
		QueryRoot queryRoot = QueryRoot.builder()
			.fields(List.of("field1"))
			.aggregation(AggregationProjector.builder()
				.aggregation(Count.builder()
					.field("countField")
					.alias("count")
					.build())
				.build())
			.build();

		String[] path = new String[]{"root"};

		// When
		fieldsValidator.validateFieldsAndAggregations(queryRoot, "TestModel", path, queryContext, validationResult, true);

		// Then
		assertTrue(validationResult.hasErrors(),
			"Should have errors when both fields and aggregations are present");

		assertTrue(validationResult.getResults().stream()
			.anyMatch(item -> item.message().contains("mutually exclusive")),
			"Should report error about fields and aggregations being mutually exclusive");
	}

	@Test(description = "Should report error when aggregations are used with links")
	public void shouldReportErrorWhenAggregationsAreUsedWithLinks() {
		// Given
		QueryRoot queryRoot = QueryRoot.builder()
			.link(QueryLink.builder()
				.relationshipModel("TestRelationship")
				.targetRole("TestRole")
				.build())
			.aggregation(AggregationProjector.builder()
				.aggregation(Count.builder()
					.field("countField")
					.alias("count")
					.build())
				.build())
			.build();

		String[] path = new String[]{"root"};

		// When
		fieldsValidator.validateFieldsAndAggregations(queryRoot, "TestModel", path, queryContext, validationResult, true);

		// Then
		assertTrue(validationResult.hasErrors(),
			"Should have errors when aggregations are used with links");

		assertTrue(validationResult.getResults().stream()
			.anyMatch(item -> item.message().contains("links together with aggregations are not supported")),
			"Should report error about links with aggregations not being supported");
	}
}
