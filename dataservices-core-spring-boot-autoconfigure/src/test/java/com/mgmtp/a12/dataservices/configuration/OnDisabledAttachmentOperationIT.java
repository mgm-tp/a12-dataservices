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

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.EmbeddedPostgresInitializer;
import com.mgmtp.a12.dataservices.InitialITConfiguration;
import com.mgmtp.a12.dataservices.autoconfigure.attachments.AttachmentConfiguration;
import com.mgmtp.a12.dataservices.autoconfigure.attachments.internal.contentstore.ContentStoreConfiguration;

import lombok.extern.slf4j.Slf4j;

import static org.testng.Assert.assertNull;

@Slf4j
@WithUserDetails("test")
@TestPropertySource(locations = { "classpath:services-version.properties" })
@TestPropertySource(properties = {
	"spring.main.allow-bean-definition-overriding=true",
	"spring.datasources.dataservices.embedded-postgres.enabled=true",
	"spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate",
	"spring.datasources.contentstore.embedded-postgres.enabled=true"
})
@ContextConfiguration(initializers = EmbeddedPostgresInitializer.class)
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class, MockitoTestExecutionListener.class, TransactionalTestExecutionListener.class },
	mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@SpringBootTest(classes = { InitialITConfiguration.class },
	properties = {"mgmtp.a12.dataservices.attachments.enabled=false"})
public class OnDisabledAttachmentOperationIT extends AbstractTestNGSpringContextTests {

	@Test
	public void testBeanInitialization() {
		// Assert beans inside ServerFactoryConfiguration
		assertBeanNull("cleanUpDirtyAttachmentsJobDetail");
		assertBeanNull("cleanUpStaleAttachmentsJobDetail");

		assertBeanNull(AttachmentConfiguration.class);
		assertBeanNull(ContentStoreConfiguration.class);
	}

	private void assertBeanNull(Class beanClass) {
		Object bean;
		try {
			bean = applicationContext.getBean(beanClass);
		} catch (NoSuchBeanDefinitionException ex) {
			bean = null;
		}
		assertNull(bean);
	}

	private void assertBeanNull(String beanName) {
		Object bean;
		try {
			bean = applicationContext.getBean(beanName);
		} catch (NoSuchBeanDefinitionException ex) {
			bean = null;
		}
		assertNull(bean);
	}
}
