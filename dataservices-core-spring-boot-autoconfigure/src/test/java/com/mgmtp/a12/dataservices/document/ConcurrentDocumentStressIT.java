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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.extern.slf4j.Slf4j;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Stress tests for concurrent document operations to verify index consistency under high load.
 *
 * These tests verify that the database and search index remain consistent when many threads
 * perform concurrent operations on the same document. This is critical for ensuring data
 * integrity in high-concurrency production scenarios.
 *
 * Important note about READ_COMMITTED isolation: With full document replacement (no version
 * field in update), concurrent updates will overwrite each other (last-write-wins). The key
 * verification is that the DATABASE and INDEX remain consistent, not that all individual
 * increments are reflected in the final counter value.
 */
@Slf4j
@Test(groups = "nightly")
public class ConcurrentDocumentStressIT extends AbstractTransactionIT {

	private static final int THREAD_COUNT = 20;
	private static final int INCREMENTS_PER_THREAD = 10;

	private DocumentReference testDocRef;

	@BeforeMethod
	public void setUp() throws IOException {
		cleanUpTestEnvironment();
		testDocRef = createTestDocument();

		// Initialize the counter field to "0"
		initializeCounterField();

		log.info("Test document created with docRef: {} and counter initialized to 0", testDocRef);
	}

	/**
	 * Initializes the Name field to "0" for use as a counter in stress tests.
	 */
	private void initializeCounterField() {
		TransactionTemplate txTemplate = createNewTransactionTemplate();
		txTemplate.executeWithoutResult(status -> {
			DataServicesDocument doc = documentService.load(testDocRef)
				.orElseThrow(() -> new IllegalStateException("Document not found"));
			DocumentV2 newDocument = updateDocumentFieldViaJson(doc, NAME_FIELD_PATH, "0");
			documentService.update(testDocRef, newDocument, null);
		});
	}

