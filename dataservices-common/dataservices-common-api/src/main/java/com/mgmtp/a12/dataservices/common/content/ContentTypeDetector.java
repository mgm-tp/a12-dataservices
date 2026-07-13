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
package com.mgmtp.a12.dataservices.common.content;

import java.io.IOException;
import java.io.InputStream;

import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Strategy interface for probing the MIME type of binary content.
 * Implementations should publish a {@link com.mgmtp.a12.dataservices.common.events.ContentTypeDetectedEvent}
 * after successful detection. Be mindful that the provided {@link InputStream} may be consumed during probing;
 * supply a fresh stream or a supplier if further processing is required.
 */
@OnlyForUsage public interface ContentTypeDetector {

	/**
	 * Detects the MIME type from the given content stream.
	 *
	 * @param inputStream Binary content stream; may be consumed by the detector and should be re-creatable if needed.
	 * @param filename Optional original filename used to improve detection accuracy; may be null.
	 * @return Detected MIME type as a string (e.g., `application/pdf`).
	 * @throws IOException If the stream cannot be read or an IO error occurs during detection.
	 */
	String probeContentType(InputStream inputStream, String filename) throws IOException;

	/**
	 * Calculates the content length in bytes for the provided array.
	 *
	 * @param bytes Content bytes; must not be null.
	 * @return Number of bytes.
	 * @throws IOException If length computation requires IO and fails. TODO: Clarify contract (uncertain behavior).
	 */
	long getContentLength(byte[] bytes) throws IOException;
}
