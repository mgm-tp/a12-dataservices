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
import java.util.Set;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.model.Model;
import com.mgmtp.a12.model.header.Header;

import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;
import static com.mgmtp.a12.dataservices.relationship.model.RelationshipModel.SUB_TYPES_ANNOTATION_KEY;
import static com.mgmtp.a12.dataservices.relationship.model.RelationshipModel.SUPER_TYPES_ANNOTATION_KEY;

/**
 * Unit tests for `DefaultModelTypeService`.
 *
 * Tests are organized by method:
 * - `findAllSubtypes` — deep subtype resolution (no caching, cache disabled path)
 * - `findDirectSubtypes` — direct children only
 *
 * Tests for `findTopmostModelName` are in `UniqueConstraintHelperTest`.
 */
@Listeners(MockitoTestNGListener.class)
public class DefaultModelTypeServiceTest {

	@Mock
	private ModelService modelService;
	@Mock
	private ModelPermissionEvaluator<Model> modelPermissionEvaluator;
	@Mock
	private Authentication authentication;

	private DefaultModelTypeService service;

	@BeforeMethod
	public void setUp() {
		Mockito.reset(modelService, modelPermissionEvaluator, authentication);
		// Construct with Optional.empty() — cache disabled, no CacheManager needed.
		service = new DefaultModelTypeService(
			modelService,
			modelPermissionEvaluator,
			null,               // dataServicesCoreProperties — never reached when cache is empty
			Optional.empty()); // cacheManager — disabled
		// All models visible by default
		Mockito.lenient().when(modelPermissionEvaluator.hasModelReadPermission(Mockito.anyString())).thenReturn(true);
		// findAllSubtypes computes a cache key via UaaConnector.getCurrentUserName() unconditionally,
		// even when CacheManager is absent. UserUtils.resolveCurrentUser() throws IllegalStateException
		// for unrecognised principal types (including null), so we stub a proper UserDetails principal.
		UserDetails userDetails = mock(UserDetails.class);
		Mockito.lenient().when(userDetails.getUsername()).thenReturn("test-user");
		Mockito.lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@AfterMethod
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}

	// -------------------------------------------------------------------------
	// findAllSubtypes
	// -------------------------------------------------------------------------

	@Test(description = "Should return empty set for a model with no subtypes")
	public void shouldReturnEmptySetWhenNoSubtypes() {
		stubHeaders(List.of(
			makeHeader("MyModel", List.of(), List.of())));

		Set<String> result = service.findAllSubtypes("MyModel");

		Assert.assertNotNull(result);
		Assert.assertTrue(result.isEmpty());
	}

	@Test(description = "Should return all deep subtypes for a parent model")
	public void shouldReturnAllDeepSubtypes() {
		// BaseDocument → ElectronicBook → AudioBook
		stubHeaders(List.of(
			makeHeader("BaseDocument", List.of("ElectronicBook"), List.of()),
			makeHeader("ElectronicBook", List.of("AudioBook"), List.of("BaseDocument")),
			makeHeader("AudioBook", List.of(), List.of("ElectronicBook"))));

		Set<String> result = service.findAllSubtypes("BaseDocument");

		Assert.assertEquals(result, Set.of("ElectronicBook", "AudioBook"));
	}

	@Test(description = "Should return only direct subtype for a single-level hierarchy")
	public void shouldReturnDirectSubtypeOnly() {
		stubHeaders(List.of(
			makeHeader("BaseDocument", List.of("ElectronicBook"), List.of()),
			makeHeader("ElectronicBook", List.of(), List.of("BaseDocument"))));

		Set<String> result = service.findAllSubtypes("BaseDocument");

		Assert.assertEquals(result, Set.of("ElectronicBook"));
	}

	// -------------------------------------------------------------------------
	// findDirectSubtypes
	// -------------------------------------------------------------------------

	@Test(description = "Should return only direct children, not grandchildren")
	public void shouldReturnOnlyDirectChildren() {
		// BaseDocument → ElectronicBook → AudioBook
		stubHeaders(List.of(
			makeHeader("BaseDocument", List.of("ElectronicBook"), List.of()),
			makeHeader("ElectronicBook", List.of("AudioBook"), List.of("BaseDocument")),
			makeHeader("AudioBook", List.of(), List.of("ElectronicBook"))));

		Set<String> result = service.findDirectSubtypes("BaseDocument");

		// Only ElectronicBook is a direct child; AudioBook is a grandchild
		Assert.assertEquals(result, Set.of("ElectronicBook"));
	}

	@Test(description = "Should return all direct children when a model has multiple direct subtypes")
	public void shouldReturnAllDirectChildren() {
		// BaseDocument → ElectronicBook, PrintBook
		stubHeaders(List.of(
			makeHeader("BaseDocument", List.of("ElectronicBook", "PrintBook"), List.of()),
			makeHeader("ElectronicBook", List.of(), List.of("BaseDocument")),
			makeHeader("PrintBook", List.of(), List.of("BaseDocument"))));

		Set<String> result = service.findDirectSubtypes("BaseDocument");

		Assert.assertEquals(result, Set.of("ElectronicBook", "PrintBook"));
	}

	// -------------------------------------------------------------------------
	// helpers
	// -------------------------------------------------------------------------

	private void stubHeaders(List<Header> headers) {
		when(modelService.findAllHeadersByType(DOCUMENT_MODEL_TYPE)).thenReturn(headers);
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

