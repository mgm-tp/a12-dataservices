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

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeaderSpec;
import com.mgmtp.a12.dataservices.attachment.AttachmentReference;
import com.mgmtp.a12.dataservices.attachment.AttachmentService;
import com.mgmtp.a12.dataservices.attachment.AttachmentUrl;
import com.mgmtp.a12.dataservices.attachment.ThumbnailType;
import com.mgmtp.a12.dataservices.attachment.header.AttachmentHeaderService;
import com.mgmtp.a12.dataservices.attachment.internal.AttachmentMapper;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Get {@link AttachmentHeader} of the attachment by attachmentId.
 */
@Slf4j
@RequiredArgsConstructor
@RemoteOperation(name = CoreOperationConstants.LOAD_ATTACHMENT_HEADER_OPERATION, group = CoreOperationConstants.ATTACHMENT_OPERATIONS_GROUP)
public class LoadAttachmentHeaderOperation {

	private final AttachmentHeaderService attachmentHeaderService;
	private final DocumentService documentService;
	private final AttachmentMapper attachmentMapper;
	private final AttachmentService attachmentService;

	/**
	 * @param attachmentId The attachment id.
	 * @param docRef The reference of the document to which the attachment is assigned.
	 * @return Object of type {@link AttachmentHeader}.
	 */
	@Transactional(readOnly = true)
	public AttachmentHeaderSpec rpc(@NonNull @JsonRpcParam("attachmentId") String attachmentId, @NonNull @JsonRpcParam("docRef") DocumentReference docRef) {
		log.debug("{} called with parameters attachmentId=[{}], docRef=[{}]", CoreOperationConstants.LOAD_ATTACHMENT_HEADER_OPERATION, attachmentId, docRef);

		Optional<AttachmentHeader> attachmentHeaderOptional = attachmentHeaderService.load(attachmentId)
			.filter(ah -> ah.getReferences().contains(AttachmentReference.fromDocRef(docRef)));

		//document needs to be loaded because of security, Security details should not be displayed to the user, header is either loadable or not
		if (documentService.load(docRef).isEmpty()) {
			log.debug("Document [{}] is not found for attachment with id [{}]", docRef, attachmentId);
			throw constructNotFoundException(attachmentId);
		}

		return attachmentHeaderOptional
			.map(ah -> attachmentMapper.toHeaderSpec(
				ah, attachmentService.findThumbnailUrl(ah, ThumbnailType.BIG).map(AttachmentUrl::getLocation).orElse(null),
					attachmentService.findThumbnailUrl(ah, ThumbnailType.SMALL).map(AttachmentUrl::getLocation).orElse(null))
				)
			.orElseThrow(() -> constructNotFoundException(attachmentId));
	}

	private static NotFoundException constructNotFoundException(String attachmentId) {
		return new NotFoundException(ExceptionKeys.ATTACHMENT_NOT_FOUND_ERROR_KEY, String.format("Attachment [%s] not found", attachmentId));
	}
}
