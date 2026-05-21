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
package com.mgmtp.a12.examples.mime;

import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentService;
import com.mgmtp.a12.examples.AbstractITBase;
import com.mgmtp.a12.examples.util.ResourceUtil;

@WithUserDetails("admin")
@ActiveProfiles({ "dataservices-example-common","dataservices-example-import_models", "dataservices-example-attachments-mime-type-custom" })
public class CustomZipTypeListenerIT extends AbstractITBase {

	public static final String ATTACHMENT_ZIP = "file.zip";

	@Autowired private AttachmentService attachmentService;

	@Autowired private ResourceUtil resourceUtil;

	@Test
	public void testCustomZipTYpeDetection() {
		InputStream zipFile = resourceUtil.getInputStream(ATTACHMENT_UPLOAD_PATH + ATTACHMENT_ZIP);
		AttachmentHeader attachmentHeader =
			attachmentService.createAttachment(
				zipFile,
				ATTACHMENT_ZIP,
				"BusinessPartner",
				"/BusinessPartnerRoot/Attachment",
				List.of());

		Assert.assertEquals(attachmentHeader.getMimeType(), "zip1");
	}
}
