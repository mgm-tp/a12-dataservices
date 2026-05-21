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
package com.mgmtp.a12.dataservices.server.internal.rest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mgmtp.a12.dataservices.api.common.rest.NoCache;
import com.mgmtp.a12.dataservices.authorization.internal.UaaConnector;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.relationship.ModelGraphRoot;
import com.mgmtp.a12.dataservices.relationship.internal.ModelGraphService;
import com.mgmtp.a12.dataservices.server.uaa.SecuredController;

import lombok.RequiredArgsConstructor;

/**
 * Endpoint to receive a model graph.
 *
 * @topic Relationships
 * @title Relationship REST API
 */
@RequestMapping("#{@dataServicesCoreProperties.server.contextPath}/")
@RequiredArgsConstructor
@SecuredController
@RestController public class RelationshipControllerImpl {

	public static final String MODELGRAPH_PATH = "modelgraph";
	private final ModelGraphService modelGraphService;
	private final DataServicesCoreProperties dataServicesCoreProperties;

	/**
	 * Get a model graph containing document models, CDMs, and relationship models.
	 *
	 * @return The ModelGraphRoot.
	 * @title Get Model Graph
	 * @authorizationScope Model Read
	 * @responseSuccess 200 OK:: The response contains the ModelGraphRoot object.
	 */
	@NoCache
	@GetMapping(path = MODELGRAPH_PATH, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ModelGraphRoot getModelGraph() {
		if (dataServicesCoreProperties.getCache().getModelGraph().isEnabled()) {
			return modelGraphService.constructModelGraph(UaaConnector.getCurrentUserName());
		} else {
			return modelGraphService.constructModelGraph();
		}
	}
}
