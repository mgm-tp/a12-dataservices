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

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper describing a content stream, its MIME type, and readiness for download.
 * The stream is provided lazily via {@link java.util.function.Supplier}, allowing delayed access.
 * The `ready` flag indicates whether the content may be downloaded, and `isPublic` marks public availability.
 */
@Data @NoArgsConstructor @AllArgsConstructor(access = AccessLevel.PRIVATE) @Builder(toBuilder = true)
public class ContentStream {
	private Supplier<InputStream> contentSupplier;
	private String contentType;
	private boolean ready;
	@Builder.Default
	private boolean isPublic = false;

	private final Lock readyLock = new ReentrantLock();
	private final Condition isReadyCondition = readyLock.newCondition();

	public void setReady() {
		readyLock.lock();
		try {
			this.ready = true;
			isReadyCondition.signalAll(); // Notify all waiting threads
		} finally {
			readyLock.unlock();
		}
	}

	/*
	 * The method for waiting: using the Condition variable
	 */
	public boolean awaitReady(long timeoutMs) throws InterruptedException, TimeoutException {
		long nanosTimeout = TimeUnit.MILLISECONDS.toNanos(timeoutMs);
		readyLock.lock();
		try {
			while (!ready) {
				if (nanosTimeout <= 0) {
					throw new TimeoutException("Content not ready within timeout");
				}
				// await() releases the lock and waits for signalAll() or timeout
				nanosTimeout = isReadyCondition.awaitNanos(nanosTimeout);
			}
			return true;
		} finally {
			readyLock.unlock();
		}
	}

	/**
	 * Builder class for {@link ContentStream}.
	 */
	public static class ContentStreamBuilder {}
}
