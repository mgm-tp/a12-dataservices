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
package com.mgmtp.a12.dataservices.authorization;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;
import com.mgmtp.a12.uaa.authorization.model.Permission;
import com.mgmtp.a12.uaa.authorization.security.PermissionCheckResult;

public abstract class AbstractCorePermissionTest extends AbstractDataServicesCoreTest {
	@Mock protected AuthorizationService authorizationService;

	protected DocumentV2 document;
	protected DataServicesDocument dataServicesDocument;
	protected DocumentV2 oldDocument;
	protected DocumentReference documentRef;

	protected Header header;
	protected IDocumentModel iDocumentModel;

	public void initData() {
		Mockito.reset(authorizationService);
		document = Mockito.mock(DocumentV2.class);
		oldDocument = Mockito.mock(DocumentV2.class);
		dataServicesDocument = makeTestDsDocument();
		documentRef = dataServicesDocument.getMetadata().getDocRef();
		iDocumentModel = mockModel(RandomStringUtils.randomAlphabetic(10));
		header = iDocumentModel.getHeader();
	}

	@DataProvider public Object[][] permissionResultCheck() {
		return new Object[][] {
			new Object[] { true },
			new Object[] { false },
		};
	}

	public PermissionCheckResult<Permission> createPermissionCheckResult(boolean result) {
		return new PermissionCheckResult.Builder<Permission>(result, Collections.emptyList()).build();
	}


	public void callCheckPermission(Supplier func, boolean result, String expectedError) {
		Mockito.lenient().when(authorizationService.checkPermissions(Mockito.any(), Mockito.any()))
			.thenReturn(createPermissionCheckResult(result));
		Mockito.lenient().when(authorizationService.checkPermissions(Mockito.any(), Mockito.any(), Mockito.any()))
			.thenReturn(createPermissionCheckResult(result));
		try {
			func.get();
			Assert.assertTrue(result);
		} catch (AccessDeniedException e) {
			Assert.assertFalse(result);
			Assert.assertEquals(e.getMessage(), expectedError);
		}
	}

	public void callHasPermission(Supplier<Boolean> func, boolean expected) {
		Mockito.lenient().when(authorizationService.checkPermissions(Mockito.any(), Mockito.any()))
			.thenReturn(createPermissionCheckResult(expected));
		Mockito.lenient().when(authorizationService.checkPermissions(Mockito.any(), Mockito.any(), Mockito.any()))
			.thenReturn(createPermissionCheckResult(expected));
		boolean result = func.get();
		Assert.assertEquals(result, expected);
	}

	public void verifyOldNewDocumentAuthorizationCheck(String scopeName) {
		Mockito.verify(authorizationService, Mockito.times(1)).checkPermissions(
			Mockito.argThat((DocumentUpdateResource arg) -> {
				Assert.assertEquals(arg.getOldDocument(), oldDocument);
				Assert.assertEquals(arg.getNewDocument(), document);
				Assert.assertEquals(arg.getDocRef(), documentRef);
				return true;
			}),
			Mockito.eq(scopeName),
			Mockito.argThat((Map<String, Object> arg) -> documentRef.equals(arg.get("docRef")))
		);
	}

	public void verifyKernelDocumentCheckPermission(String scopeName) {
		Mockito.verify(authorizationService, Mockito.times(1)).checkPermissions(
			Mockito.argThat(((DataServicesDocument arg) -> {
				Assert.assertEquals(arg, dataServicesDocument);
				return true;
			})),
			Mockito.eq(scopeName)
		);
	}

	public void verifyDataServiceDocumentWithDocRefCheckPermission(String scopeName) {
		Mockito.verify(authorizationService, Mockito.times(1)).checkPermissions(
			Mockito.argThat(((DataServicesDocument arg) -> {
				Assert.assertEquals(arg, dataServicesDocument);
				return true;
			})),
			Mockito.eq(scopeName),
			Mockito.argThat(
				(Map<String, Object> arg) -> documentRef.equals(arg.get("docRef"))
			)
		);
	}
}
