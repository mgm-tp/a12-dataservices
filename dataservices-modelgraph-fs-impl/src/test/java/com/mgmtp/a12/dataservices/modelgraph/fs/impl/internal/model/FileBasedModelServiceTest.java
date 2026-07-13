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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.exception.IntegrityException;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.model.header.DefaultHeaderParser;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderParser;

public class FileBasedModelServiceTest {

	private static final String DOCUMENT_MODEL_CONTENT =
		"{\"header\":{\"id\":\"TestModel\",\"modelType\":\"document\",\"modelVersion\":\"1.0.0\"}}";

	private static final String RELATIONSHIP_MODEL_CONTENT =
		"{\"header\":{\"id\":\"TestRelModel\",\"modelType\":\"relationship\",\"modelVersion\":\"1.0.0\"}}";

	private static final String SECOND_DOCUMENT_MODEL_CONTENT =
		"{\"header\":{\"id\":\"SecondModel\",\"modelType\":\"document\",\"modelVersion\":\"1.0.0\"}}";

	private FileBasedModelService service;

	@BeforeMethod
	public void setUp() {
		HeaderParser headerParser = new DefaultHeaderParser();
		service = new FileBasedModelService(headerParser);
	}

	@Test(description ="Should create model when valid content is provided")
	public void shouldCreateModelWhenValidContentProvided() {
		// When
		GenericModel created = service.create(DOCUMENT_MODEL_CONTENT);

		// Then
		assertThat(created).isNotNull();
		assertThat(created.getHeader().getId()).isEqualTo("TestModel");
	}

	@Test(description ="Should load model by identifier when model exists")
	public void shouldLoadModelByIdentifierWhenModelExists() {
		// Given
		service.create(DOCUMENT_MODEL_CONTENT);

		// When
		GenericModel loaded = service.load("TestModel");

		// Then
		assertThat(loaded).isNotNull();
		assertThat(loaded.getHeader().getId()).isEqualTo("TestModel");
	}

	@Test(description ="Should load multiple models by identifiers when models exist")
	public void shouldLoadMultipleModelsByIdentifiersWhenModelsExist() {
		// Given
		service.create(DOCUMENT_MODEL_CONTENT);
		service.create(SECOND_DOCUMENT_MODEL_CONTENT);

		// When
		Collection<GenericModel> loaded = service.load(List.of("TestModel", "SecondModel"));

		// Then
		assertThat(loaded).hasSize(2);
		assertThat(loaded).extracting(m -> m.getHeader().getId())
			.containsExactlyInAnyOrder("TestModel", "SecondModel");
	}

	@Test(description ="Should find all headers when models are registered")
	public void shouldFindAllHeadersWhenModelsAreRegistered() {
		// Given
		service.create(DOCUMENT_MODEL_CONTENT);
		service.create(RELATIONSHIP_MODEL_CONTENT);

		// When
		Set<Header> headers = service.findAllHeaders();

		// Then
		assertThat(headers).hasSize(2);
		assertThat(headers).extracting(Header::getId)
			.containsExactlyInAnyOrder("TestModel", "TestRelModel");
	}

	@Test(description ="Should find headers by type when matching models are registered")
	public void shouldFindHeadersByTypeWhenMatchingModelsAreRegistered() {
		// Given
		service.create(DOCUMENT_MODEL_CONTENT);
		service.create(RELATIONSHIP_MODEL_CONTENT);
		service.create(SECOND_DOCUMENT_MODEL_CONTENT);

		// When
		List<Header> documentHeaders = service.findAllHeadersByType("document");

		// Then
		assertThat(documentHeaders).hasSize(2);
		assertThat(documentHeaders).extracting(Header::getId)
			.containsExactlyInAnyOrder("TestModel", "SecondModel");
	}

	@Test(description ="Should throw IntegrityException when model is added multiple times")
	public void shouldThrowIntegrityExceptionWhenModelAddedMultipleTimes() {
		// Given
		service.create(DOCUMENT_MODEL_CONTENT);

		// When / Then
		assertThatThrownBy(() -> service.create(DOCUMENT_MODEL_CONTENT))
			.isInstanceOf(IntegrityException.class);
	}

	@Test(description ="Should throw UnsupportedOperationException when update is called")
	public void shouldThrowUnsupportedOperationExceptionWhenUpdateCalled() {
		// When / Then
		assertThatThrownBy(() -> service.update(DOCUMENT_MODEL_CONTENT))
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test(description ="Should throw UnsupportedOperationException when delete is called")
	public void shouldThrowUnsupportedOperationExceptionWhenDeleteCalled() {
		// When / Then
		assertThatThrownBy(() -> service.delete("TestModel"))
			.isInstanceOf(UnsupportedOperationException.class);
	}
}
