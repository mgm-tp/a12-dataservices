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
package com.mgmtp.a12.dataservices.model.bulkload;

import java.util.List;

import com.mgmtp.a12.dataservices.common.exception.BaseError;
import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;

import lombok.Getter;

/**
 * Aggregated exception describing one or more problems encountered during a model bulk import.
 * Carries a list of {@link BaseError} items with localized messages for presentation.
 */
@Getter public class ModelBulkImportException extends BaseException {

	private final List<BaseError> errors;

	/**
	 * Creates a new exception aggregating import errors.
	 *
	 * @param errors A list of {@link BaseError} instances collected during import; must not be null.
	 * @param message Human-readable summary message describing the import failure.
	 */
	public ModelBulkImportException(List<BaseError> errors, String message) {
		super(ExceptionCodes.MODEL_BULK_IMPORT_EXCEPTION_CODE, message);
		this.errors = errors;
		// We don't have to anonymize the message because it's the user's models that cause these problems
		this.setAnonymityMessage(message);
	}
}
