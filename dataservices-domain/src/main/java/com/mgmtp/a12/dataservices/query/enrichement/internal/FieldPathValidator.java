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
package com.mgmtp.a12.dataservices.query.enrichement.internal;

import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;

import static com.mgmtp.a12.dataservices.model.ModelConstants.FIELD_SEPARATOR;

/**
 * Validates field paths used in query enrichment and SQL generation.
 */
public final class FieldPathValidator {

	private FieldPathValidator() {}

	/**
	 * Returns `true` if `fieldPath` is non-null and starts with a leading slash.
	 *
	 * @param fieldPath the field path to check.
	 */
	public static boolean isValidFieldPath(String fieldPath) {
		return fieldPath != null && fieldPath.startsWith(FIELD_SEPARATOR);
	}

	/**
	 * Throws {@link QueryInvalidInputException} if the given field path is null or does not start
	 * with a leading slash.
	 *
	 * @param fieldPath the field path to validate; must not be null and must start with `++/++`.
	 */
	public static void validateFieldPath(String fieldPath) {
		if (!isValidFieldPath(fieldPath)) {
			throw new QueryInvalidInputException(
				ExceptionKeys.ExecutionPhase.QUERY_ENRICHMENT,
				ExceptionKeys.QUERY_INVALID_INPUT_ERROR_KEY,
				"Field path must start with a leading slash: " + fieldPath);
		}
	}
}
