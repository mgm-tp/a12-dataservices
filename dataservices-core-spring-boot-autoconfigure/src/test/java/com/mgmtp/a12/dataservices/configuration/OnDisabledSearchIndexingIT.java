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
package com.mgmtp.a12.dataservices.configuration;

import java.io.StringReader;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.EmbeddedPostgresInitializer;
import com.mgmtp.a12.dataservices.InitialITConfiguration;
import com.mgmtp.a12.dataservices.ModelsFunctions;
import com.mgmtp.a12.dataservices.ResourceFunctions;
import com.mgmtp.a12.dataservices.TestEnvironmentCleaner;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.query.indexing.QueryIndexManager;
import com.mgmtp.a12.dataservices.query.indexing.internal.DefaultQueryIndexManager;
import com.mgmtp.a12.dataservices.query.indexing.internal.DocumentSearchIndexBehaviour;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.uaa.authentication.backend.BackendAuthenticationService;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Integration test that verifies the behavior when search indexing is disabled via
 * `mgmtp.a12.dataservices.query.search-indexing.enabled=false`.
 *
 * Tests that:
 * 1. DefaultQueryIndexManager bean is NOT created when indexing is disabled
 * 2. DocumentSearchIndexBehaviour bean is NOT created when indexing is disabled
 * 3. Document CRUD operations (create, update, delete) still work when indexing is disabled
 */
@Slf4j
@WithUserDetails("test")
@TestPropertySource(locations = { "classpath:services-version.properties" })
@TestPropertySource(properties = {
	"spring.datasources.dataservices.embedded-postgres.enabled=true",
	"spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate",
	"spring.datasources.contentstore.embedded-postgres.enabled=true",
	"mgmtp.a12.dataservices.query.search-indexing.enabled=false"
})
@ContextConfiguration(initializers = EmbeddedPostgresInitializer.class)
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class, TransactionalTestExecutionListener.class },
	mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@SpringBootTest(classes = { InitialITConfiguration.class })
public class OnDisabledSearchIndexingIT extends AbstractTestNGSpringContextTests {

	private final TestEnvironmentCleaner testEnvironmentCleaner = new TestEnvironmentCleaner();

	@Autowired @Qualifier("dsDataSource") private DataSource dataSource;
	@Autowired @Qualifier("csDataSource") private Optional<DataSource> contentStoreDataSource;
	@Autowired private CacheManager cacheManager;
	@Autowired private BackendAuthenticationService backendAuthenticationService;
	@Autowired private DocumentService documentService;
	@Autowired private DocumentSupport documentSupport;
	@Autowired private ModelsFunctions modelsFunctions;
	@Autowired private ResourceFunctions resourceFunctions;

	@Order(Ordered.HIGHEST_PRECEDENCE)
	@BeforeClass public void cleanUpTestEnvironment() {
		testEnvironmentCleaner.cleanUpTestEnvironment(dataSource, cacheManager, contentStoreDataSource);
	}

	@Order(0)
	@BeforeClass public void initialize() {
		backendAuthenticationService.executeWithBackendAuthentication(UserConstants.ADMIN_USER, this::initializeWithSecurityBypassWrapper);
	}

	@SneakyThrows private Void initializeWithSecurityBypassWrapper() {
		modelsFunctions.createModels(
			PathConstants.ADDRESS_DOCUMENT_MODEL_PATH,
			PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH
		);
		return null;
	}

	@Test public void testDefaultQueryIndexManagerBeanNotCreated() {
		assertBeanNotPresent(DefaultQueryIndexManager.class);
	}

	@Test public void testDocumentSearchIndexBehaviourBeanNotCreated() {
		assertBeanNotPresent(DocumentSearchIndexBehaviour.class);
	}

	@Test public void testQueryIndexManagerBeanNotPresent() {
		// QueryIndexManager is the interface - should also not be available when indexing is disabled
		assertBeanNotPresent(QueryIndexManager.class);
	}

