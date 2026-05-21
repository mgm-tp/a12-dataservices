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
package com.mgmtp.a12.dataservices.model;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.InitialITConfiguration;
import com.mgmtp.a12.dataservices.authorization.internal.DefaultModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.authorization.internal.UaaConnector;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;

import static com.mgmtp.a12.dataservices.authorization.AuthConstants.MODEL_READ_PERMISSION;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.ADDRESS_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.PathConstants.ADDRESS_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.UserConstants.ADMIN_USER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest(classes = { InitialITConfiguration.class })
public class DefaultModelPermissionEvaluatorCachesIT extends AbstractSpringContextIT {

	private String previousUserName;

	@MockitoSpyBean private AuthorizationService authorizationServiceSpy;
	@Autowired private DefaultModelPermissionEvaluator permissionEvaluatorWithSpy;
	@Autowired private ModelService modelService;

	@BeforeMethod public void setUp() {
		setUserTo(UserConstants.ADMIN_USER);
		modelCacheManager.invalidateSecuredModelReadCaches(BUSINESS_PARTNER_DOCUMENT_MODEL);
		modelCacheManager.invalidateSecuredModelReadCaches(ADDRESS_DOCUMENT_MODEL);
		modelsFunctions.createModel(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(ADDRESS_DOCUMENT_MODEL_PATH);
		previousUserName = UaaConnector.getCurrentUserName();
	}

	@AfterMethod public void tearDown() {
		setUserTo(previousUserName);
		modelService.delete(BUSINESS_PARTNER_DOCUMENT_MODEL);
		modelService.delete(ADDRESS_DOCUMENT_MODEL);
	}

	@Test public void testCacheHitAfterFirstCall() {
		assertUserReadPermissionToModel(permissionEvaluatorWithSpy, "test", BUSINESS_PARTNER_DOCUMENT_MODEL, true);
		assertUserReadPermissionToModel(permissionEvaluatorWithSpy, "test", BUSINESS_PARTNER_DOCUMENT_MODEL, true);
		assertUserReadPermissionToModel(permissionEvaluatorWithSpy, "test", BUSINESS_PARTNER_DOCUMENT_MODEL, true);

		verifySpyTimesCalled(1);
	}

	@Test public void testCacheHappensPerUser() {
		assertUserReadPermissionToModel(permissionEvaluatorWithSpy, "test", BUSINESS_PARTNER_DOCUMENT_MODEL, true);
		assertUserReadPermissionToModel(permissionEvaluatorWithSpy, ADMIN_USER, BUSINESS_PARTNER_DOCUMENT_MODEL, true);

		verifySpyTimesCalled(2);
	}

	@Test public void testCachingOrder() {
		assertUserReadPermissionToModel(permissionEvaluatorWithSpy, TEST_USER_WITH_NO_ACCESS_RIGHTS, BUSINESS_PARTNER_DOCUMENT_MODEL,
			false);
		assertUserReadPermissionToModel(permissionEvaluatorWithSpy, ADMIN_USER, BUSINESS_PARTNER_DOCUMENT_MODEL, true);
		verifySpyTimesCalled(2);

		modelCacheManager.invalidateSecuredModelReadCaches(BUSINESS_PARTNER_DOCUMENT_MODEL);

		assertUserReadPermissionToModel(permissionEvaluatorWithSpy, ADMIN_USER, BUSINESS_PARTNER_DOCUMENT_MODEL, true);
		assertUserReadPermissionToModel(permissionEvaluatorWithSpy, TEST_USER_WITH_NO_ACCESS_RIGHTS, BUSINESS_PARTNER_DOCUMENT_MODEL,
			false);
		verifySpyTimesCalled(4);
	}

	@Test public void testCacheHappensPerModel() {
		assertUserReadPermissionToModel(permissionEvaluatorWithSpy, "test", BUSINESS_PARTNER_DOCUMENT_MODEL, true);
		assertUserReadPermissionToModel(permissionEvaluatorWithSpy, "test", ADDRESS_DOCUMENT_MODEL, true);

		verifySpyTimesCalled(2);
	}

	@Test public void testCacheMissAfterModelUpdate() {
		assertUserReadPermissionToModel(permissionEvaluatorWithSpy, "test", BUSINESS_PARTNER_DOCUMENT_MODEL, true);
		modelsFunctions.updateModel(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
		assertUserReadPermissionToModel(permissionEvaluatorWithSpy, "test", BUSINESS_PARTNER_DOCUMENT_MODEL, true);

		verifySpyTimesCalled(2);
	}

	private void verifySpyTimesCalled(int numberOfCalls) {
		Mockito.verify(authorizationServiceSpy, Mockito.times(numberOfCalls))
			.checkPermissions(any(Header.class), eq(MODEL_READ_PERMISSION));
	}

}
