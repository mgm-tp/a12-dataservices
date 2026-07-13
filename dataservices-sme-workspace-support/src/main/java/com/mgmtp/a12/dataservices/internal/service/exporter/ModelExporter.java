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

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.GenericModelContent;
import com.mgmtp.a12.dataservices.model.persistence.IModelRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.constants.SmeWorkspaceConstants.FULL_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.SmeWorkspaceConstants.JSON_EXTENSION;

/**
 * Exports model definitions to TAR archive.
 */
@RequiredArgsConstructor
@Component public class ModelExporter extends AbstractTarExporter<Void> {

	private final ModelHeaderJpaRepository modelHeaderJpaRepository;
	private final IModelRepository modelRepository;

	@Override protected void exportLogic(TarArchiveOutputStream tarStream, Void unused) {
		modelHeaderJpaRepository.findAll()
			.forEach(modelHeaderEntity -> modelRepository.load(modelHeaderEntity)
				.map(GenericModel::getContent)
				.map(GenericModelContent::getRawContent)
				.map(m -> m.getBytes(StandardCharsets.UTF_8))
				.ifPresent(modelRawContent -> writeFileToTar(tarStream, modelRawContent, constructModelPathInTar(modelHeaderEntity))));
	}

	@NonNull private static Path constructModelPathInTar(ModelHeaderEntity modelHeaderEntity) {
		return FULL_MODEL_PATH.resolve(modelHeaderEntity.getId() + JSON_EXTENSION);
	}
}
