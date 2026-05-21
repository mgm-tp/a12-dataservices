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
package com.mgmtp.a12.dataservices.common.content.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.tika.mime.MediaType;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.events.ContentTypeDetectedEvent;

public class TikaContentTypeDetectorTest {

	@Mock private ApplicationEventPublisher publisher;

	private TikaContentTypeDetector tikaContentTypeDetector;
	private AutoCloseable mocks;

	@BeforeClass public void init() {
		mocks = MockitoAnnotations.openMocks(this);
		tikaContentTypeDetector = new TikaContentTypeDetector(publisher, true);
	}

	@AfterClass public void cleanup() throws Exception {
		mocks.close();
	}

	@Test
	public void probeContentType_shouldPublishContentTypeDetectedEvent() throws IOException {
		TikaWrapper tikaWrapper = Mockito.mock(TikaWrapper.class);
		Mockito.when(tikaWrapper.detect(ArgumentMatchers.anyString())).thenReturn(MediaType.TEXT_PLAIN);

		InputStream inputStream = IOUtils.toInputStream("Content", StandardCharsets.UTF_8);
		String filename = "filename";
		String probedContentType = tikaContentTypeDetector.probeContentType(inputStream, filename);

		Assert.assertEquals(probedContentType, MediaType.TEXT_PLAIN.toString());
		Mockito.verify(publisher).publishEvent((ContentTypeDetectedEvent) ArgumentMatchers.argThat(event -> {
			ContentTypeDetectedEvent contentTypeDetectedEvent = (ContentTypeDetectedEvent) event;
			Assert.assertEquals(contentTypeDetectedEvent.getDetectedMimeType(), MediaType.TEXT_PLAIN.toString());
			Assert.assertEquals(contentTypeDetectedEvent.getFilename(), filename);
			Assert.assertNotNull(contentTypeDetectedEvent.getInputStream());
			return true;
		}));
	}

	@Test
	public void getContentLength_shouldReturnCorrectLength() throws IOException {
		byte[] content = new ClassPathResource("/attachments/application.json").getContentAsByteArray();
		long length = tikaContentTypeDetector.getContentLength(content);

		Assert.assertEquals(length, content.length);
	}

}
