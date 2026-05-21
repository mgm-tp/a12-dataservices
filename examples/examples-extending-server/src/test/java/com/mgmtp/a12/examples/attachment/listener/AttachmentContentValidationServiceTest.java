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
package com.mgmtp.a12.examples.attachment.listener;

import com.mgmtp.a12.dataservices.attachment.DataServicesAttachment;
import com.mgmtp.a12.dataservices.attachment.events.AttachmentBeforeCreateEvent;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.examples.attachment.AttachmentContentValidator;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class AttachmentContentValidationServiceTest {

	@Test
	public void beforeCreate_shouldValidateContentAndResetSupplier_success() throws Exception {
		// Arrange
		byte[] original = "content".getBytes();
		List<byte[]> validatedContent = new ArrayList<>();

		AttachmentContentValidator validator1 = validatedContent::add;
		AttachmentContentValidator validator2 = validatedContent::add;

		AttachmentContentValidationService service = new AttachmentContentValidationService(
			Arrays.asList(validator1, validator2)
		);

		DataServicesAttachment attachment = new DataServicesAttachment();
		attachment.setContent(() -> new ByteArrayInputStream(original));

		AttachmentBeforeCreateEvent event = new AttachmentBeforeCreateEvent(attachment);

		// Act
		service.beforeCreate(event);

		// Assert: validators called with buffer
		Assert.assertEquals(validatedContent.size(), 2);
		Assert.assertEquals(validatedContent.get(0), original);
		Assert.assertEquals(validatedContent.get(1), original);

		// Assert: content supplier replaced and yields same bytes
		Supplier<InputStream> newSupplier = (Supplier<InputStream>) attachment.getContent();
		Assert.assertNotNull(newSupplier);
		byte[] afterBytes = newSupplier.get().readAllBytes();
		Assert.assertEquals(afterBytes, original);
	}

	@Test(expectedExceptions = UnexpectedException.class)
	public void beforeCreate_shouldThrowUnexpectedException_onIoError() {
		// Arrange: InputStream throws IOException when read
		Supplier<InputStream> faultySupplier = () -> new InputStream() {
			@Override
			public int read() throws IOException {
				throw new IOException("read fail");
			}
		};

		AttachmentContentValidator validator = bytes -> {};
		AttachmentContentValidationService service = new AttachmentContentValidationService(
			Arrays.asList(validator)
		);

		DataServicesAttachment attachment = new DataServicesAttachment();
		attachment.setContent(faultySupplier);

		AttachmentBeforeCreateEvent event = new AttachmentBeforeCreateEvent(attachment);

		// Act + Assert
		service.beforeCreate(event);
	}

	@Test public void beforeCreate_shouldDoNothing_whenContentIsNull() {
		// Arrange
		AttachmentContentValidator validator = mock(AttachmentContentValidator.class);
		AttachmentContentValidationService service = new AttachmentContentValidationService(Arrays.asList(validator));

		DataServicesAttachment attachment = new DataServicesAttachment();
		AttachmentBeforeCreateEvent event = new AttachmentBeforeCreateEvent(attachment);

		// Act
		service.beforeCreate(event);
		// Assert: no validator call and no content reset

		verify(validator, never()).validate(any());
	}
}
