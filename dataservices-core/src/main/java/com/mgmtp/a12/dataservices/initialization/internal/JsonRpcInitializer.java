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
package com.mgmtp.a12.dataservices.initialization.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcBasicServer;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2ResponseError;
import com.mgmtp.a12.dataservices.utils.internal.DsResourceUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonRpcInitializer {

	public static final String JSON_RPC_ERROR_FOR_PATH = "Json-rpc error for path '%s'";
	public static final String JSON_RPC_SUCCESS_FOR_PATH = "Json-rpc success for path '{}'";
	static final String JSON_RPC_SUCCESS_FOR_PATH_CONTENT = JSON_RPC_SUCCESS_FOR_PATH.concat(": {}");
	static final String JSON_RPC_ERROR_FOR_PATH_EXCEPTION = JSON_RPC_ERROR_FOR_PATH.concat(": %s");
	private final JsonRpcBasicServer jsonRpcBasicServer;
	private final ObjectMapper objectMapper;
	private final List<String> paths;
	private final ResourcePatternResolver resourcePatternResolver;

	public JsonRpcInitializer(JsonRpcBasicServer jsonRpcBasicServer, ObjectMapper objectMapper, List<String> paths,
		ResourcePatternResolver resourcePatternResolver) {
		this.jsonRpcBasicServer = jsonRpcBasicServer;
		this.objectMapper = objectMapper.copy().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
		this.paths = paths;
		this.resourcePatternResolver = resourcePatternResolver;
	}

	@Transactional
	@SneakyThrows
	public void execute() {
		StopWatch stopWatch = StopWatch.createStarted();
		log.info("Running json-rpc requests on initialization");
		for (String path : paths) {
			executeForSinglePath(path);
		}
		log.info("Json-rpc requests initialized in {} ms.", stopWatch.getTime());
	}

	private void executeForSinglePath(String path) throws IOException {
		Resource[] resources = resourcePatternResolver.getResources(DsResourceUtils.addSchemeIfMissing(path));
		for (Resource resource : resources) {
			executeForSingleResource(resource);
		}
	}

	private void executeForSingleResource(Resource resource) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		jsonRpcBasicServer.handleRequest(resource.getInputStream(), outputStream);
		String response = outputStream.toString();

		Arrays.stream(objectMapper.readValue(response, JsonRpc2Response[].class))
			.filter(r -> !r.isSuccess())
			.map(JsonRpc2Response::getError)
			.map(JsonRpc2ResponseError::toString)
			.forEach(e -> {
				String errorMessage = String.format(JSON_RPC_ERROR_FOR_PATH_EXCEPTION, sneakyGetURI(resource), e);
				log.error(errorMessage);
				throw new UnexpectedException(errorMessage).withAnonymityMessage("Rpc initialization failed.");
			});

		if (log.isDebugEnabled()) {
			log.debug(JSON_RPC_SUCCESS_FOR_PATH_CONTENT, sneakyGetURI(resource), response);
		} else {
			log.info(JSON_RPC_SUCCESS_FOR_PATH, sneakyGetURI(resource));
		}
	}

	@SneakyThrows
	private String sneakyGetURI(Resource resource) {
		return resource.getURI().toString();
	}
}
