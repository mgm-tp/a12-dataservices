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
package com.mgmtp.a12.dataservices.utils;

import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for removing `__meta` metadata groups from {@link DocumentV2} instances.
 *
 * @see DocumentMetadataConstants#DOCUMENT_METADATA_GROUP_NAME
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE) public final class DocumentMetadataUtils {

	/**
	 * Removes all `__meta` metadata groups from the given document.
	 *
	 * Traverses the document and removes every group named `__meta` at any level.
	 * If the document contains no `__meta` groups, it is returned unchanged.
	 *
	 * @param document the document from which to remove metadata groups, must not be `null`
	 * @return a new document without `__meta` groups, or the same instance if none were found
	 */
	public static DocumentV2 removeMetadataGroups(DocumentV2 document) {
		List<DocumentPointer> metadataPointers = collectMetadataGroupPointers(document);

		if (metadataPointers.isEmpty()) {
			return document;
		}

		for (DocumentPointer pointer : metadataPointers) {
			document = document.withGroupRemoved(pointer);
		}
		return document;
	}

	private static List<DocumentPointer> collectMetadataGroupPointers(DocumentV2 document) {
		List<DocumentPointer> pointers = new ArrayList<>();

		document.traverse(new IDocumentV2Visitor() {
			@Override
			public DescendType visitGroup(DocumentPointer pointerRelativeToBase, GroupInstanceV2 group) {
				if (isMetadataGroupPath(pointerRelativeToBase.fullName())) {
					pointers.add(pointerRelativeToBase);
					return DescendType.SKIP_CHILDREN;
				}
				return DescendType.VISIT_CHILDREN;
			}
		});

		return pointers;
	}

	private static boolean isMetadataGroupPath(String fullName) {
		// fullName may include repetition indices, e.g. "/__meta[1]" or "/SubDoc[1]/__meta[1]"
		// Strip all repetition indices to get the pure path
		String pathOnly = fullName.replaceAll("\\[\\d+]", "");
		return pathOnly.endsWith("/" + DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_NAME);
	}
}
