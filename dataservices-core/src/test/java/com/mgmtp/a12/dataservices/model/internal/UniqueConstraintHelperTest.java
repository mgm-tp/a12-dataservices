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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModelContent;
import com.mgmtp.a12.kernel.md.model.api.IDocumentUniquenessCriterion;
import com.mgmtp.a12.model.header.Header;

import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;
import static com.mgmtp.a12.dataservices.relationship.model.RelationshipModel.SUB_TYPES_ANNOTATION_KEY;
import static com.mgmtp.a12.dataservices.relationship.model.RelationshipModel.SUPER_TYPES_ANNOTATION_KEY;

/**
 * Unit tests for `UniqueConstraintHelper.findTopmostModelName`.
 *
 * Tests cover per-constraint topmost model resolution across various inheritance hierarchy shapes.
 */
@Listeners(MockitoTestNGListener.class)
public class UniqueConstraintHelperTest {

	@Mock
	private ModelService modelService;
	@Mock
	private IModelLoader<IDocumentModel> documentModelLoader;

	private UniqueConstraintHelper service;

	@BeforeMethod
	public void setUp() {
		Mockito.reset(modelService, documentModelLoader);
		service = new UniqueConstraintHelper(
			modelService,
			documentModelLoader,
			null,              // dataServicesCoreProperties — never reached when cache is empty
			Optional.empty()); // cacheManager — disabled
	}

	// -------------------------------------------------------------------------
	// flat model (no parent)
	// -------------------------------------------------------------------------

	@Test(description = "Should return the model itself when it has no parent")
	public void shouldReturnSelfWhenNoParent() {
		// Given — flat model with no hierarchy
		stubHeaders(List.of(
			makeHeader("MyModel", List.of(), List.of())));
		stubConstraints("MyModel", List.of("isbn_unique"));

		// When
		String result = service.findTopmostModelName("MyModel", "isbn_unique");

		// Then
		Assert.assertEquals(result, "MyModel");
	}

	@Test(description = "Should return the model itself when the constraint is unknown (not defined anywhere)")
	public void shouldReturnSelfWhenConstraintNotFoundInHierarchy() {
		// Given — child model in hierarchy, constraint exists nowhere
		stubHeaders(List.of(
			makeHeader("BaseDocument", List.of("ElectronicBook"), List.of()),
			makeHeader("ElectronicBook", List.of(), List.of("BaseDocument"))));
		stubConstraints("BaseDocument", List.of());
		stubConstraints("ElectronicBook", List.of());

		// When
		String result = service.findTopmostModelName("ElectronicBook", "nonexistent_constraint");

		// Then — falls back to submitted model
		Assert.assertEquals(result, "ElectronicBook");
	}

	// -------------------------------------------------------------------------
	// shared constraint walks up to root
	// -------------------------------------------------------------------------

	@Test(description = "Should return the root model when the constraint is defined in all ancestors")
	public void shouldReturnRootWhenConstraintDefinedInAllAncestors() {
		// Given — BaseDocument → ElectronicBook both define isbn_unique
		stubHeaders(List.of(
			makeHeader("BaseDocument", List.of("ElectronicBook"), List.of()),
			makeHeader("ElectronicBook", List.of(), List.of("BaseDocument"))));
		stubConstraints("BaseDocument", List.of("isbn_unique"));
		stubConstraints("ElectronicBook", List.of("isbn_unique"));

		// When — start from child
		String result = service.findTopmostModelName("ElectronicBook", "isbn_unique");

		// Then — resolves all the way up to root
		Assert.assertEquals(result, "BaseDocument");
	}

	@Test(description = "Should return the topmost model across a three-level hierarchy")
	public void shouldReturnRootAcrossThreeLevels() {
		// Given — RootModel → MidModel → LeafModel, all define shared_constraint
		stubHeaders(List.of(
			makeHeader("RootModel", List.of("MidModel"), List.of()),
			makeHeader("MidModel", List.of("LeafModel"), List.of("RootModel")),
			makeHeader("LeafModel", List.of(), List.of("MidModel"))));
		stubConstraints("RootModel", List.of("shared_constraint"));
		stubConstraints("MidModel", List.of("shared_constraint"));
		stubConstraints("LeafModel", List.of("shared_constraint"));

		// When
		String result = service.findTopmostModelName("LeafModel", "shared_constraint");

		// Then
		Assert.assertEquals(result, "RootModel");
	}

