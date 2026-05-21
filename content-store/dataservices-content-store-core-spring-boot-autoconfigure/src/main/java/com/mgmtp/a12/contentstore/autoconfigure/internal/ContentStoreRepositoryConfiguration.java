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

import java.util.Objects;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilderCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.mgmtp.a12.contentstore.autoconfigure.internal.validation.condition.OnDisabledEmbeddedPostgresCondition;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.Nullable;
import liquibase.UpdateSummaryEnum;
import liquibase.UpdateSummaryOutputEnum;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.ui.UIServiceEnum;
import lombok.RequiredArgsConstructor;

/**
 * Spring application context configuration for Content Store Spring JPA.
 */
@RequiredArgsConstructor
@EnableTransactionManagement
@Import({ ContentStoreRepositoryConfiguration.LiquibaseConfiguration.class, ContentStoreRepositoryConfiguration.JpaConfiguration.class,
	ContentStoreRepositoryConfiguration.DataSourceConfiguration.class, CSEmbeddedPostgresDatasourceConfiguration.class })
@EnableJpaRepositories(basePackages = "com.mgmtp.a12.contentstore", entityManagerFactoryRef = "contentstoreEntityManagerFactory", transactionManagerRef = "contentstoreTransactionManager")
public class ContentStoreRepositoryConfiguration {

	public static final String CONTENTSTORE_DATASOURCE_PROPERTY_BASE = "spring.datasources.contentstore";
	public static final String CONTENTSTORE_DATASOURCE_HIKARI_PROPERTY_BASE = "spring.datasources.contentstore.hikari";

	protected static class DataSourceConfiguration {

		@ConfigurationProperties(CONTENTSTORE_DATASOURCE_PROPERTY_BASE)
		@Bean public DataSourceProperties contentstoreDatasourceProperties() {
			return new DataSourceProperties();
		}

		@ConditionalOnProperty(prefix = "spring.datasource", name = "type", matchIfMissing = true)
		@ConfigurationProperties(CONTENTSTORE_DATASOURCE_HIKARI_PROPERTY_BASE)
		@Conditional(OnDisabledEmbeddedPostgresCondition.class)
		@Bean DataSource contentstoreDataSource(@Qualifier("contentstoreDatasourceProperties") DataSourceProperties contentstoreDatasourceProperties) {
			HikariDataSource dataSource = contentstoreDatasourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
			if (StringUtils.isNotBlank(contentstoreDatasourceProperties.getName())) {
				dataSource.setPoolName(contentstoreDatasourceProperties.getName());
			}
			return dataSource;
		}
	}

	protected static class JpaConfiguration {

		@ConfigurationProperties(prefix = CONTENTSTORE_DATASOURCE_PROPERTY_BASE + ".jpa")
		@Bean public JpaProperties contentstoreJpaProperties() {
			return new JpaProperties();
		}

		@Bean public JpaVendorAdapter contentstoreJpaVendorAdapter(@Qualifier("contentstoreJpaProperties") JpaProperties contentstoreJpaProperties) {
			AbstractJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
			adapter.setShowSql(contentstoreJpaProperties.isShowSql());
			if (contentstoreJpaProperties.getDatabase() != null) {
				adapter.setDatabase(contentstoreJpaProperties.getDatabase());
			}
			if (contentstoreJpaProperties.getDatabasePlatform() != null) {
				adapter.setDatabasePlatform(contentstoreJpaProperties.getDatabasePlatform());
			}
			adapter.setGenerateDdl(contentstoreJpaProperties.isGenerateDdl());
			return adapter;
		}

		@Bean public EntityManagerFactoryBuilder contentstoreEntityManagerFactoryBuilder(
			@Qualifier("contentstoreJpaVendorAdapter") JpaVendorAdapter contentstoreJpaVendorAdapter,
			ObjectProvider<PersistenceUnitManager> persistenceUnitManager, ObjectProvider<EntityManagerFactoryBuilderCustomizer> customizers,
			@Qualifier("contentstoreJpaProperties") JpaProperties contentstoreJpaProperties) {
			EntityManagerFactoryBuilder builder =
				new EntityManagerFactoryBuilder(contentstoreJpaVendorAdapter, (DataSource datasource) -> contentstoreJpaProperties.getProperties(),
					persistenceUnitManager.getIfAvailable());
			customizers.orderedStream().forEach(customizer -> customizer.customize(builder));
			return builder;
		}

		@DependsOn("contentstoreLiquibase")
		@Bean public LocalContainerEntityManagerFactoryBean contentstoreEntityManagerFactory(@Qualifier("contentstoreDataSource") DataSource contentstoreDataSource,
			@Qualifier("contentstoreJpaProperties") JpaProperties contentstoreJpaProperties,
			@Qualifier("contentstoreEntityManagerFactoryBuilder") EntityManagerFactoryBuilder contentstoreEntityManagerFactoryBuilder) {
			return contentstoreEntityManagerFactoryBuilder
				.dataSource(contentstoreDataSource)
				.properties(contentstoreJpaProperties.getProperties())
				.packages("com.mgmtp.a12.contentstore")
				.persistenceUnit("contentstorePersistenceUnit")
				.build();
		}

