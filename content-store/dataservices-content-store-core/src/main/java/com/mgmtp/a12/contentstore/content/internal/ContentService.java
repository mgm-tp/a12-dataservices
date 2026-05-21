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
package com.mgmtp.a12.contentstore.content.internal;

import java.io.InputStream;
import java.util.Optional;

import com.mgmtp.a12.contentstore.ContentPersistenceResult;
import com.mgmtp.a12.contentstore.content.internal.jpa.entity.ContentHeaderEntity;

public interface ContentService {

	/**
	 * Implementation to persist unique content into storage system
	 *
	 * @param id Unique id of content for persisting
	 * @param persistentType type of persistence, currently support `public` and `private`,
	 * `public` content can be downloaded directly from this service
	 * while `private` content only can be downloaded via temporary downloadable url by requesting download ticket.
	 * @param contentType string media type of input stream
	 * @param inputStream the binary stream to be persisted
	 * @return Result of persistence process: size of persisted content and downloadable url for public content.
	 */
	ContentPersistenceResult save(String id, String persistentType, String contentType, InputStream inputStream);

	/**
	 * Implementation to get a specific content binary by content id
	 *
	 * @param id unique id of target content entity
	 * @return Binary of content
	 */
	Optional<byte[]> findBinaryContentById(String id);

	/**
	 * Implementation performs a check if a content header entity is existed or not by id and persistentType
	 *
	 * @param id
	 * @param persistentType
	 * @return `true` if exist or `false` if not exist
	 */
	boolean exists(String id, String persistentType);

	/**
	 * Implementation to retrieve content header by id and persistent type
	 *
	 * @param id of content for persisting
	 * @param persistentType type of persistence, currently support `public` and `private`,
	 * 	 * `public` content can be downloaded directly from this service
	 * 	 * while `private` content only can be downloaded via temporary downloadable url by requesting download ticket
	 * @return Content header
	 */
	Optional<ContentHeaderEntity> findByContentIdAndPersistentType(String id, String persistentType);

	/**
	 * Implementation to retrieve content header by id
	 *
	 * @param id of content for persisting
	 * @return Content header
	 */

	Optional<ContentHeaderEntity> findHeaderById(String id);

	/**
	 * Implementation to delete content from storage system if it's exist
	 *
	 * @param id of content for deleting
	 */
	void deleteContentById(String id);
}
