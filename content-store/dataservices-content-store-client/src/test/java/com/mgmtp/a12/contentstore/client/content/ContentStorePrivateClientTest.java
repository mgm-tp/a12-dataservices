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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.Parameter;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.contentstore.ContentPersistenceResult;
import com.mgmtp.a12.contentstore.DownloadUrlResponse;
import com.mgmtp.a12.contentstore.client.AbstractContentStoreClientTest;
import com.mgmtp.a12.contentstore.client.utils.UrlUtils;

import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.contentstore.client.constants.Constants.CONTENT_ID_PARAM;
import static com.mgmtp.a12.contentstore.client.constants.Constants.FILENAME_PARAM;
import static com.mgmtp.a12.contentstore.client.constants.Constants.MIME_TYPE_PARAM;
import static com.mgmtp.a12.contentstore.client.constants.Constants.PERSISTENT_TYPE_PARAM;
import static com.mgmtp.a12.contentstore.client.constants.Constants.PERSISTENT_TYPE_PUBLIC;
import static org.htmlunit.HttpHeader.CONTENT_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.HttpStatusCode.OK_200;

@Slf4j
public class ContentStorePrivateClientTest extends AbstractContentStoreClientTest {

	@Test public void testDeleteContent_shouldSuccess() {
		mockApiDelete();

		contentStorePrivateClient.deleteContent(contentId);
	}

	private static void assertUploadResponse(ContentPersistenceResult response, ContentPersistenceResult mockedResponse) {
		Assert.assertEquals(response.getContentId(), mockedResponse.getContentId());
		Assert.assertEquals(response.getContentType(), mockedResponse.getContentType());
		Assert.assertEquals(response.getSize(), mockedResponse.getSize());
		Assert.assertEquals(mockedResponse.getUrl(), response.getUrl());
	}

	@Test public void testUploadContent_shouldSuccess() {
		InputStream inputStream = new ByteArrayInputStream(contentUpload.getBytes());
		String id = UUID.randomUUID().toString();
		ContentPersistenceResult mockedResponse = getContentPersistenceResponse();

		mockApiUpload(id, mockedResponse, Map.of());

		ContentPersistenceResult response = contentStorePrivateClient.uploadContent(inputStream, id, PERSISTENT_TYPE_PUBLIC, null, null);

		assertUploadResponse(response, mockedResponse);
	}

	@Test(expectedExceptions = { NullPointerException.class }, expectedExceptionsMessageRegExp = "content is marked non-null but is null")
	public void testUploadContent_contentIsNull_shouldThrowException() {
		contentStorePrivateClient.uploadContent(null, UUID.randomUUID().toString(), PERSISTENT_TYPE_PUBLIC, null, null);
	}

	@Test(expectedExceptions = { NullPointerException.class }, expectedExceptionsMessageRegExp = "contentId is marked non-null but is null")
	public void testUploadContent_idIsNull_shouldThrowException() {
		contentStorePrivateClient.uploadContent(new ByteArrayInputStream(contentUpload.getBytes()), null, PERSISTENT_TYPE_PUBLIC, null, null);
	}

	@Test public void testUploadContentWithFilename_shouldSuccess() {
		String id = UUID.randomUUID().toString();
		InputStream inputStream = new ByteArrayInputStream(contentUpload.getBytes());
		ContentPersistenceResult mockedResponse = getContentPersistenceResponse();

		String fileName = RandomStringUtils.insecure().nextAlphabetic(10);
		Map<String, String> params = Map.of(FILENAME_PARAM, fileName);
		mockApiUpload(id, mockedResponse, params);

		ContentPersistenceResult responseForFilename = contentStorePrivateClient.uploadContent(inputStream, id, PERSISTENT_TYPE_PUBLIC, fileName, null);

		assertUploadResponse(responseForFilename, mockedResponse);
	}

