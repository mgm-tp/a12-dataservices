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

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;

import com.mgmtp.a12.connector.rest.RestDeleteConnector;
import com.mgmtp.a12.connector.rest.RestGetConnector;
import com.mgmtp.a12.connector.rest.RestPostConnector;
import com.mgmtp.a12.connector.rest.RestServerRequest;
import com.mgmtp.a12.connector.rest.UrlBuilderSupport;
import com.mgmtp.a12.contentstore.ContentPersistenceResult;
import com.mgmtp.a12.contentstore.DownloadUrlResponse;
import com.mgmtp.a12.contentstore.client.configuration.ContentStoreClientProperties;
import com.mgmtp.a12.contentstore.client.utils.UrlUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Client class for Content Store to handle upload, delete and generate downloadable URL.
 */
@Slf4j
public class ContentStorePrivateClient {

	private static final String CONTENT_CONTEXT = "content";
	private static final String CONTENT_ID_PARAM = "contentId";
	private static final String PERSISTENT_PARAM = "persistentType";
	private static final String FILENAME_PARAM = "filename";
	private static final String MIME_TYPE_PARAM = "mimeType";
	private final RestPostConnector postConnector;
	private final RestGetConnector getConnector;
	private final RestDeleteConnector deleteConnector;
	private final UrlBuilderSupport urlBuilderSupport;
	private final ContentStoreClientProperties contentStoreClientProperties;

	/**
	 * Content Store private client for uploading and deleting content, and getting download URL.
	 *
	 * @param properties Client properties.
	 * @param postConnector Post connector.
	 * @param getConnector Get connector.
	 * @param deleteConnector Delete connector.
	 */
	@Autowired(required = false)
	public ContentStorePrivateClient(@NonNull ContentStoreClientProperties properties, @NonNull RestPostConnector postConnector, @NonNull RestGetConnector getConnector,
									 @NonNull RestDeleteConnector deleteConnector) {
		log.debug("Initializing Content Store Private Client");
		this.urlBuilderSupport =
			UrlBuilderSupport.withBaseUrl(
				properties.getConfiguration().getRemoteUrl(),
				ContentStoreClientConstants.CONTENT_STORE_CONTEXT_PATH,
				CONTENT_CONTEXT);
		this.postConnector = postConnector;
		this.getConnector = getConnector;
		this.deleteConnector = deleteConnector;
		this.contentStoreClientProperties = properties;
	}

	/**
	 * Uploads content to the Content Store. Optionally provides filename and MIME type to aid type detection on the server side.
	 *
	 * @param content Input stream of the content to upload; must not be null.
	 * @param contentId Content identifier in UUID format; must not be null.
	 * @param persistentType Persistence type of the content ("public" or "private"); may be null to use the server default.
	 * @param filename Original file name; helps server-side MIME type detection; may be null.
	 * @param mimeType Explicit MIME type of the content; used if the server trusts client-provided type; may be null.
	 * @return Result containing the stored size and, for public content, the download URL.
	 */
	public ContentPersistenceResult uploadContent(@NonNull InputStream content, @NonNull String contentId, String persistentType, String filename, String mimeType) {
		log.info("Upload Content from client with id {}, type {}, file name {}, mime type {}", contentId, persistentType, filename, mimeType);
		String uri = urlBuilderSupport.createBuilder()
			.queryParam(CONTENT_ID_PARAM, contentId)
			.queryParam(PERSISTENT_PARAM, persistentType)
			.queryParam(FILENAME_PARAM, filename)
			.queryParam(MIME_TYPE_PARAM, mimeType)
			.toUriString();

		return sendContentPersistenceRequest(content, uri);
	}

	/**
	 * Delete the content by id.
	 *
	 * @param contentId id to delete.
	 */
	public void deleteContent(@NonNull String contentId) {
		log.info("deleteContent is called with contentId {}", contentId);
		String uri = urlBuilderSupport.createBuilder().pathSegment(contentId).toUriString();
		deleteConnector.callServer(uri, RestServerRequest.empty(), null);
	}

	/**
	 * Get the download content url by content id.
	 *
	 * @param contentId to request downloadable URL for public content.
	 * @return Download url.
	 */
	public DownloadUrlResponse getDownloadUrl(@NonNull String contentId) {
		log.info("getPublicContent is called with contentId {}", contentId);
		String uri = urlBuilderSupport.createBuilder().pathSegment(contentId).toUriString();
		DownloadUrlResponse downloadUrlResponse = getConnector.callServer(uri, RestServerRequest.empty().withAccept(MediaType.APPLICATION_JSON),
			DownloadUrlResponse.class).getData();
		UrlUtils.computeDownloadUrl(downloadUrlResponse, contentStoreClientProperties);
		return downloadUrlResponse;
	}

	private ContentPersistenceResult sendContentPersistenceRequest(InputStream content, String uri) {
		RestServerRequest<InputStreamResource> serverRequest = RestServerRequest.withPayload(new InputStreamResource(content));

		return postConnector.callServer(uri, serverRequest, ContentPersistenceResult.class)
			.getData();
	}
}
