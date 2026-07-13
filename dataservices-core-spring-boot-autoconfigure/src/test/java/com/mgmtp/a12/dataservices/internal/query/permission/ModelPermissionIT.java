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
package com.mgmtp.a12.dataservices.internal.query.permission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.exception.query.QueryValidationException;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.QueryService;
import com.mgmtp.a12.dataservices.query.projection.internal.DocumentProjectionImplementation;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ModelPermissionIT extends AbstractSpringContextIT {

	@Autowired QueryService queryService;

	@BeforeMethod
	public void init() throws Exception {
		super.cleanUpTestEnvironment();
		modelsFunctions.createModel("model/query/permission/BusinessPartner-admin-only.json");
		modelsFunctions.createModel("model/query/permission/BusinessPartnerLTD-admin-only.json");
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel("model/query/permission/BusinessPartnerSuper-admin-only.json");
	}

	@AfterMethod
	public void cleanUp() {
		setUserTo(UserConstants.ADMIN_USER);
	}

	@Test
	public void testAbstractModelWithNoPermissionOnSubtypes() {
		createDocument("BusinessPartner", "document/query/permission/BusinessPartner-1.json");
		createDocument("BusinessPartnerLTD", "document/query/permission/BusinessPartner-2.json");

		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel("BusinessPartnerSuper")
			.paging(Paging.builder()
				.pageSize(10)
				.pageNumber(0)
				.build())
			.build();
		QueryPage<Object> queryResultPageForAdmin = queryService.query(queryRoot, null);
		assertNotNull(queryResultPageForAdmin);
		assertEquals(queryResultPageForAdmin.getContent().stream().toList().size(), 2);

		// guest user does not have access right to model BusinessPartnerSuper nor BusinessPartnerSuperLTD
		setUserTo(UserConstants.GUEST_USER);
		QueryPage<Object> queryResultPage = queryService.query(queryRoot, null);
		assertNotNull(queryResultPage);
		assertEquals(queryResultPage.getContent().stream().toList().size(), 0);
	}

	@Test(expectedExceptions = AccessDeniedException.class, expectedExceptionsMessageRegExp = ".*Access Denied.*")
	public void testNoPermissionToTargetDocumentModel() {
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel("BusinessPartnerSuperAdminOnly")
			.paging(Paging.builder()
				.pageSize(10)
				.pageNumber(0)
				.build())
			.build();
		// guest user does not have access right to model BusinessPartnerSuperAdminOnly
		setUserTo(UserConstants.GUEST_USER);
		queryService.query(queryRoot, null);
	}

	@Test(expectedExceptions = QueryValidationException.class, expectedExceptionsMessageRegExp = ".*Target document model \\[BusinessPartnerNotFound] not found.*")
	public void testNoPermissionUnknownModel() {
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel("BusinessPartnerNotFound")
			.paging(Paging.builder()
				.pageSize(10)
				.pageNumber(0)
				.build())
			.build();
		queryService.query(queryRoot, null);
	}
}
