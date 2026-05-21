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
package com.mgmtp.a12.dataservices.client.model.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import com.mgmtp.a12.connector.rest.RestDeleteConnector;
import com.mgmtp.a12.connector.rest.RestGetConnector;
import com.mgmtp.a12.connector.rest.RestPostConnector;
import com.mgmtp.a12.connector.rest.RestPutConnector;
import com.mgmtp.a12.connector.rest.RestServerRequest;
import com.mgmtp.a12.connector.rest.UrlBuilderSupport;
import com.mgmtp.a12.dataservices.client.model.ModelsClient;

import lombok.NonNull;

/**
 * REST implementation of {@link ModelsClient} which uses HTTP client with Basic Auth to issue HTTP request to server.
 */
public class RestModelsClient implements ModelsClient {

	private static final String ENDPOINT_VALIDATION_CODE = "validationCode";

	private static final String[] CONTEXT = { "v2", "models" };

	private final RestGetConnector getConnector;
	private final RestPostConnector postConnector;
	private final RestPutConnector putConnector;
	private final RestDeleteConnector deleteConnector;

	private final UrlBuilderSupport urlBuilderSupport;

	/**
	 * Creates a REST-based models client.
	 *
	 * @param baseUrl the server base URL; must not be `null`.
	 * @param getConnector the GET connector used for retrieval operations; must not be `null`.
	 * @param postConnector the POST connector used for creation operations; must not be `null`.
	 * @param putConnector the PUT connector used for update operations; must not be `null`.
	 * @param deleteConnector the DELETE connector used for removal operations; must not be `null`.
	 */
	public RestModelsClient(@NonNull String baseUrl, @NonNull RestGetConnector getConnector, @NonNull RestPostConnector postConnector,
		@NonNull RestPutConnector putConnector, @NonNull RestDeleteConnector deleteConnector) {
		this.getConnector = getConnector;
		this.postConnector = postConnector;
		this.putConnector = putConnector;
		this.deleteConnector = deleteConnector;
		urlBuilderSupport = UrlBuilderSupport.withBaseUrl(baseUrl, CONTEXT);
	}

	private UriComponentsBuilder uriBuilder() {
		return urlBuilderSupport.createBuilder();
	}

	/**
	 * Imports multiple models from a bulk ZIP or stream payload.
	 *
	 * @param modelBulk the input stream containing a bulk of models; must not be `null`.
	 * @return the list of imported model identifiers.
	 * @throws RestClientException if reading the stream or uploading fails.
	 */
	@Override public List<String> importModelBulk(@NonNull InputStream modelBulk) {
		RestServerRequest<InputStreamResource> request = RestServerRequest
			.withPayload(new InputStreamResource(modelBulk))
			.withContentType(MediaType.APPLICATION_OCTET_STREAM);

		String[] response = putConnector.callServer(urlBuilderSupport.createBuilder().toUriString(), request, String[].class).getData();

		return Arrays.stream(response)
			.toList();
	}

	/**
	 * Creates a new model from the provided textual content.
	 *
	 * @param modelContent the reader supplying the model content; must not be `null`.
	 * @return the created model identifier or name as returned by the server.
	 * @throws RestClientException if the reader cannot be consumed.
	 */
	@Override public String createModel(@NonNull Reader modelContent) {
		String model;
		try {
			model = IOUtils.toString(modelContent);
		} catch (IOException e) {
			throw new RestClientException("Unable to read model", e);
		}
		RestServerRequest<String> modelRequest = RestServerRequest.withPayload(model);
		return postConnector.callServer(uriBuilder().toUriString(), modelRequest, String.class).getData();
	}

	/**
	 * Loads a model by its identifier.
	 *
	 * @param modelId the unique model identifier; must not be `null`.
	 * @return the model content as a JSON or textual representation.
	 */
	@Override public String loadModel(@NonNull String modelId) {
		String url = uriBuilder().pathSegment(modelId).toUriString();
		return getConnector.callServer(url, RestServerRequest.empty(), String.class).getData();
	}

	/**
	 * Updates an existing model with the provided content.
	 *
	 * @param modelContent the new model content; must not be `null`.
	 * @return the updated model identifier or name as returned by the server.
	 */
	@Override public String updateModel(@NonNull String modelContent) {
		RestServerRequest<String> modelRequest = RestServerRequest.withPayload(modelContent);
		String url = uriBuilder().toUriString();
		return putConnector.callServer(url, modelRequest, String.class).getData();
	}

	/**
	 * Deletes a model by its name.
	 *
	 * @param name the model name to delete; must not be `null`.
	 */
	@Override public void deleteModel(final @NonNull String name) {
		String url = uriBuilder().pathSegment(name).toUriString();
		deleteConnector.callServer(url, RestServerRequest.empty(), Boolean.class);
	}

	/**
	 * Generates client-side validation code for the given model.
	 *
	 * @param modelId the model identifier; must not be `null`.
	 * @return JavaScript code used for model validation.
	 */
	@Override public String generateValidationCode(@NonNull String modelId) {
		String url = uriBuilder().pathSegment(modelId).pathSegment(ENDPOINT_VALIDATION_CODE).toUriString();
		RestServerRequest<Void> request = RestServerRequest.empty()
			.withContentType(null)
			.withAccept(new MediaType("application", "javascript", StandardCharsets.UTF_8));
		return getConnector.callServer(url, request, String.class).getData();
	}
}
