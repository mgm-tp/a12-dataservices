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
package com.mgmtp.a12.dataservices.server.attachment.rest;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.dataservices.attachment.AttachmentAnnotation;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeaderSpec;
import com.mgmtp.a12.dataservices.attachment.AttachmentThumbnailUrl;
import com.mgmtp.a12.dataservices.attachment.DataServicesAttachmentURL;
import com.mgmtp.a12.dataservices.client.rpc.internal.JsonRpc2RequestBuilder;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;
import com.mgmtp.a12.dataservices.server.AbstractSpringContextServerTests;

import lombok.NonNull;
import lombok.SneakyThrows;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@AutoConfigureMockMvc
@TestExecutionListeners(MockitoTestExecutionListener.class)
public class AttachmentV2ControllerImplTest extends AbstractSpringContextServerTests {
	private static final String ATTACHMENT_ENDPOINT_PATH = "/v2/attachment";
	private static final String RPC_ENDPOINT_PATH = "/v2/rpc";
	private static final String FILENAME = "image.png";
	private static final List<AttachmentAnnotation> ANNOTATION_LIST =
		List.of(new AttachmentAnnotation("annotation1", "value1"), new AttachmentAnnotation("annotation2", "value2"));

	@Autowired private DataServicesCoreProperties dataServicesCoreProperties;
	@Autowired private MockMvc mvc;

	private DocumentReference documentReference;

	@BeforeMethod public void setUp() {
		super.cleanUpTestEnvironment();
		changeUserInContext("admin");
		createModel(loadResourceFromClasspathAsString(MODEL_PATH_FOLDER + "BusinessPartner.json"));
	}

	@Test public void attachmentScenario() throws Exception {
		MockHttpServletResponse uploadResponse = assertAttachmentUpload();

		AttachmentHeaderSpec newAttachmentHeader = objectMapper.readValue(uploadResponse.getContentAsString(), AttachmentHeaderSpec.class);

		changeUserInContext("admin");
		documentReference = assertDocumentWithAttachment(BUSINESS_PARTNER_MODEL_NAME,
			String.format(readFile(BUSINESS_PARTNER_DOCUMENT_FILE), newAttachmentHeader.getAttachmentId()));
		ImmutablePair<DataServicesAttachmentURL, AttachmentThumbnailUrl> attachmentUrl = assertGetUrls(newAttachmentHeader);
		assertAttachmentContent(attachmentUrl.getLeft().getLocation(), "hello".getBytes(StandardCharsets.UTF_8));
		assertThumbnails(attachmentUrl.getRight());
	}

	@NonNull private MockHttpServletResponse assertAttachmentUpload() throws Exception {
		return mvc.perform(MockMvcRequestBuilders
				.post(dataServicesCoreProperties.getServer().getContextPath() + ATTACHMENT_ENDPOINT_PATH)
				.queryParam("filename", FILENAME)
				.queryParam("documentModelName", BUSINESS_PARTNER_MODEL_NAME)
				.queryParam("pathToField", "")
				.queryParam("annotations", "annotation1:value1", "annotation2:value2")
				.content("hello".getBytes(StandardCharsets.UTF_8))
				.contentType(MediaType.TEXT_PLAIN))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.attachmentId").isNotEmpty())
			.andExpect(jsonPath("$.filename").value(FILENAME))
			.andExpect(jsonPath("$.size").value(5))
			.andExpect(jsonPath("$.annotations[0].name").value(ANNOTATION_LIST.get(0).getName()))
			.andExpect(jsonPath("$.annotations[0].value").value(ANNOTATION_LIST.get(0).getValue()))
			.andReturn().getResponse();
	}

