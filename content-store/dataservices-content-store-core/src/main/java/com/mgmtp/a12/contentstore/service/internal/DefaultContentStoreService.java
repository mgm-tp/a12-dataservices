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
package com.mgmtp.a12.contentstore.service.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.mgmtp.a12.contentstore.ContentPersistenceResult;
import com.mgmtp.a12.contentstore.configuration.ContentStoreProperties;
import com.mgmtp.a12.contentstore.content.ContentStream;
import com.mgmtp.a12.contentstore.content.internal.ContentService;
import com.mgmtp.a12.contentstore.content.internal.ContentValidator;
import com.mgmtp.a12.contentstore.content.internal.jpa.entity.ContentHeaderEntity;
import com.mgmtp.a12.contentstore.events.ContentAfterCreateEvent;
import com.mgmtp.a12.contentstore.events.ContentAfterDownloadEvent;
import com.mgmtp.a12.contentstore.events.ContentAfterRequestEvent;
import com.mgmtp.a12.contentstore.events.ContentBeforeCreateEvent;
import com.mgmtp.a12.contentstore.events.ContentBeforeDownloadEvent;
import com.mgmtp.a12.contentstore.exception.ExceptionKeys;
import com.mgmtp.a12.contentstore.exception.InvalidTypeException;
import com.mgmtp.a12.contentstore.exception.TicketNotFoundException;
import com.mgmtp.a12.contentstore.exception.TimeoutException;
import com.mgmtp.a12.contentstore.service.ContentStoreService;
import com.mgmtp.a12.contentstore.ticket.internal.TicketService;
import com.mgmtp.a12.contentstore.ticket.internal.TicketValidator;
import com.mgmtp.a12.contentstore.ticket.internal.jpa.entity.TicketInfoEntity;
import com.mgmtp.a12.contentstore.utils.Constants;
import com.mgmtp.a12.contentstore.utils.internal.UrlUtils;
import com.mgmtp.a12.dataservices.common.content.ContentTypeDetector;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.common.quantity.internal.QuantityParsers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.contentstore.utils.Constants.ACCEPTABLE_PERSISTENT_TYPES;
import static com.mgmtp.a12.contentstore.utils.Constants.PERSISTENT_TYPE_PRIVATE;
import static com.mgmtp.a12.contentstore.utils.Constants.PERSISTENT_TYPE_PUBLIC;

@Slf4j
@RequiredArgsConstructor
@Service public class DefaultContentStoreService implements ContentStoreService {

	private final TicketService ticketService;
	private final ContentService contentService;
	private final TicketValidator ticketValidator;
	private final ContentValidator contentValidator;
	private final ContentStoreProperties contentStoreProperties;
	private final ApplicationEventPublisher eventPublisher;
	private final ContentTypeDetector contentTypeDetector;

	@Override public String requestContentUrl(@NonNull String contentId, long duration) {
		log.debug("Requesting download URL for content with id {}, available for {} seconds", contentId, duration);
		TicketInfoEntity ticketInfoEntity = contentService.findHeaderById(contentId)
			.filter(c -> Constants.PERSISTENT_TYPE_PRIVATE.equalsIgnoreCase(c.getPersistentType()))
				.flatMap(contentEntity -> contentService.findBinaryContentById(contentId))
				.map(content -> publishContentAfterRequestEvent(() -> new ByteArrayInputStream(content),
				ticketService.createTicket(contentId, getTicketDuration(duration)),
				PERSISTENT_TYPE_PRIVATE)
			)
			.orElseThrow(() -> new NotFoundException(ExceptionKeys.CONTENT_NOT_FOUND_ERROR_KEY,
			Constants.CANNOT_FIND_CONTENT_BY_ID_PATTERN.formatted(contentId)));
		return UrlUtils.renderContentUrl(contentStoreProperties, ticketInfoEntity.getTicketId());
	}

	@Override public Optional<String> findPublicContentUrl(@NonNull String contentId) {
		log.debug("Requesting download URL for public content with id {}", contentId);
		return Optional.of(contentId)
			.filter(id -> exists(id, PERSISTENT_TYPE_PUBLIC))
			.map(id -> UrlUtils.renderContentUrl(contentStoreProperties, contentId));
	}

