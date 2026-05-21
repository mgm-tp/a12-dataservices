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
package com.mgmtp.a12.examples.attachment.mime;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.events.ContentTypeDetectedEvent;
import com.mgmtp.a12.examples.configuration.ExtendedServerConfigurationProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Mimetype overwrite example. Replaces a configured mime-type with a replacement mime-type after DS probing
 *
 */
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.attachments.mime-types.custom", name = "enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
@Component public class CustomZipTypeListener {

	private final ExtendedServerConfigurationProperties extendedServerConfigurationProperties;

	/**
	 * Replaces the detected MIME type with a configured custom replacement after DataServices probing.
	 *
	 * @param event event providing the detected MIME type which may be overwritten; never null.
	 */
	@EventListener public void contentTypeDetectedEventListener(ContentTypeDetectedEvent event) {
		ExtendedServerConfigurationProperties.Attachments.MimeTypes mimeTypes = extendedServerConfigurationProperties.getAttachments().getMimeTypes();

		log.info("Receiving event: {}", event.toString());
		if (mimeTypes.getCustom().getMimeType().equalsIgnoreCase(event.getDetectedMimeType())) {
			event.setDetectedMimeType(mimeTypes.getCustom().getReplacement());
		}
	}
}