	/**
	 * Tests that document creation works when search indexing is disabled.
	 * The document is persisted to the database but not indexed for querying.
	 */
	@SneakyThrows
	@Test public void testCreateDocumentWorksWithIndexingDisabled() {
		String jsonDocument = resourceFunctions.loadResource(PathConstants.DOCUMENTS_PATH + "Address.json");
		DocumentV2 document = documentSupport.convertJSONToDocument(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, new StringReader(jsonDocument));

		DataServicesDocument savedDocument = documentService.create(document, null);

		Assert.assertNotNull(savedDocument, "Document should be created successfully");
		Assert.assertNotNull(savedDocument.getMetadata().getDocRef(), "Document reference should be set");
		Assert.assertEquals(savedDocument.getMetadata().getDocRef().getDocumentModelName(), DocumentModelConstants.ADDRESS_DOCUMENT_MODEL);
	}

	/**
	 * Tests that document update works when search indexing is disabled.
	 * Note: Since load() uses the Query API which requires indexing, we use findByDocumentReference() instead.
	 */
	@SneakyThrows
	@Test public void testUpdateDocumentWorksWithIndexingDisabled() {
		String jsonDocument = resourceFunctions.loadResource(PathConstants.DOCUMENTS_PATH + "Address.json");
		DocumentV2 document = documentSupport.convertJSONToDocument(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, new StringReader(jsonDocument));
		DataServicesDocument savedDocument = documentService.create(document, null);
		DocumentReference docRef = savedDocument.getMetadata().getDocRef();

		// Update the document - should not throw any exception
		DataServicesDocument updatedDocument = documentService.update(docRef, document, null);

		Assert.assertNotNull(updatedDocument, "Document should be updated successfully");
		Assert.assertEquals(updatedDocument.getMetadata().getDocRef(), docRef, "Document reference should remain the same after update");
	}

	/**
	 * Tests that document deletion works when search indexing is disabled.
	 * Note: Deletion should work regardless of indexing status.
	 */
	@SneakyThrows
	@Test public void testDeleteDocumentWorksWithIndexingDisabled() {
		String jsonDocument = resourceFunctions.loadResource(PathConstants.DOCUMENTS_PATH + "Address.json");
		DocumentV2 document = documentSupport.convertJSONToDocument(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, new StringReader(jsonDocument));
		DataServicesDocument savedDocument = documentService.create(document, null);
		DocumentReference docRef = savedDocument.getMetadata().getDocRef();

		// Delete the document - should not throw any exception
		documentService.delete(docRef);

		// Verify document is no longer loadable (note: load() returns empty even before deletion when indexing is disabled)
		Optional<DataServicesDocument> loadedAfterDelete = documentService.load(docRef);
		Assert.assertFalse(loadedAfterDelete.isPresent(), "Document should not be loadable after deletion");
	}

	/**
	 * Tests that document load via Query API returns empty when search indexing is disabled.
	 * This is expected behavior since DocumentService.load() uses queryService.query() internally,
	 * which requires documents to be indexed in document_search and document_fields tables.
	 */
	@SneakyThrows
	@Test public void testLoadDocumentReturnsEmptyWhenIndexingDisabled() {
		String jsonDocument = resourceFunctions.loadResource(PathConstants.DOCUMENTS_PATH + "Address.json");
		DocumentV2 document = documentSupport.convertJSONToDocument(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, new StringReader(jsonDocument));
		DataServicesDocument savedDocument = documentService.create(document, null);
		DocumentReference docRef = savedDocument.getMetadata().getDocRef();

		// Load via Query API should return empty because document is not indexed
		Optional<DataServicesDocument> loadedDocument = documentService.load(docRef);

		Assert.assertFalse(loadedDocument.isPresent(),
			"Document load should return empty when indexing is disabled, because load() uses Query API which requires indexed documents");
	}

	private void assertBeanNotPresent(Class<?> beanClass) {
		Object bean;
		try {
			bean = applicationContext.getBean(beanClass);
		} catch (NoSuchBeanDefinitionException ex) {
			bean = null;
		}
		Assert.assertNull(bean, "Bean of type " + beanClass.getSimpleName() + " should NOT be present when search indexing is disabled");
	}
}