	@Override public ContentStream getContent(@NonNull String id) {
		log.debug("Requesting download stream for content with id {}", id);
		ContentStream.ContentStreamBuilder builder = ContentStream.builder();
		// Found ticket by id, return InputStream for private content
		// Ticket is not found, try to return InputStream for public content
		return ticketService.findTicket(id)
			.map(ticket -> getPrivateContent(ticket, builder))
			.orElseGet(() -> getPublicContent(id, builder));
	}

	@Override public boolean exists(@NonNull String id, String persistentType) {
		return contentService.exists(id, persistentType);
	}

	@Override public ContentPersistenceResult saveContent(@NonNull String contentId, String persistentType, @NonNull InputStream inputStream, String filename) {
		return internalSaveContent(contentId, persistentType, inputStream, filename, null);
	}

	@Override public ContentPersistenceResult saveContent(@NonNull String contentId, String persistentType, @NonNull InputStream inputStream, String filename, String mimeType) {
		return internalSaveContent(contentId, persistentType, inputStream, filename, mimeType);
	}

	@Override public void deleteById(@NonNull String contentId) {
		log.debug("Deleting content by id {}", contentId);
		contentService.deleteContentById(contentId);
	}

	private ContentStream getPublicContent(String id, ContentStream.ContentStreamBuilder builder) {
		ContentHeaderEntity headerEntity = contentService.findByContentIdAndPersistentType(id, PERSISTENT_TYPE_PUBLIC)
			.orElseThrow(() -> new NotFoundException(ExceptionKeys.CONTENT_NOT_FOUND_ERROR_KEY,
			Constants.CANNOT_FIND_PUBLIC_CONTENT_BY_CONTENT_ID_PATTERN.formatted(id)));
		return beforeDownloadEvent(builder.isPublic(true), headerEntity);

	}

	private ContentStream getPrivateContent(TicketInfoEntity ticket, ContentStream.ContentStreamBuilder builder) {
		if (ticketValidator.isAvailableTicket(ticket)) {
			ContentHeaderEntity headerEntity = contentService.findByContentIdAndPersistentType(ticket.getContentId(), PERSISTENT_TYPE_PRIVATE)
				.orElseThrow(() -> new NotFoundException(ExceptionKeys.CONTENT_NOT_FOUND_ERROR_KEY,
				Constants.CONTENT_BY_TICKET_ID_NOT_EXIST_IN_STORAGE_SYSTEM_PATTERN.formatted(ticket.getContentId())));

			ticket.setDownloaded(true);
			ticketService.update(ticket);

			ContentStream contentStream = beforeDownloadEvent(builder, headerEntity);
			eventPublisher.publishEvent(new ContentAfterDownloadEvent(ticket.getContentId()));

			return contentStream;
		} else {
			throw new TicketNotFoundException(ExceptionKeys.TICKET_UNAVAILABLE_ERROR_KEY,
				Constants.TICKET_WITH_ID_IS_NOT_AVAILABLE_PATTERN.formatted(ticket.getTicketId()));
		}
	}

	private ContentStream beforeDownloadEvent(ContentStream.ContentStreamBuilder builder, ContentHeaderEntity header) {
		ContentStream contentStream = builder
			.contentType(header.getContentType())
			.contentSupplier(new ContentSupplier(contentService.findBinaryContentById(header.getId()).orElse(null)))
			.build();
		ContentBeforeDownloadEvent contentBeforeDownloadEvent =
			new ContentBeforeDownloadEvent(header.getId(), header.getPersistentType(), contentStream);
		eventPublisher.publishEvent(contentBeforeDownloadEvent);
		contentStream = contentBeforeDownloadEvent.getContentStream();
		try {
			contentStream.awaitReady(contentStoreProperties.getContentWaitReadyTimeout());
			return contentStream;
		} catch (InterruptedException e) {
			throw new UnexpectedException(ExceptionKeys.UNEXPECTED_ERROR_KEY, e.getMessage()).withAnonymityMessage("Waiting for content interrupted.");
		} catch (java.util.concurrent.TimeoutException e) {
			throw new TimeoutException("Content not retriever in timeout");
		}
	}

