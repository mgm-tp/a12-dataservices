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
package com.mgmtp.a12.dataservices.attachment.persitence.internal.contentstore;

import java.io.InputStream;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;

import com.mgmtp.a12.contentstore.client.content.ContentStorePrivateClient;
import com.mgmtp.a12.contentstore.client.content.ContentStoreTicketClient;
import com.mgmtp.a12.contentstore.client.exception.A12ClientException;
import com.mgmtp.a12.contentstore.client.exception.RestErrorDetail;
import com.mgmtp.a12.dataservices.attachment.AttachmentUrl;
import com.mgmtp.a12.dataservices.attachment.TypeOfTheContent;
import com.mgmtp.a12.dataservices.attachment.persitence.AttachmentPersistenceResult;
import com.mgmtp.a12.dataservices.attachment.persitence.IAttachmentRepository;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.ThumbnailUtil;
import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.exception.ContentStoreClientException;
import com.mgmtp.a12.dataservices.utils.AttachmentConstants;
import com.mgmtp.a12.dataservices.utils.internal.AttachmentUtil;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class StandaloneContentStoreAttachmentRepository implements IAttachmentRepository {

	private final ContentStoreTicketClient ticketClient;
	private final ContentStorePrivateClient privateClient;
	private final ContentStoreMapper contentStoreMapper;

	private static final String UNEXPECTED_ERROR_MESSAGE = "Unable to reach content store server";
	private static final String CONNECT_ERROR_MESSAGE = "Cannot connect to content store server";

	@Override public AttachmentPersistenceResult create(@NonNull String id, @NonNull InputStream is,
		@NonNull String filename, @NonNull TypeOfTheContent type, String mimeType) {
		return switch (type) {
			case ATTACHMENT_SECURED, ATTACHMENT_PUBLIC -> createAttachment(id, is, filename, type.getPersistentType(), mimeType);
			case ATTACHMENT_THUMBNAIL -> createThumbnail(id, is);
		};
	}

	@Override public Optional<AttachmentUrl> findUrl(@NonNull String id, @NonNull String filename, @NonNull TypeOfTheContent type) {
		return switch (type) {
			case ATTACHMENT_THUMBNAIL -> findPublicAttachmentUrl(id, null);
			case ATTACHMENT_PUBLIC -> findPublicAttachmentUrl(id, filename);
			default -> findSecuredAttachmentUrl(id, filename);
		};
	}

	@Override public void delete(@NonNull String id) {
		tryCatchClientError(
			() -> {
				privateClient.deleteContent(id);
				return null;
			}
		);
	}

	private AttachmentPersistenceResult createAttachment(String attachmentId, InputStream is, String filename, String persistentType, String mimeType) {
		return tryCatchClientError(() -> contentStoreMapper.toAttachmentPersistenceResult(
			privateClient.uploadContent(is, attachmentId, persistentType, filename, mimeType)));
	}

	private AttachmentPersistenceResult createThumbnail(String id, InputStream is) {
		return contentStoreMapper.toAttachmentPersistenceResult(
			privateClient.uploadContent(is, id, AttachmentConstants.PUBLIC_TYPE, id, ThumbnailUtil.getImageMimeType()));
	}

	private Optional<AttachmentUrl> findSecuredAttachmentUrl(String attachmentId, String filename) {
		try {
			return Optional.ofNullable(ticketClient.requestTicket(attachmentId)
					.getUrl())
				.map(url -> AttachmentUtil.createAttachmentUrl(url, filename));
		} catch (A12ClientException ex) {
			return Optional.empty();
		} catch (ResourceAccessException ex) {
			throw createServerConnectionError();
		}
	}

	private Optional<AttachmentUrl> findPublicAttachmentUrl(String id, String filename) {
		try {
			return Optional.ofNullable(privateClient.getDownloadUrl(id))
				.map(downloadUrlResponse -> Optional.ofNullable(filename)
					.map(name -> AttachmentUtil.createAttachmentUrl(downloadUrlResponse.getUrl(), name))
					.orElse(new AttachmentUrl(downloadUrlResponse.getUrl()))
				);
		} catch (A12ClientException ex) {
			return Optional.empty();
		} catch (ResourceAccessException ex) {
			throw createServerConnectionError();
		}
	}

	private <T> T tryCatchClientError(Supplier<T> func) {
		try {
			return func.get();
		} catch (A12ClientException ex) {
			throw createClientError(ex);
		} catch (ResourceAccessException ex) {
			throw createServerConnectionError();
		}
	}

	private ContentStoreClientException createServerConnectionError() {
		log.error("Can not connect to content store");
		ContentStoreClientException exception =
			new ContentStoreClientException(CONNECT_ERROR_MESSAGE, HttpStatus.SC_SERVICE_UNAVAILABLE);
		exception.setRecoverable(isRecoverable(HttpStatus.SC_SERVICE_UNAVAILABLE));
		return exception;
	}

	private ContentStoreClientException createClientError(A12ClientException e) {

		ContentStoreClientException exception;

		if (e.getErrorDetail() instanceof RestErrorDetail restErrorDetail) {
			log.error("Content store got error with http status {} and message {}", restErrorDetail.getResponseCode(), e.getMessage());
			exception = new ContentStoreClientException(
				UNEXPECTED_ERROR_MESSAGE,
				restErrorDetail.getResponseCode()
			);
			com.mgmtp.a12.contentstore.client.localization.LocalizedEntry longMessage = e.getLongMessage();
			com.mgmtp.a12.contentstore.client.localization.LocalizedEntry shortMessage = e.getShortMessage();
			if (longMessage != null) {
				exception.setLongMessage(new LocalizedEntry(longMessage.getKey(), longMessage.getDefaultMessage()));
			}
			if (shortMessage != null) {
				exception.setShortMessage(new LocalizedEntry(shortMessage.getKey(), shortMessage.getDefaultMessage()));
			}
		} else {
			exception = new ContentStoreClientException(
				UNEXPECTED_ERROR_MESSAGE,
				HttpStatus.SC_INTERNAL_SERVER_ERROR
			);
		}

		exception.setRecoverable(isRecoverable(exception.getStatusCode()));
		return exception;
	}

	private boolean isRecoverable(int statusCode) {
		// Handle status SC_BAD_GATEWAY and SC_GATEWAY_TIMEOUT in case content store can be clustered and load balancer will be in the middle
		return statusCode == HttpStatus.SC_BAD_GATEWAY || statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE || statusCode == HttpStatus.SC_GATEWAY_TIMEOUT;
	}
}
