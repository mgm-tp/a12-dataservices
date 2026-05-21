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

import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.rpc.RpcException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

@Listeners(MockitoTestNGListener.class)
public class PartialModifyDocumentOperationTest {

	@InjectMocks PartialModifyDocumentOperation partialModifyDocumentOperation;

	@Mock DocumentService documentService;
	@Mock Anonymizer anonymizer;

	private final DocumentReference documentReference = new DocumentReference("BusinessPartner", "1");

	@Test public void testPartialModifyDocument_success() {
		partialModifyDocumentOperation.rpc(documentReference, List.of(), null);

		Mockito.verify(documentService, Mockito.times(1)).update(Mockito.any(), Mockito.any(List.class), Mockito.any());
	}

	@Test public void testPartialModifyDocument_documentNotFound() {
		Mockito.doThrow(NotFoundException.class)
			.when(documentService)
			.update(Mockito.any(), Mockito.any(List.class), Mockito.any());

		try {
			partialModifyDocumentOperation.rpc(documentReference, List.of(), null);
			fail("expected RpcException");
		} catch (RpcException e) {
			assertEquals(e.getOperationError().getOperationId(), RemoteOperation.RemoteOperationHelper.getOperationId(PartialModifyDocumentOperation.class));
		} catch (Exception e) {
			fail("expected RpcException");
		}
	}
}
