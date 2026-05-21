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
package com.mgmtp.a12.dataservices.utils.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.AbstractFileResolvingResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.rometools.utils.Strings;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor @Slf4j
@Component public class DsResourceUtils {

	private static final Pattern URI_SCHEME_PATTERN = Pattern.compile("^[a-z]+:.*");
	private final ResourcePatternResolver resourcePatternResolver;
	private static final String URI_SEPARATOR = "/";

	/**
	 * Sort the resources by comparing the URI
	 *
	 * @param resources The array of resources to be sorted.
	 */
	public static void sortByURI(Resource[] resources) {
		Arrays.sort(resources, new Comparator<>() {
			@SneakyThrows
			@Override public int compare(Resource r1, Resource r2) {
				return innerCompare(r1.getURI(), r2.getURI(), new LinkedList<>(List.of(
					compareString(URI::getScheme),
					compareString(URI::getHost),
					compareString(x -> String.valueOf(x.getPort())),
					compareSplitting(URI::getPath),
					compareString(URI::getQuery),
					compareString(URI::getFragment),
					compareSplitting(URI::getSchemeSpecificPart) // fallback - if every other is null, try this last one
				)));
			}
		});
	}

	/**
	 * Return all `*.json` resources from the whole subtree.
	 *
	 * @param directoryOrArchivePath Either location of root directory on filesystem or classpath pattern.
	 * @return Stream of resources matching input.
	 */
	public @NotNull Stream<Resource> getJsonResources(String directoryOrArchivePath) throws IOException {
		if (StringUtils.isBlank(directoryOrArchivePath)) {
			throw new InvalidInputException("Path must be provided.");
		}
		if (directoryOrArchivePath.startsWith("classpath:")) {
			Resource resource = resourcePatternResolver.getResource(getLocation(directoryOrArchivePath));
			if (resource.isFile()) {
				return processLocalFile(resource.getFile().getPath());
			} else {
				validateBasePath(resource.getURI());
				return Arrays.stream(
					getResources("%s%s**/*.json".formatted(directoryOrArchivePath, directoryOrArchivePath.endsWith("/") ? "" : "/")));
			}
		} else {
			return processLocalFile(directoryOrArchivePath);
		}
	}

	@SneakyThrows
	public @NotNull Resource getResource(@NonNull String location) {
		try {
			return resourcePatternResolver.getResource(getLocation(location));
		} catch (Exception e) {
			throw new InvalidInputException("Error while resolving resource for location: %s".formatted(location), e);
		}
	}

	/**
	 * Adds default `file:` scheme if it's missing in the path.
	 */
	public static String addSchemeIfMissing(String path) {
		return URI_SCHEME_PATTERN.matcher(path).matches() ? path : "file:".concat(path);
	}

	@NotNull private static Stream<Resource> processLocalFile(@NonNull String url) throws IOException {
		Path path = Path.of(sanitizePath(url)).toAbsolutePath().normalize();
		if (Files.isDirectory(path)) {
			return Files.walk(path)
				.filter(Files::isRegularFile)
				.filter(p -> p.toString().endsWith(".json"))
				.map(DsResourceUtils::readFile);
		} else {
			List<Resource> jsonContents = new ArrayList<>();
			try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(path))) {
				ZipEntry entry;
				while ((entry = zis.getNextEntry()) != null) {
					if (entry.getName().endsWith(".json")) {
						jsonContents.add(readZipEntry(entry, zis));
					}
				}
			}
			return jsonContents.stream();
		}
	}

	private static Resource readFile(Path path) {
		try {
			return new AbstractFileResolvingResource() {
				@Override public @NotNull String getDescription() {
					return path.toString();
				}

				@Override public @NotNull InputStream getInputStream() throws IOException {
					return Files.newInputStream(path);
				}
			};
		} catch (Exception e) {
			throw new RuntimeException("Failed to read file: %s".formatted(path), e);
		}
	}

	private static Resource readZipEntry(ZipEntry entry, ZipInputStream zis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(zis, baos);

		return new AbstractFileResolvingResource() {

			@Override public @NotNull InputStream getInputStream() {
				return new ByteArrayInputStream(baos.toByteArray());
			}

			@Override public @NotNull String getDescription() {
				return entry.getName();
			}
		};
	}

	private static void validateBasePath(URI basePath) {
		basePath = basePath.normalize();
		if (!basePath.isAbsolute()) {
			throw new InvalidInputException(ExceptionKeys.MODEL_BULK_IMPORT_GENERIC_ERROR_KEY, String.format("Base URI must be absolute: %s", basePath))
				.withAnonymityMessage("Base path validation failed.");
		}
	}

	private static BiFunction<URI, URI, Integer> compareString(Function<URI, String> getString) {
		return (u1, u2) -> ObjectUtils.compare(getString.apply(u1), getString.apply(u2));
	}

	private static BiFunction<URI, URI, Integer> compareSplitting(Function<URI, String> getString) {
		return (u1, u2) -> {
			String s1 = getString.apply(u1);
			String s2 = getString.apply(u2);
			if (s1 == null || s2 == null) {
				return 0;
			}
			return Arrays.compare(
				s1.split(URI_SEPARATOR),
				s2.split(URI_SEPARATOR)
			);
		};
	}

	private static int innerCompare(URI u1, URI u2, Queue<BiFunction<URI, URI, Integer>> q) {
		BiFunction<URI, URI, Integer> f = q.poll();
		if (f == null || u1.equals(u2)) {
			return 0;
		}
		int cmp = f.apply(u1, u2);
		if (cmp == 0) {
			return innerCompare(u1, u2, q);
		} else {
			return cmp;
		}
	}

	/**
	 * Resolve the given location pattern into Resource objects.
	 * It will return the resources ordered by URI. See {@link DsResourceUtils#sortByURI(Resource[])}
	 */
	@SneakyThrows
	private @NotNull Resource[] getResources(@NonNull String location) {
		Resource[] resources;
		try {
			resources = resourcePatternResolver.getResources(getLocation(location));
		} catch (Exception e) {
			log.debug("Error while resolving resources for location {}", location, e);
			throw e;
		}
		DsResourceUtils.sortByURI(resources);
		return resources;
	}

	private String getLocation(String location) {
		log.debug("Resource path: {}", location);
		if (!location.endsWith(".class")) {
			try {
				String resolvedUrl = ResourceUtils.getURL(location).toString();
				log.debug("Resolved resource path: {}", resolvedUrl);
				return resolvedUrl;
			} catch (FileNotFoundException e) {
				log.debug("Resource is not resolvable by this resolver: {}", location);
				// Pass to delegate below.
			}
		}
		return location;
	}

	private static String sanitizePath(String location) {
		if (Strings.isEmpty(location)) {
			return location;
		}

		location = location.trim();
		// Strip leading "file:" scheme (case-insensitive)
		if (location.regionMatches(true, 0, "file:", 0, 5)) {
			try {
				Path p = Paths.get(new URI(location));
				return p.toString();
			} catch (Exception e) {
				// Fallback: strip file: prefix
				return location.substring(5);
			}
		}

		return location;
	}
}
