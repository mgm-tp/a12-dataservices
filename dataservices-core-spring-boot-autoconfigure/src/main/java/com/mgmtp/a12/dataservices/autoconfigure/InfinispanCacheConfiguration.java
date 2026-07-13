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

import java.io.IOException;

import org.infinispan.commons.marshall.JavaSerializationMarshaller;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.infinispan.spring.starter.embedded.InfinispanEmbeddedAutoConfiguration;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * Infinispan cache configuration for DataServices.
 *
 * Activates the Infinispan embedded cache manager when `spring.cache.type=infinispan`.
 * In a full Spring Boot application context the Infinispan starter's
 * {@link InfinispanEmbeddedAutoConfiguration} creates the {@link EmbeddedCacheManager} from
 * the deployment XML (`infinispan.xml`, configured via `infinispan.embedded.configXml`).
 * This class runs after that auto-configuration, applies cache definitions from the appropriate
 * caches XML, and exposes a {@link SpringEmbeddedCacheManager} bean named `cacheManager`
 * that integrates Infinispan with Spring Cache.
 *
 * Cache definitions (names, TTL, maxCount) are kept separate from the deployment
 * configuration in `infinispan-caches.xml`. The cache mode is determined
 * programmatically at startup:
 *
 * - clustered manager (`isClustered() == true`): `INVALIDATION_SYNC` —
 * each pod stores data locally; JGroups propagates invalidation signals on eviction.
 * - local manager (`isClustered() == false`): `LOCAL` — in-process only,
 * no JGroups. Used in integration tests that run without network transport.
 *
 * When only this class is loaded (e.g. in unit tests with `ApplicationContextRunner`)
 * and no {@link EmbeddedCacheManager} is present, an in-memory {@link DefaultCacheManager} is
 * created as a fallback via the {@link #infinispanEmbeddedCacheManager} bean.
 *
 * When the `no_cache` profile is active, `application-dataservices-no_cache.properties`
 * sets `spring.cache.type=none`, which disables this configuration class entirely.
 * No Infinispan beans are created in that case.
 *
 * @see SpringEmbeddedCacheManager
 * @see InfinispanEmbeddedAutoConfiguration
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "infinispan")
@AutoConfigureAfter(InfinispanEmbeddedAutoConfiguration.class)
public class InfinispanCacheConfiguration {

	/**
	 * Creates a fallback {@link EmbeddedCacheManager} when none is provided by the Infinispan
	 * Spring Boot starter. This bean is only created when no {@link EmbeddedCacheManager} is
	 * already present in the context (e.g. in unit tests that load only this configuration class).
	 *
	 * If `infinispan.embedded.configXml` is set to a non-empty value, the manager is
	 * bootstrapped from that XML file (resolved as a classpath resource). Otherwise, an
	 * in-memory manager with default global configuration is used.
	 *
	 * @param configXml the classpath location of `infinispan.xml`, empty string if not set
	 * @return the started embedded cache manager (stopped on context close via `destroyMethod`)
	 * @throws IOException if the XML file cannot be read
	 */
	@Bean(destroyMethod = "stop")
	@ConditionalOnMissingBean(EmbeddedCacheManager.class)
	public EmbeddedCacheManager infinispanEmbeddedCacheManager(@Value("${infinispan.embedded.configXml:}") String configXml) throws IOException {
		return isCmlConfigAvailable(configXml)
			? prepareXmlConfigCacheManager(configXml)
			: prepareDefaultConfigCacheManager();
	}

	private static boolean isCmlConfigAvailable(String configXml) {
		return configXml != null && !configXml.isEmpty();
	}

	private static @NonNull DefaultCacheManager prepareXmlConfigCacheManager(String configXml) throws IOException {
		ConfigurationBuilderHolder holder = new ParserRegistry().parseFile(configXml);
		GlobalConfigurationBuilder globalConfigurationBuilder = holder.getGlobalConfigurationBuilder();
		if (globalConfigurationBuilder.serialization().getMarshaller() == null) {
			globalConfigurationBuilder.serialization().marshaller(new JavaSerializationMarshaller());
		}
		allowRequiredSerializationClasses(globalConfigurationBuilder);
		log.debug("Infinispan EmbeddedCacheManager created from config: {}", configXml);
		return new DefaultCacheManager(holder);
	}

	private static @NonNull DefaultCacheManager prepareDefaultConfigCacheManager() {
		GlobalConfigurationBuilder globalConfigurationBuilder = new GlobalConfigurationBuilder();
		globalConfigurationBuilder.serialization().marshaller(new JavaSerializationMarshaller());
		allowRequiredSerializationClasses(globalConfigurationBuilder);
		log.debug("Infinispan EmbeddedCacheManager created with default (in-memory) configuration");
		return new DefaultCacheManager(globalConfigurationBuilder.build());
	}

	/**
	 * Creates the {@link SpringEmbeddedCacheManager} bean that integrates Infinispan with
	 * Spring Cache. Named `cacheManager` to align with the Spring Cache convention.
	 *
	 * Before wrapping the manager, applies all named cache definitions from
	 * `infinispan-caches.xml` via {@link EmbeddedCacheManager#defineConfiguration}.
	 * The cache mode is set programmatically based on whether the manager has transport:
	 *
	 * - clustered (`isClustered() == true`): `INVALIDATION_SYNC` —
	 * local storage per pod with JGroups invalidation propagation (production)
	 * - local (`isClustered() == false`): `LOCAL` — in-process only,
	 * no JGroups (integration tests without network transport)
	 *
	 * @param embeddedCacheManager the Infinispan embedded cache manager
	 * @return the Spring Cache-compatible cache manager
	 * @throws IOException if `infinispan-caches.xml` cannot be read
	 */
	@Bean("cacheManager")
	public SpringEmbeddedCacheManager cacheManager(EmbeddedCacheManager embeddedCacheManager) throws IOException {
		boolean clustered = embeddedCacheManager.getCacheManagerConfiguration().isClustered();
		CacheMode cacheMode = clustered ? CacheMode.INVALIDATION_SYNC : CacheMode.LOCAL;

		ConfigurationBuilderHolder holder = new ParserRegistry().parseFile("infinispan-caches.xml");
		holder.getNamedConfigurationBuilders().forEach((name, builder) -> {
			builder.clustering().cacheMode(cacheMode);
			embeddedCacheManager.defineConfiguration(name, builder.build());
		});

		log.debug("Infinispan cache definitions applied with mode: {}", cacheMode);
		return new SpringEmbeddedCacheManager(embeddedCacheManager);
	}

	private static void allowRequiredSerializationClasses(GlobalConfigurationBuilder builder) {
		builder.serialization().allowList().addRegexp("java.util.*");
		builder.serialization().allowList().addRegexp("java.lang.*");
	}
}
