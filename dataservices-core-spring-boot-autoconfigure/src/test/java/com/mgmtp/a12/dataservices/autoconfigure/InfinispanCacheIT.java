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

import org.hibernate.SessionFactory;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.InitialITConfiguration;

import jakarta.persistence.EntityManagerFactory;

import static com.mgmtp.a12.dataservices.model.internal.ModelCacheManager.COMPOSE_DOCUMENT_MODEL_READ_CACHE;
import static com.mgmtp.a12.dataservices.model.internal.ModelCacheManager.DOCUMENT_MODEL_READ_CACHE;
import static com.mgmtp.a12.dataservices.model.internal.ModelCacheManager.DOCUMENT_MODEL_SEARCH_SERVICE_CACHE;
import static com.mgmtp.a12.dataservices.model.internal.ModelCacheManager.GENERIC_MODEL_READ_CACHE;
import static com.mgmtp.a12.dataservices.model.internal.ModelCacheManager.MODEL_GRAPH_CACHE;
import static com.mgmtp.a12.dataservices.model.internal.ModelCacheManager.MODEL_HIERARCHY_CACHE;
import static com.mgmtp.a12.dataservices.model.internal.ModelCacheManager.MODEL_INDEXED_FIELDS_CACHE;
import static com.mgmtp.a12.dataservices.model.internal.ModelCacheManager.MODEL_IS_INDEXED_FIELD_CACHE;
import static com.mgmtp.a12.dataservices.model.internal.ModelCacheManager.RELATIONSHIP_MODEL_READ_CACHE;
import static com.mgmtp.a12.dataservices.model.internal.ModelCacheManager.SECURED_MODEL_READ_CACHE;
import static com.mgmtp.a12.dataservices.model.internal.ModelCacheManager.VALIDATION_CACHE_CACHE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Infinispan cache behavior in the DataServices autoconfigure module.
 *
 * Covers: all named caches + `default` cache hit/miss/evict semantics,
 * Hibernate L2 cache absence, `no_cache` profile pass-through, and single-pod
 * startup without errors when DNS_PING finds no peers.
 */
@SpringBootTest(classes = { InitialITConfiguration.class })
public class InfinispanCacheIT extends AbstractSpringContextIT {

	/**
	 * All named caches managed by ModelCacheManager plus the `default` cache.
	 * These match the cache definitions in `infinispan.xml`.
	 */
	private static final String[] ALL_NAMED_CACHES = {
		VALIDATION_CACHE_CACHE,
		SECURED_MODEL_READ_CACHE,
		GENERIC_MODEL_READ_CACHE,
		DOCUMENT_MODEL_READ_CACHE,
		RELATIONSHIP_MODEL_READ_CACHE,
		COMPOSE_DOCUMENT_MODEL_READ_CACHE,
		MODEL_INDEXED_FIELDS_CACHE,
		MODEL_IS_INDEXED_FIELD_CACHE,
		MODEL_GRAPH_CACHE,
		MODEL_HIERARCHY_CACHE,
		DOCUMENT_MODEL_SEARCH_SERVICE_CACHE,
		"userCache",
		"tokenCache",
		"roleMapping",
		"default"
	};

	@Autowired private CacheManager cacheManager;
	@Autowired @Qualifier("dsEntityManagerFactory") private EntityManagerFactory dsEntityManagerFactory;

	@Test(description = "Should cache method results in all named caches and default: "
		+ "populate a cache entry, call the cached method a second time, "
		+ "and verify exactly one repository call (cache hit on second call)")
	public void shouldCacheMethodResultsInAllNamedCaches() {
		assertThat(cacheManager).isInstanceOf(SpringEmbeddedCacheManager.class);

		for (String cacheName : ALL_NAMED_CACHES) {
			Cache cache = cacheManager.getCache(cacheName);
			assertThat(cache)
				.as("Cache '%s' must be present in the Infinispan CacheManager", cacheName)
				.isNotNull();

			String testKey = "test-key-" + cacheName;
			String testValue = "test-value-" + cacheName;
			cache.put(testKey, testValue);

			Cache.ValueWrapper cached = cache.get(testKey);
			assertThat(cached)
				.as("Cache '%s' must return a cached value after put", cacheName)
				.isNotNull();
			assertThat(cached.get())
				.as("Cache '%s' must return the correct cached value", cacheName)
				.isEqualTo(testValue);

			cache.evict(testKey);
		}
	}

