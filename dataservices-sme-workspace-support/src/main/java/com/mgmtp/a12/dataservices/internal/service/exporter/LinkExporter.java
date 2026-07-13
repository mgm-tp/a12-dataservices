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

import java.nio.file.Path;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.internal.model.SmeWorkspaceLinkDescriptor;
import com.mgmtp.a12.dataservices.internal.model.SmeWorkspaceRoleSpec;
import com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelLoader;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.entity.RelationshipLinkEntity;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.entity.RelationshipRoleEntity;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.repository.RelationshipLinkJpaRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.constants.SmeWorkspaceConstants.FULL_LINK_PATH;

/**
 * Exports relationship links to TAR archive.
 */
@RequiredArgsConstructor
@Component public class LinkExporter extends AbstractTarExporter<Void> {

	private final RelationshipModelLoader relationshipModelLoader;
	private final RelationshipLinkJpaRepository relationshipLinkJpaRepository;
	private final ObjectMapper objectMapper;

	@Override protected void exportLogic(TarArchiveOutputStream tarStream, Void unused) {
		relationshipModelLoader.loadAllRelationshipModels()
			.forEach(relationshipModel -> exportForRelationshipModel(tarStream, relationshipModel));
	}

	private void exportForRelationshipModel(TarArchiveOutputStream taos, RelationshipModel relationshipModel) {
		relationshipLinkJpaRepository.findByRelationshipModel(relationshipModel.getHeader().getId(), null)
			.getContent()
			.forEach(link -> {
				try {
					writeFileToTar(taos, objectMapper.writeValueAsBytes(createLinkDescriptor(link)), constructTarPah(relationshipModel, link));
				} catch (JacksonException e) {
					throw new UnexpectedException("Error parsing link ", e);
				}
			});
	}

	@NonNull private static Path constructTarPah(RelationshipModel relationshipModel, RelationshipLinkEntity link) {
		return FULL_LINK_PATH.resolve(relationshipModel.getHeader().getId()).resolve(link.getId() + ".json");
	}

	private static SmeWorkspaceLinkDescriptor createLinkDescriptor(RelationshipLinkEntity entity) {

		SmeWorkspaceLinkDescriptor linkDescriptor = new SmeWorkspaceLinkDescriptor();
		linkDescriptor.setRelationshipModel(entity.getRelationshipModel());
		linkDescriptor.setEntities(entity.getRoles().keySet().stream()
			.map(key -> roleEntityRoSeedRoleSpec(entity, key))
			.toList());
		linkDescriptor.setLinkDocumentDocRef(entity.getLinkDocumentDocRef());
		return linkDescriptor;
	}

	@NonNull private static SmeWorkspaceRoleSpec roleEntityRoSeedRoleSpec(RelationshipLinkEntity entity, String key) {
		RelationshipRoleEntity roleEntity = entity.getRoles().get(key);
		return new SmeWorkspaceRoleSpec(roleEntity.getName(), roleEntity.getDocRef(), roleEntity.getOrder());
	}
}
