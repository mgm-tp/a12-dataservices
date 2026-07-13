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

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.security.access.AccessDeniedException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.authorization.AuthConstants;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.DocumentModelFieldsIndexer;
import com.mgmtp.a12.dataservices.exception.ModelSerializationException;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.events.ModelAfterCreateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelAfterDeleteEvent;
import com.mgmtp.a12.dataservices.model.events.ModelAfterLoadEvent;
import com.mgmtp.a12.dataservices.model.events.ModelAfterUpdateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelBeforeCreateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelBeforeDeleteEvent;
import com.mgmtp.a12.dataservices.model.events.ModelBeforeUpdateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelsAfterLoadEvent;
import com.mgmtp.a12.dataservices.model.persistence.GenericModelReadRepository;
import com.mgmtp.a12.dataservices.model.persistence.IModelRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.model.Model;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderParseException;
import com.mgmtp.a12.model.header.HeaderParser;

public class DefaultModelServiceTest extends AbstractDataServicesCoreTest {

	@Mock private HeaderParser headerParser;
	@Mock private IModelRepository modelRepository;
	@Spy private List<IModelRepository> modelRepositories;
	@Mock private ModelHeaderJpaRepository modelHeaderJpaRepository;
	@Mock private GenericModelReadRepository genericModelReadRepository;
	@Mock private ModelPermissionEvaluator<GenericModel> modelPermissionEvaluator;
	@Mock private DocumentModelFieldsIndexer documentModelFieldsIndexer;
	@Mock private UniqueConstraintModelValidator uniqueConstraintModelValidator;

	@InjectMocks private DefaultModelService defaultModelService;

	@BeforeMethod
	public void clearAndSetup() {
		Mockito.reset(eventPublisher, modelHeaderJpaRepository, modelRepository, modelPermissionEvaluator, uniqueConstraintModelValidator);
		Mockito.when(modelRepositories.stream()).thenAnswer(a -> Stream.of(modelRepository));
	}

	@Test
	public void testCreateModel_createSuccessfully() throws HeaderParseException {
		String modelContent = RandomStringUtils.randomAlphabetic(20);
		Header header = makeTestModelHeader();
		GenericModel mockedModel = new GenericModel();
		mockedModel.setHeader(header);

		Mockito.when(headerParser.parseJson(modelContent)).thenReturn(header);
		Mockito.when(modelRepository.supports(header)).thenReturn(true);
		Mockito.when(modelRepository.save(ArgumentMatchers.eq(header), ArgumentMatchers.any())).thenReturn(mockedModel);

		GenericModel result = defaultModelService.create(modelContent);

		ArgumentCaptor<ModelBeforeCreateEvent> beforeCreateEventArgumentCaptor = ArgumentCaptor.forClass(ModelBeforeCreateEvent.class);
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(beforeCreateEventArgumentCaptor.capture());
		Assert.assertEquals(beforeCreateEventArgumentCaptor.getValue().getModel().getHeader(), header);

		ArgumentCaptor<ModelAfterCreateEvent> afterCreateEventArgumentCaptor = ArgumentCaptor.forClass(ModelAfterCreateEvent.class);
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(afterCreateEventArgumentCaptor.capture());
		Assert.assertEquals(afterCreateEventArgumentCaptor.getValue().getModel(), mockedModel);
		Assert.assertEquals(afterCreateEventArgumentCaptor.getValue().getModel().getHeader(), header);

		Mockito.verify(modelHeaderJpaRepository, Mockito.times(1)).save(Mockito.argThat(param -> {
			Assert.assertEquals(param.getId(), header.getId());
			Assert.assertEquals(param.getModelType(), header.getModelType());
			return true;
		}));

		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelCreatePermission(ArgumentMatchers.any(Header.class));

		Assert.assertEquals(result.getHeader(), header);
	}

