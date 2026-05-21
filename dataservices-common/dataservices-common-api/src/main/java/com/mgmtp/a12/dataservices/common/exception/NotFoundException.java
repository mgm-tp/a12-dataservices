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


/**
 * This exception and its descendants represent errors when information cannot be found in application.
 */
public class NotFoundException extends BaseException {
	public static final int NOT_FOUND_EXCEPTION_CODE = -32004;

	public NotFoundException(String message) {
		super(NOT_FOUND_EXCEPTION_CODE, message);
	}

	public NotFoundException(String key, String message){
		super(NOT_FOUND_EXCEPTION_CODE, key, message);
	}

	public NotFoundException(String message, Exception ex) {
		super(NOT_FOUND_EXCEPTION_CODE, message, ex);
	}

	protected NotFoundException(int code, String key, String message, Throwable e) {
		super(code, key, message, null, e);
	}
}
