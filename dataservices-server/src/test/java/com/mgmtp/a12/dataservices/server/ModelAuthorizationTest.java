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

import com.mgmtp.a12.dataservices.constants.UserConstants;
import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.ADDRESS_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_LTD_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_SUPER_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.CONTRACT_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.PathConstants.ADDRESS_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.COINSURED_ADDITIONAL_PARTNER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_AMENDMENT_RELATIONSHIP_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_DOCUMENT_MODEL_PATH;

public class ModelAuthorizationTest extends AbstractSpringContextServerTests {

	private static final String PREPARED_MODEL_GROUP = "preparedModel";
	private static final String PREPARATION_REQUIRED = "preparationRequired";
	private static final String CLEANUP_REQUIRED = "cleanupRequired";

	@BeforeGroups(value = { PREPARATION_REQUIRED })
	public void beforeGroups() throws IOException {
		super.cleanUpTestEnvironment();
		super.changeUserInContext(UserConstants.ADMIN_USER);

		createModelWithRole(ADDRESS_DOCUMENT_MODEL_PATH, ADMIN_ROLE_ONLY);
		createModelWithRole(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH, COMMON_ROLES);

		modelsFunctions.createModel(BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(CONTRACT_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(COINSURED_ADDITIONAL_PARTNER_DOCUMENT_MODEL_PATH);

		modelsFunctions.createModel(CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH);
		modelsFunctions.createModel(CONTRACT_AMENDMENT_RELATIONSHIP_MODEL_PATH);
	}

	@AfterGroups(value = { PREPARATION_REQUIRED, CLEANUP_REQUIRED })
	public void afterGroups() {
		super.changeUserInContext(UserConstants.ADMIN_USER);
		super.cleanUpTestEnvironment();
	}

	@AfterMethod(onlyForGroups = { PREPARED_MODEL_GROUP })
	public void cleanupModels() {
		changeUserInContext(UserConstants.ADMIN_USER);
		try {
			modelService.delete(BUSINESS_PARTNER_LTD_MODEL);
		} catch (Exception ignored) {
			// Ignore
		}
	}

	@DataProvider(name = "modelCreateDataProvider")
	public Object[][] modelCreateDataProvider() {
		return new Object[][] {
			{ UserConstants.ADMIN_USER, true },
			{ UserConstants.ACTUATOR_USER, false },
			{ UserConstants.GUEST_USER, false },
			{ UserConstants.MODEL_MANAGER_USER, true },
			{ UserConstants.MODEL_READ_USER, false  },
			{ UserConstants.MODEL_CREATE_USER, true  },
			{ UserConstants.MODEL_UPDATE_USER, false  },
			{ UserConstants.MODEL_DELETE_USER, false  }
		};
	}

	@DataProvider(name = "modelUpdateDataProvider")
	public Object[][] modelUpdateDataProvider() {
		return new Object[][] {
			{ UserConstants.ADMIN_USER, true },
			{ UserConstants.ACTUATOR_USER, false },
			{ UserConstants.GUEST_USER, false },
			{ UserConstants.MODEL_MANAGER_USER, true },
			{ UserConstants.MODEL_READ_USER, false  },
			{ UserConstants.MODEL_CREATE_USER, false  },
			{ UserConstants.MODEL_UPDATE_USER, true  },
			{ UserConstants.MODEL_DELETE_USER, false  }
		};
	}

	@DataProvider(name = "modelReadDataProvider")
	public Object[][] modelReadDataProvider() {
		return new Object[][] {
				{ UserConstants.ADMIN_USER, true },
				{ UserConstants.ACTUATOR_USER, false },
				{ UserConstants.GUEST_USER, true },
				{ UserConstants.MODEL_MANAGER_USER, true },
				{ UserConstants.MODEL_READ_USER, true  },
				{ UserConstants.MODEL_CREATE_USER, false  },
				{ UserConstants.MODEL_UPDATE_USER, false  },
				{ UserConstants.MODEL_DELETE_USER, false  }
		};
	}

	@DataProvider(name = "modelDeleteDataProvider")
	public Object[][] modelDeleteDataProvider() {
		return new Object[][] {
			{ UserConstants.ADMIN_USER, true },
			{ UserConstants.ACTUATOR_USER, false },
			{ UserConstants.GUEST_USER, false },
			{ UserConstants.MODEL_MANAGER_USER, true },
			{ UserConstants.MODEL_READ_USER, false  },
			{ UserConstants.MODEL_CREATE_USER, false  },
			{ UserConstants.MODEL_UPDATE_USER, false  },
			{ UserConstants.MODEL_DELETE_USER, true  }
		};
	}

	@DataProvider(name = "modelListDataProvider")
	public Object[][] modelListDataProvider() {
		return new Object[][] {
				{ UserConstants.ADMIN_USER, List.of(ADDRESS_DOCUMENT_MODEL, BUSINESS_PARTNER_SUPER_MODEL, BUSINESS_PARTNER_DOCUMENT_MODEL, CONTRACT_DOCUMENT_MODEL) },
				{ UserConstants.GUEST_USER, List.of(BUSINESS_PARTNER_SUPER_MODEL, BUSINESS_PARTNER_DOCUMENT_MODEL, CONTRACT_DOCUMENT_MODEL) },
				{ UserConstants.MODEL_MANAGER_USER, List.of(ADDRESS_DOCUMENT_MODEL, BUSINESS_PARTNER_SUPER_MODEL, BUSINESS_PARTNER_DOCUMENT_MODEL, CONTRACT_DOCUMENT_MODEL) },
				{ UserConstants.MODEL_READ_USER, List.of(BUSINESS_PARTNER_SUPER_MODEL, CONTRACT_DOCUMENT_MODEL) },
				{ UserConstants.ACTUATOR_USER, List.of() }
		};
	}

	@Test(dataProvider = "modelCreateDataProvider", groups = { PREPARED_MODEL_GROUP })
	public void testModelCreateAccess(String username, boolean hasPermission) throws IOException {
		String modelContent = loadModelWithRoles("admin,guest,ModelCreate");
		changeUserInContext(username);

		assertAccessPermission(() -> modelService.create(modelContent), hasPermission,
			"Model creation for user: " + username);
	}

	@Test(dataProvider = "modelUpdateDataProvider", groups = { PREPARATION_REQUIRED, PREPARED_MODEL_GROUP })
	public void testModelUpdateAccess(String username, boolean hasPermission) throws IOException {
		String modelContent = loadModelWithRoles("admin,guest,ModelUpdate");

		modelsFunctions.createModelFromJson(modelContent);
		changeUserInContext(username);

		assertAccessPermission(() -> {
			try {
				modelService.update(replaceRoles(modelContent, "admin,guest,tester"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}, hasPermission, "Model update for user: " + username);
	}

	@Test(dataProvider = "modelReadDataProvider", groups = { PREPARATION_REQUIRED, PREPARED_MODEL_GROUP })
	public void testModelReadAccess(String username, boolean hasPermission) throws IOException {
		String modelContent = loadModelWithRoles("admin,guest,ModelRead");
		modelsFunctions.createModelFromJson(modelContent);
		changeUserInContext(username);

		assertAccessPermission(() -> modelService.load(BUSINESS_PARTNER_LTD_MODEL),
			hasPermission, "Model read for user: " + username);
	}

	@Test(dataProvider = "modelDeleteDataProvider", groups = { PREPARATION_REQUIRED, PREPARED_MODEL_GROUP })
	public void testModelDeleteAccess(String username, boolean hasPermission) throws IOException {
		String modelContent = loadModelWithRoles("admin,guest,ModelDelete");
		modelsFunctions.createModelFromJson(modelContent);
		changeUserInContext(username);

		assertAccessPermission(() -> modelService.delete(BUSINESS_PARTNER_LTD_MODEL),
			hasPermission, "Model delete for user: " + username);
	}

	@Test(dataProvider = "modelListDataProvider", groups = { PREPARATION_REQUIRED })
	public void testModelListAccess(String username, List<String> expectedModels) {
		changeUserInContext(username);

		List<String> modelsList = List.of(ADDRESS_DOCUMENT_MODEL, BUSINESS_PARTNER_SUPER_MODEL, BUSINESS_PARTNER_DOCUMENT_MODEL, CONTRACT_DOCUMENT_MODEL);
		Collection<String> modelsCollection = modelService.load(modelsList).stream()
				.map(e -> e.getHeader().getId())
				.toList();
		Assert.assertEquals(modelsCollection, expectedModels);
	}

}
