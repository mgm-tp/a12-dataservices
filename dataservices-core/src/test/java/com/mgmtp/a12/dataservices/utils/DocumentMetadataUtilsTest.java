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

import org.testng.annotations.Test;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_NAME;
import static com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_PATH;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

public class DocumentMetadataUtilsTest {

	@Test(description = "Should remove root-level __meta group from a simple document")
	public void shouldRemoveMetadataGroupWhenDocumentHasRootMeta() {
		// Create document with __meta fields populated
		DocumentV2 document = createDocumentWithMetadata(BUSINESS_PARTNER_DOCUMENT_MODEL);

		// Verify __meta exists before removal
		assertNotNull(document.group(DOCUMENT_METADATA_GROUP_PATH),
			"Document should have __meta group before removal");

		// Act
		DocumentV2 result = DocumentMetadataUtils.removeMetadataGroups(document);

		// Verify __meta is removed
		assertNull(result.group(DOCUMENT_METADATA_GROUP_PATH),
			"Document should not have __meta group after removal");
	}

	@Test(description = "Should return same document when no __meta group exists")
	public void shouldReturnSameDocumentWhenNoMetadataGroupExists() {
		// DocumentV2.empty() creates a document without any groups
		DocumentV2 document = DocumentV2.empty(BUSINESS_PARTNER_DOCUMENT_MODEL);

		// Verify no __meta group exists
		assertNull(document.group(DOCUMENT_METADATA_GROUP_PATH),
			"Precondition: empty document should not have __meta");

		// Act
		DocumentV2 result = DocumentMetadataUtils.removeMetadataGroups(document);

		// Should return the same instance (no-op)
		assertSame(result, document,
			"Should return the same document instance when no __meta groups exist");
	}

	@Test(description = "Should remove __meta group with multiple metadata fields")
	public void shouldRemoveMetadataGroupWhenDocumentHasMultipleMetadataFields() {
		// Create document with several __meta fields set
		DocumentV2 document = DocumentV2.empty(BUSINESS_PARTNER_DOCUMENT_MODEL)
			.withFieldValue(
				DocumentPointer.of(List.of(DOCUMENT_METADATA_GROUP_NAME, "docRef"), List.of(1, 1)),
				"BusinessPartner/test-id")
			.withFieldValue(
				DocumentPointer.of(List.of(DOCUMENT_METADATA_GROUP_NAME, "modelReference"), List.of(1, 1)),
				BUSINESS_PARTNER_DOCUMENT_MODEL)
			.withFieldValue(
				DocumentPointer.of(List.of(DOCUMENT_METADATA_GROUP_NAME, "creator"), List.of(1, 1)),
				"testUser");

		// Verify __meta exists with content
		assertNotNull(document.group(DOCUMENT_METADATA_GROUP_PATH),
			"Document should have __meta group before removal");

		// Act
		DocumentV2 result = DocumentMetadataUtils.removeMetadataGroups(document);

		// Verify __meta is completely removed
		assertNull(result.group(DOCUMENT_METADATA_GROUP_PATH),
			"Document should not have __meta group after removal");
	}

	@Test(description = "Should remove all __meta groups from a document with nested metadata")
	public void shouldRemoveAllMetadataGroupsWhenDocumentHasNestedMeta() {
		// Create a document that has __meta at both root and a nested path (simulating CDD structure)
		DocumentV2 document = DocumentV2.empty(BUSINESS_PARTNER_DOCUMENT_MODEL)
			.withFieldValue(
				DocumentPointer.of(List.of(DOCUMENT_METADATA_GROUP_NAME, "docRef"), List.of(1, 1)),
				"BusinessPartner/test-id");

		// Verify __meta exists before removal
		List<String> metaPathsBefore = collectMetadataGroupPaths(document);
		assertTrue(!metaPathsBefore.isEmpty(),
			"Document should have at least one __meta group, found: " + metaPathsBefore.size());

		// Act
		DocumentV2 result = DocumentMetadataUtils.removeMetadataGroups(document);

		// Verify no __meta groups remain
		List<String> metaPathsAfter = collectMetadataGroupPaths(result);
		assertTrue(metaPathsAfter.isEmpty(),
			"Document should have no __meta groups after removal, but found: " + metaPathsAfter);
	}

	private DocumentV2 createDocumentWithMetadata(String documentModelName) {
		return DocumentV2.empty(documentModelName)
			.withFieldValue(
				DocumentPointer.of(List.of(DOCUMENT_METADATA_GROUP_NAME, "docRef"), List.of(1, 1)),
				documentModelName + "/test-doc-id");
	}

	private List<String> collectMetadataGroupPaths(DocumentV2 document) {
		List<String> paths = new ArrayList<>();
		document.traverse(new IDocumentV2Visitor() {
			@Override
			public DescendType visitGroup(DocumentPointer pointerRelativeToBase, GroupInstanceV2 group) {
				String fullName = pointerRelativeToBase.fullName();
				String pathOnly = fullName.replaceAll("\\[\\d+]", "");
				if (pathOnly.endsWith("/" + DOCUMENT_METADATA_GROUP_NAME)) {
					paths.add(fullName);
					return DescendType.SKIP_CHILDREN;
				}
				return DescendType.VISIT_CHILDREN;
			}
		});
		return paths;
	}
}
