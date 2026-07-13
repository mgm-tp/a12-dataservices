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
package com.mgmtp.a12.dataservices.relationship.factory;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.internal.ranks.ComputedRank;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Factory for creating `RelationshipLink` instances.
 *
 * Implementations handle validation and domain object creation.
 * This interface is not intended for external implementation.
 * Inject and use it; do not implement it.
 *
 * @see RelationshipLink
 * @see LinkDescriptor
 */
@OnlyForUsage
public interface RelationshipLinkFactory {

	/**
	 * Creates a relationship link from the given descriptor.
	 *
	 * @param linkDescriptor the link descriptor containing link metadata; must not be `null`
	 * @param linkDocRef     the document reference for the link document; may be `null` if the
	 *                       relationship model does not require a link document
	 * @return a valid `RelationshipLink` instance
	 * @throws IllegalArgumentException if the descriptor is invalid
	 */
	RelationshipLink createLink(LinkDescriptor linkDescriptor, DocumentReference linkDocRef);

	/**
	 * Creates a relationship link using a pre-computed rank, skipping rank computation.
	 * Use this when restoring links from SME Workspace to preserve the original rank values.
	 *
	 * @param linkDescriptor the link descriptor containing link metadata; must not be `null`
	 * @param linkDocRef     the document reference for the link document; may be `null` if the
	 *                       relationship model does not require a link document
	 * @param computedRank   the pre-computed rank to assign to the link roles; must not be `null`
	 * @return a valid `RelationshipLink` instance
	 * @throws IllegalArgumentException if the descriptor is invalid
	 */
	RelationshipLink createLink(LinkDescriptor linkDescriptor, DocumentReference linkDocRef, ComputedRank computedRank);
}
