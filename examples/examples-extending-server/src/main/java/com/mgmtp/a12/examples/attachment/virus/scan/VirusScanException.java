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
package com.mgmtp.a12.examples.attachment.virus.scan;

import tools.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.common.exception.BaseException;

/**
 * Indicates that there is an error while scanning the file.
 */
public class VirusScanException extends BaseException {
	/**
	 * RPC-like error code representing a virus scan failure (-32060).
	 */
	public static final int EXCEPTION_CODE = -32060;

	/**
	 * Constructs a new exception with the given localization key and {@link VirusScanResult}.
	 *
	 * @param key localization key used for resolving the message; must not be null.
	 * @param result structured result of the virus scan, serialized into the message body; must not be null.
	 * @param priority {@link com.mgmtp.a12.dataservices.common.exception.BaseException.MessagePriority} indicating UI relevance.
	 */
	public VirusScanException(String key, VirusScanResult result, MessagePriority priority) {
		this(key, toMessage(result), priority);
	}

	/**
	 * Constructs a new exception with the given localization key and plain message.
	 *
	 * @param key localization key used for resolving the message; must not be null.
	 * @param message human-readable message; may be null if only the key resolves the text.
	 * @param priority {@link com.mgmtp.a12.dataservices.common.exception.BaseException.MessagePriority} indicating UI relevance.
	 */
	public VirusScanException(String key, String message, MessagePriority priority) {
		super(EXCEPTION_CODE, key, message, priority);
	}

	private static String toMessage(VirusScanResult result) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.writeValueAsString(result);
		} catch (Exception exception) {
			return result.getResult();
		}
	}
}
