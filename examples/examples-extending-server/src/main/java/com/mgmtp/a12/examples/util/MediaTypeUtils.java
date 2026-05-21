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
package com.mgmtp.a12.examples.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.tika.mime.MediaType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.content.ContentTypeDetector;

import lombok.RequiredArgsConstructor;

/**
 * Utility for media type detection using {@link ContentTypeDetector} and a default fallback type.
 */
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.attachments.mime-types.custom", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Component public class MediaTypeUtils {
	public static final MediaType APPLICATION_DOWNLOAD = MediaType.parse("application/x-msdownload");

	private final ContentTypeDetector contentTypeDetector;

	/**
	 * Compares the detected media type of the given bytes with the expected {@link MediaType}.
	 *
	 * @param bytes the content to inspect; must not be null.
	 * @param mediaType the expected media type to compare against; must not be null.
	 * @return true if the detected media type equals the expected type; false otherwise. If detection fails, treats unknown as {@link MediaType#OCTET_STREAM}.
	 */
	public boolean mediaTypeEquals(byte[] bytes, MediaType mediaType) {
		try {
			return detectMediaType(bytes).equals(mediaType);
		} catch (IOException e) {
			return MediaType.OCTET_STREAM.equals(mediaType);
		}
	}

	private MediaType detectMediaType(byte[] bytes) throws IOException {
		return MediaType.parse(contentTypeDetector.probeContentType(new ByteArrayInputStream(bytes), null));
	}
}
