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

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.mgmtp.a12.contentstore.content.ContentStream;
import com.mgmtp.a12.contentstore.events.ContentAfterRequestEvent;
import com.mgmtp.a12.contentstore.events.ContentBeforeCreateEvent;
import com.mgmtp.a12.contentstore.events.ContentBeforeDownloadEvent;
import com.mgmtp.a12.contentstore.ticket.internal.jpa.entity.TicketInfoEntity;

import org.mockito.ArgumentCaptor;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AttachmentEncryptionAsyncListenersTest {

	private HazelcastInstance hazelcastInstance;
	private IMap<String, byte[]> map;
	private AttachmentEncryptionAsyncListeners listeners;

	@BeforeMethod
	public void setUp() {
		hazelcastInstance = mock(HazelcastInstance.class);
		map = mock(IMap.class);
		doReturn(map).when(hazelcastInstance).getMap("data");
		listeners = new AttachmentEncryptionAsyncListeners(hazelcastInstance);
		listeners.init();
	}

	@Test
	public void encryptAttachmentBeforePersisting_encodesBase64_replacesInputStream() throws Exception {
		String original = "secret-bytes";
		ContentBeforeCreateEvent event = mock(ContentBeforeCreateEvent.class);
		when(event.getInputStream()).thenReturn(Optional.of(new ByteArrayInputStream(original.getBytes(StandardCharsets.UTF_8))));

		listeners.encryptAttachmentBeforePersisting(event);

		ArgumentCaptor<Optional<InputStream>> captor = ArgumentCaptor.forClass(Optional.class);
		verify(event, times(1)).setInputStream(captor.capture());

		Optional<InputStream> replaced = captor.getValue();
		Assert.assertTrue(replaced.isPresent());
		Assert.assertEquals(
			new String(replaced.get().readAllBytes(), StandardCharsets.UTF_8),
			Base64.getEncoder().encodeToString(original.getBytes(StandardCharsets.UTF_8))
		);
	}

	@Test
	public void decryptWhenContentAfterRequestEvent_decodesAndStores_success() throws Exception {
		String contentId = "content-1";
		String plain = "hello";
		byte[] encoded = Base64.getEncoder().encode(plain.getBytes(StandardCharsets.UTF_8));

		// Real ContentStream
		ContentStream contentStream = ContentStream.builder()
			.contentSupplier(() -> new ByteArrayInputStream(encoded))
			.build();

		// Real TicketInfoEntity - if it has a builder/constructor with contentId
		TicketInfoEntity ticketInfo = mock(TicketInfoEntity.class);
		when(ticketInfo.getContentId()).thenReturn(contentId);

		// Real ContentAfterRequestEvent
		ContentAfterRequestEvent event = new ContentAfterRequestEvent(ticketInfo, contentStream);

		listeners.decryptWhenContentAfterRequestEvent(event);

		ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
		verify(map, times(1)).put(eq(contentId), bytesCaptor.capture());
		Assert.assertEquals(new String(bytesCaptor.getValue(), StandardCharsets.UTF_8), plain);
	}

	@Test
	public void decryptContentBeforeDownload_setsSupplierAndMarksReady_contentAvailable() throws Exception {
		String contentId = "content-2";
		String plain = "world";
		byte[] decoded = plain.getBytes(StandardCharsets.UTF_8);

		when(map.get(contentId)).thenReturn(decoded);

		// Real ContentStream
		ContentStream contentStream = ContentStream.builder().build();

		// Real ContentBeforeDownloadEvent
		ContentBeforeDownloadEvent event = new ContentBeforeDownloadEvent(contentId, "public",contentStream);

		listeners.decryptContentBeforeDownload(event);

		Assert.assertTrue(contentStream.isReady());
		Assert.assertNotNull(contentStream.getContentSupplier());
		Assert.assertEquals(
			new String(contentStream.getContentSupplier().get().readAllBytes(), StandardCharsets.UTF_8),
			plain
		);
	}

	@Test
	public void decryptContentBeforeDownload_marksReady_onlyWhenContentNotAvailable() {
		String contentId = "content-3";
		when(map.get(contentId)).thenReturn(null);

		// Real ContentStream
		ContentStream contentStream = ContentStream.builder().build();

		// Real ContentBeforeDownloadEvent
		ContentBeforeDownloadEvent event = new ContentBeforeDownloadEvent(contentId, "private", contentStream);

		listeners.decryptContentBeforeDownload(event);

		Assert.assertNull(contentStream.getContentSupplier());
		Assert.assertTrue(contentStream.isReady());
	}

}