	private DocumentReference assertDocumentWithAttachment(String documentModel, String document) throws Exception {

		JsonRpc2RequestBuilder request = new JsonRpc2RequestBuilder(objectMapper);
		request.addMethodCall(CoreOperationConstants.ADD_DOCUMENT_OPERATION)
			.id("AddDocument")
			.putParameter("documentModelName", documentModel)
			.putParameter("document", objectMapper.readTree(document));

		MockHttpServletResponse response = mvc.perform(
				MockMvcRequestBuilders.post(dataServicesCoreProperties.getServer().getContextPath() + RPC_ENDPOINT_PATH)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request.build())))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[*].result").isNotEmpty())
			.andExpect(jsonPath("$[*].error").doesNotExist())
			.andReturn().getResponse();

		logger.info(String.format("Document Reference: %s", response.getContentAsString()));
		List<JsonRpc2Response> jsonRpc2Response = objectMapper.readValue(response.getContentAsString(), new TypeReference<>() {
		});

		return extractTypedResponse(jsonRpc2Response, DocumentReference.class, "AddDocument");
	}

	private ImmutablePair<DataServicesAttachmentURL, AttachmentThumbnailUrl> assertGetUrls(AttachmentHeaderSpec newAttachmentHeader) throws Exception {

		JsonRpc2RequestBuilder request = new JsonRpc2RequestBuilder(objectMapper);
		request.addMethodCall(CoreOperationConstants.LOAD_ATTACHMENT_URL_OPERATION)
			.id("LoadAttachmentUrl")
			.putParameter("attachmentId", newAttachmentHeader.getAttachmentId())
			.putParameter("docRef", documentReference.toString());
		request.addMethodCall(CoreOperationConstants.LOAD_THUMBNAIL_URL_OPERATION)
			.id("LoadThumbnailUrl")
			.putParameter("attachmentId", newAttachmentHeader.getAttachmentId());

		changeUserInContext("admin");
		MockHttpServletResponse response = mvc.perform(
				MockMvcRequestBuilders.post(dataServicesCoreProperties.getServer().getContextPath() + RPC_ENDPOINT_PATH)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request.build()))
					.queryParam("docRef", documentReference.toString()))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[*].result").isNotEmpty())
			.andExpect(jsonPath("$[*].error").doesNotExist())
			.andReturn().getResponse();

		logger.info(String.format("Attachment url: %s", response.getContentAsString()));
		List<JsonRpc2Response> jsonRpc2Response = objectMapper.readValue(response.getContentAsString(), new TypeReference<>() {
		});

		DataServicesAttachmentURL attachmentUrl = extractTypedResponse(jsonRpc2Response, DataServicesAttachmentURL.class, "LoadAttachmentUrl");
		assertTrue(ATTACHMENT_URL_PATTERN.test(attachmentUrl.getLocation()));

		AttachmentThumbnailUrl thumbnailUrls = extractTypedResponse(jsonRpc2Response, AttachmentThumbnailUrl.class, "LoadThumbnailUrl");
		assertNull(thumbnailUrls.getBigThumbnailUrl());
		assertNull(thumbnailUrls.getSmallThumbnailUrl());

		return ImmutablePair.of(attachmentUrl, thumbnailUrls);
	}

	private void assertAttachmentContent(String location, byte[] expectedContent) throws Exception {
		MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.get(adoptCsUrl(location)))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.TEXT_PLAIN))
			.andReturn().getResponse();
		assertEquals(response.getContentAsByteArray(), expectedContent);
	}

	private void assertThumbnails(AttachmentThumbnailUrl thumbnailUrls) {
		assertThumbnailsNotFound(thumbnailUrls.getBigThumbnailUrl());
		assertThumbnailsNotFound(thumbnailUrls.getSmallThumbnailUrl());
	}

	private void assertThumbnailsNotFound(String location) {
		assertNull(location);
	}

	@NonNull private static String adoptCsUrl(String location) {
		return location.replaceAll("^http://localhost:8080", "");
	}

	private <T> T extractTypedResponse(List<JsonRpc2Response> jsonRpc2Response, Class<T> valueType, String operationId) {
		return jsonRpc2Response.stream()
			.filter(r -> Objects.equals(r.getId(), operationId))
			.findFirst()
			.filter(JsonRpc2Response::isSuccess)
			.map(JsonRpc2Response::getResult)
			.map(r -> deserialize(r, valueType))
			.orElseThrow();
	}

	@SneakyThrows private <T> T deserialize(JsonNode r, Class<T> valueType) {
		return objectMapper.treeToValue(r, valueType);
	}
}

