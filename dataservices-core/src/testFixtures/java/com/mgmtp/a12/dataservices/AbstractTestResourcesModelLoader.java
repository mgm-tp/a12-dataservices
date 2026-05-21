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

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.model.Model;
import com.mgmtp.a12.model.header.DefaultHeaderParser;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderParser;

import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor public abstract class AbstractTestResourcesModelLoader<T extends Model> implements IModelLoader<T> {
	protected final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	private final HeaderParser headerParser = new DefaultHeaderParser();
	private final Map<String, Resource> customModels = new TreeMap<>();

	@Override public T loadModel(@NonNull String modelId) {
		return Optional.ofNullable(customModels.get(modelId))
			.or(() -> findModelOnClasspath(modelId))
			.map(this::toHeaderAndContent)
			.map(p -> deserializeModel(p.getLeft(), p.getRight()))
			.orElseThrow(() -> new NotFoundException("Model {} not found.", modelId));
	}

	@SneakyThrows
	protected Pair<Header, Reader> toHeaderAndContent(@NotNull Resource r) {
		String content = r.getContentAsString(StandardCharsets.UTF_8);
		return Pair.of(headerParser.parseJson(content), new StringReader(content));
	}

	protected abstract T deserializeModel(@NonNull Header h, @NonNull Reader r);

	@SneakyThrows
	@NotNull private Optional<Resource> findModelOnClasspath(String id) {
		Optional<Resource> res = Optional.of(resourcePatternResolver.getResources("classpath*:/models/**/%s.json".formatted(id)))
			.filter(r -> r.length > 0)
			.map(r -> r[0]);
		if (res.isEmpty()) {
			log.warn("Model {} not found at path {}.", id, "classpath*:/models/**/%s.json".formatted(id));
		}
		return res;
	}
}
