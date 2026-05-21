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
package com.mgmtp.a12.examples.util;

import com.mgmtp.a12.dataservices.common.content.ContentTypeDetector;
import org.apache.tika.mime.MediaType;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Listeners(MockitoTestNGListener.class)
public class MediaTypeUtilsTest {

	@Mock
	private ContentTypeDetector contentTypeDetector;

	private MediaTypeUtils utils;

	@BeforeMethod
	public void setUp() {
		utils = new MediaTypeUtils(contentTypeDetector);
	}

	@Test
	public void mediaTypeEquals_returnsTrue_whenDetectedMatchesExpected() throws Exception {
		byte[] bytes = new byte[] {1, 2, 3};
		when(contentTypeDetector.probeContentType(any(ByteArrayInputStream.class), any()))
			.thenReturn("application/pdf");

		boolean result = utils.mediaTypeEquals(bytes, MediaType.parse("application/pdf"));
		assertTrue(result);
	}

	@Test
	public void mediaTypeEquals_returnsFalse_whenDetectedDoesNotMatchExpected() throws Exception {
		byte[] bytes = new byte[] {4, 5, 6};
		when(contentTypeDetector.probeContentType(any(ByteArrayInputStream.class), any()))
			.thenReturn("image/png");

		boolean result = utils.mediaTypeEquals(bytes, MediaType.parse("image/jpeg"));
		assertFalse(result);
	}

	@Test
	public void mediaTypeEquals_returnsTrue_onIOException_whenExpectedIsOctetStream() throws Exception {
		byte[] bytes = new byte[] {7, 8, 9};
		when(contentTypeDetector.probeContentType(any(ByteArrayInputStream.class), any()))
			.thenThrow(new IOException("probe failed"));

		boolean result = utils.mediaTypeEquals(bytes, MediaType.OCTET_STREAM);
		assertTrue(result);
	}

	@Test
	public void mediaTypeEquals_returnsFalse_onIOException_whenExpectedIsNotOctetStream() throws Exception {
		byte[] bytes = new byte[] {10, 11, 12};
		when(contentTypeDetector.probeContentType(any(ByteArrayInputStream.class), any()))
			.thenThrow(new IOException("probe failed"));

		boolean result = utils.mediaTypeEquals(bytes, MediaType.parse("application/pdf"));
		assertFalse(result);
	}

	@Test
	public void mediaTypeEquals_handlesCustomType_applicationDownload() throws Exception {
		byte[] bytes = new byte[] {13, 14, 15};
		when(contentTypeDetector.probeContentType(any(ByteArrayInputStream.class), any()))
			.thenReturn("application/x-msdownload");

		boolean result = utils.mediaTypeEquals(bytes, MediaTypeUtils.APPLICATION_DOWNLOAD);
		assertTrue(result);
	}
}
