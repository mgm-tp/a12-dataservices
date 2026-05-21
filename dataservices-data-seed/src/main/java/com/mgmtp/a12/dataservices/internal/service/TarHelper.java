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
package com.mgmtp.a12.dataservices.internal.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.lang3.time.StopWatch;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j @NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TarHelper {

	/**
	 * Extracts the entire tar archive to the specified directory.
	 *
	 * @param tarInput the input stream containing the tar.gz archive
	 * @param tempDirectory the directory where files will be extracted
	 * @throws InvalidInputException if the tar cannot be read or extracted
	 */
	public static void extract(InputStream tarInput, Path tempDirectory) {
		StopWatch sw = StopWatch.createStarted();
		try (
			BufferedInputStream bi = new BufferedInputStream(tarInput);
			GzipCompressorInputStream gip = new GzipCompressorInputStream(bi);
			TarArchiveInputStream ti = new TarArchiveInputStream(gip)) {

			TarArchiveEntry entry;
			while ((entry = ti.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					continue;
				}
				Path entryPath = tempDirectory.resolve(entry.getName());
				Files.createDirectories(entryPath.getParent());
				Files.copy(ti, entryPath, StandardCopyOption.REPLACE_EXISTING);
			}
			log.debug("Tar extracted in {}", sw.formatTime());
		} catch (IOException e) {
			throw new InvalidInputException(ExceptionKeys.EXPORT_SEED_DATA_IMPORT_ERROR_KEY, "Cannot read seed data file", e);
		}
	}
}
