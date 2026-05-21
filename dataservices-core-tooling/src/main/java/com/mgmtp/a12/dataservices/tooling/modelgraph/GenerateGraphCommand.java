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
package com.mgmtp.a12.dataservices.tooling.modelgraph;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.relationship.internal.ModelGraphService;
import com.mgmtp.a12.dataservices.utils.internal.DsResourceUtils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Slf4j
@RequiredArgsConstructor
@Component
@CommandLine.Command(name = "GenerateGraph", mixinStandardHelpOptions = true)
public class GenerateGraphCommand implements Callable<Integer> {

	private final ModelGraphService modelGraphService;
	private final ObjectMapper objectMapper;
	private final ModelService modelGraphModelService;
	private final ResourcePatternResolver resourcePatternResolver;

	@CommandLine.Parameters
	private String[] paths = {};

	@CommandLine.Option(names = { "-o", "--output" }, description = "Specifies the output file path")
	private String output;

	@CommandLine.Unmatched List<String> ignored;

	/**
	 * Main entry point for the command. Processes input files, generates the model graph,
	 * and writes the output to the specified file.
	 *
	 * @return Exit code: 0 for success, 1 for errors, 2 for missing input files.
	 * @throws Exception if an unexpected error occurs.
	 */
	@Override public Integer call() throws Exception {
		DsResourceUtils dsResourceUtils = new DsResourceUtils(resourcePatternResolver);
		List<Resource> resources = Arrays.stream(paths)
			.filter(StringUtils::isNotBlank)
			.map(DsResourceUtils::addSchemeIfMissing)
			.flatMap(inputs -> getJsonResources(inputs, dsResourceUtils))
			.filter(Objects::nonNull)
			.toList();
		log.debug("Real input: {}", resources.stream().map(Resource::getDescription).collect(Collectors.joining(",")));
		log.debug("Found {} resources.", resources.size());
		if (CollectionUtils.isEmpty(resources)) {
			log.error("No input files.");
			return 2;
		}
		resources.forEach(this::addModel);
		PrintStream outputSink = System.out;
		if (output != null) {
			outputSink = new PrintStream(new FileOutputStream(output));
			log.info("Writing output into {}", output);
		}
		try {
			objectMapper.writeValue(outputSink, modelGraphService.constructModelGraph());
		} catch (Throwable t) {
			if (log.isDebugEnabled()) {
				throw t;
			} else {
				log.error(findRootCause(t).getMessage());
				return 1;
			}
		} finally {
			if (outputSink != null && outputSink != System.out) {
				outputSink.close();
			}
		}
		return 0;
	}

	private static @NonNull Stream<Resource> getJsonResources(String inputs, DsResourceUtils dsResourceUtils) {
		try {
			return dsResourceUtils.getJsonResources(inputs);
		} catch (Exception e) {
			throw new UnexpectedException(e).withAnonymityMessage("JSON resource could not be loaded.");
		}
	}

	private static Throwable findRootCause(Throwable t) {
		return Optional.of(t)
			.filter(ex -> ex.getCause() == null || ex.getCause() == ex)
			.orElseGet(() -> findRootCause(t.getCause()));
	}

	private void addModel(Resource resource) {
		try {
			modelGraphModelService.create(contentOfResource(resource));
		} catch (Throwable e) {
			log.warn("{} couldn't be added. It doesn't look like a model file: {}", resource.getDescription(), e.getMessage());
			if (log.isDebugEnabled()) {
				log.debug("Stacktrace:", e);
			}
		}
	}

	@SneakyThrows
	private String contentOfResource(Resource resource) {
		return IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
	}
}
