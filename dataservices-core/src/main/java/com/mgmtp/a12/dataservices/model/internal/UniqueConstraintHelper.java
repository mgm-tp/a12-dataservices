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

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.utils.internal.ModelUtils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;
import static com.mgmtp.a12.dataservices.model.internal.ModelCacheManager.MODEL_HIERARCHY_CACHE;

/**
 * Service responsible for resolving unique constraint–related model hierarchy information.
 *
 * Specifically, it resolves the topmost model in the inheritance hierarchy that still defines
 * a given uniqueness constraint. This is constraint-domain logic, not generic model-graph logic,
 * which is why it lives here rather than in `ModelTypeService`.
 *
 * The parent map is optionally cached using the shared `MODEL_HIERARCHY_CACHE` cache, controlled
 * by `DataServicesCoreProperties.cache.modelGraph.enabled`.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UniqueConstraintHelper {

	private static final String PARENT_MAP_CACHE_KEY = "parentMap";

	private final ModelService modelService;
	private final IModelLoader<IDocumentModel> documentModelLoader;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final Optional<CacheManager> cacheManager;

	/**
	 * Returns the topmost model in the inheritance hierarchy that still defines the given
	 * uniqueness constraint.
	 *
	 * Traverses the parent chain upward from `documentModelName`. At each step, checks whether
	 * the parent model also defines the named constraint. Stops when the parent does not define
	 * the constraint or no further parent exists. Returns the starting model if no ancestor
	 * defines the constraint.
	 *
	 * @param documentModelName the model to start traversal from.
	 * @param constraintName    the name of the uniqueness constraint to look up.
	 * @return the topmost ancestor that defines `constraintName`, or `documentModelName` itself
	 *         if no ancestor defines it.
	 */
	public String findTopmostModelName(String documentModelName, String constraintName) {
		StopWatch stopWatch = StopWatch.createStarted();
		Map<String, String> parentMap = cacheManager
			.filter(x -> dataServicesCoreProperties.getCache().getModelGraph().isEnabled())
			.map(cm -> cm.getCache(MODEL_HIERARCHY_CACHE))
			.map(c -> c.get(PARENT_MAP_CACHE_KEY, this::loadParentMap))
			.orElseGet(this::loadParentMap);

		String topmost = documentModelName;
		String current = documentModelName;
		while (parentMap.containsKey(current)) {
			String parent = parentMap.get(current);
			if (modelDefinesConstraint(parent, constraintName)) {
				topmost = parent;
				current = parent;
			} else {
				break;
			}
		}
		log.debug("Topmost model for constraint [{}] of [{}] resolved to [{}] in [{}] ms",
			constraintName, documentModelName, topmost, stopWatch.getTime());
		return topmost;
	}

	/**
	 * Builds a map from child model name to direct parent model name (one level only).
	 * Used to traverse the inheritance hierarchy upward during topmost model resolution.
	 *
	 * @return map of child model name to direct parent model name.
	 */
	private Map<String, String> loadParentMap() {
		return ModelUtils.computeModelHeterogeneity(modelService.findAllHeadersByType(DOCUMENT_MODEL_TYPE))
			.entrySet().stream()
			.flatMap(entry -> entry.getValue().getDirectSubtypes().stream()
				.map(child -> Map.entry(child.getModelName(), entry.getKey())))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Checks whether the given model defines a uniqueness criterion with the given name.
	 *
	 * @param modelName      the model to inspect.
	 * @param constraintName the constraint name to look for.
	 * @return `true` if the model defines the named constraint; `false` otherwise.
	 */
	private boolean modelDefinesConstraint(String modelName, String constraintName) {
		return documentModelLoader.loadModel(modelName).getContent()
			.getDocumentUniquenessCriteria().stream()
			.anyMatch(c -> constraintName.equals(c.getName()));
	}
}
