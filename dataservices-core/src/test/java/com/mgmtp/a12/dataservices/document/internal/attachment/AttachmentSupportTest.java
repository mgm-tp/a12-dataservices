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

import java.util.Optional;

import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;

import static com.mgmtp.a12.dataservices.document.internal.attachment.AttachmentSupport.ATTACHMENT_USAGE_TYPE;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class AttachmentSupportTest {

	private IField contentField;
	private IGroup attachmentGroup;

	@BeforeClass
	public void setUp() {
		contentField = Mockito.mock(IField.class);
		attachmentGroup = Mockito.mock(IGroup.class);
	}

	@Test
	public void testIsAttachmentContentField() {
		Mockito.when(attachmentGroup.getUsageType()).thenReturn(Optional.of(ATTACHMENT_USAGE_TYPE));
		Mockito.when(contentField.getParent()).thenReturn(attachmentGroup);
		Mockito.when(contentField.getName()).thenReturn("content");
		assertTrue(AttachmentSupport.isAttachmentContentField(contentField));
		Mockito.when(contentField.getName()).thenReturn("other");
		assertFalse(AttachmentSupport.isAttachmentContentField(contentField));

		Mockito.when(attachmentGroup.getUsageType()).thenReturn(Optional.of("other"));
		Mockito.when(contentField.getParent()).thenReturn(attachmentGroup);
		Mockito.when(contentField.getName()).thenReturn("content");
		assertFalse(AttachmentSupport.isAttachmentContentField(contentField));
	}
}
