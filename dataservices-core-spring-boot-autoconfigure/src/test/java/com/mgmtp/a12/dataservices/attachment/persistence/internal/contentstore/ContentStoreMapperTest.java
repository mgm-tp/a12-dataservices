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
package com.mgmtp.a12.dataservices.attachment.persistence.internal.contentstore;

import java.util.Optional;

import org.mapstruct.factory.Mappers;
import org.testng.annotations.Test;

import com.mgmtp.a12.contentstore.ContentPersistenceResult;
import com.mgmtp.a12.dataservices.attachment.persitence.AttachmentPersistenceResult;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.contentstore.ContentStoreMapper;

import static org.testng.Assert.assertEquals;

public class ContentStoreMapperTest {

	private final ContentStoreMapper attachmentMapper = Mappers.getMapper(ContentStoreMapper.class);

	@Test public void toAttachmentPersistenceResultTest() {
		ContentPersistenceResult contentPersistenceResult = ContentPersistenceResult.builder()
			.url(Optional.of("http://myurl:PORT/path"))
			.size(15)
			.contentType("content type")
			.build();

		AttachmentPersistenceResult attachmentPersistenceResult = attachmentMapper.toAttachmentPersistenceResult(contentPersistenceResult);

		assertEquals(attachmentPersistenceResult.getUrl(), contentPersistenceResult.getUrl());
		assertEquals(attachmentPersistenceResult.getSize(), contentPersistenceResult.getSize());
		assertEquals(attachmentPersistenceResult.getMimeType(), contentPersistenceResult.getContentType());
	}
}
