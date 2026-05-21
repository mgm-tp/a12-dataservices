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
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.model.SeedLinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.internal.DataServicesRelationshipLink;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipLinkFactory;
import com.mgmtp.a12.dataservices.relationship.internal.ranks.ComputedRank;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.RelationshipLinkRepository;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.LinkPosition;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component public class LinkImporter extends AbstractFileBasedImporter<Void> {

	private final RelationshipLinkRepository relationshipLinkRepository;
	private final RelationshipLinkFactory relationshipLinkFactory;
	private final ObjectMapper objectMapper;

	@Override protected void importFile(Path relativePath, Path linkPath, Void metadata) {

		String linkId = extractEntityId(relativePath);

		try {
			SeedLinkDescriptor seedLinkDescriptor = objectMapper.readValue(Files.newInputStream(linkPath), SeedLinkDescriptor.class);
			DataServicesRelationshipLink link = (DataServicesRelationshipLink) relationshipLinkFactory.createLink(
				prepareLinkDescriptor(seedLinkDescriptor), seedLinkDescriptor.getLinkDocumentDocRef(), getComputedRank(seedLinkDescriptor)
			);
			link.setId(linkId);
			relationshipLinkRepository.create(link);
		} catch (IOException e) {
			throw new InvalidInputException(ExceptionKeys.EXPORT_SEED_DATA_IMPORT_ERROR_KEY, "Cannot parse link with name " + relativePath)
				.withAnonymityMessage("Cannot parse link.");
		}
	}

	@NotNull private static LinkDescriptor prepareLinkDescriptor(SeedLinkDescriptor seedLinkDescriptor) {
		List<RelationshipRoleSpec> relationshipRoleSpecs = seedLinkDescriptor.getEntities().stream()
			.map(sl -> new RelationshipRoleSpec(sl.getRole(), sl.getDocRef()))
			.toList();
		return new LinkDescriptor(seedLinkDescriptor.getRelationshipModel(), relationshipRoleSpecs, seedLinkDescriptor.getLinkDocumentDocRef(),
			LinkPosition.TOP);
	}

	private static ComputedRank getComputedRank(SeedLinkDescriptor seedLinkDescriptor) {
		if (seedLinkDescriptor == null ||
			seedLinkDescriptor.getEntities().get(0).getRoleOrder() == null ||
			seedLinkDescriptor.getEntities().get(1).getRoleOrder() == null) {
			return null;
		}
		return new ComputedRank(
			seedLinkDescriptor.getEntities().get(0).getRoleOrder(),
			seedLinkDescriptor.getEntities().get(1).getRoleOrder());
	}
}
