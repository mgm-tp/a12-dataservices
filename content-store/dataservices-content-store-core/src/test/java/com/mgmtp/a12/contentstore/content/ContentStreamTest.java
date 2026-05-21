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
package com.mgmtp.a12.contentstore.content;

import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class ContentStreamTest {

	@Test
	public void shouldReturnImmediatelyIfAlreadyReady() throws Exception {
		ContentStream contentStream = ContentStream.builder()
			.contentSupplier(() -> new ByteArrayInputStream("data".getBytes()))
			.contentType("text/plain")
			.build();

		contentStream.setReady();

		boolean result = contentStream.awaitReady(1_000);

		assertTrue(result);
	}

	@Test(timeOut = 2_000)
	public void shouldWaitUntilReadyIsSignaled() throws Exception {
		ContentStream contentStream = ContentStream.builder()
			.contentSupplier(() -> new ByteArrayInputStream("async".getBytes()))
			.contentType("text/plain")
			.build();

		ExecutorService executor = Executors.newSingleThreadExecutor();

		Future<Boolean> future = executor.submit(() -> contentStream.awaitReady(5_000));

		// Simulate async producer
		Thread.sleep(200);
		contentStream.setReady();

		assertTrue(future.get(1, TimeUnit.SECONDS));

		executor.shutdownNow();
	}

	@Test(
		timeOut = 1_500,
		expectedExceptions = TimeoutException.class
	)
	public void shouldThrowTimeoutExceptionWhenNotReady() throws Exception {
		ContentStream contentStream = ContentStream.builder()
			.contentSupplier(() -> new ByteArrayInputStream("timeout".getBytes()))
			.contentType("text/plain")
			.build();

		contentStream.awaitReady(500);
	}

	@Test(timeOut = 3_000)
	public void shouldHandleMultipleWaitingThreads() throws Exception {
		ContentStream contentStream = ContentStream.builder()
			.contentSupplier(() -> new ByteArrayInputStream("multi".getBytes()))
			.contentType("text/plain")
			.build();

		ExecutorService executor = Executors.newFixedThreadPool(3);

		Callable<Boolean> task = () -> contentStream.awaitReady(2_000);

		Future<Boolean> f1 = executor.submit(task);
		Future<Boolean> f2 = executor.submit(task);
		Future<Boolean> f3 = executor.submit(task);

		Thread.sleep(200);
		contentStream.setReady();

		assertTrue(f1.get());
		assertTrue(f2.get());
		assertTrue(f3.get());

		executor.shutdownNow();
	}

	@Test(timeOut = 3_000)
	public void shouldReactToThreadInterruption() throws Exception {
		ContentStream contentStream = ContentStream.builder()
			.contentSupplier(() -> new ByteArrayInputStream("interrupt".getBytes()))
			.contentType("text/plain")
			.build();

		ExecutorService executor = Executors.newSingleThreadExecutor();

		Future<Boolean> future = executor.submit(() -> contentStream.awaitReady(5_000));

		Thread.sleep(200);
		future.cancel(true); // Interrupt waiting thread

		try {
			future.get();
			fail("Expected InterruptedException");
		} catch (CancellationException e) {
			// future is canceled
		}

		executor.shutdownNow();
	}
}
