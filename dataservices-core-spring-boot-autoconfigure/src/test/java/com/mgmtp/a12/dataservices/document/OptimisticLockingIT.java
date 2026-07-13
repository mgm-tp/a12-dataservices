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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.extern.slf4j.Slf4j;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Integration tests for optimistic locking behavior and index consistency.
 *
 * These tests verify that when optimistic locking failures occur (e.g., stale data updates),
 * the database and search index remain consistent. The successful transaction's changes should
 * be reflected in both, while the failed transaction's changes should not appear.
 */
@Slf4j
@Test(groups = "nightly")
public class OptimisticLockingIT extends AbstractTransactionIT {

	private DocumentReference testDocRef;
	private String initialName;

	@BeforeMethod
	public void setUp() throws IOException {
		cleanUpTestEnvironment();
		testDocRef = createTestDocument();

		// Capture initial state
		DataServicesDocument doc = documentService.load(testDocRef)
			.orElseThrow(() -> new IllegalStateException("Document not found"));
		initialName = getFieldValue(doc, NAME_FIELD_PATH);

		log.info("Test document created with docRef: {}, initialName: {}", testDocRef, initialName);
	}

	/**
	 * Verifies that when two concurrent transactions attempt to update the same document,
	 * and one's update is based on stale data (simulating optimistic locking failure scenario),
	 * the database and index remain consistent.
	 *
	 * Test scenario:
	 * 1. Thread 1 loads document, holds reference to current state
	 * 2. Thread 2 loads document, updates it, and commits
	 * 3. Thread 1 attempts to update using its stale reference
	 * 4. Thread 1's update either:
	 *    a) Overwrites Thread 2's changes (if no optimistic locking at application level), or
	 *    b) Fails with conflict (if optimistic locking is enforced)
	 * 5. Verify database and index are consistent regardless of outcome
	 *
	 * Expected result: Index remains consistent with database state.
	 */
	@Test(description = "Should handle optimistic locking failure correctly")
	public void shouldHandleOptimisticLockingFailureCorrectly() throws Exception {
		// Given
		CountDownLatch thread1LoadedLatch = new CountDownLatch(1);
		CountDownLatch thread2CommittedLatch = new CountDownLatch(1);
		CountDownLatch completionLatch = new CountDownLatch(2);

		AtomicReference<String> thread1FinalValue = new AtomicReference<>();
		AtomicReference<String> thread2FinalValue = new AtomicReference<>();
		AtomicReference<Exception> thread1Exception = new AtomicReference<>();
		AtomicBoolean thread1Success = new AtomicBoolean(false);

		ExecutorService executor = Executors.newFixedThreadPool(2);

		// Thread 1: Load document, wait for Thread 2 to commit, then try to update with stale data
		Runnable staleUpdateTask = () -> {
			try {
				TransactionTemplate txTemplate = createNewTransactionTemplate();

				// Load document and capture state
				final DataServicesDocument[] loadedDoc = new DataServicesDocument[1];
				final String[] loadedJson = new String[1];

				txTemplate.executeWithoutResult(status -> {
					loadedDoc[0] = documentService.load(testDocRef)
						.orElseThrow(() -> new IllegalStateException("Document not found"));
					loadedJson[0] = documentFunctions.convertDocumentToJson(loadedDoc[0]);
					log.info("Thread 1: Loaded document");
				});

				// Signal that Thread 1 has loaded the document
				thread1LoadedLatch.countDown();

				// Wait for Thread 2 to commit its update
				thread2CommittedLatch.await();
				log.info("Thread 1: Received signal that Thread 2 has committed");

				// Small delay to ensure Thread 2's transaction is fully committed
				Thread.sleep(200);

				// Now try to update using the stale loaded JSON
				TransactionTemplate updateTxTemplate = createNewTransactionTemplate();

				updateTxTemplate.executeWithoutResult(status -> {
					// Use the stale JSON from earlier load
					String updatedJson = updateJsonField(
						loadedJson[0],
						NAME_FIELD_PATH,
						"StaleUpdateFromThread1"
					);
					DocumentV2 newDocument = documentSupport.convertJSONToDocument(
						testDocRef.getDocumentModelName(),
						new java.io.StringReader(updatedJson)
					);
					documentService.update(testDocRef, newDocument, null);
					thread1FinalValue.set("StaleUpdateFromThread1");
					thread1Success.set(true);
					log.info("Thread 1: Successfully updated with stale data");
				});
			} catch (Exception e) {
				thread1Exception.set(e);
				log.info("Thread 1: Failed as expected: {}", e.getMessage());
			} finally {
				completionLatch.countDown();
			}
		};

		// Thread 2: Wait for Thread 1 to load, then update and commit
		Runnable freshUpdateTask = () -> {
			try {
				// Wait for Thread 1 to load the document first
				thread1LoadedLatch.await();
				log.info("Thread 2: Received signal that Thread 1 has loaded");

				TransactionTemplate txTemplate = createNewTransactionTemplate();

				txTemplate.executeWithoutResult(status -> {
					DataServicesDocument doc = documentService.load(testDocRef)
						.orElseThrow(() -> new IllegalStateException("Document not found"));
					DocumentV2 newDocument = updateDocumentFieldViaJson(doc, NAME_FIELD_PATH, "FreshUpdateFromThread2");
					documentService.update(testDocRef, newDocument, null);
					thread2FinalValue.set("FreshUpdateFromThread2");
					log.info("Thread 2: Successfully updated");
				});

				// Signal Thread 1 that we've committed
				thread2CommittedLatch.countDown();
			} catch (Exception e) {
				log.error("Thread 2 failed unexpectedly", e);
			} finally {
				completionLatch.countDown();
			}
		};

		// When - execute both threads
		executor.submit(staleUpdateTask);
		executor.submit(freshUpdateTask);

		// Wait for completion
		boolean finished = completionLatch.await(30, TimeUnit.SECONDS);
		executor.shutdown();

		// Then - verify results
		assertTrue(finished, "Executor did not finish within timeout");

		// The key assertion: database and index must be consistent
		DataServicesDocument dbDoc = documentService.load(testDocRef)
			.orElseThrow(() -> new IllegalStateException("Document not found"));
		String dbName = getFieldValue(dbDoc, NAME_FIELD_PATH);
		assertNotNull(dbName, "Document should exist with a name value");

		log.info("Final database state: Name={}", dbName);
		log.info("Thread 1 success: {}, Thread 2 value: {}", thread1Success.get(), thread2FinalValue.get());

		// Conditional assertion: the expected winner depends on whether thread 1 succeeded.
		// Thread 2 always commits first. If thread 1 also succeeds, thread 1 is the last writer.
		// If thread 1 fails (e.g., connection timeout on Jenkins), thread 2's value must be in the database.
		String expectedDbName;
		if (thread1Success.get()) {
			expectedDbName = "StaleUpdateFromThread1";
			assertEquals(dbName, expectedDbName,
				"Thread 1 succeeded as last writer — database must contain thread 1's value");
		} else {
			expectedDbName = "FreshUpdateFromThread2";
			assertEquals(dbName, expectedDbName,
				"Thread 1 failed — database must contain thread 2's committed value");
			log.info("Thread 1 failed with: {}", thread1Exception.get() != null ? thread1Exception.get().getMessage() : "unknown");
		}

		// Verify index consistency — index must reflect whatever the database contains
		QueryRoot query = buildVerificationQuery(testDocRef, NAME_FIELD_PATH, expectedDbName);
		QueryPage<DocumentTreeResult> indexResult = queryService.query(query, null);

		assertEquals(indexResult.getTotalElements(), 1, "Document should exist in index");
		DocumentTreeResult indexDoc = indexResult.getContent().get(0);
		assertEquals(indexDoc.getDocRef(), testDocRef, "Index docRef should match database");

		// The critical assertion: index must reflect the actual database state
		log.info("Optimistic locking test completed - index is consistent with database");
		log.info("Database value: {}, thread 1 success: {}", dbName, thread1Success.get());
	}

