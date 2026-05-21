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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;

import com.mgmtp.a12.dataservices.common.content.ContentTypeDetector;
import com.mgmtp.a12.dataservices.common.events.ContentTypeDetectedEvent;

public class TikaContentTypeDetector implements ContentTypeDetector {

	private static final Predicate<String> FILE_NAME_PATTERN = Pattern.compile(".+\\..+").asMatchPredicate();

	private final ApplicationEventPublisher eventPublisher;
	private static TikaWrapper.TikaWrapperFactory TIKA_WRAPPER_FACTORY;

	public TikaContentTypeDetector(ApplicationEventPublisher eventPublisher, boolean isInMemFileSystem) {
		this.eventPublisher = eventPublisher;
		if (TIKA_WRAPPER_FACTORY == null) {
			TIKA_WRAPPER_FACTORY = new TikaWrapper.TikaWrapperFactory(isInMemFileSystem);
		}
	}

	@Override public String probeContentType(InputStream inputStream, String filename) throws IOException {
		byte[] content = IOUtils.toByteArray(inputStream);
		try (TikaWrapper tikaWrapper = TIKA_WRAPPER_FACTORY.get(new ByteArrayInputStream(content))) {
			final String contentType = tikaWrapper.detect(Optional.ofNullable(filename)
					.filter(this::validFileName)
					.orElse(null))
				.toString();
			ContentTypeDetectedEvent contentTypeDetectedEvent = new ContentTypeDetectedEvent(contentType, filename, () -> new ByteArrayInputStream(content));
			eventPublisher.publishEvent(contentTypeDetectedEvent);
			return contentTypeDetectedEvent.getDetectedMimeType();
		}
	}

	private boolean validFileName(String name) {
		return StringUtils.isNotBlank(name) && FILE_NAME_PATTERN.test(name);
	}

	@Override public long getContentLength(byte[] bytes) throws IOException {
		try (TikaWrapper tikaInputStream = TIKA_WRAPPER_FACTORY.get(bytes)) {
			return tikaInputStream.getLength();
		}
	}
}
