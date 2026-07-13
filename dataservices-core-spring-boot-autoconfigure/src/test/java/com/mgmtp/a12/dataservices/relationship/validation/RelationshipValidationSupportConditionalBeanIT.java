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
package com.mgmtp.a12.dataservices.relationship.validation;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.document.persistence.internal.AggregatedDocumentRepository;
import com.mgmtp.a12.dataservices.model.internal.DefaultModelTypeService;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests verifying the `@ConditionalOnMissingBean` autoconfiguration behavior for
 * the `RelationshipValidationSupport` bean.
 *
 * These tests use `ApplicationContextRunner` to create lightweight Spring application contexts
 * without requiring a full server or database, making them fast and deterministic.
 */
public class RelationshipValidationSupportConditionalBeanIT {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(RelationshipValidationAutoconfiguration.class))
		.withUserConfiguration(MockDependenciesConfiguration.class);

	@Test(enabled = true, description = "Should create DS default bean when no custom bean is present")
	public void shouldCreateDefaultBeanWhenNoCustomBeanPresent() {
		contextRunner.run(ctx -> assertThat(ctx).hasSingleBean(RelationshipValidationSupport.class));
	}

	@Test(enabled = true, description = "Should use custom bean and not create DS default when custom bean is present")
	public void shouldUseCustomBeanWhenCustomBeanPresent() {
		contextRunner
			.withUserConfiguration(CustomRelationshipValidationConfiguration.class)
			.run(ctx -> {
				assertThat(ctx).hasSingleBean(RelationshipValidationSupport.class);
				assertThat(ctx.getBean(RelationshipValidationSupport.class))
					.isSameAs(ctx.getBean(CustomRelationshipValidationConfiguration.CUSTOM_BEAN_NAME, RelationshipValidationSupport.class));
			});
	}

	/**
	 * Minimal autoconfiguration providing the `RelationshipValidationSupport` bean.
	 *
	 * This configuration mirrors the relevant part of `DataServicesCoreAutoconfiguration`.
	 * The `@ConditionalOnMissingBean` annotation ensures the DS default bean is suppressed
	 * when a custom bean of this type is present in the context.
	 */
	@Configuration
	static class RelationshipValidationAutoconfiguration {

		@ConditionalOnMissingBean(RelationshipValidationSupport.class)
		@Bean
		public RelationshipValidationSupport relationshipValidationSupport(
			IModelLoader<RelationshipModel> modelLoader,
			AggregatedDocumentRepository aggregatedDocumentRepository,
			DefaultModelTypeService modelTypeService
		) {
			return new com.mgmtp.a12.dataservices.relationship.internal.RelationshipValidationSupport(
				modelLoader, aggregatedDocumentRepository, modelTypeService);
		}
	}

	/**
	 * Provides mock implementations of the dependencies required by `RelationshipValidationSupport`.
	 */
	@Configuration
	static class MockDependenciesConfiguration {

		@Bean
		public IModelLoader<RelationshipModel> relationshipModelLoader() {
			return Mockito.mock(IModelLoader.class);
		}

		@Bean
		public AggregatedDocumentRepository aggregatedDocumentRepository() {
			return Mockito.mock(AggregatedDocumentRepository.class);
		}

		@Bean
		public DefaultModelTypeService defaultModelTypeService() {
			return Mockito.mock(DefaultModelTypeService.class);
		}
	}

	/**
	 * Custom configuration simulating a project-specific override of `RelationshipValidationSupport`.
	 *
	 * When this configuration is loaded alongside `RelationshipValidationAutoconfiguration`,
	 * the `@ConditionalOnMissingBean` condition suppresses the DS default bean, leaving only
	 * the custom bean in the context.
	 *
	 * The custom bean here is a Mockito mock representing any custom implementation
	 * (for example, a no-operation implementation used during bulk import).
	 */
	@Configuration
	static class CustomRelationshipValidationConfiguration {

		static final String CUSTOM_BEAN_NAME = "customRelationshipValidationSupport";

		@Bean(CUSTOM_BEAN_NAME)
		public RelationshipValidationSupport customRelationshipValidationSupport() {
			return Mockito.mock(RelationshipValidationSupport.class);
		}
	}
}
