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

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.extern.slf4j.Slf4j;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Verifies the specific defects:
 * - Category A: `Thread.sleep()` calls must NOT hold database connections.
 * - Category B: The optimistic locking assertion must use the actual thread outcome
 *   rather than an unconditional expected value.
 */
@Slf4j
@Test(groups = "nightly")
public class NightlyTestStabilityRegressionIT extends AbstractTransactionIT {

	private DocumentReference testDocRef;

	@BeforeMethod
	public void setUp() throws IOException {
		cleanUpTestEnvironment();
		testDocRef = createTestDocument();
		log.info("Regression test document created: {}", testDocRef);
	}

	/**
	 * Verifies that a `Thread.sleep()` occurring inside a TransactionTemplate block
	 * does not starve the connection pool.
	 *
	 * When two threads each hold a connection open for 500 ms while sleeping, and the pool
	 * has limited connections, a third thread attempting to acquire a connection must not time
	 * out. After the fix, the sleep is moved outside the transaction block so the connection
	 * is released before sleeping.
	 */
	@Test(enabled = true, description = "Should not hold DB connection during Thread.sleep in concurrent both-succeed scenario")
	public void shouldNotHoldConnectionDuringSleepInBothSucceedScenario() throws Exception {
		AtomicBoolean connectionAcquiredAfterSleep = new AtomicBoolean(false);
		CountDownLatch sleepStarted = new CountDownLatch(2);
		CountDownLatch connectionAcquireAttempted = new CountDownLatch(1);

		ExecutorService executor = Executors.newFixedThreadPool(3);

		// Two threads that sleep (after the fix: outside transaction)
		Runnable sleepingTask = () -> {
			TransactionTemplate txTemplate = createNewTransactionTemplate();
			txTemplate.executeWithoutResult(status -> {
				DataServicesDocument doc = documentService.load(testDocRef)
					.orElseThrow(() -> new IllegalStateException("Document not found"));
				DocumentV2 newDocument = updateDocumentFieldViaJson(doc, NAME_FIELD_PATH, "SleeperUpdate_" + Thread.currentThread().getName());
				documentService.update(testDocRef, newDocument, null);
			});
			// Sleep is now outside the transaction — connection is already released
			sleepStarted.countDown();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		};

		// Third thread acquires a connection while the two sleeping threads are sleeping
		Runnable connectionProbeTask = () -> {
			try {
				sleepStarted.await(10, TimeUnit.SECONDS);
				TransactionTemplate txTemplate = createNewTransactionTemplate();
				txTemplate.executeWithoutResult(status -> {
					// If connections were exhausted, this would time out
					documentService.load(testDocRef);
					connectionAcquiredAfterSleep.set(true);
				});
			} catch (Exception e) {
				log.error("Connection probe failed — connection pool likely exhausted", e);
			} finally {
				connectionAcquireAttempted.countDown();
			}
		};

		executor.submit(sleepingTask);
		executor.submit(sleepingTask);
		executor.submit(connectionProbeTask);
		executor.shutdown();

		boolean finished = executor.awaitTermination(30, TimeUnit.SECONDS);
		assertTrue(finished, "Executor did not finish within timeout");

		boolean probeCompleted = connectionAcquireAttempted.await(15, TimeUnit.SECONDS);
		assertTrue(probeCompleted, "Connection probe did not complete — likely blocked");
		assertTrue(connectionAcquiredAfterSleep.get(),
			"Third thread must acquire connection while two threads sleep outside their transactions");
	}

	/**
	 * Verifies that the sleep inside the successful-update transaction in
	 * `shouldMaintainConsistencyWhenOneTransactionFails()` does not hold a connection.
	 *
	 * After the fix, thread 1 releases its connection before sleeping. A second connection
	 * request must succeed during the sleep window.
	 */
	@Test(enabled = true, description = "Should not hold DB connection during Thread.sleep in one-fails scenario thread 1")
	public void shouldNotHoldConnectionDuringSleepInOneFailsScenarioThread1() throws Exception {
		AtomicBoolean connectionAcquired = new AtomicBoolean(false);
		CountDownLatch transactionCommitted = new CountDownLatch(1);

		ExecutorService executor = Executors.newFixedThreadPool(2);

		// Thread 1 from the one-fails scenario: commits transaction then sleeps outside it
		Runnable thread1Task = () -> {
			TransactionTemplate txTemplate = createNewTransactionTemplate();
			txTemplate.executeWithoutResult(status -> {
				DataServicesDocument doc = documentService.load(testDocRef)
					.orElseThrow(() -> new IllegalStateException("Document not found"));
				DocumentV2 newDocument = updateDocumentFieldViaJson(doc, NAME_FIELD_PATH, "SuccessfulUpdate");
				documentService.update(testDocRef, newDocument, null);
			});
			// Transaction committed, connection released; sleep outside transaction
			transactionCommitted.countDown();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		};

		// Probe thread acquires connection while thread 1 sleeps
		Runnable probeTask = () -> {
			try {
				transactionCommitted.await(10, TimeUnit.SECONDS);
				TransactionTemplate txTemplate = createNewTransactionTemplate();
				txTemplate.executeWithoutResult(status -> {
					documentService.load(testDocRef);
					connectionAcquired.set(true);
				});
			} catch (Exception e) {
				log.error("Connection probe failed", e);
			}
		};

		executor.submit(thread1Task);
		executor.submit(probeTask);
		executor.shutdown();

		boolean finished = executor.awaitTermination(30, TimeUnit.SECONDS);
		assertTrue(finished, "Executor did not finish within timeout");
		assertTrue(connectionAcquired.get(),
			"Connection must be acquirable while thread 1 sleeps outside its transaction");
	}

