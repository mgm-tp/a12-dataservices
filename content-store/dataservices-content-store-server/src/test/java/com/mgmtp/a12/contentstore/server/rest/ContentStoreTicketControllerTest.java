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

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.contentstore.AbstractSpringContextServerTests;
import com.mgmtp.a12.contentstore.configuration.ContentStoreProperties;
import com.mgmtp.a12.contentstore.exception.ExceptionKeys;
import com.mgmtp.a12.contentstore.utils.Constants;

import static com.mgmtp.a12.contentstore.server.rest.constants.Constants.CONTENT_ENDPOINT_PATH;
import static com.mgmtp.a12.contentstore.server.rest.constants.Constants.NEED_TICKET_CONTENT;
import static com.mgmtp.a12.contentstore.server.rest.constants.Constants.TICKET_ENDPOINT_PATH;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ContentStoreTicketControllerTest extends AbstractSpringContextServerTests {

	private static final String PERSISTED_CONTENT_ID = UUID.randomUUID().toString();

	@Autowired
	private WebApplicationContext context;
	private MockMvc mockMvc;
	@Autowired
	public ContentStoreProperties contentStoreProperties;

	@BeforeClass
	public void setUp() throws Exception {
		mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
			.build();
		String persistentType = "private";
		mockMvc.perform(MockMvcRequestBuilders
			.post(contentStoreProperties.getServer().getContextPath() + CONTENT_ENDPOINT_PATH)
			.queryParam("contentId", PERSISTED_CONTENT_ID)
			.queryParam("persistentType", persistentType)
			.content(NEED_TICKET_CONTENT.getBytes(StandardCharsets.UTF_8))
			.contentType(MediaType.TEXT_PLAIN));
	}

	@Test
	public void testCreateTicketAndLoadContent() throws Exception {
		// register ticket
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders
				.get(contentStoreProperties.getServer().getContextPath()
					+ TICKET_ENDPOINT_PATH + "/" + PERSISTED_CONTENT_ID)
			).andExpect(status().isOk())
			.andReturn();

		JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsByteArray());
		String downloadUrl = jsonNode.get("url").textValue();
		// download content with ticket url.
		mockMvc.perform(MockMvcRequestBuilders
				.get(downloadUrl.replaceAll(contentStoreProperties.getBaseUrl(), ""))
						.queryParam("cached", "true"))
			.andExpect(status().isOk())
			.andExpect(header().string("Cache-Control", "no-store, must-revalidate, private"))
			.andExpect(content().string(NEED_TICKET_CONTENT))
			.andDo(print());
	}

	@Test
	public void testGetTicket_WithInvalidInput() throws Exception {
		String invalidDuration = "GroßeDatei";

		mockMvc.perform(MockMvcRequestBuilders
				.get(contentStoreProperties.getServer().getContextPath()
					+ TICKET_ENDPOINT_PATH + "/" + PERSISTED_CONTENT_ID)
				.queryParam("duration", invalidDuration)
			).andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.longMessage.key").value(ExceptionKeys.INVALID_INPUT_ERROR_KEY))
			.andExpect(jsonPath("$.longMessage.default").value(String.format(Constants.INVALID_INPUT_ERROR_PATTEN, invalidDuration)))
			.andExpect(jsonPath("$.shortMessage.key").value(ExceptionKeys.INVALID_INPUT_ERROR_KEY))
			.andExpect(jsonPath("$.shortMessage.default").value(String.format(Constants.INVALID_INPUT_ERROR_PATTEN, invalidDuration)))
			.andDo(print());
	}
}

