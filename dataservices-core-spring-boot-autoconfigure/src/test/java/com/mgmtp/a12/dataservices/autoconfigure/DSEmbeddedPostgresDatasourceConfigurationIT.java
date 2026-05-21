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
package com.mgmtp.a12.dataservices.autoconfigure;

import java.util.Arrays;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.EmbeddedPostgresInitializer;

import lombok.SneakyThrows;

@TestPropertySource(properties = {
	"spring.datasources.dataservices.embedded-postgres.enabled=true",
	"spring.datasources.contentstore.embedded-postgres.enabled=true",
	"spring.datasources.dataservices.embedded-postgres.connect-config.autosave=always",
	"spring.datasources.dataservices.embedded-postgres.locale-c-type=en_US.UTF-8"
})
@ContextConfiguration(initializers = EmbeddedPostgresInitializer.class)
@SpringBootTest
public class DSEmbeddedPostgresDatasourceConfigurationIT extends AbstractTestNGSpringContextTests {
	private static final String EN_US_UTF_8 = "en_US.UTF-8";

	@Qualifier("dsDataSource")
	@Autowired private DataSource dsDataSource;

	@Autowired private Environment environment;

	@SneakyThrows
	@Test public void testEmbeddedPostgresDatasource() {
		Assert.assertNotNull(dsDataSource);
		Assert.assertTrue(dsDataSource instanceof PGSimpleDataSource);
		PGSimpleDataSource pgSimpleDataSource = (PGSimpleDataSource) dsDataSource;
		Assert.assertEquals(pgSimpleDataSource.getDatabaseName(), "postgres");
		Assert.assertEquals(pgSimpleDataSource.getUser(), "postgres");
		Assert.assertEquals(pgSimpleDataSource.getProperty("autosave"), "always");

		// Verify that the DataSource is using the dynamically allocated port
		Integer expectedPort = environment.getProperty(EmbeddedPostgresInitializer.DS_PORT_PROPERTY, Integer.class);
		Assert.assertNotNull(expectedPort, "Expected port should be set in environment");
		Assert.assertTrue(Arrays.stream(pgSimpleDataSource.getPortNumbers())
			.anyMatch(portNumber -> portNumber == expectedPort));
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dsDataSource);
		Assert.assertEquals((jdbcTemplate.queryForObject("SELECT datctype FROM pg_database WHERE datname = current_database()", String.class)), EN_US_UTF_8);
	}
}
