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
package com.mgmtp.a12.dataservices.modelgraph.fs.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.relationship.ModelGraphRoot;

public class ModelGraphFactoryTest {

	private static final URI DOC_MODEL_CLASSPATH_URI = URI.create("classpath:test-models/TestDocModel.json");
	private static final URI REL_MODEL_CLASSPATH_URI = URI.create("classpath:test-models/TestRelModel.json");

	@Test(description = "Should build model graph when document model classpath resource is provided")
	public void shouldBuildModelGraphWhenDocumentModelClasspathResourceProvided() throws Exception {
		// Given
		List<URI> resourceUris = List.of(DOC_MODEL_CLASSPATH_URI);

		// When
		ModelGraphRoot result = ModelGraphFactory.fromResources(resourceUris);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getDocumentModels()).hasSize(1);
		assertThat(result.getDocumentModels().stream().map(e -> e.getModelId()).toList()).contains("TestDocModel");
	}

	@Test(description = "Should build model graph when relationship model classpath resource is provided")
	public void shouldBuildModelGraphWhenRelationshipModelClasspathResourceProvided() throws Exception {
		// Given
		List<URI> resourceUris = List.of(DOC_MODEL_CLASSPATH_URI, REL_MODEL_CLASSPATH_URI);

		// When
		ModelGraphRoot result = ModelGraphFactory.fromResources(resourceUris);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRelationshipModels()).anyMatch(s -> s.contains("TestRelModel"));
	}

	@Test(description = "Should return empty graph when no resources are provided")
	public void shouldReturnEmptyGraphWhenNoResourcesProvided() {
		// Given
		List<URI> resourceUris = List.of();

		// When
		ModelGraphRoot result = ModelGraphFactory.fromResources(resourceUris);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getDocumentModels()).isEmpty();
		assertThat(result.getRelationshipModels()).isNullOrEmpty();
	}

	@Test(description = "Should build model graph when file URI to document model is provided")
	public void shouldBuildModelGraphWhenFileUriToDocumentModelProvided() throws Exception {
		// Given - resolve actual path from classpath to get a file: URI
		URI fileUri = Path.of(getClass().getClassLoader().getResource("test-models/TestDocModel.json").toURI()).toUri();
		List<URI> resourceUris = List.of(fileUri);

		// When
		ModelGraphRoot result = ModelGraphFactory.fromResources(resourceUris);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getDocumentModels()).hasSize(1);
	}
}
