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
package com.mgmtp.a12.dataservices.client.enumeration.rest;

import java.util.Arrays;
import java.util.List;

import com.mgmtp.a12.connector.rest.RestGetConnector;
import com.mgmtp.a12.connector.rest.RestServerRequest;
import com.mgmtp.a12.connector.rest.UrlBuilderSupport;
import com.mgmtp.a12.dataservices.api.enumeration.rest.ExternalEnumResource;
import com.mgmtp.a12.dataservices.client.enumeration.EnumerationClient;
import com.mgmtp.a12.dataservices.enumeration.ExternalEnumeration;

import lombok.NonNull;

/**
 * REST implementation of {@link EnumerationClient} which uses HTTP client with Basic Auth to issue HTTP request to server.
 */
public class RestEnumerationClient implements EnumerationClient {

	private static final String[] CONTEXT = {"enum","ext"};
	private final RestGetConnector getConnector;
	private final UrlBuilderSupport urlBuilderSupport;

	/**
	 * Creates a REST-based enumeration client for external enumerations.
	 *
	 * @param baseUrl the server base URL; must not be `null`.
	 * @param getConnector the GET connector used to query enumerations; must not be `null`.
	 */
	public RestEnumerationClient(@NonNull String baseUrl, @NonNull RestGetConnector getConnector) {
		this.getConnector = getConnector;
		urlBuilderSupport = UrlBuilderSupport.withBaseUrl(baseUrl, CONTEXT);
	}

	/**
	 * Loads external enumerations associated with a given model.
	 *
	 * @param modelName the name of the model whose external enumerations to retrieve; must not be `null`.
	 * @return a list of external enumerations, possibly empty if none exist.
	 */
	@Override public List<ExternalEnumeration> loadExternalEnumerationForModel(final String modelName) {
		String url = urlBuilderSupport.createBuilder().pathSegment(modelName).toUriString();
		ExternalEnumResource[] enumerations = getConnector.callServer(url, RestServerRequest.empty(), ExternalEnumResource[].class).getData();

		return Arrays.stream(enumerations)
			.map(resource -> new ExternalEnumeration(resource.getId(), resource.getName(), resource.getDescription()))
			.toList();
	}
}