	@Test public void testCreateModel_hasNoReadPermission() throws HeaderParseException {
		String modelContent = RandomStringUtils.randomAlphabetic(20);
		Header header = makeTestModelHeader();
		GenericModel mockedModel = new GenericModel();
		mockedModel.setHeader(header);

		Mockito.when(headerParser.parseJson(modelContent)).thenReturn(header);
		Mockito.doThrow(new AccessDeniedException(AuthConstants.ACCESS_DENIED)).when(modelPermissionEvaluator)
			.checkModelCreatePermission(ArgumentMatchers.any(Header.class));

		AccessDeniedException e = Assert.expectThrows(AccessDeniedException.class, () -> defaultModelService.create(modelContent));

		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelCreatePermission(header);
		Mockito.verifyNoInteractions(eventPublisher, modelRepository, modelHeaderJpaRepository);
		Assert.assertEquals(e.getMessage(), AuthConstants.ACCESS_DENIED);
	}

	@Test public void testCreateModel_shouldCheckPermissionBeforeValidation() throws HeaderParseException {
		// Header mock returns null for getId() by default, which would fail validateHeader with InvalidInputException.
		// If permission is checked first and throws AccessDeniedException, validation is never reached.
		// If ordering were reversed, InvalidInputException would be thrown and the expectThrows below would fail.
		String modelContent = RandomStringUtils.randomAlphabetic(20);
		Header invalidHeader = Mockito.mock(Header.class);

		Mockito.when(headerParser.parseJson(modelContent)).thenReturn(invalidHeader);
		Mockito.doThrow(new AccessDeniedException(AuthConstants.ACCESS_DENIED)).when(modelPermissionEvaluator)
			.checkModelCreatePermission(ArgumentMatchers.any(Header.class));

		AccessDeniedException e = Assert.expectThrows(AccessDeniedException.class, () -> defaultModelService.create(modelContent));

		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelCreatePermission(invalidHeader);
		Mockito.verifyNoInteractions(eventPublisher, modelRepository, modelHeaderJpaRepository);
		Assert.assertEquals(e.getMessage(), AuthConstants.ACCESS_DENIED);
	}

	@Test void testUpdateModel_shouldUpdateSuccessfully() throws HeaderParseException {
		String updatedModelContent = RandomStringUtils.randomAlphabetic(20);
		Header header = makeTestModelHeader();
		ModelHeaderEntity oldHeader = new ModelHeaderEntity();
		oldHeader.setModelType(header.getModelType());
		oldHeader.setId(header.getId());

		GenericModel oldModel = new GenericModel();
		oldModel.setHeader(oldHeader);

		GenericModel updatedModel = new GenericModel();
		updatedModel.setHeader(header);

		Mockito.when(modelHeaderJpaRepository.findById(header.getId())).thenReturn(Optional.of(oldHeader));
		Mockito.when(headerParser.parseJson(updatedModelContent)).thenReturn(header);
		Mockito.when(modelRepository.supports(header)).thenReturn(true);
		Mockito.when(modelRepository.load(header)).thenReturn(Optional.of(oldModel));
		Mockito.when(modelRepository.update(header, updatedModelContent)).thenReturn(updatedModel);

		GenericModel result = defaultModelService.update(updatedModelContent);

		ArgumentCaptor<ModelBeforeUpdateEvent> beforeUpdateEventArgumentCaptor = ArgumentCaptor.forClass(ModelBeforeUpdateEvent.class);
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(beforeUpdateEventArgumentCaptor.capture());
		Assert.assertEquals(beforeUpdateEventArgumentCaptor.getValue().getOldModel(), oldModel);
		Assert.assertEquals(beforeUpdateEventArgumentCaptor.getValue().getModel().getHeader(), header);

		ArgumentCaptor<ModelAfterUpdateEvent> afterUpdateEventArgumentCaptor = ArgumentCaptor.forClass(ModelAfterUpdateEvent.class);
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(afterUpdateEventArgumentCaptor.capture());
		Assert.assertEquals(afterUpdateEventArgumentCaptor.getValue().getOldModel(), oldModel);
		Assert.assertEquals(afterUpdateEventArgumentCaptor.getValue().getModel(), updatedModel);

		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelUpdatePermission(oldHeader);
		Mockito.verify(modelHeaderJpaRepository, Mockito.times(1)).save(Mockito.argThat(modelHeaderEntity -> {
			Assert.assertEquals(modelHeaderEntity.getModelType(), header.getModelType());
			Assert.assertEquals(modelHeaderEntity.getId(), header.getId());
			Assert.assertEquals(modelHeaderEntity.getAnnotations().size(), header.getAnnotations().size());
			Assert.assertEquals(modelHeaderEntity.getAnnotations().getFirst().getName(), header.getAnnotations().getFirst().getName());
			Assert.assertEquals(modelHeaderEntity.getAnnotations().getFirst().getValue(), header.getAnnotations().getFirst().getValue());
			return true;
		}));

		Assert.assertEquals(result.getHeader().getId(), header.getId());
		Assert.assertEquals(result.getHeader().getModelType(), header.getModelType());
	}

