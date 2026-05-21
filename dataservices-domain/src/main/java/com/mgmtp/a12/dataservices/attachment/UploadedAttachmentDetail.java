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
package com.mgmtp.a12.dataservices.attachment;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Uploaded Attachment Detail class. Metadata of an uploaded attachment including thumbnails.
 *
 * @deprecated This is not used anymore and will be removed in next breaking version.
 */
@Deprecated(since = "38.1.0", forRemoval = true)
@Data @NoArgsConstructor @AllArgsConstructor(access = AccessLevel.PRIVATE) @Builder
public class UploadedAttachmentDetail {
	private String attachmentId;
	private String uploadedFile;
	private String thumbnailSmall;
	private String thumbnailBig;

	/**
	 * Builder class for {@link UploadedAttachmentDetail}.
	 */
	public static class UploadedAttachmentDetailBuilder {
		/**
		 * Adds a thumbnail reference to this builder based on the given type.
		 *
		 * @param thumbnail The thumbnail identifier or URL; must not be null.
		 * @param type The thumbnail type ({@link ThumbnailType#SMALL} or {@link ThumbnailType#BIG}); must not be null.
		 * @return This builder for fluent chaining.
		 * @throws InvalidInputException If the thumbnail {@link ThumbnailType} is not supported.
		 */
		public UploadedAttachmentDetailBuilder thumbnail(String thumbnail, ThumbnailType type) {
			switch (type) {
			case BIG:
				thumbnailBig(thumbnail);
				break;
			case SMALL:
				thumbnailSmall(thumbnail);
				break;
			default:
				throw new InvalidInputException(String.format("Thumbnail type %s is not supported.", type));
			}
			return this;
		}
	}
}

