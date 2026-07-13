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
package com.mgmtp.a12.examples;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import tools.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.DocumentFunctions;
import com.mgmtp.a12.dataservices.EmbeddedPostgresInitializer;
import com.mgmtp.a12.dataservices.ModelsFunctions;
import com.mgmtp.a12.dataservices.ResourceFunctions;
import com.mgmtp.a12.dataservices.TestEnvironmentCleaner;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentRepository;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.uaa.UaaTestHelper;

import lombok.extern.slf4j.Slf4j;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH;

@Slf4j
@WithUserDetails(UserConstants.ADMIN_USER)
@ContextConfiguration(initializers = EmbeddedPostgresInitializer.class)
@TestPropertySource(properties = {
	"spring.datasources.dataservices.embedded-postgres.enabled=true",
	"spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate",
	"spring.datasources.contentstore.embedded-postgres.enabled=true",
	"spring.main.allow-bean-definition-overriding=true"
})
@SpringBootTest(classes = { ExampleExtendingServerApplication.class })
public class AbstractITBase extends AbstractTestNGSpringContextTests {

	public static final String BUSINESS_PARTNER_DOCUMENT_MODEL_NAME = "BusinessPartner-document";
	public static final String CO_INSURER_ADDITIONAL_FIELDS_MODEL_NAME = "CoInsurerAdditionalFields";
	public static final String CONTRACT_DOCUMENT_MODEL_NAME = "Contract-document";
	public static final String PERSON_WITH_CUSTOM_TYPE_DOCUMENT_MODEL_NAME = "PersonWithCustomType";

	public static final String CO_INSURER_RELATIONSHIP_MODEL_NAME = "CoInsurer";

	public static final String SRC_MAIN_RESOURCES_PATH = "file:src/main/resources/";
	public static final String ATTACHMENT_PATH = "attachment/";
	public static final String DOCUMENT_PATH = "document/";
	public static final String CONTRACT_ROLE = "contract";
	public static final String BUSINESS_PARTNER_ROLE = "businessPartner";
	public static final String MODEL_PATH = "model/";

	@Autowired protected DocumentService documentService;
	@Autowired protected UserDetailsService userDetailsService;
	@Autowired protected ModelService modelService;
	@Autowired @Qualifier("dsDataSource") protected DataSource dataSource;
	@Autowired protected CacheManager cacheManager;
	@Autowired protected ResourceFunctions resourceFunctions;
	@Autowired protected ModelsFunctions modelsFunctions;
	@Autowired protected DocumentFunctions documentFunctions;
	@Autowired protected ObjectMapper objectMapper;
	@Autowired protected DefaultDocumentRepository documentRepository;

	/**
	 * Jackson 2.x ObjectMapper for creating Jackson 2.x JsonNode objects required by RPC operations.
	 * RPC operations use jsonrpc4j which is based on Jackson 2.x (com.fasterxml.jackson.*).
	 * Spring Boot 4.x uses Jackson 3.x (tools.jackson.*) for the injected objectMapper bean.
	 */
	protected static final tools.jackson.databind.ObjectMapper JACKSON_2_OBJECT_MAPPER = new tools.jackson.databind.ObjectMapper();

	protected static final String PARTNER_DOCUMENT_FILE = "document/BusinessPartner_Document.json";
	protected static final String CONTRACT_DOCUMENT_FILE = "document/Contract_Document.json";
	protected static final String COINSURED_DOCUMENT_FILE = "document/CoinsuredAdditionalFields_Document.json";

	protected final TestEnvironmentCleaner testEnvironmentCleaner = new TestEnvironmentCleaner();

	@Order(HIGHEST_PRECEDENCE)
	@BeforeClass public void prepareTestEnvironment() {
		setUserTo(UserConstants.ADMIN_USER);
		testEnvironmentCleaner.cleanUpTestEnvironment(dataSource, cacheManager);
		modelsFunctions.createModel(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
	}

	@AfterClass public void cleanupTestEnvironment() {
		setUserTo(UserConstants.ADMIN_USER);
		testEnvironmentCleaner.cleanUpTestEnvironment(dataSource, cacheManager);
	}

	protected void changeUserInContext(String username) {
		UaaTestHelper.setCurrentUserName(userDetailsService.loadUserByUsername(username));
	}

	protected void setUserTo(String username) {
		UaaTestHelper.setCurrentUserName(userDetailsService.loadUserByUsername(username));
	}

	private static String createPathToJsonFile(String prefixPath, String modelName) {
		return String.format("%s%s.json", prefixPath, modelName);
	}
}