	@Test void testUpdateModel_whenHasNoPermission() throws HeaderParseException {
		String updatedModelContent = RandomStringUtils.randomAlphabetic(20);
		Header header = makeTestModelHeader();
		ModelHeaderEntity oldHeader = new ModelHeaderEntity();
		oldHeader.setModelType(header.getModelType());
		oldHeader.setId(header.getId());

		GenericModel oldModel = new GenericModel();
		oldModel.setHeader(oldHeader);

		GenericModel updatedModel = new GenericModel();
		updatedModel.setHeader(header);

		Mockito.when(modelHeaderJpaRepository.findById(header.getId())).thenReturn(Optional.of(oldHeader));
		Mockito.when(headerParser.parseJson(updatedModelContent)).thenReturn(header);
		Mockito.doThrow(new AccessDeniedException(AuthConstants.ACCESS_DENIED))
			.when(modelPermissionEvaluator).checkModelUpdatePermission(ArgumentMatchers.any(Header.class));

		AccessDeniedException e = Assert.expectThrows(AccessDeniedException.class, () -> defaultModelService.update(updatedModelContent));

		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelUpdatePermission(oldHeader);
		Mockito.verifyNoInteractions(eventPublisher, modelRepository);
		Mockito.verify(modelHeaderJpaRepository, Mockito.times(1)).findById(header.getId());
		Mockito.verifyNoMoreInteractions(modelHeaderJpaRepository);
		Assert.assertEquals(e.getMessage(), AuthConstants.ACCESS_DENIED);
	}

	@Test(description = "Should invoke validateModel during create")
	public void shouldInvokeValidateModelOnCreate() throws HeaderParseException {
		String modelContent = RandomStringUtils.randomAlphabetic(20);
		Header header = makeTestModelHeader();
		GenericModel mockedModel = new GenericModel();
		mockedModel.setHeader(header);

		Mockito.when(headerParser.parseJson(modelContent)).thenReturn(header);
		Mockito.when(modelRepository.supports(header)).thenReturn(true);
		Mockito.when(modelRepository.save(ArgumentMatchers.eq(header), ArgumentMatchers.any())).thenReturn(mockedModel);

		defaultModelService.create(modelContent);

		Mockito.verify(uniqueConstraintModelValidator, Mockito.times(1))
			.validateModel(ArgumentMatchers.argThat(m -> m.getHeader().equals(header)));
	}

	@Test(description = "Should propagate ModelSerializationException when validateModel fails during create")
	public void shouldPropagateModelSerializationExceptionWhenValidateModelFailsOnCreate() throws HeaderParseException {
		String modelContent = RandomStringUtils.randomAlphabetic(20);
		Header header = makeTestModelHeader();

		Mockito.when(headerParser.parseJson(modelContent)).thenReturn(header);
		Mockito.doThrow(new ModelSerializationException("invalid unique constraint"))
			.when(uniqueConstraintModelValidator).validateModel(ArgumentMatchers.any(GenericModel.class));

		Assert.expectThrows(ModelSerializationException.class, () -> defaultModelService.create(modelContent));

		Mockito.verify(uniqueConstraintModelValidator, Mockito.times(1)).validateModel(ArgumentMatchers.any(GenericModel.class));
		Mockito.verify(modelRepository, Mockito.times(0)).save(ArgumentMatchers.any(), ArgumentMatchers.any());
	}

