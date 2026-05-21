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
package com.mgmtp.a12.dataservices.attachment.events;

import java.io.InputStream;
import java.util.Optional;
import java.util.function.Supplier;

import com.mgmtp.a12.dataservices.attachment.DataServicesAttachment;
import com.mgmtp.a12.dataservices.attachment.DataServicesThumbnail;
import com.mgmtp.a12.dataservices.attachment.ThumbnailType;
import com.mgmtp.a12.dataservices.common.events.internal.EventDocumentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

/**
 * Triggered before the attachment thumbnail is saved.
 *
 * @topic Attachment events
 */
@Data @AllArgsConstructor
@EventDocumentation public final class AttachmentThumbnailBeforeSaveEvent {
	private final DataServicesAttachment dataServicesAttachment;
	@NonNull private Optional<Supplier<InputStream>> thumbnailBig = Optional.empty();
	@NonNull private Optional<Supplier<InputStream>> thumbnailSmall = Optional.empty();

	/**
	 * Creates the event for generating thumbnails for the specified attachment.
	 *
	 * @param attachment The attachment for which thumbnails may be generated; must not be null.
	 */
	public AttachmentThumbnailBeforeSaveEvent(DataServicesAttachment attachment) {
		this.dataServicesAttachment = attachment;
	}

	/**
	 * Builds the {@link DataServicesThumbnail} instance for the BIG thumbnail, if available.
	 *
	 * @param mimeType The MIME type for the thumbnail (including charset, if applicable); must not be null.
	 * @return An {@link Optional} containing the BIG thumbnail descriptor if a supplier is present; otherwise empty.
	 */
	public Optional<DataServicesThumbnail> getThumbnailBig(String mimeType) {
		return this.thumbnailBig.map(thumbnailBigStream ->
			DataServicesThumbnail.builder()
				.content(thumbnailBigStream)
				.mimeType(mimeType)
				.type(ThumbnailType.BIG)
				.build()
		);
	}

	/**
	 * Builds the {@link DataServicesThumbnail} instance for the SMALL thumbnail, if available.
	 *
	 * @param mimeType The MIME type for the thumbnail (including charset, if applicable); must not be null.
	 * @return An {@link Optional} containing the SMALL thumbnail descriptor if a supplier is present; otherwise empty.
	 */
	public Optional<DataServicesThumbnail> getThumbnailSmall(String mimeType) {
		return this.thumbnailSmall.map(thumbnailSmallStream ->
			DataServicesThumbnail.builder()
				.content(thumbnailSmallStream)
				.mimeType(mimeType)
				.type(ThumbnailType.SMALL)
				.build()
		);
	}
}
