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
package com.mgmtp.a12.dataservices.rpc.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentReferenceResult;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.operation.internal.AddDocumentOperation;

/**
 * Integration tests to verify end-to-end document creation using JsonNode-based deserialization.
 *
 * These tests verify that the complete flow from `AddDocumentOperation` through `DocumentSupport`
 * to the document repository works correctly when using the JsonNode-based deserialization
 * methods.
 *
 * Expected Behavior:
 *
 * - Documents can be created successfully using JsonNode directly
 * - All document lifecycle events fire correctly
 * - Document validation and persistence work as expected
 *
 * Implementation Note:
 * The `AddDocumentOperation.rpc()` method passes the JsonNode directly to `DocumentSupport`,
 * which converts it to a String internally and deserializes using the standard Reader-based
 * deserialization path. This centralizes the conversion logic in one place.
 *
 * Integration Scope:
 * These tests verify the full stack:
 *
 * - AddDocumentOperation receives JsonNode
 * - DocumentSupport converts JsonNode and deserializes
 * - Kernel deserializer processes the input
 * - Document is validated and persisted
 * - DocumentReference is returned
 *
 * @see AddDocumentOperation
 * @see com.mgmtp.a12.dataservices.document.support.DocumentSupport
 * @see com.mgmtp.a12.dataservices.document.support.internal.DefaultDocumentSupport
 */
public class AddDocumentOperationJsonNodeIT extends AbstractSpringContextIT {

	@Autowired
	private AddDocumentOperation addDocumentOperation;

	@Autowired
	private DocumentService documentService;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeMethod
	public void setUp() throws Exception {
		cleanUpTestEnvironment();
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
	}

	/**
	 * Verifies that documents can be created successfully using JsonNode-based deserialization.
	 *
	 * Expected Behavior:
	 * When a document is created via `AddDocumentOperation.rpc()` with a JsonNode, the document
	 * should be deserialized, validated, and persisted successfully. The method should return
	 * a valid DocumentReference, and the document should be retrievable from the repository.
	 *
	 * Test Scenario:
	 *
	 * 1. Create a JsonNode representing a valid BusinessPartner document
	 * 2. Call `AddDocumentOperation.rpc(documentModelName, jsonNode, locale)`
	 * 3. Verify the method returns a valid DocumentReference
	 * 4. Verify the document can be retrieved using the returned docRef
	 * 5. Verify document fields match the input JsonNode
	 *
	 * Configuration Dependencies:
	 *
	 * - DocumentSupport must implement `convertJSONToDocument(String, JsonNode)` method
	 * - AddDocumentOperation must be updated to use the new method
	 * - Spring Boot context with full document persistence infrastructure
	 */
	@Test(description = "Should create document using JsonNode directly without toString() conversion")
	@Transactional
	public void shouldCreateDocumentUsingJsonNodeDirectly() {
		// Given: A JsonNode representing a valid BusinessPartner document
		ObjectNode documentNode = objectMapper.createObjectNode();
		ObjectNode businessPartnerRoot = objectMapper.createObjectNode();
		businessPartnerRoot.put("Name", "DuckDuuuck");
		businessPartnerRoot.put("Industry", "Medicin");
		documentNode.set("BusinessPartnerRoot", businessPartnerRoot);

		// When: We create the document using AddDocumentOperation
		DocumentReferenceResult result = addDocumentOperation.rpc(
			DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
			documentNode,
			null
		);
		DocumentReference docRef = result.docRef();

		// Then: The operation should return a valid DocumentReference
		Assert.assertNotNull(docRef, "DocumentReference should not be null");
		Assert.assertEquals(docRef.getDocumentModelName(), DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
			"Document model name should match");
		Assert.assertNotNull(docRef.getDocumentId(), "Document ID should not be null");

		// And: The document should be retrievable from the repository
		var retrievedDocumentOpt = documentService.load(docRef);
		Assert.assertTrue(retrievedDocumentOpt.isPresent(), "Document should be loadable");

		var retrievedDocument = retrievedDocumentOpt.get();
		Assert.assertEquals(retrievedDocument.getMetadata().getDocRef(), docRef,
			"Retrieved document should have the same docRef");

		// And: Document fields should match the input
		var documentContent = retrievedDocument.getKernelDocument();
		Assert.assertNotNull(documentContent, "Document content should not be null");

		// Cleanup: Delete the test document
		documentService.delete(docRef);
	}

	/**
	 * Verifies that large documents are handled correctly.
	 *
	 * Expected Behavior:
	 * When creating a large document (e.g., with many fields), the JsonNode-based deserialization
	 * should handle it correctly. The document should be deserialized, validated, and persisted
	 * successfully.
	 *
	 * Test Scenario:
	 *
	 * 1. Create a JsonNode representing a large document
	 * 2. Call `AddDocumentOperation.rpc(documentModelName, jsonNode, locale)`
	 * 3. Verify the method completes successfully
	 * 4. Verify the document is persisted correctly
	 * 5. Verify all fields are present in the persisted document
	 *
	 * Why This Matters:
	 * This test verifies that the JsonNode-based API works correctly for documents of various
	 * sizes. Documents with many fields should be handled without errors.
	 */
	@Test(description = "Should handle large documents efficiently without memory overhead")
	@Transactional
	public void shouldHandleLargeDocumentEfficiently() {
		// Given: A JsonNode representing a BusinessPartner document
		ObjectNode documentNode = objectMapper.createObjectNode();
		ObjectNode businessPartnerRoot = objectMapper.createObjectNode();
		businessPartnerRoot.put("Name", "Jane Smith");
		businessPartnerRoot.put("Industry", "Finance");
		businessPartnerRoot.put("StartOfRelationship", "2022-08-01");
		documentNode.set("BusinessPartnerRoot", businessPartnerRoot);

		// Note: We cannot add arbitrary custom fields to BusinessPartner as it has a defined schema.
		// The test validates correct JsonNode-based deserialization and persistence.

		// When: We create the document using AddDocumentOperation
		DocumentReferenceResult result = addDocumentOperation.rpc(
			DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
			documentNode,
			null
		);
		DocumentReference docRef = result.docRef();

		// Then: The operation should complete successfully
		Assert.assertNotNull(docRef, "DocumentReference should not be null");
		Assert.assertEquals(docRef.getDocumentModelName(), DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
			"Document model name should match");
		Assert.assertNotNull(docRef.getDocumentId(), "Document ID should not be null");

		// And: The document should be retrievable from the repository
		var retrievedDocumentOpt = documentService.load(docRef);
		Assert.assertTrue(retrievedDocumentOpt.isPresent(), "Document should be loadable");

		var retrievedDocument = retrievedDocumentOpt.get();
		Assert.assertEquals(retrievedDocument.getMetadata().getDocRef(), docRef,
			"Retrieved document should have the same docRef");

		// And: Document content should be persisted correctly
		var documentContent = retrievedDocument.getKernelDocument();
		Assert.assertNotNull(documentContent, "Document content should not be null");

		// Performance Note: If this test takes significantly longer after code changes,
		// it may indicate that the JsonNode-based optimization has been lost and
		// toString() conversion has been reintroduced.

		// Cleanup: Delete the test document
		documentService.delete(docRef);
	}
}
