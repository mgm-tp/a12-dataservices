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
package com.mgmtp.a12.dataservices;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.util.TestSocketUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j public class EmbeddedPostgresInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
	// Property keys to store allocated ports in the application context
	public static final String DS_PORT_PROPERTY = "test.embedded-postgres.ds.port";
	public static final String CS_PORT_PROPERTY = "test.embedded-postgres.cs.port";

	@Override
	public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
		int dsEmbeddedPostgresPort = TestSocketUtils.findAvailableTcpPort();
		int csEmbeddedPostgresPort = TestSocketUtils.findAvailableTcpPort();
		// Create unique temp directories for each Spring context to avoid conflicts on Jenkins
		String uniqueId = UUID.randomUUID().toString();
		File dsTempDir = createTempDir("ds-postgres-" + uniqueId);
		File csTempDir = createTempDir("cs-postgres-" + uniqueId);
		TestPropertyValues.of(
				"spring.datasources.dataservices.embedded-postgres.port=" + dsEmbeddedPostgresPort,
				"spring.datasources.contentstore.embedded-postgres.port=" + csEmbeddedPostgresPort,
				// Set unique data directories to avoid conflicts between parallel test runs
				"spring.datasources.dataservices.embedded-postgres.path=file:" + dsTempDir.getAbsolutePath(),
				"spring.datasources.contentstore.embedded-postgres.path=file:" + csTempDir.getAbsolutePath(),
				// Store ports as test properties so tests can access them
				DS_PORT_PROPERTY + "=" + dsEmbeddedPostgresPort,
				CS_PORT_PROPERTY + "=" + csEmbeddedPostgresPort,
				// Enable data directory cleaning in tests for isolation
				"spring.datasources.dataservices.embedded-postgres.clean-data-directory=true",
				"spring.datasources.contentstore.embedded-postgres.clean-data-directory=true")
			.applyTo(configurableApplicationContext.getEnvironment());
		log.info("Using embedded Postgres in {} ({}): DS={}@{}, CS={}@{} (clean data directory enabled)",
			configurableApplicationContext.getApplicationName(),
			configurableApplicationContext.getDisplayName(),
			dsEmbeddedPostgresPort, dsTempDir.getAbsolutePath(),
			csEmbeddedPostgresPort, csTempDir.getAbsolutePath());
	}

	private File createTempDir(String prefix) {
		try {
			File tempDir = Files.createTempDirectory(prefix).toFile();
			tempDir.deleteOnExit();
			return tempDir;
		} catch (IOException e) {
			throw new RuntimeException("Failed to create temp directory for embedded Postgres", e);
		}
	}
}
