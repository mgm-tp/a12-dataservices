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
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
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
import org.testng.annotations.BeforeClass;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.EmbeddedPostgresInitializer;
import com.mgmtp.a12.dataservices.TestEnvironmentCleaner;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.test.utils.UAASecurityBypassUtils;
import com.mgmtp.a12.dataservices.uaa.UaaTestHelper;
import com.mgmtp.a12.model.header.HeaderParser;
import com.mgmtp.a12.uaa.authorization.SecurityFreeCallback;
import com.mgmtp.a12.uaa.authorization.UAASecurityBypass;

import lombok.SneakyThrows;

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

	protected static final Predicate<String> ATTACHMENT_URL_PATTERN =
		Pattern.compile("^http://localhost:8080/cs/download/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\?filename=([^&]*\\.[^&]+)$")
			.asPredicate();
	protected static final Predicate<String> THUMBNAIL_URL_PATTERN =
		Pattern.compile("^http://localhost:8080/cs/download/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
			.asPredicate();

	@Autowired protected ResourcePatternResolver resourcePatternResolver;
	@Autowired protected ResourceLoader resourceLoader;
	@Autowired protected CacheManager cacheManager;
	@Autowired protected HeaderParser headerParser;
	@Autowired protected ModelService modelService;
	@Autowired protected UAASecurityBypass securityBypass;
	@Autowired protected ObjectMapper objectMapper;
	@Autowired @Qualifier("dsDataSource") private DataSource dataSource;
	@Autowired protected UserDetailsService userDetailsService;
	@Autowired protected DocumentService documentService;
	@Autowired protected DataServicesCoreProperties dataServicesCoreProperties;

	private final TestEnvironmentCleaner testEnvironmentCleaner = new TestEnvironmentCleaner();
	protected static final String RESOURCE_TEST_BASE = "src/test/resources/";
	protected static final String MODEL_PATH_FOLDER = "/models/document/";
	protected static final String BUSINESS_PARTNER_DOCUMENT_FILE = "document/BusinessPartnerWith1Attachment.json";
	protected static final String BUSINESS_PARTNER_MODEL_NAME = "BusinessPartner";

	@BeforeClass
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public void setUpUser() {
		runWithSecurityBypass(() -> changeUserInContext(UserConstants.ADMIN_USER));
	}

	@BeforeClass
	public void cleanUpTestEnvironment() {
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

	@SneakyThrows
	protected String loadResourceFromClasspathAsString(final String relativePath) {
		final Resource resource = resourcePatternResolver.getResource(String.format("classpath:%s", relativePath));
		final Writer stringWriter = new StringWriter();
		IOUtils.copy(resource.getInputStream(), stringWriter, StandardCharsets.UTF_8);
		return stringWriter.toString();
	}

	protected String readFile(final String fileName) {
		try {
			final byte[] encoded = Files.readAllBytes(Paths.get(RESOURCE_TEST_BASE + fileName));
			return new String(encoded, StandardCharsets.UTF_8);
		} catch (final IOException ex) {
			throw new IllegalStateException("Resource " + fileName + " cannot be found");
		}
	}

	protected List<String> readLinesOfFile(final String fileName) {
		try {
			return Files.readAllLines(Paths.get(RESOURCE_TEST_BASE + fileName));
		} catch (final IOException ex) {
			throw new IllegalStateException("Resource " + fileName + " cannot be found");
		}
	}

	protected GenericModel createModel(String modelContent) {
		return modelService.create(modelContent);
	}

	@SneakyThrows
	protected <T> T jsonToObject(TreeNode node, TypeReference<T> type) {
		return objectMapper
			.treeAsTokens(node)
			.readValueAs(type);
	}

	@SneakyThrows
	protected <T> T jsonToObject(TreeNode node, Class<T> type) {
		return objectMapper
			.treeAsTokens(node)
			.readValueAs(type);
	}
}

