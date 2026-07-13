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

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseDataSource;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScanPackages;
import org.springframework.boot.quartz.autoconfigure.QuartzDataSource;
import org.springframework.boot.quartz.autoconfigure.QuartzDataSourceScriptDatabaseInitializer;
import org.springframework.boot.quartz.autoconfigure.QuartzJdbcProperties;
import org.springframework.boot.quartz.autoconfigure.QuartzTransactionManager;
import org.springframework.boot.quartz.autoconfigure.SchedulerFactoryBeanCustomizer;
import org.springframework.boot.sql.autoconfigure.init.OnDatabaseInitializationCondition;
import org.springframework.boot.sql.init.dependency.DatabaseInitializationDependencyConfigurer;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
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

import com.mgmtp.a12.dataservices.autoconfigure.internal.datasource.RoutingDataSource;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.configuration.internal.validation.condition.DataSourceReplicaRoutingEnabledCondition;
import com.mgmtp.a12.dataservices.internal.DataSourceContextHolder;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.Nullable;
import liquibase.UpdateSummaryEnum;
import liquibase.UpdateSummaryOutputEnum;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.ui.UIServiceEnum;
import lombok.RequiredArgsConstructor;

/**
 * Spring Boot configuration for DataServices repository layer.
 *
 * Wires the primary {@link javax.sql.DataSource}, JPA ({@link org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean}),
 * Liquibase ({@link liquibase.integration.spring.SpringLiquibase}), and Quartz integration
 * for the DataServices module. It respects embedded Postgres settings and defers JPA initialization
 * until Liquibase has applied migrations.
 */
@RequiredArgsConstructor
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = DataServicesCoreProperties.DS_PACKAGE_PREFIX, entityManagerFactoryRef = "dsEntityManagerFactory", transactionManagerRef = "dsTransactionManager")
@Configuration(proxyBeanMethods = false) public class DataServicesRepositoryConfiguration {
	/**
	 * Base configuration properties prefix for the DataServices datasource.
	 */
	public static final String DATASERVICES_DATASOURCE_PROPERTY_BASE = "spring.datasources.dataservices";
	public static final String DATASERVICES_READ_REPLICA_DATASOURCE_PROPERTY_BASE = "spring.datasources.dataservices-read-replica";

	/**
	 * Configures the primary {@link DataSource} for DataServices when embedded Postgres is disabled.
	 */
	@ConditionalOnProperty(prefix = "spring.datasources.dataservices.embedded-postgres", name = "enabled", havingValue = "false", matchIfMissing = true)
	@Configuration(proxyBeanMethods = false) protected static class DataSourceConfiguration {

		/**
		 * Binds DataServices datasource properties.
		 *
		 * @return The bound {@link DataSourceProperties} for the DataServices datasource.
		 */
		@ConfigurationProperties(DATASERVICES_DATASOURCE_PROPERTY_BASE)
		@Primary @Bean("dsDatasourceProperties") public DataSourceProperties dsDatasourceProperties() {
			return new DataSourceProperties();
		}

		/**
		 * Creates the primary {@link HikariDataSource} for DataServices.
		 *
		 * @param dsDatasourceProperties Bound datasource properties; must not be `null`.
		 * @return The configured primary {@link HikariDataSource}.
		 */
		@Bean("dsPrimaryDataSource") public HikariDataSource dsPrimaryDataSource(@Qualifier("dsDatasourceProperties") DataSourceProperties dsDatasourceProperties) {
			HikariDataSource dataSource = dsDatasourceProperties.initializeDataSourceBuilder()
				.type(HikariDataSource.class)
				.build();
			if (StringUtils.isNotBlank(dsDatasourceProperties.getName())) {
				dataSource.setPoolName(dsDatasourceProperties.getName());
			}
			return dataSource;
		}

		/**
		 * Binds DataServices read-replica datasource properties.
		 *
		 * @return The bound {@link DataSourceProperties} for the read-replica datasource.
		 */
		@ConfigurationProperties(DATASERVICES_READ_REPLICA_DATASOURCE_PROPERTY_BASE)
		@Bean("dsReplicaDataSourceProperties") public DataSourceProperties dsReplicaDataSourceProperties() {
			return new DataSourceProperties();
		}

		/**
		 * Creates the read-replica {@link HikariDataSource} for DataServices.
		 * Only created when `spring.datasources.dataservices-read-replica.url` is configured.
		 *
		 * @param replicaDataSourceProperties Bound replica datasource properties; must not be `null`.
		 * @return The configured replica {@link HikariDataSource}.
		 */
		@Conditional(DataSourceReplicaRoutingEnabledCondition.class)
		@Bean("dsReplicaDataSource") public HikariDataSource dsReplicaDataSource(
			@Qualifier("dsReplicaDataSourceProperties") DataSourceProperties replicaDataSourceProperties) {
			return replicaDataSourceProperties.initializeDataSourceBuilder()
				.type(HikariDataSource.class)
				.build();
		}

