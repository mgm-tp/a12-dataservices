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
package com.mgmtp.a12.dataservices.attachment;

import lombok.Getter;

/**
 * Represents attachment content categories with distinct persistence semantics.
 */
public enum TypeOfTheContent {

	/**
	 * This is secured attachment content type, will not be downloaded directly but requires additional actions for a secured download.
	 */
	ATTACHMENT_SECURED("ATTACHMENT_SECURED", "private"),

	/**
	 * This is public attachment content type, which can be downloaded directly from external without any restriction or authentication.
	 */
	ATTACHMENT_PUBLIC("ATTACHMENT_PUBLIC", "public"),

	/**
	 * This is thumbnail content type, mostly used for generated thumbnails from supported image attachments, which can be downloaded directly from external without any restriction or authentication.
	 */
	ATTACHMENT_THUMBNAIL("ATTACHMENT_THUMBNAIL", "public");

	private final String type;
	@Getter private final String persistentType;

	TypeOfTheContent(String type, String persistentType) {
		this.type = type;
		this.persistentType = persistentType;
	}

	@Override public String toString() {
		return this.type;
	}
}

