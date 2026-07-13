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
package com.mgmtp.a12.dataservices.model.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.model.ModelConstants;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.model.Model;
import com.mgmtp.a12.model.header.Annotation;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.Label;
import com.mgmtp.a12.model.header.ModelReference;

import lombok.AllArgsConstructor;
import lombok.Data;

import static org.mockito.Mockito.when;

@Listeners(MockitoTestNGListener.class)
public class AbstractModelTypeServiceTest {

	@Mock private ModelService modelService;
	@Mock private ModelPermissionEvaluator<Model> modelPermissionEvaluator;

	private AbstractModelTypeService service;

	@BeforeMethod
	public void setUp() {
		service = new AbstractModelTypeService(modelService, modelPermissionEvaluator) {
		};
	}

	@Test(description = "Should return all subtypes when model hierarchy exists")
	public void shouldReturnAllSubtypesWhenModelHierarchyExists() {
		// Given - hierarchy: ModelA -> ModelB -> ModelC
		when(modelService.findAllHeadersByType(ModelConstants.DOCUMENT_MODEL_TYPE)).thenReturn(List.of(
			makeHeader("ModelA", "subTypes", "ModelB"),
			makeHeader("ModelB", "subTypes", "ModelC"),
			makeHeader("ModelC", null, null)
		));
		when(modelPermissionEvaluator.hasModelReadPermission("ModelB")).thenReturn(true);
		when(modelPermissionEvaluator.hasModelReadPermission("ModelC")).thenReturn(true);

		// When
		Set<String> result = service.findAllSubtypes("ModelA");

		// Then
		Assert.assertEquals(result, Set.of("ModelB", "ModelC"));
	}

	@Test(description = "Should return empty set when model has no subtypes")
	public void shouldReturnEmptySetWhenModelHasNoSubtypes() {
		// Given
		when(modelService.findAllHeadersByType(ModelConstants.DOCUMENT_MODEL_TYPE)).thenReturn(List.of(
			makeHeader("ModelA", null, null)
		));

		// When
		Set<String> result = service.findAllSubtypes("ModelA");

		// Then
		Assert.assertTrue(result.isEmpty());
	}

	@Test(description = "Should return direct subtypes only when model has direct subtypes")
	public void shouldReturnDirectSubtypesOnlyWhenModelHasDirectSubtypes() {
		// Given - hierarchy: ModelA -> ModelB -> ModelC
		when(modelService.findAllHeadersByType(ModelConstants.DOCUMENT_MODEL_TYPE)).thenReturn(List.of(
			makeHeader("ModelA", "subTypes", "ModelB"),
			makeHeader("ModelB", "subTypes", "ModelC"),
			makeHeader("ModelC", null, null)
		));
		when(modelPermissionEvaluator.hasModelReadPermission("ModelB")).thenReturn(true);

		// When
		Set<String> result = service.findDirectSubtypes("ModelA");

		// Then - only direct subtypes, not deeply nested ones
		Assert.assertEquals(result, Set.of("ModelB"));
	}

	@Test(description = "Should return empty set when model has no direct subtypes")
	public void shouldReturnEmptySetWhenModelHasNoDirectSubtypes() {
		// Given
		when(modelService.findAllHeadersByType(ModelConstants.DOCUMENT_MODEL_TYPE)).thenReturn(List.of(
			makeHeader("ModelA", null, null)
		));

		// When
		Set<String> result = service.findDirectSubtypes("ModelA");

		// Then
		Assert.assertTrue(result.isEmpty());
	}

	@Test(description = "Should exclude models without read permission when finding all subtypes")
	public void shouldExcludeModelsWithoutReadPermissionWhenFindingAllSubtypes() {
		// Given - ModelA -> ModelB, ModelA -> ModelC; ModelC has no read permission
		when(modelService.findAllHeadersByType(ModelConstants.DOCUMENT_MODEL_TYPE)).thenReturn(List.of(
			makeHeader("ModelA", "subTypes", "ModelB,ModelC"),
			makeHeader("ModelB", null, null),
			makeHeader("ModelC", null, null)
		));
		when(modelPermissionEvaluator.hasModelReadPermission("ModelB")).thenReturn(true);
		when(modelPermissionEvaluator.hasModelReadPermission("ModelC")).thenReturn(false);

		// When
		Set<String> result = service.findAllSubtypes("ModelA");

		// Then - ModelC excluded due to lack of permission
		Assert.assertEquals(result, Set.of("ModelB"));
	}

	@Test(description = "Should exclude models without read permission when finding direct subtypes")
	public void shouldExcludeModelsWithoutReadPermissionWhenFindingDirectSubtypes() {
		// Given - ModelA -> ModelB, ModelA -> ModelC; ModelC has no read permission
		when(modelService.findAllHeadersByType(ModelConstants.DOCUMENT_MODEL_TYPE)).thenReturn(List.of(
			makeHeader("ModelA", "subTypes", "ModelB,ModelC"),
			makeHeader("ModelB", null, null),
			makeHeader("ModelC", null, null)
		));
		when(modelPermissionEvaluator.hasModelReadPermission("ModelB")).thenReturn(true);
		when(modelPermissionEvaluator.hasModelReadPermission("ModelC")).thenReturn(false);

		// When
		Set<String> result = service.findDirectSubtypes("ModelA");

		// Then - ModelC excluded due to lack of permission
		Assert.assertEquals(result, Set.of("ModelB"));
	}

	@Test(description = "Should return subtypes from deeply nested hierarchy")
	public void shouldReturnSubtypesFromDeeplyNestedHierarchy() {
		// Given - ModelA -> ModelB -> ModelC -> ModelD
		when(modelService.findAllHeadersByType(ModelConstants.DOCUMENT_MODEL_TYPE)).thenReturn(List.of(
			makeHeader("ModelA", "subTypes", "ModelB"),
			makeHeader("ModelB", "subTypes", "ModelC"),
			makeHeader("ModelC", "subTypes", "ModelD"),
			makeHeader("ModelD", null, null)
		));
		when(modelPermissionEvaluator.hasModelReadPermission("ModelB")).thenReturn(true);
		when(modelPermissionEvaluator.hasModelReadPermission("ModelC")).thenReturn(true);
		when(modelPermissionEvaluator.hasModelReadPermission("ModelD")).thenReturn(true);

		// When
		Set<String> result = service.findAllSubtypes("ModelA");

		// Then - all subtypes in the chain are returned
		Assert.assertEquals(result, Set.of("ModelB", "ModelC", "ModelD"));
	}

	private SimpleTestHeader makeHeader(String id, String annotationName, String annotationValue) {
		SimpleTestHeader header = new SimpleTestHeader();
		header.setId(id);
		header.setModelType(ModelConstants.DOCUMENT_MODEL_TYPE);
		List<Annotation> annotations = new ArrayList<>();
		if (annotationName != null) {
			annotations.add(new SimpleAnnotation(annotationName, annotationValue));
		}
		header.setAnnotations(annotations);
		header.setLabels(List.of());
		return header;
	}

	@Data
	private static class SimpleTestHeader implements Header {
		private String id;
		private String modelType;
		private String modelVersion;
		private List<Locale> locales;
		private List<Label> labels;
		private List<Annotation> annotations;
		private List<ModelReference> modelReferences;

		@Override public String getDescription() {
			return "Test header";
		}
	}

	@Data @AllArgsConstructor
	private static class SimpleAnnotation implements Annotation {
		private String name;
		private String value;
	}
}