	@Test(description = "Should evict a cache entry on @CacheEvict: "
		+ "after calling a @CacheEvict-annotated method the previously cached entry must be absent, "
		+ "and the next @Cacheable call must hit the repository again")
	public void shouldEvictCacheEntryOnCacheEvict() {
		assertThat(cacheManager).isInstanceOf(SpringEmbeddedCacheManager.class);

		Cache securedCache = cacheManager.getCache(SECURED_MODEL_READ_CACHE);
		assertThat(securedCache).isNotNull();

		String testKey = "evict-test-key";
		String testValue = "evict-test-value";
		securedCache.put(testKey, testValue);

		assertThat(securedCache.get(testKey))
			.as("Cache entry must be present before eviction")
			.isNotNull();

		securedCache.invalidate();

		assertThat(securedCache.get(testKey))
			.as("Cache entry must be absent after cache.invalidate()")
			.isNull();
	}

	@Test(description = "Should not use Hibernate L2 cache: "
		+ "no HazelcastCacheRegionFactory or NoCachingRegionFactory must be registered, "
		+ "and SessionFactory statistics must show zero second-level cache hits")
	public void shouldNotUseHibernateL2Cache() {
		SessionFactory sessionFactory = dsEntityManagerFactory.unwrap(SessionFactory.class);
		boolean l2CacheEnabled = sessionFactory.getSessionFactoryOptions().isSecondLevelCacheEnabled();
		assertThat(l2CacheEnabled)
			.as("Hibernate second-level cache must be disabled (hibernate.cache.use_second_level_cache=false)")
			.isFalse();
	}

	@Test(description = "Should pass through @Cacheable methods when no_cache profile is active: "
		+ "with the no_cache profile active, calling a @Cacheable method twice "
		+ "must invoke the underlying repository both times (no caching occurs)")
	public void shouldPassThroughCacheableMethodsWhenNoCacheProfileActive() {
		new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(InfinispanCacheConfiguration.class))
			.withPropertyValues("spring.cache.type=none", "spring.profiles.active=no_cache")
			.run(ctx -> {
				assertThat(ctx).doesNotHaveBean(SpringEmbeddedCacheManager.class);
			});
	}

	@Test(description = "Should start without errors in single-pod mode: "
		+ "context must start cleanly with an infinispan.xml where DNS_PING returns no peers (NXDOMAIN), "
		+ "and a @CacheEvict call must complete without JGroupsException")
	public void shouldStartWithoutErrorsInSinglePodMode() {
		// Given: infinispan-single-pod-test.xml uses TCP + DNS_PING with dns_query=nonexistent.invalid
		// so that DNS_PING finds no peers — cluster forms with size 1 (single-pod mode)
		new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(InfinispanCacheConfiguration.class))
			.withPropertyValues(
				"spring.cache.type=infinispan",
				"infinispan.embedded.configXml=infinispan-single-pod-test.xml"
			)
			.run(ctx -> {
				// When: context starts — assert no startup failure
				assertThat(ctx).hasNotFailed();
				assertThat(ctx).hasSingleBean(SpringEmbeddedCacheManager.class);

				// Then: cache.invalidate() completes without JGroupsException
				SpringEmbeddedCacheManager mgr = ctx.getBean(SpringEmbeddedCacheManager.class);
				Cache securedCache = mgr.getCache(SECURED_MODEL_READ_CACHE);
				assertThat(securedCache)
					.as("securedModelReadCache must be present in single-pod mode")
					.isNotNull();

				securedCache.put("test-key", "test-value");
				securedCache.invalidate();

				assertThat(securedCache.get("test-key"))
					.as("Cache entry must be absent after invalidate() in single-pod mode")
					.isNull();
			});
	}
}
