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

import java.util.Collections;
import java.util.Set;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.TestHeader;
import com.mgmtp.a12.dataservices.document.uniqueconstraint.internal.UniqueConstraintValidator;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.ModelConstants;
import com.mgmtp.a12.dataservices.model.events.ModelAfterCreateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelAfterDeleteEvent;
import com.mgmtp.a12.dataservices.model.events.ModelAfterUpdateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelBeforeDeleteEvent;
import com.mgmtp.a12.dataservices.model.events.ModelBeforeUpdateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelsAfterImportEvent;

@Listeners(MockitoTestNGListener.class)
public class ModelChangeListenerTest {

	private static final String TEST_MODEL_ID = "TestDocumentModel";
	private static final String TEST_RELATIONSHIP_MODEL_ID = "TestRelationshipModel";

	@Mock private ModelCacheManager modelCacheManager;
	@Mock private DataServicesModelCodeCache modelCodeCache;
	@Mock private UniqueConstraintValidator uniqueConstraintValidator;

	@InjectMocks private ModelChangeListener modelChangeListener;

	@BeforeMethod
	public void setUp() {
		Mockito.reset(modelCacheManager, modelCodeCache, uniqueConstraintValidator);
	}

	@Test(description = "Should invalidate model graph caches on model created event")
	public void shouldInvalidateModelGraphCachesOnModelCreated() {
		GenericModel model = createDocumentModel(TEST_MODEL_ID);
		ModelAfterCreateEvent event = new ModelAfterCreateEvent(model);

		modelChangeListener.listenOnModelCreated(event);

		Mockito.verify(modelCacheManager).invalidateModelGraphCaches();
		Mockito.verifyNoMoreInteractions(modelCacheManager);
		Mockito.verifyNoInteractions(modelCodeCache);
	}

	@Test(description = "Should invalidate caches on model updated event")
	public void shouldInvalidateCachesOnModelUpdated() {
		GenericModel model = createDocumentModel(TEST_MODEL_ID);
		GenericModel oldModel = createDocumentModel(TEST_MODEL_ID);
		ModelAfterUpdateEvent event = new ModelAfterUpdateEvent(oldModel, model);

		modelChangeListener.listenOnModelUpdated(event);

		Mockito.verify(modelCacheManager).invalidateModelGraphCaches();
		Mockito.verify(modelCacheManager).invalidateValidationCodeCacheForDocumentModel(TEST_MODEL_ID);
		Mockito.verify(modelCacheManager).invalidateSecuredModelReadCaches(TEST_MODEL_ID);
		Mockito.verify(modelCacheManager).invalidateDocumentModelSearchServiceCache();
		Mockito.verify(modelCacheManager).invalidateModelReadCaches(TEST_MODEL_ID);
	}

	@Test(description = "Should invalidate caches on model deleted event")
	public void shouldInvalidateCachesOnModelDeleted() {
		GenericModel model = createDocumentModel(TEST_MODEL_ID);
		ModelAfterDeleteEvent event = new ModelAfterDeleteEvent(model);

		modelChangeListener.listenOnModelUpdated(event);

		Mockito.verify(modelCacheManager).invalidateModelGraphCaches();
		Mockito.verify(modelCacheManager).invalidateValidationCodeCacheForDocumentModel(TEST_MODEL_ID);
		Mockito.verify(modelCacheManager).invalidateSecuredModelReadCaches(TEST_MODEL_ID);
		Mockito.verify(modelCacheManager).invalidateDocumentModelSearchServiceCache();
		Mockito.verify(modelCacheManager).invalidateModelReadCaches(TEST_MODEL_ID);
	}

	@Test(description = "Should remove model code cache on before update event for document model")
	public void shouldRemoveModelCodeCacheOnBeforeUpdateEventForDocumentModel() {
		GenericModel model = createDocumentModel(TEST_MODEL_ID);
		GenericModel oldModel = createDocumentModel(TEST_MODEL_ID);
		ModelBeforeUpdateEvent event = new ModelBeforeUpdateEvent(oldModel, model);

		modelChangeListener.listenBeforeModelUpdated(event);

		Mockito.verify(modelCodeCache).remove(TEST_MODEL_ID);
		Mockito.verifyNoInteractions(modelCacheManager);
	}

