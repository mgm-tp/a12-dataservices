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
package com.mgmtp.a12.dataservices.query.internal.page;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mgmtp.a12.dataservices.query.QueryPage;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter @EqualsAndHashCode
public class QueryPageImpl<T> implements QueryPage<T> {

	private final long totalElements;
	private final int number;
	private final int size;
	private final List<T> content;
	private Map<String, Object> otherResults = new LinkedHashMap<>();

	public QueryPageImpl(final long totalElements, final int number, final int size, final List<T> content) {
		this.totalElements = totalElements;
		this.number = number;
		this.size = size;
		this.content = content;
	}

	public QueryPageImpl(final List<T> content, long totalElements, int number, int size, Map<String, Object> otherResults) {
		this.content = content;
		this.totalElements = totalElements;
		this.number = number;
		this.size = size;
		this.otherResults = otherResults;
	}

	public static <T> QueryPageImpl<T> empty() {
		return new QueryPageImpl<>(0,0,1, new ArrayList<>());
	}
}
