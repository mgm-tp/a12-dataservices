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
package com.mgmtp.a12.dataservices.server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.GenericModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Request;

import static com.mgmtp.a12.dataservices.constants.PathConstants.OTHER_MODELS_PATH_PATTERN;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.org.webcompere.modelassert.json.JsonAssertions.jsonObject;

@AutoConfigureMockMvc
@TestExecutionListeners(MockitoTestExecutionListener.class)
@Import(ListModelAuthorizationTest.TestConfig.class)
@Test public class ListModelAuthorizationTest extends AbstractSpringContextServerTests {

	public static final List<String> ALL_MODELS = List.of(
		GenericModelConstants.CONTRACT_OVERVIEW_MODEL, GenericModelConstants.CONTRACT_FORM_MODEL, GenericModelConstants.BUSINESS_PARTNER_OVERVIEW_MODEL,

		DocumentModelConstants.CONTRACT_CDM_MODEL, DocumentModelConstants.ANONYMIZED_BUSINESS_PARTNER_CDM_MODEL,

		DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
		DocumentModelConstants.BUSINESS_PARTNER_LTD_MODEL, DocumentModelConstants.BUSINESS_PARTNER_SUPER_MODEL,
		DocumentModelConstants.COINSURED_ADDITIONAL_FIELDS_MODEL, DocumentModelConstants.CONTRACT_DOCUMENT_MODEL,
		DocumentModelConstants.CONTRACT_AUTOMOTIVE_DOCUMENT_MODEL,

		RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL, RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL,
		RelationshipModelConstants.PARTNER_ADDRESSES_MODEL, RelationshipModelConstants.PARTNER_POSTAL_ADDRESS_MODEL);
	@Autowired private DataServicesCoreProperties dataServicesCoreProperties;
	@Autowired private MockMvc mockMvc;
	private static final String ENDPOINT_PATH = "/v2/rpc";
	private final Map<String, GenericModel> modelsMap = new HashMap<>();

	@BeforeClass void setUp() throws IOException {
		modelsMap.clear();
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
		for (Resource r : resourcePatternResolver.getResources(OTHER_MODELS_PATH_PATTERN)) {
			createModel(r.getContentAsString(StandardCharsets.UTF_8));
		}
	}

	@DataProvider public static Object[][] modelReadDataProvider() {
		return new Object[][] {
			new Object[] { UserConstants.MODEL_READ_USER, List.of(
				DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
				DocumentModelConstants.BUSINESS_PARTNER_SUPER_MODEL, DocumentModelConstants.CONTRACT_DOCUMENT_MODEL,

				RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL,
				RelationshipModelConstants.PARTNER_POSTAL_ADDRESS_MODEL) },

			new Object[] { UserConstants.GUEST_USER, List.of(
				DocumentModelConstants.CONTRACT_CDM_MODEL, DocumentModelConstants.ANONYMIZED_BUSINESS_PARTNER_CDM_MODEL,

				DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
				DocumentModelConstants.BUSINESS_PARTNER_SUPER_MODEL, DocumentModelConstants.COINSURED_ADDITIONAL_FIELDS_MODEL,
				DocumentModelConstants.CONTRACT_DOCUMENT_MODEL,

				RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL, RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL,
				RelationshipModelConstants.PARTNER_POSTAL_ADDRESS_MODEL) },

			new Object[] { UserConstants.ADMIN_USER, ALL_MODELS }

		};
	}

	@Test(dataProvider = "modelReadDataProvider")
	public void testModelReadAccess(String username, Collection<String> expectations) throws Exception {

		changeUserInContext(username);
		assertModelReadAccess(expectations);
	}

	private void assertModelReadAccess(Collection<String> expectedModels) throws Exception {

		Map<String, GenericModel> expectationsMap = new HashMap<>(modelsMap);
		expectationsMap.keySet().retainAll(expectedModels);
		String expectations = objectMapper.writeValueAsString(expectationsMap);

		ArrayList<String> unwantedModels = new ArrayList<>(modelsMap.keySet());
		unwantedModels.removeAll(expectedModels);

		List<String> models = new ArrayList<>(List.of("UnknownModel"));
		models.addAll(ALL_MODELS);

		String rpcRequest = objectMapper.writeValueAsString(List.of(JsonRpc2Request.builder()
			.id("ListModels1")
			.method(CoreOperationConstants.LIST_MODELS_INTERNAL_OPERATION)
			.params(objectMapper.valueToTree(Map.of("modelNames", models)))
			.build()));
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
			.post(dataServicesCoreProperties.getServer().getContextPath() + ENDPOINT_PATH)
			.accept(MediaType.APPLICATION_JSON)
			.characterEncoding(StandardCharsets.UTF_8)
			.contentType(MediaType.APPLICATION_JSON)
			.content(rpcRequest);

		ResultActions resultActions = mockMvc.perform(requestBuilder)
			.andExpect(status().isOk())
			.andExpect(content().encoding(StandardCharsets.UTF_8))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].result").exists())
			.andExpect(jsonPath("$[0].result.models").exists())
			.andExpect(jsonPath("$[0].result.models").isMap())
			.andExpect(jsonPath("$[0].result.models", jsonObject().where().keysInAnyOrder().isEqualTo(expectations)));
		for (String unwantedModel : unwantedModels) {
			resultActions.andExpect(jsonPath("$[0].result.models", jsonObject().doesNotContainKey(unwantedModel)));
		}
	}

	@Override protected GenericModel createModel(String modelContent) {
		GenericModel model = super.createModel(modelContent);
		return modelsMap.put(model.getHeader().getId(), model);
	}

	@TestConfiguration
	public static class TestConfig {

		@Bean public MockMvcBuilderCustomizer charsetFilterCustomizer() {
			return builder -> builder.addFilter(((request, response, chain) -> {
				chain.doFilter(request, response);
				response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			}));
		}
	}
}
