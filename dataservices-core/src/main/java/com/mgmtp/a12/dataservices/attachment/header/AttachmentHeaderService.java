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
package com.mgmtp.a12.dataservices.attachment.header;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import lombok.NonNull;
import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentReference;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.reference.GenericReference;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Service interface for managing {@link AttachmentHeader}s. {@link AttachmentHeader}s are used to store metadata about attachments (e.g.
 * filename, mime type, size) as well as references to documents the attachment is associated with.
 */
@OnlyForUsage public interface AttachmentHeaderService {

	/**
	 * Saves a new attachment header in the database.
	 *
	 * @param header The attachment header to persist; must not be null.
	 * @return The created attachment header.
	 */
	@Transactional AttachmentHeader create(@NonNull AttachmentHeader header);

	/**
	 * Delete an existing attachment header.
	 *
	 * @param attachmentId The attachment id
	 */
	void delete(@NonNull String attachmentId);

	/**
	 * Load the attachment header of an existing attachment.
	 *
	 * @param attachmentId The attachment id
	 * @return The attachment header as an {@link Optional}
	 */
	@Transactional(readOnly = true) Optional<AttachmentHeader> load(@NonNull String attachmentId);

	/**
	 * Load all attachment headers which are not assigned to any document and older than the given number of hours.
	 *
	 * @param tmpAttachmentExpireHours The number of hours an attachment may be unassigned before it is considered
	 *            for deletion.
	 * @return A list of unassigned attachment headers older than the given number of hours.
	 */
	@Transactional(readOnly = true) List<AttachmentHeader> loadUnassignedAttachmentsOlderThan(int tmpAttachmentExpireHours);

	/**
	 * Adds the passed reference to the list of references of the given attachment header.
	 *
	 * @param header The header of the attachment to update; must not be null.
	 * @param reference The reference to assign; must not be null.
	 */
	@Transactional void assignAttachment(@NonNull AttachmentHeader header, @NonNull AttachmentReference<? extends GenericReference> reference);

	/**
	 * Removes the passed reference from the list of references of the given attachment header.
	 *
	 * @param header The header of the attachment to update; must not be null.
	 * @param reference The reference to unassign; must not be null.
	 */
	@Transactional void unAssignAttachment(@NonNull AttachmentHeader header, @NonNull  AttachmentReference<? extends GenericReference> reference);

	/**
	 * Remove all references to the attachments for the passed document references.
	 *
	 * @param documentReferences The document references for which all attachment references should be removed.
	 */
	@Transactional void unAssignAttachments(@NonNull Collection<DocumentReference> documentReferences);
}
