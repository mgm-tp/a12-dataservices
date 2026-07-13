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
package com.mgmtp.a12.dataservices.client.rpc.attachment;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import tools.jackson.databind.JsonNode;
import com.mgmtp.a12.dataservices.attachment.DataServicesAttachmentURL;
import com.mgmtp.a12.dataservices.client.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.client.attachment.AttachmentClientV2;
import com.mgmtp.a12.dataservices.client.rpc.internal.JsonRpc2RequestBuilder;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;

import lombok.SneakyThrows;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class LoadAttachmentUrlOperationIT extends AbstractSpringContextIT {

	@Autowired AttachmentClientV2 attachmentClientV2;
	private final String operationId = "LoadAttachmentUrl";

	private String attachmentId;
	private DocumentReference documentReference;

	public static final Predicate<String> ATTACHMENT_URL_PATTERN =
		Pattern.compile("^http://localhost:[0-9]*/cs/download/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\?filename=([^&]*\\.[^&]+)$")
			.asPredicate();

	@SneakyThrows
	@BeforeClass public void init() {
		InputStream file = new FileInputStream(ATTACHMENT_FILE);
		findDocumentModelById(BUSINESS_PARTNER_MODEL_NAME).ifPresent(model -> cleanUpByDocumentModel(BUSINESS_PARTNER_MODEL_NAME));
		createModelFromContent(readFile(BUSINESS_PARTNER_MODEL_FILE));
		attachmentId = attachmentClientV2.uploadAttachment(file, FILENAME,
			BUSINESS_PARTNER_MODEL_NAME, "", List.of()).getAttachmentId();
		assertNotNull(attachmentId);
		documentReference = createDocumentFromJson(BUSINESS_PARTNER_MODEL_NAME, readFile(BUSINESS_PARTNER_DOCUMENT).formatted(attachmentId));
	}

	@AfterClass public void cleanUp() {
		cleanUpByDocumentModel(BUSINESS_PARTNER_MODEL_NAME);
	}

	@Test public void getAttachmentUrl() {
		JsonRpc2RequestBuilder rpcRequest1 = requestBuilderFactory.newJsonRpc2RequestBuilder();
		rpcRequest1.addMethodCall(CoreOperationConstants.LOAD_ATTACHMENT_URL_OPERATION)
			.id(operationId)
			.putParameter("attachmentId", attachmentId)
			.putParameter("docRef", documentReference.toString());

		List<JsonRpc2Response> responseList = rpcOperationsClient.invoke(rpcRequest1.build());
		responseList.forEach(res -> {
			assertNull(res.getError(), "Response [%s] should not contain error".formatted(res.getId()));
			assertNotNull(res.getResult(), "Response [%s] should contain result".formatted(res.getId()));
		});

		responseList.stream()
			.filter(e -> operationId.equals(e.getId()))
			.findFirst()
			.ifPresent(e -> assertTrue(ATTACHMENT_URL_PATTERN.test(convertResponse(e.getResult(), DataServicesAttachmentURL.class).getLocation())));
	}

	@Test public void getAttachmentUrlError() {
		JsonRpc2RequestBuilder rpcRequest = requestBuilderFactory.newJsonRpc2RequestBuilder();
		rpcRequest.addMethodCall(CoreOperationConstants.LOAD_ATTACHMENT_URL_OPERATION)
			.id(operationId)
			.putParameter("attachmentId", null)
			.putParameter("docRef", null);

		List<JsonRpc2Response> responseList = rpcOperationsClient.invoke(rpcRequest.build());
		responseList.forEach(res -> {
			assertEquals(res.getError().getMessage(), "JSON-RPC Request failed and rollback was performed");
			assertNull(res.getResult());
		});
	}

	protected <T> T convertResponse(JsonNode value, Class<T> clazz) {
		try {
			return objectMapper.treeToValue(value, clazz);
		} catch (Exception e) {
			throw new IllegalStateException("Cannot parse response data");
		}
	}
}
