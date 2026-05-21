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
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.contentstore.content.ContentStream;
import com.mgmtp.a12.contentstore.events.ContentBeforeCreateEvent;
import com.mgmtp.a12.contentstore.events.ContentBeforeDownloadEvent;
import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;

/**
 * Example demonstrating how to encrypt and decrypt attachment content using synchronous event listeners.
 *
 * * Before persisting an attachment with persistent type 'public', the content is Base64 encoded (simulating encryption).
 * * Before downloading an attachment with persistent type 'public', the content is Base64 decoded (simulating decryption).
 *
 * Note: This example uses Base64 encoding/decoding for demonstration purposes only. In a real-world scenario, proper encryption/decryption algorithms should be used.
 */
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.attachments.encryption.sync", name = "enabled", havingValue = "true")
@Component public class AttachmentEncryptionSyncListeners {

	/**
	 * Encodes attachment content using Base64 prior to persistence for attachments with persistent type `public`.
	 * The encoded stream replaces the original input stream to simulate encryption.
	 *
	 * @param contentBeforeCreateEvent event containing the input stream supplier; never null.
	 */
	@CommonDataServicesEventListener(condition = "#contentBeforeCreateEvent.persistentType.equals('public')")
	public void encryptAttachmentBeforePersisting(ContentBeforeCreateEvent contentBeforeCreateEvent) {
		Optional<InputStream> inputStreamOptional = contentBeforeCreateEvent.getInputStream();
		if (inputStreamOptional.isPresent()) {
			try (InputStream is = inputStreamOptional.get()){
				Optional<InputStream> encryptedContentOptional = Optional.of(
					new ByteArrayInputStream(Base64.getEncoder().encode(IOUtils.toByteArray(is)))
				);
				contentBeforeCreateEvent.setInputStream(encryptedContentOptional);
			} catch (IOException e) {
				throw new InvalidInputException("Could not encode attachment content", e);
			}
		}
	}

	/**
	 * Decodes Base64-encoded content before download for attachments with persistent type `public`.
	 * The content supplier is replaced to provide decoded bytes and the stream is marked ready.
	 *
	 * @param contentBeforeDownloadEvent event holding the content stream to be decoded; never null.
	 */
	@CommonDataServicesEventListener(condition = "#contentBeforeDownloadEvent.persistentType.equals('public')")
	public void decryptContentBeforeDownload(ContentBeforeDownloadEvent contentBeforeDownloadEvent) {
		ContentStream contentStream = contentBeforeDownloadEvent.getContentStream();
		Supplier<? extends InputStream> contentSupplier = contentStream.getContentSupplier();
		if (contentSupplier != null) {
			contentStream.setReady();
			contentStream.setContentSupplier(() -> {
				try (InputStream is = contentSupplier.get()){
					return new ByteArrayInputStream(Base64.getDecoder().decode(IOUtils.toByteArray(is)));
				} catch (IOException e) {
					throw new InvalidInputException("Could not decode attachment content", e);
				}
			});
		}
	}
}
