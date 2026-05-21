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
package com.mgmtp.a12.contentstore;

import java.util.Arrays;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.util.TestSocketUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.contentstore.autoconfigure.internal.ContentStoreRepositoryConfiguration;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@TestPropertySource(properties = {
	"spring.datasources.contentstore.embedded-postgres.enabled=true",
	"spring.datasources.contentstore.embedded-postgres.connect-config.autosave=always",
	"spring.datasources.contentstore.liquibase.change-log=classpath:/contentstore_db/project_model.xml",
	"spring.datasources.contentstore.embedded-postgres.locale-c-type=en_US.UTF-8"
})
@EnableConfigurationProperties
@ContextConfiguration(initializers = CSEmbeddedPostgresDatasourceConfigurationTest.CsInitializer.class)
@SpringBootTest(classes = { ContentStoreRepositoryConfiguration.class })
@Slf4j
public class CSEmbeddedPostgresDatasourceConfigurationTest extends AbstractTestNGSpringContextTests {

	private static final String EN_US_UTF_8 = "en_US.UTF-8";

	@Qualifier("contentstoreDataSource")
	@Autowired private DataSource contentstoreDataSource;

	@SneakyThrows
	@Test public void testEmbeddedPostgresDatasource() {
		Assert.assertNotNull(contentstoreDataSource);
		Assert.assertTrue(contentstoreDataSource instanceof PGSimpleDataSource);
		PGSimpleDataSource pgSimpleDataSource = (PGSimpleDataSource) contentstoreDataSource;
		Assert.assertEquals(pgSimpleDataSource.getDatabaseName(), "postgres");
		Assert.assertEquals(pgSimpleDataSource.getUser(), "postgres");
		Assert.assertEquals(pgSimpleDataSource.getProperty("autosave"), "always");
		Assert.assertTrue(Arrays.stream(pgSimpleDataSource.getPortNumbers()).anyMatch(portNumber -> portNumber == CsInitializer.csEmbeddedPostgresPort));
		JdbcTemplate jdbcTemplate = new JdbcTemplate(contentstoreDataSource);
		Assert.assertEquals((jdbcTemplate.queryForObject("SELECT datctype FROM pg_database WHERE datname = current_database()", String.class)), EN_US_UTF_8);
	}

	static class CsInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		static int csEmbeddedPostgresPort;

		@Override
		public void initialize(
			ConfigurableApplicationContext configurableApplicationContext) {
			csEmbeddedPostgresPort = TestSocketUtils.findAvailableTcpPort();
			TestPropertyValues.of("spring.datasources.contentstore.embedded-postgres.port=" + csEmbeddedPostgresPort)
				.applyTo(configurableApplicationContext.getEnvironment());
		}

	}
}
