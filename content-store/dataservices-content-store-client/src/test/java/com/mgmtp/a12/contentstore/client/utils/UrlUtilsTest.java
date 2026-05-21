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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.contentstore.DownloadUrlResponse;
import com.mgmtp.a12.contentstore.client.configuration.ContentStoreClientProperties;

public class UrlUtilsTest {

	protected static final String CONTENT_ID = "da6c08f5-1dc7-4c43-9099-6dab9d735230";
	protected static final String HTTP_BASE_URL = "http://base.url";
	protected static final String CONTENT_DOWNLOAD_PATH = "/content/download/" + CONTENT_ID;
	protected static final String CONTENT_DOWNLOAD_URL = HTTP_BASE_URL + "/content/download/" + CONTENT_ID;

	@DataProvider public static Object[][] downloadUrlDataProvider() {
		return new Object[][] {
			new Object[] { CONTENT_DOWNLOAD_PATH, HTTP_BASE_URL, CONTENT_DOWNLOAD_URL },      // Compute download URL from base and path
			new Object[] { CONTENT_DOWNLOAD_URL, HTTP_BASE_URL, HTTP_BASE_URL + CONTENT_DOWNLOAD_URL },
			// No need to compute download URL is fully URL format
			new Object[] { CONTENT_DOWNLOAD_PATH, "", CONTENT_DOWNLOAD_PATH },                // Return path when missing base URL
			new Object[] { CONTENT_DOWNLOAD_URL, "", CONTENT_DOWNLOAD_URL }                   // return URL when missing base URL
		};
	}

	@Test(dataProvider = "downloadUrlDataProvider")
	public void testRequestTicket_shouldReturnExpectedDownloadUrl(String downloadUrl, String contentBaseUrl, String expectedDownloadUrl) {
		DownloadUrlResponse downloadUrlResponse = new DownloadUrlResponse();
		downloadUrlResponse.setUrl(downloadUrl);
		ContentStoreClientProperties contentStoreClientProperties = new ContentStoreClientProperties();
		contentStoreClientProperties.getContent().setBaseUrl(contentBaseUrl);

		UrlUtils.computeDownloadUrl(downloadUrlResponse, contentStoreClientProperties);

		Assert.assertEquals(downloadUrlResponse.getUrl(), expectedDownloadUrl);
	}

}
