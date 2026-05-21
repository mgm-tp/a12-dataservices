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
package com.mgmtp.a12.dataservices.client.exception;


import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.common.exception.ErrorLevel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Main A12 client exception type. It's marker type for every implementation specific exception.
 * Implementation will usually provide more details about the error with more specific exception which 
 * will be subclass of the {@link A12ClientException} exception.    
 *
 */
@EqualsAndHashCode(callSuper = true) @NoArgsConstructor @Data
public abstract class A12ClientException extends RuntimeException {

	private LocalizedEntry longMessage;
	private LocalizedEntry shortMessage;
	private String anonymityMessage;
	private ErrorLevel errorLevel = ErrorLevel.ERROR;
	private ErrorDetail errorDetail;

	/**
	 * Constructs a new client exception with an optional message and detail payload.
	 *
	 * @param message Human-readable description of the error; may be `null`.
	 * @param errorDetail Implementation-specific diagnostic details; may be `null`.
	 */
	public A12ClientException(String message, ErrorDetail errorDetail) {
		super(message);
		this.errorDetail = errorDetail;
	}
}
