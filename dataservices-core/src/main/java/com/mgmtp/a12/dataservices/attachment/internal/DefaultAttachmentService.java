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
package com.mgmtp.a12.dataservices.attachment.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.htmlunit.util.MimeType;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import com.mgmtp.a12.contentstore.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.attachment.AttachmentAnnotation;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentReference;
import com.mgmtp.a12.dataservices.attachment.AttachmentService;
import com.mgmtp.a12.dataservices.attachment.AttachmentUrl;
import com.mgmtp.a12.dataservices.attachment.DataServicesAttachment;
import com.mgmtp.a12.dataservices.attachment.DataServicesAttachment.DataServicesAttachmentBuilder;
import com.mgmtp.a12.dataservices.attachment.DataServicesThumbnail;
import com.mgmtp.a12.dataservices.attachment.ThumbnailType;
import com.mgmtp.a12.dataservices.attachment.TypeOfTheContent;
import com.mgmtp.a12.dataservices.attachment.events.AttachmentAfterCreateEvent;
import com.mgmtp.a12.dataservices.attachment.events.AttachmentAfterDeleteEvent;
import com.mgmtp.a12.dataservices.attachment.events.AttachmentBeforeCreateEvent;
import com.mgmtp.a12.dataservices.attachment.events.AttachmentBeforeDeleteEvent;
import com.mgmtp.a12.dataservices.attachment.events.AttachmentThumbnailAfterSaveEvent;
import com.mgmtp.a12.dataservices.attachment.events.AttachmentThumbnailBeforeSaveEvent;
import com.mgmtp.a12.dataservices.attachment.exception.ThumbnailConversionException;
import com.mgmtp.a12.dataservices.attachment.header.AttachmentHeaderService;
import com.mgmtp.a12.dataservices.attachment.persitence.AttachmentPersistenceResult;
import com.mgmtp.a12.dataservices.attachment.persitence.IAttachmentRepository;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.ThumbnailUtil;
import com.mgmtp.a12.dataservices.authorization.AttachmentPermissionEvaluator;
import com.mgmtp.a12.dataservices.common.content.ContentTypeDetector;
import com.mgmtp.a12.dataservices.authorization.AuthConstants;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository;
import com.mgmtp.a12.dataservices.internal.DocumentationDiagram;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.QueryService;
import com.mgmtp.a12.dataservices.utils.internal.DocumentUtils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

import io.github.resilience4j.retry.RetryRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ATTACHMENT_NOT_FOUND_ERROR_KEY;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ATTACHMENT_THUMBNAIL_CONVERSION_ERROR_KEY;

@DocumentationDiagram
@RequiredArgsConstructor
@Slf4j
public class DefaultAttachmentService implements AttachmentService {

	private final IAttachmentRepository attachmentRepository;
	private final AttachmentHeaderService attachmentHeaderService;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final ApplicationEventPublisher eventPublisher;
	private final List<IDocumentRepository> documentRepositories;
	private final AttachmentPermissionEvaluator attachmentPermissionEvaluator;
	private final ModelPermissionEvaluator<IDocumentModel> modelPermissionEvaluator;
	private final RetryRegistry attachmentRetryRegistry;
	private final ThumbnailUrlGenerator thumbnailUrlGenerator;
	private final QueryService queryService;
	private final ContentTypeDetector contentTypeDetector;

	@Override
	public AttachmentHeader createAttachment(@NonNull InputStream is, @NonNull String filename, @NonNull String documentModelName, @NonNull String pathToField,
		@NonNull List<AttachmentAnnotation> annotations) {
		AttachmentHeader attachmentHeader = AttachmentHelper.prepareAttachmentHeader(filename, annotations);
		return internalCreateAttachment(attachmentHeader, is, documentModelName);
	}

	// TODO A12S-6792: Shouldn't we remove the whole SME Workspace support from core?
	/**
	 * Creates a new attachment with the given ID, input stream, filename, and annotations.
	 * The content of the attachment will be treated as secured. This method is only to support SME Workpace handling.
	 *
	 * @param attachmentId The unique identifier for the attachment. Cannot be null.
	 * @param is The input stream containing the attachment data. Cannot be null.
	 * @param filename The original filename of the attachment. Cannot be null.
	 * @param annotations A list of annotations associated with the attachment. Cannot be null.
	 * @return The created {@link AttachmentHeader} containing metadata about the attachment.
	 * @throws NullPointerException if any of the input parameters (`attachmentId`, `is`, `filename`, or `annotations`) are null.
	 */
	public AttachmentHeader createSecuredAttachment(@NonNull String attachmentId, @NonNull InputStream is, @NonNull String filename,
		@NonNull List<AttachmentAnnotation> annotations) {
		AttachmentHeader attachmentHeader = AttachmentHelper.prepareAttachmentHeader(attachmentId, filename, annotations);
		return internalCreateAttachment(attachmentHeader, is, TypeOfTheContent.ATTACHMENT_SECURED);
	}

