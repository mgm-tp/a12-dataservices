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
package com.mgmtp.a12.dataservices.modelgraph.fs.impl.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.AbstractFileResolvingResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.relationship.internal.ModelGraphService;

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

	private static final Pattern URI_SCHEME_PATTERN = Pattern.compile("^[a-z]+:.*");

	private final ModelGraphService modelGraphService;
	private final ObjectMapper objectMapper;
	private final ModelService modelService;

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
		List<Resource> resources = Arrays.stream(paths)
			.filter(StringUtils::isNotBlank)
			.map(GenerateGraphCommand::addSchemeIfMissing)
			.flatMap(inputs -> getJsonResources(inputs).stream())
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

	/**
	 * Adds default `file:` scheme if it is missing in the path.
	 */
	static String addSchemeIfMissing(String path) {
		return URI_SCHEME_PATTERN.matcher(path).matches() ? path : "file:".concat(path);
	}

	@SneakyThrows
	private List<Resource> getJsonResources(String url) {
		String filePath = url.startsWith("file:") ? sanitizePath(url) : url;
		Path path = Path.of(filePath).toAbsolutePath().normalize();
		if (Files.isDirectory(path)) {
			try (var stream = Files.walk(path)) {
				return stream
					.filter(Files::isRegularFile)
					.filter(p -> p.toString().endsWith(".json"))
					.map(this::readFile)
					.toList();
			}
		} else {
			// Treat as a zip/jar archive
			List<Resource> jsonContents = new ArrayList<>();
			try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(path))) {
				ZipEntry entry;
				while ((entry = zis.getNextEntry()) != null) {
					if (entry.getName().endsWith(".json")) {
						jsonContents.add(readZipEntry(entry, zis));
					}
				}
			}
			return jsonContents;
		}
	}

	private Resource readFile(Path filePath) {
		return new AbstractFileResolvingResource() {
			@Override public @NonNull String getDescription() {
				return filePath.toString();
			}

			@Override public @NonNull InputStream getInputStream() throws IOException {
				return Files.newInputStream(filePath);
			}
		};
	}

	@SneakyThrows
	private Resource readZipEntry(ZipEntry entry, ZipInputStream zis) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(zis, baos);
		byte[] bytes = baos.toByteArray();
		String entryName = entry.getName();

		return new AbstractFileResolvingResource() {
			@Override public @NonNull InputStream getInputStream() {
				return new ByteArrayInputStream(bytes);
			}

			@Override public @NonNull String getDescription() {
				return entryName;
			}
		};
	}

	private static String sanitizePath(String location) {
		if (StringUtils.isEmpty(location)) {
			return location;
		}
		location = location.trim();
		if (location.regionMatches(true, 0, "file:", 0, 5)) {
			try {
				Path p = Path.of(new URI(location));
				return p.toString();
			} catch (Exception e) {
				return location.substring(5);
			}
		}
		return location;
	}

	private static Throwable findRootCause(Throwable t) {
		return Optional.of(t)
			.filter(ex -> ex.getCause() == null || ex.getCause() == ex)
			.orElseGet(() -> findRootCause(t.getCause()));
	}

	private void addModel(Resource resource) {
		try {
			modelService.create(contentOfResource(resource));
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
