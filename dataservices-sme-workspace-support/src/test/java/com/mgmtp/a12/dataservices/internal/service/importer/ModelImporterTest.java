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
package com.mgmtp.a12.dataservices.internal.service.importer;

import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.initialization.internal.ModelImportConfiguration;
import com.mgmtp.a12.dataservices.initialization.internal.RuntimeModelImporter;
import com.mgmtp.a12.dataservices.model.events.ModelsAfterImportEvent;
import com.mgmtp.a12.dataservices.wcf.domain.ModelTuple;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderParseException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link ModelImporter} covering import behavior, failure handling,
 * and event publishing.
 */
public class ModelImporterTest {

	@Mock private RuntimeModelImporter runtimeModelImporter;
	@Mock private ApplicationEventPublisher eventPublisher;

	private ModelImportConfiguration configuration;
	private ModelImporter modelImporter;

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		configuration = ModelImportConfiguration.builder()
			.overwriteModelsDefault(true)
			.overwriteDocumentModels(true)
			.build();
		modelImporter = new ModelImporter(runtimeModelImporter, configuration, eventPublisher);
	}

	@Test(description = "Should import model and publish event")
	public void shouldImportModelAndPublishEvent() throws Exception {
		Header header = mockHeader("TestModel");
		ModelTuple modelTuple = mockModelTuple("TestModel", "{\"content\": true}");
		when(runtimeModelImporter.importRuntimeModel("{\"content\": true}", configuration)).thenReturn(header);

		modelImporter.importModels(Map.of("TestModel", modelTuple));

		verify(runtimeModelImporter).importRuntimeModel("{\"content\": true}", configuration);
		ArgumentCaptor<ModelsAfterImportEvent> captor = ArgumentCaptor.forClass(ModelsAfterImportEvent.class);
		verify(eventPublisher).publishEvent(captor.capture());
		assertEquals(captor.getValue().getImportedModels().size(), 1);
	}

	@Test(description = "Should import multiple models and publish single event with all headers")
	public void shouldImportMultipleModelsAndPublishSingleEvent() throws Exception {
		Header headerA = mockHeader("ModelA");
		Header headerB = mockHeader("ModelB");
		ModelTuple tupleA = mockModelTuple("ModelA", "{\"a\": true}");
		ModelTuple tupleB = mockModelTuple("ModelB", "{\"b\": true}");
		when(runtimeModelImporter.importRuntimeModel("{\"a\": true}", configuration)).thenReturn(headerA);
		when(runtimeModelImporter.importRuntimeModel("{\"b\": true}", configuration)).thenReturn(headerB);

		modelImporter.importModels(Map.of("ModelA", tupleA, "ModelB", tupleB));

		ArgumentCaptor<ModelsAfterImportEvent> captor = ArgumentCaptor.forClass(ModelsAfterImportEvent.class);
		verify(eventPublisher).publishEvent(captor.capture());
		assertEquals(captor.getValue().getImportedModels().size(), 2);
	}

	@Test(description = "Should not publish event when models map is empty")
	public void shouldNotPublishEventWhenEmpty() {
		modelImporter.importModels(Map.of());

		verify(eventPublisher, never()).publishEvent(any());
	}

	@Test(description = "Should continue importing remaining models when one fails with BaseException")
	public void shouldContinueWhenModelFailsWithBaseException() throws Exception {
		Header headerGood = mockHeader("GoodModel");
		ModelTuple tupleBad = mockModelTuple("BadModel", "{\"bad\": true}");
		ModelTuple tupleGood = mockModelTuple("GoodModel", "{\"good\": true}");
		when(runtimeModelImporter.importRuntimeModel("{\"bad\": true}", configuration))
			.thenThrow(new HeaderParseException("parse error"));
		when(runtimeModelImporter.importRuntimeModel("{\"good\": true}", configuration)).thenReturn(headerGood);

		modelImporter.importModels(Map.of("BadModel", tupleBad, "GoodModel", tupleGood));

		ArgumentCaptor<ModelsAfterImportEvent> captor = ArgumentCaptor.forClass(ModelsAfterImportEvent.class);
		verify(eventPublisher).publishEvent(captor.capture());
		assertEquals(captor.getValue().getImportedModels().size(), 1);
	}

	@Test(description = "Should continue importing remaining models when one fails with RuntimeException")
	public void shouldContinueWhenModelFailsWithRuntimeException() throws Exception {
		Header headerGood = mockHeader("GoodModel");
		ModelTuple tupleBad = mockModelTuple("BadModel", "{\"bad\": true}");
		ModelTuple tupleGood = mockModelTuple("GoodModel", "{\"good\": true}");
		when(runtimeModelImporter.importRuntimeModel("{\"bad\": true}", configuration))
			.thenThrow(new RuntimeException("unexpected error"));
		when(runtimeModelImporter.importRuntimeModel("{\"good\": true}", configuration)).thenReturn(headerGood);

		modelImporter.importModels(Map.of("BadModel", tupleBad, "GoodModel", tupleGood));

		ArgumentCaptor<ModelsAfterImportEvent> captor = ArgumentCaptor.forClass(ModelsAfterImportEvent.class);
		verify(eventPublisher).publishEvent(captor.capture());
		assertEquals(captor.getValue().getImportedModels().size(), 1);
	}

	@Test(description = "Should not publish event when all models fail")
	public void shouldNotPublishEventWhenAllModelsFail() throws Exception {
		ModelTuple tuple = mockModelTuple("BadModel", "{\"bad\": true}");
		when(runtimeModelImporter.importRuntimeModel(anyString(), any(ModelImportConfiguration.class)))
			.thenThrow(new RuntimeException("broken"));

		modelImporter.importModels(Map.of("BadModel", tuple));

		verify(eventPublisher, never()).publishEvent(any());
	}

	private static Header mockHeader(String id) {
		Header header = mock(Header.class);
		when(header.getId()).thenReturn(id);
		return header;
	}

	private static ModelTuple mockModelTuple(String modelId, String content) {
		Header header = mockHeader(modelId);
		ModelTuple tuple = mock(ModelTuple.class);
		when(tuple.getHeader()).thenReturn(header);
		when(tuple.getContent()).thenReturn(content);
		return tuple;
	}
}
