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
package com.mgmtp.a12.dataservices.common.exception;


/**
 * Represents unexpected failures occurring during application execution.
 * Used for conditions that do not match a more specific domain error type.
 */
public class UnexpectedException extends BaseException {
	/**
	 * RPC error code identifying unexpected exceptions.
	 */
	public static final int UNEXPECTED_EXCEPTION_CODE = -32007;

	/**
	 * Creates a new UnexpectedException with a human-readable message.
	 *
	 * @param message Human-readable summary; may be null.
	 */
	public UnexpectedException(String message) {
		super(UNEXPECTED_EXCEPTION_CODE, message);
	}

	/**
	 * Creates a new UnexpectedException wrapping an underlying cause.
	 *
	 * @param cause The originating exception; may be null.
	 */
	public UnexpectedException(Throwable cause) {
		super(UNEXPECTED_EXCEPTION_CODE, cause);
	}

	/**
	 * Creates a new UnexpectedException with a message and a cause.
	 *
	 * @param message Human-readable summary; may be null.
	 * @param cause The originating exception; may be null.
	 */
	public UnexpectedException(String message, Throwable cause) {
		super(UNEXPECTED_EXCEPTION_CODE, message, cause);
	}

	/**
	 * Creates a new UnexpectedException with a localization key and default message.
	 *
	 * @param key Localization key used for UI message resolution; may be null.
	 * @param message Default English message; may be null.
	 */
	public UnexpectedException(String key, String message) {
		super(UNEXPECTED_EXCEPTION_CODE, key, message);
	}

	/**
	 * Creates a new UnexpectedException with a custom code.
	 *
	 * @param code RPC error code to use.
	 * @param key Localization key; may be null.
	 * @param message Default English message; may be null.
	 * @param e The originating exception; may be null.
	 */
	protected UnexpectedException(int code, String key, String message, Throwable e) {
		super(code, key, message, null, e);
	}
}
