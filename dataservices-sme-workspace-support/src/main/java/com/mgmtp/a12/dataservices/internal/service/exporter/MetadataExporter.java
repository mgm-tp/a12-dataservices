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

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.attachment.internal.AttachmentMapper;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.AttachmentHeaderEntity;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.internal.configuration.SmeWorkspaceProperties;
import com.mgmtp.a12.dataservices.internal.model.AttachmentMetadataInfo;
import com.mgmtp.a12.dataservices.internal.model.SmeWorkspaceMetadata;
import com.mgmtp.a12.dataservices.utils.internal.DsResourceUtils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import static com.mgmtp.a12.dataservices.constants.SmeWorkspaceConstants.FULL_META_PATH;

/**
 * Exports SME Workspace including attachment information to TAR archive.
 */
@Slf4j @RequiredArgsConstructor
@Component public class MetadataExporter extends AbstractTarExporter<List<AttachmentHeaderEntity>> {

	private final SmeWorkspaceProperties smeWorkspaceProperties;
	private final DsResourceUtils dsResourceUtils;
	private final ObjectMapper objectMapper;

	/**
	 * Performs the actual metadata export logic.
	 *
	 * @param taos The TAR archive output stream
	 * @param attachmentHeaderEntities List of attachment headers to include in metadata
	 */
	@Override protected void exportLogic(TarArchiveOutputStream taos, List<AttachmentHeaderEntity> attachmentHeaderEntities) {
		try {
			SmeWorkspaceMetadata smeWorkspaceMetadata = loadApplicationMetadata();
			smeWorkspaceMetadata.setAttachments(attachmentHeaderEntities.stream()
				.collect(Collectors.toMap(AttachmentHeaderEntity::getId, MetadataExporter::constructAttachmentMetaInfo)));
			writeFileToTar(taos, objectMapper.writeValueAsBytes(smeWorkspaceMetadata), FULL_META_PATH);
		} catch (JacksonException e) {
			throw new UnexpectedException("Error parsing metadata file", e);
		}
	}

	@NonNull private static AttachmentMetadataInfo constructAttachmentMetaInfo(AttachmentHeaderEntity e) {
		return new AttachmentMetadataInfo(e.getFileName(),
			e.getAnnotations().stream()
				.map(AttachmentMapper::toAttachmentAnnotation)
				.toList());
	}

	private SmeWorkspaceMetadata loadApplicationMetadata() {
		try {
			return objectMapper.readValue(dsResourceUtils.getResource(smeWorkspaceProperties.getMetaData().getPath()).getInputStream(),
				SmeWorkspaceMetadata.class);
		} catch (Exception e) {
			return new SmeWorkspaceMetadata();
		}
	}
}
