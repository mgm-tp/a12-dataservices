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

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.time.StopWatch;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractImporter<M, R> {
	/**
	 * Returns the name of this importer for logging purposes.
	 * Default implementation returns the simple class name without "Importer" suffix.
	 *
	 * @return The importer name (e.g., "Documents", "Attachments")
	 */
	protected String getImporterName() {
		return getClass().getSimpleName().replace("Importer", "");
	}

	public final R doImport(Path basePath) {
		return doImport(basePath, null);
	}

	/**
	 * Main entry point for importing data from a directory with timing.
	 * Walks the directory tree, filters files, and delegates to importFile for each match.
	 *
	 * @param basePath The root path containing files to import
	 * @param metadata The metadata context for this import
	 */
	public final R doImport(Path basePath, M metadata) {
		if (!Files.exists(basePath)) {
			return getDefaultResult();
		}

		StopWatch sw = StopWatch.createStarted();
		R result = doImportLogic(basePath, metadata);
		log.debug("{} imported in {}", getImporterName(), sw.formatTime());
		return result;
	}

	protected R getDefaultResult() {
		return null;
	}

	protected abstract R doImportLogic(Path basePath, M metadata);
}
