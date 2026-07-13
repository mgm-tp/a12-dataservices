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
package com.mgmtp.a12.dataservices.internal.service.exporter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.lang3.time.StopWatch;
import org.jetbrains.annotations.NotNull;

import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for TAR archive exporters.
 * Provides common functionality for writing data to TAR archives with timing.
 */
@Slf4j
public abstract class AbstractTarExporter<T> {

	public static final String TAR_PATH_SEPARATOR = "/";

	/**
	 * Template method that exports data with timing and logging.
	 * Delegates to doExport() for actual export logic.
	 *
	 * @param tarStream The TAR archive output stream to write to
	 */
	public final void doExport(TarArchiveOutputStream tarStream) {
		StopWatch sw = StopWatch.createStarted();
		exportLogic(tarStream, null);
		log.debug("{} exported in {}", getExporterName(), sw.formatTime());
	}

	/**
	 * Template method that exports data with timing and logging.
	 * Delegates to doExport() for actual export logic.
	 *
	 * @param tarStream The TAR archive output stream to write to
	 */
	public final void doExport(TarArchiveOutputStream tarStream, T exportParameter) {
		StopWatch sw = StopWatch.createStarted();
		exportLogic(tarStream, exportParameter);
		log.debug("{} exported in {}", getExporterName(), sw.formatTime());
	}

	/**
	 * Returns the name of this exporter for logging purposes.
	 * Default implementation returns the simple class name without "Exporter" suffix.
	 *
	 * @return The exporter name (e.g., "Metadata", "Documents")
	 */
	protected String getExporterName() {
		return getClass().getSimpleName().replace("Exporter", "");
	}

	/**
	 * Performs the actual export logic. Subclasses must implement this method.
	 *
	 * @param tarStream The TAR archive output stream to write to
	 * @param exportParameter
	 */
	protected abstract void exportLogic(TarArchiveOutputStream tarStream, T exportParameter);

	/**
	 * Writes a file entry to the TAR archive.
	 *
	 * @param tarStream The TAR archive output stream
	 * @param bytes The file content as byte array
	 * @param fullPath The full path within the TAR archive
	 */
	protected void writeFileToTar(TarArchiveOutputStream tarStream, byte[] bytes, Path fullPath) {
		try {
			TarArchiveEntry tarEntry = new TarArchiveEntry(getTarEntryName(fullPath));
			tarEntry.setSize(bytes.length);
			tarStream.putArchiveEntry(tarEntry);
			tarStream.write(bytes);
			tarStream.closeArchiveEntry();
		} catch (IOException e) {
			throw new UnexpectedException("Error writing tar archive", e);
		}
	}

	@NotNull public static String getTarEntryName(Path fullPath) {
		return StreamSupport.stream(fullPath.spliterator(), false)
			.map(Path::toString)
			.collect(Collectors.joining(TAR_PATH_SEPARATOR));
	}
}
