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
package com.mgmtp.a12.dataservices.document.internal.attachment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Helper class to detect attachments inside a document.
 */
@RequiredArgsConstructor
public class AttachmentSupport {

	private final IDocumentModelResolver documentModelResolver;
	private final DocumentModelServiceFactory documentModelServiceFactory;

	public static final String ATTACHMENT_USAGE_TYPE = "attachment";
	public static final String ATTACHMENT_ID_FIELD = "attachment_id";
	private static final String ATTACHMENT_CONTENT_FIELD = "content";

	public List<String> collectAttachmentIDs(DocumentV2 document) {
		List<String> attachmentIds = new ArrayList<>();

		IDocumentModelSearchService documentModelSearchService =
			documentModelServiceFactory.createDocumentModelSearchService(documentModelResolver.getDocumentModelById(document.getDocumentModelId()));

		document.traverse(new IDocumentV2Visitor() {
			@Override public DescendType visitGroup(DocumentPointer pointerRelativeToBase,
				GroupInstanceV2 group) {
				if (isAttachmentGroup(documentModelSearchService, pointerRelativeToBase.fullName())) {
					attachmentIds.addAll(findAttachmentIds(group));
				}
				return DescendType.VISIT_CHILDREN;
			}
		});

		return attachmentIds;
	}

	public static boolean isAttachmentGroup(IGroup group) {
		return group.getUsageType()
			.filter(u -> Objects.equals(u, ATTACHMENT_USAGE_TYPE))
			.isPresent();
	}

	public static boolean isAttachmentGroup(IDocumentModelSearchService documentModelSearchService, String path) {
		return documentModelSearchService.getByPath(path).map(
			el -> {
				if (el instanceof IGroup group) {
					return group.getUsageType()
						.filter(u -> Objects.equals(u, ATTACHMENT_USAGE_TYPE))
						.isPresent();
				}
				return false;
			}
		).orElse(false);
	}

	public static boolean isAttachmentContentField(IField field) {
		return isAttachmentGroup(field.getParent()) && ATTACHMENT_CONTENT_FIELD.equals(field.getName());
	}

	@NonNull private static List<String> findAttachmentIds(GroupInstanceV2 group) {
		return group.directFields().stream()
			.filter(e -> e.getKey().equals(ATTACHMENT_ID_FIELD))
			.map(Map.Entry::getValue)
			.filter(Objects::nonNull)
			.map(FieldInstanceV2::value)
			.filter(Objects::nonNull)
			.map(String::valueOf)
			.filter(StringUtils::isNotBlank)
			.toList();
	}
}
