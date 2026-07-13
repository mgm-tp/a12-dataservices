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

import java.util.List;
import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder.Direction;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder.NullHandling;
import com.mgmtp.a12.dataservices.query.Order;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.RelationshipOrder;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.relationship.model.EntityCharacteristics;
import com.mgmtp.a12.dataservices.relationship.model.LinkConstraints;
import com.mgmtp.a12.dataservices.relationship.model.Multiplicity;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModelContent;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.INumberType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IStringType;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.model.header.Header;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link OrderValidator}.
 * Tests validation of sorting specifications including relationship-based sorting.
 */
public class OrderValidatorTest {

	private OrderValidator orderValidator;
	private DataServicesCoreProperties dataServicesCoreProperties;
	private ModelPermissionEvaluator<IDocumentModel> documentModelPermissionEvaluator;
	private ModelPermissionEvaluator<RelationshipModel> relationshipModelPermissionEvaluator;
	private IModelLoader<RelationshipModel> relationshipModelLoader;
	private ModelTypeService modelTypeService;
	private DocumentModelUtils documentModelUtils;
	private QueryContext queryContext;

	@BeforeMethod
	public void setUp() {
		dataServicesCoreProperties = new DataServicesCoreProperties();
		documentModelPermissionEvaluator = mock(ModelPermissionEvaluator.class);
		relationshipModelPermissionEvaluator = mock(ModelPermissionEvaluator.class);
		relationshipModelLoader = mock(IModelLoader.class);
		modelTypeService = mock(ModelTypeService.class);
		documentModelUtils = mock(DocumentModelUtils.class);
		queryContext = mock(QueryContext.class);

		orderValidator = new OrderValidator(
			dataServicesCoreProperties,
			documentModelPermissionEvaluator,
			relationshipModelPermissionEvaluator,
			relationshipModelLoader,
			modelTypeService,
			documentModelUtils
		);
	}

