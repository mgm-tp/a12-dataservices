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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.utils.internal.DocumentUtils;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Integration tests for concurrent document update operations.
 *
 * These tests verify that the index remains consistent with the database when:
 *
 * - Two concurrent write operations target the same document
 * - One operation succeeds while the other's transaction is rolled back
 */
@Slf4j
@Test(groups = "nightly")
public class ConcurrentDocumentUpdateIT extends AbstractTransactionIT {

	private DocumentReference testDocRef;

	@BeforeMethod
	public void setUp() throws IOException {
		cleanUpTestEnvironment();
		testDocRef = createTestDocument();

		log.info("Test document created with docRef: {}", testDocRef);
	}

	/**
	 * Verifies that when two threads update the same document concurrently,
	 * the database and index remain consistent regardless of which thread wins.
	 *
	 * Test steps:
	 * 1. Create a test document with initial fields
	 * 2. Spawn two threads concurrently updating the same document
	 * 3. Use CountDownLatch to ensure both threads start simultaneously
	 * 4. Wait for both threads to complete
	 * 5. Verify database and index are consistent
	 *
	 * Note: With full document replacement and concurrent transactions, both threads
	 * will load the document before either commits. The last thread to commit will
	 * overwrite the first thread's changes entirely. This is expected "last-write-wins"
	 * behavior. The key assertion is that database and index remain consistent.
	 *
	 * Expected result: Document not corrupted, index consistent with database.
	 */
	@Test(description = "Should handle concurrent updates when both succeed")
	public void shouldHandleConcurrentUpdatesWhenBothSucceed() throws Exception {
		String nameUpdatedValue = "UpdatedByThread1";
		String industryUpdatedValue = "UpdatedByThread2";

		// Given - initial document state
		DataServicesDocument initialDoc = documentService.load(testDocRef)
			.orElseThrow(() -> new IllegalStateException("Test document not found"));
		String initialName = getFieldValue(initialDoc, NAME_FIELD_PATH);
		String initialIndustry = getFieldValue(initialDoc, INDUSTRY_FIELD_PATH);
		log.info("Initial document: Name={}, Industry={}", initialName, initialIndustry);

		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch completionLatch = new CountDownLatch(2);
		AtomicReference<Exception> thread1Exception = new AtomicReference<>();
		AtomicReference<Exception> thread2Exception = new AtomicReference<>();

		// When - two threads update the same document concurrently
		ExecutorService executor = Executors.newFixedThreadPool(2);

		Callable<Void> updateNameTask = () -> {
			try {
				startLatch.await();
				TransactionTemplate txTemplate = createNewTransactionTemplate();
				txTemplate.executeWithoutResult(status -> {
					DataServicesDocument doc = documentService.load(testDocRef)
						.orElseThrow(() -> new IllegalStateException("Document not found"));
					DocumentV2 newDocument = updateDocumentFieldViaJson(doc, NAME_FIELD_PATH, nameUpdatedValue);
					documentService.update(testDocRef, newDocument, null);
					log.info("Thread 1: Updated Name field");
				});
				// Short delay to increase likelihood of overlapping operations — outside transaction
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException(e);
				}
			} catch (Exception e) {
				thread1Exception.set(e);
				log.error("Thread 1 failed", e);
			} finally {
				completionLatch.countDown();
			}
			return null;
		};

