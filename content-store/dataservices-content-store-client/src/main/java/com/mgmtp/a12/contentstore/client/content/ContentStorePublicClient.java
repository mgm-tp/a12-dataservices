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
package com.mgmtp.a12.contentstore.client.content;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MimeType;
import org.springframework.web.client.RestClientException;

import com.mgmtp.a12.connector.rest.RestGetConnector;
import com.mgmtp.a12.connector.rest.RestServerRequest;
import com.mgmtp.a12.connector.rest.RestServerResponse;
import com.mgmtp.a12.connector.rest.UrlBuilderSupport;
import com.mgmtp.a12.contentstore.client.configuration.ContentStoreClientProperties;
import com.mgmtp.a12.contentstore.client.content.response.DownloadContentResponse;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Client class for Content Store to handle download content
 */
@Slf4j
public class ContentStorePublicClient {

	private static final String DOWNLOAD_CONTEXT = "download";
	private final RestGetConnector getConnector;
	private final UrlBuilderSupport urlBuilderSupport;

	/**
	 * Creates a client for downloading content from the Content Store.
	 *
	 * @param properties Client properties providing the remote base URL; must not be null.
	 * @param getConnector REST GET connector used for HTTP requests; must not be null.
	 */
	public ContentStorePublicClient(@NonNull ContentStoreClientProperties properties, @NonNull RestGetConnector getConnector) {
		log.debug("Initializing Content Store Public Client");
		this.getConnector = getConnector;
		this.urlBuilderSupport = UrlBuilderSupport.withBaseUrl(properties.getConfiguration().getRemoteUrl(), DOWNLOAD_CONTEXT);
	}

	/**
	 * Downloads content by identifier.
	 *
	 * @param id Content identifier or ticket identifier; must not be null.
	 * @return Response containing the content stream and content type; never null, but may contain null fields if not available.
	 */
	public DownloadContentResponse downloadContent(@NonNull String id) {
		String uri = urlBuilderSupport.createBuilder().pathSegment(id).toUriString();
		return download(uri);
	}

	/**
	 * Downloads content from a full URI.
	 *
	 * @param uri Fully qualified URI to download from; must not be null.
	 * @return Response containing the content stream and content type; never null, but may contain null fields if not available.
	 */
	public DownloadContentResponse download(@NonNull String uri) {
		RestServerResponse<Resource> serverResponse = (RestServerResponse<Resource>) getConnector.callServer(uri, RestServerRequest.empty(), Resource.class);
		return Optional.ofNullable(serverResponse)
			.map(response -> new DownloadContentResponse(responseToInputStream(response), responseToContentType(response)))
			.orElse(new DownloadContentResponse(null, null));
	}

	private static String responseToContentType(RestServerResponse<Resource> response) {
		return Optional.of(response)
			.map(RestServerResponse::getHeaders)
			.map(HttpHeaders::getContentType)
			.map(MimeType::toString)
			.orElse(null);
	}

	private static InputStream responseToInputStream(RestServerResponse<Resource> response) {
		return Optional.of(response)
			.map(RestServerResponse::getData)
			.map(ContentStorePublicClient::responseToInputStream)
			.orElse(null);
	}

	private static InputStream responseToInputStream(Resource resource) {
		try {
			return resource.getInputStream();
		} catch (IOException e) {
			throw new RestClientException("Unable to download content", e);
		}
	}

}
