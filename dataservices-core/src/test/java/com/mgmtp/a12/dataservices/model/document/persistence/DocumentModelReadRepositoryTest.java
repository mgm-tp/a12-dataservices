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
package com.mgmtp.a12.dataservices.model.document.persistence;

import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.model.ModelConstants;
import com.mgmtp.a12.dataservices.model.events.ModelAfterRepositoryLoadEvent;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelJpaRepository;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelWrapper;
import com.mgmtp.a12.model.header.Header;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DocumentModelReadRepositoryTest extends AbstractDataServicesCoreTest {

	@Mock private ModelJpaRepository modelJpaRepository;
	@Mock private ModelHeaderJpaRepository modelHeaderJpaRepository;
	@Mock private DocumentModelUtils documentModelUtils;

	@InjectMocks
	private DocumentModelReadRepository documentModelReadRepository;

	@Test
	void testLoadModel_shouldLoadSuccessfully() {

		Header header = makeTestModelHeader(ModelConstants.DOCUMENT_MODEL_TYPE);
		ModelHeaderEntity headerEntity = new ModelHeaderEntity();
		headerEntity.setId(header.getId());
		headerEntity.setModelType(header.getModelType());
		String modelId = header.getId();

		String modelContent = RandomStringUtils.randomAlphabetic(30);

		ModelEntity modelEntity = new ModelEntity();
		modelEntity.setId(header.getId());
		modelEntity.setContent(modelContent);

		DocumentModel documentModel = new DocumentModel();
		documentModel.setHeader(header);

		DocumentModelWrapper modelWrapper = new DocumentModelWrapper(documentModel);

		when(modelHeaderJpaRepository.findById(modelId)).thenReturn(Optional.of(headerEntity));
		when(modelJpaRepository.findById(modelId)).thenReturn(Optional.of(modelEntity));
		when(documentModelUtils.deserializeDocumentModel(modelId, modelContent)).thenReturn(modelWrapper);

		IDocumentModel result = documentModelReadRepository.readModel(modelId);

		ArgumentCaptor<ModelAfterRepositoryLoadEvent> modelAfterRepositoryLoadEventArgumentCaptor =
			ArgumentCaptor.forClass(ModelAfterRepositoryLoadEvent.class);
		verify(eventPublisher, times(1)).publishEvent(modelAfterRepositoryLoadEventArgumentCaptor.capture());
		Assert.assertEquals(modelAfterRepositoryLoadEventArgumentCaptor.getValue().getModelType(), ModelConstants.DOCUMENT_MODEL_TYPE);
		Assert.assertEquals(modelAfterRepositoryLoadEventArgumentCaptor.getValue().getModelName(), modelId);
		Assert.assertEquals(modelAfterRepositoryLoadEventArgumentCaptor.getValue().getModelEntityContent(), modelContent);

		Assert.assertEquals(modelWrapper, result);

	}
}
