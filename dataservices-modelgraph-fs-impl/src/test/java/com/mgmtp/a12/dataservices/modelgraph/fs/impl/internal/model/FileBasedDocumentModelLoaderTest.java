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
package com.mgmtp.a12.dataservices.modelgraph.fs.impl.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Reader;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.model.document.persistence.DocumentModelLoader;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.model.header.DefaultHeaderParser;
import com.mgmtp.a12.model.header.HeaderParser;

public class FileBasedDocumentModelLoaderTest {

	private static final String DOCUMENT_MODEL_CONTENT =
		"{\"header\":{\"id\":\"TestDocModel\",\"modelType\":\"document\",\"modelVersion\":\"1.0.0\"}}";

	private FileBasedModelService fileBasedModelService;
	private IDocumentModelSerializer documentModelSerializer;
	private FileBasedDocumentModelLoader loader;

	@BeforeMethod
	public void setUp() {
		HeaderParser headerParser = new DefaultHeaderParser();
		fileBasedModelService = new FileBasedModelService(headerParser);
		documentModelSerializer = mock(IDocumentModelSerializer.class);
		loader = new FileBasedDocumentModelLoader(fileBasedModelService, documentModelSerializer);
	}

	@Test(description ="Should load document model when model exists")
	public void shouldLoadDocumentModelWhenModelExists() throws IOException {
		// Given
		fileBasedModelService.create(DOCUMENT_MODEL_CONTENT);
		IDocumentModel expectedModel = mock(IDocumentModel.class);
		when(documentModelSerializer.deserialize(any(Reader.class))).thenReturn(expectedModel);

		// When
		IDocumentModel result = loader.loadModel("TestDocModel");

		// Then
		assertThat(result).isEqualTo(expectedModel);
	}

	@Test(description ="Should throw NotFoundException when document model does not exist")
	public void shouldThrowNotFoundExceptionWhenDocumentModelDoesNotExist() {
		// When / Then
		assertThatThrownBy(() -> loader.loadModel("NonExistentModel"))
			.isInstanceOf(NotFoundException.class);
	}

	@Test(description = "Should implement DocumentModelLoader")
	public void shouldImplementIDocumentModelLoaderInterface() {
		assertThat(loader).isInstanceOf(DocumentModelLoader.class);
	}
}
