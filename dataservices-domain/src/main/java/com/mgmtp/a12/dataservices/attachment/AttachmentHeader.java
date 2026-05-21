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

import java.time.Instant;
import java.util.List;

import com.mgmtp.a12.dataservices.reference.GenericReference;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The main persistence model for attachment header information.
 *
 */
@Data @AllArgsConstructor @NoArgsConstructor(access = AccessLevel.PRIVATE) @Builder(toBuilder = true)
public class AttachmentHeader {

	private String attachmentId;
	private String thumbnailBigId;
	private String thumbnailSmallId;
	private String filename;
	private List<AttachmentReference<GenericReference>> references;
	private Instant createdAt;
	private String createdBy;
	private Instant modifiedAt;
	private String modifiedBy;
	private String mimeType;
	private Long size;
	private TypeOfTheContent typeOfTheContent;
	private List<AttachmentAnnotation> annotations;
}