	@Test(description = "Should invoke validateModel during update")
	public void shouldInvokeValidateModelOnUpdate() throws HeaderParseException {
		String updatedModelContent = RandomStringUtils.randomAlphabetic(20);
		Header header = makeTestModelHeader();
		ModelHeaderEntity oldHeader = new ModelHeaderEntity();
		oldHeader.setModelType(header.getModelType());
		oldHeader.setId(header.getId());
		GenericModel oldModel = new GenericModel();
		oldModel.setHeader(oldHeader);
		GenericModel updatedModel = new GenericModel();
		updatedModel.setHeader(header);

		Mockito.when(modelHeaderJpaRepository.findById(header.getId())).thenReturn(Optional.of(oldHeader));
		Mockito.when(headerParser.parseJson(updatedModelContent)).thenReturn(header);
		Mockito.when(modelRepository.supports(header)).thenReturn(true);
		Mockito.when(modelRepository.load(header)).thenReturn(Optional.of(oldModel));
		Mockito.when(modelRepository.update(header, updatedModelContent)).thenReturn(updatedModel);

		defaultModelService.update(updatedModelContent);

		Mockito.verify(uniqueConstraintModelValidator, Mockito.times(1))
			.validateModel(ArgumentMatchers.argThat(m -> m.getHeader().equals(header)));
	}

	@Test(description = "Should propagate ModelSerializationException when validateModel fails during update")
	public void shouldPropagateModelSerializationExceptionWhenValidateModelFailsOnUpdate() throws HeaderParseException {
		String updatedModelContent = RandomStringUtils.randomAlphabetic(20);
		Header header = makeTestModelHeader();
		ModelHeaderEntity oldHeader = new ModelHeaderEntity();
		oldHeader.setModelType(header.getModelType());
		oldHeader.setId(header.getId());
		GenericModel oldModel = new GenericModel();
		oldModel.setHeader(oldHeader);

		Mockito.when(modelHeaderJpaRepository.findById(header.getId())).thenReturn(Optional.of(oldHeader));
		Mockito.when(headerParser.parseJson(updatedModelContent)).thenReturn(header);
		Mockito.when(modelRepository.supports(header)).thenReturn(true);
		Mockito.when(modelRepository.load(header)).thenReturn(Optional.of(oldModel));
		Mockito.doThrow(new ModelSerializationException("invalid unique constraint"))
			.when(uniqueConstraintModelValidator).validateModel(ArgumentMatchers.any(GenericModel.class));

		Assert.expectThrows(ModelSerializationException.class, () -> defaultModelService.update(updatedModelContent));

		Mockito.verify(uniqueConstraintModelValidator, Mockito.times(1)).validateModel(ArgumentMatchers.any(GenericModel.class));
		Mockito.verify(modelRepository, Mockito.times(0)).update(ArgumentMatchers.any(), ArgumentMatchers.any());
	}

