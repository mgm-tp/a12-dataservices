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
package com.mgmtp.a12.dataservices.rpc.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentSpec;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder.Direction;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder.NullHandling;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.RelationshipOrder;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.LinkPosition;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;

import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.CONTRACT_CDM_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.CONTRACT_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL;
import static com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants.CONTRACT_ROLE;
import static com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants.PARTNER_ROLE;

/**
 * Integration tests for relationship-based sorting in QUERY operation using CDD projection.
 *
 * These tests verify that `projectionName: "cdd"` queries return correctly sorted results when
 * ordering by fields of out-going to-1 link properties or target documents. They mirror the
 * structure of {@link RelationshipOrderIT} but target the CDD projection.
 */
@Slf4j
@Test
public class CddRelationshipOrderIT extends AbstractSpringContextIT {

	private final List<DocumentReference> testDocuments = new ArrayList<>();
	private final List<String> testLinks = new ArrayList<>();

	@Override
	protected void initializeWithSecurityBypass() throws Exception {
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.CONTRACT_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.ADDRESS_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.COINSURED_ADDITIONAL_PARTNER_DOCUMENT_MODEL_PATH);
		createModel(resourceFunctions.loadResource(PathConstants.CONTRACT_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH));
		createModel(resourceFunctions.loadResource(PathConstants.PARTNER_ADDRESSES_RELATIONSHIP_MODEL_PATH));
		createModel(resourceFunctions.loadResource(PathConstants.PARTNER_POSTAL_ADDRESS_RELATIONSHIP_MODEL_PATH));
		createModel(resourceFunctions.loadResource(PathConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH));
		modelsFunctions.saveCdms(PathConstants.CONTRACT_CDM_MODEL_PATH);
	}

	@BeforeMethod
	public void setUp() {
		log.info("Setting up test data for CddRelationshipOrderIT");
		testDocuments.clear();
		testLinks.clear();
	}

	@AfterMethod
	@Transactional
	public void tearDown() {
		log.info("Cleaning up test data: {} documents, {} links", testDocuments.size(), testLinks.size());

		for (DocumentReference docRef : testDocuments) {
			try {
				documentService.delete(docRef);
			} catch (Exception e) {
				log.warn("Failed to delete document {}: {}", docRef, e.getMessage());
			}
		}
		testDocuments.clear();
		testLinks.clear();
	}

	/**
	 * Verifies that a CDD projection query sorted by ContractBusinessPartner Partner.Name ASC NULLS_LAST
	 * returns results in alphabetical ascending order, with unlinked contracts appearing last.
	 *
	 * Test data: 4 contracts — three linked to partners named "Engine*", "First", "Second", one unlinked.
	 * Expected order: Engine* < First < Second < null.
	 */
	@Test(description = "Should sort CDDs by Partner.Name ASC with NULLS_LAST")
	@Transactional
	public void shouldSortCddByPartnerNameAscWhenNullsLast() throws IOException {
		// Given: 4 contracts, 3 linked to partners
		DocumentReference contract1 = createContract("Contract-alan.json");
		DocumentReference contract2 = createContract("Contract-beth.json");
		DocumentReference contract3 = createContract("Contract-karl.json");
		DocumentReference contract4 = createContract("Contract-theo.json");

		DocumentReference partner3 = createBusinessPartner("BusinessPartner-3.json"); // Name: "Engine*"
		DocumentReference partner1 = createBusinessPartner("BusinessPartner-1.json"); // Name: "First"
		DocumentReference partner2 = createBusinessPartner("BusinessPartner-2.json"); // Name: "Second"

		linkContractToPartner(contract1, partner3); // Engine*
		linkContractToPartner(contract2, partner1); // First
		linkContractToPartner(contract3, partner2); // Second
		// contract4 has no partner link (NULL case)

		// When: Query CDDs sorted by Partner.Name ASC NULLS_LAST
		RelationshipOrder relationshipOrder = new RelationshipOrder(
			CONTRACT_BUSINESS_PARTNER_MODEL,
			PARTNER_ROLE,
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.ASC, false, NullHandling.NULLS_LAST)
		);

		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(CONTRACT_CDM_MODEL)
			.projectionName("cdd")
			.sort(List.of(relationshipOrder))
			.paging(Paging.builder().pageNumber(0).pageSize(100).build())
			.build();
		PagedResultSet<DocumentSpec> result = queryOperation.rpc(queryRoot);

		// Then: CDD results should be ordered by partner Name (Engine*, First, Second, then NULL)
		Assert.assertNotNull(result);
		List<DocumentSpec> documents = result.getEntries();
		Assert.assertTrue(documents.size() >= 4, "Expected at least 4 CDD root entries");

		List<String> contractIds = extractRootContractIds(documents);

		int engineContractIndex = contractIds.indexOf(contract1.toString());   // Engine*
		int firstContractIndex = contractIds.indexOf(contract2.toString());    // First
		int secondContractIndex = contractIds.indexOf(contract3.toString());   // Second
		int nullContractIndex = contractIds.indexOf(contract4.toString());     // NULL

		Assert.assertTrue(engineContractIndex >= 0, "Engine* contract should be in results");
		Assert.assertTrue(firstContractIndex >= 0, "First contract should be in results");
		Assert.assertTrue(secondContractIndex >= 0, "Second contract should be in results");
		Assert.assertTrue(nullContractIndex >= 0, "NULL contract should be in results");

		Assert.assertTrue(engineContractIndex < firstContractIndex, "Engine* should come before First");
		Assert.assertTrue(firstContractIndex < secondContractIndex, "First should come before Second");
		Assert.assertTrue(secondContractIndex < nullContractIndex, "Second should come before NULL (contract4)");
	}

	/**
	 * Verifies that a CDD projection query sorted by ContractBusinessPartner Partner.Name DESC NULLS_FIRST
	 * returns results in reverse alphabetical order, with unlinked contracts appearing first.
	 *
	 * Test data: 4 contracts — three linked to partners named "First", "Second", "Engine*", one unlinked.
	 * Expected order: null < Second < First < Engine*.
	 */
	@Test(description = "Should sort CDDs by Partner.Name DESC with NULLS_FIRST")
	@Transactional
	public void shouldSortCddByPartnerNameDescWhenNullsFirst() throws IOException {
		// Given: 4 contracts, 3 with partners
		DocumentReference contract1 = createContract("Contract-alan.json");
		DocumentReference contract2 = createContract("Contract-beth.json");
		DocumentReference contract3 = createContract("Contract-karl.json");
		DocumentReference contract4 = createContract("Contract-theo.json");

		DocumentReference partner1 = createBusinessPartner("BusinessPartner-1.json"); // Name: "First"
		DocumentReference partner2 = createBusinessPartner("BusinessPartner-2.json"); // Name: "Second"
		DocumentReference partner3 = createBusinessPartner("BusinessPartner-3.json"); // Name: "Engine*"

		linkContractToPartner(contract1, partner1); // First
		linkContractToPartner(contract2, partner2); // Second
		linkContractToPartner(contract3, partner3); // Engine*
		// contract4 has no partner (NULL)

		// When: Query CDDs sorted by Partner.Name DESC NULLS_FIRST
		RelationshipOrder relationshipOrder = new RelationshipOrder(
			CONTRACT_BUSINESS_PARTNER_MODEL, PARTNER_ROLE,
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.DESC, false, NullHandling.NULLS_FIRST)
		);

		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(CONTRACT_CDM_MODEL)
			.projectionName("cdd")
			.sort(List.of(relationshipOrder))
			.paging(Paging.builder().pageNumber(0).pageSize(100).build())
			.build();
		PagedResultSet<DocumentSpec> result = queryOperation.rpc(queryRoot);

		// Then: CDD results ordered: NULL, Second, First, Engine*
		Assert.assertNotNull(result);
		List<DocumentSpec> documents = result.getEntries();
		Assert.assertTrue(documents.size() >= 4);

		List<String> contractIds = extractRootContractIds(documents);

		int nullIndex = contractIds.indexOf(contract4.toString());       // NULL
		int secondIndex = contractIds.indexOf(contract2.toString());     // Second
		int firstIndex = contractIds.indexOf(contract1.toString());      // First
		int engineIndex = contractIds.indexOf(contract3.toString());     // Engine*

		Assert.assertTrue(nullIndex >= 0 && secondIndex >= 0 && firstIndex >= 0 && engineIndex >= 0);
		Assert.assertTrue(nullIndex < secondIndex, "NULL should come first");
		Assert.assertTrue(secondIndex < firstIndex, "Second should come before First");
		Assert.assertTrue(firstIndex < engineIndex, "First should come before Engine*");
	}

	/**
	 * Verifies that NULLS_FIRST causes unlinked contracts (with no partner) to appear at the
	 * beginning of CDD projection results when sorted by Partner.Name ASC.
	 *
	 * Test data: 3 contracts — two linked to named partners, one unlinked.
	 * Expected: unlinked contract appears at position 0 (before linked ones).
	 */
	@Test(description = "Should handle NULLS_FIRST for CDD projection — null entries appear first")
	@Transactional
	public void shouldHandleNullsFirstInCddProjection() throws IOException {
		// Given: 3 contracts — 2 linked to named partners, 1 unlinked
		DocumentReference contractWithPartner1 = createContract("Contract-alan.json");
		DocumentReference contractWithPartner2 = createContract("Contract-beth.json");
		DocumentReference contractUnlinked = createContract("Contract-karl.json");

		DocumentReference partner1 = createBusinessPartner("BusinessPartner-1.json"); // Name: "First"
		DocumentReference partner2 = createBusinessPartner("BusinessPartner-2.json"); // Name: "Second"

		linkContractToPartner(contractWithPartner1, partner1);
		linkContractToPartner(contractWithPartner2, partner2);
		// contractUnlinked has no partner

		// When: Query CDDs sorted by Partner.Name ASC NULLS_FIRST
		RelationshipOrder relationshipOrder = new RelationshipOrder(
			CONTRACT_BUSINESS_PARTNER_MODEL, PARTNER_ROLE,
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.ASC, false, NullHandling.NULLS_FIRST)
		);

		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(CONTRACT_CDM_MODEL)
			.projectionName("cdd")
			.sort(List.of(relationshipOrder))
			.paging(Paging.builder().pageNumber(0).pageSize(100).build())
			.build();
		PagedResultSet<DocumentSpec> result = queryOperation.rpc(queryRoot);

		// Then: unlinked contract appears at position 0 (NULLS_FIRST)
		Assert.assertNotNull(result);
		List<DocumentSpec> documents = result.getEntries();
		Assert.assertTrue(documents.size() >= 3, "Expected at least 3 CDD root entries");

		List<String> contractIds = extractRootContractIds(documents);
		Assert.assertTrue(contractIds.size() >= 3);

		int unlinkedIndex = contractIds.indexOf(contractUnlinked.toString());
		int linkedIndex1 = contractIds.indexOf(contractWithPartner1.toString());
		int linkedIndex2 = contractIds.indexOf(contractWithPartner2.toString());

		Assert.assertTrue(unlinkedIndex >= 0, "Unlinked contract should be in results");
		Assert.assertTrue(linkedIndex1 >= 0, "First linked contract should be in results");
		Assert.assertTrue(linkedIndex2 >= 0, "Second linked contract should be in results");

		Assert.assertEquals(unlinkedIndex, 0, "Unlinked contract (null partner) should appear at position 0 with NULLS_FIRST");
		Assert.assertTrue(linkedIndex1 > unlinkedIndex, "Linked contracts should appear after the unlinked one");
		Assert.assertTrue(linkedIndex2 > unlinkedIndex, "Linked contracts should appear after the unlinked one");
	}

	/**
	 * Verifies that NULLS_LAST causes unlinked contracts (with no partner) to appear at the
	 * end of CDD projection results when sorted by Partner.Name ASC.
	 *
	 * Test data: 3 contracts — two linked to named partners, one unlinked.
	 * Expected: unlinked contract appears last.
	 */
	@Test(description = "Should handle NULLS_LAST for CDD projection — null entries appear last")
	@Transactional
	public void shouldHandleNullsLastInCddProjection() throws IOException {
		// Given: 3 contracts — 2 linked to named partners, 1 unlinked
		DocumentReference contractWithPartner1 = createContract("Contract-alan.json");
		DocumentReference contractWithPartner2 = createContract("Contract-beth.json");
		DocumentReference contractUnlinked = createContract("Contract-karl.json");

		DocumentReference partner1 = createBusinessPartner("BusinessPartner-1.json"); // Name: "First"
		DocumentReference partner2 = createBusinessPartner("BusinessPartner-2.json"); // Name: "Second"

		linkContractToPartner(contractWithPartner1, partner1);
		linkContractToPartner(contractWithPartner2, partner2);
		// contractUnlinked has no partner

		// When: Query CDDs sorted by Partner.Name ASC NULLS_LAST
		RelationshipOrder relationshipOrder = new RelationshipOrder(
			CONTRACT_BUSINESS_PARTNER_MODEL, PARTNER_ROLE,
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.ASC, false, NullHandling.NULLS_LAST)
		);

		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(CONTRACT_CDM_MODEL)
			.projectionName("cdd")
			.sort(List.of(relationshipOrder))
			.paging(Paging.builder().pageNumber(0).pageSize(100).build())
			.build();
		PagedResultSet<DocumentSpec> result = queryOperation.rpc(queryRoot);

		// Then: unlinked contract appears last (NULLS_LAST)
		Assert.assertNotNull(result);
		List<DocumentSpec> documents = result.getEntries();
		Assert.assertTrue(documents.size() >= 3, "Expected at least 3 CDD root entries");

		List<String> contractIds = extractRootContractIds(documents);
		Assert.assertTrue(contractIds.size() >= 3);

		int unlinkedIndex = contractIds.indexOf(contractUnlinked.toString());
		int linkedIndex1 = contractIds.indexOf(contractWithPartner1.toString());
		int linkedIndex2 = contractIds.indexOf(contractWithPartner2.toString());

		Assert.assertTrue(unlinkedIndex >= 0, "Unlinked contract should be in results");
		Assert.assertTrue(linkedIndex1 >= 0, "First linked contract should be in results");
		Assert.assertTrue(linkedIndex2 >= 0, "Second linked contract should be in results");

		Assert.assertTrue(unlinkedIndex > linkedIndex1, "Linked contracts should appear before the unlinked one (NULLS_LAST)");
		Assert.assertTrue(unlinkedIndex > linkedIndex2, "Linked contracts should appear before the unlinked one (NULLS_LAST)");
	}

	// Helper methods

	private DocumentReference createContract(String fileName) throws IOException {
		DocumentReference docRef = documentFunctions.createDocumentFromFileAndGetDocRef(
			CONTRACT_DOCUMENT_MODEL,
			PathConstants.DOCUMENTS_SEARCH_PATH + "singleModelSort/" + fileName
		);
		testDocuments.add(docRef);
		return docRef;
	}

	private DocumentReference createBusinessPartner(String fileName) throws IOException {
		DocumentReference docRef = documentFunctions.createDocumentFromFileAndGetDocRef(
			BUSINESS_PARTNER_DOCUMENT_MODEL,
			PathConstants.DOCUMENTS_SEARCH_FILTERING_PATH + fileName
		);
		testDocuments.add(docRef);
		return docRef;
	}

	private void linkContractToPartner(DocumentReference contract, DocumentReference partner) {
		RelationshipRoleSpec partnerRole = new RelationshipRoleSpec(PARTNER_ROLE, partner);
		RelationshipRoleSpec contractRole = new RelationshipRoleSpec(CONTRACT_ROLE, contract);
		LinkDescriptor linkDescriptor = new LinkDescriptor(
			CONTRACT_BUSINESS_PARTNER_MODEL,
			Arrays.asList(contractRole, partnerRole),
			LinkPosition.TOP
		);

		RelationshipLinkSpec linkSpec = addLinkOperation.rpc(linkDescriptor, null);
		testLinks.add(linkSpec.getId());
	}

	/**
	 * Extracts document IDs from CDD query results.
	 *
	 * CDD projection returns one {@link DocumentSpec} per root contract document. The `docRef`
	 * inside each spec uses the CDM model name as the model and the full CRD reference string
	 * (e.g. "CONTRACT/uuid") as the document ID. Callers must compare against
	 * {@link DocumentReference#toString()} (not `getDocumentId()`).
	 *
	 * @param documents list of CDD result entries
	 * @return ordered list of CRD reference strings (e.g. "CONTRACT/uuid")
	 */
	private List<String> extractRootContractIds(List<DocumentSpec> documents) {
		return documents.stream()
			.map(DocumentSpec::getDocRef)
			.map(DocumentReference::getDocumentId)
			.toList();
	}
}
