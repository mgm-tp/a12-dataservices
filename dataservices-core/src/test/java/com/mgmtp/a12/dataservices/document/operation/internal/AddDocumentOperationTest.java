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
package com.mgmtp.a12.dataservices.document.operation.internal;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.springframework.security.access.AccessDeniedException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.exception.DocumentValidationException;
import com.mgmtp.a12.dataservices.document.internal.DefaultDataServicesDocument;
import com.mgmtp.a12.dataservices.document.internal.DefaultDocumentMetadata;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.IntegrityException;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.rpc.RpcException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

@Listeners(MockitoTestNGListener.class)
public class AddDocumentOperationTest {

	@InjectMocks private AddDocumentOperation addDocumentOperation;

	@Mock private DocumentService documentService;
	@Mock private Anonymizer anonymizer;
	@Mock private DefaultDocumentMetadata documentMetadata;
	private final ObjectMapper objectMapper = new ObjectMapper();

	private AutoCloseable openMocks;
	private DataServicesDocument dataServicesDocument;
	private ObjectNode jsonDocument;
	private final String testDocumentModelName = "BusinessPartner";

	@BeforeMethod
	public void setUp() {
		openMocks = MockitoAnnotations.openMocks(documentMetadata);
		dataServicesDocument = new DefaultDataServicesDocument(null, documentMetadata);
		jsonDocument = objectMapper.createObjectNode();
		jsonDocument.put("name", "John Doe");
	}

	@AfterMethod
	public void tearDown() throws Exception {
		if (openMocks != null) {
			openMocks.close();
		}
	}

	@Test public void testAddDocument_success() {
		Mockito.when(documentService.create(Mockito.anyString(), Mockito.any(JsonNode.class), Mockito.any())).thenReturn(dataServicesDocument);
		Mockito.when(documentMetadata.getDocRef()).thenReturn(new DocumentReference("BusinessPartner", "1"));

		addDocumentOperation.rpc(testDocumentModelName, jsonDocument, null);

		Mockito.verify(documentService, Mockito.times(1)).create(testDocumentModelName, jsonDocument, null);
	}

	@Test(expectedExceptions = NotFoundException.class,
		description = "Should wrap NotFoundException with 'Could not create document' message")
	public void testAddDocument_notFoundExceptionWrappedWithMessage() {
		Mockito.doThrow(new NotFoundException("model.not.found", "Model not found"))
			.when(documentService)
			.create(Mockito.anyString(), Mockito.any(JsonNode.class), Mockito.any());

		try {
			addDocumentOperation.rpc(testDocumentModelName, jsonDocument, null);
			fail("Expected NotFoundException");
		} catch (NotFoundException e) {
			assertEquals(e.getShortMessage().getDefaultMessage(), "Could not create document");
			throw e;
		}
	}

	@Test
	public void testAddDocument_baseException() {
		Mockito.doThrow(DocumentValidationException.class)
			.when(documentService)
			.create(Mockito.anyString(), Mockito.any(JsonNode.class), Mockito.any());

		try {
			addDocumentOperation.rpc(testDocumentModelName, jsonDocument, null);
			fail("Expected exception");
		} catch (BaseException e) {
			assertEquals(e.getShortMessage().getDefaultMessage(), "Could not create document");
		} catch (Exception e) {
			fail("Expected BaseException");
		}
	}

	@Test
	public void testAddDocument_unknownException() {
		Mockito.doThrow(RuntimeException.class)
			.when(documentService)
			.create(Mockito.anyString(), Mockito.any(JsonNode.class), Mockito.any());

		try {
			addDocumentOperation.rpc(testDocumentModelName, jsonDocument, null);
			fail("Expected RpcException");
		} catch (RpcException e) {
			assertEquals(e.getOperationError().getOperationId(), RemoteOperation.RemoteOperationHelper.getOperationId(AddDocumentOperation.class));
		} catch (Exception e) {
			fail("Expected RpcException");
		}
	}

	@Test
	public void testAddDocument_withExceptionWithoutKey() {
		String errorMessage = RandomStringUtils.randomAlphabetic(20);
		Mockito.doThrow(new IntegrityException(errorMessage))
			.when(documentService)
			.create(Mockito.anyString(), Mockito.any(JsonNode.class), Mockito.any());

		try {
			addDocumentOperation.rpc(testDocumentModelName, jsonDocument, null);
			Assert.fail("Expected IntegrityException");
		} catch (IntegrityException e) {
			Assert.assertNotNull(e.getShortMessage());
			Assert.assertEquals(e.getShortMessage().getKey(), ExceptionKeys.ADD_DOCUMENT_ERROR_KEY);
			Assert.assertEquals(e.getShortMessage().getDefaultMessage(), "Could not create document");

			Assert.assertNotNull(e.getLongMessage());
			Assert.assertEquals(e.getLongMessage().getKey(), ExceptionKeys.ADD_DOCUMENT_ERROR_KEY);
			Assert.assertEquals(e.getLongMessage().getDefaultMessage(), "Could not create document");
		}
	}

	@Test(description = "Should throw RpcException with access denied code when service denies permission")
	public void shouldThrowAccessDeniedWhenNoCreatePermission() {
		Mockito.doThrow(new AccessDeniedException("Access Denied"))
			.when(documentService)
			.create(Mockito.anyString(), Mockito.any(JsonNode.class), Mockito.any());

		try {
			addDocumentOperation.rpc(testDocumentModelName, jsonDocument, null);
			fail("Expected RpcException");
		} catch (RpcException e) {
			Mockito.verify(documentService, Mockito.times(1)).create(testDocumentModelName, jsonDocument, null);
		}
	}
}
