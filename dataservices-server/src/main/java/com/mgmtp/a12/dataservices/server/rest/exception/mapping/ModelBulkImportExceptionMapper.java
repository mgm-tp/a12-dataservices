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

import com.mgmtp.a12.dataservices.common.exception.mapping.BaseExceptionMapper;
import com.mgmtp.a12.dataservices.model.bulkload.ModelBulkImportException;

/**
 * Maps {@link ModelBulkImportException} to response.
 */
public class ModelBulkImportExceptionMapper extends BaseExceptionMapper<ModelBulkImportException> {

	/**
	 * {@inheritDoc}
	 *
	 * Exposes the collection of import errors as the response body.
	 *
	 * @param exception a {@link ModelBulkImportException}; never null.
	 */
	@Override public Object getEntity(ModelBulkImportException exception) {
		return exception.getErrors();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Maps bulk import validation failures to {@link HttpStatus#BAD_REQUEST}.
	 *
	 * @param exception a {@link ModelBulkImportException}; never null.
	 * @return client error status indicating import issues.
	 */
	@Override public HttpStatus getHttpStatus(ModelBulkImportException exception) {
		return HttpStatus.BAD_REQUEST;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Suppresses stack trace logging for import validation failures.
	 *
	 * @param exception a {@link ModelBulkImportException}; never null.
	 * @return `false` to avoid excessive logging for user input errors.
	 */
	@Override public boolean shouldLogStackTrace(ModelBulkImportException exception) {
		return false;
	}
}
