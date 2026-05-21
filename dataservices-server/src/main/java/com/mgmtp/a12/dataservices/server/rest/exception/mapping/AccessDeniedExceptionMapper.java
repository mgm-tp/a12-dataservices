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
package com.mgmtp.a12.dataservices.server.rest.exception.mapping;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.common.exception.ErrorLevel;
import com.mgmtp.a12.dataservices.common.exception.mapping.GenericThrowableMapper;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.rpc.OperationError;

/**
 * Maps {@link AccessDeniedException} and its subtypes to response.
 */
public class AccessDeniedExceptionMapper extends GenericThrowableMapper<AccessDeniedException> {

	/**
	 * {@inheritDoc}
	 *
	 * Produces a {@link OperationError} with forbidden semantics and localized messages.
	 *
	 * @param exception the thrown {@link AccessDeniedException}; never null.
	 */
	@Override public Object getEntity(AccessDeniedException exception) {
		String message = "User is not allowed to perform requested operation";
		String key = ExceptionKeys.SECURITY_NOT_AUTHORIZED_ERROR_KEY;
		LocalizedEntry shortMessageObject = new LocalizedEntry(key + ".title", message);
		LocalizedEntry longMessageObject = new LocalizedEntry(key + ".description", message);
		return OperationError.builder()
			.operationId("N/A")
			.level(ErrorLevel.ERROR)
			.shortMessage(shortMessageObject)
			.longMessage(longMessageObject)
			.build();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Maps unauthorized access to {@link HttpStatus#FORBIDDEN}.
	 *
	 * @param exception the thrown {@link AccessDeniedException}; never null.
	 * @return forbidden status indicating insufficient permissions.
	 */
	@Override public HttpStatus getHttpStatus(AccessDeniedException exception) {
		return HttpStatus.FORBIDDEN;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Suppresses stack trace logging for authorization failures.
	 *
	 * @param exception the thrown {@link AccessDeniedException}; never null.
	 * @return `false` to reduce noise for expected security checks.
	 */
	@Override public boolean shouldLogStackTrace(AccessDeniedException exception) {
		return false;
	}
}
