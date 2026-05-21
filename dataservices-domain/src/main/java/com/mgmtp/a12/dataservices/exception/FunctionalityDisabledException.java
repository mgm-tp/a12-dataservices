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
package com.mgmtp.a12.dataservices.exception;

import com.mgmtp.a12.dataservices.common.exception.BaseException;

/**
 * Indicates that a requested functionality is disabled and cannot be invoked.
 * Use to signal feature toggles or restricted operations.
 */
public class FunctionalityDisabledException extends BaseException {

	/**
	 * Creates a FunctionalityDisabledException with a message.
	 *
	 * @param message human-readable description; may be null.
	 */
	public FunctionalityDisabledException(String message) {
		super(ExceptionCodes.FUNCTIONALITY_DISABLED_EXCEPTION_CODE, ExceptionKeys.FUNCTIONALITY_DISABLED_ERROR_KEY, message);
	}

	/**
	 * Creates a FunctionalityDisabledException with explicit code and cause for advanced scenarios.
	 *
	 * @param code numeric error code to expose.
	 * @param key localization key that identifies the error.
	 * @param message human-readable description; may be null.
	 * @param e underlying cause; may be null.
	 */
	protected FunctionalityDisabledException(int code, String key, String message, Throwable e) {
		super(code, key, message, null, e);
	}
}

