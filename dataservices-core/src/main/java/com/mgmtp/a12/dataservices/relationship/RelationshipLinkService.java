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

import java.util.Collection;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterDeleteEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterRepositoryLoadEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentAfterRepositoryCreateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelAfterLoadEvent;
import com.mgmtp.a12.dataservices.relationship.events.RelationshipLinkAfterCreateEvent;
import com.mgmtp.a12.dataservices.relationship.events.RelationshipLinkAfterDeleteEvent;
import com.mgmtp.a12.dataservices.relationship.events.RelationshipLinkAfterUpdateEvent;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * The interface provides methods for commands and queries of relationship links.
 */
@OnlyForUsage public interface RelationshipLinkService {

	/**
	 * Creates a new relationship link with the specified link descriptor.
	 *
	 * @param linkDescriptor the descriptor of the link to create
	 * @return the created RelationshipLink
	 * @event {@link RelationshipLinkAfterCreateEvent}
	 * @event {@link DocumentAfterRepositoryCreateEvent}
	 * @event {@link DocumentAfterCreateEvent}
	 * @event {@link ModelAfterLoadEvent}
	 */
	RelationshipLink create(@NonNull LinkDescriptor linkDescriptor);

	/**
	 * Creates a new relationship link with the specified link descriptor and link document.
	 *
	 * This method is executed within a JPA transaction. Thus, if an error occurs during execution,
	 * all changes are rolled back in the database.
	 *
	 * @param linkDescriptor the descriptor of the link to create
	 * @param linkDocument the link document associated with the link
	 * @return the created RelationshipLink
	 * @event {@link RelationshipLinkAfterCreateEvent}
	 * @event {@link DocumentAfterRepositoryCreateEvent}
	 * @event {@link DocumentAfterCreateEvent}
	 * @event {@link ModelAfterLoadEvent}
	 *
	 */
	RelationshipLink create(@NonNull LinkDescriptor linkDescriptor, @NonNull String linkDocument);

	/**
	 * Creates a new relationship link with the specified link descriptor and link document reference.
	 *
	 * @param linkDescriptor the descriptor of the link to create
	 * @param linkDocRef the reference to the link document associated with the link
	 * @return the created RelationshipLink
	 * @event {@link RelationshipLinkAfterCreateEvent}
	 * @event {@link DocumentAfterRepositoryCreateEvent}
	 * @event {@link DocumentAfterCreateEvent}
	 * @event {@link ModelAfterLoadEvent}
	 */
	RelationshipLink create(@NonNull LinkDescriptor linkDescriptor, @NonNull DocumentReference linkDocRef);

	/**
	 * Updates an existing relationship link.
	 *
	 * This method is executed within a JPA transaction. Thus, if an error occurs during execution,
	 * all changes are rolled back in the database.
	 *
	 * @param id the ID of the relationship link to update
	 * @param linkDescriptor the new descriptor for the link
	 * @param linkDocument the new link document associated with the link; may be null if unchanged
	 * @return the updated RelationshipLink
	 * @event {@link ModelAfterLoadEvent}
	 * @event {@link RelationshipLinkAfterUpdateEvent}
	 */
	RelationshipLink update(
		@NonNull String id,
		@NonNull LinkDescriptor linkDescriptor,
		String linkDocument
	);

	/**
	 * Deletes a relationship link by its ID.
	 *
	 * This method is executed within a JPA transaction. Thus, if an error occurs during execution,
	 * all changes are rolled back in the database.
	 *
	 * @param id the ID of the relationship link to delete
	 * @event {@link RelationshipLinkAfterDeleteEvent}
	 * @event {@link ModelAfterLoadEvent}
	 * @event {@link DocumentAfterDeleteEvent}
	 * @event {@link DocumentAfterRepositoryLoadEvent}
	 */
	void delete(@NonNull String id);

	/**
	 * Deletes all relationship links with the specified IDs.
	 *
	 * This method is executed within a JPA transaction. Thus, if an error occurs during execution,
	 * all changes are rolled back in the database.
	 *
	 * @param id the set of IDs of the relationship links to delete
	 * @event {@link RelationshipLinkAfterDeleteEvent}
	 * @event {@link ModelAfterLoadEvent}
	 * @event {@link DocumentAfterDeleteEvent}
	 * @event {@link DocumentAfterRepositoryLoadEvent}
	 */
	void deleteAllByIds(@NonNull Set<String> id);

	/**
	 * This method is loading only link information. Source, target and link documents are not loaded.
	 *
 *
	 * Loads a relationship link by its ID.
	 *
	 * @param id the ID of the relationship link to load
	 * @return the loaded RelationshipLink
	 * @throws NotFoundException if the relationship link with the specified ID is not found
	 * @event {@link ModelAfterLoadEvent}
	 * @event {@link DocumentAfterRepositoryLoadEvent}
	 */
	RelationshipLink load(@NonNull String id) throws NotFoundException;

	/**
	 * Relinks an existing relationship link with a new descriptor.
	 *
	 * @param linkDescriptor the new descriptor for the link
	 * @param linkId the ID of the existing relationship link to relink
	 * @return the relinked RelationshipLink
	 * @event {@link RelationshipLinkAfterCreateEvent}
	 * @event {@link RelationshipLinkAfterDeleteEvent}
	 * @event {@link ModelAfterLoadEvent}
	 */
	RelationshipLink relink(@NonNull LinkDescriptor linkDescriptor, @NonNull String linkId);

	/**
	 * Loads a page of relationship links.
	 * IMPORTANT: In case of null target and source RelationshipLinkSpecification the workaround for correct paging on skip non-loadable documents will not work,
	 * all relationship links for the relationship model will be returned with appropriate pageable; the caller method must handle skipping non-loadable documents within links.
	 *
	 * @param specification the specification to filter links
	 * @param pageable the pagination information
	 * @return a page of RelationshipLinks
	 * @event {@link ModelAfterLoadEvent}
	 */
	Page<? extends RelationshipLink> load(@NonNull RelationshipLinkSpecification specification, Pageable pageable);

	/**
	 * Deletes relationship links by their role document references.
	 *
	 * This method is executed within a JPA transaction. Thus, if an error occurs during execution,
	 * all changes are rolled back in the database.
	 *
	 * @param documentReferences the collection of role document references whose associated links to delete
	 * @event {@link RelationshipLinkAfterDeleteEvent}
	 * @event {@link ModelAfterLoadEvent}
	 * @event {@link DocumentAfterDeleteEvent}
	 * @event {@link DocumentAfterRepositoryLoadEvent}
	 */
	void deleteByRoleDocRefs(Collection<DocumentReference> documentReferences);
}
