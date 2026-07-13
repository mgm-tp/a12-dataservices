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
package com.mgmtp.a12.dataservices.server.relationship.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.mockito.testng.MockitoTestNGListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.server.AbstractSpringContextServerTests;

import lombok.NonNull;

import static com.mgmtp.a12.dataservices.constants.PathConstants.OTHER_MODELS_PATH_PATTERN;
import static com.mgmtp.a12.dataservices.server.internal.rest.RelationshipControllerImpl.MODELGRAPH_PATH;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Listeners(MockitoTestNGListener.class)
@Test public class RelationshipControllerImplTest extends AbstractSpringContextServerTests {

	@Autowired private DataServicesCoreProperties dataServicesCoreProperties;
	@Autowired private WebApplicationContext webApplicationContext;
	private MockMvc mvc;
	@Autowired private ResourcePatternResolver resourcePatternResolver;

	@BeforeClass void setUp() throws IOException {
		mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		changeUserInContext(UserConstants.ADMIN_USER);
		for (Resource r : resourcePatternResolver.getResources(PathConstants.DOCUMENT_MODELS_PATH_PATTERN)) {
			createModel(r.getContentAsString(StandardCharsets.UTF_8));
		}
		for (Resource r : resourcePatternResolver.getResources(PathConstants.RELATIONSHIP_MODELS_PATH_PATTERN)) {
			createModel(r.getContentAsString(StandardCharsets.UTF_8));
		}
		for (Resource r : resourcePatternResolver.getResources(PathConstants.CDM_MODELS_PATH_PATTERN)) {
			createModel(r.getContentAsString(StandardCharsets.UTF_8));
		}
		for (Resource r : resourcePatternResolver.getResources(OTHER_MODELS_PATH_PATTERN))
			createModel(r.getContentAsString(StandardCharsets.UTF_8));
	}

	@Test void testGetModelGraphWithModelReferences() throws Exception {
		assertModelReferences(
			assertModelGraph(q -> {}));
	}

	private void assertModelReferences(ResultActions resultActions) throws Exception {
		resultActions
			.andExpect(jsonPath("$.genericModels").exists())
			.andExpect(jsonPath("$.genericModels").isNotEmpty())
			.andExpect(jsonPath("$.composeDocumentModels[?(@.modelReferences)]").isNotEmpty())
			.andExpect(jsonPath("$.genericModels[?(@.modelReferences)]").isNotEmpty())
		;
	}

	@NonNull private ResultActions assertModelGraph(Consumer<MockHttpServletRequestBuilder> query) throws Exception {
		MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
			.get(dataServicesCoreProperties.getServer().getContextPath() + "/" + MODELGRAPH_PATH);
		query.accept(mockHttpServletRequestBuilder);
		return mvc.perform(mockHttpServletRequestBuilder)
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.documentModels").exists())
			.andExpect(jsonPath("$.documentModels").isNotEmpty())
			.andExpect(jsonPath("$.documentModels[*].modelId").isNotEmpty())
			.andExpect(jsonPath("$.composeDocumentModels").exists())
			.andExpect(jsonPath("$.composeDocumentModels").isNotEmpty())
			.andExpect(jsonPath("$.composeDocumentModels[*].modelId").isNotEmpty())
			.andExpect(jsonPath("$.relationshipModels").exists())
			.andExpect(jsonPath("$.relationshipModels").isNotEmpty())
			.andExpect(jsonPath("$.relationshipModels[*].header.id").isNotEmpty());
	}

}
