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
package com.mgmtp.a12.contentstore.client.utils;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.contentstore.DownloadUrlResponse;
import com.mgmtp.a12.contentstore.client.configuration.ContentStoreClientProperties;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for URL operations of the Content Store.
 *
 * +computeDownloadUrl+ method computes the full download URL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlUtils {

	/**
	 * This method is about to compute download URL from download URL response.
	 * If client base prefix from ContentStoreClientProperties is configured,
	 * this method will concatenate base prefix and relative download URL in to full downloadable URL.
	 *
	 * @param downloadUrlResponse The response contains download URL to be computed.
	 * @param properties The client properties configuration.
	 */
	public static void computeDownloadUrl(DownloadUrlResponse downloadUrlResponse, ContentStoreClientProperties properties) {
		if (StringUtils.isNotBlank(properties.getContent().getBasePrefix())) {
			downloadUrlResponse.setUrl(properties.getContent().getBasePrefix() + downloadUrlResponse.getUrl());
		}
	}
}
