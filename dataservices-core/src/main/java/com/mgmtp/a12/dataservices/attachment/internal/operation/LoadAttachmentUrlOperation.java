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
import com.mgmtp.a12.dataservices.attachment.DataServicesAttachmentURL;
import com.mgmtp.a12.dataservices.attachment.internal.AttachmentMapper;
import com.mgmtp.a12.dataservices.attachment.AttachmentService;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Get URL of attachment from Data Services. The link should be considered to be secure, which means that it should be unpredictable and only temporarily accessible.
 */
@Slf4j
@RequiredArgsConstructor
@RemoteOperation(name = CoreOperationConstants.LOAD_ATTACHMENT_URL_OPERATION, group = CoreOperationConstants.ATTACHMENT_OPERATIONS_GROUP)
public class LoadAttachmentUrlOperation {

	private final AttachmentService attachmentService;
	private final AttachmentMapper attachmentMapper;

	/**
	 * @param attachmentId The attachment id.
	 * @param docRef The reference of the document to which the attachment is assigned.
	 * @return Object of type {@link DataServicesAttachmentURL}.
	 */
	@Transactional(readOnly = true)
	public DataServicesAttachmentURL rpc(@NonNull @JsonRpcParam("attachmentId") String attachmentId, @NonNull @JsonRpcParam("docRef") DocumentReference docRef) {
		log.debug("{} called with parameters attachmentId=[{}], docRef=[{}]", CoreOperationConstants.LOAD_ATTACHMENT_URL_OPERATION, attachmentId, docRef);

		return attachmentService.findAttachmentUrl(attachmentId, docRef)
			.map(attachmentMapper::toDataServicesAttachmentURL)
			.orElseThrow(() -> new NotFoundException(ExceptionKeys.ATTACHMENT_NOT_FOUND_ERROR_KEY,
				String.format("No URL from attachmentId %s could be found.", attachmentId)));
	}
}
