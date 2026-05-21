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
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.configuration.SeedDataProperties;
import com.mgmtp.a12.dataservices.model.SeedMetadata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component public class MetadataImporter extends AbstractImporter<Void, SeedMetadata> {

	private final SeedDataProperties seedDataProperties;
	private final ObjectMapper objectMapper;

	public SeedMetadata doImportLogic(Path metaPath, Void metadata) {
		try {
			Path targetPath = Paths.get(seedDataProperties.getSeedData().getMetaData().getPath());
			if (!targetPath.toFile().exists()) {
				Files.createDirectories(targetPath.getParent());
			}
			Files.copy(metaPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
			return objectMapper.readValue(targetPath.toFile(), SeedMetadata.class);
		} catch (IOException e) {
			throw new UnexpectedException("Cannot load seed metadata file", e);
		}
	}

	@Override protected SeedMetadata getDefaultResult() {
		return new SeedMetadata();
	}
}