		/**
		 * Exposes the active {@link DataSource} for DataServices.
		 *
		 * When a read-replica datasource is configured, returns a {@link RoutingDataSource} that routes
		 * read-only transactions to the replica and read-write transactions to the primary.
		 * When no replica is configured, returns the primary datasource directly.
		 *
		 * @param primaryDataSource The primary HikariCP datasource.
		 * @param replicaDataSource Provider for the optional replica datasource.
		 * @return The active {@link DataSource}.
		 */
		@Primary @Bean("dsDataSource") public DataSource dsDataSource(
			@Qualifier("dsPrimaryDataSource") HikariDataSource primaryDataSource,
			@Qualifier("dsReplicaDataSource") ObjectProvider<DataSource> replicaDataSource) {
			DataSource replica = replicaDataSource.getIfAvailable();
			if (replica != null) {
				RoutingDataSource routingDataSource = new RoutingDataSource();
				routingDataSource.setTargetDataSources(Map.of(
					DataSourceContextHolder.DataSourceType.PRIMARY, primaryDataSource,
					DataSourceContextHolder.DataSourceType.REPLICA, replica));
				routingDataSource.setDefaultTargetDataSource(primaryDataSource);
				routingDataSource.afterPropertiesSet();
				return routingDataSource;
			}
			return primaryDataSource;
		}
	}

	/**
	 * Configures JPA for the DataServices module.
	 */
	@Configuration(proxyBeanMethods = false) protected static class JpaConfiguration {
		/**
		 * Binds JPA properties for the DataServices datasource.
		 *
		 * @return The bound {@link JpaProperties}.
		 */
		@ConfigurationProperties(prefix = DATASERVICES_DATASOURCE_PROPERTY_BASE + ".jpa")
		@Primary @Bean("dsJpaProperties") public JpaProperties dsJpaProperties() {
			return new JpaProperties();
		}

		/**
		 * Creates a {@link JpaVendorAdapter} configured from {@link JpaProperties}.
		 *
		 * @param dsJpaProperties Bound JPA properties; must not be `null`.
		 * @return The configured {@link HibernateJpaVendorAdapter}.
		 */
		@Primary @Bean("dsJpaVendorAdapter") public JpaVendorAdapter dsJpaVendorAdapter(@Qualifier("dsJpaProperties") JpaProperties dsJpaProperties) {
			AbstractJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
			adapter.setShowSql(dsJpaProperties.isShowSql());
			if (dsJpaProperties.getDatabase() != null) {
				adapter.setDatabase(dsJpaProperties.getDatabase());
			}
			if (dsJpaProperties.getDatabasePlatform() != null) {
				adapter.setDatabasePlatform(dsJpaProperties.getDatabasePlatform());
			}
			adapter.setGenerateDdl(dsJpaProperties.isGenerateDdl());
			return adapter;
		}

		/**
		 * Creates the {@link EntityManagerFactoryBuilder} for DataServices.
		 *
		 * @param dsJpaVendorAdapter The JPA vendor adapter.
		 * @param persistenceUnitManager Optional persistence unit manager.
		 * @param dsJpaProperties Bound JPA properties.
		 * @return The {@link EntityManagerFactoryBuilder}.
		 */
		@Primary @Bean("dsEntityManagerFactoryBuilder") @DependsOnDatabaseInitialization public EntityManagerFactoryBuilder dsEntityManagerFactoryBuilder(
			@Qualifier("dsJpaVendorAdapter") JpaVendorAdapter dsJpaVendorAdapter,
			ObjectProvider<PersistenceUnitManager> persistenceUnitManager,
			@Qualifier("dsJpaProperties") JpaProperties dsJpaProperties) {

			return new EntityManagerFactoryBuilder(dsJpaVendorAdapter, datasource -> dsJpaProperties.getProperties(), persistenceUnitManager.getIfAvailable());
		}

