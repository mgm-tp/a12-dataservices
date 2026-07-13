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
package com.mgmtp.a12.dataservices.rpc;

import com.mgmtp.a12.dataservices.rpc.internal.RpcOperationExceptionBuilder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * All RPC exceptions should be instances of RpcException because the proper error structures cannot be guaranteed otherwise.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RpcExceptionSupport {

	/**
	 * Creates an {@link RpcException} with structured details and an operation id.
	 *
	 * @param code Error code to expose to clients.
	 * @param keyPrefix Error key prefix used for localization.
	 * @param title Short error title in English.
	 * @param description Detailed error description in English.
	 * @param operationId Identifier of the operation where the error occurred.
	 * @return A new {@link RpcException} instance.
	 */
	public static RpcException createException(int code, String keyPrefix, String title, String description, String operationId) {
		return RpcOperationExceptionBuilder
			.withKeyPrefix(keyPrefix)
			.withCode(code)
			.withTitle(title)
			.withDescription(description)
			.withOperation(operationId)
			.build();
	}

	/**
	 * Creates an {@link RpcException} with structured details, an operation id, and a cause.
	 *
	 * @param code Error code to expose to clients.
	 * @param keyPrefix Error key prefix used for localization.
	 * @param title Short error title in English.
	 * @param description Detailed error description in English.
	 * @param operationId Identifier of the operation where the error occurred.
	 * @param cause Underlying cause; may be `null`.
	 * @return A new {@link RpcException} instance.
	 */
	public static RpcException createException(int code, String keyPrefix, String title, String description, String operationId, Throwable cause) {
		return RpcOperationExceptionBuilder
			.withKeyPrefix(keyPrefix)
			.withCode(code)
			.withTitle(title)
			.withDescription(description)
			.withOperation(operationId)
			.withCause(cause)
			.build();
	}

	/**
	 * Creates an {@link RpcException} with structured details and no operation id.
	 *
	 * @param code Error code to expose to clients.
	 * @param keyPrefix Error key prefix used for localization.
	 * @param title Short error title in English.
	 * @param description Detailed error description in English.
	 * @return A new {@link RpcException} instance.
	 */
	public static RpcException createException(int code, String keyPrefix, String title, String description) {
		return RpcOperationExceptionBuilder
			.withKeyPrefix(keyPrefix)
			.withCode(code)
			.withTitle(title)
			.withDescription(description)
			.build();
	}
}