	/**
	 * Stress test: Verifies that concurrent document updates maintain database-index consistency.
	 *
	 * Test scenario:
	 * 1. Create a document with a counter field initialized to 0
	 * 2. Spawn 20 threads, each performing 10 update operations (200 total)
	 * 3. Each operation: read current value, increment by 1, update document
	 * 4. Due to READ_COMMITTED isolation with full document replacement, concurrent updates
	 *    will overwrite each other (last-write-wins behavior)
	 * 5. Verify database and index are consistent with each other
	 *
	 * Note: With READ_COMMITTED isolation and no optimistic locking on the document itself,
	 * concurrent read-modify-write operations on the same document will NOT accumulate.
	 * This is expected behavior - the key verification is that the DATABASE and INDEX
	 * remain consistent, not that all increments are reflected.
	 *
	 * Expected result: Database and index are consistent, document is not corrupted.
	 */
	@Test(description = "Should maintain consistency under high concurrency (20 threads x 10 updates)",
		  timeOut = 120000)
	public void shouldMaintainConsistencyUnderHighConcurrency() throws InterruptedException {
		// Given
		CountDownLatch startLatch = new CountDownLatch(1);
		AtomicInteger successfulUpdates = new AtomicInteger(0);
		AtomicInteger failedUpdates = new AtomicInteger(0);

		ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
		List<Future<Void>> futures = new ArrayList<>();

		// When - spawn 20 threads, each performing 10 increments
		for (int t = 0; t < THREAD_COUNT; t++) {
			final int threadId = t;
			Callable<Void> incrementTask = () -> {
				try {
					startLatch.await();
					for (int i = 0; i < INCREMENTS_PER_THREAD; i++) {
						boolean success = performAtomicIncrement(threadId, i);
						if (success) {
							successfulUpdates.incrementAndGet();
						} else {
							failedUpdates.incrementAndGet();
						}
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				return null;
			};
			futures.add(executor.submit(incrementTask));
		}

		// Start all threads simultaneously
		log.info("Starting {} threads, each performing {} increments (total {} operations)",
			THREAD_COUNT, INCREMENTS_PER_THREAD, THREAD_COUNT * INCREMENTS_PER_THREAD);
		startLatch.countDown();

		// Wait for all threads to complete
		executor.shutdown();
		boolean finished = executor.awaitTermination(90, TimeUnit.SECONDS);
		assertTrue(finished, "Executor did not finish within timeout");

		// Check for exceptions
		for (Future<Void> future : futures) {
			try {
				future.get();
			} catch (ExecutionException e) {
				log.error("Thread failed with exception", e.getCause());
			}
		}

		// Then - verify results
		log.info("Stress test completed: {} successful updates, {} failed updates",
			successfulUpdates.get(), failedUpdates.get());

		// Verify document exists and is not corrupted
		DataServicesDocument dbDoc = documentService.load(testDocRef)
			.orElseThrow(() -> new IllegalStateException("Document not found"));
		String dbCounterStr = getFieldValue(dbDoc, NAME_FIELD_PATH);
		int dbCounter = Integer.parseInt(dbCounterStr);

		log.info("Final counter value in database: {}", dbCounter);

		// Key assertion: Document value should be a valid integer (no data corruption)
		// Due to concurrent overwrites, the final value may be less than total successful operations
		assertTrue(dbCounter >= 0, "Database counter should be non-negative (no corruption)");
		assertTrue(dbCounter <= successfulUpdates.get(),
			"Database counter should not exceed the number of attempted increments");

		// Key assertion: Index must be consistent with database
		QueryRoot query = buildVerificationQuery(testDocRef, NAME_FIELD_PATH, dbCounterStr);
		QueryPage<DocumentTreeResult> indexResult = queryService.query(query, null);

		assertEquals(indexResult.getTotalElements(), 1, "Document should exist in index");

		// Verify at least some updates succeeded
		assertTrue(dbCounter > 0, "At least some updates should have succeeded");
		log.info("High concurrency stress test passed - database and index are consistent");
	}

	/**
	 * Performs an atomic increment of the counter field using a dedicated transaction.
	 *
	 * @param threadId The thread identifier for logging
	 * @param iteration The iteration number within the thread
	 * @return true if the increment was successful, false otherwise
	 */
	private boolean performAtomicIncrement(int threadId, int iteration) {
		TransactionTemplate txTemplate = createNewTransactionTemplate();

		try {
			txTemplate.executeWithoutResult(status -> {
				// Read current value
				DataServicesDocument doc = documentService.load(testDocRef)
					.orElseThrow(() -> new IllegalStateException("Document not found"));
				String currentValueStr = getFieldValue(doc, NAME_FIELD_PATH);
				int currentValue = Integer.parseInt(currentValueStr);

				// Increment
				int newValue = currentValue + 1;

				// Update document
				DocumentV2 newDocument = updateDocumentFieldViaJson(doc, NAME_FIELD_PATH, String.valueOf(newValue));
				documentService.update(testDocRef, newDocument, null);

				log.debug("Thread-{} iteration-{}: incremented {} -> {}",
					threadId, iteration, currentValue, newValue);
			});
			return true;
		} catch (Exception e) {
			log.debug("Thread-{} iteration-{}: failed with {}", threadId, iteration, e.getMessage());
			return false;
		}
	}

	/**
	 * Stress test: Verifies that rolled back transactions do not affect the final database or index state.
	 *
	 * Test scenario:
	 * 1. Create a document with a counter field
	 * 2. Spawn 10 threads performing successful increments
	 * 3. Spawn 10 threads performing increments followed by forced rollback
	 * 4. Verify database and index are consistent
	 * 5. Verify document contains a valid value (no corruption from rolled back transactions)
	 *
	 * Note: Due to READ_COMMITTED isolation with concurrent updates, the final counter value
	 * reflects last-write-wins behavior among successful commits. The key verification is that
	 * rolled back transactions do NOT corrupt the database or cause index inconsistency.
	 *
	 * Expected result: Database and index are consistent, rolled back values are not persisted.
	 */
	@Test(description = "Should maintain consistency when some transactions fail",
		  timeOut = 120000)
	public void shouldMaintainConsistencyWhenSomeTransactionsFail() throws InterruptedException {
		// Given
		CountDownLatch startLatch = new CountDownLatch(1);
		AtomicInteger successfulUpdates = new AtomicInteger(0);
		AtomicInteger rolledBackUpdates = new AtomicInteger(0);

		ExecutorService executor = Executors.newFixedThreadPool(20);
		List<Future<Void>> futures = new ArrayList<>();

		// When - spawn 10 threads that will succeed and 10 that will rollback
		for (int t = 0; t < 10; t++) {
			final int threadId = t;
			// Successful threads
			Callable<Void> successTask = () -> {
				try {
					startLatch.await();
					for (int i = 0; i < 5; i++) {
						boolean success = performAtomicIncrement(threadId, i);
						if (success) {
							successfulUpdates.incrementAndGet();
						}
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				return null;
			};
			futures.add(executor.submit(successTask));

			// Rollback threads
			Callable<Void> rollbackTask = () -> {
				try {
					startLatch.await();
					for (int i = 0; i < 5; i++) {
						performIncrementWithRollback(threadId + 10, i);
						rolledBackUpdates.incrementAndGet();
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				return null;
			};
			futures.add(executor.submit(rollbackTask));
		}

		// Start all threads
		log.info("Starting mixed success/rollback stress test");
		startLatch.countDown();

		// Wait for completion
		executor.shutdown();
		boolean finished = executor.awaitTermination(90, TimeUnit.SECONDS);
		assertTrue(finished, "Executor did not finish within timeout");

		// Then - verify results
		log.info("Mixed stress test completed: {} successful, {} rolled back",
			successfulUpdates.get(), rolledBackUpdates.get());

		// Verify final counter value in database
		DataServicesDocument dbDoc = documentService.load(testDocRef)
			.orElseThrow(() -> new IllegalStateException("Document not found"));
		String dbCounterStr = getFieldValue(dbDoc, NAME_FIELD_PATH);
		int dbCounter = Integer.parseInt(dbCounterStr);

		log.info("Final counter value in database: {}", dbCounter);

		// Key assertion: Value should be valid and reflect only committed changes
		// Due to concurrent overwrites, value may be less than successful operation count
		assertTrue(dbCounter >= 0, "Database counter should be non-negative (no corruption)");
		assertTrue(dbCounter <= successfulUpdates.get(),
			"Database counter should not exceed the number of successful commits");
		assertTrue(dbCounter > 0, "At least some successful updates should be reflected");

		// Key assertion: Index must be consistent with database
		QueryRoot query = buildVerificationQuery(testDocRef, NAME_FIELD_PATH, dbCounterStr);
		QueryPage<DocumentTreeResult> indexResult = queryService.query(query, null);
		assertEquals(indexResult.getTotalElements(), 1, "Document should exist in index");

		log.info("Mixed success/rollback stress test passed - database and index are consistent");
	}

	/**
	 * Performs an increment followed by a forced rollback.
	 */
	private void performIncrementWithRollback(int threadId, int iteration) {
		TransactionTemplate txTemplate = createNewTransactionTemplate();

		try {
			txTemplate.executeWithoutResult(status -> {
				DataServicesDocument doc = documentService.load(testDocRef)
					.orElseThrow(() -> new IllegalStateException("Document not found"));
				String currentValueStr = getFieldValue(doc, NAME_FIELD_PATH);
				int currentValue = Integer.parseInt(currentValueStr);
				int newValue = currentValue + 1;

				DocumentV2 newDocument = updateDocumentFieldViaJson(doc, NAME_FIELD_PATH, String.valueOf(newValue));
				documentService.update(testDocRef, newDocument, null);

				// Force rollback
				throw new RuntimeException("Intentional rollback");
			});
		} catch (RuntimeException e) {
			log.debug("Thread-{} iteration-{}: rolled back as expected", threadId, iteration);
		}
	}
}
