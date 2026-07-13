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
package com.mgmtp.a12.dataservices.attachment;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Main entrypoint for working with attachments. Provides methods to create attachments and to retrieve URLs for downloading attachments and their thumbnails.
 *
 */
@OnlyForUsage public interface AttachmentService {

	/**
	 * Saves an attachment that is passed as InputStream.
	 *
	 * @param is The input stream that contains the attachment; must not be null.
	 * @param filename A file name that can be used by clients; must not be null.
	 * @param documentModelName The document model for which read permission is required to save the attachment; must not be null.
	 * @param pathToField Currently not used.
	 * @param annotations The list of annotations for this attachment; must not be null.
	 * @return The attachment header containing the attachment metadata.
	 */
	@Transactional
	AttachmentHeader createAttachment(@NonNull InputStream is, @NonNull String filename, @NonNull String documentModelName, @NonNull String pathToField, @NonNull List<AttachmentAnnotation> annotations);

	/**
	 * Loads the attachment URL for downloading the attachment with the passed attachment id that is referenced by the document specified by the passed docRef.
	 *
	 * @param attachmentId The attachment id.
	 * @param docRef The document reference of the document that references this attachment.
	 * @return An Optional with a URL for downloading this attachment, or an empty Optional if it could not be found.
	 */
	Optional<AttachmentUrl> findAttachmentUrl(@NonNull String attachmentId, @NonNull DocumentReference docRef);

	/**
	 * Loads the thumbnail URL for downloading the thumbnail of given type for the passed attachment id.
	 *
	 * @param attachmentId The attachment id.
	 * @param type The thumbnail type.
	 * @return An Optional with a URL for downloading this thumbnail, or an empty Optional if it could not be found.
	 */
	Optional<AttachmentUrl> findThumbnailUrl(@NonNull String attachmentId, @NonNull ThumbnailType type);

	/**
	 * Loads the thumbnail URL for downloading the thumbnail of given type for the passed attachment header.
	 *
	 * @param header The attachment header.
	 * @param type The thumbnail type.
	 * @return An Optional with a URL for downloading this thumbnail, or an empty Optional if it could not be found.
	 */
	Optional<AttachmentUrl> findThumbnailUrl(@NonNull AttachmentHeader header, @NonNull ThumbnailType type);
}
