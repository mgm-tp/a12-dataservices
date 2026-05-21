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
package com.mgmtp.a12.dataservices.utils.internal;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.mgmtp.a12.dataservices.common.exception.BaseException;

/**
 * This exception and its descendants represent errors when (de)serializing documents.
 */
public class DataServicesDocumentProblemReporterException extends BaseException {

	private transient AbstractListProblemReporter<?> iProblemReporter;

	public DataServicesDocumentProblemReporterException(int code, String key, AbstractListProblemReporter<?> pr, String updateMessage) {
		this(code, key, updateMessage.isEmpty() ? pr.toString() : updateMessage.concat(". " + pr.toString()), pr);
	}

	public DataServicesDocumentProblemReporterException(int code, String key, String message, AbstractListProblemReporter<?> pr) {
		super(code, key, message);
		iProblemReporter = pr;
	}

	public DataServicesDocumentProblemReporterException(int code, String key, String message) {
		super(code, key, message);
	}

	public DataServicesDocumentProblemReporterException(int errorCode, String errorKey, AbstractListProblemReporter<?> pr) {
		this(errorCode, errorKey, pr.toString(), pr);
	}

	@SuppressWarnings("unchecked") // As in the Java the Exception couldn't be generalize, we have to handle it this unclear way.
	public <T> List<T> getProblems() {
		return Objects.nonNull(iProblemReporter) ?  (List<T>) iProblemReporter.getProblems() : Collections.emptyList();
	}

}
