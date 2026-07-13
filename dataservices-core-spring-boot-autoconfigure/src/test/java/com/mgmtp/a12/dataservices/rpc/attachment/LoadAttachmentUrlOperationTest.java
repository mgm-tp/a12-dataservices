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
package com.mgmtp.a12.dataservices.rpc.attachment;

import java.util.Optional;
import java.util.UUID;

import com.mgmtp.a12.dataservices.AbstractDataServiceTest;
import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.attachment.AttachmentUrl;
import com.mgmtp.a12.dataservices.attachment.DataServicesAttachmentURL;
import com.mgmtp.a12.dataservices.attachment.internal.AttachmentMapper;
import com.mgmtp.a12.dataservices.attachment.internal.operation.LoadAttachmentUrlOperation;
import com.mgmtp.a12.dataservices.attachment.AttachmentService;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;

import lombok.SneakyThrows;

public class LoadAttachmentUrlOperationTest extends AbstractDataServiceTest {

	private DocumentReference docRef;
	private String attachmentId;

	private LoadAttachmentUrlOperation loadAttachmentUrlOperation;

	@Mock private AttachmentService attachmentService;
	@Mock private AttachmentMapper attachmentMapper;

	@SneakyThrows
	@BeforeClass public void init() {
		super.init();
		loadAttachmentUrlOperation = new LoadAttachmentUrlOperation(attachmentService, attachmentMapper);
	}

	@BeforeMethod
	public void beforeMethod() {
		attachmentId = UUID.randomUUID().toString();
		docRef = new DocumentReference(RandomStringUtils.randomAlphabetic(10), RandomStringUtils.randomAlphabetic(15));
	}

	@Test public void testFindAttachmentUrl_shouldReturnUrl() {
		String randomUrl = RandomStringUtils.randomAlphabetic(20);
		AttachmentUrl attachmentUrl = new AttachmentUrl(randomUrl);
		DataServicesAttachmentURL dataServicesAttachmentURL = new DataServicesAttachmentURL(randomUrl);
		Mockito.doReturn(Optional.of(attachmentUrl)).when(attachmentService).findAttachmentUrl(attachmentId, docRef);
		Mockito.doReturn(dataServicesAttachmentURL).when(attachmentMapper).toDataServicesAttachmentURL(attachmentUrl);

		DataServicesAttachmentURL url = loadAttachmentUrlOperation.rpc(attachmentId, docRef.toString());

		Assert.assertEquals(url.getLocation(), randomUrl);
	}

	@Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp="No URL from attachmentId .* could be found.")
	public void testAttachmentNotFound_ShouldThrowException() {
		Mockito.doReturn(Optional.empty()).when(attachmentService).findAttachmentUrl(attachmentId, docRef);

		loadAttachmentUrlOperation.rpc(attachmentId, docRef.toString());
	}

	@Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp="No URL from attachmentId .* could be found.")
	public void testDocumentNotFound_ShouldThrowAttachmentException() {
		Mockito.doThrow(new NotFoundException("No URL from attachmentId %s could be found.".formatted(attachmentId)))
			.when(attachmentService).findAttachmentUrl(attachmentId, docRef);

		loadAttachmentUrlOperation.rpc(attachmentId, docRef.toString());
	}

}
