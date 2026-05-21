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
package com.mgmtp.a12.dataservices.exception.query;

import com.mgmtp.a12.dataservices.exception.ExceptionKeys;

import static com.mgmtp.a12.dataservices.exception.ExceptionCodes.QUERY_INDEX_ERROR_CODE;

/**
 * Indicates that an error occurred during indexing-related processing.
 * Use for failures while preparing or updating index structures.
 */
public class QueryIndexingException extends QueryException {

	/**
	 * Creates a QueryIndexingException with a message.
	 *
	 * @param executionPhase phase of query processing when the error is detected.
	 * @param message human-readable description; may be null.
	 */
	public QueryIndexingException(ExceptionKeys.ExecutionPhase executionPhase, String message) {
		super(executionPhase, QUERY_INDEX_ERROR_CODE, message);
	}

	/**
	 * Creates a QueryIndexingException with a message and cause.
	 *
	 * @param executionPhase phase of query processing when the error is detected.
	 * @param message human-readable description; may be null.
	 * @param cause underlying cause; may be null.
	 */
	public QueryIndexingException(ExceptionKeys.ExecutionPhase executionPhase, String message, Throwable cause) {
		super(executionPhase, QUERY_INDEX_ERROR_CODE, message, cause);
	}

	/**
	 * Creates a QueryIndexingException with an explicit indexing error code.
	 *
	 * @param executionPhase phase of query processing when the error is detected.
	 * @param queryIndexErrorCode numeric error code specific to indexing.
	 * @param msg human-readable description; may be null.
	 * @param e underlying cause; may be null.
	 */
	public QueryIndexingException(ExceptionKeys.ExecutionPhase executionPhase, int queryIndexErrorCode, String msg, Exception e) {
		super(executionPhase, queryIndexErrorCode, msg, e);
	}
}

