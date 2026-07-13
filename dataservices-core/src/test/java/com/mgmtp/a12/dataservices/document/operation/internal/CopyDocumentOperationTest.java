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
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.springframework.security.access.AccessDeniedException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.exception.DocumentValidationException;
import com.mgmtp.a12.dataservices.document.internal.DefaultDataServicesDocument;
import com.mgmtp.a12.dataservices.document.internal.DefaultDocumentMetadata;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.rpc.RpcException;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

@Listeners(MockitoTestNGListener.class)
public class CopyDocumentOperationTest {

	@InjectMocks private CopyDocumentOperation copyDocumentOperation;

	@Mock private DocumentService documentService;
	@Mock private Anonymizer anonymizer;
	@Mock private DefaultDocumentMetadata documentMetadata;

	private AutoCloseable openMocks;
	private DataServicesDocument dataServicesDocument;
	@Mock private DocumentV2 kernelDocument;
	private final DocumentReference documentReference = new DocumentReference("BusinessPartner", "1");

	@BeforeMethod
	public void setUp() {
		openMocks = MockitoAnnotations.openMocks(documentMetadata);
		dataServicesDocument = new DefaultDataServicesDocument(kernelDocument, documentMetadata);
	}

	@AfterMethod
	public void tearDown() throws Exception {
		if (openMocks != null) {
			openMocks.close();
		}
	}

	@Test public void testCopyDocument_success() {
		Mockito.when(documentService.copy(documentReference, null)).thenReturn(dataServicesDocument);
		Mockito.when(documentMetadata.getDocRef()).thenReturn(documentReference);

		copyDocumentOperation.rpc(documentReference.toString(), null);

		Mockito.verify(documentService, Mockito.times(1)).copy(documentReference, null);
	}

	@Test(expectedExceptions = NotFoundException.class)
	public void testCopyDocument_documentNotFound() {
		Mockito.when(documentService.copy(documentReference, null))
			.thenThrow(new NotFoundException("document.not.found", "Document not found"));

		copyDocumentOperation.rpc(documentReference.toString(), null);
	}

	@Test
	public void testCopyDocument_baseException() {
		Mockito.doThrow(DocumentValidationException.class)
			.when(documentService)
			.copy(Mockito.any(), Mockito.any());

		try {
			copyDocumentOperation.rpc(documentReference.toString(), null);
			fail("Expected BaseException");
		} catch (BaseException e) {
			assertEquals(e.getShortMessage().getDefaultMessage(), "Could not copy document");
		} catch (Exception e) {
			fail("Expected BaseException");
		}
	}

	@Test
	public void testCopyDocument_unknownException() {
		Mockito.doThrow(RuntimeException.class)
			.when(documentService)
			.copy(Mockito.any(), Mockito.any());

		try {
			copyDocumentOperation.rpc(documentReference.toString(), null);
			fail("Expected RpcException");
		} catch (RpcException e) {
			assertEquals(e.getOperationError().getOperationId(), RemoteOperation.RemoteOperationHelper.getOperationId(CopyDocumentOperation.class));
		} catch (Exception e) {
			fail("Expected RpcException");
		}
	}

	@Test(description = "Should throw RpcException with access denied code when service denies permission")
	public void shouldThrowAccessDeniedWhenNoCreatePermission() {
		Mockito.doThrow(new AccessDeniedException("Access Denied"))
			.when(documentService)
			.copy(Mockito.any(), Mockito.any());

		try {
			copyDocumentOperation.rpc(documentReference.toString(), null);
			fail("Expected RpcException");
		} catch (RpcException e) {
			Mockito.verify(documentService, Mockito.times(1)).copy(documentReference, null);
		}
	}
}
