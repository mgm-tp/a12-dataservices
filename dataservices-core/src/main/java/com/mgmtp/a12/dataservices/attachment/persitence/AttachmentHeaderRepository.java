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
package com.mgmtp.a12.dataservices.attachment.persitence;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentReference;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.AttachmentReferenceEntity;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.reference.GenericReference;

import lombok.NonNull;
/**
 * Repository abstraction for managing {@link AttachmentHeader} entities and their references.
 *
 * Implementations persist headers, query by id, and manage {@link AttachmentReference} relations.
 */
public interface AttachmentHeaderRepository {

	/**
	 * Creates a new attachment header.
	 *
	 * @param header The attachment header to create; must not be null.
	 */
	void create(@NonNull AttachmentHeader header);

	/**
	 * Finds an attachment header by its identifier.
	 *
	 * @param attachmentId The attachment identifier; must not be null.
	 * @return An {@link Optional} containing the header if found; otherwise empty.
	 */
	Optional<AttachmentHeader> findById(@NonNull String attachmentId);

	/**
	 * Deletes an attachment header by its identifier.
	 *
	 * @param attachmentId The attachment identifier to delete; must not be null.
	 */
	void delete(@NonNull String attachmentId);

	/**
	 * Adds a reference to an attachment header.
	 *
	 * @param header The attachment header to update; must not be null.
	 * @param reference The reference to add; must not be null.
	 */
	void addReference(@NonNull AttachmentHeader header, @NonNull AttachmentReference<?> reference);

	/**
	 * Removes a reference from an attachment header.
	 *
	 * @param header The attachment header to update; must not be null.
	 * @param reference The reference to remove; must not be null.
	 */
	void removeReference(@NonNull AttachmentHeader header, @NonNull AttachmentReference<? extends GenericReference> reference);

	/**
	 * Finds and removes all attachment references for the given document references.
	 *
	 * @param documentReferences The document references whose attachment relations are to be removed; must not be null.
	 * @return The list of removed {@link AttachmentReferenceEntity} relations; never null.
	 */
	List<AttachmentReferenceEntity> findAndRemoveReferencesFor(@NonNull Collection<DocumentReference> documentReferences);

	/**
	 * Checks whether the given reference exists for the attachment header.
	 *
	 * @param header The attachment header; must not be null.
	 * @param reference The reference to verify; must not be null.
	 * @return `true` if the reference exists; `false` otherwise.
	 */
	boolean referenceExists(@NonNull AttachmentHeader header,@NonNull AttachmentReference<? extends GenericReference> reference);

	/**
	 * Finds all unassigned attachment headers older than the given threshold instant.
	 *
	 * @param threshold The cutoff instant (attachments older than this are returned); must not be null.
	 * @return The list of matching headers; never null.
	 */
	List<AttachmentHeader> findUnassignedAttachmentsOlderThan(Instant threshold);
}
