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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.springframework.core.io.DefaultResourceLoader;

import com.mgmtp.a12.dataservices.constants.SmeWorkspaceConstants;
import com.mgmtp.a12.dataservices.internal.service.exporter.AbstractTarExporter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SmeWorkspaceServiceTestHelper {

	protected static final DefaultResourceLoader DEFAULT_RESOURCE_LOADER = new DefaultResourceLoader();

	public static void createSmeWorkspaceTar(OutputStream outputStream, String attachmentId, String documentId, String linkId, boolean includedModels,
		boolean includeUsers, boolean includeRoles, boolean includeMeta)
		throws IOException {
		try (
			GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(outputStream);
			TarArchiveOutputStream tars = new TarArchiveOutputStream(gzOut)) {

			if (includeMeta) {
				writeFile(tars, SmeWorkspaceConstants.FULL_META_PATH,
					DEFAULT_RESOURCE_LOADER.getResource("classpath:import/data/meta/workspacedata_items.json").getContentAsByteArray());
			}
			if (includedModels) {
				writeFile(tars, "data/models/Contract.json",
					DEFAULT_RESOURCE_LOADER.getResource("classpath:/models/document/Contract.json").getContentAsByteArray());
			}
			if (attachmentId != null) {
				writeFile(tars, "data/attachments/" + attachmentId + ".json",
					DEFAULT_RESOURCE_LOADER.getResource("classpath:import/data/attachments/" + attachmentId + ".jpg").getContentAsByteArray());
			}
			if (documentId != null) {
				writeFile(tars, "data/documents/Contract/" + documentId + ".json",
					DEFAULT_RESOURCE_LOADER.getResource("classpath:import/data/documents/" + documentId + ".json").getContentAsByteArray());
			}
			if (linkId != null) {
				writeFile(tars, "data/links/ContractCoInsuredPartner/" + linkId + ".json",
					DEFAULT_RESOURCE_LOADER.getResource("classpath:import/data/links/" + linkId + ".json").getContentAsByteArray());
			}
			if (includeRoles) {
				writeFile(tars, "data/user/roles.yaml",
					DEFAULT_RESOURCE_LOADER.getResource("classpath:import/data/user/roles.yaml").getContentAsByteArray());
			}
			if (includeUsers) {
				writeFile(tars, "data/user/users.yaml",
					DEFAULT_RESOURCE_LOADER.getResource("classpath:import/data/user/users.yaml").getContentAsByteArray());
			}
			tars.finish();
			gzOut.finish();
		}
	}

	private static void writeFile(TarArchiveOutputStream tars, String entryName, byte[] bytes) throws IOException {
		writeFile(tars, Path.of(entryName), bytes);
	}

	private static void writeFile(TarArchiveOutputStream tars, Path entryPath, byte[] bytes) throws IOException {
		TarArchiveEntry tarEntry = new TarArchiveEntry(AbstractTarExporter.getTarEntryName(entryPath));
		tarEntry.setSize(bytes.length);
		tars.putArchiveEntry(tarEntry);
		tars.write(bytes);
		tars.closeArchiveEntry();
	}
}
