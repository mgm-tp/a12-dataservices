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

import java.io.InputStream;
import java.util.Optional;

import com.mgmtp.a12.dataservices.attachment.AttachmentUrl;
import com.mgmtp.a12.dataservices.attachment.TypeOfTheContent;

import lombok.NonNull;

/**
 * Repository abstraction for persisting and accessing attachments and thumbnails in the content store.
 *
 * Security concerns are handled outside this repository.
 */
public interface IAttachmentRepository {

	/**
	 * Persists an attachment or thumbnail in the content store.
	 *
	 * @param id The identifier of the attachment or thumbnail to persist; must not be null.
	 * @param is The binary stream to persist; must not be null.
	 * @param filename The file name suggested for download; must not be null.
	 * @param type The content type discriminator; determines persistence behavior ({@link TypeOfTheContent}); must not be null.
	 * @param mimeType The MIME type of the content. If non-null, the MIME type has already been probed by Data Services
	 *   and the Content Store must use it as-is. If null, Data Services did not probe and the Content Store is responsible
	 *   for probing the MIME type itself.
	 * @return The result describing the persisted content.
	 */
	AttachmentPersistenceResult create(@NonNull String id, @NonNull InputStream is, @NonNull String filename, @NonNull TypeOfTheContent type, String mimeType);

	/**
	 * Finds a download URL by id based on {@link TypeOfTheContent}.
	 *
	 * @param id The identifier of the attachment or thumbnail to resolve; must not be null.
	 * @param filename The file name suggested for download; must not be null.
	 * @param type The content type discriminator; determines how the content is downloaded ({@link TypeOfTheContent}); must not be null.
	 * @return The URL for downloading the attachment or thumbnail, if available.
	 */
	Optional<AttachmentUrl> findUrl(@NonNull String id, @NonNull String filename, @NonNull TypeOfTheContent type);

	/**
	 * Deletes an attachment or thumbnail by its identifier.
	 *
	 * @param id The identifier of the content to delete; must not be null.
	 */
	void delete(@NonNull String id);
}
