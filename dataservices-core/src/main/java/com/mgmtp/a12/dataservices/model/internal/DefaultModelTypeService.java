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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.authorization.internal.UaaConnector;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.internal.DocumentationDiagram;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.utils.internal.ModelUtils;
import com.mgmtp.a12.model.Model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;
import static com.mgmtp.a12.dataservices.model.internal.ModelCacheManager.MODEL_HIERARCHY_CACHE;

/**
 * Model graph calculation is an expensive operation to be executed during run time, therefore, the model graph caches have been introduced. In production
 * environments, change of models is not needed but during modeling time, the model graph needs not to be cached because models need to be changed during runtime.
 * ModelTypeService works as a switch for model graph caching
 */
@DocumentationDiagram
@RequiredArgsConstructor @Slf4j
@Service public class DefaultModelTypeService implements ModelTypeService {
	private final ModelService modelService;
	private final ModelPermissionEvaluator<Model> modelPermissionEvaluator;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final Optional<CacheManager> cacheManager;

	@Override public Set<String> findAllSubtypes(String documentModelName) {
		StopWatch stopWatch = StopWatch.createStarted();
		String cacheKey = UaaConnector.getCurrentUserName() + UaaConnector.getCurrentUserAuthoritiesAsString();

		Set<String> subtypes = cacheManager
			.filter(x -> dataServicesCoreProperties.getCache().getModelGraph().isEnabled())
			.map(cm -> cm.getCache(MODEL_HIERARCHY_CACHE))
			.map(c -> c.get(cacheKey, this::loadSubtypes))
			.or(() -> Optional.of(loadSubtypes()))
			.map(s -> s.get(documentModelName))
			.orElse(Set.of());
		log.debug("All subtypes of document model [{}] have been loaded in [{}] ms", documentModelName, stopWatch.getTime());
		return subtypes;
	}

	@Override public Set<String> findDirectSubtypes(String documentModelName) {
		StopWatch stopWatch = StopWatch.createStarted();
		Set<String> directSubtypes = Optional.of(ModelUtils.computeModelHeterogeneity(modelService.findAllHeadersByType(DOCUMENT_MODEL_TYPE)))
			.map(subTypes -> remapModelsToSubtypes(subTypes, this::getDirectSubtypesWithReadPermission))
			.map(subtypesMap -> subtypesMap.get(documentModelName))
			.orElse(Set.of());
		log.debug("Direct subtypes of model [{}] have been loaded in [{}] ms", documentModelName, stopWatch.getTime());
		return directSubtypes;
	}

	/**
	 * Return deeply nested Document Model names which are subtypes of the queried Document Model and the queried Document Model name.
	 *
	 * @param documentModelName The model to load subtypes of.
	 * @return Set of nested Document Model names which are subtypes of the queried type and the type name by itself too.
	 * @note Document Model from parameter is included in output too.
	 */
	public Set<String> findModelNameAndAllSubtypes(String documentModelName) {
		return SetUtils.union(findAllSubtypes(documentModelName), Set.of(documentModelName));
	}

	public boolean isSubtype(String parentModelName, String testedModelName) {
		return findModelNameAndAllSubtypes(parentModelName).stream().anyMatch(m -> Objects.equals(m, testedModelName));
	}

	private Map<String, Set<String>> loadSubtypes() {
		return remapModelsToSubtypes(ModelUtils.computeModelHeterogeneity(modelService.findAllHeadersByType(DOCUMENT_MODEL_TYPE)),
			this::getAllSubtypesWithReadPermission);
	}

	private Set<String> getDirectSubtypesWithReadPermission(ModelSubtypes value) {
		return value.getDirectSubtypes().stream()
			.map(ModelSubtypes::getModelName)
			.filter(modelPermissionEvaluator::hasModelReadPermission)
			.collect(Collectors.toSet());
	}

	private Set<String> getAllSubtypesWithReadPermission(ModelSubtypes modelInheritanceEntity) {
		return StreamSupport.stream(modelInheritanceEntity.spliterator(), false)
			.map(ModelSubtypes::getModelName)
			.filter(modelPermissionEvaluator::hasModelReadPermission)
			.collect(Collectors.toSet());
	}

	private Map<String, Set<String>> remapModelsToSubtypes(Map<String, ModelSubtypes> modeInheritanceEntity, Function<ModelSubtypes, Set<String>> transformer) {
		return modeInheritanceEntity.entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, m -> transformer.apply(m.getValue())));
	}
}
