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
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mgmtp.a12.dataservices.common.LocalizedEntry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Error detail class.
 *
 */
@Data @ToString @NoArgsConstructor @AllArgsConstructor @Builder
public class ErrorDetail implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int RPC_ERROR_EXCEPTION_CODE = -32035;

	public static final LocalizedEntry SHORT_GENERIC = new LocalizedEntry("error.generic.short", "Generic Operation Error");
	public static final LocalizedEntry LONG_GENERIC = new LocalizedEntry("error.generic.long", "Operation failed to execute. See error details.");

	private String code;
	private String subsystem;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	private OffsetDateTime time;

	public ErrorDetail(int code) {
		this.code = String.valueOf(code);
		this.subsystem = "GENERIC";
		this.time = OffsetDateTime.now();
	}

	public ErrorDetail(int code, String subsystem, OffsetDateTime offsetDateTime) {
		this.code = String.valueOf(code);
		this.subsystem = subsystem;
		this.time = offsetDateTime;
	}

	public static ErrorDetail createGenericError() {
		return new ErrorDetail(String.valueOf(RPC_ERROR_EXCEPTION_CODE), "GENERIC", OffsetDateTime.now());
	}
}
