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
package com.mgmtp.a12.dataservices.document.search.facets.request;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;

/**
 * Facet query that groups results by distinct terms of a field and supports nested sub-facets.
 *
 * @deprecated The functionality was only used in Solr-based implementations and is no longer supported.
 */
@SuppressWarnings({"removal"})
@Deprecated(since = "38.1.0", forRemoval = true)
@EqualsAndHashCode(callSuper = true, of = {})
public class TermFacetQuery extends AbstractNestableFacetQuery {

	/** Sorts buckets by count by default. */
	public static final Boolean DEFAULT_SORT_BY_COUNT = true;
	/** Includes buckets for missing values by default. */
	public static final Boolean DEFAULT_MISSING = false;
	/** Uses descending sort order by default. */
	public static final Boolean DEFAULT_SORT_DESC = true;
	/** Includes a union bucket aggregating all terms by default. */
	public static final Boolean DEFAULT_INCLUDE_UNION_BUCKET = true;
	/** Includes the total number of buckets in the response by default. */
	public static final Boolean DEFAULT_INCLUDE_TOTAL_NUM_BUCKETS = true;

	/**
	 * Creates a `term` facet query.
	 *
	 * @param id Facet identifier; produces a corresponding entry in the response.
	 * @param field Index field whose distinct terms are bucketed.
	 * @param facets Nested facet queries to compute per term. May be null; an empty list is applied.
	 * @param offset Zero-based starting bucket offset. May be null; defaults to {@link AbstractNestableFacetQuery#DEFAULT_OFFSET}.
	 * @param limit Maximum number of buckets to return. May be null; defaults to {@link AbstractNestableFacetQuery#DEFAULT_LIMIT}.
	 */
	@JsonCreator public TermFacetQuery(@JsonProperty("id") String id, @JsonProperty("field") String field, @JsonProperty("facets") Collection<? extends AbstractFacetQuery> facets,
		@JsonProperty("offset") Integer offset, @JsonProperty("limit") Integer limit) {
		super(id, field, facets, offset, limit, FacetType.TERM);
	}

}
