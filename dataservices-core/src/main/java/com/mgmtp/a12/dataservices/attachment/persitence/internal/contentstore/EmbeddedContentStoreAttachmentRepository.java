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

import com.mgmtp.a12.contentstore.service.ContentStoreService;
import com.mgmtp.a12.dataservices.attachment.AttachmentUrl;
import com.mgmtp.a12.dataservices.attachment.TypeOfTheContent;
import com.mgmtp.a12.dataservices.attachment.persitence.AttachmentPersistenceResult;
import com.mgmtp.a12.dataservices.attachment.persitence.IAttachmentRepository;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.ThumbnailUtil;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.utils.internal.AttachmentUtil;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EmbeddedContentStoreAttachmentRepository implements IAttachmentRepository {

	private final ContentStoreService contentStoreService;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final ContentStoreMapper contentStoreMapper;

	@Override public AttachmentPersistenceResult create(@NonNull String id, @NonNull InputStream is,
		@NonNull String filename, @NonNull TypeOfTheContent type, String mimeType) {
		return switch (type) {
			case ATTACHMENT_SECURED, ATTACHMENT_PUBLIC -> createAttachment(id, is, filename, type.getPersistentType(), mimeType);
			case ATTACHMENT_THUMBNAIL -> createThumbnail(id, is, type.getPersistentType());
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
		contentStoreService.deleteById(id);
	}

	private AttachmentPersistenceResult createAttachment(String attachmentId, InputStream is, String filename, String persistentType, String mimeType) {
		return contentStoreMapper.toAttachmentPersistenceResult(
			contentStoreService.saveContent(attachmentId, persistentType, is, filename, mimeType));
	}

	private AttachmentPersistenceResult createThumbnail(String id, InputStream is, String persistentType) {
		return contentStoreMapper.toAttachmentPersistenceResult(
			contentStoreService.saveContent(id, persistentType,
				is, id, ThumbnailUtil.getImageMimeType()));
	}

	private Optional<AttachmentUrl> findSecuredAttachmentUrl(String id, String filename) {
		try {
			return Optional.ofNullable(contentStoreService.requestContentUrl(id, getTicketTimeout(dataServicesCoreProperties)))
				.map(url -> AttachmentUtil.createAttachmentUrl(url, filename));
		} catch (NotFoundException exception) {
			return Optional.empty();
		}
	}

	private Optional<AttachmentUrl> findPublicAttachmentUrl(String id, String filename) {
		try {
			return contentStoreService.findPublicContentUrl(id)
				.map(url ->
					Optional.ofNullable(filename)
						.map(name -> AttachmentUtil.createAttachmentUrl(url, name))
						.orElse(new AttachmentUrl(url))
				);
		} catch (NotFoundException exception) {
			return Optional.empty();
		}
	}

	private long getTicketTimeout(DataServicesCoreProperties dataServicesCoreProperties) {
		return dataServicesCoreProperties.getAttachments().getExt().getContentstore().getTicketTimeout();
	}

}
