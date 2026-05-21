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
package com.mgmtp.a12.dataservices.relationship.exception;


import com.mgmtp.a12.dataservices.common.exception.BaseException;

import static com.mgmtp.a12.dataservices.exception.ExceptionCodes.RELATIONSHIP_VERSION_VALIDATION_EXCEPTION_CODE;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.RELATIONSHIP_VALIDATION_WRONG_VERSION_ERROR_KEY;

/**
 * This exception and its descendants represent errors when the version of relationship model is invalid.
 */
public class RelationshipVersionValidationException extends BaseException {

	/**
	 * Creates an exception indicating the version of a relationship model is invalid.
	 *
	 * @param message a human-readable description of the version validation error.
	 */
	public RelationshipVersionValidationException(String message) {
		super(RELATIONSHIP_VERSION_VALIDATION_EXCEPTION_CODE, RELATIONSHIP_VALIDATION_WRONG_VERSION_ERROR_KEY, message);
	}

	/**
	 * Creates an exception with fully specified code and key for invalid relationship model versions.
	 *
	 * @param code the application-specific error code.
	 * @param key the error key used for localization.
	 * @param message the detailed validation message.
	 * @param e the underlying cause of the validation error; may be null.
	 */
	protected RelationshipVersionValidationException(int code, String key, String message, Throwable e) {
		super(code, key, message, e);
	}
}