	@Test(description = "Should accept valid direct field order")
	public void shouldAcceptValidDirectFieldOrder() {
		// Given
		IDocumentModelSearchService searchService = mock(IDocumentModelSearchService.class);
		when(queryContext.getDocumentModelSearchService("DocumentModel")).thenReturn(searchService);

		IElement fieldElement = mock(IElement.class);
		IGroup rootGroup = mock(IGroup.class);
		when(searchService.getByPath("/Root/fieldName")).thenReturn(Optional.of(fieldElement));
		when(fieldElement.getParent()).thenReturn(rootGroup); // Root group (non-repeatable)
		when(rootGroup.getRepeatability()).thenReturn(1); // Not repeatable
		when(rootGroup.getParent()).thenReturn(null); // No more parents

		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(new DirectFieldOrder("/Root/fieldName", Direction.ASC)))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "DocumentModel", queryContext, result);

		// Then
		assertTrue(!result.hasErrors(), "Direct field order should be valid");
	}

	@Test(description = "Should reject direct field order when field path has no leading slash")
	public void shouldRejectDirectFieldOrderWhenFieldPathHasNoLeadingSlash() {
		// Given
		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(new DirectFieldOrder("Root/fieldName", Direction.ASC)))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "DocumentModel", queryContext, result);

		// Then
		assertTrue(result.hasErrors(), "Direct field order without leading slash should be invalid");
		assertTrue(result.getResults().stream()
			.anyMatch(item -> !item.valid() && item.message().contains("Field path must start with a leading slash")));
	}

	@Test(description = "Should reject relationship sort when terminal field path has no leading slash")
	public void shouldRejectRelationshipSortWhenTerminalFieldPathHasNoLeadingSlash() {
		// Given
		RelationshipModel relationshipModel = createMockRelationshipModelWithCardinality("TestRM", "TargetRole", "TargetModel", false, 1);
		when(relationshipModelLoader.loadModel("TestRM")).thenReturn(relationshipModel);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(relationshipModel)).thenReturn(true);

		DirectFieldOrder terminal = new DirectFieldOrder(
			"TargetRoot/fieldName", Direction.ASC, false, NullHandling.NULLS_LAST);
		RelationshipOrder order = new RelationshipOrder("TestRM", "TargetRole", terminal);
		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(order))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "DocumentModel", queryContext, result);

		// Then
		assertTrue(result.hasErrors(), "Relationship sort with terminal field without leading slash should be invalid");
		assertTrue(result.getResults().stream()
			.anyMatch(item -> !item.valid() && item.message().contains("Field path must start with a leading slash")));
	}

	@Test(description = "Should accept ignoreCase=true on direct field order regardless of field type")
	public void shouldAcceptIgnoreCaseTrueOnDirectFieldOrderForAnyFieldType() {
		// Given: direct field sort with ignoreCase=true — validator does not inspect field type
		IDocumentModelSearchService searchService = mock(IDocumentModelSearchService.class);
		when(queryContext.getDocumentModelSearchService("DocumentModel")).thenReturn(searchService);

		IElement fieldElement = mock(IElement.class);
		IGroup rootGroup = mock(IGroup.class);
		when(searchService.getByPath("/Root/numericField")).thenReturn(Optional.of(fieldElement));
		when(fieldElement.getParent()).thenReturn(rootGroup);
		when(rootGroup.getRepeatability()).thenReturn(1); // Not repeatable
		when(rootGroup.getParent()).thenReturn(null);

		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(new DirectFieldOrder("/Root/numericField", Direction.ASC, true, NullHandling.NULLS_LAST)))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "DocumentModel", queryContext, result);

		// Then: ignoreCase=true on a direct field does not produce a validation error
		assertFalse(result.getResults().stream()
				.anyMatch(item -> !item.valid() && item.message().contains("ignoreCase")),
			"ignoreCase=true on direct field sort should NOT produce an error (consistent with relationship field sort)");
	}

	@Test(description = "Should reject direct field order in repeatable group")
	public void shouldRejectDirectFieldOrderInRepeatableGroup() {
		// Given
		IElement fieldElement = mock(IElement.class);
		IGroup repeatableGroup = mock(IGroup.class);
		IDocumentModelSearchService searchService = mock(IDocumentModelSearchService.class);
		when(queryContext.getDocumentModelSearchService("DocumentModel")).thenReturn(searchService);
		when(searchService.getByPath("/Root/repeatableGroup/fieldName")).thenReturn(Optional.of(fieldElement));
		when(fieldElement.getParent()).thenReturn(repeatableGroup);
		when(repeatableGroup.getRepeatability()).thenReturn(2); // Repeatable (>1 means repeatable)
		when(repeatableGroup.getParent()).thenReturn(null);

		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(new DirectFieldOrder("/Root/repeatableGroup/fieldName", Direction.ASC)))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "DocumentModel", queryContext, result);

		// Then
		assertTrue(result.hasErrors(), "Direct field order in repeatable group should be invalid");
		assertTrue(result.getResults().stream()
			.anyMatch(item -> !item.valid() && item.message().contains("repeatable group")));
	}


	@Test(description = "Should reject RelationshipOrder with no terminal field and no sortBy")
	public void shouldRejectOrderWithNeitherFieldNorRelationshipOrder() {
		// Given — terminal with neither field nor linkField is invalid
		// In the new hierarchy, this would be a DirectFieldOrder with null field, which is invalid
		DirectFieldOrder terminal = new DirectFieldOrder(
			null, Direction.ASC, false, NullHandling.NULLS_LAST);
		RelationshipOrder order = new RelationshipOrder("RelModel", "TargetRole",
			terminal);
		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(order))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "DocumentModel", queryContext, result);

		// Then
		assertFalse(!result.hasErrors(), "RelationshipOrder with neither field nor sortBy should be invalid");
	}

	@Test(description = "Should require explicit nullHandling for relationship orders")
	public void shouldRequireExplicitNullHandlingForRelationshipOrders() {
		// Given
		RelationshipModel relationshipModel = createMockRelationshipModel("RelModel", "TargetRole", "TargetDocumentModel");
		when(relationshipModelLoader.loadModel("RelModel")).thenReturn(relationshipModel);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(relationshipModel)).thenReturn(true);

		DirectFieldOrder terminal = new DirectFieldOrder(
			"field", Direction.ASC, false, NullHandling.NATIVE);
		RelationshipOrder order = new RelationshipOrder("RelModel", "TargetRole", terminal);
		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(order))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "DocumentModel", queryContext, result);

		// Then
		assertFalse(!result.hasErrors(), "Relationship order with NATIVE nullHandling should be invalid");
		assertTrue(result.getResults().stream()
			.anyMatch(item -> !item.valid() && item.message().contains("Explicit nullHandling is required")));
	}

	@Test(description = "Should reject too many relationship orders")
	public void shouldRejectTooManyRelationshipOrders() {
		// Given
		dataServicesCoreProperties.getQuery().getRelationshipOrder().setMaxCount(2);

		List<Order> orders = List.of(
			new RelationshipOrder("RM1", "Role1",
				new DirectFieldOrder("field1", Direction.ASC, false, NullHandling.NULLS_LAST)),
			new RelationshipOrder("RM2", "Role2",
				new DirectFieldOrder("field2", Direction.ASC, false, NullHandling.NULLS_LAST)),
			new RelationshipOrder("RM3", "Role3",
				new DirectFieldOrder("field3", Direction.ASC, false, NullHandling.NULLS_LAST))
		);

		QueryRoot queryRoot = QueryRoot.builder()
			.sort(orders)
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "DocumentModel", queryContext, result);

		// Then
		assertFalse(!result.hasErrors(), "Too many relationship orders should be invalid");
		assertTrue(result.getResults().stream()
			.anyMatch(item -> !item.valid() && item.message().contains("exceeds configured maximum")));
	}


	@Test(description = "Should reject RelationshipOrder with neither field nor sortBy")
	public void shouldRejectRelationshipOrderWithNeitherFieldNorSortBy() {
		// Given
		DirectFieldOrder terminal = new DirectFieldOrder(null, Direction.ASC, false, NullHandling.NULLS_LAST);
		RelationshipOrder order = new RelationshipOrder("RM1", "Role1", terminal);
		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(order))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "DocumentModel", queryContext, result);

		// Then
		assertFalse(!result.hasErrors(), "RelationshipSortOrder with neither field nor sortBy should be invalid");
	}

	@Test(description = "Should reject relationship order exceeding max nesting depth")
	public void shouldRejectRelationshipOrderExceedingMaxNestingDepth() {
		// Given
		dataServicesCoreProperties.getQuery().getRelationshipOrder().setMaxNestingDepth(2);

		// Mock relationship models for each level
		RelationshipModel rm1 = createMockRelationshipModelWithCardinality("RM1", "Role1", "Model2", false, 1);
		when(relationshipModelLoader.loadModel("RM1")).thenReturn(rm1);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(rm1)).thenReturn(true);

		RelationshipModel rm2 = createMockRelationshipModelWithCardinality("RM2", "Role2", "Model3", false, 1);
		when(relationshipModelLoader.loadModel("RM2")).thenReturn(rm2);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(rm2)).thenReturn(true);

		RelationshipModel rm3 = createMockRelationshipModelWithCardinality("RM3", "Role3", "Model4", false, 1);
		when(relationshipModelLoader.loadModel("RM3")).thenReturn(rm3);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(rm3)).thenReturn(true);

		// Create nested relationship order with depth 3 (exceeds max of 2): RM1 -> RM2 -> RM3 -> field
		DirectFieldOrder terminal = new DirectFieldOrder(
			"field", Direction.ASC, false, NullHandling.NULLS_LAST);
		RelationshipOrder order = new RelationshipOrder("RM1", "Role1",
			new RelationshipOrder("RM2", "Role2",
				new RelationshipOrder("RM3", "Role3", terminal)));

		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(order))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "DocumentModel", queryContext, result);

		// Then
		assertTrue(result.hasErrors(), "Relationship order exceeding max depth should be invalid");
		assertTrue(result.getResults().stream()
			.anyMatch(item -> !item.valid() && item.message().contains("nesting depth")));
	}

	@Test(description = "Should reject relationship order when relationship model not found")
	public void shouldRejectRelationshipOrderWhenRelationshipModelNotFound() {
		// Given
		when(relationshipModelLoader.loadModel("NonExistentRM")).thenThrow(new NotFoundException("Not found"));

		DirectFieldOrder terminal = new DirectFieldOrder(
			"field", Direction.ASC, false, NullHandling.NULLS_LAST);
		RelationshipOrder order = new RelationshipOrder("NonExistentRM", "Role1", terminal);
		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(order))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "DocumentModel", queryContext, result);

		// Then
		assertFalse(!result.hasErrors(), "Relationship order with non-existent model should be invalid");
		assertTrue(result.getResults().stream()
			.anyMatch(item -> !item.valid() && item.message().contains("not found")));
	}

	@Test(description = "Should reject relationship order when user lacks permission to relationship model")
	public void shouldRejectRelationshipOrderWhenUserLacksPermissionToRelationshipModel() {
		// Given
		RelationshipModel relationshipModel = createMockRelationshipModel("TestRM", "TargetRole", "TargetModel");
		when(relationshipModelLoader.loadModel("TestRM")).thenReturn(relationshipModel);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(relationshipModel)).thenReturn(false);

		DirectFieldOrder terminal = new DirectFieldOrder(
			"field", Direction.ASC, false, NullHandling.NULLS_LAST);
		RelationshipOrder order = new RelationshipOrder("TestRM", "TargetRole", terminal);
		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(order))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "DocumentModel", queryContext, result);

		// Then
		assertFalse(!result.hasErrors(), "Relationship order without permission should be invalid");
		assertTrue(result.getResults().stream()
			.anyMatch(item -> !item.valid() && item.message().contains("does not have permission")));
	}

	@Test(description = "Should reject relationship order when target role not found")
	public void shouldRejectRelationshipOrderWhenTargetRoleNotFound() {
		// Given
		RelationshipModel relationshipModel = createMockRelationshipModel("TestRM", "ValidRole", "TargetModel");
		when(relationshipModelLoader.loadModel("TestRM")).thenReturn(relationshipModel);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(relationshipModel)).thenReturn(true);

		DirectFieldOrder terminal = new DirectFieldOrder(
			"field", Direction.ASC, false, NullHandling.NULLS_LAST);
		RelationshipOrder order = new RelationshipOrder("TestRM", "InvalidRole", terminal);
		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(order))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "DocumentModel", queryContext, result);

		// Then
		assertFalse(!result.hasErrors(), "Relationship order with invalid role should be invalid");
		assertTrue(result.getResults().stream()
			.anyMatch(item -> !item.valid() && item.message().contains("Target role") && item.message().contains("not found")));
	}

	@Test(description = "Should reject relationship order when user lacks permission to target document model")
	public void shouldRejectRelationshipOrderWhenUserLacksPermissionToTargetDocumentModel() {
		// Given
		RelationshipModel relationshipModel = createMockRelationshipModel("TestRM", "TargetRole", "TargetModel");
		when(relationshipModelLoader.loadModel("TestRM")).thenReturn(relationshipModel);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(relationshipModel)).thenReturn(true);
		doThrow(new AccessDeniedException("Access denied")).when(documentModelPermissionEvaluator).checkModelReadPermission("TargetModel");

		DirectFieldOrder terminal = new DirectFieldOrder(
			"field", Direction.ASC, false, NullHandling.NULLS_LAST);
		RelationshipOrder order = new RelationshipOrder("TestRM", "TargetRole", terminal);
		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(order))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "DocumentModel", queryContext, result);

		// Then
		assertFalse(!result.hasErrors(), "Relationship order without permission to target model should be invalid");
		assertTrue(result.getResults().stream()
			.anyMatch(item -> !item.valid() && item.message().contains("does not have permission")));
	}

	@Test(description = "Should reject to-many relationship for sorting")
	public void shouldRejectToManyRelationshipForSorting() {
		// Given
		RelationshipModel relationshipModel = createMockRelationshipModelWithCardinality("TestRM", "TargetRole", "TargetModel", false, 10);
		when(relationshipModelLoader.loadModel("TestRM")).thenReturn(relationshipModel);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(relationshipModel)).thenReturn(true);

		DirectFieldOrder terminal = new DirectFieldOrder(
			"field", Direction.ASC, false, NullHandling.NULLS_LAST);
		RelationshipOrder order = new RelationshipOrder("TestRM", "TargetRole", terminal);
		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(order))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "DocumentModel", queryContext, result);

		// Then
		assertFalse(!result.hasErrors(), "To-many relationship should be invalid for sorting");
		assertTrue(result.getResults().stream()
			.anyMatch(item -> !item.valid() && item.message().contains("to-many cardinality")));
	}

	@Test(description = "Should reject unbounded relationship for sorting")
	public void shouldRejectUnboundedRelationshipForSorting() {
		// Given
		RelationshipModel relationshipModel = createMockRelationshipModelWithCardinality("TestRM", "TargetRole", "TargetModel", true, null);
		when(relationshipModelLoader.loadModel("TestRM")).thenReturn(relationshipModel);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(relationshipModel)).thenReturn(true);

		DirectFieldOrder terminal = new DirectFieldOrder(
			"field", Direction.ASC, false, NullHandling.NULLS_LAST);
		RelationshipOrder order = new RelationshipOrder("TestRM", "TargetRole", terminal);
		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(order))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "DocumentModel", queryContext, result);

		// Then
		assertFalse(!result.hasErrors(), "Unbounded relationship should be invalid for sorting");
		assertTrue(result.getResults().stream()
			.anyMatch(item -> !item.valid() && item.message().contains("to-many cardinality")));
	}

	@Test(description = "Should accept to-one relationship for sorting")
	public void shouldAcceptToOneRelationshipForSorting() {
		// Given
		RelationshipModel relationshipModel = createMockRelationshipModelWithCardinality("TestRM", "TargetRole", "TargetModel", false, 1);
		when(relationshipModelLoader.loadModel("TestRM")).thenReturn(relationshipModel);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(relationshipModel)).thenReturn(true);

		IDocumentModel documentModel = mock(IDocumentModel.class);
		IField field = mock(IField.class);
		IDocumentModelSearchService searchService = mock(IDocumentModelSearchService.class);
		when(queryContext.getDocumentModel("TargetModel")).thenReturn(documentModel);
		when(queryContext.getDocumentModelSearchService("TargetModel")).thenReturn(searchService);
		when(documentModelUtils.findField(documentModel, "/TargetRoot/fieldName")).thenReturn(Optional.of(field));
		when(searchService.getByPath(anyString())).thenReturn(Optional.empty()); // No repeatable groups

		DirectFieldOrder terminal = new DirectFieldOrder(
			"/TargetRoot/fieldName", Direction.ASC, false, NullHandling.NULLS_LAST);
		RelationshipOrder order = new RelationshipOrder("TestRM", "TargetRole", terminal);
		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(order))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "DocumentModel", queryContext, result);

		// Then
		assertTrue(!result.hasErrors(), "To-one relationship should be valid for sorting");
	}

	@Test(description = "Should reject field not found on target document model")
	public void shouldRejectFieldNotFoundOnTargetDocumentModel() {
		// Given
		RelationshipModel relationshipModel = createMockRelationshipModelWithCardinality("TestRM", "TargetRole", "TargetModel", false, 1);
		when(relationshipModelLoader.loadModel("TestRM")).thenReturn(relationshipModel);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(relationshipModel)).thenReturn(true);

		IDocumentModel documentModel = mock(IDocumentModel.class);
		when(queryContext.getDocumentModel("TargetModel")).thenReturn(documentModel);
		when(documentModelUtils.findField(documentModel, "/TargetRoot/nonExistentField")).thenReturn(Optional.empty());

		DirectFieldOrder terminal = new DirectFieldOrder(
			"/TargetRoot/nonExistentField", Direction.ASC, false, NullHandling.NULLS_LAST);
		RelationshipOrder order = new RelationshipOrder("TestRM", "TargetRole", terminal);
		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(order))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "DocumentModel", queryContext, result);

		// Then
		assertFalse(!result.hasErrors(), "Field not found should be invalid");
		assertTrue(result.getResults().stream()
			.anyMatch(item -> !item.valid() && item.message().contains("Field") && item.message().contains("not found")));
	}

	@Test(description = "Should reject field in repeatable group")
	public void shouldRejectFieldInRepeatableGroup() {
		// Given
		RelationshipModel relationshipModel = createMockRelationshipModelWithCardinality("TestRM", "TargetRole", "TargetModel", false, 1);
		when(relationshipModelLoader.loadModel("TestRM")).thenReturn(relationshipModel);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(relationshipModel)).thenReturn(true);

		IDocumentModel documentModel = mock(IDocumentModel.class);
		IField field = mock(IField.class);
		IGroup repeatableGroup = mock(IGroup.class);
		IDocumentModelSearchService searchService = mock(IDocumentModelSearchService.class);

		when(queryContext.getDocumentModel("TargetModel")).thenReturn(documentModel);
		when(queryContext.getDocumentModelSearchService("TargetModel")).thenReturn(searchService);
		when(documentModelUtils.findField(documentModel, "/TargetRoot/repeatableGroup/field")).thenReturn(Optional.of(field));
		when(searchService.getByPath("/TargetRoot/repeatableGroup/field")).thenReturn(Optional.of(field));
		when(field.getParent()).thenReturn(repeatableGroup);
		when(repeatableGroup.getRepeatability()).thenReturn(2); // Repeatable (>1 means repeatable)
		when(repeatableGroup.getParent()).thenReturn(null);

		DirectFieldOrder terminal = new DirectFieldOrder(
			"/TargetRoot/repeatableGroup/field", Direction.ASC, false, NullHandling.NULLS_LAST);
		RelationshipOrder order = new RelationshipOrder("TestRM", "TargetRole", terminal);
		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(order))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "DocumentModel", queryContext, result);

		// Then
		assertFalse(!result.hasErrors(), "Field in repeatable group should be invalid");
		assertTrue(result.getResults().stream()
			.anyMatch(item -> !item.valid() && item.message().contains("repeatable group")));
	}

	@Test(description = "Should reject nested RelationshipOrder where inner RelationshipOrder has null sortBy")
	public void shouldRejectNestedRelationshipOrderWithNullSortBy() {
		// Given
		RelationshipModel rm1 = createMockRelationshipModelWithCardinality("RM1", "Role1", "IntermediateModel", false, 1);
		when(relationshipModelLoader.loadModel("RM1")).thenReturn(rm1);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(rm1)).thenReturn(true);

		RelationshipModel rm2 = createMockRelationshipModelWithCardinality("RM2", "Role2", "TargetModel", false, 1);
		when(relationshipModelLoader.loadModel("RM2")).thenReturn(rm2);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(rm2)).thenReturn(true);

		RelationshipOrder innerRO = new RelationshipOrder("RM2", "Role2", null); // sortBy is null
		RelationshipOrder outerRO = new RelationshipOrder("RM1", "Role1", innerRO);
		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(outerRO))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "SourceModel", queryContext, result);

		// Then
		assertTrue(result.hasErrors(), "Nested RelationshipOrder with null sortBy should be invalid");
		assertTrue(result.getResults().stream()
			.anyMatch(item -> !item.valid() && item.message().contains("sortBy is required on RelationshipOrder")));
	}

	@Test(description = "Should accept valid nested relationship order")
	public void shouldAcceptValidNestedRelationshipOrder() {
		// Given
		// Setup first level: SourceModel -> RM1 -> IntermediateModel
		RelationshipModel rm1 = createMockRelationshipModelWithCardinality("RM1", "Role1", "IntermediateModel", false, 1);
		when(relationshipModelLoader.loadModel("RM1")).thenReturn(rm1);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(rm1)).thenReturn(true);

		// Setup second level: IntermediateModel -> RM2 -> TargetModel
		RelationshipModel rm2 = createMockRelationshipModelWithCardinality("RM2", "Role2", "TargetModel", false, 1);
		when(relationshipModelLoader.loadModel("RM2")).thenReturn(rm2);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(rm2)).thenReturn(true);

		IDocumentModel documentModel = mock(IDocumentModel.class);
		IField field = mock(IField.class);
		IDocumentModelSearchService searchService = mock(IDocumentModelSearchService.class);

		when(queryContext.getDocumentModel("TargetModel")).thenReturn(documentModel);
		when(queryContext.getDocumentModelSearchService("TargetModel")).thenReturn(searchService);
		when(documentModelUtils.findField(documentModel, "/TargetRoot/fieldName")).thenReturn(Optional.of(field));
		when(searchService.getByPath(anyString())).thenReturn(Optional.empty()); // No repeatable groups

		DirectFieldOrder terminal = new DirectFieldOrder(
			"/TargetRoot/fieldName", Direction.ASC, false, NullHandling.NULLS_LAST);
		RelationshipOrder level1 = new RelationshipOrder("RM1", "Role1", new RelationshipOrder("RM2", "Role2", terminal));
		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(level1))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "SourceModel", queryContext, result);

		// Then
		assertTrue(!result.hasErrors(), "Valid nested relationship order should be accepted");
	}

	@Test(description = "Should accept ignoreCase=true when terminal field is IDateType in relationship order (consistent with direct field sort)")
	public void shouldAcceptIgnoreCaseTrueWhenTerminalFieldIsDateInRelationshipOrder() {
		// Given
		RelationshipModel relationshipModel = createMockRelationshipModelWithCardinality("TestRM", "TargetRole", "TargetModel", false, 1);
		when(relationshipModelLoader.loadModel("TestRM")).thenReturn(relationshipModel);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(relationshipModel)).thenReturn(true);

		IDocumentModel documentModel = mock(IDocumentModel.class);
		IField field = mock(IField.class);
		IDateType dateType = mock(IDateType.class);
		IDocumentModelSearchService searchService = mock(IDocumentModelSearchService.class);

		when(queryContext.getDocumentModel("TargetModel")).thenReturn(documentModel);
		when(queryContext.getDocumentModelSearchService("TargetModel")).thenReturn(searchService);
		when(documentModelUtils.findField(documentModel, "/TargetRoot/StartOfRelationship")).thenReturn(Optional.of(field));
		when(field.getFieldType()).thenReturn(dateType);
		when(searchService.getByPath(anyString())).thenReturn(Optional.empty());

		DirectFieldOrder terminal = new DirectFieldOrder(
			"/TargetRoot/StartOfRelationship", Direction.ASC, true, NullHandling.NULLS_LAST);
		RelationshipOrder order = new RelationshipOrder("TestRM", "TargetRole", terminal);
		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(order))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "SourceModel", queryContext, result);

		// Then: ignoreCase on a non-string field is silently ignored, not rejected
		assertFalse(result.getResults().stream()
				.anyMatch(item -> !item.valid() && item.message().contains("ignoreCase")),
			"ignoreCase=true on a date field should NOT produce an error (consistent with direct field sort)");
	}

	@Test(description = "Should accept ignoreCase=true when terminal field is INumberType in relationship order (consistent with direct field sort)")
	public void shouldAcceptIgnoreCaseTrueWhenTerminalFieldIsNumberInRelationshipOrder() {
		// Given
		RelationshipModel relationshipModel = createMockRelationshipModelWithCardinality("TestRM", "TargetRole", "TargetModel", false, 1);
		when(relationshipModelLoader.loadModel("TestRM")).thenReturn(relationshipModel);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(relationshipModel)).thenReturn(true);

		IDocumentModel documentModel = mock(IDocumentModel.class);
		IField field = mock(IField.class);
		INumberType numberType = mock(INumberType.class);
		IDocumentModelSearchService searchService = mock(IDocumentModelSearchService.class);

		when(queryContext.getDocumentModel("TargetModel")).thenReturn(documentModel);
		when(queryContext.getDocumentModelSearchService("TargetModel")).thenReturn(searchService);
		when(documentModelUtils.findField(documentModel, "/TargetRoot/accountNumber")).thenReturn(Optional.of(field));
		when(field.getFieldType()).thenReturn(numberType);
		when(searchService.getByPath(anyString())).thenReturn(Optional.empty());

		DirectFieldOrder terminal = new DirectFieldOrder(
			"/TargetRoot/accountNumber", Direction.ASC, true, NullHandling.NULLS_LAST);
		RelationshipOrder order = new RelationshipOrder("TestRM", "TargetRole", terminal);
		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(order))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "SourceModel", queryContext, result);

		// Then: ignoreCase on a non-string field is silently ignored, not rejected
		assertFalse(result.getResults().stream()
				.anyMatch(item -> !item.valid() && item.message().contains("ignoreCase")),
			"ignoreCase=true on a number field should NOT produce an error (consistent with direct field sort)");
	}

	@Test(description = "Should accept ignoreCase=true when terminal field is IStringType in relationship order")
	public void shouldAcceptIgnoreCaseTrueWhenTerminalFieldIsStringInRelationshipOrder() {
		// Given
		RelationshipModel relationshipModel = createMockRelationshipModelWithCardinality("TestRM", "TargetRole", "TargetModel", false, 1);
		when(relationshipModelLoader.loadModel("TestRM")).thenReturn(relationshipModel);
		when(relationshipModelPermissionEvaluator.hasModelReadPermission(relationshipModel)).thenReturn(true);

		IDocumentModel documentModel = mock(IDocumentModel.class);
		IField field = mock(IField.class);
		IStringType stringType = mock(IStringType.class);
		IDocumentModelSearchService searchService = mock(IDocumentModelSearchService.class);

		when(queryContext.getDocumentModel("TargetModel")).thenReturn(documentModel);
		when(queryContext.getDocumentModelSearchService("TargetModel")).thenReturn(searchService);
		when(documentModelUtils.findField(documentModel, "/TargetRoot/Name")).thenReturn(Optional.of(field));
		when(field.getFieldType()).thenReturn(stringType);
		when(searchService.getByPath(anyString())).thenReturn(Optional.empty());

		DirectFieldOrder terminal = new DirectFieldOrder(
			"/TargetRoot/Name", Direction.ASC, true, NullHandling.NULLS_LAST);
		RelationshipOrder order = new RelationshipOrder("TestRM", "TargetRole", terminal);
		QueryRoot queryRoot = QueryRoot.builder()
			.sort(List.of(order))
			.build();
		ValidationResult result = new ValidationResult();

		// When
		orderValidator.validateSorting(queryRoot, "SourceModel", queryContext, result);

		// Then: no validation error for ignoreCase on a string field
		assertFalse(result.getResults().stream()
				.anyMatch(item -> !item.valid() && item.message().contains("ignoreCase")),
			"ignoreCase=true on a string field should NOT produce an error");
	}


	// Helper methods

	private RelationshipModel createMockRelationshipModel(String relationshipModelName, String targetRole, String targetDocumentModel) {
		return createMockRelationshipModelWithCardinality(relationshipModelName, targetRole, targetDocumentModel, false, 1);
	}

	private RelationshipModel createMockRelationshipModelWithCardinality(String relationshipModelName, String targetRole,
		String targetDocumentModel, boolean unbounded, Integer upperLimit) {

		RelationshipModel relationshipModel = new RelationshipModel();
		Header header = mock(Header.class);
		when(header.getId()).thenReturn(relationshipModelName);
		relationshipModel.setHeader(header);

		RelationshipModelContent content = new RelationshipModelContent();

		EntityCharacteristics sourceChar = new EntityCharacteristics();
		sourceChar.setRole("SourceRole");
		sourceChar.setDocumentModel("SourceModel");

		EntityCharacteristics targetChar = new EntityCharacteristics();
		targetChar.setRole(targetRole);
		targetChar.setDocumentModel(targetDocumentModel);

		// Set cardinality
		Multiplicity multiplicity = new Multiplicity();
		multiplicity.setUnbounded(unbounded);
		multiplicity.setUpperLimit(upperLimit);

		LinkConstraints linkConstraints = new LinkConstraints();
		linkConstraints.setMultiplicity(multiplicity);
		targetChar.setLinkConstraints(linkConstraints);

		content.setEntityCharacteristics(List.of(sourceChar, targetChar));
		relationshipModel.setContent(content);

		return relationshipModel;
	}


}
