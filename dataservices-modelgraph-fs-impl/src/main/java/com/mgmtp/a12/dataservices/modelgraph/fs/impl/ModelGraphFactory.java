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
package com.mgmtp.a12.dataservices.modelgraph.fs.impl;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import tools.jackson.databind.ObjectMapper;

import com.mgmtp.a12.dataservices.modelgraph.fs.impl.internal.model.FileBasedModelService;
import com.mgmtp.a12.dataservices.modelgraph.fs.impl.internal.model.FileBasedModelTypeService;
import com.mgmtp.a12.dataservices.modelgraph.fs.impl.internal.model.FileBasedRelationshipModelLoader;
import com.mgmtp.a12.dataservices.modelgraph.fs.impl.internal.model.NoOpModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.relationship.ModelGraphRoot;
import com.mgmtp.a12.dataservices.relationship.internal.ModelGraphService;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModelSerializer;
import com.mgmtp.a12.dataservices.relationship.model.internal.DefaultRelationshipModelSerializer;
import com.mgmtp.a12.model.header.DefaultHeaderParser;
import com.mgmtp.a12.model.header.HeaderParser;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Convenience factory for building a {@link ModelGraphRoot} without Spring Boot or server infrastructure.
 *
 * Provides two static factory methods for constructing a model graph in standalone contexts:
 *
 * - {@link #fromContent(List)} — accepts pre-loaded JSON model strings.
 * - {@link #fromResources(List)} — reads JSON model files from given URIs (supports `file:` and `classpath:` schemes).
 *
 * Neither method requires a running Spring application context. Beans are wired internally.
 */
@Slf4j
public final class ModelGraphFactory {

	private ModelGraphFactory() {
		// Utility class — not instantiable
	}

	/**
	 * Builds a {@link ModelGraphRoot} from a list of pre-loaded JSON model content strings.
	 * No filesystem access is performed.
	 *
	 * @param modelContents list of raw JSON strings representing model files; must not be `null`
	 * @return the constructed {@link ModelGraphRoot}
	 */
	public static ModelGraphRoot fromContent(List<String> modelContents) {
		HeaderParser headerParser = new DefaultHeaderParser();
		FileBasedModelService modelService = new FileBasedModelService(headerParser);
		NoOpModelPermissionEvaluator permissionEvaluator = new NoOpModelPermissionEvaluator();

		ObjectMapper objectMapper = new ObjectMapper();
		RelationshipModelSerializer relationshipModelSerializer = new DefaultRelationshipModelSerializer(objectMapper);
		FileBasedRelationshipModelLoader relationshipModelLoader = new FileBasedRelationshipModelLoader(modelService, relationshipModelSerializer);
		FileBasedModelTypeService modelTypeService = new FileBasedModelTypeService(modelService, permissionEvaluator);

		ModelGraphService modelGraphService = new ModelGraphService(
			modelService,
			relationshipModelLoader,
			relationshipModelSerializer,
			modelTypeService,
			Optional.of(modelTypeService),
			permissionEvaluator
		);

		for (String content : modelContents) {
			try {
				modelService.create(content);
			} catch (Throwable e) {
				log.warn("Model content could not be added: {}", e.getMessage());
			}
		}

		return modelGraphService.constructModelGraph();
	}

	/**
	 * Builds a {@link ModelGraphRoot} by reading JSON model files from the provided URIs.
	 * Supports `file:` and `classpath:` URI schemes.
	 *
	 * @param resourceUris list of URIs pointing to JSON model files; must not be `null`
	 * @return the constructed {@link ModelGraphRoot}
	 */
	public static ModelGraphRoot fromResources(List<URI> resourceUris) {
		List<String> modelContents = resourceUris.stream()
			.map(ModelGraphFactory::readContent)
			.toList();
		return fromContent(modelContents);
	}

	@SneakyThrows
	private static String readContent(URI uri) {
		String scheme = uri.getScheme();
		if ("classpath".equals(scheme)) {
			String path = uri.getSchemeSpecificPart();
			Resource resource = new ClassPathResource(path);
			return resource.getContentAsString(StandardCharsets.UTF_8);
		} else {
			Path path = Path.of(uri);
			return Files.readString(path, StandardCharsets.UTF_8);
		}
	}
}
