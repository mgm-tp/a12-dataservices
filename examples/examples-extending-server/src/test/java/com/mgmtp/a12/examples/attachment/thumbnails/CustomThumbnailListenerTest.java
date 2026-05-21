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
package com.mgmtp.a12.examples.attachment.thumbnails;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.examples.util.ResourceUtil;

import com.mgmtp.a12.dataservices.attachment.DataServicesAttachment;
import com.mgmtp.a12.dataservices.attachment.events.AttachmentThumbnailBeforeSaveEvent;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

@Listeners(MockitoTestNGListener.class)
public class CustomThumbnailListenerTest {

	@Mock
	private ResourceUtil resourceUtil;

	@DataProvider(name = "customThumbnailCases")
	public Object[][] customThumbnailCases() {
		return new Object[][] {
			{ "test.pdf", "application/pdf", new byte[] { 1, 2 }, new byte[] { 3, 4 } },
			{ "image.png", "image/png", new byte[] { 5, 6 }, new byte[] { 7, 8 } }
		};
	}

	@Test(expectedExceptions = UnexpectedException.class,
		expectedExceptionsMessageRegExp = "Throwing exception for rolling back persisted attachment from content store")
	public void attachmentThumbnailBeforeCreateListener_rollbackAttachment_throwsUnexpectedException() {
		CustomThumbnailListener listener = new CustomThumbnailListener(resourceUtil);
		AttachmentThumbnailBeforeSaveEvent event = prepareEvent(false, CustomThumbnailListener.ROLLBACK_ATTACHMENT_FILE_NAME, "image/png");
		listener.attachmentThumbnailBeforeCreateListener(event);
	}

	@Test(dataProvider = "customThumbnailCases")
	public void attachmentThumbnailBeforeCreateListener_supportedMimeTypes_setsCustomThumbnails(String filename, String mimeType, byte[] bigBytes, byte[] smallBytes) throws IOException {
		CustomThumbnailListener listener = new CustomThumbnailListener(resourceUtil);
		AttachmentThumbnailBeforeSaveEvent event = prepareEvent(false, filename, mimeType);

		InputStream bigStream = new ByteArrayInputStream(bigBytes);
		InputStream smallStream = new ByteArrayInputStream(smallBytes);

		when(resourceUtil.getInputStream("classpath:attachment/big_thumb.png")).thenReturn(bigStream);
		when(resourceUtil.getInputStream("classpath:attachment/small_thumb.png")).thenReturn(smallStream);

		listener.attachmentThumbnailBeforeCreateListener(event);

		Assert.assertTrue(event.getThumbnailBig().isPresent());
		Assert.assertTrue(event.getThumbnailSmall().isPresent());
		Assert.assertEquals(event.getThumbnailBig().get().get().readAllBytes(), bigBytes);
		Assert.assertEquals(event.getThumbnailSmall().get().get().readAllBytes(), smallBytes);

		verify(resourceUtil, times(1)).getInputStream("classpath:attachment/big_thumb.png");
		verify(resourceUtil, times(1)).getInputStream("classpath:attachment/small_thumb.png");
	}

	@Test
	public void attachmentThumbnailBeforeCreateListener_unsupportedMimeType_doesNotSetThumbnails() {
		CustomThumbnailListener listener = new CustomThumbnailListener(resourceUtil);
		AttachmentThumbnailBeforeSaveEvent event = prepareEvent(true, "file.jpg", "image/jpeg");

		listener.attachmentThumbnailBeforeCreateListener(event);

		verify(event, never()).setThumbnailBig(any());
		verify(event, never()).setThumbnailSmall(any());
		verify(resourceUtil, never()).getInputStream(anyString());
	}

	private AttachmentThumbnailBeforeSaveEvent prepareEvent(boolean mockEvent, String filename, String mimeType) {
		DataServicesAttachment dataServicesAttachment = mock(DataServicesAttachment.class);
		AttachmentThumbnailBeforeSaveEvent event =
			mockEvent ? mock(AttachmentThumbnailBeforeSaveEvent.class) : new AttachmentThumbnailBeforeSaveEvent(dataServicesAttachment);
		AttachmentHeader header = mock(AttachmentHeader.class);

		when(header.getFilename()).thenReturn(filename);
		when(header.getMimeType()).thenReturn(mimeType);
		when(dataServicesAttachment.getHeader()).thenReturn(header);
		if (mockEvent) {
			when(event.getDataServicesAttachment()).thenReturn(dataServicesAttachment);
		}
		return event;
	}
}
