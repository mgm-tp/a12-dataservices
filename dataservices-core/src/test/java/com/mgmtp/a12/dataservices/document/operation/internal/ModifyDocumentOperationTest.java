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

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.rpc.RpcException;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

@Listeners(MockitoTestNGListener.class)
public class ModifyDocumentOperationTest {

	@InjectMocks ModifyDocumentOperation modifyDocumentOperation;

	@Mock DocumentService documentService;
	@Mock DocumentSupport documentSupport;
	@Mock Anonymizer anonymizer;

	private final DocumentReference documentReference = new DocumentReference("BusinessPartner", "1");
	private final ObjectMapper objectMapper = new ObjectMapper();
	private ObjectNode jsonDocument;

	@BeforeMethod
	public void setUp() {
		jsonDocument = objectMapper.createObjectNode();
		jsonDocument.put("name", "John Doe");
	}

	@Test public void modifyDocument_success() {
		Mockito.when(documentSupport.convertJSONToDocument(Mockito.anyString(), Mockito.any(tools.jackson.databind.JsonNode.class), Mockito.any(DocumentReference.class)))
			.thenReturn(DocumentV2.empty("model"));

		modifyDocumentOperation.rpc(documentReference.toString(), jsonDocument, null);

		Mockito.verify(documentSupport, Mockito.times(1)).convertJSONToDocument(Mockito.anyString(), Mockito.any(tools.jackson.databind.JsonNode.class), Mockito.any(DocumentReference.class));
		Mockito.verify(documentService, Mockito.times(1)).update(Mockito.any(), Mockito.any(DocumentV2.class), Mockito.any());
	}

	@Test public void modifyDocument_documentNotFound() {
		Mockito.when(documentSupport.convertJSONToDocument(Mockito.anyString(), Mockito.any(tools.jackson.databind.JsonNode.class), Mockito.any(DocumentReference.class)))
			.thenReturn(DocumentV2.empty("model"));
		Mockito.doThrow(NotFoundException.class)
			.when(documentService)
			.update(Mockito.any(), Mockito.any(DocumentV2.class), Mockito.any());

		try {
			modifyDocumentOperation.rpc(documentReference.toString(), jsonDocument, null);
			fail("Expected RpcException");
		} catch (RpcException e) {
			assertEquals(e.getOperationError().getOperationId(), RemoteOperation.RemoteOperationHelper.getOperationId(ModifyDocumentOperation.class));
		} catch (Exception e) {
			fail("Expected RpcException");
		}
	}
}
