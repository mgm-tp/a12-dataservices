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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @deprecated The class is based on the Solr API which is no longer supported. Use {@link QueryRoot} instead.
 */
@Data @NoArgsConstructor @AllArgsConstructor(access = AccessLevel.PACKAGE) @Builder(toBuilder = true)
@Deprecated(since = "38.1.0", forRemoval = true)
public class FilterSpec implements Serializable {

	private String fulltext;
	@Builder.Default()
	private List<String> filters = new ArrayList<>();
	private String lang;

	/**
	 * Adds a filter expression to this specification.
	 * @param filter a Solr-style filter string.
	 * @return this spec for fluent chaining.
	 */
	public FilterSpec addFilter(String filter) {
		if (filters == null) {
			filters = new ArrayList<>();
		}
		filters.add(filter);
		return this;
	}

	public static class FilterSpecBuilder {
		private List<String> filters$value = new ArrayList<>();
		private boolean filters$set;

		/**
		 * Adds a filter expression to the builder.
		 * @param filter a Solr-style filter string.
		 * @return this builder for fluent chaining.
		 */
		public FilterSpecBuilder filter(String filter) {
			if (filters$value == null) {
				filters$value = new ArrayList<>();
			}
			filters$value.add(filter);
			filters$set = true;
			return this;
		}
	}
}
