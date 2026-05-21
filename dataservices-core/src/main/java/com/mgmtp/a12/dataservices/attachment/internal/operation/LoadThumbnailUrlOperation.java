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
package com.mgmtp.a12.dataservices.attachment.internal.operation;

import org.springframework.transaction.annotation.Transactional;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.attachment.AttachmentThumbnailUrl;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.ThumbnailUtil;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Get {@link AttachmentThumbnailUrl} which contains all thumbnail URLs of an attachment.
 */
@Slf4j
@AllArgsConstructor
@RemoteOperation(name = CoreOperationConstants.LOAD_THUMBNAIL_URL_OPERATION, group = CoreOperationConstants.ATTACHMENT_OPERATIONS_GROUP)
public class LoadThumbnailUrlOperation {

	private final ThumbnailUtil thumbnailUtil;

	/**
	 * @param attachmentId The attachment id.
	 * @return Object of type {@link AttachmentThumbnailUrl} with properties:
	 * `smallThumbnailUrl`:: type String,
	 * `bigThumbnailUrl`:: type String.
	 */
	@Transactional(readOnly = true)
	public AttachmentThumbnailUrl rpc(@NonNull @JsonRpcParam("attachmentId") String attachmentId) {
		log.debug("{} called with parameters attachmentId=[{}]", CoreOperationConstants.LOAD_THUMBNAIL_URL_OPERATION, attachmentId);
		return thumbnailUtil.getThumbnailUrl(attachmentId);
	}
}
