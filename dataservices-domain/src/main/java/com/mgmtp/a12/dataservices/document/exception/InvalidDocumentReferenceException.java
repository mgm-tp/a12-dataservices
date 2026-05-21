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
package com.mgmtp.a12.dataservices.document.exception;


import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.DOCUMENT_REFERENCE_INVALID_INPUT;

/**
 * This exception and its descendants represent errors in document reference being passed to application.
 */
public class InvalidDocumentReferenceException extends BaseException {

	/**
	 * Creates an exception indicating an invalid document reference was provided.
	 *
	 * @param message Human-readable explanation of the validation failure; English text, not localized.
	 */
	public InvalidDocumentReferenceException(String message) {
		super(ExceptionCodes.INVALID_DOCUMENT_REFERENCE_EXCEPTION_CODE, DOCUMENT_REFERENCE_INVALID_INPUT, message);
	}

	/**
	 * Creates an exception with a specific error key for invalid document reference input.
	 *
	 * @param key Error key used for localization and client handling.
	 * @param message Human-readable explanation of the validation failure; English text, not localized.
	 */
	public InvalidDocumentReferenceException(String key, String message) {
		super(ExceptionCodes.INVALID_DOCUMENT_REFERENCE_EXCEPTION_CODE, key, message);
	}

	/**
	 * Creates an exception with an error key and underlying cause for invalid document reference input.
	 *
	 * @param key Error key used for localization and client handling.
	 * @param message Human-readable explanation of the validation failure; English text, not localized.
	 * @param cause Root cause providing additional context; may be null.
	 */
	public InvalidDocumentReferenceException(String key, String message, Throwable cause) {
		super(ExceptionCodes.INVALID_DOCUMENT_REFERENCE_EXCEPTION_CODE, key, message, cause);
	}

	/**
	 * Creates a customized exception instance for advanced scenarios.
	 *
	 * @param code Error code to report to clients and logs.
	 * @param key Error key used for localization and client handling.
	 * @param message Human-readable explanation of the issue; English text.
	 * @param e Root cause; may be null.
	 */
	protected InvalidDocumentReferenceException(int code, String key, String message, Throwable e) {
		super(code, key, message, e);
	}
}
