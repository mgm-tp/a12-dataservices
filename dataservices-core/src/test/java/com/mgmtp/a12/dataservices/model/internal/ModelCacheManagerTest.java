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
package com.mgmtp.a12.dataservices.model.internal;

import org.mockito.Mockito;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.mgmtp.a12.dataservices.model.internal.ModelCacheManager.SECURED_MODEL_READ_CACHE;
import static org.testng.Assert.assertThrows;

/**
 * Unit tests for {@link ModelCacheManager#invalidateSecuredModelReadCaches(String)}.
 *
 * These tests verify the provider-independent cache invalidation behavior using
 * `Cache#invalidate()` rather than casting to a provider-specific native cache.
 */
public class ModelCacheManagerTest {

	private static final String TEST_DOCUMENT_MODEL_NAME = "TestDocumentModel";

	private CacheManager cacheManager;
	private Cache cache;
	private DocumentModelSearchServiceCacheResolver cacheResolver;
	private ModelCacheManager modelCacheManager;

	@BeforeMethod
	public void setUp() {
		cacheManager = Mockito.mock(CacheManager.class);
		cache = Mockito.mock(Cache.class);
		cacheResolver = Mockito.mock(DocumentModelSearchServiceCacheResolver.class);
		modelCacheManager = new ModelCacheManager(cacheResolver, cacheManager);
	}

	@Test(description = "Should call cache.invalidate() when invalidating secured model read caches")
	public void shouldCallCacheInvalidateWhenInvalidatingSecuredModelReadCaches() {
		// Given
		Mockito.when(cacheManager.getCache(SECURED_MODEL_READ_CACHE)).thenReturn(cache);

		// When
		modelCacheManager.invalidateSecuredModelReadCaches(TEST_DOCUMENT_MODEL_NAME);

		// Then
		Mockito.verify(cache).invalidate();
	}

	@Test(description = "Should never call cache.getNativeCache() when invalidating secured model read caches")
	public void shouldNotCallGetNativeCacheWhenInvalidatingSecuredModelReadCaches() {
		// Given
		Mockito.when(cacheManager.getCache(SECURED_MODEL_READ_CACHE)).thenReturn(cache);

		// When
		modelCacheManager.invalidateSecuredModelReadCaches(TEST_DOCUMENT_MODEL_NAME);

		// Then
		Mockito.verify(cache, Mockito.never()).getNativeCache();
	}

	@Test(description = "Should throw IllegalStateException when secured model read cache is not found")
	public void shouldThrowIllegalStateExceptionWhenSecuredModelReadCacheNotFound() {
		// Given
		Mockito.when(cacheManager.getCache(SECURED_MODEL_READ_CACHE)).thenReturn(null);

		// When / Then
		assertThrows(IllegalStateException.class, () -> modelCacheManager.invalidateSecuredModelReadCaches(TEST_DOCUMENT_MODEL_NAME));
	}
}
