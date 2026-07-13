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
package com.mgmtp.a12.dataservices.initialization.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.events.ModelsAfterImportEvent;
import com.mgmtp.a12.dataservices.utils.internal.DsResourceUtils;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderParser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link RuntimeModelImporter} covering create-or-update logic
 * and event publishing.
 */
public class RuntimeModelImporterTest {

	@Mock private ModelService modelService;
	@Mock private HeaderParser headerParser;
	@Mock private DsResourceUtils dsResourceUtils;
	@Mock private ApplicationEventPublisher eventPublisher;

	private RuntimeModelImporter modelImporter;
	private ModelImportConfiguration configuration;

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		modelImporter = new RuntimeModelImporter(modelService, headerParser, dsResourceUtils, eventPublisher);
		configuration = ModelImportConfiguration.builder()
			.overwriteModelsDefault(true)
			.overwriteDocumentModels(true)
			.build();
	}

	@Test(description = "Should create model when it does not exist")
	public void shouldCreateModelWhenNotExists() throws Exception {
		String modelContent = "{\"header\":{\"id\":\"TestModel\"}}";
		Header header = mockHeader("TestModel");
		Resource resource = mockResource(modelContent);

		when(dsResourceUtils.getJsonResources("classpath:/models/")).thenReturn(Stream.of(resource));
		when(headerParser.parseJson(modelContent)).thenReturn(header);
		when(modelService.exists(header)).thenReturn(false);

		SortedSet<String> result = modelImporter.importModels("classpath:/models/", configuration, false);

		assertEquals(result, new TreeSet<>(Set.of("TestModel")));
		verify(modelService).create(modelContent);
		verify(modelService, never()).update(any());
		verify(eventPublisher).publishEvent(any(ModelsAfterImportEvent.class));
	}

	@Test(description = "Should update model when it already exists")
	public void shouldUpdateModelWhenAlreadyExists() throws Exception {
		String modelContent = "{\"header\":{\"id\":\"ExistingModel\"}}";
		Header header = mockHeader("ExistingModel");
		Resource resource = mockResource(modelContent);

		when(dsResourceUtils.getJsonResources("classpath:/models/")).thenReturn(Stream.of(resource));
		when(headerParser.parseJson(modelContent)).thenReturn(header);
		when(modelService.exists(header)).thenReturn(true);

		SortedSet<String> result = modelImporter.importModels("classpath:/models/", configuration, false);

		assertEquals(result, new TreeSet<>(Set.of("ExistingModel")));
		verify(modelService, never()).create(any());
		verify(modelService).update(modelContent);
	}

	@Test(description = "Should throw when failOnError is true and model fails to import")
	public void shouldThrowWhenFailOnErrorAndModelFails() throws Exception {
		String modelContent = "{\"header\":{\"id\":\"BrokenModel\"}}";
		Header header = mockHeader("BrokenModel");
		Resource resource = mockResource(modelContent);

		when(dsResourceUtils.getJsonResources("archive:/models.zip")).thenReturn(Stream.of(resource));
		when(headerParser.parseJson(modelContent)).thenReturn(header);
		when(modelService.exists(header)).thenReturn(false);
		doThrow(new RuntimeException("broken")).when(modelService).create(modelContent);

		assertThrows(InvalidInputException.class, () -> modelImporter.importModels("archive:/models.zip", configuration, true));
	}

	@Test(description = "Should continue importing other models when one fails and failOnError is false")
	public void shouldContinueWhenModelFailsAndFailOnErrorFalse() throws Exception {
		String goodModel = "{\"header\":{\"id\":\"GoodModel\"}}";
		String badModel = "{\"header\":{\"id\":\"BadModel\"}}";
		Header goodHeader = mockHeader("GoodModel");
		Header badHeader = mockHeader("BadModel");
		Resource goodResource = mockResource(goodModel);
		Resource badResource = mockResource(badModel);

		when(dsResourceUtils.getJsonResources("classpath:/models/"))
			.thenReturn(Stream.of(badResource, goodResource));
		when(headerParser.parseJson(goodModel)).thenReturn(goodHeader);
		when(headerParser.parseJson(badModel)).thenReturn(badHeader);
		when(modelService.exists(any())).thenReturn(false);
		doThrow(new RuntimeException("broken")).when(modelService).create(badModel);

		SortedSet<String> result = modelImporter.importModels("classpath:/models/", configuration, false);

		assertEquals(result, new TreeSet<>(Set.of("GoodModel")));
		verify(modelService).create(goodModel);
	}

	@Test(description = "Should not publish event when no models imported")
	public void shouldNotPublishEventWhenNoModelsImported() throws Exception {
		when(dsResourceUtils.getJsonResources("classpath:/empty/")).thenReturn(Stream.empty());

		SortedSet<String> result = modelImporter.importModels("classpath:/empty/", configuration, false);

		assertTrue(result.isEmpty());
		verify(eventPublisher, never()).publishEvent(any());
	}

	@Test(description = "Should publish event with all imported model headers")
	public void shouldPublishEventWithAllImportedHeaders() throws Exception {
		String modelA = "{\"header\":{\"id\":\"ModelA\"}}";
		String modelB = "{\"header\":{\"id\":\"ModelB\"}}";
		Header headerA = mockHeader("ModelA");
		Header headerB = mockHeader("ModelB");
		Resource resourceA = mockResource(modelA);
		Resource resourceB = mockResource(modelB);

		when(dsResourceUtils.getJsonResources("classpath:/models/"))
			.thenReturn(Stream.of(resourceA, resourceB));
		when(headerParser.parseJson(modelA)).thenReturn(headerA);
		when(headerParser.parseJson(modelB)).thenReturn(headerB);
		when(modelService.exists(any())).thenReturn(false);

		modelImporter.importModels("classpath:/models/", configuration, false);

		ArgumentCaptor<ModelsAfterImportEvent> captor = ArgumentCaptor.forClass(ModelsAfterImportEvent.class);
		verify(eventPublisher).publishEvent(captor.capture());
		assertEquals(captor.getValue().getImportedModels().size(), 2);
	}

	@Test(description = "Should return sorted IDs from archive import")
	public void shouldReturnSortedIdsFromArchiveImport() throws Exception {
		String modelB = "{\"header\":{\"id\":\"Zebra\"}}";
		String modelA = "{\"header\":{\"id\":\"Alpha\"}}";
		Header headerB = mockHeader("Zebra");
		Header headerA = mockHeader("Alpha");
		Resource resourceB = mockResource(modelB);
		Resource resourceA = mockResource(modelA);

		when(dsResourceUtils.getJsonResources("archive:/models.zip"))
			.thenReturn(Stream.of(resourceB, resourceA));
		when(headerParser.parseJson(modelB)).thenReturn(headerB);
		when(headerParser.parseJson(modelA)).thenReturn(headerA);
		when(modelService.exists(any())).thenReturn(false);

		SortedSet<String> result = modelImporter.importModels("archive:/models.zip", configuration, true);

		assertEquals(result, new TreeSet<>(Set.of("Alpha", "Zebra")));
	}

	@Test(description = "Should create model via importRuntimeModel when not exists")
	public void shouldCreateViaImportRuntimeModel() throws Exception {
		String content = "{\"header\":{\"id\":\"NewModel\"}}";
		Header header = mockHeader("NewModel");

		when(headerParser.parseJson(content)).thenReturn(header);
		when(modelService.exists(header)).thenReturn(false);

		Header result = modelImporter.importRuntimeModel(content, configuration);

		assertEquals(result, header);
		verify(modelService).create(content);
		verify(modelService, never()).update(any());
	}

	@Test(description = "Should update model via importRuntimeModel when already exists")
	public void shouldUpdateViaImportRuntimeModel() throws Exception {
		String content = "{\"header\":{\"id\":\"ExistingModel\"}}";
		Header header = mockHeader("ExistingModel");

		when(headerParser.parseJson(content)).thenReturn(header);
		when(modelService.exists(header)).thenReturn(true);

		Header result = modelImporter.importRuntimeModel(content, configuration);

		assertEquals(result, header);
		verify(modelService, never()).create(any());
		verify(modelService).update(content);
	}

	@Test(description = "Should skip existing model when overwrite is disabled")
	public void shouldSkipExistingModelWhenOverwriteDisabled() throws Exception {
		String content = "{\"header\":{\"id\":\"ProtectedModel\"}}";
		Header header = mockHeader("ProtectedModel");
		ModelImportConfiguration noOverwrite = ModelImportConfiguration.builder()
			.overwriteModelsDefault(false)
			.overwriteDocumentModels(false)
			.build();

		when(headerParser.parseJson(content)).thenReturn(header);
		when(modelService.exists(header)).thenReturn(true);

		Header result = modelImporter.importRuntimeModel(content, noOverwrite);

		assertEquals(result, null);
		verify(modelService, never()).create(any());
		verify(modelService, never()).update(any());
	}

	private static Header mockHeader(String id) {
		Header header = mock(Header.class);
		when(header.getId()).thenReturn(id);
		when(header.getModelType()).thenReturn("document");
		return header;
	}

	private static Resource mockResource(String content) throws IOException {
		Resource resource = mock(Resource.class);
		when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
		when(resource.getDescription()).thenReturn("mock-resource");
		return resource;
	}
}
