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
package com.mgmtp.a12.dataservices.document.operation.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.dataservices.attachment.AttachmentReferenceType;
import com.mgmtp.a12.dataservices.attachment.AttachmentThumbnailUrl;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.AttachmentHeaderEntity;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.AttachmentReferenceEntity;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.repository.AttachmentReferenceJpaRepository;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.ThumbnailUtil;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.utils.internal.LoadedDocumentReferencesContextHolder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Internal operation to load thumbnails for attachments of all documents loaded in single web request. It is achieved by storing documents in request context
 * which is then processed to load thumbnails.
 */
@Slf4j
@RemoteOperation(name = CoreOperationConstants.LOAD_THUMBNAIL_URLS_INTERNAL_OPERATION, group = CoreOperationConstants.A12_INTERNAL_OPERATIONS_GROUP)
@RequiredArgsConstructor
public class LoadThumbnailUrlsOperation {

	private final Optional<ThumbnailUtil> thumbnailUtilOpt;
	private final AttachmentReferenceJpaRepository attachmentReferenceJpaRepository;

	/**
	 * Method takes no parameters as documents are taken from request context.
	 *
	 * @return Returns a map where the keys are document references, and the values are
	 *  * maps with keys representing attachment IDs and values representing associated thumbnail URLs.
	 */
	public Map<String, Map<String, AttachmentThumbnailUrl>> rpc() {
		// When attachments are disabled and there is no ThumbnailUtil bean , dataservices still return an empty result.
		if (thumbnailUtilOpt.isEmpty()) {
			return new HashMap<>();
		}

		Map<String, Map<String, AttachmentThumbnailUrl>> result = new HashMap<>();
		Set<DocumentReference> documentReferences = LoadedDocumentReferencesContextHolder.getAllDocumentReferences();

		for (DocumentReference documentReference : documentReferences) {
			Map<String, AttachmentThumbnailUrl> thumbnailUrlsMap = constructThumbnailUrlsMap(documentReference.toString(), thumbnailUtilOpt.get());
			if (!thumbnailUrlsMap.isEmpty()) {
				result.put(documentReference.toString(), thumbnailUrlsMap);
			}
		}

		return result;
	}

	private Map<String, AttachmentThumbnailUrl> constructThumbnailUrlsMap(String documentReference, ThumbnailUtil thumbnailUtil) {
		return attachmentReferenceJpaRepository.findAllByTypeAndReference(AttachmentReferenceType.DOCUMENT, documentReference).stream()
			.map(AttachmentReferenceEntity::getAttachmentHeader)
			.collect(Collectors.toMap(AttachmentHeaderEntity::getId, thumbnailUtil::getThumbnailUrl));
	}
}
