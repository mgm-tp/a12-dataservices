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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.events.ModelsAfterImportEvent;
import com.mgmtp.a12.dataservices.utils.internal.DsResourceUtils;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderParseException;
import com.mgmtp.a12.model.header.HeaderParser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Shared component for importing models one-by-one from filesystem paths or ZIP archives.
 *
 * Used by {@link BusinessModelInitializer} for startup imports and by the REST endpoint
 * for runtime archive imports. Each model is created or updated individually via
 * {@link ModelService#create(String)} and {@link ModelService#update(String)}.
 */
@RequiredArgsConstructor
@Slf4j
@Component public class RuntimeModelImporter {

	private final ModelService modelService;
	private final HeaderParser headerParser;
	private final DsResourceUtils dsResourceUtils;
	private final ApplicationEventPublisher eventPublisher;

	/**
	 * Imports all JSON model files found at the given path.
	 *
	 * @param path filesystem or classpath location to scan for JSON model files; can be a directory
	 *             or a ZIP/JAR archive (resolved transparently by {@link DsResourceUtils}).
	 * @param configuration import configuration controlling overwrite behavior; must not be null.
	 * @param failOnError if true, throws {@link InvalidInputException} when models permanently fail.
	 * @return sorted set of imported model IDs.
	 * @throws IOException if the path cannot be resolved or resources cannot be read.
	 */
	public SortedSet<String> importModels(String path, ModelImportConfiguration configuration, boolean failOnError) throws IOException {

		List<ImportResult> results = dsResourceUtils.getJsonResources(path)
			.map(resource -> tryToImportJsonResourceAsModel(configuration, resource))
			.filter(Objects::nonNull)
			.toList();
		Set<Header> importedHeaders = results.stream()
			.filter(ImportResult::success)
			.map(ImportResult::modelHeader)
			.collect(Collectors.toSet());
		List<String> failedModelIds = results.stream()
			.filter(h -> !h.success())
			.map(ImportResult::modelHeader)
			.map(Header::getId)
			.toList();

		if (!importedHeaders.isEmpty()) {
			eventPublisher.publishEvent(new ModelsAfterImportEvent(importedHeaders));
		}

		// Individual model import exceptions are caught in tryToImportJsonResourceAsModel (e.g. unresolved
		// dependencies), collected as failed IDs, and reported here as a single aggregate error.
		if (failOnError && !failedModelIds.isEmpty()) {
			throw new InvalidInputException(ExceptionKeys.MODEL_IMPORT_GENERIC_ERROR_KEY, "Failed to import models: " + String.join(", ", failedModelIds));
		}

		return importedHeaders.stream()
			.map(Header::getId)
			.sorted()
			.collect(Collectors.toCollection(TreeSet::new));
	}

	/**
	 * Imports a single model from its JSON content string. The model is created if it does not
	 * exist yet, or updated if it already exists and the configuration allows overwriting.
	 *
	 * @param content the raw JSON model content
	 * @param configuration the import configuration controlling overwrite behavior
	 * @return the model `Header` if imported successfully, or `null` if skipped
	 */
	public Header importRuntimeModel(String content, ModelImportConfiguration configuration) throws HeaderParseException {
		Header header = headerParser.parseJson(content);
		if (!modelService.exists(header)) {
			modelService.create(content);
		} else if (configuration.getOverwriteModel(header.getModelType())) {
			modelService.update(content);
		} else {
			log.info("Skipping existing model: {}", header.getId());
			return null;
		}
		return header;
	}

	private ImportResult tryToImportJsonResourceAsModel(ModelImportConfiguration configuration, Resource resource) {
		try {
			String modelContent = readResource(resource);
			Header header = headerParser.parseJson(modelContent);
			try {
				Header result = importRuntimeModel(modelContent, configuration);
				if (result != null) {
					return new ImportResult(header, true);
				}
			} catch (Exception e) {
				log.warn("Failed to import model {}: {}", header.getId(), e.getMessage(), e);
				return new ImportResult(header, false);
			}
		} catch (Exception e) {
			log.warn("Failed to read or import model resource {}: {}", resource.getDescription(), e.getMessage(), e);
		}
		return null;
	}

	private static String readResource(Resource resource) throws IOException {
		try (InputStream is = resource.getInputStream()) {
			return IOUtils.toString(is, StandardCharsets.UTF_8);
		}
	}

	private record ImportResult(Header modelHeader, boolean success) {}
}