	@Override public Optional<AttachmentUrl> findThumbnailUrl(@NonNull String attachmentId, @NonNull ThumbnailType type) {
		return attachmentHeaderService.load(attachmentId)
			.flatMap(header -> findThumbnailUrl(header, type));
	}

	@Override public Optional<AttachmentUrl> findThumbnailUrl(@NonNull AttachmentHeader header, @NonNull ThumbnailType type) {
		StopWatch stopWatch = StopWatch.createStarted();
		if (hasThumbnailOfType(type, header)) {
			return thumbnailUrlGenerator.generateThumbnailUrl(header, type)
				.map(url -> logAndReturn(new AttachmentUrl(url), "for attachment [{}] generated", header.getAttachmentId(), type, stopWatch))
				.or(() -> getThumbnailFromHeader(header, type, stopWatch));
		} else {
			return logAndReturn(Optional.empty(), "not present in attachment [{}]", header.getAttachmentId(), type, stopWatch);
		}
	}

	@Override public Optional<AttachmentUrl> findAttachmentUrl(@NonNull String attachmentId, @NonNull DocumentReference documentReference) {
		checkPermissionsForLoadAttachment(documentReference, attachmentId);

		return attachmentHeaderService.load(attachmentId)
			.filter(ah -> matchDocument(AttachmentReference.fromDocRef(documentReference), ah))
			.flatMap(attachmentHeader -> attachmentRepository.findUrl(attachmentHeader.getAttachmentId(),
				attachmentHeader.getFilename(), attachmentHeader.getTypeOfTheContent()));
	}

	private void rollbackContentsInHeader(AttachmentHeader ah) {
		Optional.ofNullable(ah.getThumbnailBigId())
			.ifPresent(this::rollbackContent);
		Optional.ofNullable(ah.getThumbnailSmallId())
			.ifPresent(this::rollbackContent);
		this.rollbackContent(ah.getAttachmentId());
	}

	private void rollbackContent(String contentId) {
		try {
			attachmentRetryRegistry.retry(contentId)
				.executeRunnable(() -> attachmentRepository.delete(contentId));
		} catch (Exception e) {
			// The original exception will be thrown after retry reaches max attempts with exception, in this case cannot do anything further
			// Just log this exception, so that un-deleted content will be handled manually
			log.error("Error occurs while trying to rollback content with id {}", contentId);
		}
	}

	private static void saveContentInputStreamIntoMemory(DataServicesAttachment attachment) {
		try (InputStream inputStream = attachment.getContent().get()) {
			byte[] buffer = IOUtils.toByteArray(inputStream);
			attachment.setContent(() -> new ByteArrayInputStream(buffer));
		} catch (IOException e) {
			throw new UnexpectedException("Error during handle attachment saving", e);
		}
	}

	public void delete(@NonNull AttachmentHeader attachmentHeader) {
		StopWatch stopWatch = StopWatch.createStarted();
		AttachmentBeforeDeleteEvent beforeDeleteEvent = new AttachmentBeforeDeleteEvent(attachmentHeader);
		eventPublisher.publishEvent(beforeDeleteEvent);

		String attachmentId = attachmentHeader.getAttachmentId();

		attachmentRepository.delete(attachmentId);
		if (hasThumbnailOfType(ThumbnailType.BIG, attachmentHeader)) {
			attachmentRepository.delete(attachmentHeader.getThumbnailBigId());
		}
		if (hasThumbnailOfType(ThumbnailType.SMALL, attachmentHeader)) {
			attachmentRepository.delete(attachmentHeader.getThumbnailSmallId());
		}
		attachmentHeaderService.delete(attachmentId);
		eventPublisher.publishEvent(new AttachmentAfterDeleteEvent(attachmentHeader));
		log.debug("Attachment [{}] deleted in [{}] ms", attachmentHeader.getAttachmentId(), stopWatch.getTime(TimeUnit.MILLISECONDS));
	}

	@NonNull private static <T> T logAndReturn(T attachmentUrl, String message, String attachmentId, ThumbnailType type, StopWatch stopWatch) {
		log.debug("Thumbnail url of type [{}] %s in [{}] ms".formatted(message), type, attachmentId, stopWatch.getTime());
		return attachmentUrl;
	}

	private boolean hasThumbnailOfType(ThumbnailType type, AttachmentHeader ah) {
		return (ThumbnailType.BIG == type && StringUtils.isNotEmpty(ah.getThumbnailBigId())) ||
			(ThumbnailType.SMALL == type && StringUtils.isNotEmpty(ah.getThumbnailSmallId()));
	}

