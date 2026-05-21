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
package com.mgmtp.a12.contentstore.content.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.mgmtp.a12.contentstore.exception.ExceptionKeys;
import com.mgmtp.a12.contentstore.utils.Constants;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Slf4j
public class FileSystemContentRepository implements ContentRepository {
	private final File contentLocation;

	public FileSystemContentRepository(@NonNull File contentDirectory) {
		this.contentLocation = contentDirectory;
		if (!this.contentLocation.mkdirs() && !this.contentLocation.isDirectory()) {
			throw new UnexpectedException(ExceptionKeys.INVALID_CONTENT_LOCATION_ERROR_KEY,
				String.format(Constants.CONTENT_LOCATION_UNAVAILABLE_ERROR_PATTERN, this.contentLocation.getName()));
		}
	}

	@Override public Optional<byte[]> findBinaryContentById(String id) {
		Path contentPath = FileSystemContentUtil.getContentPath(contentLocation, id);
		if (Files.notExists(contentPath)) {
			throw new NotFoundException(ExceptionKeys.CONTENT_NOT_FOUND_ERROR_KEY, String.format(Constants.CANNOT_FIND_PHYSICAL_CONTENT_BY_ID_PATTERN, id));
		}

		try (InputStream inputStream = Files.newInputStream(contentPath)) {
			return Optional.of(IOUtils.toByteArray(inputStream));
		} catch (IOException e) {
			throw new UnexpectedException(ExceptionKeys.UNEXPECTED_ERROR_KEY, e.getMessage()).withAnonymityMessage("Resolving content failed.");
		}
	}

	@Override public long save(String contentId, InputStream inputStream) {
		Path contentPath = FileSystemContentUtil.getContentPath(contentLocation, contentId);
		try (InputStream in = inputStream) {
			// Do not persist content entity to database.
			// Persist file content into FS
			contentPath.toFile()
				.getCanonicalFile()
				.getParentFile()
				.mkdirs();
			return Files.copy(in, contentPath, REPLACE_EXISTING);
		} catch (IOException e) {
			log.error(String.format(Constants.CANNOT_PERSIST_CONTENT_TO_FS_ERROR_PATTERN, contentId), e);
			FileUtils.deleteQuietly(contentPath.toFile());
			throw new UnexpectedException(ExceptionKeys.UNEXPECTED_ERROR_KEY,
				String.format(Constants.CANNOT_PERSIST_CONTENT_TO_FS_ERROR_PATTERN, contentId));
		}
	}

	@Override public void delete(String id) {
		try {
			Files.deleteIfExists(FileSystemContentUtil.getContentPath(contentLocation, id));
		} catch (Exception e) {
			log.error("Can not delete file with id {} in file system, please try to handle delete this file manually.", id, e);
		}
	}
}
