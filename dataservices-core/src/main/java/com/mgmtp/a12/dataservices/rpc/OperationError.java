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
package com.mgmtp.a12.dataservices.rpc;

import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.common.exception.BaseError;
import com.mgmtp.a12.dataservices.common.exception.ErrorDetail;
import com.mgmtp.a12.dataservices.common.exception.ErrorLevel;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Operation Error class. Class for details of the exception that occurred during the operation.
 *
 */
@NoArgsConstructor @Data @ToString @Builder @AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OperationError implements BaseError {

	private int code;
	private String operationId;
	private ErrorLevel level;
	private LocalizedEntry shortMessage;
	private LocalizedEntry longMessage;
	private ErrorDetail errorDetail;

	/**
	 * Sets the operation id and returns this instance for fluent chaining.
	 *
	 * @param id The operation identifier; may be `null`.
	 * @return This {@link OperationError} instance.
	 */
	public OperationError withOperationId(String id) {
		setOperationId(id);
		return this;
	}

	/**
	 * Builder class for {@link OperationError}.
	 */
	public static class OperationErrorBuilder {

		/**
		 * Sets generic short and long messages on the builder.
		 *
		 * @return This builder for fluent chaining.
		 */
		public OperationErrorBuilder genericMessage() {
			shortMessage(ErrorDetail.SHORT_GENERIC);
			longMessage(ErrorDetail.LONG_GENERIC);
			return this;
		}
	}
}
