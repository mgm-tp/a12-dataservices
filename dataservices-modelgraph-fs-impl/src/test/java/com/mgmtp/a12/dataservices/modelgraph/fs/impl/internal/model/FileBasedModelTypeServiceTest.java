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
package com.mgmtp.a12.dataservices.modelgraph.fs.impl.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
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

/**
 * Unit tests for `FileBasedModelTypeService`.
 *
 */
public class FileBasedModelTypeServiceTest {

	private ModelService modelService;
	@SuppressWarnings("unchecked")
	private ModelPermissionEvaluator<Model> modelPermissionEvaluator;
	private FileBasedModelTypeService service;

	@BeforeMethod
	@SuppressWarnings("unchecked")
	public void setUp() {
		modelService = mock(ModelService.class);
		modelPermissionEvaluator = mock(ModelPermissionEvaluator.class);
		service = new FileBasedModelTypeService(modelService, modelPermissionEvaluator);
	}

	@Test(description = "Should find all subtypes when subtype hierarchy exists")
	public void shouldFindAllSubtypesWhenSubtypeHierarchyExists() {
		// Given - ParentModel -> ChildModel (using subTypes annotation)
		when(modelService.findAllHeadersByType(ModelConstants.DOCUMENT_MODEL_TYPE)).thenReturn(List.of(
			makeHeader("ParentModel", "subTypes", "ChildModel"),
			makeHeader("ChildModel", null, null)
		));
		when(modelPermissionEvaluator.hasModelReadPermission("ChildModel")).thenReturn(true);

		// When
		Set<String> result = service.findAllSubtypes("ParentModel");

		// Then
		assertThat(result).contains("ChildModel");
	}

	@Test(description = "Should return empty set when model has no subtypes")
	public void shouldReturnEmptySetWhenModelHasNoSubtypes() {
		// Given - model with no subtypes
		when(modelService.findAllHeadersByType(ModelConstants.DOCUMENT_MODEL_TYPE)).thenReturn(List.of(
			makeHeader("StandaloneModel", null, null)
		));

		// When
		Set<String> result = service.findAllSubtypes("StandaloneModel");

		// Then
		assertThat(result).isEmpty();
	}

	@Test(description = "Should find direct subtypes only when model has subtype hierarchy")
	public void shouldFindDirectSubtypesOnly() {
		// Given - ParentModel -> ChildModel -> GrandChildModel
		when(modelService.findAllHeadersByType(ModelConstants.DOCUMENT_MODEL_TYPE)).thenReturn(List.of(
			makeHeader("ParentModel", "subTypes", "ChildModel"),
			makeHeader("ChildModel", "subTypes", "GrandChildModel"),
			makeHeader("GrandChildModel", null, null)
		));
		when(modelPermissionEvaluator.hasModelReadPermission("ChildModel")).thenReturn(true);
		when(modelPermissionEvaluator.hasModelReadPermission("GrandChildModel")).thenReturn(true);

		// When
		Set<String> result = service.findDirectSubtypes("ParentModel");

		// Then - only direct subtypes, not deeply nested ones
		assertThat(result).contains("ChildModel");
		assertThat(result).doesNotContain("GrandChildModel");
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
