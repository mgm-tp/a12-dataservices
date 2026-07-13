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
package com.mgmtp.a12.dataservices.relationship.spec;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mgmtp.a12.dataservices.document.DocumentReference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Link Descriptor class. Complete representation of a link to be created between entities.
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkDescriptor implements Serializable {

	@Serial private static final long serialVersionUID = 1L;

	private String relationshipModel;
	private List<RelationshipRoleSpec> entities = new ArrayList<>();

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private DocumentReference linkDocumentDocRef;

	/**
	 * The order for which link has a predecessor is defined by {@link #entities}, If predecessor is null and relationship is ordered the link should be the
	 * first. If the predecessor is null and relationship is not ordered it will be ignored
	 */
	private String predecessorLinkRef;

	/**
	 * This is a non-mandatory field that indicates if the desired link will be at the TOP or the BOTTOM in the order
	 * of the relationship. It will be ignored if predecessorLinkRef is being used.
	 */
	private LinkPosition position = LinkPosition.TOP;

	/**
	 * Creates a link descriptor with explicit link document and position.
	 *
	 * @param relationshipModel identifier of the relationship model; must not be null.
	 * @param entities ordered list of role specifications (source first, target second); must not be null.
	 * @param linkDocumentDocRef reference of the link document; may be null if not applicable.
	 * @param position desired positioning in an ordered relationship.
	 */
	public LinkDescriptor(String relationshipModel, List<RelationshipRoleSpec> entities, DocumentReference linkDocumentDocRef, LinkPosition position) {
		this(relationshipModel, entities, linkDocumentDocRef, null, position);
	}

	/**
	 * Creates a link descriptor without link document reference.
	 *
	 * @param relationshipModel identifier of the relationship model; must not be null.
	 * @param entities ordered list of role specifications (source first, target second); must not be null.
	 * @param position desired positioning in an ordered relationship.
	 */
	public LinkDescriptor(String relationshipModel, List<RelationshipRoleSpec> entities, LinkPosition position) {
		this(relationshipModel, entities, null, null, position);
	}

	/**
	 * Creates a link descriptor with default position {@link LinkPosition#TOP}.
	 *
	 * @param relationshipModel identifier of the relationship model; must not be null.
	 * @param entities ordered list of role specifications (source first, target second); must not be null.
	 */
	public LinkDescriptor(String relationshipModel, List<RelationshipRoleSpec> entities) {
		this(relationshipModel, entities, null, LinkPosition.TOP);
	}

	/**
	 * Returns the role specification considered as source (first entity).
	 */
	@JsonIgnore
	public RelationshipRoleSpec getSourceRole() {
		return entities.getFirst();
	}

	/**
	 * Returns the role specification considered as target (second entity).
	 */
	@JsonIgnore
	public RelationshipRoleSpec getTargetRole() {
		return entities.get(1);
	}
}
