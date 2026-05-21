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

import org.springframework.stereotype.Service;

import com.mgmtp.a12.contentstore.ContentPersistenceResult;
import com.mgmtp.a12.contentstore.configuration.ContentStoreProperties;
import com.mgmtp.a12.contentstore.content.internal.jpa.entity.ContentHeaderEntity;
import com.mgmtp.a12.contentstore.content.internal.jpa.repository.ContentHeaderJpaRepository;
import com.mgmtp.a12.contentstore.utils.Constants;
import com.mgmtp.a12.contentstore.utils.internal.UrlUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service public class DefaultContentService implements ContentService {

	private final ContentRepository contentRepository;
	private final ContentHeaderJpaRepository contentHeaderJpaRepository;
	private final ContentStoreProperties contentStoreProperties;

	@Override public ContentPersistenceResult save(String id, String persistentType, String contentType, InputStream inputStream) {
		ContentPersistenceResult.ContentPersistenceResultBuilder builder = ContentPersistenceResult.builder()
			.contentType(contentType);
		// set downloadable url for public persistent type
		if (Constants.PERSISTENT_TYPE_PUBLIC.equalsIgnoreCase(persistentType)) {
			builder.url(Optional.of(UrlUtils.renderContentUrl(contentStoreProperties, id)));
		}
		// persist content by using content store repository
		ContentHeaderEntity contentHeaderEntity = new ContentHeaderEntity(id, persistentType, contentType);

		return persistContent(id, inputStream, builder, contentHeaderEntity);
	}

	@Override public Optional<byte[]> findBinaryContentById(String id) {
		return contentRepository.findBinaryContentById(id);
	}

	@Override public boolean exists(String id, String persistentType) {
		return contentHeaderJpaRepository.existsByIdAndPersistentType(id, persistentType);
	}

	private ContentPersistenceResult persistContent(String id, InputStream inputStream,
		ContentPersistenceResult.ContentPersistenceResultBuilder builder, ContentHeaderEntity contentHeader) {

		ContentHeaderEntity contentHeaderEntity = contentHeaderJpaRepository.findById(id)
			.map(persistedEntity -> persistedEntity.copy(contentHeader))
			.orElse(contentHeader);

		contentHeaderJpaRepository.save(contentHeaderEntity);

		return builder.size(contentRepository.save(id, inputStream))
			.contentId(id)
			.build();
	}

	@Override public Optional<ContentHeaderEntity> findByContentIdAndPersistentType(String contentId, String persistentType) {
		return contentHeaderJpaRepository.findByIdAndPersistentTypeIgnoreCase(contentId, persistentType);
	}

	@Override public Optional<ContentHeaderEntity> findHeaderById(String id) {
		return contentHeaderJpaRepository.findById(id);
	}

	@Override public void deleteContentById(String id) {
		// There is no roll-back for file delete, so we move file delete to last operation.
		contentHeaderJpaRepository.findById(id).ifPresent(contentHeaderJpaRepository::delete);
		contentRepository.delete(id);
	}
}
