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

import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentUrl;
import com.mgmtp.a12.dataservices.attachment.ThumbnailType;
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
	 * @return The result describing the persisted content.
	 * @deprecated Use {@link #create(String, InputStream, String, TypeOfTheContent, String)} instead.
	 */
	@Deprecated(since = "37.1.0")
	AttachmentPersistenceResult create(@NonNull String id, @NonNull InputStream is, @NonNull String filename, @NonNull TypeOfTheContent type);

	/**
	 * Implementation will persist attachment or thumbnail to content store.
	 *
	 * @param id id of attachment or thumbnail to be persisted.
	 * @param is Binary stream to be persisted.
	 * @param filename is for setting the name of downloaded file.
	 * @param type is type of the content, decides how the attachment will be persisted {@link TypeOfTheContent}.
	 * @param mimeType The mime type of the content (including the used charset).
	 * @return {@link AttachmentPersistenceResult} result of successful or fail persisting attachment or thumbnail.
	 */
	default AttachmentPersistenceResult create(@NonNull String id, @NonNull InputStream is, @NonNull String filename, @NonNull TypeOfTheContent type,
		@NonNull String mimeType) {
		return create(id, is, filename, type);
	}

	/**
	 * @deprecated Use {@link IAttachmentRepository#create(String, InputStream, String, TypeOfTheContent)} instead
	 */
	@Deprecated(since = "36.2.0")
	AttachmentPersistenceResult createAttachment(@NonNull AttachmentHeader attachmentHeader, @NonNull InputStream is);

	/**
	 * Persists content to the content store.
	 *
	 * @param attachmentHeader The header of the attachment for which to generate the thumbnail; must not be null.
	 * @param is The binary stream to persist; must not be null.
	 * @param filename The file name suggested for download; must not be null.
	 * @return A generated thumbnail URL without invoking the content store's generator.
	 * @deprecated Use {@link IAttachmentRepository#create(String, InputStream, String, TypeOfTheContent)} instead.
	 */
	@Deprecated(since = "37.1.0")
	AttachmentPersistenceResult createAttachment(@NonNull AttachmentHeader attachmentHeader, @NonNull InputStream is, @NonNull String filename);

	/**
	 * @deprecated Use {@link IAttachmentRepository#create(String, InputStream, String, TypeOfTheContent)} instead
	 */
	@Deprecated(since = "37.1.0")
	AttachmentPersistenceResult createThumbnail(@NonNull AttachmentHeader attachmentHeader, @NonNull InputStream is);

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
	 * @deprecated Use {@link IAttachmentRepository#findUrl(String, String, TypeOfTheContent)} instead
	 */
	@Deprecated(since = "37.1.0")
	Optional<AttachmentUrl> findAttachmentUrl(@NonNull String attachmentId, @NonNull String filename);

	/**
	 * @deprecated Use {@link IAttachmentRepository#findUrl(String, String, TypeOfTheContent)} instead
	 */
	@Deprecated(since = "36.0.1")
	Optional<AttachmentUrl> findThumbnailUrl(@NonNull String attachmentId, @NonNull ThumbnailType type);

	/**
	 * @deprecated Use {@link IAttachmentRepository#findUrl(String, String, TypeOfTheContent)} instead
	 */
	@Deprecated(since = "37.1.0")
	Optional<AttachmentUrl> findThumbnailUrl(@NonNull AttachmentHeader attachmentHeader, @NonNull ThumbnailType type);

	/**
	 * Deletes an attachment or thumbnail by its identifier.
	 *
	 * @param id The identifier of the content to delete; must not be null.
	 */
	void delete(@NonNull String id);

	/**
	 * @deprecated Use {@link IAttachmentRepository#delete} instead
	 */
	@Deprecated(since = "36.0.1")
	void deleteThumbnail(@NonNull String attachmentId);
}
