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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.AttachmentHeaderEntity;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.repository.AttachmentAnnotationJpaRepository;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.repository.AttachmentHeaderJpaRepository;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.repository.AttachmentReferenceJpaRepository;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.repository.DirtyAttachmentJpaRepository;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.document.persistence.internal.DocumentJpaRepository;
import com.mgmtp.a12.dataservices.internal.service.exporter.AttachmentExporter;
import com.mgmtp.a12.dataservices.internal.service.exporter.DocumentExporter;
import com.mgmtp.a12.dataservices.internal.service.exporter.LinkExporter;
import com.mgmtp.a12.dataservices.internal.service.exporter.MetadataExporter;
import com.mgmtp.a12.dataservices.internal.service.exporter.ModelExporter;
import com.mgmtp.a12.dataservices.internal.service.exporter.RoleExporter;
import com.mgmtp.a12.dataservices.internal.service.exporter.UserExporter;
import com.mgmtp.a12.dataservices.internal.service.importer.AttachmentImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.DocumentImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.LinkImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.MetadataImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.ModelImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.RoleImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.UserImporter;
import com.mgmtp.a12.dataservices.model.SeedMetadata;
import com.mgmtp.a12.dataservices.model.internal.ModelCacheManager;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.QueryIndexManager;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.LocalizedFieldsJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.ModelFieldsJpaRepository;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.repository.RelationshipLinkJpaRepository;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.repository.RelationshipRoleJpaRepository;
import com.mgmtp.a12.dataservices.wcf.WorkspaceConversionService;
import com.mgmtp.a12.uaa.authentication.backend.BackendAuthenticationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor @Slf4j
@Service public class SeedDataService {
	public static final String SEED_DATA = "data";
	public static final String META_DIR = "meta";
	public static final String USER_DIR = "user";
	public static final String MODELS_DIR = "models";
	public static final String ATTACHMENTS_DIR = "attachments";
	public static final String DOCUMENTS_DIR = "documents";
	public static final String LINKS_DIR = "links";
	public static final String SEED_METADATA_FILE = "seed_metadata.json";
	public static final String USERS_FILE = "users.yaml";
	public static final String ROLES_FILE = "roles.yaml";
	public static final Path FULL_DOCUMENT_PATH = Path.of(SEED_DATA, DOCUMENTS_DIR);
	public static final Path FULL_MODEL_PATH = Path.of(SEED_DATA, MODELS_DIR);
	public static final Path FULL_ATTACHMENT_PATH = Path.of(SEED_DATA, ATTACHMENTS_DIR);
	public static final Path FULL_LINK_PATH = Path.of(SEED_DATA, LINKS_DIR);
	public static final Path FULL_USER_PATH = Path.of(SEED_DATA, USER_DIR, USERS_FILE);
	public static final Path FULL_ROLE_PATH = Path.of(SEED_DATA, USER_DIR, ROLES_FILE);
	public static final Path FULL_META_PATH = Path.of(SEED_DATA, META_DIR, SEED_METADATA_FILE);
	public static final String JSON_EXTENSION = ".json";

	private final BackendAuthenticationService backendAuthenticationService;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final CacheManager cacheManager;
	private final ModelCacheManager modelCacheManager;
	private final RelationshipLinkJpaRepository relationshipLinkJpaRepository;
	private final RelationshipRoleJpaRepository relationshipRoleJpaRepository;
	private final DocumentJpaRepository documentJpaRepository;
	private final QueryIndexManager queryIndexManager;
	private final ModelHeaderJpaRepository modelHeaderJpaRepository;
	private final ModelJpaRepository modelJpaRepository;
	private final ModelFieldsJpaRepository modelFieldsJpaRepository;
	private final LocalizedFieldsJpaRepository localizedFieldsJpaRepository;
	private final AttachmentHeaderJpaRepository attachmentHeaderJpaRepository;
	private final AttachmentReferenceJpaRepository attachmentReferenceJpaRepository;
	private final AttachmentAnnotationJpaRepository attachmentAnnotationJpaRepository;
	private final DirtyAttachmentJpaRepository dirtyAttachmentJpaRepository;
	private final jakarta.persistence.EntityManager entityManager;
	private final WorkspaceConversionService workspaceConversionService;

	// Importers
	private final MetadataImporter metadataImporter;
	private final UserImporter userImporter;
	private final RoleImporter roleImporter;
	private final ModelImporter modelImporter;
	private final DocumentImporter documentImporter;
	private final AttachmentImporter attachmentImporter;
	private final LinkImporter linkImporter;

	// Exporters
	private final MetadataExporter metadataExporter;
	private final UserExporter userExporter;
	private final RoleExporter roleExporter;
	private final ModelExporter modelExporter;
	private final DocumentExporter documentExporter;
	private final AttachmentExporter attachmentExporter;
	private final LinkExporter linkExporter;

