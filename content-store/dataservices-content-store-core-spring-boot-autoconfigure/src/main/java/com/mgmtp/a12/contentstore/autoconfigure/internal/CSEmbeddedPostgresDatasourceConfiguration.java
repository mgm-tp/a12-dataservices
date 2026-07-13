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
package com.mgmtp.a12.contentstore.autoconfigure.internal;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;

import com.mgmtp.a12.contentstore.autoconfigure.internal.validation.condition.OnEnabledEmbeddedPostgresCondition;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.common.properties.EmbeddedPostgresProperties;
import com.mgmtp.a12.dataservices.configuration.ExposePropertiesToActuator;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;

@ConditionalOnProperty(prefix = "spring.datasource", name = "type", matchIfMissing = true)
@Conditional(OnEnabledEmbeddedPostgresCondition.class)
@Configuration public class CSEmbeddedPostgresDatasourceConfiguration {
	public static final String CONTENT_STORE_EMBEDDED_POSTGRES_DATASOURCE_PROPERTY_BASE = "spring.datasources.contentstore.embedded-postgres";
	private EmbeddedPostgres embeddedPostgres;

	@ExposePropertiesToActuator
	@ConfigurationProperties(CONTENT_STORE_EMBEDDED_POSTGRES_DATASOURCE_PROPERTY_BASE)
	@Bean public EmbeddedPostgresProperties csEmbeddedPostgresProperties() {
		return new EmbeddedPostgresProperties();
	}

	@Primary @Bean("csDataSource") public DataSource csDataSource(EmbeddedPostgresProperties csEmbeddedPostgresProperties, ResourceLoader resourceLoader)
		throws IOException {
		if (embeddedPostgres == null) {
			EmbeddedPostgres.Builder postgresBuilder = EmbeddedPostgres.builder();
			Optional.ofNullable(csEmbeddedPostgresProperties.getPath()).ifPresent(path -> {
				try {
					File dataDirectory = resourceLoader.getResource(csEmbeddedPostgresProperties.getPath()).getFile();
					if (!dataDirectory.exists()) {
						dataDirectory.mkdirs();
					}
					postgresBuilder.setDataDirectory(dataDirectory);
				} catch (IOException e) {
					throw new UnexpectedException(e).withAnonymityMessage("Bean creation failed.");
				}
				postgresBuilder.setCleanDataDirectory(csEmbeddedPostgresProperties.isCleanDataDirectory());
			});
			postgresBuilder.setPort(csEmbeddedPostgresProperties.getPort());
			csEmbeddedPostgresProperties.getConnectConfig().forEach(postgresBuilder::setConnectConfig);
			postgresBuilder.setLocaleConfig(EmbeddedPostgresProperties.LC_CTYPE, csEmbeddedPostgresProperties.getLocaleCType());
			postgresBuilder.setLocaleConfig(EmbeddedPostgresProperties.LC_COLLATE, csEmbeddedPostgresProperties.getLocaleCollate());
			csEmbeddedPostgresProperties.getPostgresConfig().forEach(postgresBuilder::setServerConfig);

			if (csEmbeddedPostgresProperties.getOverrideWorkingDirectory() != null) {
				postgresBuilder.setOverrideWorkingDirectory(csEmbeddedPostgresProperties.getOverrideWorkingDirectory());
			}

			postgresBuilder.setPGStartupWait(csEmbeddedPostgresProperties.getPgStartupWait());
			embeddedPostgres = postgresBuilder.start();
		}
		return embeddedPostgres.getPostgresDatabase();
	}

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
