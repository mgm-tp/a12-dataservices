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
package com.mgmtp.a12.dataservices.relationship.internal;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelLoader;
import com.mgmtp.a12.dataservices.relationship.ModelGraphRoot;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModelSerializer;
import com.mgmtp.a12.model.Model;

import static com.mgmtp.a12.dataservices.model.internal.ModelCacheManager.MODEL_GRAPH_CACHE;

/**
 * Spring-managed extension of {@link ModelGraphService} that adds caching and transactional behaviour.
 *
 * This class carries no additional logic beyond the annotations — all computation is delegated to the parent.
 * {@link ModelGraphRoot} results for a given username are cached in `MODEL_GRAPH_CACHE`.
 */
@Service public class CachedModelGraphService extends ModelGraphService {

	public CachedModelGraphService(ModelService modelService, RelationshipModelLoader relationshipModelLoader,
		RelationshipModelSerializer relationshipModelSerializer, ModelTypeService modelTypeService,
		Optional<ModelTypeService> modelTypeServiceOpt, ModelPermissionEvaluator<Model> modelPermissionEvaluator) {
		super(modelService, relationshipModelLoader, relationshipModelSerializer, modelTypeService,
			modelTypeServiceOpt, modelPermissionEvaluator);
	}

	@Cacheable(value = MODEL_GRAPH_CACHE, key = "{#username, T(com.mgmtp.a12.dataservices.authorization.internal.UaaConnector).getCurrentUserAuthoritiesAsString()}")
	@Transactional(readOnly = true)
	@Override public ModelGraphRoot constructModelGraph(String username) {
		return super.constructModelGraph(username);
	}

	@Transactional(readOnly = true)
	@Override public ModelGraphRoot constructModelGraph() {
		return super.constructModelGraph();
	}
}
