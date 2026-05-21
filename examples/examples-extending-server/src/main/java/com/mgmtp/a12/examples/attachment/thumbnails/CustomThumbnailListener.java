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
package com.mgmtp.a12.examples.attachment.thumbnails;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import com.mgmtp.a12.dataservices.attachment.events.AttachmentThumbnailBeforeSaveEvent;
import com.mgmtp.a12.examples.util.ResourceUtil;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Example on how to use the {@link AttachmentThumbnailBeforeSaveEvent} to have a special handling of attachment with specific mime types.
 * Providing custom thumbnails for PDF and PNG files.
 *
 */
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.attachments.custom-thumbnails", name = "enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
@Component public class CustomThumbnailListener {

	private final ResourceUtil resourceUtil;

	private static final List<MimeType> MIME_TYPES_FOR_CUSTOM_THUMBNAILS =
		List.of(MimeType.valueOf("application/pdf"), MimeTypeUtils.IMAGE_PNG);
	/**
	 * File name that triggers a rollback during thumbnail generation to demonstrate error handling.
	 */
	public static final String ROLLBACK_ATTACHMENT_FILE_NAME = "rollback_attachment_file_name.png";

	/**
	 * Handles {@link AttachmentThumbnailBeforeSaveEvent} to provide custom thumbnails for selected MIME types.
	 * If the attachment file name equals {@link #ROLLBACK_ATTACHMENT_FILE_NAME}, throws an
	 * {@link com.mgmtp.a12.dataservices.common.exception.UnexpectedException} to roll back persistence.
	 *
	 * @param event the event carrying the attachment details and allowing thumbnail suppliers to be set; never null.
	 */
	@EventListener
	public void attachmentThumbnailBeforeCreateListener(AttachmentThumbnailBeforeSaveEvent event) {
		log.info("Receiving event: {}", event.toString());
		if (ROLLBACK_ATTACHMENT_FILE_NAME.equalsIgnoreCase(event.getDataServicesAttachment().getHeader().getFilename())) {
			throw new UnexpectedException("Throwing exception for rolling back persisted attachment from content store");
		}
		MIME_TYPES_FOR_CUSTOM_THUMBNAILS.stream()
			.filter(mimeType -> mimeType.toString().equalsIgnoreCase(event.getDataServicesAttachment().getHeader().getMimeType()))
			.findAny().ifPresent(entry -> {
				event.setThumbnailBig(Optional.of(() -> resourceUtil.getInputStream("classpath:attachment/big_thumb.png")));
				event.setThumbnailSmall(Optional.of(() -> resourceUtil.getInputStream("classpath:attachment/small_thumb.png")));
			});
	}

}
