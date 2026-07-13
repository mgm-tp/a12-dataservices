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
import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.ReactorContextTestExecutionListener;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

import tools.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.DocumentFunctions;
import com.mgmtp.a12.dataservices.EmbeddedPostgresInitializer;
import com.mgmtp.a12.dataservices.JsonFunctions;
import com.mgmtp.a12.dataservices.LinksFunctions;
import com.mgmtp.a12.dataservices.ModelsFunctions;
import com.mgmtp.a12.dataservices.ResourceFunctions;
import com.mgmtp.a12.dataservices.TestEnvironmentCleaner;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.test.utils.UAASecurityBypassUtils;
import com.mgmtp.a12.dataservices.uaa.UaaTestHelper;
import com.mgmtp.a12.uaa.authorization.SecurityFreeCallback;
import com.mgmtp.a12.uaa.authorization.UAASecurityBypass;

import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_LTD_MODEL_PATH;

/**
 * Base test class with all necessary configurations to run repository/service tests
 */
@ContextConfiguration(initializers = EmbeddedPostgresInitializer.class)
@WithUserDetails(
	value = UserConstants.ADMIN_USER,
	setupBefore = TestExecutionEvent.TEST_EXECUTION)
@TestPropertySource(properties = {
	"spring.main.allow-bean-definition-overriding=true",
	"spring.datasources.dataservices.embedded-postgres.enabled=true",
	"spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate",
	"spring.datasources.contentstore.embedded-postgres.enabled=true"
})
@TestExecutionListeners(
	inheritListeners = false,
	listeners = {
		WithSecurityContextTestExecutionListener.class,
		ReactorContextTestExecutionListener.class
	},
	mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
@SpringBootTest(classes = { ServerConfiguration.class })
public abstract class AbstractSpringContextServerTests extends AbstractTestNGSpringContextTests {

	@Autowired protected ResourceFunctions resourceFunctions;
	@Autowired protected ModelsFunctions modelsFunctions;
	@Autowired protected DocumentFunctions documentFunctions;
	@Autowired protected LinksFunctions linksFunctions;

	@Autowired protected CacheManager cacheManager;

	@Autowired protected UAASecurityBypass securityBypass;
	@Autowired protected ObjectMapper objectMapper;
	@Autowired @Qualifier("dsDataSource") private DataSource dataSource;
	@Autowired @Qualifier("dsEntityManagerFactory") private EntityManagerFactory dsEntityManagerFactory;

	@Autowired protected UserDetailsService userDetailsService;
	@Autowired protected ModelService modelService;
	@Autowired protected DocumentService documentService;

	private final TestEnvironmentCleaner testEnvironmentCleaner = new TestEnvironmentCleaner();

	protected static final String ADMIN_ROLE_ONLY = "admin";
	protected static final String COMMON_ROLES = "admin,guest";

	protected static final String BUSINESS_PARTNER_DOCUMENT_FILE = "document/BusinessPartnerWith1Attachment.json";

	protected static final String PARTNER_DOCUMENT_FILE = "document/BusinessPartnerDocument.json";
	protected static final String CONTRACT_DOCUMENT_FILE = "document/ContractDocument.json";
	protected static final String COINSURED_DOCUMENT_FILE = "document/CoinsuredAdditionalFieldsDocument.json";

	@BeforeClass
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public void setUpUser() {
		runWithSecurityBypass(() -> changeUserInContext(UserConstants.ADMIN_USER));
	}

	@BeforeClass
	public void cleanUpTestEnvironment() {
		dsEntityManagerFactory.getCache().evictAll();
		testEnvironmentCleaner.cleanUpDatabase(dataSource);
		testEnvironmentCleaner.cleanUpCache(cacheManager);
	}

	protected void changeUserInContext(String username) {
		UaaTestHelper.setCurrentUserName(userDetailsService.loadUserByUsername(username));
	}

	protected void runWithSecurityBypass(SecurityFreeCallback code) {
		try {
			UAASecurityBypassUtils.modifyBypassDisabledFlag(securityBypass, false);
		} catch (IllegalAccessException e) {
			throw new UnexpectedException(e);
		}
		securityBypass.runWithSecurityBypass(code);
	}

	protected void createModelWithRole(String path, String role) throws IOException {
		String modelContent = resourceFunctions.loadResource(path);
		modelContent = replaceRoles(modelContent, role);
		modelsFunctions.createModelFromJson(modelContent);
	}

	protected String loadModelWithRoles(String roles) throws IOException {
		String modelContent = resourceFunctions.loadResource(BUSINESS_PARTNER_LTD_MODEL_PATH);
		return replaceRoles(modelContent, roles);
	}

	protected void createModel(String modelContent) {
		modelService.create(modelContent);
	}

	protected void updateModelRole(String modelName, String newRole) throws IOException {
		changeUserInContext(UserConstants.ADMIN_USER);

		String existingModelJson = objectMapper.writeValueAsString(modelService.load(modelName));
		String updatedModelJson = replaceRoles(existingModelJson, newRole);
		modelService.update(updatedModelJson);
	}

	protected String replaceRoles(String modelContent, String roles) throws IOException {
		return JsonFunctions.replaceValue(modelContent, "/header/annotations/0/value", roles, true);
	}

	/**
	 * Helper method to reduce boilerplate in authorization tests
	 * Executes an operation and asserts whether AccessDeniedException is thrown correctly
	 */
	protected void assertAccessPermission(Runnable operation, boolean hasPermission, String operationDescription) {
		try {
			operation.run();
			Assert.assertTrue(hasPermission,
					operationDescription + " should have been denied but was allowed");
		} catch (AccessDeniedException e) {
			Assert.assertFalse(hasPermission,
					operationDescription + " should have been allowed but was denied: " + e.getMessage());
		}
	}

	private static String createPathToJsonFile(String prefixPath, String modelName) {
		return "%s%s.json".formatted(prefixPath, modelName);
	}

}

