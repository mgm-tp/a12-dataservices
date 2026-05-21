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

import java.util.List;
import java.util.Map;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.rpc.internal.jpa.InsecureModelCacheResolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component public class ModelCacheManager {

	private final InsecureModelCacheResolver coreCacheResolver;

	public static final String INSECURE_MODEL_CACHE_RESOLVER = "insecureModelCacheResolver";
	public static final String VALIDATION_CACHE_CACHE = "validationCache";
	public static final String SECURED_MODEL_READ_CACHE = "securedModelReadCache";
	public static final String GENERIC_MODEL_READ_CACHE = "com.mgmtp.a12.dataservices.model.GenericModel";
	public static final String DOCUMENT_MODEL_READ_CACHE = "com.mgmtp.a12.kernel.md.model.api.IDocumentModel";
	public static final String RELATIONSHIP_MODEL_READ_CACHE = "com.mgmtp.a12.dataservices.relationship.model.RelationshipModel";
	public static final String COMPOSE_DOCUMENT_MODEL_READ_CACHE = "com.mgmtp.a12.dataservices.cdd.jms.internal.ComposeDocumentModel";
	public static final String MODEL_GRAPH_CACHE = "modelGraphCache";
	public static final String MODEL_HIERARCHY_CACHE = "modelSubTypesMapCache";
	public static final String UNSECURED_MODEL_READ_CACHE = "unsecuredModelReadCache";
	public static final String MODEL_INDEXED_FIELDS_CACHE = "documentModelIndexedFieldsCache";
	public static final String MODEL_IS_INDEXED_FIELD_CACHE = "documentModelIsIndexedFieldCache";
	private final CacheManager cacheManager;


	@CacheEvict(value = { MODEL_GRAPH_CACHE, MODEL_HIERARCHY_CACHE }, allEntries = true)
	public void invalidateModelGraphCaches() {
		log.debug("{} and {} caches has been evicted for Model Graph", MODEL_GRAPH_CACHE, MODEL_HIERARCHY_CACHE);
	}

	@CacheEvict(value = VALIDATION_CACHE_CACHE, key = "#documentModelName")
	public void invalidateValidationCodeCacheForDocumentModel(String documentModelName) {
		log.debug("Cache [{}] has been evicted for document model [{}]", VALIDATION_CACHE_CACHE, documentModelName);
	}

	public void invalidateSecuredModelReadCaches(String documentModelName) {
		Cache cache = cacheManager.getCache(SECURED_MODEL_READ_CACHE);
		if (cache == null) {
			throw new IllegalStateException("Cache: " + SECURED_MODEL_READ_CACHE + " doesn't exist");
		}

		if (cache.getNativeCache() instanceof Map) {
			Map<List<String>, Boolean> nativeCache = (Map<List<String>, Boolean>) cache.getNativeCache();
			nativeCache.forEach((key, value) -> {
				if (key.contains(documentModelName)) {
					nativeCache.remove(key);
				}
			});
		}

		log.debug("Cache [{}] for document model name [{}] have been evicted.", SECURED_MODEL_READ_CACHE, documentModelName);
	}

	@CacheEvict(value = { GENERIC_MODEL_READ_CACHE, DOCUMENT_MODEL_READ_CACHE, RELATIONSHIP_MODEL_READ_CACHE,
		COMPOSE_DOCUMENT_MODEL_READ_CACHE, MODEL_INDEXED_FIELDS_CACHE, MODEL_IS_INDEXED_FIELD_CACHE }, key = "#modelName")
	public void invalidateModelReadCaches(String modelName) {
		log.debug("Caches [{}, {}, {}, {}, {}, {}] have been evicted for model [{}]", GENERIC_MODEL_READ_CACHE, DOCUMENT_MODEL_READ_CACHE,
			RELATIONSHIP_MODEL_READ_CACHE, COMPOSE_DOCUMENT_MODEL_READ_CACHE, MODEL_INDEXED_FIELDS_CACHE, MODEL_IS_INDEXED_FIELD_CACHE, modelName);
	}

	public void invalidateUnsecuredModelReadCaches() {
		coreCacheResolver.evictCache(UNSECURED_MODEL_READ_CACHE);
		log.debug("Cache [{}] have been evicted.", UNSECURED_MODEL_READ_CACHE);
	}
}
