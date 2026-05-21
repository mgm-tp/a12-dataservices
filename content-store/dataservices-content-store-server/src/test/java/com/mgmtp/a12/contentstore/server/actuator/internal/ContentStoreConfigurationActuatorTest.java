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
package com.mgmtp.a12.contentstore.server.actuator.internal;

import com.mgmtp.a12.contentstore.AbstractSpringContextServerTests;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.mgmtp.a12.contentstore.server.actuator.ContentStoreConfigurationEndpoint.CHANGES;
import static com.mgmtp.a12.contentstore.server.actuator.ContentStoreConfigurationEndpoint.CONFIGURATION_ENDPOINT;
import static com.mgmtp.a12.contentstore.server.actuator.ContentStoreConfigurationEndpoint.WARNINGS;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@ActiveProfiles({"contentstore-uaa","contentstore-embedded_postgres","contentstore-actuators","dataservices-core-test-local_test"})
@TestPropertySource(properties = {
	"management.endpoints.web.exposure.include=" + CONFIGURATION_ENDPOINT,
	"management.endpoints.access.default=unrestricted",
	"management.endpoint." + CONFIGURATION_ENDPOINT + ".enabled=true"
})
public class ContentStoreConfigurationActuatorTest extends AbstractSpringContextServerTests {

	@Autowired private WebApplicationContext context;

	private MockMvc mockMvc;
	private static final String CONFIG_ENDPOINT = "/actuator/".concat(CONFIGURATION_ENDPOINT);

	@BeforeMethod
	public void setUp() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(this.context)
			.alwaysDo(print())
			.build();
	}

	@Test public void healthEndpointShouldHaveInitializationFinished() throws Exception {
		MvcResult configurationResult = mockMvc.perform(MockMvcRequestBuilders.get(CONFIG_ENDPOINT)).andReturn();
		JSONObject actualJson = new JSONObject(configurationResult.getResponse().getContentAsString());
		assertEquals(configurationResult.getResponse().getStatus(), 200);
		JSONObject changes = actualJson.getJSONObject(CHANGES);
		JSONArray warnings = actualJson.getJSONArray(WARNINGS);
		assertFalse(changes.isEmpty());
		assertTrue(warnings.isEmpty());
		assertProperty(changes, "mgmtp.a12.dataservices.contentstore.server.api.enabled", "false", "true");
		assertProperty(changes, "mgmtp.a12.dataservices.contentstore.baseUrl", "null", "http://localhost:8080");
	}

	private static void assertProperty(JSONObject changes, String key, Object regular, Object current) {
		assertTrue(changes.has(key));
		JSONObject initMode = changes.getJSONObject(key);
		assertTrue(initMode.has("default"));
		assertTrue(initMode.has("current"));
		assertEquals(initMode.get("default"), regular);
		assertEquals(initMode.get("current"), current);
	}

}
