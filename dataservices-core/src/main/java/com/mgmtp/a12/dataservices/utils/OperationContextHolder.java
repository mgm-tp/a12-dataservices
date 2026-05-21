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
package com.mgmtp.a12.dataservices.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds per-thread operation context for RPC and internal processing.
 * Uses thread-local storage to keep an operation ID and an associated contextual object per thread.
 * Not thread-safe across threads; each thread maintains an independent context.
 */
public class OperationContextHolder {

	private static final ThreadLocal<Map<String, Object>> OPERATION_ID_TO_CONTEXT_HOLDER = ThreadLocal.withInitial(HashMap::new);

	/**
	 * Operation ID holder.
	 *
	 * Keeps current operation ID to be accessible from context. Available until replaces by {@link #id(String)}.
	 */
	private static final ThreadLocal<String> ID = new ThreadLocal<>();
	private static final ThreadLocal<Boolean> ERROR = ThreadLocal.withInitial(() -> false);

	/**
	 * Clears the current thread's operation context.
	 * Removes the operation ID, error flag, and all stored context objects.
	 */
	public static void clear() {
		noid();
		noError();
		OPERATION_ID_TO_CONTEXT_HOLDER.remove();
	}

	/**
	 * Associates a context object with the given operation ID in the current thread.
	 *
	 * @param operationId The operation identifier; may be `null`.
	 * @param context Arbitrary context object; may be `null`.
	 */
	public static void put(String operationId, Object context) {
		OPERATION_ID_TO_CONTEXT_HOLDER.get().put(operationId, context);
	}

	/**
	 * Associates a context object with the current operation ID.
	 *
	 * @param context Arbitrary context object; may be `null`.
	 */
	public static void put(Object context) {
		put(ID.get(), context);
	}

	/**
	 * Returns the map of all operation contexts for the current thread.
	 *
	 * @return The map keyed by operation ID; never `null`.
	 */
	public static Map<String, Object> get() {
		return OPERATION_ID_TO_CONTEXT_HOLDER.get();
	}

	/**
	 * Sets the current operation ID for this thread.
	 *
	 * @param operationId The operation identifier; may be `null`.
	 */
	public static void id(String operationId) {
		ID.set(operationId);
	}

	/**
	 * Returns the current operation ID for this thread.
	 *
	 * @return The operation identifier; may be `null` if not set.
	 */
	public static String id() {
		return ID.get();
	}

	/**
	 * Removes the current operation ID from this thread's context.
	 */
	public static void noid() {
		ID.remove();
	}

	/**
	 * Marks the current operation as failed in this thread.
	 */
	public static void error() {
		ERROR.set(true);
	}

	/**
	 * Indicates whether the current operation is marked as failed.
	 *
	 * @return `true` if failure is flagged, otherwise `false`.
	 */
	public static boolean isFailed() {
		return ERROR.get() != null && ERROR.get();
	}

	/**
	 * Clears the failure flag for the current operation in this thread.
	 */
	public static void noError() {
		ERROR.remove();
	}

	/**
	 * Returns the context object associated with the current operation ID.
	 *
	 * @return The context object; may be `null` if no context is stored or no ID is set.
	 */
	public static Object getCurrent() {
		return get().get(ID.get());
	}
}
