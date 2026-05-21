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
package com.mgmtp.a12.dataservices.client.attachment;

import java.io.InputStream;

/**
 * Wraps an {@link InputStream} together with metadata to represent an attachment returned by the client.
 *
 * The `contentType` may be `null` for uploads; the server probes the content type from the file.
 * During downloads the content type is mapped from the HTTP header by the attachment controller.
 */
public class AttachmentDetail {

	private InputStream inputStream;
	// Content-type is only mapped from header when downloading image, for upload is not sent to the server. Server is probing content-type from file
	private String contentType;
	private String filename;

	/**
	 *
	 * @param inputStream content of the attachment
	 * @param contentType of the attachment (not required for the attachment upload)
	 * @param filename of the attachment
	 */
	public AttachmentDetail(InputStream inputStream, String contentType, String filename) {
		this.inputStream = inputStream;
		this.contentType = contentType;
		this.filename = filename;
	}

	/**
	 * Gets the input stream of the attachment.
	 *
	 * @return input stream
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * Sets the input stream of the attachment.
	 *
	 * @param inputStream to set
	 */
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	/**
	 * Content-type is set by the attachment controller during download and it should be mapped from http header, for upload it is not necessary because
	 * content-type is not transferred via http
	 * @return content-type or null
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Not needed for attachment upload
	 *
	 * @param contentType is set only for attachment download, it is not mandatory for upload
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Gets the filename of the attachment.
	 *
	 * @return filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Sets the filename of the attachment.
	 *
	 * @param filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}
}
