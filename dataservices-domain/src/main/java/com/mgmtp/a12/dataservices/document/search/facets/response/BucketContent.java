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
package com.mgmtp.a12.dataservices.document.search.facets.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;
/**
 * Represents a bucket entry with its value, document count, and optional nested facet results.
 *
 * @deprecated The functionality was only used in Solr-based implementations and is no longer supported.
 */
@SuppressWarnings({"removal"})
@Data @JsonInclude(JsonInclude.Include.NON_EMPTY)
@Deprecated(since = "38.1.0", forRemoval = true)
public class BucketContent {

	private final Object value;
	private final long count;
	private final Map<String, IFacetResult> facets;

	/**
	 * Constructs a bucket entry with nested facet results.
	 *
	 * @param value Bucket value (term, range boundary, etc.). May be null depending on facet type.
	 * @param count Number of matching documents in this bucket.
	 * @param facets Nested facet results per facet id. May be null; an empty map is applied.
	 */
	public BucketContent(Object value, long count, Map<String, IFacetResult> facets) {
		this.value = value;
		this.count = count;
		this.facets = facets == null ? Map.of() : facets;
	}

	/**
	 * Constructs a bucket entry without nested facet results.
	 *
	 * @param value Bucket value (term, range boundary, etc.). May be null depending on facet type.
	 * @param count Number of matching documents in this bucket.
	 */
	public BucketContent(Object value, long count) {
		this.value = value;
		this.count = count;
		this.facets = Map.of();
	}
}
