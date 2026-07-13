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
package com.mgmtp.a12.dataservices.query.enrichment.internal;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

import lombok.NoArgsConstructor;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_ENRICHMENT;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.QUERY_UNSUPPORTED_LOCALE_ERROR_KEY;

/**
 * Helper that validates and normalizes the query locale against the locales declared on the
 * target document model.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
final class LocaleEnrichmentHelper {

	/**
	 * Returns the enriched locale for `documentModelName`. Throws
	 * {@link QueryInvalidInputException} when the locale is not supported by the document model.
	 */
	public static String determineEnrichedLocale(QueryContext context, String documentModelName) {
		IDocumentModel documentModel = context.getDocumentModel(documentModelName);
		String locale = context.getLocale();
		if (!StringUtils.isBlank(locale) &&
			documentModel.getHeader().getLocales().stream()
				.map(Locale::toString)
				.noneMatch(locale::equalsIgnoreCase)) {
			throw new QueryInvalidInputException(QUERY_ENRICHMENT, QUERY_UNSUPPORTED_LOCALE_ERROR_KEY,
				"Unable to construct query for unsupported locale: %s".formatted(locale));
		}
		return StringUtils.isBlank(locale) ? null : locale;
	}
}
