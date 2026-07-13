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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.mockito.testng.MockitoTestNGListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.util.TestSocketUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.InitialITConfiguration;
import com.mgmtp.a12.dataservices.internal.DataSourceContextHolder;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WithUserDetails("test")
@TestPropertySource(
	locations = "classpath:services-version.properties",
	properties = {
		"spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate",
		"spring.datasources.contentstore.embedded-postgres.enabled=true",
		"spring.main.allow-bean-definition-overriding=true"
	})
@Listeners(MockitoTestNGListener.class)
@TestExecutionListeners(
	listeners = {
		WithSecurityContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class,
	},
	mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
@ContextConfiguration(initializers = ReadReplicaRoutingIT.TwoDatasourcesInitializer.class)
@SpringBootTest(classes = { InitialITConfiguration.class })
public class ReadReplicaRoutingIT extends AbstractTestNGSpringContextTests {

	@Autowired
	@Qualifier("dsDataSource")
	private DataSource dsDataSource;

	@Test
	public void shouldRouteToReplicaWhenContextIsReplica() throws SQLException {
		DataSourceContextHolder.setDataSourceType(DataSourceContextHolder.DataSourceType.REPLICA);
		try {
			int replicaPort = TwoDatasourcesInitializer.getReplicaPort();
			try (Connection conn = dsDataSource.getConnection()) {
				String url = conn.getMetaData().getURL();
				assertTrue(url.contains(":" + replicaPort + "/"),
					"REPLICA context must connect to replica datasource, but URL was: " + url);
			}
		} finally {
			DataSourceContextHolder.clearDataSourceType();
		}
	}

	@Test
	public void shouldRouteToPrimaryWhenContextIsPrimary() throws SQLException {
		DataSourceContextHolder.setDataSourceType(DataSourceContextHolder.DataSourceType.PRIMARY);
		try {
			int replicaPort = TwoDatasourcesInitializer.getReplicaPort();
			try (Connection conn = dsDataSource.getConnection()) {
				String url = conn.getMetaData().getURL();
				assertFalse(url.contains(":" + replicaPort + "/"),
					"PRIMARY context must connect to primary datasource, but URL was: " + url);
			}
		} finally {
			DataSourceContextHolder.clearDataSourceType();
		}
	}

	@Test
	public void shouldDefaultToPrimaryWhenNoContextSet() throws SQLException {
		DataSourceContextHolder.forceClear();

		assertEquals(DataSourceContextHolder.getDataSourceType(), DataSourceContextHolder.DataSourceType.PRIMARY,
			"Default datasource type must be PRIMARY when no context is set");

		int replicaPort = TwoDatasourcesInitializer.getReplicaPort();
		try (Connection conn = dsDataSource.getConnection()) {
			String url = conn.getMetaData().getURL();
			assertFalse(url.contains(":" + replicaPort + "/"),
				"Default (no-context) connection must not use the replica port, but URL was: " + url);
		}
	}

	@AfterClass(alwaysRun = true)
	public void closeEmbeddedPostgres() {
		TwoDatasourcesInitializer.closeAll();
	}

	public static class TwoDatasourcesInitializer
		implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		private static EmbeddedPostgres primaryPostgres;
		private static EmbeddedPostgres replicaPostgres;

		@Override
		public void initialize(ConfigurableApplicationContext context) {
			try {
				if (primaryPostgres == null) {
					primaryPostgres = EmbeddedPostgres.builder()
						.setDataDirectory(createTempDir("ds-primary-" + UUID.randomUUID()))
						.setCleanDataDirectory(true)
						.start();
				}
				if (replicaPostgres == null) {
					replicaPostgres = EmbeddedPostgres.builder()
						.setDataDirectory(createTempDir("ds-replica-" + UUID.randomUUID()))
						.setCleanDataDirectory(true)
						.start();
				}

				String primaryUrl = primaryPostgres.getJdbcUrl("postgres", "postgres");
				String replicaUrl = replicaPostgres.getJdbcUrl("postgres", "postgres");

				int csPort = TestSocketUtils.findAvailableTcpPort();
				String csDataDir = createTempDir("cs-postgres-" + UUID.randomUUID()).getAbsolutePath();

				TestPropertyValues.of(
					"spring.datasources.dataservices.embedded-postgres.enabled=false",
					"spring.datasources.dataservices.url=" + primaryUrl,
					"spring.datasources.dataservices.username=postgres",
					"spring.datasources.dataservices.password=",
					"spring.datasources.dataservices.jpa.database=postgresql",
					"spring.datasources.dataservices-read-replica.url=" + replicaUrl,
					"spring.datasources.dataservices-read-replica.username=postgres",
					"spring.datasources.dataservices-read-replica.password=",
					"spring.datasources.contentstore.embedded-postgres.port=" + csPort,
					"spring.datasources.contentstore.embedded-postgres.path=file:" + csDataDir,
					"spring.datasources.contentstore.embedded-postgres.clean-data-directory=true"
				).applyTo(context.getEnvironment());

				log.info("Primary DS postgres at {}", primaryUrl);
				log.info("Replica DS postgres at {}", replicaUrl);
			} catch (IOException e) {
				throw new RuntimeException("Failed to start embedded Postgres instances for routing test", e);
			}
		}

		public static int getReplicaPort() {
			if (replicaPostgres == null) {
				throw new IllegalStateException("Replica Postgres has not been started");
			}
			return replicaPostgres.getPort();
		}

		public static void closeAll() {
			closeQuietly(primaryPostgres);
			closeQuietly(replicaPostgres);
			primaryPostgres = null;
			replicaPostgres = null;
		}

		private static void closeQuietly(EmbeddedPostgres pg) {
			if (pg != null) {
				try {
					pg.close();
				} catch (IOException e) {
					log.warn("Failed to close embedded Postgres", e);
				}
			}
		}

		private static File createTempDir(String prefix) {
			try {
				File tempDir = Files.createTempDirectory(prefix).toFile();
				tempDir.deleteOnExit();
				return tempDir;
			} catch (IOException e) {
				throw new RuntimeException("Failed to create temp directory", e);
			}
		}
	}
}
