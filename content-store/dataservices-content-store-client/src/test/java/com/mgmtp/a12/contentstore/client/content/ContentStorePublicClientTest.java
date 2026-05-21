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

import com.mgmtp.a12.contentstore.client.AbstractContentStoreClientTest;
import com.mgmtp.a12.contentstore.client.content.response.DownloadContentResponse;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;
import shaded_package.com.google.common.net.MediaType;

import java.io.IOException;

import static org.htmlunit.HttpHeader.CONTENT_TYPE;
import static org.mockserver.model.BinaryBody.binary;
import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.HttpStatusCode.OK_200;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

@Slf4j
public class ContentStorePublicClientTest extends AbstractContentStoreClientTest {

	@Test public void testDownloadContent_shouldSuccess() throws IOException {
		mockApiDownload();

		DownloadContentResponse downloadContentResponse = contentStorePublicClient.downloadContent(contentId);
		Assert.assertEquals(downloadContentResponse.getInputStream().readAllBytes(), contentUpload.getBytes());
		Assert.assertEquals(downloadContentResponse.getContentType(), MediaType.JPEG.toString());
	}
	@Test(expectedExceptions = {NullPointerException.class}, expectedExceptionsMessageRegExp = "id is marked non-null but is null")
	public void testDownloadContent_idIsNull_shouldThrowException() {
		contentStorePublicClient.downloadContent(null);
	}

	public void mockApiDownload() {
		mockServer.when(
				request()
					.withPath("/download/" + contentId)
					.withMethod("GET")
			)
			.respond(
				response()
					.withStatusCode(OK_200.code())
					.withReasonPhrase(OK_200.reasonPhrase())
					.withHeaders(
						header(CONTENT_TYPE, MediaType.JPEG.toString()),
						header(CONTENT_DISPOSITION, "form-data; name=\"filename.jpg\"; filename=\"filename.jpg\"")
					)
					.withBody(binary(contentUpload.getBytes()))
			);
	}
}
