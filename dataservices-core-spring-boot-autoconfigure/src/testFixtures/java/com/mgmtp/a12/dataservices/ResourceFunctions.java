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
package com.mgmtp.a12.dataservices;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component public class ResourceFunctions {

	@Autowired private ResourceLoader resourceLoader;
	@Autowired private ObjectMapper objectMapper;

	public String loadResource(final String relativePath) throws IOException {
		return loadResourceFromClassPath(relativePath)
			.getContentAsString(StandardCharsets.UTF_8);
	}

	public Reader loadResourceAsReader(final String resourcePath) throws IOException {
		return new InputStreamReader(loadResourceAsStream(resourcePath));
	}

	public InputStream loadResourceAsStream(final String resourcePath) throws IOException {
		final Resource resource = loadResourceFromClassPath(resourcePath);
		return resource.getInputStream();
	}

	public <T> T loadResourceAsObject(String resourcePath, TypeReference<T> valueTypeRef, Object... params) throws IOException {
		return objectMapper.readValue(String.format(loadResource(resourcePath), params), valueTypeRef);
	}

	public Resource loadResourceFromClassPath(String classPath) {
		return resourceLoader.getResource(String.format("classpath:%s", classPath));
	}
}
