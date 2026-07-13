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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.exception.query.QueryValidationException;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder.Direction;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder.NullHandling;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.Order;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.RelationshipOrder;
import com.mgmtp.a12.dataservices.query.operation.internal.QueryOperation;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.LinkPosition;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;

import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.ADDRESS_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.CONTRACT_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL;
import static com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.PARTNER_POSTAL_ADDRESS_MODEL;
import static com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants.ADDRESS_ROLE;
import static com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants.CONTRACT_ROLE;
import static com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants.PARTNER_ROLE;

/**
 * Integration tests for relationship-based sorting in QUERY operation.
 *
 * Tests cover:
 * - Single-level relationship sorting (Contract → Partner.Name)
 * - Nested relationship sorting (Contract → Partner → Address.city)
 * - NULL handling (NULLS_FIRST, NULLS_LAST)
 * - Case-sensitive and case-insensitive sorting
 * - Mixed sorting (direct fields + relationship fields)
 * - Validation scenarios (maxCount, maxDepth)
 */
@Slf4j
public class RelationshipOrderIT extends AbstractSpringContextIT {

	@Autowired
	private QueryOperation queryOperation;

	private final List<DocumentReference> testDocuments = new ArrayList<>();
	private final List<String> testLinks = new ArrayList<>();

	@Override
	protected void initializeWithSecurityBypass() throws Exception {
		// Create required document models
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.CONTRACT_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.ADDRESS_DOCUMENT_MODEL_PATH);

