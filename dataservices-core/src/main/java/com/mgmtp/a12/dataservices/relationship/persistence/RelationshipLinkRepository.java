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
package com.mgmtp.a12.dataservices.relationship.persistence;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipRole;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Public repository interface for managing persistence operations on relationship links.
 *
 * This interface is the primary extension point for custom relationship link persistence.
 * It provides a complete set of CRUD operations and query methods for relationship links.
 *
 * Data Services registers a default implementation as a Spring bean. External components
 * (such as relational-persistence) should depend on this public interface rather than on
 * the internal implementation class.
 *
 * @see RelationshipLink
 * @see DocumentReference
 */
@OnlyForUsage
public interface RelationshipLinkRepository {

	/**
	 * Creates a new relationship link in the data store.
	 *
	 * @param relationshipLink the relationship link to be created; must not be `null`
	 * @return the created relationship link
	 */
	RelationshipLink create(@NonNull RelationshipLink relationshipLink);

	/**
	 * Updates an existing relationship link with the specified ID in the data store.
	 *
	 * @param id               the ID of the relationship link to be updated; must not be `null`
	 * @param relationshipLink the updated relationship link; must not be `null`
	 * @return the updated relationship link
	 * @throws NotFoundException if no relationship link with the specified ID exists
	 */
	RelationshipLink update(@NonNull String id, @NonNull RelationshipLink relationshipLink) throws NotFoundException;

	/**
	 * Deletes the relationship link with the specified ID from the data store.
	 *
	 * @param id the ID of the relationship link to be deleted; must not be `null`
	 */
	void delete(@NonNull String id);

	/**
	 * Deletes multiple relationship links identified by the provided set of IDs from the data store.
	 *
	 * @param ids the set of IDs of the relationship links to be deleted; must not be `null`
	 */
	void deleteAllByIds(@NonNull Set<String> ids);

	/**
	 * Retrieves the relationship link with the specified ID from the data store, if it exists.
	 *
	 * @param id the ID of the relationship link to be retrieved; must not be `null`
	 * @return an `Optional` containing the retrieved relationship link, or empty if no
	 *         relationship link with the specified ID exists
	 */
	Optional<? extends RelationshipLink> findById(@NonNull String id);

	/**
	 * Retrieves relationship links from the data store based on the specified relationship model name,
	 * paginated according to the provided pageable settings.
	 *
	 * @param modelName the name of the relationship model; must not be `null`
	 * @param pageable  pagination information
	 * @return the paginated relationship links matching the specified relationship model name
	 */
	Page<? extends RelationshipLink> findByRelationshipModelName(@NonNull String modelName, Pageable pageable);

	/**
	 * Retrieves relationship links from the data store based on the specified relationship model name
	 * and source entity details, paginated according to the provided pageable.
	 *
	 * @param relationshipModelName the name of the relationship model; must not be `null`
	 * @param sourceRole            the name of the source role; must not be `null`
	 * @param sourceDocRef          the document reference of the source entity; must not be `null`
	 * @param pageable              pagination information; returns all if no pagination provided
	 * @return the paginated relationship links matching the specified criteria
	 */
	Page<? extends RelationshipLink> findByRelationshipModelNameAndSource(
		@NonNull String relationshipModelName,
		@NonNull String sourceRole,
		@NonNull DocumentReference sourceDocRef,
		Pageable pageable
	);

	/**
	 * Retrieves relationship links from the data store based on the specified relationship model name,
	 * source entity details, and target entity details, paginated according to the provided pageable.
	 *
	 * @param relationshipModelName the name of the relationship model; must not be `null`
	 * @param role1                 the name of the first role (source role); must not be `null`
	 * @param docRef1               the document reference of the first entity (source entity); must not be `null`
	 * @param role2                 the name of the second role (target role); must not be `null`
	 * @param docRef2               the document reference of the second entity (target entity); must not be `null`
	 * @param pageable              pagination information; returns all if no pagination provided
	 * @return the paginated relationship links matching the specified criteria
	 */
	Page<? extends RelationshipLink> findByRelationshipModelNameAndSourceAndTarget(
		@NonNull String relationshipModelName,
		@NonNull String role1,
		@NonNull DocumentReference docRef1,
		@NonNull String role2,
		@NonNull DocumentReference docRef2,
		Pageable pageable
	);

	/**
	 * Retrieves relationship links associated with the specified link document reference,
	 * paginated according to the provided pageable.
	 *
	 * @param docRef   the document reference of the link document; must not be `null`
	 * @param pageable pagination information; returns all if no pagination provided
	 * @return the paginated relationship links associated with the link document
	 */
	Page<? extends RelationshipLink> findByLinkDocument(
		@NonNull DocumentReference docRef,
		Pageable pageable
	);

	/**
	 * Retrieves terminating relationship links based on the specified relationship model name
	 * and terminating role name, paginated according to the provided pageable.
	 *
	 * @param relationshipModelName the name of the relationship model; must not be `null`
	 * @param terminatingRoleName   the name of the terminating role; must not be `null`
	 * @param pageable              pagination information
	 * @return the paginated terminating relationship links matching the specified criteria
	 */
	Page<? extends RelationshipLink> findTerminatingNodes(
		@NonNull String relationshipModelName,
		@NonNull String terminatingRoleName,
		Pageable pageable
	);

	/**
	 * Counts the number of relationship links associated with the specified relationship model name
	 * and roles.
	 *
	 * @param relationshipModelName the name of the relationship model; must not be `null`
	 * @param sourceRole            the source relationship role; must not be `null`
	 * @param targetRole            the target relationship role; must not be `null`
	 * @return the number of relationship links associated with the specified roles
	 */
	long countByRoles(
		@NonNull String relationshipModelName,
		@NonNull RelationshipRole sourceRole,
		@NonNull RelationshipRole targetRole
	);

	/**
	 * Counts the number of relationship links for a given role and document reference.
	 *
	 * @param relationshipModelName the name of the relationship model; must not be `null`
	 * @param role                  the name of the role; must not be `null`
	 * @param docRef                the document reference for the role; must not be `null`
	 * @return the number of relationship links matching the specified role and document reference
	 */
	long countByRole(
		@NonNull String relationshipModelName,
		@NonNull String role,
		@NonNull DocumentReference docRef
	);

	/**
	 * Counts the total number of relationship links for the given relationship model.
	 *
	 * @param relationshipModelName the name of the relationship model; must not be `null`
	 * @return the total number of relationship links for the specified model
	 */
	long countByRelationshipModel(@NonNull String relationshipModelName);

	/**
	 * Retrieves all relationship links that contain any of the specified document references
	 * in their roles, paginated according to the provided pageable.
	 *
	 * This method returns `Page<? extends RelationshipLink>` to avoid leaking internal entity
	 * types to callers.
	 *
	 * @param documentReferences the collection of document references to match against role
	 *                           document references; must not be `null`
	 * @param pageable           pagination information
	 * @return the paginated relationship links whose roles contain any of the specified document references
	 */
	Page<? extends RelationshipLink> findAllByRoleDocRef(@NonNull Collection<DocumentReference> documentReferences, Pageable pageable);

	/**
	 * Counts the number of relationship links where the link document's document reference
	 * is among the specified collection.
	 *
	 * @param documentReferences the collection of document references; must not be `null`
	 * @return the number of matching relationship links
	 */
	long countByLinkInDocumentDocRefs(@NonNull Collection<DocumentReference> documentReferences);
}
