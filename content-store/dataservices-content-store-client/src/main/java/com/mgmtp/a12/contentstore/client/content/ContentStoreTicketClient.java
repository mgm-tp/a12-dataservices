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

import lombok.NonNull;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;

import com.mgmtp.a12.connector.rest.RestGetConnector;
import com.mgmtp.a12.connector.rest.RestServerRequest;
import com.mgmtp.a12.connector.rest.UrlBuilderSupport;
import com.mgmtp.a12.contentstore.DownloadUrlResponse;
import com.mgmtp.a12.contentstore.client.configuration.ContentStoreClientProperties;
import com.mgmtp.a12.contentstore.client.utils.UrlUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Client class for requesting private content downloadable URL
 */
@Slf4j
public class ContentStoreTicketClient {
	private static final String CONTENT_CONTEXT = "ticket";
	private static final String DURATION_CONTEXT = "duration";
	private final RestGetConnector getConnector;
	private final UrlBuilderSupport urlBuilderSupport;
	private final ContentStoreClientProperties contentStoreClientProperties;

	/**
	 * Creates a client for requesting download tickets for private content.
	 *
	 * @param properties Client properties (base URL configuration); must not be null.
	 * @param getConnector REST GET connector used for HTTP requests; must not be null.
	 */
	public ContentStoreTicketClient(@NonNull ContentStoreClientProperties properties, @NonNull RestGetConnector getConnector) {
		log.debug("Initializing Content Store Ticket Client");
		this.getConnector = getConnector;
		this.urlBuilderSupport =
			UrlBuilderSupport.withBaseUrl(properties.getConfiguration().getRemoteUrl(), ContentStoreClientConstants.CONTENT_STORE_CONTEXT_PATH,
				CONTENT_CONTEXT);
		this.contentStoreClientProperties = properties;
	}

	/**
	 * Request ticket for downloading private content by id.
	 *
	 * @param contentId The content id.
	 * @return The download url payload.
	 */
	public DownloadUrlResponse requestTicket(@NonNull String contentId) {
		return downloadUrl(urlBuilderSupport.createBuilder().pathSegment(contentId).toUriString());
	}

	/**
	 * Request ticket for downloading private content by id.
	 * @param contentId The content id.
	 * @param duration Input string for transferring to seconds in long number, input case is insensitive.
	 * @return The download url payload
	 */
	public DownloadUrlResponse requestTicket(@NonNull String contentId, @NonNull String duration) {
		String uri = urlBuilderSupport.createBuilder()
			.pathSegment(contentId)
			.queryParam(DURATION_CONTEXT, URLEncoder.encode(duration, StandardCharsets.UTF_8))
			.toUriString();
		return downloadUrl(uri);
	}

	private DownloadUrlResponse downloadUrl(String uri) {
		DownloadUrlResponse downloadUrlResponse = getConnector.callServer(uri, RestServerRequest.empty().withAccept(MediaType.APPLICATION_JSON),
			DownloadUrlResponse.class).getData();
		UrlUtils.computeDownloadUrl(downloadUrlResponse, contentStoreClientProperties);
		return downloadUrlResponse;
	}
}
