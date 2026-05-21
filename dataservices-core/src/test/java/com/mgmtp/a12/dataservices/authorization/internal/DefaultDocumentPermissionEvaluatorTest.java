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
package com.mgmtp.a12.dataservices.authorization.internal;

import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.authorization.AbstractCorePermissionTest;
import com.mgmtp.a12.dataservices.authorization.AuthConstants;
import com.mgmtp.a12.model.header.Header;

public class DefaultDocumentPermissionEvaluatorTest extends AbstractCorePermissionTest {

	@InjectMocks DefaultDocumentPermissionEvaluator documentPermissionEvaluator;
	@BeforeMethod
	void beforeMethod() {
		super.initData();
	}

	@Test(dataProvider = "permissionResultCheck")
	void testCheckDocumentCreate_success(boolean result) {
		callCheckPermission(
			() -> {
				documentPermissionEvaluator.checkDocumentCreatePermission(document);
				return null;
			},
			result,
			AuthConstants.ACCESS_DENIED
		);
		Mockito.verify(authorizationService, Mockito.times(1)).checkPermissions(document, AuthConstants.DOCUMENT_CREATE_PERMISSION);
	}

	@Test(dataProvider = "permissionResultCheck")
	void testCheckDocumentPartialUpdatePermission_success(boolean result) {
		callCheckPermission(
			() -> {
				documentPermissionEvaluator.checkDocumentPartialUpdatePermission(oldDocument, document, documentRef);
				return null;
			},
			result,
			AuthConstants.ACCESS_DENIED
		);
		verifyOldNewDocumentAuthorizationCheck(AuthConstants.DOCUMENT_PARTIAL_UPDATE_PERMISSION);
	}

	@Test(dataProvider = "permissionResultCheck")
	void testCheckDocumentUpdatePermission_success(boolean result) {
		callCheckPermission(
			() -> {
				documentPermissionEvaluator.checkDocumentUpdatePermission(oldDocument, document, documentRef);
				return null;
			},
			result,
			AuthConstants.ACCESS_DENIED
		);
		verifyOldNewDocumentAuthorizationCheck(AuthConstants.DOCUMENT_UPDATE_PERMISSION);
	}

	@Test(dataProvider = "permissionResultCheck")
	void testCheckDocumentDeletePermission_success(boolean result) {
		callCheckPermission(
			() -> {
				documentPermissionEvaluator.checkDocumentDeletePermission(dataServicesDocument);
				return null;
			},
			result,
			AuthConstants.ACCESS_DENIED
		);
		verifyDataServiceDocumentWithDocRefCheckPermission(AuthConstants.DOCUMENT_DELETE_PERMISSION);
	}

	@Test(dataProvider = "permissionResultCheck")
	void testCheckDocumentMultiDeletePermission_success(boolean result) {
		List<Header> headers = List.of(
			makeTestModelHeader(),
			makeTestModelHeader(),
			makeTestModelHeader()
		);
		callCheckPermission(
			() -> {
				documentPermissionEvaluator.checkDocumentMultiDeletePermission(headers);
				return null;
			},
			result,
			"No Document Multi Delete permission"
		);
		Mockito.verify(authorizationService, Mockito.times(1)).checkPermissions(
			Mockito.argThat(((List<Header> args) -> {
				Assert.assertEquals(args.size(), 3);
				Assert.assertEquals(args.get(0), headers.get(0));
				Assert.assertEquals(args.get(1), headers.get(1));
				Assert.assertEquals(args.get(2), headers.get(2));
				return true;
			})),
			Mockito.eq(AuthConstants.DOCUMENT_MULTI_DELETE_PERMISSION)
		);
	}

	@Test(dataProvider = "permissionResultCheck")
	void testHasDocumentCreatePermission_success(boolean result) {
		callHasPermission(
			() -> documentPermissionEvaluator.hasDocumentCreatePermission(document),
			result
		);
		Mockito.verify(authorizationService, Mockito.times(1)).checkPermissions(document, AuthConstants.DOCUMENT_CREATE_PERMISSION);
	}

	@Test(dataProvider = "permissionResultCheck")
	void testHasExportListCDDPermission_success(boolean result) {
		callHasPermission(
			() -> documentPermissionEvaluator.hasExportListCDDPermission(),
			result
		);
		Mockito.verify(authorizationService, Mockito.times(1)).checkPermissions(null, AuthConstants.EXPORT_LIST_CDD_PERMISSION);

	}

	@Test(dataProvider = "permissionResultCheck")
	void testCheckExportListCDDPermission_success(boolean result) {
		callCheckPermission(
			() -> {
				documentPermissionEvaluator.checkExportListCDDPermission();
				return null;
			},
			result,
			AuthConstants.ACCESS_DENIED
		);
		Mockito.verify(authorizationService, Mockito.times(1)).checkPermissions(null, AuthConstants.EXPORT_LIST_CDD_PERMISSION);
	}

	@Test(dataProvider = "permissionResultCheck")
	void testCheckDocumentQueryPermission_success(boolean result) {
		callCheckPermission(
			() -> {
				// No need for a real model resource here because we are checking only the correct call of `AuthorizationService`.
				documentPermissionEvaluator.checkDocumentQueryPermission("someModel");
				return null;
			},
			result,
			AuthConstants.ACCESS_DENIED
		);
		Mockito.verify(authorizationService, Mockito.times(1)).checkPermissions("someModel", AuthConstants.DOCUMENT_QUERY_PERMISSION);
	}
}
