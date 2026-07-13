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
package com.mgmtp.a12.dataservices.relationship;

import java.io.IOException;
import java.util.List;

import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import tools.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.model.AbstractModelServiceIT;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.relationship.internal.ModelGraphService;
import com.mgmtp.a12.dataservices.uaa.UaaTestHelper;

public class ModelGraphServiceIT extends AbstractModelServiceIT {

	@Autowired private ModelGraphService modelGraphService;
	@Autowired private ObjectMapper objectMapper;
	@Autowired private ModelService modelService;

	private static final String MODELGRAPH_FILE = "model/modelGraph.json";
	private static final String MODELGRAPH_WITH_REFERENCES_FILE = "model/modelGraphWithReferences.json";

	@BeforeClass
	public void otherModels() throws IOException {
		setUserTo(UserConstants.ADMIN_USER);
		modelService.create(resourceFunctions.loadResource("/model/other/BusinessPartnerOverview.json"));
		modelService.create(resourceFunctions.loadResource("/model/other/ContractForm.json"));
		modelService.create(resourceFunctions.loadResource("/model/other/ContractOverview.json"));
		modelService.create(resourceFunctions.loadResource("/model/other/RecursiveOverview.json"));
	}

	@Test public void testModelGraphNoReferences() throws IOException {
		JSONAssert.assertEquals(
			resourceFunctions.loadResource(MODELGRAPH_FILE),
			objectMapper.writeValueAsString(modelGraphService.constructModelGraph()), false);
	}

	@Test public void testModelGraphWithReferences() throws IOException {
		JSONAssert.assertEquals(
			resourceFunctions.loadResource(MODELGRAPH_WITH_REFERENCES_FILE),
			objectMapper.writeValueAsString(modelGraphService.constructModelGraph()), false);
	}

	@Test public void testModelGraphWhenChangingRoles() throws IOException {
		UaaTestHelper.TestUserDetails user = UaaTestHelper.createUser();
		user.setAuthorities(List.of(
			new UaaTestHelper.TestGrantedAuthority("tester", List.of())
		));
		// without admin role, user can not get any models.
		UaaTestHelper.setCurrentUserName(user);
		ModelGraphRoot modelGraphRoot = modelGraphService.constructModelGraph(user.getUsername());
		Assert.assertEquals(modelGraphRoot.getRelationshipModels().size(), 0);
		Assert.assertEquals(modelGraphRoot.getDocumentModels().size(), 0);
		Assert.assertEquals(modelGraphRoot.getComposeDocumentModels().size(), 0);
		Assert.assertEquals(modelGraphRoot.getGenericModels().size(), 0);

		// update new role for user then user can load model graph.
		user.setAuthorities(List.of(
			new UaaTestHelper.TestGrantedAuthority("admin", List.of(new UaaTestHelper.TestAccessRight("MODEL_READ")))
		));

		UaaTestHelper.setCurrentUserName(user);
		JSONAssert.assertEquals(
			resourceFunctions.loadResource(MODELGRAPH_FILE),
			objectMapper.writeValueAsString(modelGraphService.constructModelGraph(user.getUsername())), false);
	}
}
