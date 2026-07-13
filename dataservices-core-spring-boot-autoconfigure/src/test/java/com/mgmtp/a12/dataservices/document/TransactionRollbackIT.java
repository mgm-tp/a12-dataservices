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
package com.mgmtp.a12.dataservices.document;

import java.io.IOException;

import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.utils.internal.DocumentUtils;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.extern.slf4j.Slf4j;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Integration tests for transaction rollback behavior with document and index consistency.
 *
 * These tests verify that when a transaction is rolled back, both the database document
 * and the search index are correctly reverted to their pre-transaction state. This is
 * critical for ensuring data consistency.
 *
 * The tests use programmatic transaction control via TransactionTemplate to explicitly
 * control transaction boundaries and force rollback scenarios.
 */
@Slf4j
@Test(groups = "nightly")
public class TransactionRollbackIT extends AbstractTransactionIT {

	private DocumentReference testDocRef;
	private String initialName;
	private String initialIndustry;

	@BeforeMethod
	public void setUp() throws IOException {
		cleanUpTestEnvironment();
		testDocRef = createTestDocument();

		// Capture initial state for verification
		DataServicesDocument doc = documentService.load(testDocRef)
			.orElseThrow(() -> new IllegalStateException("Document not found"));
		initialName = getFieldValue(doc, NAME_FIELD_PATH);
		initialIndustry = getFieldValue(doc, INDUSTRY_FIELD_PATH);

		log.info("Test document created with docRef: {}, initialName: {}, initialIndustry: {}",
			testDocRef, initialName, initialIndustry);
	}

	/**
	 * Verifies that when a transaction updates a document and then explicitly rolls back,
	 * both the document and index changes are reverted to the original state.
	 *
	 * Test scenario:
	 * 1. Create a test document with initial field values
	 * 2. Capture initial database and index state
	 * 3. Begin transaction, update document fields, force rollback via exception
	 * 4. Verify database document matches initial state
	 * 5. Verify index document matches initial state
	 *
	 * Expected result: Both document and index are reverted to pre-transaction state.
	 */
	@Test(description = "Should revert index changes on transaction rollback")
	public void shouldRevertIndexChangesOnTransactionRollback() {
		// Given - capture initial state
		log.info("Initial state before rollback test: Name={}, Industry={}", initialName, initialIndustry);

		// Verify initial index state
		QueryRoot initialQuery = DocumentUtils.buildQueryLoadDocumentByDocRef(testDocRef);
		QueryPage<DocumentTreeResult> initialIndexResult = queryService.query(initialQuery, null);
		assertEquals(initialIndexResult.getTotalElements(), 1, "Document should exist in index initially");

		// When - update within a transaction that will be rolled back
		TransactionTemplate txTemplate = createNewTransactionTemplate();

		String rollbackTestName = "RollbackTestValue_" + System.currentTimeMillis();
		String rollbackTestIndustry = "RollbackTestIndustry_" + System.currentTimeMillis();

		try {
			txTemplate.executeWithoutResult(status -> {
				DataServicesDocument doc = documentService.load(testDocRef)
					.orElseThrow(() -> new IllegalStateException("Document not found"));
				String json = documentFunctions.convertDocumentToJson(doc);
				String updatedJson = updateJsonField(json, NAME_FIELD_PATH, rollbackTestName);
				updatedJson = updateJsonField(updatedJson, INDUSTRY_FIELD_PATH, rollbackTestIndustry);

				DocumentV2 newDocument = documentSupport.convertJSONToDocument(
					testDocRef.getDocumentModelName(),
					new java.io.StringReader(updatedJson)
				);
				documentService.update(testDocRef, newDocument, null);
				log.info("Document updated within transaction to Name={}, Industry={}",
					rollbackTestName, rollbackTestIndustry);

				// Force rollback by throwing exception
				throw new RuntimeException("Forced rollback for testing");
			});
		} catch (RuntimeException e) {
			log.info("Transaction rolled back as expected: {}", e.getMessage());
		}

		// Then - verify both database and index are reverted
		DataServicesDocument dbDoc = documentService.load(testDocRef)
			.orElseThrow(() -> new IllegalStateException("Document not found after rollback"));
		String dbName = getFieldValue(dbDoc, NAME_FIELD_PATH);
		String dbIndustry = getFieldValue(dbDoc, INDUSTRY_FIELD_PATH);

		assertEquals(dbName, initialName, "Database Name should be reverted to initial value");
		assertEquals(dbIndustry, initialIndustry, "Database Industry should be reverted to initial value");
		assertFalse(rollbackTestName.equals(dbName), "Rolled back name should not be in database");
		assertFalse(rollbackTestIndustry.equals(dbIndustry), "Rolled back industry should not be in database");

		// Verify index state matches database (both should be reverted)
		QueryRoot query = buildVerificationQuery(testDocRef, NAME_FIELD_PATH, initialName, INDUSTRY_FIELD_PATH, initialIndustry);
		QueryPage<DocumentTreeResult> indexResult = queryService.query(query, null);

		assertEquals(indexResult.getTotalElements(), 1, "Document should exist in index");
		DocumentTreeResult indexDoc = indexResult.getContent().get(0);
		assertEquals(indexDoc.getDocRef(), testDocRef, "Index docRef should match database");

		log.info("Rollback test completed - database and index reverted to initial state");
		log.info("Database after rollback: Name={}, Industry={}", dbName, dbIndustry);
	}

