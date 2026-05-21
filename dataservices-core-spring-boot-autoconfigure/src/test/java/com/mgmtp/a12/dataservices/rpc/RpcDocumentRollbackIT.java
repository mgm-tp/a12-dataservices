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
package com.mgmtp.a12.dataservices.rpc;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.projection.internal.DocumentProjectionImplementation;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.SearchConstants.EN_LOCALE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@WithUserDetails(value = UserConstants.ADMIN_USER, setupBefore = TestExecutionEvent.TEST_EXECUTION)
@TestExecutionListeners(listeners = { TransactionalTestExecutionListener.class })
public class RpcDocumentRollbackIT extends AbstractSpringContextIT {

	private static final String BUSINESS_PARTNER_NAME_PATH = "/BusinessPartnerRoot/Name";
	private static final String META_DOC_REF_PATH = "/__meta/docRef";
	private DataServicesDocument businessPartner1;
	private DataServicesDocument businessPartner2;

	@BeforeClass
	public void setUp() throws Exception {
		setUserTo(UserConstants.ADMIN_USER);
		modelsFunctions.createModel(PathConstants.CAMPAIGN_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH);

		businessPartner1 = createDocument(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
			PathConstants.RPC_DOCUMENTS_PATH + "BusinessPartner-1.json");
		businessPartner2 = createDocument(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
			PathConstants.RPC_DOCUMENTS_PATH + "BusinessPartner-1.json");
	}

	@Test
	public void rollbackCreationOfDocuments() throws IOException {
		int numberOfBusinessPartnersInRepo = documentJpaRepository.findByModelName(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL).size();
		int numberOfBusinessPartnersInIndex = getNumberOfBusinessPartnersFromIndex();

		assertEquals(numberOfBusinessPartnersInIndex, numberOfBusinessPartnersInRepo);
		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "document_rollbacks/request_add_documents_fail.json");

		List<JsonRpc2Response> responses = sendRpcRequest(request);
		assertTrue(responses.stream().anyMatch(e -> !e.isSuccess()));

		int numberOfBusinessPartnersInRepoAfter = documentJpaRepository.findByModelName(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL).size();
		int numberOfBusinessPartnersInIndexAfter = getNumberOfBusinessPartnersFromIndex();

