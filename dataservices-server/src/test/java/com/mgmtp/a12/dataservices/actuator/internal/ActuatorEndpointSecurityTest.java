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

import com.mgmtp.a12.dataservices.EmbeddedPostgresInitializer;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.server.ServerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test verifying that actuator endpoint security is properly enforced.
 *
 * - All actuator endpoints require authentication and the `ACCESS_ACTUATOR` role.
 * - The health endpoint (`/actuator/health/**`) remains publicly accessible for K8s probes.
 */
@SpringBootTest(classes = {ServerConfiguration.class})
@ContextConfiguration(initializers = EmbeddedPostgresInitializer.class)
@ActiveProfiles({
	"dataservices-uaa",
	"dataservices-core-test-local_auth",
	"dataservices-external_postgres",
	"dataservices-embedded_contentstore",
	"dataservices-rpc",
	"dataservices-actuators"
})
@TestPropertySource(properties = {
	"spring.main.allow-bean-definition-overriding=true",
	"spring.datasources.dataservices.embedded-postgres.enabled=true",
	"spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate",
	"spring.datasources.contentstore.embedded-postgres.enabled=true"
})
@TestExecutionListeners(
	listeners = {WithSecurityContextTestExecutionListener.class},
	mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class ActuatorEndpointSecurityTest extends AbstractTestNGSpringContextTests {

	@Autowired
	private FilterChainProxy springSecurityFilterChain;

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@BeforeMethod
	public void setUp() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(this.context)
			.apply(SecurityMockMvcConfigurers.springSecurity(springSecurityFilterChain))
			.build();
	}

	// --- Health endpoint: publicly accessible (no auth required) ---

	@Test(description = "Should return 200 when accessing /actuator/health without authentication")
	public void shouldReturn200WhenAccessingHealthWithoutAuthentication() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/actuator/health"))
			.andExpect(status().isOk());
	}

	@Test(description = "Should return 200 when accessing /actuator/health/readiness without authentication")
	public void shouldReturn200WhenAccessingHealthReadinessWithoutAuthentication() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/actuator/health/readiness"))
			.andExpect(status().isOk());
	}

	@Test(description = "Should return 200 when accessing /actuator/health/liveness without authentication")
	public void shouldReturn200WhenAccessingHealthLivenessWithoutAuthentication() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/actuator/health/liveness"))
			.andExpect(status().isOk());
	}

	// --- Sensitive endpoints: require authentication ---

	@Test(description = "Should return 401 when accessing /actuator/env without authentication")
	public void shouldReturn401WhenAccessingEnvWithoutAuthentication() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/actuator/env"))
			.andExpect(status().isUnauthorized());
	}

	@Test(description = "Should return 401 when accessing /actuator/metrics without authentication")
	public void shouldReturn401WhenAccessingMetricsWithoutAuthentication() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/actuator/metrics"))
			.andExpect(status().isUnauthorized());
	}

	@Test(description = "Should return 401 when accessing /actuator/beans without authentication")
	public void shouldReturn401WhenAccessingBeansWithoutAuthentication() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/actuator/beans"))
			.andExpect(status().isUnauthorized());
	}

	@Test(description = "Should return 401 when accessing /actuator/mappings without authentication")
	public void shouldReturn401WhenAccessingMappingsWithoutAuthentication() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/actuator/mappings"))
			.andExpect(status().isUnauthorized());
	}

	// --- Sensitive endpoints: require ACCESS_ACTUATOR role ---

	@Test(description = "Should return 403 when guest user accesses /actuator/env")
	@WithUserDetails(value = UserConstants.GUEST_USER, setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void shouldReturn403WhenGuestAccessesEnv() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/actuator/env"))
			.andExpect(status().isForbidden());
	}

	@Test(description = "Should return 403 when guest user accesses /actuator/metrics")
	@WithUserDetails(value = UserConstants.GUEST_USER, setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void shouldReturn403WhenGuestAccessesMetrics() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/actuator/metrics"))
			.andExpect(status().isForbidden());
	}

	// --- Actuator user (has ACCESS_ACTUATOR): should succeed ---

	@Test(description = "Should return 200 when actuator user accesses /actuator/env")
	@WithUserDetails(value = UserConstants.ACTUATOR_USER, setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void shouldReturn200WhenActuatorUserAccessesEnv() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/actuator/env"))
			.andExpect(status().isOk());
	}

	@Test(description = "Should return 200 when actuator user accesses /actuator/metrics")
	@WithUserDetails(value = UserConstants.ACTUATOR_USER, setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void shouldReturn200WhenActuatorUserAccessesMetrics() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/actuator/metrics"))
			.andExpect(status().isOk());
	}

	@Test(description = "Should return 200 when actuator user accesses /actuator/beans")
	@WithUserDetails(value = UserConstants.ACTUATOR_USER, setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void shouldReturn200WhenActuatorUserAccessesBeans() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/actuator/beans"))
			.andExpect(status().isOk());
	}

	@Test(description = "Should return 200 when actuator user accesses /actuator/mappings")
	@WithUserDetails(value = UserConstants.ACTUATOR_USER, setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void shouldReturn200WhenActuatorUserAccessesMappings() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/actuator/mappings"))
			.andExpect(status().isOk());
	}
}
