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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for RAR file MIME type detection behavior.
 *
 * <p>These tests document the expected MIME type detection behavior for RAR archive files
 * after the junrar library was excluded from the classpath due to GPL-3.0 license incompatibility.
 *
 * <p>The junrar library can be re-added to the classpath if the project accepts the GPL-3.0 license.
 * See migration notes for 2025.06-ext5 for details.
 *
 */
public class RarMimeTypeDetectionTest {

	/**
	 * RAR file signature bytes (RAR 4.x format).
	 * Hex: 52 61 72 21 1A 07 00
	 */
	private static final byte[] RAR4_SIGNATURE = new byte[] {
		0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00
	};

	/**
	 * RAR file signature bytes (RAR 5.0 format).
	 * Hex: 52 61 72 21 1A 07 01 00
	 */
	private static final byte[] RAR5_SIGNATURE = new byte[] {
		0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00
	};

	@Mock
	private ApplicationEventPublisher publisher;

	private TikaContentTypeDetector tikaContentTypeDetector;
	private AutoCloseable mocks;

	@BeforeClass
	public void init() {
		mocks = MockitoAnnotations.openMocks(this);
		tikaContentTypeDetector = new TikaContentTypeDetector(publisher, true);
	}

	@AfterClass
	public void cleanup() throws Exception {
		mocks.close();
	}

	/**
	 * Tests MIME type detection for RAR 4.x format files.
	 *
	 * Without the junrar library, Tika can still detect RAR archives via signature detection.
	 * The MIME type returned is `application/x-rar-compressed; version=4`.
	 *
	 * This demonstrates that junrar exclusion does not prevent RAR file identification,
	 * as Tika's signature-based detection still works correctly.
	 */
	@Test(description = "Should detect RAR 4.x file MIME type without junrar library")
	public void shouldDetectRar4MimeTypeWithoutJunrar() throws IOException {
		// Given
		byte[] rarContent = createMinimalRarFile(RAR4_SIGNATURE);

		// When
		String mimeType = tikaContentTypeDetector.probeContentType(
			new ByteArrayInputStream(rarContent),
			"test.rar"
		);

		// Then
		// Actual MIME type discovered: application/x-rar-compressed; version=4
		// Tika can detect RAR files via signature even without junrar parser
		Assert.assertNotNull(mimeType, "MIME type should not be null");
		Assert.assertEquals(
			mimeType,
			"application/x-rar-compressed; version=4",
			"Expected RAR 4.x MIME type with version parameter"
		);
	}

	/**
	 * Tests MIME type detection for RAR 5.0 format files.
	 *
	 * Without the junrar library, Tika can still detect RAR 5.0 archives via signature detection.
	 * The MIME type returned is `application/x-rar-compressed; version=5`.
	 *
	 * This demonstrates that junrar exclusion does not prevent RAR 5.0 file identification,
	 * as Tika's signature-based detection still works correctly.
	 */
	@Test(description = "Should detect RAR 5.0 file MIME type without junrar library")
	public void shouldDetectRar5MimeTypeWithoutJunrar() throws IOException {
		// Given
		byte[] rarContent = createMinimalRarFile(RAR5_SIGNATURE);

		// When
		String mimeType = tikaContentTypeDetector.probeContentType(
			new ByteArrayInputStream(rarContent),
			"test.rar"
		);

		// Then
		// Actual MIME type discovered: application/x-rar-compressed; version=5
		// Tika can detect RAR 5.0 files via signature even without junrar parser
		Assert.assertNotNull(mimeType, "MIME type should not be null");
		Assert.assertEquals(
			mimeType,
			"application/x-rar-compressed; version=5",
			"Expected RAR 5.0 MIME type with version parameter"
		);
	}

	/**
	 * Creates a minimal byte array with RAR file signature.
	 * This is sufficient for MIME type detection testing.
	 */
	private byte[] createMinimalRarFile(byte[] signature) {
		// Create a byte array with the RAR signature followed by some padding
		byte[] content = new byte[signature.length + 100];
		System.arraycopy(signature, 0, content, 0, signature.length);
		return content;
	}
}
