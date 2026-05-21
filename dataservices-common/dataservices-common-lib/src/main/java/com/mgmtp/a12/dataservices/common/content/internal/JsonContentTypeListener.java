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
package com.mgmtp.a12.dataservices.common.content.internal;

import java.io.IOException;
import java.util.Optional;

import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.util.MimeTypeUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.common.events.ContentTypeDetectedEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * This is listener which listen on {@link ContentTypeDetectedEvent} to perform application/json content-type custom detection.
 */
@Slf4j
public class JsonContentTypeListener {

	private final ObjectMapper objectMapper;

	public JsonContentTypeListener(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper.copy().configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true);
	}

	/**
	 * This method tries to detect if the input stream in the event is application/json content-type or not.
	 *
	 * @param event This event contains input stream need to be detected application/json content-type, `DetectedMimeType` of this event will be mutated
	 */
	@EventListener public void jsonMimeTypeDetection(ContentTypeDetectedEvent event) {
		if (isSuitableJsonTypeDetection(event)) {
			try {
				String mimeType = Optional.ofNullable(objectMapper.readTree(event.getInputStream().get()))
					.map(node -> MimeTypeUtils.APPLICATION_JSON_VALUE).orElse(event.getDetectedMimeType());
				log.debug("Handling json detection for mime type: {} for file name: {}, result is: {}",
					event.getDetectedMimeType(), event.getFilename(), mimeType);
				event.setDetectedMimeType(mimeType);
			} catch (IOException e) {
				log.debug("An error occurs while trying to handle json mime type with file name: {}", event.getFilename(), e);
			}
		}
	}

	private static boolean isSuitableJsonTypeDetection(@NonNull ContentTypeDetectedEvent event) {
		return MimeTypeUtils.TEXT_PLAIN_VALUE.equalsIgnoreCase(event.getDetectedMimeType())
			&& event.getInputStream() != null && StringUtils.isBlank(event.getFilename());
	}

}
