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
package com.mgmtp.a12.dataservices.rpc.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mgmtp.a12.dataservices.document.search.facets.response.IFacetResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * Legacy container for query results returned by the deprecated RPC API.

 * @param <T> the item type contained in the `entries` list.
 * @deprecated The API has been replaced by the new Query API.
 */
@SuppressWarnings({"removal"})
@Data @AllArgsConstructor(access = AccessLevel.PRIVATE) @NoArgsConstructor @Builder(toBuilder = true)
@Deprecated(since = "38.1.0", forRemoval = true)
public class ResultSet<T> {

	private Long fullSize;
	private PageSpec page;
	private List<T> entries;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Map<String, IFacetResult> facets;

	/**
	 * Creates a result set with the given content and page specification.
	 * @param fullSize the total number of matching records across all pages.
	 * @param page the legacy paging settings associated with this result set.
	 * @param entries the items included on the current page; may be empty but never `null`.
	 */
	public ResultSet(Long fullSize, PageSpec page, List<T> entries) {
		this.fullSize = fullSize;
		this.page = page;
		this.entries = entries;
	}

	/**
	 * Creates an empty result set for the given page.
	 * @param page the legacy paging settings.
	 * @param <T> the item type parameter for the empty result.
	 * @return a `ResultSet` with size `0` and empty `entries`.
	 */
	public static <T> ResultSet<T> empty(PageSpec page) {
		return ResultSet.<T>builder()
			.fullSize(0L)
			.page(page)
			.entries(List.of())
			.build();
	}
}
