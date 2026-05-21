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
package com.mgmtp.a12.dataservices.relationship;

import java.time.Instant;
import java.util.Map;

import com.mgmtp.a12.dataservices.document.DocumentReference;

/**
 * Relationship Link interface. Contains only references to the documents. The actual documents need to be loaded separately.
 *
 */
public interface RelationshipLink {

	/**
	 * Gets the unique identifier of this relationship link.
	 * @return String the unique identifier
	 */
	String getId();

	/**
	 * Gets the relationship model of this relationship link.
	 * @return String the relationship model
	 */
	String getRelationshipModel();

	/**
	 * Gets the creation timestamp of this relationship link.
	 * @return Instant the creation timestamp
	 */
	Instant getCreatedAt();

	/**
	 * Gets the document reference of the link document.
	 * @return DocumentReference the document reference
	 */
	DocumentReference getLinkDocumentDocRef();

	/**
	 * Gets the roles of this relationship link.
	 * @return Map<String, ? extends RelationshipRole> the roles
	 */
	Map<String, ? extends RelationshipRole> getRoles();

	/**
	 * Sets the document reference of the link document.
	 * @param documentReference the document reference to set
	 */
	void setLinkDocumentDocRef(DocumentReference documentReference);

	/**
	 * Adds a role to this relationship link.
	 * @param relationshipRole the role to add
	 */
	void addRole(RelationshipRole relationshipRole);
}
