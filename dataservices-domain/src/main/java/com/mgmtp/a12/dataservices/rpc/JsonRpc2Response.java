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

import java.io.Serial;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.JsonNode;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * JSON-RPC 2.0 response envelope holding either a successful `result` or an {@link JsonRpc2ResponseError}.
 * When `error` is present, `result` is omitted and vice versa.
 */
@Data @NoArgsConstructor @ToString @EqualsAndHashCode(callSuper = true)
public class JsonRpc2Response extends JsonRpc2Message {

	@Serial private static final long serialVersionUID = 1L;

	private JsonRpc2ResponseError error;
	private JsonNode result;

	/**
	 * Creates a response carrying an error object.
	 * @param error the {@link JsonRpc2ResponseError} describing failure; must not be null.
	 */
	public JsonRpc2Response(JsonRpc2ResponseError error) {
		this.error = error;
	}

	/**
	 * Creates a response carrying a successful result.
	 * @param result the JSON payload representing the method result; may be `null` if the method returns no value.
	 */
	public JsonRpc2Response(JsonNode result) {
		this.result = result;
	}

	@Override
	@JsonInclude(JsonInclude.Include.ALWAYS)
	public String getId() {
		return super.getId();
	}

	/**
	 * Indicates whether this response represents a successful call.
	 * @return `true` if no error is present; `false` otherwise.
	 */
	@JsonIgnore
	public boolean isSuccess() {
		return error == null;
	}
}
