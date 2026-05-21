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
import java.nio.file.Path;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class FileSystemContentUtil {

	// Top level folder consists of characters 1 to FIRST_LEVEL
	private static final int FIRST_LEVEL = 2;
	// Second level folder consists of characters FIRST_LEVEL+1 to SECOND_LEVEL
	private static final int SECOND_LEVEL = 4;
	// Third level folder consists of characters SECOND_LEVEL+1 to THIRD_LEVEL
	private static final int THIRD_LEVEL = 6;

	public static Path getContentPath(File contentRootDirectory, String id) {
		return Path.of(contentRootDirectory.getAbsolutePath(), getContentFolder(id), id);
	}

	/**
	 * Contents are stored in a folder structure to avoid having too many files in a single directory.
	 * We use a folder structure of depth 3 where each folder name is built of two subsequent characters of the content id.
	 * This is always possible since content ids are UUIDs which follow the pattern ^[0-9a-fA-F]{8}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{12}$
	 *
	 * Example:
	 * contentId = abcdef78-gh34-ab34-cd34-abc456cdef12
	 * => First level folder = ab (characters at position 0 and 1),
	 * second level folder = cd (characters at position 2 and 3),
	 * third level folder = ef (characters at position 4 and 5)
	 * => Content is saved as ab/cd/ef/abcdef78-abc4-abs4-abc4-abcdefghij12
	 *
	 * @param contentId The id og the content.
	 * @return The relative content folder path.
	 */
	@NonNull @SneakyThrows
	private static String getContentFolder(String contentId) {
		if (contentId.length() < THIRD_LEVEL) return contentId;
		return contentId.substring(0, FIRST_LEVEL)
			.concat(File.separator).concat(contentId.substring(FIRST_LEVEL, SECOND_LEVEL))
			.concat(File.separator).concat(contentId.substring(SECOND_LEVEL, THIRD_LEVEL));
	}
}
