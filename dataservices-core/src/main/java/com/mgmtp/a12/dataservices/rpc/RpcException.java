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

/**
 * Runtime exception for RPC operations carrying structured `OperationError` details.
 * Extends `RuntimeException` directly because RPC error payloads are already
 * prepared for client consumption via `OperationError`. All other Data Services
 * exceptions should extend `BaseException` instead.
 * 
 * @see OperationError for structured error details  
 */
// TODO [A12S-6311] : we need an exception which doesn't extends from BaseException but ideally from our local exception
public class RpcException extends RuntimeException {

	private final OperationError operationError;

	/**
	 * Creates a new RPC exception with a cause and structured error details.
	 *
	 * @param cause The underlying cause of the failure; may be `null`.
	 * @param operationError The structured error payload; must not be `null`.
	 */
	public RpcException(Throwable cause, OperationError operationError) {
		super(cause);
		this.operationError = operationError;
	}

	/**
	 * Creates a new RPC exception with a message and structured error details.
	 *
	 * @param message Human-readable description; may be `null`.
	 * @param operationError The structured error payload; must not be `null`.
	 */
	public RpcException(String message, OperationError operationError) {
		super(message);
		this.operationError = operationError;
	}

	/**
	 * Creates a new RPC exception with a message, structured error details, and a cause.
	 *
	 * @param message Human-readable description; may be `null`.
	 * @param operationError The structured error payload; must not be `null`.
	 * @param cause The underlying cause of the failure; may be `null`.
	 */
	public RpcException(String message, OperationError operationError, Throwable cause) {
		super(message, cause);
		this.operationError = operationError;
	}

	/**
	 * Returns the structured error payload for this exception.
	 *
	 * @return The associated {@link OperationError}; never `null`.
	 */
	public OperationError getOperationError() {
		return operationError;
	}
}
