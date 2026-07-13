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
package com.mgmtp.a12.contentstore.service;

import java.io.InputStream;
import java.util.Optional;

import com.mgmtp.a12.contentstore.ContentPersistenceResult;
import com.mgmtp.a12.contentstore.annotation.internal.ContentStoreTransactional;
import com.mgmtp.a12.contentstore.content.ContentStream;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Service interface for content store operations. Implementations handle content storage, retrieval, and management.
 */
@OnlyForUsage public interface ContentStoreService {

	/**
	 * Registers a download ticket for private content and returns a time-limited URL containing the ticket id.
	 *
	 * @param contentId unique content identifier; must refer to private content.
	 * @param duration time to live in seconds for the download ticket (seconds).
	 * @return time-limited downloadable URL derived from the registered ticket.
	 */
	@ContentStoreTransactional
	String requestContentUrl(@NonNull String contentId, long duration);

	/**
	 * Resolves a public download URL for the given content id, if available.
	 *
	 * @param contentId unique content identifier; refers to public content.
	 * @return URL of the public content if available; {@link Optional#empty()} otherwise.
	 */
	Optional<String> findPublicContentUrl(@NonNull String contentId);

	/**
	 * Returns a content stream for the provided identifier. If the id is a valid ticket id, a private content stream is returned.
	 * Otherwise, a public content stream is resolved using the content id.
	 *
	 * @param id ticket id (private) or content id (public); must not be blank.
	 * @return {@link ContentStream} describing the content and its readiness state.
	 * @see com.mgmtp.a12.contentstore.content.ContentStream
	 */
	@ContentStoreTransactional
	ContentStream getContent(@NonNull String id);

	/**
	 * Checks whether content exists for the given id and persistent type.
	 *
	 * @param id unique content identifier.
	 * @param persistentType persistent type discriminator; case sensitivity and allowed values depend on implementation.
	 * @return true if content exists; false otherwise.
	 */
	boolean exists(String id, String persistentType);

	/**
	 * Persists a content stream to the storage system after validating its size. The filename is used as a hint for MIME type detection.
	 *
	 * @param contentId unique UUID of the content to store.
	 * @param persistentType type of the content to be stored (e.g., public or private).
	 * @param inputStream binary stream of the content; must not be null and must be readable.
	 * @param filename original filename used to improve MIME type detection; may be null.
	 * @return result including content size and public URL if applicable.
	 */
	@ContentStoreTransactional
	ContentPersistenceResult saveContent(@NonNull String contentId, String persistentType, @NonNull InputStream inputStream, String filename);

	/**
	 * Persists a content stream to the storage system after validating its size. The filename is used as a hint for MIME type detection.
	 * If external MIME type trust is enabled, `mimeType` overrides detection.
	 *
	 * @param contentId unique UUID of the content to store.
	 * @param persistentType type of the content to be stored (e.g., public or private).
	 * @param inputStream binary stream of the content; must not be null and must be readable.
	 * @param filename original filename used to improve MIME type detection; may be null.
	 * @param mimeType MIME type of the uploading content; only honored if
	 *                 `mgmtp.a12.dataservices.contentstore.server.api.mimeType.trustExternalMimeType.enabled` is true; may be null.
	 * @return result including content size and public URL if applicable.
	 */
	@ContentStoreTransactional
	ContentPersistenceResult saveContent(@NonNull String contentId, String persistentType, @NonNull InputStream inputStream, String filename,
		String mimeType);

	/**
	 * Deletes content by id.
	 *
	 * @param contentId unique UUID of the content to delete; must not be null.
	 */
	@ContentStoreTransactional
	void deleteById(@NonNull String contentId);
}
