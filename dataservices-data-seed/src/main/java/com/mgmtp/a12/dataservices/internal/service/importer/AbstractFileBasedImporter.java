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
package com.mgmtp.a12.dataservices.internal.service.importer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for file-based importers that process files from extracted directories.
 * Provides common functionality for walking directories, filtering files, and extracting entity IDs.
 *
 * @param <M> The type of metadata used during import
 */
@Slf4j
public abstract class AbstractFileBasedImporter<M> extends AbstractImporter<M, Void> {

	protected static final String JSON_EXTENSION = ".json";

	/**
	 * Imports a single file with the provided metadata.
	 *
	 * @param relativePath The base path where processed files are stored
	 * @param filePath The path to the file to import
	 * @param metadata The metadata context for this import
	 */
	protected abstract void importFile(Path relativePath, Path filePath, M metadata);

	/**
	 * Returns a predicate to filter files for processing.
	 * Default implementation returns all regular files.
	 * Override to add custom filtering (e.g., by extension).
	 *
	 * @return Predicate for file filtering
	 */
	protected Predicate<Path> getFileFilter() {
		return p -> Files.isRegularFile(p) && p.toString().endsWith(JSON_EXTENSION);
	}

	protected Void doImportLogic(Path basePath, M metadata) {
		try (Stream<Path> paths = Files.walk(basePath)) {
			paths.filter(getFileFilter())
				.forEach(p -> importFile(basePath.relativize(p), p, metadata));
		} catch (IOException e) {
			throw new UnexpectedException("Cannot read directory: " + basePath, e);
		}
		return null;
	}

	/**
	 * Extracts entity ID from a file path using the standard ID pattern.
	 * Pattern matches the last path segment before the file extension: /([^/]+)\.
	 *
	 * @param relativePath The entry name (relative or absolute path)
	 * @return The extracted entity ID
	 * @throws InvalidInputException if the ID cannot be extracted
	 */
	protected String extractEntityId(Path relativePath) {
		String fileName = relativePath.getFileName().toString();
		int dotIndex = fileName.lastIndexOf('.');
		String result = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
		if (StringUtils.isNotBlank(result)) {
			return result;
		} else {
			throw new InvalidInputException(
				ExceptionKeys.EXPORT_SEED_DATA_IMPORT_ERROR_KEY,
				"Cannot extract entity id from: " + relativePath
			).withAnonymityMessage("Could not get entity id.");
		}
	}
}
