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
package com.mgmtp.a12.dataservices.relationship.internal;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.factory.RelationshipLinkFactory;
import com.mgmtp.a12.dataservices.relationship.internal.ranks.ComputedRank;
import com.mgmtp.a12.dataservices.relationship.internal.ranks.RelationshipRankService;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;
import com.mgmtp.a12.dataservices.relationship.validation.RelationshipValidationSupport;

import lombok.RequiredArgsConstructor;

/**
 * Default implementation of `RelationshipLinkFactory`.
 *
 * Validates the link descriptor and either reuses a provided `ComputedRank` or delegates
 * rank computation to `RelationshipRankService` before constructing the `RelationshipLink`.
 */
@RequiredArgsConstructor
public class DefaultRelationshipLinkFactory implements RelationshipLinkFactory {

	private final RelationshipRankService relationshipRankService;
	private final RelationshipValidationSupport relationshipValidationSupport;

	@Override
	public RelationshipLink createLink(LinkDescriptor linkDescriptor, DocumentReference linkDocRef) {
		return getRelationshipLink(linkDescriptor, linkDocRef, null);
	}

	/**
	 * Creates a relationship link using a pre-computed rank, skipping rank computation.
	 *
	 * @param linkDescriptor the descriptor containing relationship model and role specifications
	 * @param linkDocRef     the document reference for the link document, or `null` if none
	 * @param computedRank   the pre-computed rank to assign to the link roles, or `null` to compute a new rank
	 * @return the created relationship link
	 */
	public RelationshipLink createLink(LinkDescriptor linkDescriptor, DocumentReference linkDocRef, ComputedRank computedRank) {
		return getRelationshipLink(linkDescriptor, linkDocRef, computedRank);
	}

	@NotNull private RelationshipLink getRelationshipLink(LinkDescriptor linkDescriptor, DocumentReference linkDocRef,
		ComputedRank computedRank) {
		relationshipValidationSupport.validateLink(linkDescriptor, linkDocRef);

		if (computedRank == null) {
			computedRank = relationshipRankService.computeRank(linkDescriptor);
		}

		RelationshipLink newLink = new DataServicesRelationshipLink(linkDescriptor.getRelationshipModel());
		List<RelationshipRoleSpec> entities = linkDescriptor.getEntities();
		newLink.addRole(new DataServicesRelationshipRole(entities.get(0).getRole(), entities.get(0).getDocRef(), computedRank.getOrderRank()));
		newLink.addRole(new DataServicesRelationshipRole(entities.get(1).getRole(), entities.get(1).getDocRef(), computedRank.getComplementaryOrderRank()));
		newLink.setLinkDocumentDocRef(linkDocRef);
		return newLink;
	}
}