	/**
	 * Verifies that when a transaction fails due to a simulated version conflict,
	 * the index correctly reflects only the committed transaction's state.
	 *
	 * Test scenario:
	 * 1. Thread 1 updates the document successfully
	 * 2. Thread 2 loads document, then throws exception simulating version conflict
	 * 3. Verify Thread 1's changes are in database and index
	 * 4. Verify Thread 2's intended changes are NOT in database or index
	 *
	 * Expected result: Only Thread 1's committed changes appear in database and index.
	 */
	@Test(description = "Should maintain index consistency after simulated version conflict")
	public void shouldMaintainIndexConsistencyAfterSimulatedVersionConflict() throws Exception {
		// Given
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch thread1CommittedLatch = new CountDownLatch(1);
		CountDownLatch completionLatch = new CountDownLatch(2);

		String successValue = "SuccessfulCommit_" + System.currentTimeMillis();
		String failedValue = "FailedConflict_" + System.currentTimeMillis();

		AtomicReference<Exception> thread2Exception = new AtomicReference<>();

		ExecutorService executor = Executors.newFixedThreadPool(2);

		// Thread 1: Successful update
		Runnable successfulUpdateTask = () -> {
			try {
				startLatch.await();
				TransactionTemplate txTemplate = createNewTransactionTemplate();

				txTemplate.executeWithoutResult(status -> {
					DataServicesDocument doc = documentService.load(testDocRef)
						.orElseThrow(() -> new IllegalStateException("Document not found"));
					DocumentV2 newDocument = updateDocumentFieldViaJson(doc, NAME_FIELD_PATH, successValue);
					documentService.update(testDocRef, newDocument, null);
					log.info("Thread 1: Successfully committed update");
				});

				thread1CommittedLatch.countDown();
			} catch (Exception e) {
				log.error("Thread 1 failed unexpectedly", e);
			} finally {
				completionLatch.countDown();
			}
		};

		// Thread 2: Load, update, then simulate version conflict exception
		Runnable conflictUpdateTask = () -> {
			try {
				startLatch.await();
				// Wait for Thread 1 to commit
				thread1CommittedLatch.await();
				Thread.sleep(100);

				TransactionTemplate txTemplate = createNewTransactionTemplate();

				txTemplate.executeWithoutResult(status -> {
					DataServicesDocument doc = documentService.load(testDocRef)
						.orElseThrow(() -> new IllegalStateException("Document not found"));
					DocumentV2 newDocument = updateDocumentFieldViaJson(doc, NAME_FIELD_PATH, failedValue);
					documentService.update(testDocRef, newDocument, null);
					log.info("Thread 2: Update performed, now simulating conflict");

					// Simulate version conflict by throwing exception
					throw new RuntimeException("Simulated optimistic locking failure - version conflict");
				});
			} catch (Exception e) {
				thread2Exception.set(e);
				log.info("Thread 2: Simulated conflict occurred: {}", e.getMessage());
			} finally {
				completionLatch.countDown();
			}
		};

		// When
		executor.submit(successfulUpdateTask);
		executor.submit(conflictUpdateTask);

		startLatch.countDown();

		boolean finished = completionLatch.await(30, TimeUnit.SECONDS);
		executor.shutdown();

		// Then
		assertTrue(finished, "Executor did not finish within timeout");

		// Thread 2 should have thrown the simulated conflict exception
		assertNotNull(thread2Exception.get(), "Thread 2 should have thrown simulated conflict exception");

		// Verify database state - should contain Thread 1's value (or Thread 2's depending on timing)
		// but NOT the failed value if rollback worked correctly
		DataServicesDocument dbDoc = documentService.load(testDocRef)
			.orElseThrow(() -> new IllegalStateException("Document not found"));
		String dbName = getFieldValue(dbDoc, NAME_FIELD_PATH);

		// The key assertion: failed value should NOT be in database
		assertFalse(failedValue.equals(dbName),
			"Failed transaction's value should NOT be in database");

		log.info("Database value after conflict: {}", dbName);

		// Verify index consistency
		QueryRoot query = buildVerificationQuery(testDocRef, NAME_FIELD_PATH, successValue);
		QueryPage<DocumentTreeResult> indexResult = queryService.query(query, null);

		assertEquals(indexResult.getTotalElements(), 1, "Document should exist in index");
		DocumentTreeResult indexDoc = indexResult.getContent().get(0);
		assertEquals(indexDoc.getDocRef(), testDocRef, "Index docRef should match database");

		log.info("Version conflict test completed - index is consistent with database");
	}

