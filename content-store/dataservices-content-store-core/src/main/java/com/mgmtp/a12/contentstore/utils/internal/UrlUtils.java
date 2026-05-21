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
package com.mgmtp.a12.contentstore.utils.internal;

import java.util.Optional;

import com.mgmtp.a12.contentstore.configuration.ContentStoreProperties;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlUtils {

	public static final String CONTENT_STORE_DOWNLOAD_URL_PATTERN = "%s%s/download/%s";

	/**
	 * Creates URL for content download based on the baseUrl and id (ticketId or contentId)
	 * The url will combine configured `baseUrl`, `contextPath` and `id` to construct full downloadable url
	 *
	 * @param contentStoreProperties properties to combine with pattern
	 * @param id unique id of ticket or content to render downloadable url
	 * @return downloadable url String
	 */
	public static String renderContentUrl(ContentStoreProperties contentStoreProperties, String id) {
		return buildContentUrl(contentStoreProperties.getBaseUrl(), contentStoreProperties.getServer().getContextPath(), id);
	}

	/**
	 * Creates URL for content download based on the baseUrl and id (ticketId or contentId)
	 * The url will combine configured `baseUrl`, and `id` to construct full downloadable url
	 *
	 * @param baseUrl to use for generate downloadable url
	 * @param contextPath to use for generate downloadable url
	 * @param id unique id of ticket or content to render downloadable url
	 * @return downloadable url String
	 */
	public static String buildContentUrl(String baseUrl, String contextPath, String id) {
		return String.format(
			CONTENT_STORE_DOWNLOAD_URL_PATTERN,
			Optional.ofNullable(baseUrl).orElse(""),
			Optional.ofNullable(contextPath).orElse(""),
			id).replaceAll("(?<!(http:|https:))///?", "/");
	}
}
