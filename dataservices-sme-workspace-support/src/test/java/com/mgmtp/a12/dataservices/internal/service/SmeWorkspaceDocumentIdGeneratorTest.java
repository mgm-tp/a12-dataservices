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
package com.mgmtp.a12.dataservices.internal.service;

import java.util.UUID;

import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import static com.mgmtp.a12.dataservices.constants.SmeWorkspaceConstants.IGNORED_ID;
import static org.mockito.Mockito.spy;

@Listeners(MockitoTestNGListener.class)
public class SmeWorkspaceDocumentIdGeneratorTest {
	private SmeWorkspaceDocumentIdGenerator smeWorkspaceDocumentIdGenerator = spy(SmeWorkspaceDocumentIdGenerator.class);

	@Test
	void testGenerateId_keepCurrentDocumentId() {
		String id = UUID.randomUUID().toString();
		DocumentV2 document = DocumentV2.empty(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, id);
		Assert.assertEquals(smeWorkspaceDocumentIdGenerator.generateId(document).get(), id);
	}

	@Test
	void testGenerateId_returnNew() {
		String id = UUID.randomUUID().toString();
		DocumentV2 document = DocumentV2.empty(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);
		document = document.withId(IGNORED_ID);
		Assert.assertNotEquals(smeWorkspaceDocumentIdGenerator.generateId(document).get(), IGNORED_ID);

		document = document.withFieldValue(DocumentMetadataConstants.DOCREF_METADATA_PATH, id);
		Assert.assertNotEquals(smeWorkspaceDocumentIdGenerator.generateId(document).get(), id);
		Assert.assertNotNull(smeWorkspaceDocumentIdGenerator.generateId(null).get());
	}

}
