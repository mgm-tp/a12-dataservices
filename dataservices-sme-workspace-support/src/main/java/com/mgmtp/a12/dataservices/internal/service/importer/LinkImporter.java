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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.internal.model.SmeWorkspaceLinkDescriptor;
import com.mgmtp.a12.dataservices.internal.model.SmeWorkspaceRoleSpec;
import com.mgmtp.a12.dataservices.relationship.factory.RelationshipLinkFactory;
import com.mgmtp.a12.dataservices.relationship.internal.DataServicesRelationshipLink;
import com.mgmtp.a12.dataservices.relationship.internal.ranks.ComputedRank;
import com.mgmtp.a12.dataservices.relationship.persistence.RelationshipLinkRepository;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.LinkPosition;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Component public class LinkImporter {

	private final RelationshipLinkRepository relationshipLinkRepository;
	private final RelationshipLinkFactory relationshipLinkFactory;
	private final ObjectMapper objectMapper;

	/**
	 * Imports a single relationship link from in-memory content.
	 *
	 * @param linkId the link entity ID
	 * @param content the raw JSON link descriptor content
	 */
	public void importLink(String linkId, byte[] content) {
		try {
			SmeWorkspaceLinkDescriptor smeDescriptor = objectMapper.readValue(content, SmeWorkspaceLinkDescriptor.class);
			LinkDescriptor linkDescriptor = prepareLinkDescriptor(smeDescriptor);
			DataServicesRelationshipLink link = (DataServicesRelationshipLink) relationshipLinkFactory.createLink(
				linkDescriptor, smeDescriptor.getLinkDocumentDocRef(), getComputedRank(smeDescriptor, linkDescriptor)
			);
			link.setId(linkId);
			relationshipLinkRepository.create(link);
		} catch (JacksonException e) {
			throw new InvalidInputException(ExceptionKeys.SME_WORKSPACE_IMPORT_ERROR_KEY, "Cannot parse link with id " + linkId)
				.withAnonymityMessage("Cannot parse link.");
		}
	}

	@NotNull private static LinkDescriptor prepareLinkDescriptor(SmeWorkspaceLinkDescriptor smeDescriptor) {
		List<RelationshipRoleSpec> relationshipRoleSpecs = smeDescriptor.getEntities().stream()
			.map(sl -> new RelationshipRoleSpec(sl.getRole(), sl.getDocRef()))
			.toList();
		return new LinkDescriptor(smeDescriptor.getRelationshipModel(), relationshipRoleSpecs, smeDescriptor.getLinkDocumentDocRef(),
			LinkPosition.TOP);
	}

	private static ComputedRank getComputedRank(SmeWorkspaceLinkDescriptor smeDescriptor, LinkDescriptor linkDescriptor) {
		// Look up rank values by role name rather than by positional index so that the mapping
		// is correct regardless of the iteration order used by the exporter's HashMap.keySet().
		String sourceRoleName = linkDescriptor.getSourceRole().getRole();
		String targetRoleName = linkDescriptor.getTargetRole().getRole();
		String sourceRank = findRoleOrder(smeDescriptor, sourceRoleName);
		String targetRank = findRoleOrder(smeDescriptor, targetRoleName);
		if (sourceRank == null || targetRank == null) {
			return null;
		}
		return new ComputedRank(sourceRank, targetRank);
	}

	private static String findRoleOrder(SmeWorkspaceLinkDescriptor smeDescriptor, String roleName) {
		return smeDescriptor.getEntities().stream()
			.filter(e -> roleName.equals(e.getRole()))
			.map(SmeWorkspaceRoleSpec::getRoleOrder)
			.findFirst()
			.orElse(null);
	}
}
