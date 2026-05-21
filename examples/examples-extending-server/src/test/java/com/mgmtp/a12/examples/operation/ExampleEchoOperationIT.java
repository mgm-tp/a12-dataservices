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

import org.apache.commons.io.IOUtils;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.server.rpc.JsonRpcControllerImpl;
import com.mgmtp.a12.examples.AbstractITBase;

import lombok.SneakyThrows;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ExampleEchoOperationIT extends AbstractITBase {

	@Autowired JsonRpcControllerImpl jsonRpcController;
	@Autowired ObjectMapper objectMapper;

	@SneakyThrows
	@Test public void testEchoOperationDefaultVersion() {
		ResponseEntity<ByteArrayResource> response = jsonRpcController.jsonRpc(getClass().getResourceAsStream("/rpc/echoRequest.json"), null);
		JSONAssert.assertEquals(IOUtils.toString(getClass().getResourceAsStream("/rpc/echoResponse.json"), UTF_8),
			IOUtils.toString(response.getBody().getInputStream(), UTF_8), false);
	}
}
