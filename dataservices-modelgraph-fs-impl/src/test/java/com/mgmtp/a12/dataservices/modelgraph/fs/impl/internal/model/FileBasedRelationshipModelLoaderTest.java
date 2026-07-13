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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModelSerializer;
import com.mgmtp.a12.model.header.DefaultHeaderParser;
import com.mgmtp.a12.model.header.HeaderParser;

public class FileBasedRelationshipModelLoaderTest {

	private static final String RELATIONSHIP_MODEL_CONTENT =
		"{\"header\":{\"id\":\"TestRelModel\",\"modelType\":\"relationship\",\"modelVersion\":\"1.0.0\"},"
			+ "\"roles\":[],\"links\":[]}";

	private static final String DOCUMENT_MODEL_CONTENT =
		"{\"header\":{\"id\":\"TestDocModel\",\"modelType\":\"document\",\"modelVersion\":\"1.0.0\"}}";

	private FileBasedModelService fileBasedModelService;
	private RelationshipModelSerializer relationshipModelSerializer;
	private FileBasedRelationshipModelLoader loader;

	@BeforeMethod
	public void setUp() {
		HeaderParser headerParser = new DefaultHeaderParser();
		fileBasedModelService = new FileBasedModelService(headerParser);
		relationshipModelSerializer = mock(RelationshipModelSerializer.class);
		loader = new FileBasedRelationshipModelLoader(fileBasedModelService, relationshipModelSerializer);
	}

	@Test(description ="Should load all relationship models when relationship models are registered")
	public void shouldLoadAllRelationshipModelsWhenRelationshipModelsAreRegistered() {
		// Given
		fileBasedModelService.create(RELATIONSHIP_MODEL_CONTENT);
		RelationshipModel expectedModel = mock(RelationshipModel.class);
		when(relationshipModelSerializer.deserialize(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
			.thenReturn(expectedModel);

		// When
		Set<RelationshipModel> result = loader.loadAllRelationshipModels();

		// Then
		assertThat(result).hasSize(1);
		assertThat(result).containsExactly(expectedModel);
	}

	@Test(description ="Should return empty set when no relationship models are registered")
	public void shouldReturnEmptySetWhenNoRelationshipModelsAreRegistered() {
		// Given - only document model registered
		fileBasedModelService.create(DOCUMENT_MODEL_CONTENT);

		// When
		Set<RelationshipModel> result = loader.loadAllRelationshipModels();

		// Then
		assertThat(result).isEmpty();
	}
}
