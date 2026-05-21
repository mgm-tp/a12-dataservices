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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper for paged result sets of the QueryService.
 * @param <T> the item type contained in the result lists (e.g., {@link DocumentTreeResult} or a domain DTO).
 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder(toBuilder = true)
public class PagedResultSet<T> {

	private long fullSize;
	private Paging page;
	/**
	 * If the result items are of type {@link DocumentTreeResult}, the entries list contains only documents of type ROOT.
	 * Otherwise, all result items are put into this list.
	 */
	private List<T> entries;
	/**
	 * If the result items are of type {@link DocumentTreeResult}, the entries list contains only documents of type LINK or CHILD.
	 * Otherwise, this list remains empty.
	 */
	private List<T> links;

	private Map<String, Object> otherResults;

	/**
	 * Creates an empty result set for the given page specification.
	 * @param page the requested paging information; must not be null.
	 * @param <T> the item type for the empty result.
	 * @return a `PagedResultSet` with zero size, empty entries and links, and an empty `otherResults` map.
	 */
	public static <T> PagedResultSet<T> empty(Paging page) {
		return PagedResultSet.<T>builder()
			.fullSize(0)
			.page(page)
			.entries(List.of())
			.links(List.of())
			.otherResults(new HashMap<>())
			.build();
	}
}
