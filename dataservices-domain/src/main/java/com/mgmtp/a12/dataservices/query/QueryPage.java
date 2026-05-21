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
package com.mgmtp.a12.dataservices.query;

import java.util.List;
import java.util.Map;

import com.mgmtp.a12.dataservices.query.internal.page.QueryPageImpl;

/**
 * Query page interface. Used in Query Services to return paged results.
 *
 */
public interface QueryPage<T> {

	/**
	 * Returns additional result data that accompanies the page content.
	 * Keys and values are domain-specific and may be empty if no extra data is provided.
	 *
	 * @return a map of other results associated with the page; never null.
	 */
	Map<String, Object> getOtherResults();

	/**
	 * Returns the total amount of elements.
	 *
	 * @return the total amount of elements
	 */
	long getTotalElements();

	/**
	 * Returns the number of the current page. Is always non-negative.
	 *
	 * @return the number of the current page.
	 */
	int getNumber();

	/**
	 * Returns the size of the page.
	 *
	 * @return the size of the page.
	 */
	int getSize();

	/**
	 * Returns the page content as {@link List}.
	 *
	 * @return List of elements in current page.
	 */
	List<T> getContent();

	/**
	 * A static convenient method to retrieve an empty `QueryPage`.
	 * @return `QueryPageImpl` with empty setup result.
	 */
	static <T> QueryPage<T> empty() {
		return QueryPageImpl.empty();
	}

	/**
	 * Main entry point to create instance of `QueryPage`
	 * @param content is list of elements to be set for this instance.
	 * @param totalElements total queried records.
	 * @param number current page number.
	 * @param size number of elements per page.
	 * @param otherResults contains map of other results.
	 * @return instance of `QueryPage`
	 */
	static <T> QueryPage<T> of(List<T> content, long totalElements, int number, int size, Map<String, Object> otherResults) {
		return new QueryPageImpl<>(content, totalElements, number, size, otherResults);
	}
}