	/**
	 * Imports data from the provided InputStream, which is expected to be a GZIP-compressed TAR archive. This operation is performed with backend authentication.
	 *
	 * @param inputStream The InputStream containing the GZIP-compressed TAR archive.
	 * @throws IOException If an I/O error occurs during the import process.
	 */
	@Transactional
	public void importData(InputStream inputStream) throws IOException {
		StopWatch sw = StopWatch.createStarted();
		Path tempDirectory = Files.createTempDirectory("ds_upload_workspace_");
		try {
			backendAuthenticationService.executeWithBackendAuthentication(
				dataServicesCoreProperties.getAuthorization().getBackendJob().getPrincipal().getUsername(),
				() -> {
					Path seedDataOutputPath = prepareWorkspace(inputStream, tempDirectory);
					// Import metadata
					SeedMetadata seedMetadata = metadataImporter.doImport(seedDataOutputPath.resolve(FULL_META_PATH));
					userImporter.doImport(seedDataOutputPath.resolve(FULL_USER_PATH));
					roleImporter.doImport(seedDataOutputPath.resolve(FULL_ROLE_PATH));
					modelImporter.doImport(seedDataOutputPath.resolve(FULL_MODEL_PATH));
					attachmentImporter.doImport(seedDataOutputPath.resolve(FULL_ATTACHMENT_PATH), seedMetadata);
					documentImporter.doImport(seedDataOutputPath.resolve(FULL_DOCUMENT_PATH), seedMetadata);
					linkImporter.doImport(seedDataOutputPath.resolve(FULL_LINK_PATH));

					log.debug("seed-data imported in {}", sw.formatTime());
					return true;
				});
		} finally {
			FileUtils.deleteDirectory(tempDirectory.toFile());
		}
	}

	@NotNull private Path prepareWorkspace(InputStream inputStream, Path tempDirectory) {

		Path seedDataSourcePath = tempDirectory.resolve("in");
		Path seedDataOutputPath = tempDirectory.resolve("out");

		TarHelper.extract(inputStream, seedDataSourcePath);

		workspaceConversionService.process(seedDataSourcePath, seedDataOutputPath);

		try {
			FileUtils.deleteDirectory(seedDataSourcePath.toFile());
		} catch (IOException ignored) {
			log.warn("could not delete directory {}", seedDataSourcePath);
		}
		return seedDataOutputPath;
	}

	/**
	 * Exports all data, including metadata, documents, attachments, links, and users,
	 * to the provided TAR archive output stream.
	 *
	 * @param outputStream The OutputStream to which the TAR archive will be written.
	 * @param includeModels `true` if the exported data should include model definitions; `false` otherwise.
	 */
	@Transactional(readOnly = true)
	public void exportAllData(OutputStream outputStream, boolean includeModels) {
		handleResult(backendAuthenticationService.executeWithBackendAuthentication(
			dataServicesCoreProperties.getAuthorization().getBackendJob().getPrincipal().getUsername(),
			() -> exportAllDataInternal(outputStream, includeModels)));
	}

	/**
	 * Deletes all data, including relationships, documents, attachments, and models,
	 * and clears relevant caches. This operation is performed with backend authentication.
	 */
	@Transactional
	public void deleteAllData() {
		backendAuthenticationService.executeWithBackendAuthentication(
			dataServicesCoreProperties.getAuthorization().getBackendJob().getPrincipal().getUsername(),
			() -> {
				deleteRelationships();
				deleteDocuments();
				deleteAttachments();
				deleteModels();
				clearCaches();
				entityManager.flush();
				return true;
			});
	}

	private static void handleResult(RuntimeException result) {
		if (result != null) {
			log.error(result.getMessage(), result);
			throw result;
		}
	}

	@Nullable private RuntimeException exportAllDataInternal(OutputStream outputStream, boolean includeModels) {
		try (GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(outputStream);
			TarArchiveOutputStream tars = new TarArchiveOutputStream(gzOut)) {

			StopWatch sw = StopWatch.createStarted();
			List<AttachmentHeaderEntity> attachmentHeaderEntities = attachmentHeaderJpaRepository.findAll();

			metadataExporter.doExport(tars, attachmentHeaderEntities);
			if (includeModels) {
				modelExporter.doExport(tars);
			}
			documentExporter.doExport(tars);
			attachmentExporter.doExport(tars, attachmentHeaderEntities);
			linkExporter.doExport(tars);
			roleExporter.doExport(tars);
			userExporter.doExport(tars);

			log.debug("All seed-data exported in {}", sw.formatTime());

			tars.finish();
			gzOut.finish();

			return null;
		} catch (RuntimeException e) {
			return e;
		} catch (Exception e) {
			return new UnexpectedException(e).withAnonymityMessage("Export data failed.");
		}
	}

	private void clearCaches() {
		cacheManager.getCacheNames().stream()
			.map(cacheManager::getCache)
			.filter(Objects::nonNull)
			.forEach(Cache::clear);
		modelCacheManager.invalidateUnsecuredModelReadCaches();
	}

	private void deleteModels() {
		modelFieldsJpaRepository.deleteAllInBatch();
		localizedFieldsJpaRepository.deleteAllInBatch();
		modelHeaderJpaRepository.deleteAllInBatch();
		modelJpaRepository.deleteAllInBatch();
	}

	private void deleteAttachments() {
		attachmentReferenceJpaRepository.deleteAllInBatch();
		attachmentAnnotationJpaRepository.deleteAllInBatch();
		dirtyAttachmentJpaRepository.deleteAllInBatch();
		attachmentHeaderJpaRepository.deleteAllInBatch();
	}

	private void deleteDocuments() {
		queryIndexManager.cleanIndex();
		documentJpaRepository.deleteAllInBatch();
	}

	private void deleteRelationships() {
		relationshipRoleJpaRepository.deleteAllInBatch();
		relationshipLinkJpaRepository.deleteAllInBatch();
	}
}
