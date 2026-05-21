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
package com.mgmtp.a12.examples.attachment.audit;

import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.DataServicesAttachment;
import com.mgmtp.a12.dataservices.attachment.events.AttachmentBeforeCreateEvent;
import com.mgmtp.a12.examples.extra.ExtraEntity;
import com.mgmtp.a12.examples.extra.ExtraEntityRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.Assert;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Listeners(MockitoTestNGListener.class)
public class AttachmentAuditServiceTest {

	@Mock private ExtraEntityRepository extraEntityRepository;
	@InjectMocks private AttachmentAuditService auditService;

	@Test
	public void attachmentBeforeCreateListener_persistsAudit_success() {
		// Arrange: use real objects
		AttachmentHeader header = AttachmentHeader.builder()
			.attachmentId("att-123")
			.filename("doc.pdf")
			.build();
		DataServicesAttachment attachment = DataServicesAttachment.builder()
			.header(header)
			.build();
		AttachmentBeforeCreateEvent event = new AttachmentBeforeCreateEvent(attachment);
		// Act
		auditService.attachmentBeforeCreateListener(event);

		// Assert
		ArgumentCaptor<ExtraEntity> captor = ArgumentCaptor.forClass(ExtraEntity.class);
		verify(extraEntityRepository, times(1)).save(captor.capture());

		ExtraEntity saved = captor.getValue();
		Assert.assertNotNull(saved);
		Assert.assertEquals(saved.getId(), "att-123");
		Assert.assertEquals(saved.getText(), "doc.pdf");
	}
}
