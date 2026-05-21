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

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

@Listeners(MockitoTestNGListener.class)
public class BusinessPartnerTaxAuthorityRegistrationStatusNoOpTest {

	@InjectMocks
	private BusinessPartnerTaxAuthorityRegistrationStatusNoOp projection;

	@Mock
	private QueryRoot queryRoot;

	@Mock
	private QueryContext queryContext;

	private DocumentTreeResult r1;
	private DocumentTreeResult r2;
	private Page<DocumentTreeResult> page;

	@BeforeMethod
	public void setup() {
		r1 = DocumentTreeResult.builder()
			.docRef(new DocumentReference("businessPartner", UUID.randomUUID().toString()))
			.type(DocumentTreeNodeType.ROOT)
			.build();
		r2 = DocumentTreeResult.builder()
			.docRef(new DocumentReference("businessPartner", UUID.randomUUID().toString()))
			.type(DocumentTreeNodeType.ROOT)
			.build();
		page = new PageImpl<>(List.of(r1, r2));
	}

	@Test
	public void preprocess_returnsSameInstance_success() {
		QueryRoot result = projection.preprocess(queryRoot, queryContext);
		assertSame(result, queryRoot);
		verifyNoInteractions(queryContext);
	}

	@Test
	public void postprocess_emptyPage_emptyResult() {
		Page<DocumentTreeResult> empty = new PageImpl<>(List.of());
		QueryPage<DocumentTreeResult> qp = projection.postprocess(queryRoot, empty, queryContext);

		assertTrue(qp.getContent().isEmpty());
		assertEquals(qp.getTotalElements(), 0);
		assertEquals(qp.getSize(), empty.getSize());
		assertEquals(qp.getNumber(), empty.getNumber());
		verifyNoInteractions(queryRoot, queryContext);
	}
}
