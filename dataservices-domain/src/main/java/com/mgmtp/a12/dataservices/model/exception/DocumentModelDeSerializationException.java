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

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.DOCUMENT_MODEL_DESERIALIZATION_ERROR_KEY;

/**
 * -
 * This exception and its descendants represent errors when deserializing document models.
 */
public class DocumentModelDeSerializationException extends BaseException {

	/**
	 * Creates an exception indicating that the given model could not be deserialized with a custom localization key.
	 *
	 * @param key the localization key identifying the error message; never null.
	 * @param modelId the identifier of the model that failed to deserialize; never null.
	 * @param cause the underlying cause of the failure; may be null if unavailable.
	 */
	public DocumentModelDeSerializationException(String key, String modelId, Throwable cause) {
		super(ExceptionCodes.DOCUMENT_MODEL_DE_SERIALIZATION_EXCEPTION_CODE, key, formatMessage(modelId, cause.getMessage()), cause);
	}

	/**
	 * Creates an exception indicating that the given model could not be deserialized.
	 *
	 * @param modelId the identifier of the model that failed to deserialize; never null.
	 * @param cause the underlying cause of the failure; may be null if unavailable.
	 */
	public DocumentModelDeSerializationException(String modelId, Throwable cause) {
		super(ExceptionCodes.DOCUMENT_MODEL_DE_SERIALIZATION_EXCEPTION_CODE, DOCUMENT_MODEL_DESERIALIZATION_ERROR_KEY,
			formatMessage(modelId, cause.getMessage()), cause);
	}

	/**
	 * Creates a specialized deserialization exception with explicit error code and key.
	 *
	 * @param code the application-specific error code.
	 * @param key the localization key identifying the error message.
	 * @param message the human-readable message explaining the error.
	 * @param e the underlying cause; may be null.
	 */
	protected DocumentModelDeSerializationException(int code, String key, String message, Throwable e) {
		super(code, key, message, e);
	}

	/**
	 * Creates a deserialization exception with details taken from the given exception.
	 *
	 * @param ex the underlying exception used to derive message details; never null.
	 */
	public DocumentModelDeSerializationException(Exception ex) {
		super(ExceptionCodes.DOCUMENT_MODEL_DE_SERIALIZATION_EXCEPTION_CODE, DOCUMENT_MODEL_DESERIALIZATION_ERROR_KEY, ex.getMessage(), ex);
	}

	private static String formatMessage(String modelId, String message) {
		StringBuilder sb = new StringBuilder("Error while deserializing model");
		if (StringUtils.isNotBlank(modelId)) {
			sb.append(" ").append(modelId);
		}
		sb.append(".");
		if (StringUtils.isNotBlank(message)) {
			sb.append(" ").append(message);
		}
		return sb.toString();
	}
}

