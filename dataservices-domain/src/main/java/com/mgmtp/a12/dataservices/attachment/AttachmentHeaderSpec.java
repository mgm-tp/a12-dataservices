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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Attachment Header Spec class used for storing metadata of an attachment.
 *
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class AttachmentHeaderSpec {

	/**
	 * Deprecated convenience constructor for legacy deserialization.
	 * Prefer using the builder.
	 *
	 * @param attachmentId The unique attachment identifier; may be null if not yet persisted.
	 * @param filename Original file name; may be null.
	 * @param mimeType MIME type of the content; may be null.
	 * @param size Size in bytes; may be null.
	 * @param annotations Optional application-specific annotations; may be null or empty.
	 * @deprecated Use {@link AttachmentHeaderSpec()} and setters or the Lombok builder. This constructor will be removed in the next breaking version.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	@Generated
	public AttachmentHeaderSpec(final String attachmentId, final String filename, final String mimeType, final Long size, final List<AttachmentAnnotation> annotations) {
		this.attachmentId = attachmentId;
		this.filename = filename;
		this.mimeType = mimeType;
		this.size = size;
		this.annotations = annotations;
	}

	private String attachmentId;
	private String filename;
	private String mimeType;
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String smallThumbnailUrl;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String bigThumbnailUrl;
	private Long size;
	private List<AttachmentAnnotation> annotations;
}