	@Test void testDeleteModel_shouldDeleteSuccessfully() {
		String modelId = RandomStringUtils.randomAlphabetic(10);
		ModelHeaderEntity modelHeaderEntity = new ModelHeaderEntity();
		modelHeaderEntity.setId(modelId);
		GenericModel genericModel = new GenericModel();
		genericModel.setHeader(modelHeaderEntity);
		Mockito.when(modelHeaderJpaRepository.findById(modelId)).thenReturn(Optional.of(modelHeaderEntity));
		Mockito.when(modelRepository.supports(modelHeaderEntity)).thenReturn(true);
		Mockito.when(modelRepository.load(modelHeaderEntity)).thenReturn(Optional.of(genericModel));
		Mockito.when(modelRepository.delete(modelHeaderEntity)).thenReturn(true);

		boolean result = defaultModelService.delete(modelId);

		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelDeletePermission(modelHeaderEntity);
		Mockito.verify(modelHeaderJpaRepository, Mockito.times(1)).deleteById(modelId);
		Mockito.verify(modelRepository, Mockito.times(1)).delete(modelHeaderEntity);

		ArgumentCaptor<ModelBeforeDeleteEvent> beforeDeleteEventArgumentCaptor = ArgumentCaptor.forClass(ModelBeforeDeleteEvent.class);
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(beforeDeleteEventArgumentCaptor.capture());
		Assert.assertEquals(beforeDeleteEventArgumentCaptor.getValue().getModel(), genericModel);
		Assert.assertEquals(beforeDeleteEventArgumentCaptor.getValue().getModel().getHeader(), genericModel.getHeader());

		ArgumentCaptor<ModelAfterDeleteEvent> afterDeleteEventArgumentCaptor = ArgumentCaptor.forClass(ModelAfterDeleteEvent.class);
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(afterDeleteEventArgumentCaptor.capture());
		Assert.assertEquals(afterDeleteEventArgumentCaptor.getValue().getModel(), genericModel);
		Assert.assertEquals(afterDeleteEventArgumentCaptor.getValue().getModel().getHeader(), genericModel.getHeader());

		Assert.assertTrue(result);
	}

	@Test void testDeleteModel_whenHasNoPermission() {
		String modelId = RandomStringUtils.randomAlphabetic(10);
		ModelHeaderEntity modelHeaderEntity = new ModelHeaderEntity();
		modelHeaderEntity.setId(modelId);
		GenericModel genericModel = new GenericModel();
		genericModel.setHeader(modelHeaderEntity);
		Mockito.when(modelHeaderJpaRepository.findById(modelId)).thenReturn(Optional.of(modelHeaderEntity));
		Mockito.when(modelRepository.supports(modelHeaderEntity)).thenReturn(true);
		Mockito.when(modelRepository.load(modelHeaderEntity)).thenReturn(Optional.of(genericModel));
		Mockito.doThrow(new AccessDeniedException(AuthConstants.ACCESS_DENIED))
			.when(modelPermissionEvaluator).checkModelDeletePermission(ArgumentMatchers.any(Header.class));

		AccessDeniedException e = Assert.expectThrows(AccessDeniedException.class, () -> defaultModelService.delete(modelId));

		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelDeletePermission(modelHeaderEntity);

		Mockito.verifyNoInteractions(eventPublisher);
		Mockito.verify(modelHeaderJpaRepository, Mockito.times(1)).findById(modelId);
		Mockito.verifyNoMoreInteractions(modelHeaderJpaRepository);
		Mockito.verify(modelRepository, Mockito.times(1)).supports(modelHeaderEntity);
		Mockito.verifyNoMoreInteractions(modelRepository);
		Assert.assertEquals(e.getMessage(), AuthConstants.ACCESS_DENIED);
	}

	@Test void testLoadModel_shouldLoadSuccessfully() {
		GenericModel model = mockModel();
		String modelId = model.getHeader().getId();

		Mockito.when(genericModelReadRepository.readModel(modelId)).thenReturn(model);

		GenericModel result = defaultModelService.load(modelId);

		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(model);
		Mockito.verify(genericModelReadRepository, Mockito.times(1)).readModel(modelId);

		ArgumentCaptor<ModelAfterLoadEvent> afterLoadEventArgumentCaptor = ArgumentCaptor.forClass(ModelAfterLoadEvent.class);
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(afterLoadEventArgumentCaptor.capture());

		assertModel(afterLoadEventArgumentCaptor.getValue().getModel(), model);
		assertModel(result, model);
	}

	@Test(expectedExceptions = AccessDeniedException.class, expectedExceptionsMessageRegExp = "Access Denied")
	void testLoadModel_whenHasNoPermission() {
		GenericModel model = mockModel();
		String modelId = model.getHeader().getId();

		Mockito.when(genericModelReadRepository.readModel(modelId)).thenReturn(model);
		Mockito.doThrow(new AccessDeniedException(AuthConstants.ACCESS_DENIED))
			.when(modelPermissionEvaluator).checkModelReadPermission(model);

		defaultModelService.load(modelId);

		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(model);
		Mockito.verifyNoInteractions(eventPublisher);
	}

