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
package com.mgmtp.a12.examples.query;

import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.QueryService;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Listeners(MockitoTestNGListener.class)
public class GermanBusinessPartnersOperationTest {

	@Mock
	private QueryService queryService;

	@InjectMocks
	private GermanBusinessPartnersOperation operation;

	@Mock
	private QueryRoot queryRoot;

	@Mock
	private DocumentTreeResult result1, result2;

	@Mock
	private QueryPage<Object> queryPage;

	@BeforeMethod
	public void setUp() {
		when(queryPage.getContent()).thenReturn(List.of(result1, result2));
		when(queryService.query(any(), isNull())).thenReturn(queryPage);
	}

	@Test
	public void rpc_appliesGermanBusinessPartnersConstraint_returnsResults() {
		List<DocumentTreeResult> results = operation.rpc(queryRoot);

		verify(queryRoot).setConstraint(any());
		verify(queryService).query(eq(queryRoot), isNull());
		assertEquals(results.size(), 2);
		assertTrue(results.contains(result1));
		assertTrue(results.contains(result2));
	}
}
