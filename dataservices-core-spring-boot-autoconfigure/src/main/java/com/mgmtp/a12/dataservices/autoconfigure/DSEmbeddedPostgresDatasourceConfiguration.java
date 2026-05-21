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

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;

import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.common.properties.EmbeddedPostgresProperties;
import com.mgmtp.a12.dataservices.configuration.ExposePropertiesToActuator;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;

/**
 * Spring Boot configuration for an embedded Postgres {@link javax.sql.DataSource} used by DataServices.
 *
 * Starts an embedded Postgres instance on demand and exposes it as the primary datasource when enabled
 * via `spring.datasources.dataservices.embedded-postgres.enabled=true`.
 */
@ConditionalOnProperty(prefix = "spring.datasources.dataservices.embedded-postgres", name = "enabled", havingValue = "true")
@Configuration public class DSEmbeddedPostgresDatasourceConfiguration {
	/**
	 * Base configuration properties prefix for the embedded Postgres datasource.
	 */
	public static final String DATASERVICES_EMBEDDED_POSTGRES_DATASOURCE_PROPERTY_BASE = "spring.datasources.dataservices.embedded-postgres";
	private EmbeddedPostgres embeddedPostgres;

	/**
	 * Binds the embedded Postgres properties for DataServices.
	 *
	 * @return The bound {@link EmbeddedPostgresProperties}.
	 */
	@ExposePropertiesToActuator
	@ConfigurationProperties(DATASERVICES_EMBEDDED_POSTGRES_DATASOURCE_PROPERTY_BASE)
	@Bean public EmbeddedPostgresProperties dsEmbeddedPostgresProperties() {
		return new EmbeddedPostgresProperties();
	}

	/**
	 * Creates and starts the embedded Postgres instance and exposes its {@link DataSource}.
	 *
	 * @param dsEmbeddedPostgresProperties Bound properties controlling embedded Postgres behavior.
	 * @param resourceLoader Spring resource loader used to resolve and create the data directory.
	 * @return The primary {@link DataSource} backed by the embedded Postgres.
	 * @throws IOException If the data directory cannot be resolved or created.
	 */
	@Primary @Bean public DataSource dsDataSource(EmbeddedPostgresProperties dsEmbeddedPostgresProperties, ResourceLoader resourceLoader) throws IOException {
		if (embeddedPostgres == null) {
			EmbeddedPostgres.Builder postgresBuilder = EmbeddedPostgres.builder();
			Optional.ofNullable(dsEmbeddedPostgresProperties.getPath()).ifPresent(path -> {
				try {
					File dataDirectory = resourceLoader.getResource(dsEmbeddedPostgresProperties.getPath()).getFile();
					if (!dataDirectory.exists()) {
						dataDirectory.mkdirs();
					}
					postgresBuilder.setDataDirectory(dataDirectory);
				} catch (IOException e) {
					throw new UnexpectedException(e).withAnonymityMessage("Bean creation failed.");
				}
				postgresBuilder.setCleanDataDirectory(dsEmbeddedPostgresProperties.isCleanDataDirectory());
			});
			dsEmbeddedPostgresProperties.getConnectConfig().forEach(postgresBuilder::setConnectConfig);
			postgresBuilder.setLocaleConfig(EmbeddedPostgresProperties.LC_CTYPE, dsEmbeddedPostgresProperties.getLocaleCType());
			dsEmbeddedPostgresProperties.getPostgresConfig().forEach(postgresBuilder::setServerConfig);
			postgresBuilder.setPort(dsEmbeddedPostgresProperties.getPort());

			if (dsEmbeddedPostgresProperties.getOverrideWorkingDirectory() != null) {
				postgresBuilder.setOverrideWorkingDirectory(dsEmbeddedPostgresProperties.getOverrideWorkingDirectory());
			}

			embeddedPostgres = postgresBuilder.start();
		}
		return embeddedPostgres.getPostgresDatabase();
	}

	/**
	 * Shuts down the embedded Postgres instance if it has been started.
	 *
	 * @throws com.mgmtp.a12.dataservices.common.exception.UnexpectedException If closing the instance fails.
	 */
	@SneakyThrows @PreDestroy public void shutdown() {
		Optional.ofNullable(embeddedPostgres)
			.ifPresent(ep -> {
				try {
					ep.close();
				} catch (IOException e) {
					throw new UnexpectedException(e).withAnonymityMessage("Shutdown failed.");
				}
			});
	}
}
