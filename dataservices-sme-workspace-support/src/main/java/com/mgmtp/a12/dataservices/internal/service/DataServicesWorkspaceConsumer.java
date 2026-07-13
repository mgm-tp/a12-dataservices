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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.constants.SmeWorkspaceConstants;
import com.mgmtp.a12.dataservices.internal.model.SmeWorkspaceMetadata;
import com.mgmtp.a12.dataservices.internal.service.importer.AttachmentImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.DocumentImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.LinkImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.MetadataImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.ModelImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.RoleImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.UserImporter;
import com.mgmtp.a12.dataservices.wcf.IWorkspaceConsumer;
import com.mgmtp.a12.dataservices.wcf.domain.FileTuple;
import com.mgmtp.a12.dataservices.wcf.domain.Workspace;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Workspace consumer that directly invokes DS API services for each file in the workspace,
 * without writing files to the output filesystem path.
 *
 * This consumer replaces `DefaultWorkspaceConsumer` and routes each `FileTuple` by its
 * `outputPath` prefix to the appropriate importer. Files are processed in the correct
 * dependency order: metadata, users, roles, models, attachments, documents, and links.
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class DataServicesWorkspaceConsumer implements IWorkspaceConsumer {

	private static final String JSON_EXTENSION = ".json";

	private final MetadataImporter metadataImporter;
	private final UserImporter userImporter;
	private final RoleImporter roleImporter;
	private final ModelImporter modelImporter;
	private final DocumentImporter documentImporter;
	private final AttachmentImporter attachmentImporter;
	private final LinkImporter linkImporter;

	@Override
	public void accept(Workspace workspace, Path outputPath) {
		WorkspaceFiles workspaceFiles = getWorkspaceFiles(workspace);

		// Import in dependency order
		SmeWorkspaceMetadata metadata = (workspaceFiles.metaContent() != null)
			? metadataImporter.doImport(workspaceFiles.metaContent())
			: new SmeWorkspaceMetadata();

		if (workspaceFiles.userContent() != null) {
			userImporter.doImport(workspaceFiles.userContent());
		}

		if (workspaceFiles.roleContent() != null) {
			roleImporter.doImport(workspaceFiles.roleContent());
		}

		modelImporter.importModels(workspace.getModels());

		workspaceFiles.attachmentFiles()
			.forEach((relativePath, content) -> attachmentImporter.importAttachment(extractEntityId(relativePath), content, metadata));

		workspaceFiles.documentFiles()
			.forEach((relativePath, content) -> documentImporter.importDocument(
				extractModelName(relativePath),
				extractEntityId(Path.of(relativePath).getFileName().toString()),
				content));

		workspaceFiles.linkFiles()
			.forEach((relativePath, content) -> linkImporter.importLink(extractEntityId(Path.of(relativePath).getFileName().toString()), content));
	}

	private static @NonNull WorkspaceFiles getWorkspaceFiles(Workspace workspace) {

		byte[] metaContent = null;
		byte[] userContent = null;
		byte[] roleContent = null;
		Map<String, byte[]> documentFiles = new LinkedHashMap<>();
		Map<String, byte[]> attachmentFiles = new LinkedHashMap<>();
		Map<String, byte[]> linkFiles = new LinkedHashMap<>();

		for (Map.Entry<String, FileTuple> entry : workspace.getFiles().entrySet()) {
			String fileOutputPath = entry.getValue().getOutputPath();
			if (fileOutputPath == null) {
				continue;
			}

			Path outputFilePath = Path.of(fileOutputPath);
			byte[] content = resolveContent(workspace, entry.getKey(), entry.getValue());

			if (outputFilePath.equals(SmeWorkspaceConstants.FULL_META_PATH)) {
				metaContent = content;
			} else if (outputFilePath.equals(SmeWorkspaceConstants.FULL_USER_PATH)) {
				userContent = content;
			} else if (outputFilePath.equals(SmeWorkspaceConstants.FULL_ROLE_PATH)) {
				roleContent = content;
			} else if (outputFilePath.startsWith(SmeWorkspaceConstants.FULL_DOCUMENT_PATH)
				&& fileOutputPath.endsWith(JSON_EXTENSION)) {
				String relativePath = SmeWorkspaceConstants.FULL_DOCUMENT_PATH.relativize(outputFilePath).toString();
				documentFiles.put(relativePath, content);
			} else if (outputFilePath.startsWith(SmeWorkspaceConstants.FULL_ATTACHMENT_PATH)) {
				String relativePath = SmeWorkspaceConstants.FULL_ATTACHMENT_PATH.relativize(outputFilePath).toString();
				attachmentFiles.put(relativePath, content);
			} else if (outputFilePath.startsWith(SmeWorkspaceConstants.FULL_LINK_PATH)
				&& fileOutputPath.endsWith(JSON_EXTENSION)) {
				String relativePath = SmeWorkspaceConstants.FULL_LINK_PATH.relativize(outputFilePath).toString();
				linkFiles.put(relativePath, content);
			}
		}

		return new WorkspaceFiles(metaContent, userContent, roleContent, documentFiles, attachmentFiles, linkFiles);
	}

	private record WorkspaceFiles(byte[] metaContent, byte[] userContent, byte[] roleContent, Map<String, byte[]> documentFiles,
	                              Map<String, byte[]> attachmentFiles, Map<String, byte[]> linkFiles) {}

	private static byte[] resolveContent(Workspace workspace, String key, FileTuple fileTuple) {
		byte[] content = fileTuple.getContent();
		if (content != null) {
			return content;
		}
		try {
			return Files.readAllBytes(Path.of(workspace.getInputDir()).resolve(key));
		} catch (IOException e) {
			throw new UnexpectedException("Cannot read file: " + key, e);
		}
	}

	private static String extractEntityId(String fileName) {
		int dotIndex = fileName.lastIndexOf('.');
		return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
	}

	private static String extractModelName(String relativePath) {
		return Path.of(relativePath).getName(0).toString();
	}
}
