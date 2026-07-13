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

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.commons.api.BasicCache;
import org.infinispan.commons.marshall.JavaSerializationMarshaller;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.context.Flag;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.mgmtp.a12.dataservices.model.internal.ModelCacheManager.SECURED_MODEL_READ_CACHE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for cross-pod Infinispan cache eviction propagation via JGroups MPING loopback.
 *
 * Creates two `EmbeddedCacheManager` instances that join the same JGroups cluster using
 * `jgroups-test.xml` (MPING on 127.0.0.1 with a shared multicast address). Verifies that
 * a cache invalidation triggered on node A is propagated to node B via JGroups INVALIDATE messages.
 *
 * Uses `infinispan-cluster-test.xml` which references `jgroups-test.xml` via
 * a `stack-file` element. All caches are declared as `invalidation-cache` to mirror
 * the production configuration.
 *
 * Cache warm-up uses `Flag.CACHE_MODE_LOCAL` to insert entries into each node's local
 * store without triggering cluster-wide INVALIDATE messages. This simulates the production scenario
 * where each pod independently loads data from the database and populates its local cache.
 * Without this flag, an INVALIDATION-mode `put()` on node B would immediately evict node A's
 * copy, making the pre-condition assertion trivially false.
 *
 */
public class InfinispanClusterEvictionIT {

	private static final String CLUSTER_CONFIG = "infinispan-cluster-test.xml";
	private static final String TEST_KEY = "eviction-test-key";
	private static final String TEST_VALUE = "eviction-test-value";

	private EmbeddedCacheManager nodeA;
	private EmbeddedCacheManager nodeB;

	@BeforeMethod
	public void startClusterNodes() throws IOException {
		nodeA = createCacheManager();
		nodeB = createCacheManager();
	}

	@AfterMethod
	public void stopClusterNodes() {
		if (nodeA != null) {
			nodeA.stop();
		}
		if (nodeB != null) {
			nodeB.stop();
		}
	}

	@Test(description = "Should propagate cache eviction from node A to node B: "
		+ "start two application contexts with jgroups-test.xml (MPING loopback), "
		+ "warm up securedModelReadCache on both nodes, invalidate on node A, "
		+ "and verify node B cache entry is null")
	public void shouldPropagateEvictionFromNodeAToNodeB() {
		// Given: both nodes have joined the cluster (MPING loopback)
		SpringEmbeddedCacheManager cacheManagerA = new SpringEmbeddedCacheManager(nodeA);
		SpringEmbeddedCacheManager cacheManagerB = new SpringEmbeddedCacheManager(nodeB);

		org.springframework.cache.Cache springCacheOnA = cacheManagerA.getCache(SECURED_MODEL_READ_CACHE);
		org.springframework.cache.Cache springCacheOnB = cacheManagerB.getCache(SECURED_MODEL_READ_CACHE);

		assertThat(springCacheOnA).as("securedModelReadCache must be available on node A").isNotNull();
		assertThat(springCacheOnB).as("securedModelReadCache must be available on node B").isNotNull();

		// Obtain the underlying Infinispan AdvancedCache instances so we can use CACHE_MODE_LOCAL for warm-up.
		// In production each pod independently loads from the DB — this simulates that without
		// triggering cross-node INVALIDATE messages that would immediately evict the other pod's entry.
		AdvancedCache<Object, Object> infinispanCacheOnA = getInfinispanAdvancedCache(springCacheOnA);
		AdvancedCache<Object, Object> infinispanCacheOnB = getInfinispanAdvancedCache(springCacheOnB);

		// Warm up: insert locally on each node without broadcasting an INVALIDATE
		infinispanCacheOnA.withFlags(Flag.CACHE_MODE_LOCAL).put(TEST_KEY, TEST_VALUE);
		infinispanCacheOnB.withFlags(Flag.CACHE_MODE_LOCAL).put(TEST_KEY, TEST_VALUE);

		assertThat(springCacheOnA.get(TEST_KEY))
			.as("Node A cache must contain the entry before invalidation")
			.isNotNull();
		assertThat(springCacheOnB.get(TEST_KEY))
			.as("Node B cache must contain the entry before invalidation")
			.isNotNull();

		// When: invalidate the entire cache on node A.
		// Infinispan INVALIDATION mode sends JGroups INVALIDATE messages synchronously to all cluster members.
		springCacheOnA.invalidate();

		// Then: node A entry is removed locally
		assertThat(springCacheOnA.get(TEST_KEY))
			.as("Node A cache entry must be null after cache.invalidate()")
			.isNull();

		// Then: INVALIDATE message has propagated to node B — entry is also evicted there
		assertThat(springCacheOnB.get(TEST_KEY))
			.as("Node B cache entry must be null after node A cache.invalidate() propagation")
			.isNull();
	}

	@SuppressWarnings("unchecked")
	private static AdvancedCache<Object, Object> getInfinispanAdvancedCache(org.springframework.cache.Cache springCache) {
		Object nativeCache = springCache.getNativeCache();
		assertThat(nativeCache).isInstanceOf(BasicCache.class);
		return ((Cache<Object, Object>) nativeCache).getAdvancedCache();
	}

	private static EmbeddedCacheManager createCacheManager() throws IOException {
		ConfigurationBuilderHolder holder = new ParserRegistry().parseFile(CLUSTER_CONFIG);
		GlobalConfigurationBuilder globalBuilder = holder.getGlobalConfigurationBuilder();
		if (globalBuilder.serialization().getMarshaller() == null) {
			globalBuilder.serialization().marshaller(new JavaSerializationMarshaller());
		}
		globalBuilder.serialization().allowList().addRegexp("java.util.*");
		globalBuilder.serialization().allowList().addRegexp("java.lang.*");
		DefaultCacheManager manager = new DefaultCacheManager(holder, false);
		manager.start();

		new ParserRegistry().parseFile("infinispan-caches.xml")
			.getNamedConfigurationBuilders()
			.forEach((name, builder) -> {
				builder.clustering().cacheMode(CacheMode.INVALIDATION_SYNC);
				manager.defineConfiguration(name, builder.build());
			});

		return manager;
	}
}
