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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mgmtp.a12.dataservices.api.common.rest.NoCache;
import com.mgmtp.a12.dataservices.api.enumeration.rest.ExternalEnumResource;
import com.mgmtp.a12.dataservices.enumeration.external.ExternalEnumeration;
import com.mgmtp.a12.dataservices.enumeration.external.ExternalEnumerationService;
import com.mgmtp.a12.dataservices.server.internal.condition.ExternalEnumerationEndpointEnabledCondition;

import lombok.RequiredArgsConstructor;

/**
 * API to retrieve external enumerations for a model.
 * Java definition of /enum/ext which provides mappings between business specified document names and document ids. There can be only one mapping defined per
 * document model.
 *
 * @title External Enumeration REST API
 * @topic Documents
 */
@RequestMapping("#{@dataServicesCoreProperties.server.contextPath}/enum/ext")
@Conditional(ExternalEnumerationEndpointEnabledCondition.class)
@RequiredArgsConstructor
@RestController public class ExternalEnumerationControllerImpl  {

	public static final String MODEL_NAME_PARAM = "document-model-name";

	private final ExternalEnumerationService enumerationService;

	/**
	 * Loads external enumeration per document model. The external enumeration has to be implemented as for document model via extension point.
	 *
	 * @param modelName Queried Document Model to retrieve External Enumeration.
	 * @return List of defined external enumerations for modelName.
	 * @note HTTP response will have cache-related headers modified. For the cache information see {@link NoCache} its usage.
	 * @title Get External Enumeration for Document Model
	 * @headers Accept:: application/json
	 * @authorizationScope Model Read
	 * @authorizationScope Query
	 * @responseSuccess 200 OK:: Loaded External Enumeration.
	 * @responseError 412 Precondition Failed:: Validation code could not be generated.
	 */
	@NoCache
	@GetMapping(path = "/{" + MODEL_NAME_PARAM + "}", produces = { MediaType.APPLICATION_JSON_VALUE})
	public List<ExternalEnumResource> loadExternalEnumerationForModel(@PathVariable(MODEL_NAME_PARAM) final String modelName) {
		List<ExternalEnumeration> enumerations = enumerationService.loadExternalEnumerationForModel(modelName);
		return convertToEnumResources(enumerations);
	}

	private List<ExternalEnumResource> convertToEnumResources(final List<ExternalEnumeration> enumerations) {
		if (CollectionUtils.isNotEmpty(enumerations)) {
			return enumerations.stream().map(enumeration ->
					new ExternalEnumResource(enumeration.getName(), enumeration.getName(), enumeration.getDescription())
			).toList();
		}
		return new ArrayList<>();
	}
}
