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
package com.mgmtp.a12.contentstore.service.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.contentstore.AbstractContentStoreTest;
import com.mgmtp.a12.contentstore.TestConfiguration;
import com.mgmtp.a12.contentstore.autoconfigure.internal.ContentStoreAutoConfiguration;
import com.mgmtp.a12.contentstore.autoconfigure.internal.ContentStoreRepositoryConfiguration;
import com.mgmtp.a12.contentstore.exception.TimeoutException;
import com.mgmtp.a12.contentstore.utils.Constants;

@SpringBootTest(classes = { TestConfiguration.class,
	ContentStoreAutoConfiguration.class, ContentStoreRepositoryConfiguration.class },
	properties = {
		"mgmtp.a12.dataservices.contentstore.enableDefaultDownloadListener=false",
		"mgmtp.a12.dataservices.contentstore.contentWaitReadyTimeout=3000"
	})
public class ContentStoreServiceIT extends AbstractContentStoreTest {

	@BeforeMethod
	public void initData() {
		privateContentId = UUID.randomUUID().toString();
		InputStream privateContentStream = new ByteArrayInputStream(PRIVATE_UPLOAD_CONTENT.getBytes());
		contentStoreService.saveContent(privateContentId, Constants.PERSISTENT_TYPE_PRIVATE, privateContentStream, FILE_NAME);
	}

	@Test(expectedExceptions = TimeoutException.class) public void shouldThrowExceptionAfterTimeout() {
		String url = contentStoreService.requestContentUrl(privateContentId, 3000);
		String ticketId = url.substring(url.lastIndexOf("/") + 1);

		contentStoreService.getContent(ticketId);
	}
}