		/**
		 * Builds the DataServices {@link LocalContainerEntityManagerFactoryBean}.
		 *
		 * Depends on Liquibase to ensure the schema is ready before JPA initialization.
		 *
		 * @param dsDataSource The primary DataServices datasource.
		 * @param dsJpaProperties Bound JPA properties.
		 * @param beanFactory Spring bean factory used to resolve entity scan packages.
		 * @param dsEntityManagerFactoryBuilder The builder to create the entity manager factory.
		 * @return The configured {@link LocalContainerEntityManagerFactoryBean}.
		 */
		@DependsOn("dsLiquibase")
		@Primary @Bean("dsEntityManagerFactory") public LocalContainerEntityManagerFactoryBean dsEntityManagerFactory(
			@Qualifier("dsDataSource") DataSource dsDataSource,
			@Qualifier("dsJpaProperties") JpaProperties dsJpaProperties, BeanFactory beanFactory,
			@Qualifier("dsEntityManagerFactoryBuilder") EntityManagerFactoryBuilder dsEntityManagerFactoryBuilder) {
			return dsEntityManagerFactoryBuilder
				.dataSource(dsDataSource)
				.properties(dsJpaProperties.getProperties())
				.packages(Stream.concat(
						Stream.of(DataServicesCoreProperties.DS_PACKAGE_PREFIX),
						EntityScanPackages.get(beanFactory).getPackageNames().stream())
					.distinct()
					.toArray(String[]::new))
				.persistenceUnit("dsPersistenceUnit")
				.build();
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

	/**
	 * Configures Liquibase for the DataServices datasource.
	 */
	@Configuration(proxyBeanMethods = false) protected static class LiquibaseConfiguration {
		/**
		 * Binds Liquibase properties for the DataServices datasource.
		 *
		 * @return The bound {@link LiquibaseProperties}.
		 */
		@ConfigurationProperties(prefix = DATASERVICES_DATASOURCE_PROPERTY_BASE + ".liquibase")
		@Primary @Bean("dsLiquibaseProperties") public LiquibaseProperties dsLiquibaseProperties() {
			return new LiquibaseProperties();
		}

		/**
		 * Configures the Liquibase migration runner for DataServices.
		 * If a migration-specific DataSource is not provided, the primary DataServices datasource is used.
		 *
		 * @param dsDataSource The DataServices {@link DataSource}; may be `null` if a unique bean is not available.
		 * @param migrationDataSource Optional Liquibase-specific {@link DataSource} to run migrations.
		 * @param dsLiquibaseProperties Bound Liquibase properties.
		 * @return The configured {@link SpringLiquibase} instance.
		 */
		@Primary @Bean("dsLiquibase") public SpringLiquibase dsLiquibase(@Qualifier("dsDataSource") ObjectProvider<DataSource> dsDataSource,
			@LiquibaseDataSource @Qualifier("dsMigrationDataSource") ObjectProvider<DataSource> migrationDataSource,
			@Qualifier("dsLiquibaseProperties") LiquibaseProperties dsLiquibaseProperties) {

			SpringLiquibase liquibase = new SpringLiquibase();
			liquibase.setDataSource(resolveDataSource(dsDataSource.getIfUnique(), migrationDataSource.getIfAvailable(), dsLiquibaseProperties));

			liquibase.setChangeLog(dsLiquibaseProperties.getChangeLog());
			liquibase.setClearCheckSums(dsLiquibaseProperties.isClearChecksums());
			if (!CollectionUtils.isEmpty(dsLiquibaseProperties.getContexts())) {
				liquibase.setContexts(org.springframework.util.StringUtils.collectionToCommaDelimitedString(dsLiquibaseProperties.getContexts()));
			}
			liquibase.setDefaultSchema(dsLiquibaseProperties.getDefaultSchema());
			liquibase.setLiquibaseSchema(dsLiquibaseProperties.getLiquibaseSchema());
			liquibase.setLiquibaseTablespace(dsLiquibaseProperties.getLiquibaseTablespace());
			liquibase.setDatabaseChangeLogTable(dsLiquibaseProperties.getDatabaseChangeLogTable());
			liquibase.setDatabaseChangeLogLockTable(dsLiquibaseProperties.getDatabaseChangeLogLockTable());
			liquibase.setDropFirst(dsLiquibaseProperties.isDropFirst());
			liquibase.setShouldRun(dsLiquibaseProperties.isEnabled());
			if (!CollectionUtils.isEmpty(dsLiquibaseProperties.getLabelFilter())) {
				liquibase.setLabelFilter(org.springframework.util.StringUtils.collectionToCommaDelimitedString(dsLiquibaseProperties.getLabelFilter()));
			}
			liquibase.setChangeLogParameters(dsLiquibaseProperties.getParameters());
			liquibase.setRollbackFile(dsLiquibaseProperties.getRollbackFile());
			liquibase.setTestRollbackOnUpdate(dsLiquibaseProperties.isTestRollbackOnUpdate());
			liquibase.setTag(dsLiquibaseProperties.getTag());
			if (dsLiquibaseProperties.getShowSummary() != null) {
				liquibase.setShowSummary(UpdateSummaryEnum.valueOf(dsLiquibaseProperties.getShowSummary().name()));
			}
			if (dsLiquibaseProperties.getShowSummaryOutput() != null) {
				liquibase.setShowSummaryOutput(UpdateSummaryOutputEnum.valueOf(dsLiquibaseProperties.getShowSummaryOutput().name()));
			}
			if (dsLiquibaseProperties.getUiService() != null) {
				liquibase.setUiService(UIServiceEnum.valueOf(dsLiquibaseProperties.getUiService().name()));
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

	/**
	 * Exposes the primary JPA {@link PlatformTransactionManager} for DataServices.
	 *
	 * @param dsEntityManagerFactory The DataServices entity manager factory bean; must not be `null`.
	 * @return The configured {@link JpaTransactionManager}.
	 */
	@Primary @Bean public PlatformTransactionManager dsTransactionManager(
		@Qualifier("dsEntityManagerFactory") LocalContainerEntityManagerFactoryBean dsEntityManagerFactory) {
		return new JpaTransactionManager(Objects.requireNonNull(dsEntityManagerFactory.getObject()));
	}

	/**
	 * Configures Quartz to use JDBC-backed storage with the DataServices datasource.
	 */
	@ConditionalOnProperty(prefix = "spring.quartz", name = "job-store-type", havingValue = "jdbc")
	@Import(DatabaseInitializationDependencyConfigurer.class)
	@Configuration(proxyBeanMethods = false)
	protected static class QuartzConfiguration {

		/**
		 * Exposes the Quartz {@link DataSource} backed by the DataServices datasource.
		 *
		 * @param dsDataSource The DataServices datasource provider.
		 * @return The Quartz {@link DataSource}; may be `null` if not available.
		 */
		@Primary @Bean @QuartzDataSource public DataSource quartzDataSource(@Qualifier("dsDataSource") ObjectProvider<DataSource> dsDataSource) {
			return dsDataSource.getIfAvailable();
		}

		/**
		 * Exposes the Quartz {@link PlatformTransactionManager} backed by the JPA transaction manager.
		 *
		 * @param dsTransactionManager The primary DataServices transaction manager.
		 * @return The Quartz transaction manager.
		 */
		@Bean @QuartzTransactionManager
		public PlatformTransactionManager quartzTransactionManager(PlatformTransactionManager dsTransactionManager) {
			return dsTransactionManager;
		}

		/**
		 * Customizes the {@link org.springframework.scheduling.quartz.SchedulerFactoryBean} to use
		 * the configured Quartz {@link DataSource} and {@link PlatformTransactionManager}.
		 *
		 * @param quartzDataSource Provider for the Quartz datasource.
		 * @param quartzTransactionManager Provider for the Quartz transaction manager.
		 * @return The customizer to apply to the scheduler factory.
		 */

		@Order(0)
		@ConditionalOnMissingBean(name = "dsQuartzCustomizer")
		@Bean public SchedulerFactoryBeanCustomizer dsQuartzCustomizer(@QuartzDataSource ObjectProvider<DataSource> quartzDataSource,
			@QuartzTransactionManager ObjectProvider<PlatformTransactionManager> quartzTransactionManager) {
			return schedulerFactoryBean -> {
				quartzDataSource.ifAvailable(schedulerFactoryBean::setDataSource);
				quartzTransactionManager.ifAvailable(schedulerFactoryBean::setTransactionManager);
			};
		}

		/**
		 * Initializes Quartz JDBC schema if necessary.
		 *
		 * @param quartzDataSource Provider for the Quartz datasource.
		 * @param properties Quartz properties controlling schema initialization.
		 * @return The database initializer for Quartz.
		 */
		@Bean @ConditionalOnMissingBean(QuartzDataSourceScriptDatabaseInitializer.class) @DependsOnDatabaseInitialization
		@Conditional(OnQuartzDatasourceInitializationCondition.class)
		public QuartzDataSourceScriptDatabaseInitializer quartzDataSourceScriptDatabaseInitializer(
			@QuartzDataSource ObjectProvider<DataSource> quartzDataSource, QuartzJdbcProperties properties) {
			return new QuartzDataSourceScriptDatabaseInitializer(quartzDataSource.getIfAvailable(), properties);
		}

		static class OnQuartzDatasourceInitializationCondition extends OnDatabaseInitializationCondition {

			OnQuartzDatasourceInitializationCondition() {
				super("Quartz", "spring.quartz.jdbc.initialize-schema");
			}

		}
	}

}
