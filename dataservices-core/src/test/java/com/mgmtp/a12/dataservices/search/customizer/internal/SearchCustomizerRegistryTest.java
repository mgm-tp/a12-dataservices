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
package com.mgmtp.a12.dataservices.search.customizer.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.ModelFieldEntity;
import com.mgmtp.a12.dataservices.search.customizer.DocumentFieldContext;
import com.mgmtp.a12.dataservices.search.customizer.ModelFieldsContext;
import com.mgmtp.a12.dataservices.search.customizer.SearchCustomizer;
import com.mgmtp.a12.dataservices.search.customizer.SearchDataContext;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

public class SearchCustomizerRegistryTest {

	private IDocumentModelSearchService documentModelSearchService;
	private SearchCustomizer mockCustomizer1;
	private SearchCustomizer mockCustomizer2;

	@BeforeMethod public void setUp() {
		documentModelSearchService = mock(IDocumentModelSearchService.class);
		mockCustomizer1 = mock(SearchCustomizer.class);
		mockCustomizer2 = mock(SearchCustomizer.class);
	}

	@Test public void testEmptyCustomizerList() {
		assertFalse(new SearchCustomizerRegistry(null).hasCustomizers());
	}

	@Test public void testWithEmptyList() {
		assertFalse(new SearchCustomizerRegistry(new ArrayList<>()).hasCustomizers());
	}

	@Test public void testWithSingleCustomizer() {
		assertTrue(new SearchCustomizerRegistry(List.of(mockCustomizer1)).hasCustomizers());
	}

	@Test public void testWithMultipleCustomizers() {
		assertTrue(new SearchCustomizerRegistry(List.of(mockCustomizer1, mockCustomizer2)).hasCustomizers());
	}

	@Test public void testCustomizeSearchDataWithNoCustomizers() {
		String baseSearchData = "base search data";

		String result = new SearchCustomizerRegistry(null)
			.customizeSearchData(documentModelSearchService, mock(DocumentV2.class), "TestModel", baseSearchData);

		assertEquals(result, baseSearchData);
		verify(mockCustomizer1, never()).customizeSearchData(org.mockito.ArgumentMatchers.any());
	}

	@Test public void testCustomizeSearchDataWithCustomizers() {

		String result = new SearchCustomizerRegistry(List.of(mockCustomizer1, mockCustomizer2))
			.customizeSearchData(documentModelSearchService, mock(DocumentV2.class), "TestModel", "base search data");

		assertNotNull(result);
		verify(mockCustomizer1, times(1)).customizeSearchData(org.mockito.ArgumentMatchers.any(SearchDataContext.class));
		verify(mockCustomizer2, times(1)).customizeSearchData(org.mockito.ArgumentMatchers.any(SearchDataContext.class));
	}

	@Test public void testCustomizeDocumentFieldsWithNoCustomizers() {

		assertThrows(IllegalStateException.class, () ->
			new SearchCustomizerRegistry(null)
				.customizeDocumentFields(documentModelSearchService, mock(DocumentReference.class), mock(DocumentPointer.class),
					mock(FieldInstanceV2.class), mock(IField.class), (model, path) -> 1L));
	}

	@Test public void testCustomizeDocumentFieldsWithCustomizers() {
		DocumentReference documentReference = mock(DocumentReference.class);
		DocumentPointer pointer = mock(DocumentPointer.class);

		when(documentReference.getDocumentModelName()).thenReturn("TestModel");
		when(documentReference.toString()).thenReturn("TestModel/123");
		when(pointer.fullName()).thenReturn("testField");
		when(pointer.repetitionIndexes()).thenReturn(List.of());

		DocumentFieldContextImpl result = new SearchCustomizerRegistry(List.of(mockCustomizer1, mockCustomizer2))
			.customizeDocumentFields(documentModelSearchService, documentReference, pointer, mock(FieldInstanceV2.class),
				mock(IField.class), (model, path) -> 1L);

		assertNotNull(result);
		verify(mockCustomizer1, times(1)).customizeDocumentFields(org.mockito.ArgumentMatchers.any(DocumentFieldContext.class));
		verify(mockCustomizer2, times(1)).customizeDocumentFields(org.mockito.ArgumentMatchers.any(DocumentFieldContext.class));
	}

	@Test public void testCustomizeModelFieldsWithNoCustomizers() {

		new SearchCustomizerRegistry(null)
			.customizeModelFields("TestModel", "testField", mock(IField.class), mock(IFieldType.class), ModelFieldEntity.builder(), new HashMap<>());

		verify(mockCustomizer1, never()).customizeModelFields(org.mockito.ArgumentMatchers.any());
	}

	@Test public void testCustomizeModelFieldsWithCustomizers() {

		new SearchCustomizerRegistry(List.of(mockCustomizer1, mockCustomizer2))
			.customizeModelFields("TestModel", "testField", mock(IField.class), mock(IFieldType.class), ModelFieldEntity.builder(), new HashMap<>());

		verify(mockCustomizer1, times(1)).customizeModelFields(org.mockito.ArgumentMatchers.any(ModelFieldsContext.class));
		verify(mockCustomizer2, times(1)).customizeModelFields(org.mockito.ArgumentMatchers.any(ModelFieldsContext.class));
	}

	@Test public void testCustomizersCalledInOrder() {
		List<String> callOrder = new ArrayList<>();

		SearchCustomizer orderedCustomizer1 = new SearchCustomizer() {
			@Override public void customizeSearchData(SearchDataContext context) {
				callOrder.add("customizer1-searchData");
			}

			@Override public void customizeDocumentFields(DocumentFieldContext context) {
				callOrder.add("customizer1-documentFields");
			}

			@Override public void customizeModelFields(ModelFieldsContext context) {
				callOrder.add("customizer1-modelFields");
			}
		};

		SearchCustomizer orderedCustomizer2 = new SearchCustomizer() {
			@Override public void customizeSearchData(SearchDataContext context) {
				callOrder.add("customizer2-searchData");
			}

			@Override public void customizeDocumentFields(DocumentFieldContext context) {
				callOrder.add("customizer2-documentFields");
			}

			@Override public void customizeModelFields(ModelFieldsContext context) {
				callOrder.add("customizer2-modelFields");
			}
		};

		new SearchCustomizerRegistry(List.of(orderedCustomizer1, orderedCustomizer2))
			.customizeSearchData(documentModelSearchService, mock(DocumentV2.class), "TestModel", "base data");

		assertEquals(callOrder.size(), 2);
		assertEquals(callOrder.get(0), "customizer1-searchData");
		assertEquals(callOrder.get(1), "customizer2-searchData");
	}
}
