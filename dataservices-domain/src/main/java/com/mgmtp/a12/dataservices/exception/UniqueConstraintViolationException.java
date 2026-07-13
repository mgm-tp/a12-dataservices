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

import lombok.Getter;

/**
 * Thrown when a document violates a named uniqueness constraint defined on its Document Model.
 *
 * Extends `IntegrityException` so existing callers that catch `IntegrityException` continue
 * to work. Callers that need to handle unique constraint violations specifically can catch
 * this type directly and access the structured `constraintName` and `modelName` fields.
 *
 * Always uses error code `-32060` (`UNIQUE_CONSTRAINT_VIOLATION_EXCEPTION_CODE`).
 */
@Getter
public class UniqueConstraintViolationException extends IntegrityException {

	private final String constraintName;
	private final String modelName;

	/**
	 * Creates an exception carrying the constraint and model name.
	 *
	 * @param constraintName the name of the violated uniqueness constraint.
	 * @param modelName the topmost Document Model name the constraint belongs to.
	 */
	public UniqueConstraintViolationException(String constraintName, String modelName) {
		this(constraintName, modelName,
			"Unique constraint '%s' violated for model '%s'".formatted(constraintName, modelName));
	}

	/**
	 * Creates an exception carrying the constraint name, model name, and a localized message.
	 *
	 * @param constraintName the name of the violated uniqueness constraint.
	 * @param modelName the topmost Document Model name the constraint belongs to.
	 * @param localizedMessage the user-facing error message resolved for the current locale.
	 */
	public UniqueConstraintViolationException(String constraintName, String modelName, String localizedMessage) {
		super(ExceptionCodes.UNIQUE_CONSTRAINT_VIOLATION_EXCEPTION_CODE,
			ExceptionKeys.UNIQUE_CONSTRAINT_VIOLATION_ERROR_KEY,
			localizedMessage,
			null);
		this.constraintName = constraintName;
		this.modelName = modelName;
	}
}