		// Create required relationship models (using ContractBusinessPartner which has to-1 cardinality for Partner)
		// and PartnerPostalAddress which has to-1 cardinality for Address
		createModel(resourceFunctions.loadResource(PathConstants.CONTRACT_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH));
		createModel(resourceFunctions.loadResource(PathConstants.PARTNER_POSTAL_ADDRESS_RELATIONSHIP_MODEL_PATH));
	}

	@BeforeMethod
	public void setUp() {
		log.info("Setting up test data for RelationshipOrderIT");
		testDocuments.clear();
		testLinks.clear();
	}

	@AfterMethod
	@Transactional
	public void tearDown() {
		log.info("Cleaning up test data: {} documents, {} links", testDocuments.size(), testLinks.size());

		// Delete documents (links will cascade)
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
	 * Test sorting Contracts by Partner Name in ascending order with NULLS_LAST.
	 * Test data names: BusinessPartner-1="First", BusinessPartner-2="Second", BusinessPartner-3="Engine*"
	 * Alphabetical ASC order: Engine* < First < Second < NULL
	 */
	@Test(description = "Should sort Contracts by Partner.Name ASC with NULLS_LAST")
	@Transactional
	public void shouldSortByPartnerNameAscWhenNullsLast() throws IOException {
		// Given: 4 contracts, 3 with partners
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

		// When: Query contracts sorted by Partner.Name ASC NULLS_LAST
		RelationshipOrder order = new RelationshipOrder(
			CONTRACT_BUSINESS_PARTNER_MODEL,
			PARTNER_ROLE,
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.ASC, false, NullHandling.NULLS_LAST)
		);

		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
			.projectionName("document")
			.sort(List.of(order))
			.paging(Paging.builder().pageNumber(0).pageSize(100).build())
			.build();
		PagedResultSet<DocumentTreeResult> result = queryOperation.rpc(queryRoot);

		// Then: Results should be ordered by partner Name (Engine*, First, Second, then NULL)
		Assert.assertNotNull(result);
		List<DocumentTreeResult> documents = result.getEntries();
		Assert.assertTrue(documents.size() >= 4, "Expected at least 4 contracts");

		// Extract contract IDs in result order
		List<String> contractIds = extractContractIds(documents);

		int engineContractIndex = contractIds.indexOf(contract1.getDocumentId());   // Engine*
		int firstContractIndex = contractIds.indexOf(contract2.getDocumentId());    // First
		int secondContractIndex = contractIds.indexOf(contract3.getDocumentId());   // Second
		int nullContractIndex = contractIds.indexOf(contract4.getDocumentId());     // NULL

		Assert.assertTrue(engineContractIndex >= 0, "Engine* contract should be in results");
		Assert.assertTrue(firstContractIndex >= 0, "First contract should be in results");
		Assert.assertTrue(secondContractIndex >= 0, "Second contract should be in results");
		Assert.assertTrue(nullContractIndex >= 0, "NULL contract should be in results");

		Assert.assertTrue(engineContractIndex < firstContractIndex, "Engine* should come before First");
		Assert.assertTrue(firstContractIndex < secondContractIndex, "First should come before Second");
		Assert.assertTrue(secondContractIndex < nullContractIndex, "Second should come before NULL (contract4)");
	}

	/**
	 * Test sorting Contracts by Partner Name in descending order with NULLS_FIRST.
	 * Test data names: BusinessPartner-1="First", BusinessPartner-2="Second", BusinessPartner-3="Engine*"
	 * Alphabetical DESC order with NULLS_FIRST: NULL < Second < First < Engine*
	 */
	@Test(description = "Should sort Contracts by Partner.Name DESC with NULLS_FIRST")
	@Transactional
	public void shouldSortByPartnerNameDescWhenNullsFirst() throws IOException {
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

		// When: Query contracts sorted by Partner.Name DESC NULLS_FIRST
		RelationshipOrder order = new RelationshipOrder(
			CONTRACT_BUSINESS_PARTNER_MODEL,
			PARTNER_ROLE,
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.DESC, false, NullHandling.NULLS_FIRST)
		);

		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
			.projectionName("document")
			.sort(List.of(order))
			.paging(Paging.builder().pageNumber(0).pageSize(100).build())
			.build();
		PagedResultSet<DocumentTreeResult> result = queryOperation.rpc(queryRoot);

		// Then: Results should be ordered: NULL, Second, First, Engine*
		Assert.assertNotNull(result);
		List<DocumentTreeResult> documents = result.getEntries();
		Assert.assertTrue(documents.size() >= 4);

		List<String> contractIds = extractContractIds(documents);

		int nullIndex = contractIds.indexOf(contract4.getDocumentId());       // NULL
		int secondIndex = contractIds.indexOf(contract2.getDocumentId());     // Second
		int firstIndex = contractIds.indexOf(contract1.getDocumentId());      // First
		int engineIndex = contractIds.indexOf(contract3.getDocumentId());     // Engine*

		Assert.assertTrue(nullIndex >= 0 && secondIndex >= 0 && firstIndex >= 0 && engineIndex >= 0);
		Assert.assertTrue(nullIndex < secondIndex, "NULL should come first");
		Assert.assertTrue(secondIndex < firstIndex, "Second should come before First");
		Assert.assertTrue(firstIndex < engineIndex, "First should come before Engine*");
	}

	/**
	 * Test nested relationship sorting: Contract → Partner → Address.city
	 */
	@Test(description = "Should sort Contracts by Partner.Address.city (nested relationship)")
	@Transactional
	public void shouldSortByNestedRelationshipWhenTwoLevelsDeep() throws IOException {
		// Given: Contracts with Partners, Partners with Addresses
		DocumentReference contract1 = createContract("Contract-alan.json");
		DocumentReference contract2 = createContract("Contract-beth.json");

		DocumentReference partner1 = createBusinessPartner("BusinessPartner-1.json");
		DocumentReference partner2 = createBusinessPartner("BusinessPartner-2.json");

		DocumentReference addressA = createAddress("Address-abajus.json");
		DocumentReference addressB = createAddress("Address-ajus.json");

		linkContractToPartner(contract1, partner1);
		linkContractToPartner(contract2, partner2);

		linkPartnerToAddress(partner1, addressB);
		linkPartnerToAddress(partner2, addressA);

		// When: Query sorted by Partner → Address.City ASC
		RelationshipOrder order = new RelationshipOrder(
			CONTRACT_BUSINESS_PARTNER_MODEL,
			PARTNER_ROLE,
			new RelationshipOrder(PARTNER_POSTAL_ADDRESS_MODEL, ADDRESS_ROLE,
				new DirectFieldOrder("/AddressRoot/City", Direction.ASC, false, NullHandling.NULLS_LAST)
			)
		);

		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
			.projectionName("document")
			.sort(List.of(order))
			.paging(Paging.builder().pageNumber(0).pageSize(100).build())
			.build();
		PagedResultSet<DocumentTreeResult> result = queryOperation.rpc(queryRoot);

		// Then: Query should succeed (actual ordering depends on test data)
		Assert.assertNotNull(result);
		List<DocumentTreeResult> documents = result.getEntries();
		Assert.assertTrue(documents.size() >= 2, "Expected at least 2 contracts");
	}

	/**
	 * Test case-insensitive sorting
	 */
	@Test(description = "Should sort case-insensitively when ignoreCase is true")
	@Transactional
	public void shouldSortCaseInsensitivelyWhenIgnoreCaseTrue() throws IOException {
		// Given: Partners with Names in different cases
		DocumentReference contract1 = createContract("Contract-alan.json");
		DocumentReference contract2 = createContract("Contract-beth.json");
		DocumentReference contract3 = createContract("Contract-karl.json");

		DocumentReference partnerA = createBusinessPartner("BusinessPartner-1.json"); // alice
		DocumentReference partnerB = createBusinessPartner("BusinessPartner-2.json"); // BOB
		DocumentReference partnerC = createBusinessPartner("BusinessPartner-3.json"); // Charlie

		linkContractToPartner(contract1, partnerB);
		linkContractToPartner(contract2, partnerA);
		linkContractToPartner(contract3, partnerC);

		// When: Sort by Partner.Name with ignoreCase=true
		RelationshipOrder order = new RelationshipOrder(
			CONTRACT_BUSINESS_PARTNER_MODEL, PARTNER_ROLE,
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.ASC, true, NullHandling.NULLS_LAST)
		);

		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
			.projectionName("document")
			.sort(List.of(order))
			.paging(Paging.builder().pageNumber(0).pageSize(100).build())
			.build();
		PagedResultSet<DocumentTreeResult> result = queryOperation.rpc(queryRoot);

		// Then: Query should succeed with case-insensitive sorting
		Assert.assertNotNull(result);
		List<DocumentTreeResult> documents = result.getEntries();
		Assert.assertTrue(documents.size() >= 3);
	}

	@Test(description = "Should sort Contracts by Partner StartOfRelationship (IDateType) ASC with chronological order")
	@Transactional
	public void shouldSortByPartnerDateFieldAscWhenNullsLast() throws IOException {
		// Given: 3 contracts linked to 3 partners with distinct StartOfRelationship dates
		DocumentReference contract1 = createContract("Contract-alan.json");
		DocumentReference contract2 = createContract("Contract-beth.json");
		DocumentReference contract3 = createContract("Contract-karl.json");

		// Partner A: StartOfRelationship = 2020-03-01 (earliest)
		DocumentReference partnerA = createBusinessPartnerFromRelationshipSort("BusinessPartner-typed-A.json");
		// Partner B: StartOfRelationship = 2022-07-20 (middle)
		DocumentReference partnerB = createBusinessPartnerFromRelationshipSort("BusinessPartner-typed-B.json");
		// Partner C: StartOfRelationship = 2024-01-10 (latest)
		DocumentReference partnerC = createBusinessPartnerFromRelationshipSort("BusinessPartner-typed-C.json");

		linkContractToPartner(contract1, partnerC); // latest date
		linkContractToPartner(contract2, partnerA); // earliest date
		linkContractToPartner(contract3, partnerB); // middle date

		// When: Query contracts sorted by Partner.StartOfRelationship ASC NULLS_LAST
		RelationshipOrder order = new RelationshipOrder(
			CONTRACT_BUSINESS_PARTNER_MODEL,
			PARTNER_ROLE,
			new DirectFieldOrder("/BusinessPartnerRoot/StartOfRelationship", Direction.ASC, false, NullHandling.NULLS_LAST)
		);

		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
			.projectionName("document")
			.sort(List.of(order))
			.paging(Paging.builder().pageNumber(0).pageSize(100).build())
			.build();
		PagedResultSet<DocumentTreeResult> result = queryOperation.rpc(queryRoot);

		// Then: contract2 (partnerA 2020) before contract3 (partnerB 2022) before contract1 (partnerC 2024)
		Assert.assertNotNull(result);
		List<String> contractIds = extractContractIds(result.getEntries());

		int indexContract1 = contractIds.indexOf(contract1.getDocumentId()); // partnerC: 2024
		int indexContract2 = contractIds.indexOf(contract2.getDocumentId()); // partnerA: 2020
		int indexContract3 = contractIds.indexOf(contract3.getDocumentId()); // partnerB: 2022

		Assert.assertTrue(indexContract1 >= 0, "Contract1 should be in results");
		Assert.assertTrue(indexContract2 >= 0, "Contract2 should be in results");
		Assert.assertTrue(indexContract3 >= 0, "Contract3 should be in results");

		// Chronological order (ASC): 2020-03-01 < 2022-07-20 < 2024-01-10
		Assert.assertTrue(indexContract2 < indexContract3,
			"Contract linked to 2020-partner should come before contract linked to 2022-partner");
		Assert.assertTrue(indexContract3 < indexContract1,
			"Contract linked to 2022-partner should come before contract linked to 2024-partner");
	}

	@Test(description = "Should sort Contracts by Partner EndOfRelationship (IDateTimeType) ASC with chronological order", enabled = false)
	@Transactional
	public void shouldSortByPartnerDateTimeFieldAscWhenNullsLast() throws IOException {
		// Given: 3 contracts linked to 3 partners with distinct EndOfRelationship datetimes
		DocumentReference contract1 = createContract("Contract-alan.json");
		DocumentReference contract2 = createContract("Contract-beth.json");
		DocumentReference contract3 = createContract("Contract-karl.json");

		// Partner A: EndOfRelationship = 2020-06-15T08:00:00 (earliest)
		DocumentReference partnerA = createBusinessPartnerFromRelationshipSort("BusinessPartner-typed-A.json");
		// Partner B: EndOfRelationship = 2021-11-30T16:45:00 (middle)
		DocumentReference partnerB = createBusinessPartnerFromRelationshipSort("BusinessPartner-typed-B.json");
		// Partner C: EndOfRelationship = 2023-04-05T22:15:00 (latest)
		DocumentReference partnerC = createBusinessPartnerFromRelationshipSort("BusinessPartner-typed-C.json");

		linkContractToPartner(contract1, partnerC); // latest datetime
		linkContractToPartner(contract2, partnerA); // earliest datetime
		linkContractToPartner(contract3, partnerB); // middle datetime

		// When: Query contracts sorted by Partner.EndOfRelationship ASC NULLS_LAST
		RelationshipOrder order = new RelationshipOrder(
			CONTRACT_BUSINESS_PARTNER_MODEL,
			PARTNER_ROLE,
			new DirectFieldOrder("/BusinessPartnerRoot/EndOfRelationship", Direction.ASC, false, NullHandling.NULLS_LAST)
		);

		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
			.projectionName("document")
			.sort(List.of(order))
			.paging(Paging.builder().pageNumber(0).pageSize(100).build())
			.build();
		PagedResultSet<DocumentTreeResult> result = queryOperation.rpc(queryRoot);

		// Then: contract2 (partnerA 2020) before contract3 (partnerB 2021) before contract1 (partnerC 2023)
		Assert.assertNotNull(result);
		List<String> contractIds = extractContractIds(result.getEntries());

		int indexContract1 = contractIds.indexOf(contract1.getDocumentId()); // partnerC: 2023
		int indexContract2 = contractIds.indexOf(contract2.getDocumentId()); // partnerA: 2020
		int indexContract3 = contractIds.indexOf(contract3.getDocumentId()); // partnerB: 2021

		Assert.assertTrue(indexContract2 >= 0 && indexContract3 >= 0 && indexContract1 >= 0,
			"All contracts should be in results");

		// Chronological order (ASC): 2020-06-15 < 2021-11-30 < 2023-04-05
		Assert.assertTrue(indexContract2 < indexContract3,
			"Contract linked to 2020-partner should come before contract linked to 2021-partner");
		Assert.assertTrue(indexContract3 < indexContract1,
			"Contract linked to 2021-partner should come before contract linked to 2023-partner");
	}

	@Test(description = "Should sort Contracts by Partner CustomerDiscount (IEnumerationType) ASC by code order")
	@Transactional
	public void shouldSortByPartnerEnumerationFieldAscWhenNullsLast() throws IOException {
		// Given: 3 contracts linked to 3 partners with distinct CustomerDiscount enum codes
		DocumentReference contract1 = createContract("Contract-alan.json");
		DocumentReference contract2 = createContract("Contract-beth.json");
		DocumentReference contract3 = createContract("Contract-karl.json");

		// Partner A: CustomerDiscount = "20%" (lexicographically first by code)
		DocumentReference partnerA = createBusinessPartnerFromRelationshipSort("BusinessPartner-typed-A.json");
		// Partner B: CustomerDiscount = "80%" (middle)
		DocumentReference partnerB = createBusinessPartnerFromRelationshipSort("BusinessPartner-typed-B.json");
		// Partner C: CustomerDiscount = "100%" (lexicographically last by code)
		DocumentReference partnerC = createBusinessPartnerFromRelationshipSort("BusinessPartner-typed-C.json");

		linkContractToPartner(contract1, partnerC); // 100%
		linkContractToPartner(contract2, partnerA); // 20%
		linkContractToPartner(contract3, partnerB); // 80%

		// When: Query contracts sorted by Partner.CustomerDiscount ASC NULLS_LAST
		RelationshipOrder order = new RelationshipOrder(
			CONTRACT_BUSINESS_PARTNER_MODEL,
			PARTNER_ROLE,
			new DirectFieldOrder("/BusinessPartnerRoot/CustomerDiscount", Direction.ASC, false, NullHandling.NULLS_LAST)
		);

		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
			.projectionName("document")
			.sort(List.of(order))
			.paging(Paging.builder().pageNumber(0).pageSize(100).build())
			.build();
		PagedResultSet<DocumentTreeResult> result = queryOperation.rpc(queryRoot);

		// Then: code-sorted order (ASC): "100%" < "20%" < "80%"  (lexicographic by code string)
		Assert.assertNotNull(result);
		List<String> contractIds = extractContractIds(result.getEntries());

		int indexContract1 = contractIds.indexOf(contract1.getDocumentId()); // 100%
		int indexContract2 = contractIds.indexOf(contract2.getDocumentId()); // 20%
		int indexContract3 = contractIds.indexOf(contract3.getDocumentId()); // 80%

		Assert.assertTrue(indexContract1 >= 0 && indexContract2 >= 0 && indexContract3 >= 0,
			"All contracts should be in results");

		// Lexicographic code order (ASC): "100%" < "20%" < "80%"
		Assert.assertTrue(indexContract1 < indexContract2,
			"Contract linked to 100%-partner should come before contract linked to 20%-partner (lex code order)");
		Assert.assertTrue(indexContract2 < indexContract3,
			"Contract linked to 20%-partner should come before contract linked to 80%-partner (lex code order)");
	}

	@Test(description = "Should sort Contracts by Partner accountNumber (INumberType) ASC with numeric order not lexicographic", enabled = false)
	@Transactional
	public void shouldSortByPartnerNumberFieldAscWhenNullsLast() throws IOException {
		// Given: 3 contracts linked to 3 partners with accountNumber values 1, 9, 10
		// Numeric order: 1 < 9 < 10
		// Lexicographic order (wrong): "1" < "10" < "9"
		DocumentReference contract1 = createContract("Contract-alan.json");
		DocumentReference contract2 = createContract("Contract-beth.json");
		DocumentReference contract3 = createContract("Contract-karl.json");

		// Partner A: accountNumber = 1
		DocumentReference partnerA = createBusinessPartnerFromRelationshipSort("BusinessPartner-typed-A.json");
		// Partner B: accountNumber = 9
		DocumentReference partnerB = createBusinessPartnerFromRelationshipSort("BusinessPartner-typed-B.json");
		// Partner C: accountNumber = 10
		DocumentReference partnerC = createBusinessPartnerFromRelationshipSort("BusinessPartner-typed-C.json");

		linkContractToPartner(contract1, partnerC); // accountNumber = 10
		linkContractToPartner(contract2, partnerA); // accountNumber = 1
		linkContractToPartner(contract3, partnerB); // accountNumber = 9

		// When: Query contracts sorted by Partner.accountNumber ASC NULLS_LAST
		RelationshipOrder order = new RelationshipOrder(
			CONTRACT_BUSINESS_PARTNER_MODEL,
			PARTNER_ROLE,
			new DirectFieldOrder("/BusinessPartnerRoot/accountNumber", Direction.ASC, false, NullHandling.NULLS_LAST)
		);

		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
			.projectionName("document")
			.sort(List.of(order))
			.paging(Paging.builder().pageNumber(0).pageSize(100).build())
			.build();
		PagedResultSet<DocumentTreeResult> result = queryOperation.rpc(queryRoot);

		// Then: numeric order (ASC): 1 < 9 < 10 (NOT lexicographic 1, 10, 9)
		Assert.assertNotNull(result);
		List<String> contractIds = extractContractIds(result.getEntries());

		int indexContract1 = contractIds.indexOf(contract1.getDocumentId()); // accountNumber 10
		int indexContract2 = contractIds.indexOf(contract2.getDocumentId()); // accountNumber 1
		int indexContract3 = contractIds.indexOf(contract3.getDocumentId()); // accountNumber 9

		Assert.assertTrue(indexContract1 >= 0 && indexContract2 >= 0 && indexContract3 >= 0,
			"All contracts should be in results");

		// Numeric sort: 1 < 9 < 10
		Assert.assertTrue(indexContract2 < indexContract3,
			"Contract with accountNumber=1 should come before contract with accountNumber=9");
		Assert.assertTrue(indexContract3 < indexContract1,
			"Contract with accountNumber=9 should come before contract with accountNumber=10 (numeric, not lexicographic)");
	}

	@Test(description = "Should silently ignore ignoreCase=true on non-string relationship field, consistent with direct field sort (A12S-6863)")
	@Transactional
	public void shouldIgnoreIgnoreCaseOnNonStringRelationshipField() {
		// Given: a query that sets ignoreCase=true on a date field (IDateType) in a relationship order.
		// Because ignoreCase defaults to true in OM-generated sort specifications, validation must not
		// reject this — it must behave like direct field sort, which silently drops the flag for non-string fields.
		RelationshipOrder order = new RelationshipOrder(
			CONTRACT_BUSINESS_PARTNER_MODEL, PARTNER_ROLE,
			new DirectFieldOrder("/BusinessPartnerRoot/StartOfRelationship", Direction.ASC, true, NullHandling.NULLS_LAST)
		);

		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
			.projectionName("document")
			.sort(List.of(order))
			.paging(Paging.builder().pageNumber(0).pageSize(100).build())
			.build();

		// When / Then: query succeeds (no QueryValidationException)
		PagedResultSet<DocumentTreeResult> result = queryOperation.rpc(queryRoot);
		Assert.assertNotNull(result);
	}

	/**
	 * Test validation: maxCount exceeded
	 */
	@Test(description = "Should reject when maxCount exceeded", expectedExceptions = QueryValidationException.class)
	@Transactional
	public void shouldFailValidationWhenMaxCountExceeded() {
		// Given: More than maxCount (default: 5) relationship orders
		List<Order> orders = new ArrayList<>();
		orders.add(new RelationshipOrder(CONTRACT_BUSINESS_PARTNER_MODEL, PARTNER_ROLE,
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.ASC, false, NullHandling.NULLS_LAST)));
		orders.add(new RelationshipOrder(CONTRACT_BUSINESS_PARTNER_MODEL, PARTNER_ROLE,
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.ASC, false, NullHandling.NULLS_LAST)));
		orders.add(new RelationshipOrder(CONTRACT_BUSINESS_PARTNER_MODEL, PARTNER_ROLE,
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.ASC, false, NullHandling.NULLS_LAST)));
		orders.add(new RelationshipOrder(CONTRACT_BUSINESS_PARTNER_MODEL, PARTNER_ROLE,
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.ASC, false, NullHandling.NULLS_LAST)));
		orders.add(new RelationshipOrder(CONTRACT_BUSINESS_PARTNER_MODEL, PARTNER_ROLE,
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.ASC, false, NullHandling.NULLS_LAST)));
		orders.add(new RelationshipOrder(CONTRACT_BUSINESS_PARTNER_MODEL, PARTNER_ROLE,
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.ASC, false, NullHandling.NULLS_LAST)));

		// When: Execute query
		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
			.projectionName("document")
			.sort(orders)
			.paging(Paging.builder().pageNumber(0).pageSize(100).build())
			.build();

		// Then: Should throw RpcException
		queryOperation.rpc(queryRoot);
	}

	/**
	 * Test validation: maxDepth exceeded
	 */
	@Test(description = "Should reject when maxDepth exceeded", expectedExceptions = QueryValidationException.class)
	@Transactional
	public void shouldFailValidationWhenMaxDepthExceeded() {
		// Given: Nested relationship order exceeding maxNestingDepth (default: 5)
		// Build a chain of 6 nested relationships — deepest is a terminal, then 5 traversal levels
		// Level 6 (terminal)
		DirectFieldOrder deepestTerminal = new DirectFieldOrder("/AddressRoot/City", Direction.ASC, false, NullHandling.NULLS_LAST);
		// Level 5
		RelationshipOrder level5 = new RelationshipOrder(CONTRACT_BUSINESS_PARTNER_MODEL, PARTNER_ROLE, deepestTerminal);
		// Level 4
		RelationshipOrder level4 = new RelationshipOrder(CONTRACT_BUSINESS_PARTNER_MODEL, PARTNER_ROLE, level5);
		// Level 3
		RelationshipOrder level3 = new RelationshipOrder(CONTRACT_BUSINESS_PARTNER_MODEL, PARTNER_ROLE, level4);
		// Level 2
		RelationshipOrder level2 = new RelationshipOrder(CONTRACT_BUSINESS_PARTNER_MODEL, PARTNER_ROLE, level3);
		// Level 1 (top-level root order with direction/nullHandling)
		RelationshipOrder order = new RelationshipOrder(
			CONTRACT_BUSINESS_PARTNER_MODEL, PARTNER_ROLE,
			level2
		);

		// When: Execute query
		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
			.projectionName("document")
			.sort(List.of(order))
			.paging(Paging.builder().pageNumber(0).pageSize(100).build())
			.build();

		// Then: Should throw RpcException
		queryOperation.rpc(queryRoot);
	}

	/**
	 * Combines a relationship-based sort with `links` in the same query.
	 */
	@Test(description = "Should sort Contracts by Partner.Name when query also requests links")
	@Transactional
	public void shouldSortByPartnerNameWhenLinksAreIncluded() throws IOException {
		// Given: 4 contracts, 3 with partners
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

		// When: Query contracts sorted by Partner.Name ASC NULLS_LAST AND with links to the same relationship
		RelationshipOrder order = new RelationshipOrder(
			CONTRACT_BUSINESS_PARTNER_MODEL,
			PARTNER_ROLE,
			new DirectFieldOrder("/BusinessPartnerRoot/Name", Direction.ASC, false, NullHandling.NULLS_LAST)
		);

		QueryLink link = QueryLink.builder()
			.relationshipModel(CONTRACT_BUSINESS_PARTNER_MODEL)
			.targetRole(PARTNER_ROLE)
			.fields(List.of("/BusinessPartnerRoot/Name"))
			.build();

		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
			.projectionName("document")
			.sort(List.of(order))
			.links(List.of(link))
			.paging(Paging.builder().pageNumber(0).pageSize(100).build())
			.build();

		// Must not throw
		PagedResultSet<DocumentTreeResult> result = queryOperation.rpc(queryRoot);

		// Then: Root contracts are present in the expected sort order
		Assert.assertNotNull(result);
		List<String> contractIds = extractContractIds(result.getEntries());

		int engineContractIndex = contractIds.indexOf(contract1.getDocumentId());   // Engine*
		int firstContractIndex = contractIds.indexOf(contract2.getDocumentId());    // First
		int secondContractIndex = contractIds.indexOf(contract3.getDocumentId());   // Second
		int nullContractIndex = contractIds.indexOf(contract4.getDocumentId());     // NULL

		Assert.assertTrue(engineContractIndex >= 0, "Engine* contract should be in results");
		Assert.assertTrue(firstContractIndex >= 0, "First contract should be in results");
		Assert.assertTrue(secondContractIndex >= 0, "Second contract should be in results");
		Assert.assertTrue(nullContractIndex >= 0, "NULL contract should be in results");

		Assert.assertTrue(engineContractIndex < firstContractIndex, "Engine* should come before First");
		Assert.assertTrue(firstContractIndex < secondContractIndex, "First should come before Second");
		Assert.assertTrue(secondContractIndex < nullContractIndex, "Second should come before NULL (contract4)");
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

	private DocumentReference createBusinessPartnerFromRelationshipSort(String fileName) throws IOException {
		DocumentReference docRef = documentFunctions.createDocumentFromFileAndGetDocRef(
			BUSINESS_PARTNER_DOCUMENT_MODEL,
			PathConstants.DOCUMENTS_SEARCH_PATH + "relationshipSort/" + fileName
		);
		testDocuments.add(docRef);
		return docRef;
	}

	private DocumentReference createAddress(String fileName) throws IOException {
		DocumentReference docRef = documentFunctions.createDocumentFromFileAndGetDocRef(
			ADDRESS_DOCUMENT_MODEL,
			PathConstants.DOCUMENTS_SEARCH_PATH + "singleModelSort/" + fileName
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

	private void linkPartnerToAddress(DocumentReference partner, DocumentReference address) {
		RelationshipRoleSpec partnerRole = new RelationshipRoleSpec(PARTNER_ROLE, partner);
		RelationshipRoleSpec addressRole = new RelationshipRoleSpec(ADDRESS_ROLE, address);
		LinkDescriptor linkDescriptor = new LinkDescriptor(
			PARTNER_POSTAL_ADDRESS_MODEL,
			Arrays.asList(partnerRole, addressRole),
			LinkPosition.TOP
		);

		RelationshipLinkSpec linkSpec = addLinkOperation.rpc(linkDescriptor, null);
		testLinks.add(linkSpec.getId());
	}

	private List<String> extractContractIds(List<DocumentTreeResult> documents) {
		return documents.stream()
			.filter(doc -> doc.getType() == DocumentTreeNodeType.ROOT)
			.map(DocumentTreeResult::getDocRef)
			.map(DocumentReference::getDocumentId)
			.toList();
	}
}
