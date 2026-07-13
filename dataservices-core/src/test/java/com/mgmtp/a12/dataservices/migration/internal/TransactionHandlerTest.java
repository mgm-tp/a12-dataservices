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
package com.mgmtp.a12.dataservices.migration.internal;

import java.util.concurrent.atomic.AtomicBoolean;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.internal.TransactionHandler;

import static org.testng.Assert.assertTrue;

public class TransactionHandlerTest {

	private TransactionHandler transactionHandler;

	@BeforeMethod
	public void setUp() {
		transactionHandler = new TransactionHandler();
	}

	@Test(description = "Should execute runnable in default transaction")
	public void shouldExecuteRunnableInDefaultTransaction() {
		AtomicBoolean executed = new AtomicBoolean(false);

		transactionHandler.runMethodInDefaultTransaction(() -> executed.set(true));

		assertTrue(executed.get(), "Runnable should have been executed");
	}

	@Test(description = "Should execute runnable in new transaction")
	public void shouldExecuteRunnableInNewTransaction() {
		AtomicBoolean executed = new AtomicBoolean(false);

		transactionHandler.runMethodInNewTransaction(() -> executed.set(true));

		assertTrue(executed.get(), "Runnable should have been executed");
	}

	@Test(description = "Should execute runnable in read-only transaction")
	public void shouldExecuteRunnableInReadOnlyTransaction() {
		AtomicBoolean executed = new AtomicBoolean(false);

		transactionHandler.runMethodInReadOnlyTransaction(() -> executed.set(true));

		assertTrue(executed.get(), "Runnable should have been executed");
	}

	@Test(description = "Should propagate exceptions from runnable",
		expectedExceptions = RuntimeException.class,
		expectedExceptionsMessageRegExp = "Test exception")
	public void shouldPropagateExceptionsFromRunnable() {
		transactionHandler.runMethodInDefaultTransaction(() -> {
			throw new RuntimeException("Test exception");
		});
	}
}
