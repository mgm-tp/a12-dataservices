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
package com.mgmtp.a12.dataservices.common.exception;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mgmtp.a12.dataservices.common.LocalizedEntry;

/**
 * Base error payload interface exposed by Data Services.
 * Provides structured, serializable error metadata for clients and logs.
 */
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE,
	getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE,
	setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public interface BaseError extends Serializable {

	/**
	 * Returns the severity level associated with this error payload.
	 *
	 * @return The {@link ErrorLevel}, never null.
	 */
	@JsonProperty default ErrorLevel getLevel() {
		return ErrorLevel.ERROR;
	}

	/**
	 * Returns a short, human-readable error message intended for compact UI display.
	 *
	 * @return A {@link LocalizedEntry} containing the localization key and English default text; never null.
	 */
	@JsonProperty LocalizedEntry getShortMessage();

	/**
	 * Returns a long, human-readable error message intended for detailed UI display.
	 *
	 * @return A {@link LocalizedEntry} containing the localization key and English default text; never null.
	 */
	@JsonProperty LocalizedEntry getLongMessage();

	/**
	 * Returns structured error metadata for diagnostics and client processing.
	 *
	 * @return {@link ErrorDetail} including error code, subsystem, and timestamp; never null.
	 */
	@JsonProperty ErrorDetail getErrorDetail();
}
