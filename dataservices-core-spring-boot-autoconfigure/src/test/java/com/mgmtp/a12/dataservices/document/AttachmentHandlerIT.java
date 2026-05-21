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
package com.mgmtp.a12.dataservices.document;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.document.internal.attachment.AttachmentSupport;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

public class AttachmentHandlerIT extends AbstractSpringContextIT {

	@Autowired private AttachmentSupport attachmentSupport;

	/**
	 * Method verifies that while model has 3 fields with "attachment_id" name, only two of them should be returned (the ones which are stored in group with "attachment" purpose type)
	 */
	@Test public void testCollectAttachmentIds() throws Exception {
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_MULTIPLE_ATTACHMENT_IDS_PATH);

		DocumentV2 document = documentFunctions.getKernelDocumentFromFile(
			DocumentModelConstants.BUSINESS_PARTNER_MULTIPLE_ATTACHMENT_IDS, PathConstants.DOCUMENTS_PATH + "BusinessPartnerWithMultipleAttachmentIdsDoc.json");

		List<String> attachmentIDs = attachmentSupport.collectAttachmentIDs(document);
		Assert.assertEquals(attachmentIDs.size(), 2);
	}
}