	private InputStream publishContentBeforeCreateEvent(String contentId, String persistentType, String contentType, InputStream inputStream) {
		ContentBeforeCreateEvent contentBeforeCreateEvent =
			new ContentBeforeCreateEvent(contentId, persistentType, contentType, Optional.of(inputStream));
		eventPublisher.publishEvent(contentBeforeCreateEvent);
		return contentBeforeCreateEvent.getInputStream().orElse(inputStream);
	}

	private TicketInfoEntity publishContentAfterRequestEvent(Supplier<InputStream> contentSupplier, TicketInfoEntity ticket, String contentType) {
		ContentStream contentStream = ContentStream.builder()
			.contentType(contentType)
			.contentSupplier(contentSupplier)
			.build();
		contentStream.setReady();
		ContentAfterRequestEvent contentAfterRequestEvent = new ContentAfterRequestEvent(ticket, contentStream);
		eventPublisher.publishEvent(contentAfterRequestEvent);
		return ticket;
	}

	private long getTicketDuration(long duration) {
		return duration <= 0 ? QuantityParsers.parseTimeQuantity(contentStoreProperties.getTicketDuration()) : duration;
	}

	private ContentPersistenceResult internalSaveContent(String contentId, String persistentType, InputStream inputStream, String filename,
		String externalMimeType) {
		log.debug("Persisting {} content with id {}, filename {} and externalMimeType {}", persistentType, contentId, filename, externalMimeType);
		if (StringUtils.isBlank(contentId)) {
			throw new InvalidInputException(ExceptionKeys.INVALID_INPUT_ERROR_KEY, Constants.INVALID_CONTENT_ID_ERROR);
		}

		// TODO A12S-4250 Analyze an option to incude other persistent types in the future.
		if (!Arrays.asList(ACCEPTABLE_PERSISTENT_TYPES).contains(persistentType)) {
			throw new InvalidTypeException(ExceptionKeys.INVALID_PERSISTENT_TYPE_ERROR_KEY,
				Constants.INVALID_TYPE_ERROR_PATTEN.formatted(persistentType)
			);
		}

		try {
			byte[] bytes = IOUtils.toByteArray(inputStream);
			String contentType = getContentType(filename, externalMimeType, bytes);

			InputStream contentInputStream = publishContentBeforeCreateEvent(contentId, persistentType, contentType, new ByteArrayInputStream(bytes));
			// we keep original size of file because after event publishing, content size may be changed.
			long fileSize = contentValidator.getSizeAndValidate(bytes);
			ContentPersistenceResult result = contentService.save(contentId, persistentType, contentType, contentInputStream);
			result.setSize(fileSize);

			eventPublisher.publishEvent(new ContentAfterCreateEvent(contentId, persistentType, result.getSize(), result.getContentType()));
			return result;
		} catch (IOException e) {
			contentService.deleteContentById(contentId);
			throw new RuntimeException(e);
		} catch (Exception e) {
			contentService.deleteContentById(contentId);
			throw e;
		}
	}

	private String getContentType(String filename, String externalMimeType, byte[] bytes) throws IOException {
		return contentStoreProperties.getServer().getApi().getMimeType().getTrustExternalMimeType().isEnabled() ?
			Optional.ofNullable(externalMimeType)
				.orElseThrow(() -> new InvalidInputException(ExceptionKeys.INVALID_INPUT_ERROR_KEY, Constants.CONTENT_MIME_TYPE_MANDATORY_ERROR)) :
			contentTypeDetector.probeContentType(new ByteArrayInputStream(bytes), filename);
	}

	@RequiredArgsConstructor
	static class ContentSupplier implements Supplier<InputStream> {

		private final byte[] contentBinary;

		@Override public InputStream get() {
			return new ByteArrayInputStream(contentBinary);
		}
	}
}