	@Test(description = "Should remove model code cache on before delete event for document model")
	public void shouldRemoveModelCodeCacheOnBeforeDeleteEventForDocumentModel() {
		GenericModel model = createDocumentModel(TEST_MODEL_ID);
		ModelBeforeDeleteEvent event = new ModelBeforeDeleteEvent(model);

		modelChangeListener.listenBeforeModelUpdated(event);

		Mockito.verify(modelCodeCache).remove(TEST_MODEL_ID);
		Mockito.verifyNoInteractions(modelCacheManager);
	}

	@Test(description = "Should not remove model code cache for non-document model types")
	public void shouldNotRemoveModelCodeCacheForNonDocumentModelTypes() {
		GenericModel model = createRelationshipModel(TEST_RELATIONSHIP_MODEL_ID);
		GenericModel oldModel = createRelationshipModel(TEST_RELATIONSHIP_MODEL_ID);
		ModelBeforeUpdateEvent event = new ModelBeforeUpdateEvent(oldModel, model);

		modelChangeListener.listenBeforeModelUpdated(event);

		Mockito.verifyNoInteractions(modelCodeCache);
		Mockito.verifyNoInteractions(modelCacheManager);
	}

	@Test(description = "Should call deleteByModel on unique constraint validator when a document model is deleted")
	public void shouldDeleteUniqueConstraintEntriesOnDocumentModelBeforeDelete() {
		GenericModel model = createDocumentModel(TEST_MODEL_ID);
		ModelBeforeDeleteEvent event = new ModelBeforeDeleteEvent(model);

		modelChangeListener.listenBeforeModelDeleted(event);

		Mockito.verify(uniqueConstraintValidator).deleteByModel(TEST_MODEL_ID);
		Mockito.verifyNoInteractions(modelCacheManager, modelCodeCache);
	}

	@Test(description = "Should not call deleteByModel when a non-document model is deleted")
	public void shouldNotDeleteUniqueConstraintEntriesOnNonDocumentModelBeforeDelete() {
		GenericModel model = createRelationshipModel(TEST_RELATIONSHIP_MODEL_ID);
		ModelBeforeDeleteEvent event = new ModelBeforeDeleteEvent(model);

		modelChangeListener.listenBeforeModelDeleted(event);

		Mockito.verifyNoInteractions(uniqueConstraintValidator, modelCacheManager, modelCodeCache);
	}

	@Test(description = "Should invalidate caches on bulk import event")
	public void shouldInvalidateCachesOnBulkImportEvent() {
		TestHeader documentModelHeader = new TestHeader();
		documentModelHeader.setId(TEST_MODEL_ID);
		documentModelHeader.setModelType(ModelConstants.DOCUMENT_MODEL_TYPE);

		ModelsAfterImportEvent event = new ModelsAfterImportEvent(Set.of(documentModelHeader));

		modelChangeListener.listenOnBulkImportEvent(event);

		Mockito.verify(modelCodeCache).remove(TEST_MODEL_ID);
		Mockito.verify(modelCacheManager).invalidateModelGraphCaches();
		Mockito.verify(modelCacheManager).invalidateDocumentModelSearchServiceCache();
		Mockito.verify(modelCacheManager).invalidateValidationCodeCacheForDocumentModel(TEST_MODEL_ID);
	}

