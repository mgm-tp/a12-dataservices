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
package com.mgmtp.a12.dataservices.model.document.persistence.internal;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.events.ModelBeforeRepositorySaveEvent;
import com.mgmtp.a12.dataservices.model.persistence.IModelReadRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.DefaultModelRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelJpaRepository;
import com.mgmtp.a12.model.header.Header;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class DefaultModelRepositoryTest extends AbstractDataServicesCoreTest {
	@Mock private ModelJpaRepository modelJpaRepository;
	@Mock private IModelReadRepository<GenericModel> genericModelReadRepository;

	@InjectMocks private DefaultModelRepository defaultModelRepository;

	@BeforeMethod
	public void clear() {
		Mockito.reset(eventPublisher);
		Mockito.reset(modelJpaRepository);
	}

	@Test
	public void testSave_shouldSaveSuccessfully() {
		String username = RandomStringUtils.randomAlphabetic(10);
		setCurrentUser(username);

		String modelContent = RandomStringUtils.randomAlphabetic(30);
		Header header = makeTestModelHeader();
		String modelId = header.getId();
		ModelEntity modelEntity = new ModelEntity();
		modelEntity.setContent(modelContent);
		modelEntity.setId(modelId);

		when(modelJpaRepository.save(any())).thenReturn(modelEntity);

		GenericModel result = defaultModelRepository.save(header, modelContent);

		ArgumentCaptor<ModelBeforeRepositorySaveEvent> modelBeforeRepositorySaveEventArgumentCaptor =
			ArgumentCaptor.forClass(ModelBeforeRepositorySaveEvent.class);
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(modelBeforeRepositorySaveEventArgumentCaptor.capture());
		Assert.assertEquals(modelBeforeRepositorySaveEventArgumentCaptor.getValue().getModelName(), modelId);
		Assert.assertEquals(modelBeforeRepositorySaveEventArgumentCaptor.getValue().getModelType(), header.getModelType());
		Assert.assertEquals(modelBeforeRepositorySaveEventArgumentCaptor.getValue().getModelEntityContent(), modelContent);

		Mockito.verify(modelJpaRepository).save(Mockito.argThat(modelEntity1 -> {
			Assert.assertEquals(modelEntity1.getId(), modelId);
			Assert.assertEquals(modelEntity1.getCreatedBy(), username);
			return modelEntity1.getContent().equals(modelContent);
		}));

		Assert.assertEquals(result.getHeader().getId(), modelId);
		Assert.assertEquals(result.getContent().getRawContent(), modelContent);
	}

	@Test
	public void testUpdate_shouldSaveSuccessfully() {
		String username = RandomStringUtils.randomAlphabetic(10);
		setCurrentUser(username);

		String modelContent = RandomStringUtils.randomAlphabetic(30);
		Header header = makeTestModelHeader();
		String modelId = header.getId();
		ModelEntity modelEntity = new ModelEntity();
		modelEntity.setContent(modelContent);
		modelEntity.setId(modelId);

		when(modelJpaRepository.save(any())).thenReturn(modelEntity);

		GenericModel result = defaultModelRepository.update(header, modelContent);

		ArgumentCaptor<ModelBeforeRepositorySaveEvent> modelBeforeRepositorySaveEventArgumentCaptor =
			ArgumentCaptor.forClass(ModelBeforeRepositorySaveEvent.class);
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(modelBeforeRepositorySaveEventArgumentCaptor.capture());
		Assert.assertEquals(modelBeforeRepositorySaveEventArgumentCaptor.getValue().getModelName(), modelId);
		Assert.assertEquals(modelBeforeRepositorySaveEventArgumentCaptor.getValue().getModelType(), header.getModelType());
		Assert.assertEquals(modelBeforeRepositorySaveEventArgumentCaptor.getValue().getModelEntityContent(), modelContent);

		Mockito.verify(modelJpaRepository).save(Mockito.argThat(modelEntity1 -> {
			Assert.assertEquals(modelEntity1.getId(), modelId);
			Assert.assertEquals(modelEntity1.getUpdatedBy(), username);
			return modelEntity1.getContent().equals(modelContent);
		}));

		Assert.assertEquals(result.getHeader().getId(), modelId);
		Assert.assertEquals(result.getContent().getRawContent(), modelContent);
	}

}