	// -------------------------------------------------------------------------
	// constraint defined only on a subtype
	// -------------------------------------------------------------------------

	@Test(description = "Should return the child model when only the child defines the constraint")
	public void shouldReturnChildWhenOnlyChildDefinesConstraint() {
		// Given — BaseDocument has no constraints; ElectronicBook adds format_unique
		stubHeaders(List.of(
			makeHeader("BaseDocument", List.of("ElectronicBook"), List.of()),
			makeHeader("ElectronicBook", List.of(), List.of("BaseDocument"))));
		stubConstraints("BaseDocument", List.of());
		stubConstraints("ElectronicBook", List.of("format_unique"));

		// When
		String result = service.findTopmostModelName("ElectronicBook", "format_unique");

		// Then — parent does not have it, so ElectronicBook is the topmost
		Assert.assertEquals(result, "ElectronicBook");
	}

	@Test(description = "Should resolve two constraints independently across the same hierarchy")
	public void shouldResolveTwoConstraintsIndependentlyInSameHierarchy() {
		// Given — BaseDocument → ElectronicBook
		// isbn_unique: both define it → topmost = BaseDocument
		// format_unique: only ElectronicBook defines it → topmost = ElectronicBook
		stubHeaders(List.of(
			makeHeader("BaseDocument", List.of("ElectronicBook"), List.of()),
			makeHeader("ElectronicBook", List.of(), List.of("BaseDocument"))));
		stubConstraints("BaseDocument", List.of("isbn_unique"));
		stubConstraints("ElectronicBook", List.of("isbn_unique", "format_unique"));

		Assert.assertEquals(service.findTopmostModelName("ElectronicBook", "isbn_unique"), "BaseDocument");
		Assert.assertEquals(service.findTopmostModelName("ElectronicBook", "format_unique"), "ElectronicBook");
	}

	@Test(description = "Should stop at the mid-level model when the constraint is absent from the root")
	public void shouldStopAtMidLevelWhenRootLacksConstraint() {
		// Given — RootModel → MidModel → LeafModel
		// mid_constraint only defined in MidModel and LeafModel (not RootModel)
		stubHeaders(List.of(
			makeHeader("RootModel", List.of("MidModel"), List.of()),
			makeHeader("MidModel", List.of("LeafModel"), List.of("RootModel")),
			makeHeader("LeafModel", List.of(), List.of("MidModel"))));
		stubConstraints("RootModel", List.of());
		stubConstraints("MidModel", List.of("mid_constraint"));
		stubConstraints("LeafModel", List.of("mid_constraint"));

		// When
		String result = service.findTopmostModelName("LeafModel", "mid_constraint");

		// Then — stops at MidModel (root doesn't have it)
		Assert.assertEquals(result, "MidModel");
	}

	// -------------------------------------------------------------------------
	// helpers
	// -------------------------------------------------------------------------

	private void stubHeaders(List<Header> headers) {
		when(modelService.findAllHeadersByType(DOCUMENT_MODEL_TYPE)).thenReturn(headers);
	}

	private void stubConstraints(String modelName, List<String> constraintNames) {
		List<IDocumentUniquenessCriterion> criteria = constraintNames.stream()
			.map(name -> {
				IDocumentUniquenessCriterion c = mock(IDocumentUniquenessCriterion.class);
				Mockito.lenient().when(c.getName()).thenReturn(name);
				return c;
			})
			.toList();

		IDocumentModelContent content = mock(IDocumentModelContent.class);
		Mockito.lenient().when(content.getDocumentUniquenessCriteria()).thenReturn(criteria);
		IDocumentModel model = mock(IDocumentModel.class);
		Mockito.lenient().when(model.getContent()).thenReturn(content);
		Mockito.lenient().when(documentModelLoader.loadModel(modelName)).thenReturn(model);
	}

	private static Header makeHeader(String name, Collection<String> subtypes, Collection<String> supertypes) {
		ModelHeaderEntity header = new ModelHeaderEntity();
		header.setId(name);
		header.setModelType(DOCUMENT_MODEL_TYPE);
		header.setAnnotations(Map.of(
			SUB_TYPES_ANNOTATION_KEY, String.join(",", subtypes),
			SUPER_TYPES_ANNOTATION_KEY, String.join(",", supertypes)));
		return header;
	}
}

