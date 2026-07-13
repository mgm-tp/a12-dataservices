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

import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockserver.model.Header;
import org.mockserver.model.Parameter;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.contentstore.DownloadUrlResponse;
import com.mgmtp.a12.contentstore.client.AbstractContentStoreClientTest;
import com.mgmtp.a12.contentstore.client.utils.UrlUtils;

import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.contentstore.client.constants.Constants.DURATION_PARAM;
import static org.htmlunit.HttpHeader.CONTENT_TYPE;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.HttpStatusCode.OK_200;

@Slf4j
public class ContentStoreTicketClientTest extends AbstractContentStoreClientTest {

	@BeforeMethod public void initData() {
		contentId = UUID.randomUUID().toString();
	}

	@Test public void testRequestTicket_shouldSuccess() {
		String url = RandomStringUtils.randomAlphabetic(50);
		mockApiGetTicket(url);

		try (MockedStatic<UrlUtils> urlUtilsMockedStatic = Mockito.mockStatic(UrlUtils.class)) {

			DownloadUrlResponse downloadUrlResponse = contentStoreTicketClient.requestTicket(contentId);

			// this ensure UrlUtils.computeDownloadUrl has been called.
			urlUtilsMockedStatic.verify(() -> UrlUtils.computeDownloadUrl(argThat(res -> res.getUrl().equals(url))
				, eq(contentStoreClientProperties)));
			Assert.assertEquals(downloadUrlResponse.getUrl(), url);
		}
	}

	@Test public void testUploadContentWithDuration_shouldSuccess() {
		String url = RandomStringUtils.randomAlphabetic(50);
		String duration = "1 s";
		mockApiGetTicketWithDuration(url, duration);

		try (MockedStatic<UrlUtils> urlUtilsMockedStatic = Mockito.mockStatic(UrlUtils.class)) {

			DownloadUrlResponse downloadUrlResponse = contentStoreTicketClient.requestTicket(contentId, duration);

			// this ensure UrlUtils.computeDownloadUrl has been called.
			urlUtilsMockedStatic.verify(() -> UrlUtils.computeDownloadUrl(argThat(res -> res.getUrl().equals(url))
				, eq(contentStoreClientProperties)));
			Assert.assertEquals(downloadUrlResponse.getUrl(), url);
		}
	}

	@Test(expectedExceptions = { NullPointerException.class }, expectedExceptionsMessageRegExp = "contentId is marked non-null but is null")
	public void testRequestTicket_idIsNull_shouldThrowException() {
		contentStoreTicketClient.requestTicket(null);
	}

	public void mockApiGetTicket(String url) {
		DownloadUrlResponse response = new DownloadUrlResponse();
		response.setUrl(url);

		mockServer.when(
				request()
					.withPath("/api/ticket/" + contentId)
					.withMethod("GET")
			)
			.respond(
				response()
					.withStatusCode(OK_200.code())
					.withReasonPhrase(OK_200.reasonPhrase())
					.withHeaders(
						new Header(CONTENT_TYPE, "application/json")
					)
					.withBody(objectWriter.writeValueAsString(response))
			);
	}

	public void mockApiGetTicketWithDuration(String url, String duration) {
		DownloadUrlResponse response = new DownloadUrlResponse();
		response.setUrl(url);

		mockServer.when(
				request()
					.withPath("/api/ticket/" + contentId)
					.withQueryStringParameter(new Parameter(DURATION_PARAM, duration))
					.withMethod("GET")
			)
			.respond(
				response()
					.withStatusCode(OK_200.code())
					.withReasonPhrase(OK_200.reasonPhrase())
					.withHeaders(
						new Header(CONTENT_TYPE, "application/json")
					)
					.withBody(objectWriter.writeValueAsString(response))
			);
	}
}
