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

import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link InfinispanCacheConfiguration} bean creation and profile-based exclusion.
 */
public class InfinispanCacheConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(InfinispanCacheConfiguration.class));

	@Test(description = "Should create SpringEmbeddedCacheManager bean when no_cache profile is not active")
	public void shouldCreateSpringEmbeddedCacheManagerBean() {
		// Given: no_cache profile is not active (default context runner)

		// When / Then
		contextRunner
			.withPropertyValues("spring.cache.type=infinispan")
			.run(ctx -> assertThat(ctx).hasSingleBean(SpringEmbeddedCacheManager.class));
	}

	@Test(description = "Should not create Infinispan beans when no_cache profile is active")
	public void shouldNotCreateInfinispanBeansWhenNoCacheProfileIsActive() {
		// Given: no_cache profile is active

		// When / Then
		contextRunner
			.withPropertyValues("spring.cache.type=none", "spring.profiles.active=no_cache")
			.run(ctx -> {
				assertThat(ctx).doesNotHaveBean(SpringEmbeddedCacheManager.class);
				assertThat(ctx).doesNotHaveBean(EmbeddedCacheManager.class);
			});
	}
}
