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
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

@Listeners(MockitoTestNGListener.class)
public class BusinessPartnerTaxAuthorityRegistrationStatusTest {

	@InjectMocks
	private BusinessPartnerTaxAuthorityRegistrationStatus projection;

	@Mock
	private QueryRoot queryRoot;

	@Mock
	private QueryContext queryContext;

	@Test
	public void preprocess_valid_returnsSameInstance_success() {
		when(queryRoot.getTargetDocumentModel()).thenReturn(BusinessPartnerTaxAuthorityRegistrationStatus.BUSINESS_PARTNER_DOCUMENT_MODEL_NAME);
		when(queryRoot.getLinks()).thenReturn(null);

		QueryRoot result = projection.preprocess(queryRoot, queryContext);

		assertSame(result, queryRoot);
	}

	@Test(expectedExceptions = QueryInvalidInputException.class)
	public void preprocess_invalidTargetModel_throwsQueryInvalidInputException() {
		when(queryRoot.getTargetDocumentModel()).thenReturn("OtherModel");
		when(queryRoot.getLinks()).thenReturn(null);

		projection.preprocess(queryRoot, queryContext);
	}

	@Test(expectedExceptions = QueryInvalidInputException.class)
	public void preprocess_withLinks_throwsQueryInvalidInputException() {
		when(queryRoot.getTargetDocumentModel()).thenReturn(BusinessPartnerTaxAuthorityRegistrationStatus.BUSINESS_PARTNER_DOCUMENT_MODEL_NAME);
		when(queryRoot.getLinks()).thenReturn(List.of(QueryLink.builder().build()));

		projection.preprocess(queryRoot, queryContext);
	}

	@Test
	public void postprocess_enrichesResults_addsLinks_keepsTotalElements() {
		// original documents
		DocumentTreeResult doc1 = DocumentTreeResult.builder()
			.docRef(new DocumentReference(BusinessPartnerTaxAuthorityRegistrationStatus.BUSINESS_PARTNER_DOCUMENT_MODEL_NAME, UUID.randomUUID().toString()))
			.type(DocumentTreeNodeType.ROOT)
			.build();
		DocumentTreeResult doc2 = DocumentTreeResult.builder()
			.docRef(new DocumentReference(BusinessPartnerTaxAuthorityRegistrationStatus.BUSINESS_PARTNER_DOCUMENT_MODEL_NAME, UUID.randomUUID().toString()))
			.type(DocumentTreeNodeType.ROOT)
			.build();

		Page<DocumentTreeResult> basePage = new PageImpl<>(List.of(doc1, doc2));

		QueryPage<DocumentTreeResult> enriched = projection.postprocess(queryRoot, basePage, queryContext);

		// content size doubled (original + generated link entries)
		assertEquals(enriched.getContent().size(), 4);
		// total elements unchanged (original page total)
		assertEquals(enriched.getTotalElements(), basePage.getTotalElements());

		// verify original docs still present
		assertTrue(enriched.getContent().contains(doc1));
		assertTrue(enriched.getContent().contains(doc2));

		// count link entries and validate their properties
		long linkCount = enriched.getContent().stream()
			.filter(r -> BusinessPartnerTaxAuthorityRegistrationStatus.MAPPED_DOCUMENTS_RELATIONSHIP_MODEL_NAME.equals(r.getRelationshipModel()))
			.count();
		assertEquals(linkCount, 2);

		enriched.getContent().stream()
			.filter(r -> BusinessPartnerTaxAuthorityRegistrationStatus.MAPPED_DOCUMENTS_RELATIONSHIP_MODEL_NAME.equals(r.getRelationshipModel()))
			.forEach(link -> {
				assertNotNull(link.getSourceDocRef());
				assertEquals(link.getType(), DocumentTreeNodeType.CHILD);
				assertEquals(link.getDocRef().getDocumentModelName(), BusinessPartnerTaxAuthorityRegistrationStatus.MAPPED_DOCUMENTS_DOCUMENT_MODEL_NAME);
			});
	}
}
