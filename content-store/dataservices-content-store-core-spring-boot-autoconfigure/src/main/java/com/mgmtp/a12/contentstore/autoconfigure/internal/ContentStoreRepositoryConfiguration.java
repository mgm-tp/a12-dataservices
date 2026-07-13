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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.jpa.autoconfigure.EntityManagerFactoryBuilderCustomizer;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseDataSource;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseProperties;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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

import static com.mgmtp.a12.contentstore.ContentStoreApplication.CONTENT_STORE_BASE_PACKAGE;

/**
 * Spring application context configuration for Content Store Spring JPA.
 */
@RequiredArgsConstructor
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = CONTENT_STORE_BASE_PACKAGE, entityManagerFactoryRef = "csEntityManagerFactory", transactionManagerRef = "csTransactionManager")
@Configuration(proxyBeanMethods = false) public class ContentStoreRepositoryConfiguration {

	public static final String CONTENTSTORE_DATASOURCE_PROPERTY_BASE = "spring.datasources.contentstore";

	@ConditionalOnProperty(prefix = "spring.datasources.contentstore.embedded-postgres", name = "enabled", havingValue = "false", matchIfMissing = true)
	@Configuration(proxyBeanMethods = false) protected static class DataSourceConfiguration {

		@ConfigurationProperties(CONTENTSTORE_DATASOURCE_PROPERTY_BASE)
		@Bean("csDataSourceProperties") public DataSourceProperties csDataSourceProperties() {
			return new DataSourceProperties();
		}

		@ConditionalOnProperty(prefix = "spring.datasource", name = "type", matchIfMissing = true)
		@Conditional(OnDisabledEmbeddedPostgresCondition.class)
		@Bean("csDataSource") public DataSource csDataSource(@Qualifier("csDataSourceProperties") DataSourceProperties properties) {
			return properties.initializeDataSourceBuilder()
				.type(HikariDataSource.class)
				.build();
		}
	}

	@Configuration(proxyBeanMethods = false) protected static class JpaConfiguration {

		@Order(0)
		@ConfigurationProperties(prefix = CONTENTSTORE_DATASOURCE_PROPERTY_BASE + ".jpa")
		@Bean(name = "csJpaProperties", defaultCandidate = false) public JpaProperties csJpaProperties() {
			return new JpaProperties();
		}

		@Bean("csJpaVendorAdapter") public JpaVendorAdapter csJpaVendorAdapter(@Qualifier("csJpaProperties") JpaProperties contentstoreJpaProperties) {
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

		@Bean("csEntityManagerFactoryBuilder") @DependsOnDatabaseInitialization public EntityManagerFactoryBuilder csEntityManagerFactoryBuilder(
			@Qualifier("csJpaVendorAdapter") JpaVendorAdapter csJpaVendorAdapter,
			ObjectProvider<PersistenceUnitManager> persistenceUnitManager,
			ObjectProvider<EntityManagerFactoryBuilderCustomizer> customizers,
			@Qualifier("csJpaProperties") JpaProperties contentstoreJpaProperties) {

			EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(csJpaVendorAdapter,
				datasource -> contentstoreJpaProperties.getProperties(), persistenceUnitManager.getIfAvailable());
			customizers.orderedStream().forEach(customizer -> customizer.customize(builder));
			return builder;
		}

		@DependsOn("csLiquibase")
		@Bean("csEntityManagerFactory") public LocalContainerEntityManagerFactoryBean csEntityManagerFactory(
			@Qualifier("csDataSource") DataSource contentstoreDataSource,
			@Qualifier("csJpaProperties") JpaProperties contentstoreJpaProperties,
			@Qualifier("csEntityManagerFactoryBuilder") EntityManagerFactoryBuilder csEntityManagerFactoryBuilder) {
			return csEntityManagerFactoryBuilder
				.dataSource(contentstoreDataSource)
				.properties(contentstoreJpaProperties.getProperties())
				.packages(CONTENT_STORE_BASE_PACKAGE)
				.persistenceUnit("csPersistenceUnit")
				.build();
		}

	}

