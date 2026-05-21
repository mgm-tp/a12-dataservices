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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.mgmtp.a12.contentstore.content.ContentStream;
import com.mgmtp.a12.contentstore.events.ContentAfterRequestEvent;
import com.mgmtp.a12.contentstore.events.ContentBeforeCreateEvent;
import com.mgmtp.a12.contentstore.events.ContentBeforeDownloadEvent;
import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Example demonstrating how to encrypt and decrypt attachment content using asynchronous event listeners.
 *
 * * Before persisting an attachment with persistent type 'private', the content is Base64 encoded (simulating encryption).
 * * After the content is requested, it is Base64 decoded (simulating decryption) and stored in a Hazelcast map for later retrieval.
 * * Before downloading an attachment with persistent type 'private', the decrypted content is retrieved from the Hazelcast map and set as the content supplier.

 * Note: This example uses Base64 encoding/decoding for demonstration purposes only. In a real-world scenario, proper encryption/decryption algorithms should be used.
 */
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.attachments.encryption.async", name = "enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
@Component public class AttachmentEncryptionAsyncListeners {

	private final HazelcastInstance hazelcastInstance;
	private IMap<String, byte[]> contentStreamDataMap;

	@PostConstruct
	void init() {
		contentStreamDataMap = hazelcastInstance.getMap("data");
	}

	/**
	 * Encodes attachment content using Base64 prior to persistence for attachments with persistent type `private`.
	 * The encoded stream replaces the original input stream to simulate encryption.
	 *
	 * @param contentBeforeCreateEvent event containing the input stream supplier; never null.
	 */
	@CommonDataServicesEventListener(condition = "#contentBeforeCreateEvent.persistentType.equals('private')")
	public void encryptAttachmentBeforePersisting(ContentBeforeCreateEvent contentBeforeCreateEvent) {
		contentBeforeCreateEvent.getInputStream()
			.ifPresent(inputStream -> {
				try (inputStream) {
					Optional<InputStream> encryptedContentOptional = Optional.of(
						new ByteArrayInputStream(Base64.getEncoder().encode(inputStream.readAllBytes()))
					);
					contentBeforeCreateEvent.setInputStream(encryptedContentOptional);
				} catch (IOException e) {
					throw new InvalidInputException("Could not encode attachment content", e);
				}
			});
	}

	/**
	 * Asynchronously decodes content after it has been requested and stores decrypted bytes in a Hazelcast map.
	 *
	 * @param contentAfterRequestEvent event providing the content stream and identifiers; never null.
	 */
	@Async
	@CommonDataServicesEventListener()
	public void decryptWhenContentAfterRequestEvent(ContentAfterRequestEvent contentAfterRequestEvent) {
		String contentId = contentAfterRequestEvent.getTicketInfoEntity().getContentId();
		try (InputStream is = contentAfterRequestEvent.getContentStream().getContentSupplier().get()){
			storeDecryptedContent(
				contentId,
				Base64.getDecoder().decode(is.readAllBytes())
			);
		} catch (IOException e) {
			throw new InvalidInputException("Could not decode attachment content", e);
		}
	}

	/**
	 * Asynchronously waits for decrypted content to become available, then sets a supplier for download.
	 * Marks the content stream as ready regardless of availability to proceed with the request flow.
	 *
	 * @param contentBeforeDownloadEvent event holding the content stream and content identifier; never null.
	 */
	@Async
	@CommonDataServicesEventListener(condition = "#contentBeforeDownloadEvent.persistentType.equals('private')")
	public void decryptContentBeforeDownload(ContentBeforeDownloadEvent contentBeforeDownloadEvent) {
		ContentStream contentStream = contentBeforeDownloadEvent.getContentStream();
		String contentId = contentBeforeDownloadEvent.getContentId();

		int secondsToWait = 60;
		long endWaitTime = System.currentTimeMillis() + secondsToWait * 1000;
		boolean isConditionMet = false;
		while (System.currentTimeMillis() < endWaitTime) {
			isConditionMet = getDecryptedContent(contentId) != null;
			if (isConditionMet) {
				break;
			} else {
				try {
					log.info("Waiting content decrypting...");
					Thread.sleep(200);
				} catch (InterruptedException e) {
					log.error("Can not get decrypted content.");
				}
			}
		}
		if (isConditionMet) {
			contentStream.setContentSupplier(() -> new ByteArrayInputStream(getDecryptedContent(contentId)));
		}
		contentStream.setReady();
	}

	private void storeDecryptedContent(String contentId, byte[] arrays) {
		contentStreamDataMap.put(contentId, arrays);
	}

	private byte[] getDecryptedContent(String contentId) {
		return contentStreamDataMap.get(contentId);
	}

}