	@Test(description = "Should only invalidate validation cache for document models in bulk import")
	public void shouldOnlyInvalidateValidationCacheForDocumentModelsInBulkImport() {
		TestHeader documentModelHeader = new TestHeader();
		documentModelHeader.setId(TEST_MODEL_ID);
		documentModelHeader.setModelType(ModelConstants.DOCUMENT_MODEL_TYPE);

		TestHeader relationshipModelHeader = new TestHeader();
		relationshipModelHeader.setId(TEST_RELATIONSHIP_MODEL_ID);
		relationshipModelHeader.setModelType("relationship");

		ModelsAfterImportEvent event = new ModelsAfterImportEvent(Set.of(documentModelHeader, relationshipModelHeader));

		modelChangeListener.listenOnBulkImportEvent(event);

		Mockito.verify(modelCodeCache).remove(TEST_MODEL_ID);
		Mockito.verify(modelCacheManager).invalidateModelGraphCaches();
		Mockito.verify(modelCacheManager).invalidateDocumentModelSearchServiceCache();
		Mockito.verify(modelCacheManager).invalidateValidationCodeCacheForDocumentModel(TEST_MODEL_ID);
		Mockito.verify(modelCacheManager, Mockito.never()).invalidateValidationCodeCacheForDocumentModel(TEST_RELATIONSHIP_MODEL_ID);
	}

	@Test(description = "Should handle empty bulk import event")
	public void shouldHandleEmptyBulkImportEvent() {
		ModelsAfterImportEvent event = new ModelsAfterImportEvent(Collections.emptySet());

		modelChangeListener.listenOnBulkImportEvent(event);

		Mockito.verify(modelCacheManager).invalidateModelGraphCaches();
		Mockito.verify(modelCacheManager).invalidateDocumentModelSearchServiceCache();
	}

	@Test(description = "Should handle null headers in bulk import event")
	public void shouldHandleNullHeadersInBulkImportEvent() {
		TestHeader documentModelHeader = new TestHeader();
		documentModelHeader.setId(TEST_MODEL_ID);
		documentModelHeader.setModelType(ModelConstants.DOCUMENT_MODEL_TYPE);

		Set<com.mgmtp.a12.model.header.Header> headers = new java.util.HashSet<>();
		headers.add(documentModelHeader);
		headers.add(null);

		ModelsAfterImportEvent event = new ModelsAfterImportEvent(headers);

		modelChangeListener.listenOnBulkImportEvent(event);

		Mockito.verify(modelCacheManager).invalidateModelGraphCaches();
		Mockito.verify(modelCacheManager).invalidateDocumentModelSearchServiceCache();
		Mockito.verify(modelCacheManager).invalidateValidationCodeCacheForDocumentModel(TEST_MODEL_ID);
	}

	@Test(description = "Should invalidate document model search service cache on bulk import event")
	public void shouldInvalidateDocumentModelSearchServiceCacheOnBulkImportWithDocumentModels() {
		// Given
		TestHeader documentModelHeader = new TestHeader();
		documentModelHeader.setId(TEST_MODEL_ID);
		documentModelHeader.setModelType(ModelConstants.DOCUMENT_MODEL_TYPE);
		ModelsAfterImportEvent event = new ModelsAfterImportEvent(Set.of(documentModelHeader));

		// When
		modelChangeListener.listenOnBulkImportEvent(event);

		// Then
		Mockito.verify(modelCacheManager).invalidateDocumentModelSearchServiceCache();
	}

	@Test(description = "Should invalidate document model search service cache even when bulk import event is empty")
	public void shouldInvalidateDocumentModelSearchServiceCacheOnBulkImportEvenForEmptyEvent() {
		// Given
		ModelsAfterImportEvent event = new ModelsAfterImportEvent(Collections.emptySet());

		// When
		modelChangeListener.listenOnBulkImportEvent(event);

		// Then
		Mockito.verify(modelCacheManager).invalidateDocumentModelSearchServiceCache();
	}

	private GenericModel createDocumentModel(String modelId) {
		TestHeader header = new TestHeader();
		header.setId(modelId);
		header.setModelType(ModelConstants.DOCUMENT_MODEL_TYPE);
		GenericModel model = new GenericModel();
		model.setHeader(header);
		return model;
	}

	private GenericModel createRelationshipModel(String modelId) {
		TestHeader header = new TestHeader();
		header.setId(modelId);
		header.setModelType("relationship");
		GenericModel model = new GenericModel();
		model.setHeader(header);
		return model;
	}
}
