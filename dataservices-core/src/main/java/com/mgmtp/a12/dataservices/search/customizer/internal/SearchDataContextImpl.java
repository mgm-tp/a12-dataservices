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
package com.mgmtp.a12.dataservices.search.customizer.internal;

import org.jetbrains.annotations.NotNull;

import com.mgmtp.a12.dataservices.search.customizer.SearchDataContext;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import static com.mgmtp.a12.dataservices.model.ModelConstants.FIELD_SEPARATOR;

@Getter @AllArgsConstructor
public class SearchDataContextImpl implements SearchDataContext {

	private final IDocumentModelSearchService documentModelSearchService;
	private final DocumentV2 indexableDocument;
	private final String modelName;
	@NonNull private String searchData;

	@Override public String getCurrentSearchData() {
		return searchData;
	}

	@Override public void appendToSearchData(String additionalData) {
		if (additionalData != null && !additionalData.isEmpty()) {
			this.searchData += sanitizeAdditionalSearchData(additionalData);
		}
	}

	@NotNull private String sanitizeAdditionalSearchData(String additionalData) {
		// Remove leading slash from additionalData if searchData ends with slash
		if (this.searchData.endsWith(FIELD_SEPARATOR) && additionalData.startsWith(FIELD_SEPARATOR)) {
			additionalData = additionalData.substring(1);
		}

		// Ensure the result ends with slash
		if (!additionalData.endsWith(FIELD_SEPARATOR)) {
			additionalData += FIELD_SEPARATOR;
		}

		return additionalData;
	}

	@Override public void replaceSearchData(String newSearchData) {
		this.searchData = newSearchData + (newSearchData.endsWith(FIELD_SEPARATOR) ? "" : FIELD_SEPARATOR);
	}
}
