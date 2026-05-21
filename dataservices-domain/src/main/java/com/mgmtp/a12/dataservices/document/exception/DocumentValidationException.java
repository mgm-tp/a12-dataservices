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

import java.util.List;

import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.document.DocumentValidationResult;

import lombok.Getter;

import static com.mgmtp.a12.dataservices.common.exception.InvalidInputException.INVALID_INPUT_EXCEPTION_CODE;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.DOCUMENT_VALIDATION_ERROR_KEY;

/**
 * This exception and its descendants represent document validation errors.
 */
@Getter
public class DocumentValidationException extends BaseException {

	private final List<DocumentValidationResult> documentValidationResults;

	/**
	 * Creates an exception representing one or more document validation issues.
	 *
	 * @param documentValidationResults List of validation results describing errors and warnings; may be empty but not null.
	 * @param message Human-readable summary of the validation failure; English text, not localized.
	 */
	public DocumentValidationException(List<DocumentValidationResult> documentValidationResults, String message) {
		// TODO A12S-4049: Add document validation exception code when having breaking release
		super(INVALID_INPUT_EXCEPTION_CODE, DOCUMENT_VALIDATION_ERROR_KEY, message);
		this.documentValidationResults = documentValidationResults;
	}

	/**
	 * Creates a customized exception instance for advanced scenarios.
	 *
	 * @param code Error code to report to clients and logs.
	 * @param key Error key used for localization and client handling.
	 * @param message Human-readable explanation; English text.
	 * @param e Root cause; may be null.
	 * @param documentValidationResults Validation results to attach; may be empty but not null.
	 */
	protected DocumentValidationException(int code, String key, String message, Throwable e, List<DocumentValidationResult> documentValidationResults) {
		super(code, key, message, e);
		this.documentValidationResults = documentValidationResults;
	}
}
