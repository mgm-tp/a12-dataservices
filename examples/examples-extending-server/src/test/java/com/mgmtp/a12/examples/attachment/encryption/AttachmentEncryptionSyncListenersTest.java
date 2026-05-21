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
package com.mgmtp.a12.examples.attachment.encryption;

import com.mgmtp.a12.contentstore.content.ContentStream;
import com.mgmtp.a12.contentstore.events.ContentBeforeCreateEvent;
import com.mgmtp.a12.contentstore.events.ContentBeforeDownloadEvent;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Supplier;

public class AttachmentEncryptionSyncListenersTest {

	private AttachmentEncryptionSyncListeners listeners;

	@BeforeMethod
	public void setUp() {
		listeners = new AttachmentEncryptionSyncListeners();
	}

	@Test
	public void encryptAttachmentBeforePersisting_replacesStream_base64Encoded() throws Exception {
		String original = "plain-text";
		ContentBeforeCreateEvent event = new ContentBeforeCreateEvent(
			"contentId",
			null,
			"public",
			Optional.of(new ByteArrayInputStream(original.getBytes()))
		);

		listeners.encryptAttachmentBeforePersisting(event);

		Optional<InputStream> replaced = event.getInputStream();
		Assert.assertTrue(replaced.isPresent());
		Assert.assertEquals(
			new String(replaced.get().readAllBytes()),
			Base64.getEncoder().encodeToString(original.getBytes())
		);
	}

	@Test(expectedExceptions = InvalidInputException.class)
	public void encryptAttachmentBeforePersisting_throwsInvalidInputException_onIoError() {
		InputStream faulty = new InputStream() {
			@Override
			public int read() throws IOException {
				throw new IOException("fail");
			}
		};
		ContentBeforeCreateEvent event = new ContentBeforeCreateEvent(
			"contentId",
			"public",
			null,
			Optional.of(faulty)
			);

		listeners.encryptAttachmentBeforePersisting(event);
	}

	@Test
	public void decryptContentBeforeDownload_replacesSupplier_decodedContent() throws Exception {
		String plain = "data";
		byte[] encoded = Base64.getEncoder().encode(plain.getBytes());
		Supplier<InputStream> originalSupplier = () -> new ByteArrayInputStream(encoded);

		ContentStream contentStream = new ContentStream();
		contentStream.setContentSupplier(originalSupplier);

		ContentBeforeDownloadEvent event = new ContentBeforeDownloadEvent(
			null,
			null,
			contentStream
		);

		listeners.decryptContentBeforeDownload(event);

		Assert.assertTrue(contentStream.isReady());
		Supplier<InputStream> replacedSupplier = contentStream.getContentSupplier();
		Assert.assertNotNull(replacedSupplier);
		Assert.assertEquals(new String(replacedSupplier.get().readAllBytes()), plain);
	}

	@Test
	public void decryptContentBeforeDownload_doesNothing_supplierNull() {
		ContentStream contentStream = new ContentStream();
		contentStream.setContentSupplier(null);

		ContentBeforeDownloadEvent event = new ContentBeforeDownloadEvent(
			null,
			null,
			contentStream
		);

		listeners.decryptContentBeforeDownload(event);

		Assert.assertFalse(contentStream.isReady());
		Assert.assertNull(contentStream.getContentSupplier());
	}
}