	private void checkPermissionsForLoadAttachment(DocumentReference documentReference, String attachmentId) {

		documentRepositories.stream()
			.map(dr -> dr.findByDocumentReference(documentReference))
			.flatMap(Optional::stream)
			.findAny()
			.ifPresentOrElse(doc -> {
					modelPermissionEvaluator.checkModelReadPermission(doc.getMetadata().getDocRef().getDocumentModelName());
					QueryPage<Object> queryResult = queryService.query(DocumentUtils.buildQueryLoadDocRefOnly(doc.getMetadata().getDocRef()), null);
					if (CollectionUtils.isEmpty(queryResult.getContent())) {
						throw new AccessDeniedException(AuthConstants.ACCESS_DENIED);
					}
				},
				() -> {
					throw new NotFoundException(ATTACHMENT_NOT_FOUND_ERROR_KEY, "No URL from attachmentId %s could be found.".formatted(attachmentId));
				});
	}

	private void handleThumbnails(DataServicesAttachment attachment, String contentType) {
		AttachmentThumbnailBeforeSaveEvent attachmentThumbnailBeforeSaveEvent = new AttachmentThumbnailBeforeSaveEvent(attachment);
		eventPublisher.publishEvent(attachmentThumbnailBeforeSaveEvent);

		handleThumbnail(attachment, attachmentThumbnailBeforeSaveEvent.getThumbnailBig(contentType), ThumbnailType.BIG, null);

		if (StringUtils.isBlank(attachment.getHeader().getThumbnailBigId()) && attachmentThumbnailBeforeSaveEvent.getThumbnailSmall(contentType).isEmpty()) {
			// when we can not create thumbnail big and there is no custom small thumbnail
			// we don't need to create thumbnail small.
			return;
		}

		handleThumbnail(attachment, attachmentThumbnailBeforeSaveEvent.getThumbnailSmall(contentType), ThumbnailType.SMALL,
			attachment.getThumbnails().stream().filter(
					thumbnail -> thumbnail.getType().equals(ThumbnailType.BIG)
				)
				.findFirst()
				.map(DataServicesThumbnail::getContent)
				.orElse(null));
	}

	private void handleThumbnail(DataServicesAttachment attachment, Optional<DataServicesThumbnail> thumbnailFromEvent, ThumbnailType type,
		Supplier<? extends InputStream> inputStreamSupplier) {
		thumbnailFromEvent
			.or(() -> createThumbnailFromAttachment(inputStreamSupplier != null ? inputStreamSupplier : attachment.getContent(), type))
			.ifPresent(t -> persistThumbnail(attachment, type, t));
	}

	private void persistThumbnail(DataServicesAttachment attachment, ThumbnailType type, DataServicesThumbnail t) {
		try (InputStream thumbnailStream = t.getContent().get()) {
			String thumbnailId = UUID.randomUUID().toString();
			AttachmentPersistenceResult result = attachmentRepository.create(thumbnailId, thumbnailStream,
				thumbnailId, TypeOfTheContent.ATTACHMENT_THUMBNAIL, MimeType.IMAGE_PNG);
			if (ThumbnailType.BIG == type) {
				attachment.getHeader().setThumbnailBigId(result.getAttachmentId());
			} else {
				attachment.getHeader().setThumbnailSmallId(result.getAttachmentId());
			}

			eventPublisher.publishEvent(new AttachmentThumbnailAfterSaveEvent(Optional.of(t)));

			attachment.getThumbnails().add(t);
		} catch (IOException e) {
			throw new UnexpectedException(ExceptionKeys.UNEXPECTED_ERROR_KEY, e.getMessage()).withAnonymityMessage("Persist thumbnail failed.");
		}
	}

	private Optional<DataServicesThumbnail> createThumbnailFromAttachment(Supplier<? extends InputStream> inputStreamSupplier, ThumbnailType thumbnailType) {
		try (InputStream inputStream = inputStreamSupplier.get()) {
			return ThumbnailUtil.convertToDSThumbnail(inputStream, thumbnailType, getThumbnailSize(thumbnailType),
				dataServicesCoreProperties.getAttachments().getThumbnail());
		} catch (IOException e) {
			throw new ThumbnailConversionException(ATTACHMENT_THUMBNAIL_CONVERSION_ERROR_KEY, e.getMessage(),
				BaseException.MessagePriority.LOW, e).withAnonymityMessage("Creation of thumbnail failed.");
		}
	}

	private DataServicesAttachment beforeCreate(AttachmentHeader header, InputStream content) {
		DataServicesAttachmentBuilder attachmentBuilder = DataServicesAttachment.builder()
			.header(header)
			.content(() -> content);
		AttachmentBeforeCreateEvent beforeCreateEvent = new AttachmentBeforeCreateEvent(attachmentBuilder.build());
		eventPublisher.publishEvent(beforeCreateEvent);
		return beforeCreateEvent.getAttachment();
	}

