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
package com.mgmtp.a12.dataservices.internal;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-local holder that tracks which datasource (primary or replica) the current thread
 * should use.
 *
 * The dispatcher sets the type before opening a transaction via {@link #setDataSourceType} and
 * clears it in a `finally` block via {@link #clearDataSourceType}. When no type has been set,
 * {@link #getDataSourceType} falls back to {@link DataSourceType#PRIMARY}.
 *
 * *Restriction:* non-mutating operations routed to the replica must not open a write transaction
 * (e.g. `Propagation.REQUIRES_NEW` without `readOnly = true`) anywhere in their call chain.
 * Doing so will cause a database error because the replica datasource is read-only.
 *
 * Note: this context is thread-local and is NOT propagated to child threads. Spawned threads
 * always start with an empty context and fall back to {@link DataSourceType#PRIMARY}.
 */
@Slf4j
public final class DataSourceContextHolder {

	/**
	 * Identifies which datasource a thread should target.
	 */
	public enum DataSourceType {
		/** The main read-write datasource. */
		PRIMARY,
		/** The read-only replica datasource. */
		REPLICA
	}

	private static final ThreadLocal<DataSourceType> CONTEXT = new ThreadLocal<>();

	private DataSourceContextHolder() {
	}

	/**
	 * Sets the datasource type for the current thread.
	 * Must be paired with {@link #clearDataSourceType} in a `finally` block.
	 */
	public static void setDataSourceType(DataSourceType type) {
		CONTEXT.set(type);
		log.debug("Set datasource type to: {}", type);
	}

	/**
	 * Returns the datasource type for the current thread,
	 * or {@link DataSourceType#PRIMARY} if none has been set.
	 */
	public static DataSourceType getDataSourceType() {
		DataSourceType type = CONTEXT.get();
		return type != null ? type : DataSourceType.PRIMARY;
	}

	/**
	 * Returns `true` if a datasource type has been set on the current thread.
	 */
	public static boolean isDataSourceTypeSet() {
		return CONTEXT.get() != null;
	}

	/**
	 * Removes the datasource type from the current thread.
	 */
	public static void clearDataSourceType() {
		DataSourceType removed = CONTEXT.get();
		CONTEXT.remove();
		log.debug("Cleared datasource type: {}", removed);
	}

	/**
	 * Unconditionally removes the datasource type from the current thread.
	 * Use only in error-recovery paths where normal cleanup cannot be guaranteed.
	 */
	public static void forceClear() {
		CONTEXT.remove();
		log.warn("Force cleared datasource context");
	}
}