	/*
	 * ===== BEGIN THIRD-PARTY SOURCE: [spring-boot] (https://github.com/spring-projects/spring-boot),
	 * Licensed under the [Apache-2.0] License.
	 *
	 * Copyright 2012-2025 the original author or authors.
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
	 * Modified by mgm technology partners on [2026-02-10].
	 */
	@Configuration(proxyBeanMethods = false) protected static class LiquibaseConfiguration {

		@ConfigurationProperties(prefix = CONTENTSTORE_DATASOURCE_PROPERTY_BASE + ".liquibase")
		@Bean public LiquibaseProperties csLiquibaseProperties() {
			return new LiquibaseProperties();
		}

		@Bean public SpringLiquibase csLiquibase(@Qualifier("csDataSource") ObjectProvider<DataSource> csDataSource,
			@LiquibaseDataSource @Qualifier("contentstoreMigrationDataSource") ObjectProvider<DataSource> migrationDataSource,
			@Qualifier("csLiquibaseProperties") LiquibaseProperties csLiquibaseProperties) {

			SpringLiquibase liquibase = new SpringLiquibase();
			liquibase.setDataSource(
				resolveDataSource(csDataSource.getIfUnique(), migrationDataSource.getIfAvailable(), csLiquibaseProperties));

			liquibase.setChangeLog(csLiquibaseProperties.getChangeLog());
			liquibase.setClearCheckSums(csLiquibaseProperties.isClearChecksums());
			if (!CollectionUtils.isEmpty(csLiquibaseProperties.getContexts())) {
				liquibase.setContexts(org.springframework.util.StringUtils.collectionToCommaDelimitedString(csLiquibaseProperties.getContexts()));
			}
			liquibase.setDefaultSchema(csLiquibaseProperties.getDefaultSchema());
			liquibase.setLiquibaseSchema(csLiquibaseProperties.getLiquibaseSchema());
			liquibase.setLiquibaseTablespace(csLiquibaseProperties.getLiquibaseTablespace());
			liquibase.setDatabaseChangeLogTable(csLiquibaseProperties.getDatabaseChangeLogTable());
			liquibase.setDatabaseChangeLogLockTable(csLiquibaseProperties.getDatabaseChangeLogLockTable());
			liquibase.setDropFirst(csLiquibaseProperties.isDropFirst());
			liquibase.setShouldRun(csLiquibaseProperties.isEnabled());
			if (!CollectionUtils.isEmpty(csLiquibaseProperties.getLabelFilter())) {
				liquibase.setLabelFilter(
					org.springframework.util.StringUtils.collectionToCommaDelimitedString(csLiquibaseProperties.getLabelFilter()));
			}
			liquibase.setChangeLogParameters(csLiquibaseProperties.getParameters());
			liquibase.setRollbackFile(csLiquibaseProperties.getRollbackFile());
			liquibase.setTestRollbackOnUpdate(csLiquibaseProperties.isTestRollbackOnUpdate());
			liquibase.setTag(csLiquibaseProperties.getTag());
			if (csLiquibaseProperties.getShowSummary() != null) {
				liquibase.setShowSummary(UpdateSummaryEnum.valueOf(csLiquibaseProperties.getShowSummary().name()));
			}
			if (csLiquibaseProperties.getShowSummaryOutput() != null) {
				liquibase.setShowSummaryOutput(UpdateSummaryOutputEnum.valueOf(csLiquibaseProperties.getShowSummaryOutput().name()));
			}
			if (csLiquibaseProperties.getUiService() != null) {
				liquibase.setUiService(UIServiceEnum.valueOf(csLiquibaseProperties.getUiService().name()));
			}
			return liquibase;
		}

		private DataSource resolveDataSource(@Nullable DataSource main, @Nullable DataSource migration, LiquibaseProperties props) {

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

	@Bean public PlatformTransactionManager csTransactionManager(
		@Qualifier("csEntityManagerFactory") LocalContainerEntityManagerFactoryBean csEntityManagerFactory) {
		return new JpaTransactionManager(Objects.requireNonNull(csEntityManagerFactory.getObject()));
	}
}
