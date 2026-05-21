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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.client.ResourceAccessException;

import com.mgmtp.a12.contentstore.client.content.ContentStorePrivateClient;
import com.mgmtp.a12.contentstore.client.content.ContentStoreTicketClient;
import com.mgmtp.a12.contentstore.client.exception.A12ClientException;
import com.mgmtp.a12.contentstore.client.exception.RestErrorDetail;
import com.mgmtp.a12.contentstore.utils.Constants;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentUrl;
import com.mgmtp.a12.dataservices.attachment.ThumbnailType;
import com.mgmtp.a12.dataservices.attachment.TypeOfTheContent;
import com.mgmtp.a12.dataservices.attachment.header.AttachmentHeaderService;
import com.mgmtp.a12.dataservices.attachment.persitence.AttachmentPersistenceResult;
import com.mgmtp.a12.dataservices.attachment.persitence.IAttachmentRepository;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.ThumbnailUtil;
import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.common.content.ContentTypeDetector;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
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
	private final AttachmentHeaderService attachmentHeaderService;
	private final ContentTypeDetector contentTypeDetector;
	private final DataServicesCoreProperties dataServicesCoreProperties;

	private static final String UNEXPECTED_ERROR_MESSAGE = "Unable to reach content store server";
	private static final String CONNECT_ERROR_MESSAGE = "Cannot connect to content store server";

	@Override public AttachmentPersistenceResult create(@NonNull String id, @NonNull InputStream is,
			@NonNull String filename, @NonNull TypeOfTheContent type) {
		return switch (type) {
			case ATTACHMENT_SECURED, ATTACHMENT_PUBLIC -> createAttachment(id, is, filename, type.getPersistentType(), null);
			case ATTACHMENT_THUMBNAIL -> createThumbnail(id, is);
		};
	}

	@Override public AttachmentPersistenceResult create(@NonNull String id, @NonNull InputStream is,
		@NonNull String filename, @NonNull TypeOfTheContent type, @NonNull String mimeType) {
		return switch (type) {
			case ATTACHMENT_SECURED, ATTACHMENT_PUBLIC -> createAttachment(id, is, filename, type.getPersistentType(), mimeType);
			case ATTACHMENT_THUMBNAIL -> createThumbnail(id, is);
		};
	}

	@Override public AttachmentPersistenceResult createAttachment(@NonNull AttachmentHeader attachmentHeader, @NonNull InputStream is) {
		return createAttachment(attachmentHeader.getAttachmentId(), is, null, TypeOfTheContent.ATTACHMENT_SECURED.getPersistentType(), null);
	}

	@Override public AttachmentPersistenceResult createAttachment(@NonNull AttachmentHeader attachmentHeader, @NonNull InputStream is, @NonNull String filename) {
		return createAttachment(attachmentHeader.getAttachmentId(), is, filename, TypeOfTheContent.ATTACHMENT_SECURED.getPersistentType(), null);
	}

	@Override public AttachmentPersistenceResult createThumbnail(@NonNull AttachmentHeader attachmentHeader, @NonNull InputStream is) {
		return createThumbnail(UUID.randomUUID().toString(), is);
	}

	@Override public Optional<AttachmentUrl> findAttachmentUrl(@NonNull String attachmentId, @NonNull String filename) {
		try {
			return Optional.ofNullable(ticketClient.requestTicket(attachmentId).getUrl()).map(url -> AttachmentUtil.createAttachmentUrl(url, filename));
		} catch (A12ClientException ex) {
			return Optional.empty();
		} catch (ResourceAccessException ex) {
			throw createServerConnectionError();
		}
	}

	@Override public Optional<AttachmentUrl> findUrl(@NonNull String id, @NonNull String filename, @NonNull TypeOfTheContent type) {
		return switch (type) {
			case ATTACHMENT_THUMBNAIL -> findPublicAttachmentUrl(id, null);
			case ATTACHMENT_PUBLIC -> findPublicAttachmentUrl(id, filename);
			default -> findSecuredAttachmentUrl(id, filename);
		};
	}

	@Override public Optional<AttachmentUrl> findThumbnailUrl(@NonNull String attachmentId, @NonNull ThumbnailType type) {
		return tryCatchClientError(() -> Optional.of(attachmentId)
			.flatMap(attachmentHeaderService::load)
			.flatMap(ah -> findThumbnailUrlFromHeader(ah, type)));
	}

	@Override public Optional<AttachmentUrl> findThumbnailUrl(@NonNull AttachmentHeader attachmentHeader, @NonNull ThumbnailType type) {
		return tryCatchClientError(() -> findThumbnailUrlFromHeader(attachmentHeader, type));
	}

	@Override public void delete(@NonNull String id) {
		tryCatchClientError(
			() -> {
				privateClient.deleteContent(id);
				return null;
			}
		);
	}

	@Override public void deleteThumbnail(String attachmentId) {
		Optional.of(attachmentId)
			.flatMap(attachmentHeaderService::load)
			.ifPresent(attachmentHeader -> {
				Optional.ofNullable(attachmentHeader.getThumbnailBigId()).ifPresent(privateClient::deleteContent);
				Optional.ofNullable(attachmentHeader.getThumbnailSmallId()).ifPresent(privateClient::deleteContent);
			});
	}

	private AttachmentPersistenceResult createAttachment(String attachmentId, InputStream is, String filename, String persistentType, String mimeType) {
		if (mimeType != null) {
			return tryCatchClientError(() -> contentStoreMapper.toAttachmentPersistenceResult(
				privateClient.uploadContent(is, attachmentId, persistentType, filename, mimeType)));
		}
		if (dataServicesCoreProperties.getAttachments().getMimeType().getProbeMimeType().isEnabled()) {
			return tryCatchClientError(() -> {
				try {
					byte[] bytes = IOUtils.toByteArray(is);
					return contentStoreMapper.toAttachmentPersistenceResult(
						privateClient.uploadContent(new ByteArrayInputStream(bytes), attachmentId,
							persistentType, filename,
							contentTypeDetector.probeContentType(new ByteArrayInputStream(bytes), filename)));
				} catch (IOException e) {
					throw new UnexpectedException(Constants.INVALID_CONTENT_INPUT_STREAM, e).withAnonymityMessage("Creation of attachment failed.");
				}
			});
		} else {
			return tryCatchClientError(() -> contentStoreMapper.toAttachmentPersistenceResult(
				privateClient.uploadContent(is, attachmentId, persistentType, filename, null)));
		}
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

	private Optional<AttachmentUrl> findThumbnailUrlFromHeader(@NotNull AttachmentHeader attachmentHeader, @NotNull ThumbnailType type) {
		if (ThumbnailType.BIG == type && StringUtils.isNotEmpty(attachmentHeader.getThumbnailBigId())) {
			return Optional.of(new AttachmentUrl(privateClient.getDownloadUrl(attachmentHeader.getThumbnailBigId()).getUrl()));
		} else if (ThumbnailType.SMALL == type && StringUtils.isNotEmpty(attachmentHeader.getThumbnailSmallId())) {
			return Optional.of(new AttachmentUrl(privateClient.getDownloadUrl(attachmentHeader.getThumbnailSmallId()).getUrl()));
		} else {
			return Optional.empty();
		}
	}
}
