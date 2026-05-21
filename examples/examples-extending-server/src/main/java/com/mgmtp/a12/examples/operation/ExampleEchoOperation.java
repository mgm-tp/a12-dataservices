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
package com.mgmtp.a12.examples.operation;

import org.springframework.stereotype.Component;

import com.googlecode.jsonrpc4j.JsonRpcMethod;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.utils.OperationContextHolder;

import lombok.extern.slf4j.Slf4j;

/**
 * Demonstrate an operation which has no return value in first version. This is usually for operations which modify a data.
 * No return is represented by `void` where you can return `+null+` or `+Void+` and it's not serialized in the response.
 * The String parameter shows that the message will be stored into the context and can be referenced by subsequent RPC operations in the same request.
 *
 * Since the second version there is a string return value.
 *
 * When the method is called without version, the v1 is used as default for backwards compatibility - see {@link #rpc(String)}.
 *
 * The version 3 is the most recent, that's why other methods are deprecated.
 */
@Slf4j @Component
@RemoteOperation(name = "ECHO") public class ExampleEchoOperation {

	/**
	 * Executes the default (deprecated) ECHO operation.
	 *
	 * @param message the message to echo; may be null.
	 * @deprecated Use {@link #rpcV3(String)} instead.
	 */
	@Deprecated
	public void rpc(@JsonRpcParam("message") String message) {
		rpcV1(message);
	}

	/**
	 * Version 1 of the ECHO operation; logs and stores the message in the operation context without returning a value.
	 *
	 * @param message the message to echo; may be null.
	 * @deprecated Use {@link #rpcV3(String)} for the current version.
	 */
	@Deprecated
	@JsonRpcMethod("1") public void rpcV1(@JsonRpcParam("message") String message) {
		message = "V1: " + message;
		log.info(message);
		OperationContextHolder.put(message);
	}

	/**
	 * Version 2 of the ECHO operation; logs, stores, and returns the message.
	 *
	 * @param message the message to echo; may be null.
	 * @return the echoed message with a +V2+ prefix.
	 * @deprecated Use {@link #rpcV3(String)} for the current version.
	 */
	@Deprecated
	@JsonRpcMethod("2") public String rpcV2(@JsonRpcParam("message") String message) {
		message = "V2: " + message;
		log.info(message);
		OperationContextHolder.put(message);
		return message;
	}

	/**
	 * Version 3 of the ECHO operation; logs, stores, and returns the message.
	 *
	 * @param message the message to echo; may be null.
	 * @return the echoed message with a +V3+ prefix.
	 */
	@JsonRpcMethod("3") public String rpcV3(@JsonRpcParam("message") String message) {
		message = "V3: " + message;
		log.info(message);
		OperationContextHolder.put(message);
		return message;
	}
}
