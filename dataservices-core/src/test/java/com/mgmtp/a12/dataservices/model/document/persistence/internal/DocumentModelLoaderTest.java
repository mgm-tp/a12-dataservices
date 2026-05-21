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

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.model.events.ModelAfterLoadEvent;
import com.mgmtp.a12.dataservices.model.persistence.IModelReadRepository;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelWrapper;
import com.mgmtp.a12.model.header.Header;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class DocumentModelLoaderTest extends AbstractDataServicesCoreTest {

	@Mock
	protected ModelPermissionEvaluator<IDocumentModel> modelPermissionEvaluator;
	@Mock
	protected IModelReadRepository<IDocumentModel> modelReadRepository;

	@InjectMocks private DocumentModelLoader documentModelLoader;

	@Test
	void testLoadModel_shouldLoadSuccessfully() {
		Header header = makeTestModelHeader();
		String modelId = header.getId();
		DocumentModel documentModel = new DocumentModel();
		documentModel.setHeader(header);

		DocumentModelWrapper modelWrapper = new DocumentModelWrapper(documentModel);

		when(modelReadRepository.readModel(modelId)).thenReturn(modelWrapper);

		IDocumentModel result = documentModelLoader.loadModel(modelId);

		ArgumentCaptor<ModelAfterLoadEvent> modelAfterLoadEventArgumentCaptor = ArgumentCaptor.forClass(ModelAfterLoadEvent.class);
		verify(eventPublisher, times(1)).publishEvent(modelAfterLoadEventArgumentCaptor.capture());
		assertEquals(modelAfterLoadEventArgumentCaptor.getValue().getModel(), modelWrapper);
		assertEquals(modelAfterLoadEventArgumentCaptor.getValue().getModel().getHeader(), modelWrapper.getHeader());

		verify(modelPermissionEvaluator, times(1)).checkModelReadPermission(modelWrapper);
		assertEquals(modelWrapper, result);
	}
}
