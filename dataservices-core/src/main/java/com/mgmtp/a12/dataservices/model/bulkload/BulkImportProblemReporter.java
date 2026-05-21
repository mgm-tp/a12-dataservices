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
package com.mgmtp.a12.dataservices.model.bulkload;

import java.util.Objects;
import java.util.stream.Collectors;

import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.common.exception.BaseError;
import com.mgmtp.a12.dataservices.common.exception.ErrorDetail;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.utils.internal.AbstractListProblemReporter;
import com.mgmtp.a12.dataservices.utils.internal.ProblemFormatter;

import lombok.extern.slf4j.Slf4j;

/**
 * Bulk Import Problem Reporter class. Reports problems during bulk import of models.
 *
 */
@Slf4j
public class BulkImportProblemReporter extends AbstractListProblemReporter<BaseError> {

	/**
	 * Separator used to join multiple problem messages into a single error string.
	 */
	// TODO A12S-5063 change it to private
	public static final String PROBLEM_SEPARATOR = ",\n";

	/**
	 * Reports an exception that occurred during bulk import as a problem entry.
	 *
	 * @param e The exception to report; may be null, in which case a generic error is recorded.
	 */
	public void reportProblem(Exception e) {
		log.warn("Error while importing models", e);
		super.reportProblem(e instanceof BaseError baseError ? baseError : createBaseError(e.getMessage()));
	}

	/**
	 * Validates the collected problems and throws an aggregated exception if any exist.
	 *
	 * @throws ModelBulkImportException if one or more problems were collected during the import.
	 */
	public void validate() {
		if (hasProblems()) {
			throw new ModelBulkImportException(getProblems(), getProblems().stream()
				.map(BaseError::getLongMessage)
				.filter(Objects::nonNull)
				.map(LocalizedEntry::getDefaultMessage)
				.filter(Objects::nonNull)
				.collect(Collectors.joining(PROBLEM_SEPARATOR))).withAnonymityMessage("Bulk import validation failed.");
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return A formatter that stringifies {@link BaseError} instances.
	 */
	@Override public ProblemFormatter<BaseError> getFormatter() {
		return Object::toString;
	}

	private static BaseError createBaseError(String exceptionMessage) {
		LocalizedEntry localizedEntry = new LocalizedEntry(ExceptionKeys.MODEL_BULK_IMPORT_GENERIC_ERROR_KEY, exceptionMessage);
		
		return new BaseError() {
			@Override public LocalizedEntry getShortMessage() {
				return localizedEntry;
			}

			@Override public LocalizedEntry getLongMessage() {
				return localizedEntry;
			}

			@Override public ErrorDetail getErrorDetail() {
				return null;
			}
		};
	}
}
