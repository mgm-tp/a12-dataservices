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


import java.io.Serializable;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.dataservices.common.anonymizing.internal.masking.Sensitive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Error object for JSON-RPC 2.0 responses.
 * Encapsulates an error `code`, human-readable `message`, and optional `data` as defined by the JSON-RPC 2.0 specification.
 * The message may contain sensitive text and is therefore marked {@link com.mgmtp.a12.dataservices.common.anonymizing.internal.masking.Sensitive}.
 */
@Data @ToString @NoArgsConstructor @AllArgsConstructor
public class JsonRpc2ResponseError implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Error code for malformed JSON payloads (parse error). JSON-RPC 2.0 value: -32700. */
	public static final int PARSE_ERROR = -32700;
	/** Error code for invalid request envelope. JSON-RPC 2.0 value: -32600. */
	public static final int INVALID_REQUEST = -32600;
	/** Error code for unknown or missing method. JSON-RPC 2.0 value: -32601. */
	public static final int METHOD_NOT_FOUND = -32601;
	/** Error code for invalid method parameters. JSON-RPC 2.0 value: -32602. */
	public static final int INVALID_PARAMS = -32602;
	/** Error code for internal server errors not covered by other codes. JSON-RPC 2.0 value: -32603. */
	public static final int INTERNAL_ERROR = -32603;

	private int code;
	@Sensitive
	private String message;
	private JsonNode data;
}
