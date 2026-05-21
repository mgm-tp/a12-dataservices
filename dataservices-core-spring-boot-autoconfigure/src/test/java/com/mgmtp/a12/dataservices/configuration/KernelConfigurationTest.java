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
package com.mgmtp.a12.dataservices.configuration;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.autoconfigure.kernel.internal.KernelConfiguration;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link KernelConfiguration }
 */
public class KernelConfigurationTest {
	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(KernelConfiguration.class, PropertiesConfig.class));

	@Test
	void shouldHaveDefaultKernelDe_SerializationConfigurationBean() {
		contextRunner.run(ctx-> {
			assertThat(ctx).hasSingleBean(DocumentDeserializationConfig.class);
			assertThat(ctx).hasSingleBean(DocumentSerializationConfig.class);
			DocumentSerializationConfig documentSerializationConfig = ctx.getBean(DocumentSerializationConfig.class);
			DocumentDeserializationConfig documentDeserializationConfig = ctx.getBean(DocumentDeserializationConfig.class);
			DataServicesCoreProperties dataServicesCoreProperties = ctx.getBean(DataServicesCoreProperties.class);

			assertThat(documentSerializationConfig.getFormat()).isEqualTo(DocumentSerializationConfig.Format.JSON);
			assertThat(documentSerializationConfig.isSkipTransientFields()).isEqualTo(!dataServicesCoreProperties.getDocuments().getPersistTransientFields().isEnabled());

			assertThat(documentDeserializationConfig.getFormat()).isEqualTo(DocumentSerializationConfig.Format.JSON);
			assertThat(documentDeserializationConfig.isAddTransientFields()).isEqualTo(dataServicesCoreProperties.getDocuments().getPersistTransientFields().isEnabled());
		});
	}

	@Test
	void shouldReplaceBeanOnMissingBeanCondition() {
		contextRunner.withUserConfiguration(CustomizedKernelDe_SerializationConfiguration.class)
			.run(ctx -> {
				assertThat(ctx).hasSingleBean(DocumentDeserializationConfig.class);
				assertThat(ctx).hasSingleBean(DocumentSerializationConfig.class);
				DocumentSerializationConfig documentSerializationConfig = ctx.getBean(DocumentSerializationConfig.class);
				DocumentDeserializationConfig documentDeserializationConfig = ctx.getBean(DocumentDeserializationConfig.class);

				assertThat(documentSerializationConfig.getFormat()).isEqualTo(DocumentSerializationConfig.Format.JSON);
				assertThat(documentSerializationConfig.isSkipTransientFields()).isFalse();
				assertThat(documentSerializationConfig.isFailIfUnreadableXML()).isTrue();

				assertThat(documentDeserializationConfig.getFormat()).isEqualTo(DocumentSerializationConfig.Format.JSON);
				assertThat(documentDeserializationConfig.isAddTransientFields()).isTrue();
				assertThat(documentDeserializationConfig.isRemoveEmptyFieldsAndGroups()).isFalse();
		});
	}


	@Configuration
	static class PropertiesConfig {
		@Bean DataServicesCoreProperties dataServicesCoreProperties() {
			DataServicesCoreProperties dataServicesCoreProperties = new DataServicesCoreProperties();
			dataServicesCoreProperties.getDocuments().getPersistTransientFields().setEnabled(true);
			return dataServicesCoreProperties;
		}
	}

	@Configuration
	static class CustomizedKernelDe_SerializationConfiguration {
		@Bean public DocumentDeserializationConfig documentJsonDeserializationConfig( ) {
			return DocumentDeserializationConfig.builder()
				.format(DocumentSerializationConfig.Format.JSON)
				.addTransientFields(true)
				.removeEmptyFieldsAndGroups(false)
				.build();
		}

		@Bean public DocumentSerializationConfig documentJsonSerializationConfig( ) {
			return DocumentSerializationConfig.builder()
				.format(DocumentSerializationConfig.Format.JSON)
				.skipTransientFields(false)
				.failIfUnreadableXML(true)
				.build();
		}
	}
}