	@Test void testLoadModeList_shouldLoadSuccessfully() {
		GenericModel model1 = mockModel();
		GenericModel model2 = mockModel();
		String modelId1 = model1.getHeader().getId();
		String modelId2 = model2.getHeader().getId();

		Mockito.when(modelHeaderJpaRepository.existsById(modelId1)).thenReturn(true);
		Mockito.when(modelHeaderJpaRepository.existsById(modelId2)).thenReturn(true);
		Mockito.when(genericModelReadRepository.readModel(modelId1)).thenReturn(model1);
		Mockito.when(genericModelReadRepository.readModel(modelId2)).thenReturn(model2);
		Mockito.when(modelPermissionEvaluator.hasModelReadPermission(model1)).thenReturn(true);
		Mockito.when(modelPermissionEvaluator.hasModelReadPermission(model2)).thenReturn(true);

		List<GenericModel> result = defaultModelService.load(List.of(modelId1, modelId2))
			.stream()
			.toList();

		Mockito.verify(modelPermissionEvaluator, Mockito.times(2)).hasModelReadPermission(ArgumentMatchers.any(GenericModel.class));
		Mockito.verify(genericModelReadRepository, Mockito.times(2)).readModel(ArgumentMatchers.any());
		Mockito.verify(modelHeaderJpaRepository, Mockito.times(2)).existsById(ArgumentMatchers.any());

		ArgumentCaptor<ModelsAfterLoadEvent> afterLoadEventArgumentCaptor = ArgumentCaptor.forClass(ModelsAfterLoadEvent.class);
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(afterLoadEventArgumentCaptor.capture());
		List models = afterLoadEventArgumentCaptor.getValue().getModels().stream().toList();
		Assert.assertEquals(models.size(), 2);
		assertModel((Model) models.getFirst(), model1);
		assertModel((Model) models.get(1), model2);
		assertModel(result.getFirst(), model1);
		assertModel(result.get(1), model2);
	}

	@Test void testLoadModeList_hasNoPermission() {
		GenericModel model1 = mockModel();
		GenericModel model2 = mockModel();
		String modelId1 = model1.getHeader().getId();
		String modelId2 = model2.getHeader().getId();

		Mockito.when(modelHeaderJpaRepository.existsById(modelId1)).thenReturn(true);
		Mockito.when(genericModelReadRepository.readModel(modelId1)).thenReturn(model1);
		Mockito.when(modelPermissionEvaluator.hasModelReadPermission(model1)).thenThrow(
			new AccessDeniedException(AuthConstants.ACCESS_DENIED)
		);
		AccessDeniedException e = Assert.expectThrows(AccessDeniedException.class, () -> defaultModelService.load(List.of(modelId1, modelId2)));

		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).hasModelReadPermission(ArgumentMatchers.any(GenericModel.class));
		Mockito.verify(genericModelReadRepository, Mockito.times(1)).readModel(ArgumentMatchers.any());
		Mockito.verify(modelHeaderJpaRepository, Mockito.times(1)).existsById(ArgumentMatchers.any());
		Mockito.verifyNoInteractions(eventPublisher);
		Assert.assertEquals(e.getMessage(), AuthConstants.ACCESS_DENIED);
	}

	private GenericModel mockModel() {
		Header header = makeTestModelHeader();
		GenericModel model = new GenericModel();
		model.setHeader(header);
		return model;
	}

	void assertModel(Model model1, Model model2) {
		Assert.assertEquals(model1, model2);
		Assert.assertEquals(model1.getHeader(), model2.getHeader());
		Assert.assertEquals(model1.getHeader().getId(), model2.getHeader().getId());
		Assert.assertEquals(model1.getHeader().getModelType(), model2.getHeader().getModelType());
	}
}