		Callable<Void> updateIndustryTask = () -> {
			try {
				startLatch.await();
				TransactionTemplate txTemplate = createNewTransactionTemplate();
				txTemplate.executeWithoutResult(status -> {
					DataServicesDocument doc = documentService.load(testDocRef)
						.orElseThrow(() -> new IllegalStateException("Document not found"));
					DocumentV2 newDocument = updateDocumentFieldViaJson(doc, INDUSTRY_FIELD_PATH, industryUpdatedValue);
					documentService.update(testDocRef, newDocument, null);
					log.info("Thread 2: Updated Industry field");
				});
				// Short delay to increase likelihood of overlapping operations — outside transaction
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException(e);
				}
			} catch (Exception e) {
				thread2Exception.set(e);
				log.error("Thread 2 failed", e);
			} finally {
				completionLatch.countDown();
			}
			return null;
		};

		executor.submit(updateNameTask);
		executor.submit(updateIndustryTask);

		// Start both threads simultaneously
		startLatch.countDown();

		// Wait for both to complete
		boolean finished = completionLatch.await(30, TimeUnit.SECONDS);
		executor.shutdown();

		// Then - verify results
		assertTrue(finished, "Executor did not finish within timeout");

		// Note: With full document replacement and concurrent transactions:
		// - Both threads load the document BEFORE either commits
		// - Each thread sees the initial document state
		// - The last thread to commit overwrites the first thread's changes entirely
		// This is expected "last-write-wins" behavior for full document updates.
		// The critical assertion is that database and index remain CONSISTENT.

		// Verify database state - document should not be corrupted
		DataServicesDocument dbDoc = documentService.load(testDocRef)
			.orElseThrow(() -> new IllegalStateException("Document not found"));
		assertNotNull(dbDoc, "Document should exist in database");

		String dbName = getFieldValue(dbDoc, NAME_FIELD_PATH);
		String dbIndustry = getFieldValue(dbDoc, INDUSTRY_FIELD_PATH);
		log.info("Final database state: Name={}, Industry={}", dbName, dbIndustry);

		// Key assertion: At least one of the updates should be present
		// (The last writer's changes will be reflected)
		boolean nameUpdated = nameUpdatedValue.equals(dbName);
		boolean industryUpdated = industryUpdatedValue.equals(dbIndustry);
		assertTrue(nameUpdated || industryUpdated,
			"At least one update should be present in the final document");

		// Verify index state matches database (this is the critical consistency check)
		QueryRoot query = DocumentUtils.buildQueryLoadDocumentByDocRef(testDocRef);
		QueryPage<DocumentTreeResult> indexResult = queryService.query(query, null);

		assertEquals(indexResult.getTotalElements(), 1, "Document should exist in index");
		DocumentTreeResult indexDoc = indexResult.getContent().get(0);
		assertEquals(indexDoc.getDocRef(), testDocRef, "Index docRef should match database");

		// Verify specific field values in index match database
		QueryRoot verifyQuery = buildVerificationQuery(testDocRef, NAME_FIELD_PATH, dbName);
		QueryPage<DocumentTreeResult> verifyResult = queryService.query(verifyQuery, null);
		assertEquals(verifyResult.getTotalElements(), 1,
			"Index should reflect the same Name value as database");

		log.info("Concurrent update test completed - index is consistent with database");
		log.info("Name updated: {}, Industry updated: {}", nameUpdated, industryUpdated);
	}

	/**
	 * Verifies that when two threads update the same document concurrently and one transaction
	 * fails, the successful transaction's changes are reflected in both database and index,
	 * and the failed transaction's changes are completely rolled back.
	 *
	 * Test steps:
	 * 1. Create a test document
	 * 2. Thread 1: Update field to valid value (should succeed)
	 * 3. Thread 2: Update and then throw exception to force rollback
	 * 4. Verify Thread 1's changes are in database and index
	 * 5. Verify Thread 2's changes are NOT in database or index
	 *
	 * Expected result: Thread 1's changes committed, Thread 2 fully rolled back, index consistent.
	 */
	@Test(description = "Should maintain consistency when one transaction fails")
	public void shouldMaintainConsistencyWhenOneTransactionFails() throws Exception {
		String nameUpdated = "SuccessfulUpdate";
		// Given - initial document state
		DataServicesDocument initialDoc = documentService.load(testDocRef)
			.orElseThrow(() -> new IllegalStateException("Document not found"));
		String initialName = getFieldValue(initialDoc, NAME_FIELD_PATH);
		log.info("Initial document Name: {}", initialName);

		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch thread1StartedLatch = new CountDownLatch(1);
		CountDownLatch completionLatch = new CountDownLatch(2);
		AtomicReference<Exception> thread1Exception = new AtomicReference<>();
		AtomicReference<Exception> thread2Exception = new AtomicReference<>();

		ExecutorService executor = Executors.newFixedThreadPool(2);

		// Thread 1: Successful update
		Callable<Void> successfulUpdateTask = () -> {
			try {
				startLatch.await();
				TransactionTemplate txTemplate = createNewTransactionTemplate();
				txTemplate.executeWithoutResult(status -> {
					thread1StartedLatch.countDown();
					DataServicesDocument doc = documentService.load(testDocRef)
						.orElseThrow(() -> new IllegalStateException("Document not found"));
					DocumentV2 newDocument = updateDocumentFieldViaJson(doc, NAME_FIELD_PATH, nameUpdated);
					documentService.update(testDocRef, newDocument, null);
					log.info("Thread 1: Successful update completed");
				});
				// Delay to allow thread 2 to start its transaction — outside transaction
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException(e);
				}
			} catch (Exception e) {
				thread1Exception.set(e);
				log.error("Thread 1 failed unexpectedly", e);
			} finally {
				completionLatch.countDown();
			}
			return null;
		};

		// Thread 2: Update then force rollback by throwing exception
		Callable<Void> failingUpdateTask = () -> {
			try {
				startLatch.await();
				// Wait for thread 1 to start its transaction
				thread1StartedLatch.await();
				Thread.sleep(200); // Small delay to ensure thread 1 has acquired the lock

				TransactionTemplate txTemplate = createNewTransactionTemplate();
				txTemplate.executeWithoutResult(status -> {
					DataServicesDocument doc = documentService.load(testDocRef)
						.orElseThrow(() -> new IllegalStateException("Document not found"));
					DocumentV2 newDocument = updateDocumentFieldViaJson(doc, NAME_FIELD_PATH, "FailedUpdate");
					documentService.update(testDocRef, newDocument, null);
					log.info("Thread 2: Update completed, now forcing rollback");
					// Force rollback by throwing exception
					throw new RuntimeException("Intentional rollback for testing");
				});
			} catch (Exception e) {
				thread2Exception.set(e);
				log.info("Thread 2: Transaction rolled back as expected: {}", e.getMessage());
			} finally {
				completionLatch.countDown();
			}
			return null;
		};

		// When
		executor.submit(successfulUpdateTask);
		executor.submit(failingUpdateTask);

		// Start both threads
		startLatch.countDown();

		// Wait for completion
		boolean finished = completionLatch.await(30, TimeUnit.SECONDS);
		executor.shutdown();

		// Then
		assertTrue(finished, "Executor did not finish within timeout");

		// Thread 2 should have thrown an exception (intentional rollback)
		assertNotNull(thread2Exception.get(), "Thread 2 should have thrown an exception");
		assertTrue(thread2Exception.get().getMessage().contains("Intentional rollback"),
			"Thread 2 exception should be the intentional rollback");

		// Verify database state - should NOT contain "FailedUpdate"
		DataServicesDocument dbDoc = documentService.load(testDocRef)
			.orElseThrow(() -> new IllegalStateException("Document not found"));
		assertNotNull(dbDoc, "Document should exist in database");
		String dbName = getFieldValue(dbDoc, NAME_FIELD_PATH);
		assertFalse("FailedUpdate".equals(dbName), "Database should NOT contain the failed update's value");
		log.info("Database document Name: {}", dbName);

		// Verify index state matches database
		QueryRoot query = buildVerificationQuery(testDocRef, NAME_FIELD_PATH, nameUpdated);
		QueryPage<DocumentTreeResult> indexResult = queryService.query(query, null);

		assertEquals(indexResult.getTotalElements(), 1, "Document should exist in index");
		DocumentTreeResult indexDoc = indexResult.getContent().get(0);
		assertEquals(indexDoc.getDocRef(), testDocRef, "Index docRef should match database");

		log.info("Transaction failure test completed - index is consistent with database");
		log.info("Final document state in database: Name={}", dbName);
	}

	/**
	 * Verifies that when a transaction updates a document and then rolls back,
	 * both the document and index changes are reverted to the original state.
	 *
	 * Test steps:
	 * 1. Create a test document with initial field values
	 * 2. Capture initial index state
	 * 3. Begin transaction, update document, force rollback
	 * 4. Verify database and index match initial state
	 *
	 * Expected result: Both document and index are reverted to pre-transaction state.
	 */
	@Test(description = "Should revert index changes on transaction rollback")
	public void shouldRevertIndexChangesOnTransactionRollback() {
		// Given - capture initial state
		DataServicesDocument initialDoc = documentService.load(testDocRef)
			.orElseThrow(() -> new IllegalStateException("Document not found"));
		String initialName = getFieldValue(initialDoc, NAME_FIELD_PATH);
		String initialIndustry = getFieldValue(initialDoc, INDUSTRY_FIELD_PATH);
		log.info("Initial state: Name={}, Industry={}", initialName, initialIndustry);

		// When - update within a transaction that will be rolled back
		TransactionTemplate txTemplate = createNewTransactionTemplate();

		try {
			txTemplate.executeWithoutResult(status -> {
				DataServicesDocument doc = documentService.load(testDocRef)
						.orElseThrow(() -> new IllegalStateException("Document not found"));
				String json = documentFunctions.convertDocumentToJson(doc);
				String updatedJson = updateJsonField(json, NAME_FIELD_PATH, "RollbackTestValue");
				updatedJson = updateJsonField(updatedJson, INDUSTRY_FIELD_PATH, "RollbackTestIndustry");

				DocumentV2 newDocument = documentSupport.convertJSONToDocument(
					testDocRef.getDocumentModelName(),
					new java.io.StringReader(updatedJson)
				);
				documentService.update(testDocRef, newDocument, null);
				log.info("Document updated within transaction");

				// Force rollback
				throw new RuntimeException("Forced rollback for testing");
			});
		} catch (RuntimeException e) {
			log.info("Transaction rolled back as expected: {}", e.getMessage());
		}

		// Then - verify both database and index are reverted
		DataServicesDocument dbDoc = documentService.load(testDocRef)
			.orElseThrow(() -> new IllegalStateException("Document not found"));
		String dbName = getFieldValue(dbDoc, NAME_FIELD_PATH);
		String dbIndustry = getFieldValue(dbDoc, INDUSTRY_FIELD_PATH);

		assertEquals(dbName, initialName, "Database Name should be reverted to initial value");
		assertEquals(dbIndustry, initialIndustry, "Database Industry should be reverted to initial value");

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
	 * Verifies that no OptimisticLockException is thrown during concurrent updates,
	 * consistent with the existing behavior in DefaultDocumentServiceIT.
	 *
	 * This test is similar to the existing updateDocumentConcurrently test but focuses
	 * specifically on verifying that the @Query-based delete implementation prevents
	 * OptimisticLockException from occurring.
	 */
	@Test(description = "Should not throw OptimisticLockException during concurrent updates")
	public void shouldNotThrowOptimisticLockExceptionDuringConcurrentUpdates() throws Exception {
		// Given
		documentService.load(testDocRef).orElseThrow(() -> new IllegalStateException("Document not found"));
		log.info("Initial document created: {}", testDocRef);

		ExecutorService executor = Executors.newFixedThreadPool(2);
		AtomicReference<Boolean> optimisticLockThrown = new AtomicReference<>(false);

		Callable<Void> concurrentUpdateTask = () -> {
			TransactionTemplate txTemplate = createNewTransactionTemplate();
			txTemplate.executeWithoutResult(status -> {
				DataServicesDocument doc = documentService.load(testDocRef)
					.orElseThrow(() -> new IllegalStateException("Document not found"));
				DocumentV2 newDocument = updateDocumentFieldViaJson(
					doc, NAME_FIELD_PATH, "ConcurrentUpdate_" + Thread.currentThread().getName());
				documentService.update(testDocRef, newDocument, null);
			});
			// Delay to increase likelihood of overlapping DB operations — outside transaction
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			}
			return null;
		};

		// When
		Future<Void> future1 = executor.submit(concurrentUpdateTask);
		Future<Void> future2 = executor.submit(concurrentUpdateTask);

		executor.shutdown();
		boolean finished = executor.awaitTermination(30, TimeUnit.SECONDS);
		assertTrue(finished, "Executor did not finish within timeout");

		// Then - check for OptimisticLockException
		for (Future<Void> future : List.of(future1, future2)) {
			try {
				future.get();
			} catch (ExecutionException e) {
				Throwable cause = e.getCause();
				if (cause instanceof OptimisticLockException ||
					(cause.getCause() != null && cause.getCause() instanceof OptimisticLockException)) {
					optimisticLockThrown.set(true);
					log.warn("Caught OptimisticLockException: {}", cause.toString());
				} else {
					log.error("Unexpected exception during concurrent update", cause);
					throw e;
				}
			}
		}

		assertFalse(optimisticLockThrown.get(),
			"OptimisticLockException should not occur with @Query-based delete implementation");

		// Verify index consistency
		QueryRoot query = DocumentUtils.buildQueryLoadDocumentByDocRef(testDocRef);
		QueryPage<DocumentTreeResult> indexResult = queryService.query(query, null);
		assertEquals(indexResult.getTotalElements(), 1, "Document should exist in index after concurrent updates");

		log.info("No OptimisticLockException test completed successfully");
	}
}
