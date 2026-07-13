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
package com.mgmtp.a12.contentstore.server.rest;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.contentstore.AbstractSpringContextServerTests;
import com.mgmtp.a12.contentstore.configuration.ContentStoreProperties;

import static com.mgmtp.a12.contentstore.server.rest.constants.Constants.CONTENT_ENDPOINT_PATH;
import static com.mgmtp.a12.contentstore.server.rest.constants.Constants.DOWNLOAD_ENDPOINT_PATH;
import static com.mgmtp.a12.contentstore.server.rest.constants.Constants.ERROR_CONTENT_STORE_INPUT_INVALID;
import static com.mgmtp.a12.contentstore.server.rest.constants.Constants.LARGE_ATTACHMENT_FILE;
import static com.mgmtp.a12.contentstore.server.rest.constants.Constants.PLAIN_TEXT_FILE_NAME;
import static com.mgmtp.a12.contentstore.server.rest.constants.Constants.THUMBNAIL_URL_PATTERN;
import static com.mgmtp.a12.contentstore.utils.Constants.CONTENT_MIME_TYPE_MANDATORY_ERROR;
import static com.mgmtp.a12.contentstore.utils.Constants.PERSISTENT_TYPE_PRIVATE;
import static com.mgmtp.a12.contentstore.utils.Constants.PERSISTENT_TYPE_PUBLIC;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ContentStorePrivateControllerTest extends AbstractSpringContextServerTests {


	@Autowired
	private WebApplicationContext context;
	private MockMvc mockMvc;
	@Autowired
	public ContentStoreProperties contentStoreProperties;

	@BeforeMethod
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
			.build();
	}

	@Test(description = "Should upload and retrieve public content successfully")
	public void shouldUploadAndRetrievePublicContent() throws Exception {
		// Given
		String contentId = UUID.randomUUID().toString();
		byte[] content = "helloWorld".getBytes(StandardCharsets.UTF_8);

		// When - Upload public content
		mockMvc.perform(MockMvcRequestBuilders
				.post(contentStoreProperties.getServer().getContextPath() + CONTENT_ENDPOINT_PATH)
				.queryParam("contentId", contentId)
				.queryParam("persistentType", PERSISTENT_TYPE_PUBLIC)
				.content(content)
				.contentType(MediaType.TEXT_PLAIN))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.size").value(10))
			.andExpect(jsonPath("$.url").isNotEmpty())
			.andExpect(jsonPath("$.url", Matchers.matchesPattern(THUMBNAIL_URL_PATTERN)));

		// Then - Retrieve the uploaded content
		mockMvc.perform(MockMvcRequestBuilders
				.get(contentStoreProperties.getServer().getContextPath() + CONTENT_ENDPOINT_PATH + "/" + contentId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.url").isNotEmpty())
			.andExpect(jsonPath("$.url", Matchers.matchesPattern(THUMBNAIL_URL_PATTERN)));
	}

	@Test
	public void uploadPrivateContent() throws Exception {
		String contentId = UUID.randomUUID().toString();
		mockMvc.perform(MockMvcRequestBuilders
				.post(contentStoreProperties.getServer().getContextPath() + CONTENT_ENDPOINT_PATH)
				.queryParam("contentId", contentId)
				.queryParam("persistentType", PERSISTENT_TYPE_PRIVATE)
				.content("ramdomStr".getBytes(StandardCharsets.UTF_8))
				.contentType(MediaType.TEXT_PLAIN))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.size").value(9))
			.andExpect(jsonPath("$.url").isEmpty());
	}

	@Test
	public void uploadPrivateContent_acceptExternalMimetype() throws Exception {
		String contentId = UUID.randomUUID().toString();
		contentStoreProperties.getServer().getApi().getMimeType().getTrustExternalMimeType().setEnabled(true);
		mockMvc.perform(MockMvcRequestBuilders
				.post(contentStoreProperties.getServer().getContextPath() + CONTENT_ENDPOINT_PATH)
				.queryParam("contentId", contentId)
				.queryParam("persistentType", PERSISTENT_TYPE_PRIVATE)
				.queryParam("filename", PLAIN_TEXT_FILE_NAME)
				.queryParam("mimeType", MediaType.TEXT_PLAIN_VALUE)
				.content("ramdomStr".getBytes(StandardCharsets.UTF_8))
				.contentType(MediaType.TEXT_PLAIN))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.contentType").value(MediaType.TEXT_PLAIN_VALUE))
			.andExpect(jsonPath("$.size").value(9))
			.andExpect(jsonPath("$.url").isEmpty());
		contentStoreProperties.getServer().getApi().getMimeType().getTrustExternalMimeType().setEnabled(false);
	}

	@Test
	public void uploadPrivateContent_acceptExternalMimetype_shouldThrowExceptionOnMissingMimeType() throws Exception {
		String contentId = UUID.randomUUID().toString();
		contentStoreProperties.getServer().getApi().getMimeType().getTrustExternalMimeType().setEnabled(true);
		mockMvc.perform(MockMvcRequestBuilders
				.post(contentStoreProperties.getServer().getContextPath() + CONTENT_ENDPOINT_PATH)
				.queryParam("contentId", contentId)
				.queryParam("persistentType", PERSISTENT_TYPE_PRIVATE)
				.content("ramdomStr".getBytes(StandardCharsets.UTF_8))
				.contentType(MediaType.TEXT_PLAIN))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.shortMessage.key").value(ERROR_CONTENT_STORE_INPUT_INVALID))
			.andExpect(jsonPath("$.shortMessage.default").value(Matchers.containsString(CONTENT_MIME_TYPE_MANDATORY_ERROR)));
		contentStoreProperties.getServer().getApi().getMimeType().getTrustExternalMimeType().setEnabled(false);
	}

	@Test
	public void deleteContent() throws Exception {
		String contentId = UUID.randomUUID().toString();
		String persistentType = "private";
		uploadContent(contentId, persistentType, "uploadContent".getBytes());

		// Send request to delete content.
		mockMvc.perform(MockMvcRequestBuilders
				.delete(contentStoreProperties.getServer().getContextPath() + CONTENT_ENDPOINT_PATH + "/" + contentId))
			.andExpect(status().isNoContent());

		// Assert the content is deleted
		mockMvc.perform(MockMvcRequestBuilders
				.get(contentStoreProperties.getServer().getContextPath() + DOWNLOAD_ENDPOINT_PATH + "/" + contentId))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.shortMessage.key").value("error.content-store.content.notFound"));
	}

	@Test
	public void uploadPublicContent_largeFileSize_hasError() throws Exception {
		try (InputStream file = new FileInputStream(LARGE_ATTACHMENT_FILE)) {
			String contentId = UUID.randomUUID().toString();
			mockMvc.perform(MockMvcRequestBuilders
					.post(contentStoreProperties.getServer().getContextPath() + CONTENT_ENDPOINT_PATH)
					.queryParam("contentId", contentId)
					.queryParam("persistentType", PERSISTENT_TYPE_PUBLIC)
					.content(file.readAllBytes())
					.contentType(MediaType.IMAGE_PNG))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.shortMessage.key").value("error.content-store.content.invalidSize"))
				.andExpect(jsonPath("$.shortMessage.default").value(Matchers.containsString("Content size cannot exceed")));
		}
	}

	private void uploadContent(String contentId, String persistentType, byte[] contents) throws Exception {
		mockMvc.perform(MockMvcRequestBuilders
			.post(contentStoreProperties.getServer().getContextPath() + CONTENT_ENDPOINT_PATH)
			.queryParam("contentId", contentId)
			.queryParam("persistentType", persistentType)
			.content(contents)
			.contentType(MediaType.TEXT_PLAIN));
	}

}