	private int getThumbnailSize(ThumbnailType type) {
		return switch (type) {
			case BIG -> dataServicesCoreProperties.getAttachments().getThumbnail().getSizeBig();
			case SMALL -> dataServicesCoreProperties.getAttachments().getThumbnail().getSizeSmall();
		};
	}

	private static boolean matchDocument(AttachmentReference<?> attachmentReference, AttachmentHeader ah) {
		return ah.getReferences().contains(attachmentReference);
	}

	private DataServicesAttachment afterCreate(DataServicesAttachment attachment) {
		AttachmentAfterCreateEvent afterCreateEvent = new AttachmentAfterCreateEvent(attachment);
		eventPublisher.publishEvent(afterCreateEvent);
		return afterCreateEvent.getDataServicesAttachment();
	}

	private Optional<AttachmentUrl> getThumbnailFromHeader(@NotNull AttachmentHeader header, @NotNull ThumbnailType type, StopWatch stopWatch) {
		if (hasThumbnailOfType(type, header)) {
			String thumbnailId = type == ThumbnailType.BIG ? header.getThumbnailBigId() : header.getThumbnailSmallId();
			return logAndReturn(attachmentRepository.findUrl(thumbnailId, thumbnailId, TypeOfTheContent.ATTACHMENT_THUMBNAIL),
				"for attachment [{}] loaded in", header.getAttachmentId(), type, stopWatch);
		} else {
			return Optional.empty();
		}
	}

	private AttachmentHeader internalCreateAttachment(@NonNull AttachmentHeader attachmentHeader, @NonNull InputStream is, @NonNull String documentModelName) {
		modelPermissionEvaluator.checkModelReadPermission(documentModelName);
		TypeOfTheContent typeOfTheContent = AttachmentHelper.classifyAttachmentType(documentModelName, dataServicesCoreProperties);
		return internalCreateAttachment(attachmentHeader, is, typeOfTheContent);
	}

	private AttachmentHeader internalCreateAttachment(@NonNull AttachmentHeader attachmentHeader, @NonNull InputStream is,
		@NonNull TypeOfTheContent typeOfTheContent) {
		StopWatch stopWatch = StopWatch.createStarted();

		attachmentPermissionEvaluator.checkUploadPermission(attachmentHeader);
		DataServicesAttachment attachment = beforeCreate(attachmentHeader, is);

		if (dataServicesCoreProperties.getAttachments().getThumbnail().getPreview().isEnabled()) {
			saveContentInputStreamIntoMemory(attachment);
		}

		try (InputStream contentStream = attachment.getContent().get()) {
			final String mimeType;
			final InputStream streamForCreate;
			if (dataServicesCoreProperties.getAttachments().getMimeType().getProbeMimeType().isEnabled()) {
				byte[] bytes = IOUtils.toByteArray(contentStream);
				mimeType = contentTypeDetector.probeContentType(new ByteArrayInputStream(bytes), attachmentHeader.getFilename());
				streamForCreate = new ByteArrayInputStream(bytes);
			} else {
				mimeType = null;
				streamForCreate = contentStream;
			}
			AttachmentPersistenceResult persistenceResult = attachmentRepository
				.create(attachmentHeader.getAttachmentId(), streamForCreate, attachmentHeader.getFilename(), typeOfTheContent, mimeType);
			attachmentHeader.setSize(persistenceResult.getSize());
			attachmentHeader.setMimeType(persistenceResult.getMimeType());
			attachmentHeader.setTypeOfTheContent(typeOfTheContent);
			if (dataServicesCoreProperties.getAttachments().getThumbnail().getPreview().isEnabled()) {
				handleThumbnails(attachment, persistenceResult.getMimeType());
			}
			attachmentHeader = attachmentHeaderService.create(attachmentHeader);
			attachment.setHeader(attachmentHeader);
			log.debug("Attachment [{}] saved in [{}] ms", attachmentHeader.getAttachmentId(), stopWatch.getTime(TimeUnit.MILLISECONDS));
			return afterCreate(attachment).getHeader();
		} catch (IOException e) {
			rollbackContentsInHeader(attachmentHeader);
			throw new UnexpectedException(ExceptionKeys.UNEXPECTED_ERROR_KEY, e.getMessage()).withAnonymityMessage("Create attachment failed.");
		} catch (Exception e) {
			rollbackContentsInHeader(attachmentHeader);
			log.error("Failed to save attachment [{}]. Data rollbacked in [{}] ms", attachmentHeader.getAttachmentId(), stopWatch.getTime(), e);
			throw e;
		}
	}
}
