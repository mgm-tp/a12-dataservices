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
package com.mgmtp.a12.dataservices.rpc.attachment;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentThumbnailUrl;
import com.mgmtp.a12.dataservices.attachment.AttachmentService;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;

import lombok.SneakyThrows;

import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

public class LoadThumbnailUrlOperationIT extends AbstractSpringContextIT {

	@Autowired private AttachmentService attachmentService;
	private AttachmentHeader attachemnt;

	@SneakyThrows @BeforeMethod
	public void setUp() {
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
		attachemnt = attachmentService.createAttachment(resourceFunctions.loadResourceAsStream("/attachment/image-attachment.png"), "image-attachment.png",
			DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, "", List.of());
	}

	@Test public void testSendLoadThumbnailRequest() throws IOException {
		String loadThumbnailUrlRequest =
			String.format(loadResourceFromClasspathAsString(PathConstants.ATTACHMENT_RPC_PATH + "/load_thumbnail_url_request.json"), attachemnt.getAttachmentId());
		List<JsonRpc2Response> responses = sendRpcRequest(loadThumbnailUrlRequest);
		JsonRpc2Response jsonRpcresponse = responses.stream().filter(r -> Objects.equals(r.getId(), "loadThumbnailUrl")).findFirst().orElseThrow();
		assertTrue(jsonRpcresponse.isSuccess());
		AttachmentThumbnailUrl result = objectMapper.treeToValue(jsonRpcresponse.getResult(), AttachmentThumbnailUrl.class);
		String expected = String.format(resourceFunctions.loadResource(PathConstants.ATTACHMENT_RPC_PATH + "load_thumbnail_url_response.json"), result.getSmallThumbnailUrl(),
			result.getBigThumbnailUrl());
		String actual = objectMapper.writeValueAsString(responses);
		JSONAssert.assertEquals(expected, actual, false);
		assertTrue(PUBLIC_URL_PATTERN.test(result.getBigThumbnailUrl()));
		assertTrue(PUBLIC_URL_PATTERN.test(result.getSmallThumbnailUrl()));
		assertNotEquals(result.getBigThumbnailUrl(), result.getSmallThumbnailUrl());
	}
}
