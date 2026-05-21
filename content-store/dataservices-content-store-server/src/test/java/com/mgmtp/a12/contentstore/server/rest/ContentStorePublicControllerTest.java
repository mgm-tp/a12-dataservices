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

import com.mgmtp.a12.contentstore.AbstractSpringContextServerTests;
import com.mgmtp.a12.contentstore.configuration.ContentStoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.mgmtp.a12.contentstore.server.rest.constants.Constants.CONTENT_ENDPOINT_PATH;
import static com.mgmtp.a12.contentstore.server.rest.constants.Constants.DOWNLOAD_ENDPOINT_PATH;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ContentStorePublicControllerTest extends AbstractSpringContextServerTests {

	@Autowired
	private WebApplicationContext context;
	@Autowired
	public ContentStoreProperties contentStoreProperties;

	private MockMvc mockMvc;

	@BeforeMethod
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
				.alwaysDo(print())
				.build();
	}

	@Test
	public void downloadPublicContent() throws Exception {
		String contentId = UUID.randomUUID().toString();
		String persistentType = "public";
		createContent(contentId, persistentType);

		mockMvc.perform(MockMvcRequestBuilders
						.get(contentStoreProperties.getServer().getContextPath() + DOWNLOAD_ENDPOINT_PATH + "/" + contentId)
						.queryParam("cacheDuration", "5000"))
				.andExpect(status().isOk())
				.andExpect(header().string("Cache-Control", String.format("max-age=%s, must-revalidate, public", 5000)))
				.andExpect(content().string("randomStr"));

		mockMvc.perform(MockMvcRequestBuilders
						.get(contentStoreProperties.getServer().getContextPath() + DOWNLOAD_ENDPOINT_PATH + "/" + contentId))
				.andExpect(status().isOk())
				.andExpect(header().string("Cache-Control", String.format("max-age=%s, must-revalidate, public", 3600)))
				.andExpect(content().string("randomStr"));

		mockMvc.perform(MockMvcRequestBuilders
						.get(contentStoreProperties.getServer().getContextPath() + DOWNLOAD_ENDPOINT_PATH + "/" + contentId)
						.queryParam("cacheDuration", "-1"))
				.andExpect(status().isOk())
				.andExpect(header().string("Cache-Control", String.format("max-age=%s, must-revalidate, public", Integer.MAX_VALUE)))
				.andExpect(content().string("randomStr"));


	}

	@Test
	public void downloadPublicContentWithNoCached() throws Exception {
		String contentId = UUID.randomUUID().toString();
		String persistentType = "public";
		createContent(contentId, persistentType);

		mockMvc.perform(MockMvcRequestBuilders
						.get(contentStoreProperties.getServer().getContextPath() + DOWNLOAD_ENDPOINT_PATH + "/" + contentId)
						.queryParam("cacheDuration", "0")
				)
				.andExpect(status().isOk())
				.andExpect(header().string("Cache-Control", String.format("max-age=%s, must-revalidate, public", 0)))
				.andExpect(content().string("randomStr"));
	}

	@Test
	public void downloadPrivateContent() throws Exception {
		String contentId = UUID.randomUUID().toString();

		mockMvc.perform(MockMvcRequestBuilders
						.get(DOWNLOAD_ENDPOINT_PATH + "/" + contentId))
				.andExpect(status().isNotFound());
	}

	private MockHttpServletResponse createContent(String contentId, String persistentType) throws Exception {
		return mockMvc.perform(MockMvcRequestBuilders
						.post(contentStoreProperties.getServer().getContextPath() + CONTENT_ENDPOINT_PATH)
						.queryParam("contentId", contentId)
						.queryParam("persistentType", persistentType)
						.characterEncoding(StandardCharsets.UTF_8)
						.content("randomStr".getBytes(StandardCharsets.UTF_8))
						.contentType(MediaType.TEXT_PLAIN))
				.andReturn().getResponse();
	}
}
