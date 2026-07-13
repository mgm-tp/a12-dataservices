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
package com.mgmtp.a12.dataservices.actuator.internal;

import com.mgmtp.a12.dataservices.server.AbstractSpringContextServerTests;
import com.mgmtp.a12.dataservices.initialization.events.DataServicesInitializationFinishedEvent;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@TestPropertySource(properties = {
	"management.endpoint.health.show-details=always",
	// These properties below disable health check of components not needed for these tests
	"management.health.defaults.enabled=false"
})
public class DataServicesInitializationFinishedHealthIndicatorTest extends AbstractSpringContextServerTests {
	private static final String HEALTH_ENDPOINT = "/actuator/health";
	private static final String INITIALIZATION_FINISHED_ENDPOINT = "/actuator/health/dataservicesInitializationFinished";

	@Autowired private WebApplicationContext context;
	@Autowired private ApplicationEventPublisher applicationEventPublisher;

	private MockMvc mockMvc;

	@BeforeMethod public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	@Test public void healthEndpointShouldHaveInitializationFinished() throws Exception {
		applicationEventPublisher.publishEvent(new DataServicesInitializationFinishedEvent());
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(HEALTH_ENDPOINT)).andReturn();

		JSONObject actualJson = new JSONObject(mvcResult.getResponse().getContentAsString());
		assertEquals(mvcResult.getResponse().getStatus(), 200);
		assertTrue(actualJson.getJSONObject("components").has("dataservicesInitializationFinished"));
	}

	@Test public void eventPublished_DirectEndpointShouldBe200AndUP() throws Exception {
		applicationEventPublisher.publishEvent(new DataServicesInitializationFinishedEvent());
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(INITIALIZATION_FINISHED_ENDPOINT)).andReturn();

		JSONObject actualJson = new JSONObject(mvcResult.getResponse().getContentAsString());
		assertEquals(mvcResult.getResponse().getStatus(), 200);
		assertEquals(actualJson.get("status"), "UP");
	}
}
