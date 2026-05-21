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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.attachment.DataServicesAttachment;
import com.mgmtp.a12.dataservices.attachment.events.AttachmentBeforeCreateEvent;
import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.examples.attachment.AttachmentContentValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Listener component demonstrating validation of attachment content via injected {@link AttachmentContentValidator} implementations.
 * Each validator is executed before an attachment is persisted; a validator signals failure by throwing an exception.
 * The active validator set is controlled by Spring configuration (e.g. profiles) enabling environment-specific validation rules.
 */
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.attachments.content-validation", name = "enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
@Component public class AttachmentContentValidationService {

	private final List<AttachmentContentValidator> attachmentContentValidators;

	@CommonDataServicesEventListener public void beforeCreate(AttachmentBeforeCreateEvent attachmentBeforeCreateEvent) {
		DataServicesAttachment dataServicesAttachment = attachmentBeforeCreateEvent.getAttachment();
		Optional.ofNullable(dataServicesAttachment.getContent())
			.ifPresent(cs -> {
				try (InputStream is = cs.get()){
					byte[] buffer = IOUtils.toByteArray(is);
					attachmentContentValidators.forEach(attachmentContentValidator -> attachmentContentValidator.validate(buffer));
					// Create a new input stream after consuming it because input stream is NOT re-readable
					dataServicesAttachment.setContent(() -> new ByteArrayInputStream(buffer));
				} catch (IOException e) {
					throw new UnexpectedException("Error during before create attachment", e);
				}
			});
	}
}
