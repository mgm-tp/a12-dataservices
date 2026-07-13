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
package com.mgmtp.a12.dataservices.model.exception;

import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.UNKNOWN_ERROR_KEY;

/**
 * This exception and its descendants represent errors when serializing document models.
 */
public class DocumentModelSerializationException extends BaseException {

	/**
	 * Creates an exception indicating that the given model could not be serialized.
	 *
	 * @param modelId the identifier of the model that failed to serialize; never null.
	 * @param cause the underlying cause of the failure; may be null if unavailable.
	 */
	public DocumentModelSerializationException(String modelId, Throwable cause) {
		super(ExceptionCodes.DOCUMENT_MODEL_SERIALIZATION_EXCEPTION_CODE, formatMessage(modelId), cause);
		setLongMessage(new LocalizedEntry(UNKNOWN_ERROR_KEY, formatMessage(modelId)));
		setShortMessage(new LocalizedEntry(UNKNOWN_ERROR_KEY, formatMessage(modelId)));
	}

	/**
	 * Creates a specialized serialization exception with explicit error code and key.
	 *
	 * @param code the application-specific error code.
	 * @param key the localization key identifying the error message.
	 * @param message the human-readable message explaining the error.
	 * @param e the underlying cause; may be null.
	 */
	protected DocumentModelSerializationException(int code, String key, String message, Throwable e) {
		super(code, key, message, e);
	}

	private static String formatMessage(String modelId) {
		return "Unable to serialize model %s".formatted(modelId);
	}
}