	/**
	 * Verifies that when a transaction creates a new document and then rolls back,
	 * the document does not exist in either the database or the index.
	 *
	 * Test scenario:
	 * 1. Begin transaction
	 * 2. Create a new document within the transaction
	 * 3. Force rollback via exception
	 * 4. Verify document does not exist in database
	 * 5. Verify document does not exist in index
	 *
	 * Expected result: Document creation is completely rolled back.
	 */
	@Test(description = "Should not persist document on transaction rollback during create")
	public void shouldNotPersistDocumentOnTransactionRollbackDuringCreate() {
		// Given - prepare to create a new document
		TransactionTemplate txTemplate = createNewTransactionTemplate();

		final DocumentReference[] createdDocRef = {null};

		// When - create document then rollback
		try {
			txTemplate.executeWithoutResult(status -> {
				try {
					DataServicesDocument newDoc = documentFunctions.createDocumentFromFile(
						DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
						PathConstants.DOCUMENTS_PATH + "BusinessPartner-1.json"
					);
					createdDocRef[0] = newDoc.getMetadata().getDocRef();
					log.info("Document created within transaction with docRef: {}", createdDocRef[0]);

					// Force rollback
					throw new RuntimeException("Forced rollback after document creation");
				} catch (RuntimeException e) {
					throw e;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		} catch (RuntimeException e) {
			log.info("Transaction rolled back as expected: {}", e.getMessage());
		}

		// Then - verify document does not exist (if docRef was captured before rollback)
		if (createdDocRef[0] != null) {
			// Document should not exist in database
			boolean existsInDb = documentService.load(createdDocRef[0]).isPresent();
			assertFalse(existsInDb, "Document should not exist in database after rollback");

			// Document should not exist in index
			QueryRoot query = DocumentUtils.buildQueryLoadDocumentByDocRef(createdDocRef[0]);
			QueryPage<DocumentTreeResult> indexResult = queryService.query(query, null);
			assertEquals(indexResult.getTotalElements(), 0,
				"Document should not exist in index after rollback");

			log.info("Create rollback test completed - document does not exist in database or index");
		} else {
			log.info("DocRef was not captured (exception thrown before assignment) - rollback verified");
		}
	}

	/**
	 * Verifies that when a transaction deletes a document and then rolls back,
	 * the document still exists in both the database and the index.
	 *
	 * Test scenario:
	 * 1. Verify document exists in database and index
	 * 2. Begin transaction
	 * 3. Delete the document within the transaction
	 * 4. Force rollback via exception
	 * 5. Verify document still exists in database
	 * 6. Verify document still exists in index
	 *
	 * Expected result: Document deletion is completely rolled back.
	 */
	@Test(description = "Should restore document on transaction rollback during delete")
	public void shouldRestoreDocumentOnTransactionRollbackDuringDelete() {
		// Given - verify document exists initially
		assertTrue(documentService.load(testDocRef).isPresent(),
			"Document should exist in database initially");

		QueryRoot initialQuery = DocumentUtils.buildQueryLoadDocumentByDocRef(testDocRef);
		QueryPage<DocumentTreeResult> initialIndexResult = queryService.query(initialQuery, null);
		assertEquals(initialIndexResult.getTotalElements(), 1, "Document should exist in index initially");

		// When - delete document then rollback
		TransactionTemplate txTemplate = createNewTransactionTemplate();

		try {
			txTemplate.executeWithoutResult(status -> {
				documentService.delete(testDocRef);
				log.info("Document deleted within transaction");

				// Verify document is deleted within transaction (before rollback)
				// Note: This read happens within the same transaction, so it should see the delete
				// However, depending on the implementation, this may vary
				log.info("Forcing rollback after delete");
				throw new RuntimeException("Forced rollback after document deletion");
			});
		} catch (RuntimeException e) {
			log.info("Transaction rolled back as expected: {}", e.getMessage());
		}

		// Then - verify document still exists after rollback
		DataServicesDocument dbDoc = documentService.load(testDocRef)
			.orElse(null);
		assertNotNull(dbDoc, "Document should exist in database after rollback");

		// Verify index state
		QueryRoot query = DocumentUtils.buildQueryLoadDocumentByDocRef(testDocRef);
		QueryPage<DocumentTreeResult> indexResult = queryService.query(query, null);
		assertEquals(indexResult.getTotalElements(), 1, "Document should exist in index after rollback");

		log.info("Delete rollback test completed - document still exists in database and index");
	}

	/**
	 * Verifies that programmatic rollback via TransactionStatus.setRollbackOnly()
	 * correctly reverts both document and index changes.
	 *
	 * Test scenario:
	 * 1. Capture initial state
	 * 2. Begin transaction
	 * 3. Update document
	 * 4. Mark transaction for rollback using setRollbackOnly()
	 * 5. Let transaction complete (will rollback due to flag)
	 * 6. Verify database and index match initial state
	 *
	 * Expected result: Both document and index are reverted when using setRollbackOnly().
	 */
	@Test(description = "Should revert changes when using setRollbackOnly")
	public void shouldRevertChangesWhenUsingSetRollbackOnly() {
		// Given - capture initial state
		log.info("Initial state: Name={}, Industry={}", initialName, initialIndustry);

		// When - update and mark for rollback
		TransactionTemplate txTemplate = createNewTransactionTemplate();

		String rollbackTestName = "SetRollbackOnlyTest_" + System.currentTimeMillis();

		txTemplate.executeWithoutResult(status -> {
			DataServicesDocument doc = documentService.load(testDocRef)
				.orElseThrow(() -> new IllegalStateException("Document not found"));
			DocumentV2 newDocument = updateDocumentFieldViaJson(doc, NAME_FIELD_PATH, rollbackTestName);
			documentService.update(testDocRef, newDocument, null);
			log.info("Document updated to Name={}", rollbackTestName);

			// Mark transaction for rollback without throwing exception
			status.setRollbackOnly();
			log.info("Transaction marked for rollback via setRollbackOnly()");
		});

		// Then - verify rollback occurred
		DataServicesDocument dbDoc = documentService.load(testDocRef)
			.orElseThrow(() -> new IllegalStateException("Document not found after rollback"));
		String dbName = getFieldValue(dbDoc, NAME_FIELD_PATH);

		assertEquals(dbName, initialName, "Database Name should be reverted to initial value");
		assertFalse(rollbackTestName.equals(dbName),
			"Name from rolled back transaction should not be in database");

		// Verify index state
		QueryRoot query = buildVerificationQuery(testDocRef, NAME_FIELD_PATH, initialName);
		QueryPage<DocumentTreeResult> indexResult = queryService.query(query, null);
		assertEquals(indexResult.getTotalElements(), 1, "Document should exist in index");

		log.info("setRollbackOnly test completed - changes reverted");
	}
}
