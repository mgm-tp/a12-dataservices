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
package com.mgmtp.a12.dataservices.server.internal.rest;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.springframework.security.access.AccessDeniedException;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.authorization.DocumentPermissionEvaluator;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.query.QueryService;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.testng.Assert.assertThrows;

/**
 * Unit tests for {@link AggregationController}.
 */
@Listeners(MockitoTestNGListener.class)
@Test
public class AggregationControllerTest {

	@Mock
	private QueryService queryService;

	@Mock
	private DataServicesCoreProperties dataServicesCoreProperties;

	@Mock
	private DocumentPermissionEvaluator documentPermissionEvaluator;

	@InjectMocks
	private AggregationController aggregationController;

	@Test(description = "Should throw AccessDeniedException when user lacks query permission")
	public void shouldThrowAccessDeniedWhenUserLacksQueryPermission() {
		// Given
		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel("Contract")
			.build();

		doThrow(new AccessDeniedException("Access Denied"))
			.when(documentPermissionEvaluator).checkDocumentQueryPermission("Contract");

		// When / Then
		assertThrows(AccessDeniedException.class, () -> aggregationController.loadAggregations(queryRoot));

		verify(documentPermissionEvaluator).checkDocumentQueryPermission("Contract");
		verifyNoInteractions(queryService);
	}
}
