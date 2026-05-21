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

import org.springframework.context.event.EventListener;

import com.mgmtp.a12.dataservices.common.constants.ContentTypeConstants;
import com.mgmtp.a12.dataservices.common.events.ContentTypeDetectedEvent;

/**
 * This is listener which listen on {@link ContentTypeDetectedEvent} to perform application/msword content-type custom detection.
 */
public class MsWordContentTypeListener {

	protected static final String CONTENT_TYPE_X_TIKA_MSOFFICE = "application/x-tika-msoffice";

	/**
	 * This method will just simply map from application/x-tika-msoffice to application/msword.
	 *
	 * @param event This event contains input stream need to be detected application/msword content-type, `DetectedMimeType` of this event will be mutated
	 */
	@EventListener public void msWordMimeTypeDetection(ContentTypeDetectedEvent event) {
		if (CONTENT_TYPE_X_TIKA_MSOFFICE.equalsIgnoreCase(event.getDetectedMimeType())) {
			event.setDetectedMimeType(ContentTypeConstants.CONTENT_TYPE_MS_WORD);
		}
	}
}