	@Test public void testGetDownloadUrl_shouldSuccess() {
		String url = RandomStringUtils.insecure().nextAlphabetic(50);
		mockApiGetDownloadUrl(url);

		try (MockedStatic<UrlUtils> urlUtilsMockedStatic = Mockito.mockStatic(UrlUtils.class)) {
			DownloadUrlResponse downloadUrlResponse = contentStorePrivateClient.getDownloadUrl(contentId);
			urlUtilsMockedStatic.when(() -> UrlUtils.computeDownloadUrl(any(), any()))
				.thenAnswer((Answer<Void>) invocation -> null);

			// this ensure UrlUtils.computeDownloadUrl has been called.
			urlUtilsMockedStatic.verify(() -> UrlUtils.computeDownloadUrl(any(), eq(contentStoreClientProperties)));
			Assert.assertEquals(downloadUrlResponse.getUrl(), url);
		}
	}

	public void mockApiDelete() {
		mockServer.when(
				request()
					.withPath("/api/content/" + contentId)
					.withMethod("DELETE")
			)
			.respond(
				response()
					.withHeaders(
						new Header(CONTENT_TYPE, "application/json")
					)
					.withStatusCode(204)
			);
	}

	@Test public void testUploadContentWithFilenameAndMineType_shouldSuccess() {
		String id = UUID.randomUUID().toString();
		InputStream inputStream = new ByteArrayInputStream(contentUpload.getBytes());
		ContentPersistenceResult mockedResponse = getContentPersistenceResponse();
		String fileName = RandomStringUtils.insecure().nextAlphabetic(10);
		String mineType = RandomStringUtils.insecure().nextAlphabetic(10);
		Map<String, String> params = Map.of(FILENAME_PARAM, fileName, MIME_TYPE_PARAM, mineType);

		mockApiUpload(id, mockedResponse, params);

		ContentPersistenceResult response = contentStorePrivateClient.uploadContent(inputStream, id, PERSISTENT_TYPE_PUBLIC, fileName, mineType);

		assertUploadResponse(response, mockedResponse);
	}

	private void mockApiGetDownloadUrl(String url) {
		DownloadUrlResponse response = new DownloadUrlResponse();
		response.setUrl(url);

		mockServer.when(
				request()
					.withPath("/api/content/" + contentId)
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

	public void mockApiUpload(String id, ContentPersistenceResult response, Map<String, String> queryParams) {
		String jsonResponse = objectWriter.writeValueAsString(response);

		HttpResponse httpResponse = response()
			.withStatusCode(OK_200.code())
			.withReasonPhrase(OK_200.reasonPhrase())
			.withHeaders(
				new Header(CONTENT_TYPE, "application/json")
			)
			.withBody(jsonResponse);

		HttpRequest request = request()
			.withPath("/api/content")
			.withQueryStringParameter(new Parameter(CONTENT_ID_PARAM, id))
			.withQueryStringParameter(new Parameter(PERSISTENT_TYPE_PARAM, PERSISTENT_TYPE_PUBLIC))
			.withBody(contentUpload.getBytes())
			.withMethod("POST");

		if (queryParams.containsKey(FILENAME_PARAM)) {
			request.withQueryStringParameter(new Parameter(FILENAME_PARAM, queryParams.get(FILENAME_PARAM)));
		}
		if (queryParams.containsKey(MIME_TYPE_PARAM)) {
			request.withQueryStringParameter(new Parameter(MIME_TYPE_PARAM, queryParams.get(MIME_TYPE_PARAM)));
		}

		mockServer.when(request)
			.respond(httpResponse);
	}

	private ContentPersistenceResult getContentPersistenceResponse() {
		ContentPersistenceResult mockedResponse = new ContentPersistenceResult();
		mockedResponse.setContentId(UUID.randomUUID().toString());
		mockedResponse.setContentType(RandomStringUtils.insecure().nextAlphabetic(10));
		mockedResponse.setUrl(Optional.of(RandomStringUtils.insecure().nextAlphabetic(50)));
		mockedResponse.setSize(1000);
		return mockedResponse;
	}

}