		assertEquals(numberOfBusinessPartnersInRepoAfter, numberOfBusinessPartnersInRepo);
		assertEquals(numberOfBusinessPartnersInIndexAfter, numberOfBusinessPartnersInIndex);
	}

	@Test
	public void rollbackModificationOfDocuments() throws IOException {
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL)
			.constraint(ExactMatchOperator.builder()
				.field(BUSINESS_PARTNER_NAME_PATH)
				.value("DuckDuuuck")
				.build())
			.paging(Paging.builder()
				.pageNumber(0)
				.pageSize(100)
				.build())
			.build();

		int numberOfBusinessPartnersInRepo = documentJpaRepository.findByModelName(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL).size();
		int numberOfBusinessPartnersInIndex = getNumberOfBusinessPartnersFromIndex();
		QueryPage<DocumentTreeResult> result = queryService.query(queryRoot, EN_LOCALE);
		assertEquals(result.getTotalElements(), 2);
		assertTrue(result.getContent().stream().anyMatch(documentTreeResult -> documentTreeResult.getDocRef().equals(businessPartner2.getMetadata().getDocRef())));
		assertTrue(result.getContent().stream().anyMatch(documentTreeResult -> documentTreeResult.getDocRef().equals(businessPartner1.getMetadata().getDocRef())));
		assertEquals(numberOfBusinessPartnersInIndex, numberOfBusinessPartnersInRepo);

		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "document_rollbacks/request_modify_documents_fail.json");
		String partner2 = loadResourceFromClasspathAsString(PathConstants.RPC_DOCUMENTS_PATH + "BusinessPartner-2.json");
		request = String.format(request, businessPartner1.getMetadata().getDocRef(), partner2);

		List<JsonRpc2Response> responses = sendRpcRequest(request);
		assertTrue(responses.stream().anyMatch(e -> !e.isSuccess()));

		int numberOfBusinessPartnersInRepoAfter = documentJpaRepository.findByModelName(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL).size();
		int numberOfBusinessPartnersInIndexAfter = getNumberOfBusinessPartnersFromIndex();
		assertEquals(numberOfBusinessPartnersInRepoAfter, numberOfBusinessPartnersInRepo);
		assertEquals(numberOfBusinessPartnersInIndexAfter, numberOfBusinessPartnersInIndex);

		Optional<DataServicesDocument> partner1After = documentRepository.findByDocumentReference(businessPartner1.getMetadata().getDocRef());
		Optional<DataServicesDocument> partner2After = documentRepository.findByDocumentReference(businessPartner2.getMetadata().getDocRef());
		assertTrue(partner1After.isPresent());
		assertTrue(partner2After.isPresent());
		JSONAssert.assertEquals(documentFunctions.convertDocumentToJson(businessPartner1), documentFunctions.convertDocumentToJson(partner1After.get()), false);
		JSONAssert.assertEquals(documentFunctions.convertDocumentToJson(businessPartner2), documentFunctions.convertDocumentToJson(partner2After.get()), false);

		result = queryService.query(queryRoot, EN_LOCALE);
		assertEquals(result.getTotalElements(), 2);
		assertTrue(result.getContent().stream().anyMatch(documentTreeResult -> documentTreeResult.getDocRef().equals(businessPartner2.getMetadata().getDocRef())));
		assertTrue(result.getContent().stream().anyMatch(documentTreeResult -> documentTreeResult.getDocRef().equals(businessPartner1.getMetadata().getDocRef())));
	}

	@Test
	public void rollbackDeletionOfDocuments() throws IOException {

		int numberOfBusinessPartnersInRepo = documentJpaRepository.findByModelName(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL).size();
		int numberOfBusinessPartnersInIndex = getNumberOfBusinessPartnersFromIndex();

		assertEquals(numberOfBusinessPartnersInIndex, numberOfBusinessPartnersInRepo);

		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "document_rollbacks/request_delete_documents_fail.json");

		request = String.format(
			request,
			DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, businessPartner1.getMetadata().getDocRef().getDocumentId(),
			DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, businessPartner2.getMetadata().getDocRef().getDocumentId()
		);

		List<JsonRpc2Response> responses = sendRpcRequest(request);
		assertTrue(responses.stream().anyMatch(e -> !e.isSuccess()), "Response should be faulty hence one fail be found!");

		int numberOfBusinessPartnersInRepoAfter = documentJpaRepository.findByModelName(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL).size();
		int numberOfBusinessPartnersInIndexAfter = getNumberOfBusinessPartnersFromIndex();
		assertEquals(numberOfBusinessPartnersInRepoAfter, numberOfBusinessPartnersInRepo);
		assertEquals(numberOfBusinessPartnersInIndexAfter, numberOfBusinessPartnersInIndex);
		assertNotNull(documentJpaRepository.findById(businessPartner1.getMetadata().getDocRef().getDocumentId()));
		assertNotNull(documentJpaRepository.findById(businessPartner2.getMetadata().getDocRef().getDocumentId()));
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL)
			.constraint(ExactMatchOperator.builder()
				.field(META_DOC_REF_PATH)
				.value(businessPartner1.getMetadata().getDocRef())
				.build())
			.paging(Paging.builder()
				.pageNumber(0)
				.pageSize(100)
				.build())
			.build();
		QueryPage<DocumentTreeResult> result = queryService.query(queryRoot, EN_LOCALE);
		int partner1Count = (int) result.getTotalElements();
		queryRoot.setConstraint(ExactMatchOperator.builder()
			.field(META_DOC_REF_PATH)
			.value(businessPartner2.getMetadata().getDocRef())
			.build());
		result = queryService.query(queryRoot, EN_LOCALE);
		int partner2Count = (int) result.getTotalElements();
		assertEquals(partner1Count, 1);
		assertEquals(partner2Count, 1);
	}

	@Test
	public void rollbackCreationAndDeletionOfDocuments() throws IOException {
		int numberOfBusinessPartnersInRepo = documentJpaRepository.findByModelName(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL).size();
		int numberOfBusinessPartnersInIndex = getNumberOfBusinessPartnersFromIndex();

		assertEquals(numberOfBusinessPartnersInIndex, numberOfBusinessPartnersInRepo);

		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "document_rollbacks/request_add_delete_document_fail.json");

		request = String.format(
			request,
			businessPartner1.getMetadata().getDocRef()
		);

		List<JsonRpc2Response> responses = sendRpcRequest(request);
		assertTrue(responses.stream().anyMatch(e -> !e.isSuccess()));

		int numberOfCampaignsInRepoAfter = documentJpaRepository.findByModelName(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL).size();
		int numberOfBusinessPartnersInIndexAfter = getNumberOfBusinessPartnersFromIndex();
		assertEquals(numberOfCampaignsInRepoAfter, numberOfBusinessPartnersInRepo);
		assertEquals(numberOfBusinessPartnersInIndexAfter, numberOfBusinessPartnersInIndex);
		assertNotNull(documentJpaRepository.findById(businessPartner1.getMetadata().getDocRef().getDocumentId()));
		assertNotNull(documentJpaRepository.findById(businessPartner2.getMetadata().getDocRef().getDocumentId()));
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL)
			.constraint(ExactMatchOperator.builder()
				.field(META_DOC_REF_PATH)
				.value(businessPartner1.getMetadata().getDocRef())
				.build())
			.paging(Paging.builder()
				.pageNumber(0)
				.pageSize(100)
				.build())
			.build();
		QueryPage<DocumentTreeResult> result = queryService.query(queryRoot, EN_LOCALE);
		int partner1Count = (int) result.getTotalElements();
		queryRoot.setConstraint(ExactMatchOperator.builder()
			.field(META_DOC_REF_PATH)
			.value(businessPartner2.getMetadata().getDocRef())
			.build());
		result = queryService.query(queryRoot, EN_LOCALE);
		int partner2Count = (int) result.getTotalElements();
		assertEquals(partner1Count, 1);
		assertEquals(partner2Count, 1);
	}

	@Test
	public void rollbackCacheCleaning() throws IOException {
		int numberOfBusinessPartnersInRepo = documentJpaRepository.findByModelName(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL).size();
		int numberOfBusinessPartnersInIndex = getNumberOfBusinessPartnersFromIndex();

		assertEquals(numberOfBusinessPartnersInIndex, numberOfBusinessPartnersInRepo);

		// Successful RPC request should create 2 new documents
		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "document_rollbacks/request_add_2_documents.json");
		List<JsonRpc2Response> responses = sendRpcRequest(request);
		assertFalse(responses.stream().anyMatch(e -> !e.isSuccess()), "Response should be correct and without fail!");
		// 2 new documents have been created in repo and in index
		numberOfBusinessPartnersInRepo += 2;
		numberOfBusinessPartnersInIndex += 2;
		assertEquals(numberOfBusinessPartnersInRepo, documentJpaRepository.findByModelName(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL).size());
		assertEquals(numberOfBusinessPartnersInIndex, getNumberOfBusinessPartnersFromIndex());

		// Unsuccessful RPC request should rollback creation of documents in the current request but no other
		request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "document_rollbacks/request_add_documents_fail.json");
		responses = sendRpcRequest(request);
		assertTrue(responses.stream().anyMatch(e -> !e.isSuccess()), "Response should be faulty hence one fail be found!");

		int numberOfBusinessPartnersInRepoAfter = documentJpaRepository.findByModelName(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL).size();
		int numberOfBusinessPartnersInIndexAfter = getNumberOfBusinessPartnersFromIndex();
		assertEquals(numberOfBusinessPartnersInRepoAfter, numberOfBusinessPartnersInRepo);
		assertEquals(numberOfBusinessPartnersInIndexAfter, numberOfBusinessPartnersInIndex);
	}

	private int getNumberOfBusinessPartnersFromIndex() {
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL)
			.paging(Paging.builder()
				.pageNumber(0)
				.pageSize(100)
				.build())
			.build();
		QueryPage<DocumentTreeResult> result = queryService.query(queryRoot, EN_LOCALE);
		return (int) result.getTotalElements();
	}

}
