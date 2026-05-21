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

import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.common.exception.ErrorDetail;
import com.mgmtp.a12.dataservices.common.exception.ErrorLevel;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.rpc.OperationError;
import com.mgmtp.a12.dataservices.rpc.RpcException;

public class RpcOperationExceptionBuilder {

	private Integer code;
	private final String keyPrefix;
	private String title;
	private String description;
	private String operation;
	private Throwable cause;

	private RpcOperationExceptionBuilder(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

	public static RpcOperationExceptionBuilder withKeyPrefix(String keyPrefix) {
		return new RpcOperationExceptionBuilder(keyPrefix);
	}

	public RpcOperationExceptionBuilder withCode(int code) {
		this.code = code;
		return this;
	}

	public RpcOperationExceptionBuilder withOperation(String operationId) {
		this.operation = operationId;
		return this;
	}

	public RpcOperationExceptionBuilder withDescription(String description) {
		this.description = description;
		return this;
	}

	public RpcOperationExceptionBuilder withTitle(String title) {
		this.title = title;
		return this;
	}

	public RpcOperationExceptionBuilder withCause(Throwable cause) {
		this.cause = cause;
		return this;
	}

	public RpcException build() {
		int responseCode = code == null ? ExceptionCodes.RPC_ERROR_EXCEPTION_CODE : code;
		return new RpcException(title, OperationError.builder()
			.code(responseCode)
			.operationId(operation == null ? "N/A" : operation)
			.level(ErrorLevel.ERROR)
			.shortMessage(new LocalizedEntry(keyPrefix + ".title", title))
			.longMessage(new LocalizedEntry(keyPrefix + ".description", description))
			.errorDetail(new ErrorDetail(responseCode)).build(), cause);
	}
}
