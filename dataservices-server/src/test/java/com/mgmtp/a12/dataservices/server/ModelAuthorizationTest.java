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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.uaa.authentication.backend.BackendAuthenticationService;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ModelAuthorizationTest extends AbstractSpringContextServerTests {

	@Autowired private WebApplicationContext context;
	@Autowired private DataServicesCoreProperties dataServicesCoreProperties;
	@Autowired private BackendAuthenticationService backendAuthenticationService;

	private MockMvc mockMvc;
	private static final String ENDPOINT_PATH = "/v2/models";
	private static final String AUTHORIZATION_MODELS_PATH = "model/document/authorizationTests/";

	@BeforeClass public void initClass() {
		mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	@BeforeMethod public void initMethod() {
		super.cleanUpTestEnvironment();
	}

	@DataProvider(name = "modelCreateDataProvider")
	public Object[][] modelCreateDataProvider() {
		return new Object[][] {
			{ UserConstants.ADMIN_USER, AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", true },
			{ "actuator", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", false },
			{ "guest", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", false },
			{ "model_manager_user", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", true },
			{ UserConstants.MODEL_READ_USER, AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", false  },
			{ "model_create_user", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", true  },
			{ "model_update_user", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", false  },
			{ "model_delete_user", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", false  }
		};
	}

	@DataProvider(name = "modelUpdateDataProvider")
	public Object[][] modelUpdateDataProvider() {
		return new Object[][] {
			{ UserConstants.ADMIN_USER, AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", true },
			{ "actuator", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", false },
			{ "guest", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", false },
			{ "model_manager_user", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", true },
			{ UserConstants.MODEL_READ_USER, AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", false  },
			{ "model_create_user", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", false  },
			{ "model_update_user", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", true  },
			{ "model_delete_user", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", false  }
		};
	}

	@DataProvider(name = "modelReadDataProvider")
	public Object[][] modelReadDataProvider() {
		return new Object[][] {
			{ UserConstants.ADMIN_USER, "AuthorizationTestModel", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", true },
			{ "actuator", "AuthorizationTestModel", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", false },
			{ "guest", "AuthorizationTestModel", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", true },
			{ "model_manager_user", "AuthorizationTestModel", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", true },
			{ UserConstants.MODEL_READ_USER, "AuthorizationTestModel", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", true  },
			{ "model_create_user", "AuthorizationTestModel", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", false  },
			{ "model_update_user", "AuthorizationTestModel", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", false  },
			{ "model_delete_user", "AuthorizationTestModel", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", false  }
		};
	}

	@DataProvider(name = "modelDeleteDataProvider")
	public Object[][] modelDeleteDataProvider() {
		return new Object[][] {
			{ UserConstants.ADMIN_USER, "AuthorizationTestModel", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", true },
			{ "actuator", "AuthorizationTestModel", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", false },
			{ "guest", "AuthorizationTestModel", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", false },
			{ "model_manager_user", "AuthorizationTestModel", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", true },
			{ UserConstants.MODEL_READ_USER, "AuthorizationTestModel", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", false  },
			{ "model_create_user", "AuthorizationTestModel", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", false  },
			{ "model_update_user", "AuthorizationTestModel", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", false  },
			{ "model_delete_user", "AuthorizationTestModel", AUTHORIZATION_MODELS_PATH + "ModelAuthorizationTestModel.json", true  }
		};
	}

	@Test(dataProvider = "modelCreateDataProvider")
	public void testModelCreateAccess(String username, String modelPath, boolean hasModelCreatePermission) throws Exception {
		changeUserInContext(username);
		assertModelCreateAccess(modelPath, hasModelCreatePermission);
	}

	@Test(dataProvider = "modelUpdateDataProvider")
	public void testModelUpdateAccess(String username, String modelPath, boolean hasModelUpdatePermission) throws Exception {
		backendAuthenticationService.executeWithBackendAuthentication("superUser", () -> modelService.create(loadResourceFromClasspathAsString(modelPath)));

		changeUserInContext(username);
		assertModelUpdateAccess(modelPath, hasModelUpdatePermission);
	}

	@Test(dataProvider = "modelReadDataProvider")
	public void testModelReadAccess(String username, String modelId, String modelPath, boolean hasModelReadPermission) throws Exception {
		backendAuthenticationService.executeWithBackendAuthentication("superUser", () -> modelService.create(loadResourceFromClasspathAsString(modelPath)));

		changeUserInContext(username);
		assertModelReadAccess(modelId, hasModelReadPermission);
	}

	@Test(dataProvider = "modelDeleteDataProvider")
	public void testModelDeleteAccess(String username, String modelId, String modelPath, boolean hasModelReadPermission) throws Exception {
		backendAuthenticationService.executeWithBackendAuthentication("superUser", () -> modelService.create(loadResourceFromClasspathAsString(modelPath)));

		changeUserInContext(username);
		assertModelDeleteAccess(modelId, hasModelReadPermission);
	}

	private void assertModelCreateAccess(String modelPath, boolean isAllowed) throws Exception {
		String model = readFile(modelPath);
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
			.post(dataServicesCoreProperties.getServer().getContextPath() + ENDPOINT_PATH)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.content(model);
		mockMvc.perform(builder)
			.andExpect(isAllowed ? status().isOk() : status().isForbidden())
			.andDo(result -> {
				int status = result.getResponse().getStatus();
				if (status == HttpStatus.FORBIDDEN.value()) {
					jsonPath("$.longMessage.key").value("error.security.notAuthorized.description").match(result);
					jsonPath("$.longMessage.default").value("User is not allowed to perform requested operation").match(result);
					jsonPath("$.shortMessage.key").value("error.security.notAuthorized.title").match(result);
					jsonPath("$.shortMessage.default").value("User is not allowed to perform requested operation").match(result);
				}
			});
	}

	private void assertModelUpdateAccess(String modelPath, boolean isAllowed) throws Exception {
		String model = readFile(modelPath);
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
			.put(dataServicesCoreProperties.getServer().getContextPath() + ENDPOINT_PATH)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.content(model);
		mockMvc.perform(builder)
			.andExpect(isAllowed ? status().isOk() : status().isForbidden())
			.andDo(result -> {
				int status = result.getResponse().getStatus();
				if (status == HttpStatus.FORBIDDEN.value()) {
					jsonPath("$.longMessage.key").value("error.security.notAuthorized.description").match(result);
					jsonPath("$.longMessage.default").value("User is not allowed to perform requested operation").match(result);
					jsonPath("$.shortMessage.key").value("error.security.notAuthorized.title").match(result);
					jsonPath("$.shortMessage.default").value("User is not allowed to perform requested operation").match(result);
				}
			});
	}

	private void assertModelReadAccess(String modelName, boolean isAllowed) throws Exception {
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
				.get(dataServicesCoreProperties.getServer().getContextPath() + ENDPOINT_PATH + "/" + modelName)
				.accept(MediaType.APPLICATION_JSON);
		mockMvc.perform(builder)
				.andExpect(isAllowed ? status().isOk() : status().isForbidden())
				.andDo(result -> {
					int status = result.getResponse().getStatus();
					if (status == HttpStatus.FORBIDDEN.value()) {
						jsonPath("$.longMessage.key").value("error.security.notAuthorized.description").match(result);
						jsonPath("$.longMessage.default").value("User is not allowed to perform requested operation").match(result);
						jsonPath("$.shortMessage.key").value("error.security.notAuthorized.title").match(result);
						jsonPath("$.shortMessage.default").value("User is not allowed to perform requested operation").match(result);
					}
				});
	}

	private void assertModelDeleteAccess(String modelName, boolean isAllowed) throws Exception {
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
			.delete(dataServicesCoreProperties.getServer().getContextPath() + ENDPOINT_PATH + "/" + modelName)
			.accept(MediaType.APPLICATION_JSON);
		mockMvc.perform(builder)
			.andExpect(isAllowed ? status().isOk() : status().isForbidden())
			.andDo(result -> {
				int status = result.getResponse().getStatus();
				if (status == HttpStatus.FORBIDDEN.value()) {
					jsonPath("$.longMessage.key").value("error.security.notAuthorized.description").match(result);
					jsonPath("$.longMessage.default").value("User is not allowed to perform requested operation").match(result);
					jsonPath("$.shortMessage.key").value("error.security.notAuthorized.title").match(result);
					jsonPath("$.shortMessage.default").value("User is not allowed to perform requested operation").match(result);
				}
			});
	}
}