		@Order(0)
		@Bean public JpaProperties jpaProperties(JpaProperties contentstoreJpaProperties) {
			return contentstoreJpaProperties;
		}
	}

	/*
	 * ===== BEGIN THIRD-PARTY SOURCE: [spring-boot] (https://github.com/spring-projects/spring-boot),
	 * Licensed under the [Apache-2.0] License.
	 *
	 * Copyright 2012-2023 the original author or authors.
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 *
	 *      https://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 *
	 * Modified by mgm technology partners on [2025-01-14].
	 */
	protected static class LiquibaseConfiguration {

		@ConfigurationProperties(prefix = CONTENTSTORE_DATASOURCE_PROPERTY_BASE + ".liquibase")
		@Bean public LiquibaseProperties contentstoreLiquibaseProperties() {
			return new LiquibaseProperties();
		}

		@Bean public SpringLiquibase contentstoreLiquibase(@Qualifier("contentstoreDataSource") ObjectProvider<DataSource> contentstoreDataSource,
			@LiquibaseDataSource @Qualifier("contentstoreMigrationDataSource") ObjectProvider<DataSource> migrationDataSource,
			@Qualifier("contentstoreLiquibaseProperties") LiquibaseProperties contentstoreLiquibaseProperties) {

			SpringLiquibase liquibase = new SpringLiquibase();
			liquibase.setDataSource(resolveDataSource(contentstoreDataSource.getIfUnique(), migrationDataSource.getIfAvailable(), contentstoreLiquibaseProperties));

			liquibase.setChangeLog(contentstoreLiquibaseProperties.getChangeLog());
			liquibase.setClearCheckSums(contentstoreLiquibaseProperties.isClearChecksums());
			if (!CollectionUtils.isEmpty(contentstoreLiquibaseProperties.getContexts())) {
				liquibase.setContexts(org.springframework.util.StringUtils.collectionToCommaDelimitedString(contentstoreLiquibaseProperties.getContexts()));
			}
			liquibase.setDefaultSchema(contentstoreLiquibaseProperties.getDefaultSchema());
			liquibase.setLiquibaseSchema(contentstoreLiquibaseProperties.getLiquibaseSchema());
			liquibase.setLiquibaseTablespace(contentstoreLiquibaseProperties.getLiquibaseTablespace());
			liquibase.setDatabaseChangeLogTable(contentstoreLiquibaseProperties.getDatabaseChangeLogTable());
			liquibase.setDatabaseChangeLogLockTable(contentstoreLiquibaseProperties.getDatabaseChangeLogLockTable());
			liquibase.setDropFirst(contentstoreLiquibaseProperties.isDropFirst());
			liquibase.setShouldRun(contentstoreLiquibaseProperties.isEnabled());
			if (!CollectionUtils.isEmpty(contentstoreLiquibaseProperties.getLabelFilter())) {
				liquibase.setLabelFilter(org.springframework.util.StringUtils.collectionToCommaDelimitedString(contentstoreLiquibaseProperties.getLabelFilter()));
			}
			liquibase.setChangeLogParameters(contentstoreLiquibaseProperties.getParameters());
			liquibase.setRollbackFile(contentstoreLiquibaseProperties.getRollbackFile());
			liquibase.setTestRollbackOnUpdate(contentstoreLiquibaseProperties.isTestRollbackOnUpdate());
			liquibase.setTag(contentstoreLiquibaseProperties.getTag());
			if (contentstoreLiquibaseProperties.getShowSummary() != null) {
				liquibase.setShowSummary(UpdateSummaryEnum.valueOf(contentstoreLiquibaseProperties.getShowSummary().name()));
			}
			if (contentstoreLiquibaseProperties.getShowSummaryOutput() != null) {
				liquibase.setShowSummaryOutput(UpdateSummaryOutputEnum.valueOf(contentstoreLiquibaseProperties.getShowSummaryOutput().name()));
			}
			if (contentstoreLiquibaseProperties.getUiService() != null) {
				liquibase.setUiService(UIServiceEnum.valueOf(contentstoreLiquibaseProperties.getUiService().name()));
			}
			return liquibase;
		}

		private DataSource resolveDataSource(
			@Nullable DataSource main,
			@Nullable DataSource migration,
			LiquibaseProperties props) {

			if (migration != null) {
				return migration;
			}

			if (props.getUrl() != null) {
				DataSourceBuilder<?> builder = DataSourceBuilder.create();
				builder.url(props.getUrl());
				builder.username(props.getUser());
				builder.password(props.getPassword());
				if (org.springframework.util.StringUtils.hasText(props.getDriverClassName())) {
					builder.driverClassName(props.getDriverClassName());
				}
				return builder.build();
			}

			Assert.state(main != null, "Liquibase requires a DataSource or URL");
			return main;
		}
	}

	// ===== END THIRD-PARTY SOURCE =====

	@Bean public PlatformTransactionManager contentstoreTransactionManager(
		@Qualifier("contentstoreEntityManagerFactory") LocalContainerEntityManagerFactoryBean contentstoreEntityManagerFactory) {
		return new JpaTransactionManager(Objects.requireNonNull(contentstoreEntityManagerFactory.getObject()));
	}
}
