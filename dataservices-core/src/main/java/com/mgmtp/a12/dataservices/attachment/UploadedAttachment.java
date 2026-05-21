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

import java.io.File;

import lombok.Builder;

/**
 * Uploaded Attachment class. Contains the attachment ID as well as the uploaded file and its thumbnails.
 *
 */
@Builder
public class UploadedAttachment {

	private String attachmentId;
	private File uploadedFile;
	private File thumbnailSmall;
	private File thumbnailBig;

	/**
	 * Creates a new uploaded attachment value object.
	 *
	 * @param attachmentId The attachment identifier; must not be null.
	 * @param uploadedFile The uploaded file content; must not be null.
	 * @param thumbnailSmall The small thumbnail file; may be null if not generated.
	 * @param thumbnailBig The big thumbnail file; may be null if not generated.
	 */
	public UploadedAttachment(String attachmentId, File uploadedFile, File thumbnailSmall, File thumbnailBig) {
		this.attachmentId = attachmentId;
		this.uploadedFile = uploadedFile;
		this.thumbnailSmall = thumbnailSmall;
		this.thumbnailBig = thumbnailBig;
	}

	/**
	 * Gets the attachment ID.
	 *
	 * @return The attachment ID.
	 */
	public String getAttachmentId() {
		return attachmentId;
	}

	/**
	 * Gets the uploaded file.
	 *
	 * @return The uploaded file.
	 */
	public File getUploadedFile() {
		return uploadedFile;
	}

	/**
	 * Gets the small thumbnail file.
	 *
	 * @return The small thumbnail file.
	 */
	public File getThumbnailSmall() {
		return thumbnailSmall;
	}

	/**
	 * Gets the big thumbnail file.
	 *
	 * @return The big thumbnail file.
	 */
	public File getThumbnailBig() {
		return thumbnailBig;
	}

}