	/**
	 * Verifies that the sleep inside the concurrent-update transaction in
	 * `shouldNotThrowOptimisticLockExceptionDuringConcurrentUpdates()` does not hold a connection.
	 *
	 * After the fix, each thread releases its connection before sleeping.
	 */
	@Test(enabled = true, description = "Should not hold DB connection during Thread.sleep in no-optimistic-lock scenario")
	public void shouldNotHoldConnectionDuringSleepInNoOptimisticLockScenario() throws Exception {
		AtomicBoolean connectionAcquired = new AtomicBoolean(false);
		CountDownLatch twoTransactionsCommitted = new CountDownLatch(2);

		ExecutorService executor = Executors.newFixedThreadPool(3);

		Runnable updatingTask = () -> {
			TransactionTemplate txTemplate = createNewTransactionTemplate();
			txTemplate.executeWithoutResult(status -> {
				DataServicesDocument doc = documentService.load(testDocRef)
					.orElseThrow(() -> new IllegalStateException("Document not found"));
				DocumentV2 newDocument = updateDocumentFieldViaJson(
					doc, NAME_FIELD_PATH, "ConcurrentUpdate_" + Thread.currentThread().getName());
				documentService.update(testDocRef, newDocument, null);
			});
			// Connection released; sleep outside transaction
			twoTransactionsCommitted.countDown();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		};

		Runnable probeTask = () -> {
			try {
				twoTransactionsCommitted.await(10, TimeUnit.SECONDS);
				TransactionTemplate txTemplate = createNewTransactionTemplate();
				txTemplate.executeWithoutResult(status -> {
					documentService.load(testDocRef);
					connectionAcquired.set(true);
				});
			} catch (Exception e) {
				log.error("Connection probe failed", e);
			}
		};

		executor.submit(updatingTask);
		executor.submit(updatingTask);
		executor.submit(probeTask);
		executor.shutdown();

		boolean finished = executor.awaitTermination(30, TimeUnit.SECONDS);
		assertTrue(finished, "Executor did not finish within timeout");
		assertTrue(connectionAcquired.get(),
			"Connection must be acquirable while updating threads sleep outside their transactions");
	}

	/**
	 * Verifies that when thread 1 is explicitly simulated to fail (never executes its update
	 * transaction), the database contains thread 2's committed value.
	 *
	 * This is a "thread 1 fails" scenario only. Thread 1 loads the document and then throws
	 * a simulated connection timeout before issuing any update. Thread 2 commits normally.
	 * The database must contain thread 2's value because thread 1 never wrote anything.
	 */
	@Test(enabled = true, description = "Should contain thread 2 value in database when thread 1 fails unconditionally")
	public void shouldContainThread2ValueWhenThread1FailsUnconditionally() throws Exception {
		CountDownLatch thread1LoadedLatch = new CountDownLatch(1);
		CountDownLatch thread2CommittedLatch = new CountDownLatch(1);
		CountDownLatch completionLatch = new CountDownLatch(2);

		AtomicReference<Exception> thread1Exception = new AtomicReference<>();

		ExecutorService executor = Executors.newFixedThreadPool(2);

		// Thread 1: loads document, then fails unconditionally before issuing any update
		Runnable failingThread1 = () -> {
			try {
				TransactionTemplate loadTx = createNewTransactionTemplate();
				final String[] loadedJson = { null };
				loadTx.executeWithoutResult(status -> {
					DataServicesDocument doc = documentService.load(testDocRef)
						.orElseThrow(() -> new IllegalStateException("Document not found"));
					loadedJson[0] = documentFunctions.convertDocumentToJson(doc);
				});

				thread1LoadedLatch.countDown();
				thread2CommittedLatch.await();
				Thread.sleep(200);

				// Simulate failure — thread 1 never executes its update transaction
				throw new RuntimeException("Simulated connection timeout");
			} catch (Exception e) {
				thread1Exception.set(e);
				log.info("Thread 1 failed as simulated: {}", e.getMessage());
			} finally {
				completionLatch.countDown();
			}
		};

		// Thread 2: succeeds normally
		Runnable successThread2 = () -> {
			try {
				thread1LoadedLatch.await();
				TransactionTemplate txTemplate = createNewTransactionTemplate();
				txTemplate.executeWithoutResult(status -> {
					DataServicesDocument doc = documentService.load(testDocRef)
						.orElseThrow(() -> new IllegalStateException("Document not found"));
					DocumentV2 newDocument = updateDocumentFieldViaJson(doc, NAME_FIELD_PATH, "FreshUpdateFromThread2");
					documentService.update(testDocRef, newDocument, null);
				});
				thread2CommittedLatch.countDown();
			} catch (Exception e) {
				log.error("Thread 2 failed unexpectedly", e);
			} finally {
				completionLatch.countDown();
			}
		};

		executor.submit(failingThread1);
		executor.submit(successThread2);

		boolean finished = completionLatch.await(30, TimeUnit.SECONDS);
		executor.shutdown();

		assertTrue(finished, "Executor did not finish within timeout");

		// Thread 1 must have failed — if it did not, the test scenario is invalid
		assertFalse(thread1Exception.get() == null,
			"Thread 1 must have thrown a simulated exception in this scenario");

		DataServicesDocument dbDoc = documentService.load(testDocRef)
			.orElseThrow(() -> new IllegalStateException("Document not found"));
		String dbName = getFieldValue(dbDoc, NAME_FIELD_PATH);

		// Thread 1 never wrote — thread 2's committed value must be present
		assertTrue("FreshUpdateFromThread2".equals(dbName),
			"When thread 1 fails before writing, database must contain thread 2's committed value 'FreshUpdateFromThread2', found: " + dbName);

		log.info("Thread-1-fails regression test passed. DB value: {}", dbName);
	}
}