	/**
	 * Verifies that rapid sequential updates do not cause index inconsistency.
	 *
	 * Test scenario:
	 * 1. Perform 10 sequential updates in rapid succession
	 * 2. Each update increments a counter in the Name field
	 * 3. Verify final database value matches expected counter
	 * 4. Verify index is consistent with database
	 *
	 * Expected result: All updates applied, index consistent.
	 */
	@Test(description = "Should handle rapid sequential updates without index inconsistency")
	public void shouldHandleRapidSequentialUpdatesWithoutIndexInconsistency() {
		// Given
		final int updateCount = 10;

		// When - perform rapid sequential updates
		for (int i = 1; i <= updateCount; i++) {
			final int updateNum = i;
			TransactionTemplate txTemplate = createNewTransactionTemplate();

			txTemplate.executeWithoutResult(status -> {
				DataServicesDocument doc = documentService.load(testDocRef)
					.orElseThrow(() -> new IllegalStateException("Document not found"));
				DocumentV2 newDocument = updateDocumentFieldViaJson(doc, NAME_FIELD_PATH, "Update_" + updateNum);
				documentService.update(testDocRef, newDocument, null);
			});
		}

		// Then - verify final state
		DataServicesDocument dbDoc = documentService.load(testDocRef)
			.orElseThrow(() -> new IllegalStateException("Document not found"));
		String dbName = getFieldValue(dbDoc, NAME_FIELD_PATH);

		assertEquals(dbName, "Update_" + updateCount,
			"Database should contain final update value");

		// Verify index consistency
		QueryRoot query = buildVerificationQuery(testDocRef, NAME_FIELD_PATH, dbName);
		QueryPage<DocumentTreeResult> indexResult = queryService.query(query, null);

		assertEquals(indexResult.getTotalElements(), 1, "Document should exist in index");
		DocumentTreeResult indexDoc = indexResult.getContent().get(0);
		assertEquals(indexDoc.getDocRef(), testDocRef, "Index docRef should match database");

		log.info("Rapid sequential updates test completed - {} updates applied, index consistent", updateCount);
	}
}
