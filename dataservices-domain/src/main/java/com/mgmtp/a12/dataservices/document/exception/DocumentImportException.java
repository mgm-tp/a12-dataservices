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

/**
 * This exception and its descendants represent errors when importing documents.
 */
public class DocumentImportException extends BaseException {

	private final String importId;

	/**
	 * Creates an exception indicating an import-related document error without a specific import identifier.
	 *
	 * @param key Error key used for localization and client handling.
	 * @param message Human-readable explanation of the import failure; English text, not localized.
	 */
	public DocumentImportException(String key, String message) {
		this(key, message, "N/A");
	}

	/**
	 * Creates an exception indicating an import-related document error with a specific import identifier.
	 *
	 * @param key Error key used for localization and client handling.
	 * @param message Human-readable explanation of the import failure; English text, not localized.
	 * @param importId Identifier of the import process associated with the error; may be "N/A" if unknown.
	 */
	public DocumentImportException(String key, String message, String importId) {
		super(ExceptionCodes.DOCUMENT_IMPORT_EXCEPTION_CODE, key, message);
		this.importId = importId;
	}

	/**
	 * Creates a customized exception instance for advanced scenarios.
	 *
	 * @param code Error code to report to clients and logs.
	 * @param key Error key used for localization and client handling.
	 * @param message Human-readable explanation; English text.
	 * @param e Root cause; may be null.
	 * @param importId Identifier of the import process associated with the error; may be null.
	 */
	protected DocumentImportException(int code, String key, String message, Throwable e, String importId) {
		super(code, key, message, e);
		this.importId = importId;
	}

	/**
	 * Returns the identifier of the import process associated with this error.
	 *
	 * @return Import identifier; may be "N/A" when not available.
	 */
	public String getImportId() {
		return importId;
	}
}
