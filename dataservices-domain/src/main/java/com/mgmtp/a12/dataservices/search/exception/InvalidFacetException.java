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
package com.mgmtp.a12.dataservices.search.exception;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.document.search.facets.request.AbstractFacetQuery;
import com.mgmtp.a12.dataservices.document.search.facets.request.UnknownFacetQuery;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;

/**
 * @deprecated The functionality was only used in Solr-based implementations and is no longer supported.
 * This exception and its descendants represent errors in facets data being passed to application.
 */
@SuppressWarnings({"removal"})
@Deprecated(since = "38.1.0", forRemoval = true)
public class InvalidFacetException extends BaseException {

	/**
	 * Creates an exception indicating invalid facet input with a custom message.
	 *
	 * @param message human-readable description of the facet error; never null.
	 */
	public InvalidFacetException(String message) {
		super(ExceptionCodes.INVALID_FACET_EXCEPTION_CODE, ExceptionKeys.INVALID_FACET_ERROR_KEY, message);
	}

	/**
	 * Creates an exception for a facet with an unknown or unsupported type.
	 *
	 * @param unknownFacetQuery the facet query with unsupported type; never null.
	 */
	public InvalidFacetException(UnknownFacetQuery unknownFacetQuery) {
		super(ExceptionCodes.INVALID_FACET_EXCEPTION_CODE, ExceptionKeys.INVALID_FACET_ERROR_KEY,
			String.format("Type of facet with id: %s is not supported. Supported types: %s",
			unknownFacetQuery.getType(), Arrays.stream(AbstractFacetQuery.FacetType.values())
				.map(AbstractFacetQuery.FacetType::getValue)
				.collect(Collectors.joining(", "))));
	}

	/**
	 * Creates a specialized invalid facet exception with explicit error code and key.
	 *
	 * @param code the application-specific error code.
	 * @param key the localization key identifying the error message.
	 * @param message the human-readable message explaining the error.
	 * @param e the underlying cause; may be null.
	 */
	protected InvalidFacetException(int code, String key, String message, Throwable e) {
		super(code, key, message, e);
	}
}

