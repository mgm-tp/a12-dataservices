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

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.io.IOUtils;

import com.mgmtp.a12.contentstore.content.internal.jpa.entity.ContentEntity;
import com.mgmtp.a12.contentstore.content.internal.jpa.repository.ContentJpaRepository;
import com.mgmtp.a12.contentstore.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DatabaseContentRepository implements ContentRepository {
	private final ContentJpaRepository contentJpaRepository;

	@Override public Optional<byte[]> findBinaryContentById(String id) {
		return contentJpaRepository.findById(id).map(ContentEntity::getContent);
	}

	@Override public long save(String contentId, InputStream inputStream) {
		try(InputStream in = inputStream) {
			ContentEntity contentEntity = contentJpaRepository.findById(contentId).orElse(ContentEntity.builder().id(contentId).build());
			byte[] inputBytes = IOUtils.toByteArray(in);
			contentEntity.setContent(inputBytes);
			contentJpaRepository.save(contentEntity);
			return inputBytes.length;
		} catch (IOException e) {
			throw new UnexpectedException(ExceptionKeys.UNEXPECTED_ERROR_KEY, e.getMessage()).withAnonymityMessage("Saving the content failed.");
		}
	}

	/**
	 * Implementation to delete content in storage but for database mode, content entity will be deleted by foreign key constraint.
	 * So this method does nothing.
	 *
	 * @param id Unique id of content for deleting.
	 */
	@Override public void delete(String id) {
		// do nothing because content will be deleted with header by using foreign key.
	}

}
