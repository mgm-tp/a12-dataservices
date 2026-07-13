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
package com.mgmtp.a12.dataservices.rpc.internal;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.dataservices.common.exception.ErrorDetail;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.exception.InvalidDocumentReferenceException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;

import lombok.NoArgsConstructor;

/**
 * Shared helper for parsing the `docRef` RPC parameter into a {@link DocumentReference}.
 *
 * RPC operations accept `docRef` as `String` rather than `DocumentReference` directly so that
 * Jackson raises errors after method entry. This lets blank/null input be mapped to the same
 * `InvalidInputException` that `@NonNull` would have produced, while genuinely malformed
 * non-blank values propagate as {@link InvalidDocumentReferenceException} unchanged.
 */
@NoArgsConstructor
public final class RpcDocRefParser {

	/**
	 * Parses a raw `docRef` string from an RPC parameter into a {@link DocumentReference}.
	 *
	 * @param docRef the raw string value of the `docRef` RPC parameter; may be null or blank.
	 * @return the parsed {@link DocumentReference}; never null.
	 * @throws InvalidInputException if `docRef` is null or blank.
	 * @throws InvalidDocumentReferenceException if `docRef` is non-blank but has an invalid format.
	 */
	public static DocumentReference parseDocRef(String docRef) {
		try {
			return new DocumentReference(docRef);
		} catch (InvalidDocumentReferenceException e) {
			if (StringUtils.isBlank(docRef)) {
				throw new InvalidInputException(ErrorDetail.RPC_ERROR_EXCEPTION_CODE, ExceptionKeys.INVALID_INPUT_ERROR_KEY, "docRef is marked non-null but is null");
			}
			throw e;
		}
	}
}
