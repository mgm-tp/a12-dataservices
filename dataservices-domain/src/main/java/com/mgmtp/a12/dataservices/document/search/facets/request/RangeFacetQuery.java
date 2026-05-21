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
import lombok.Getter;
import lombok.Setter;

/**
 * Facet query that partitions values into contiguous ranges.
 *
 * @deprecated The functionality was only used in Solr-based implementations and is no longer supported.
 */
@SuppressWarnings({"removal"})
@Deprecated(since = "38.1.0", forRemoval = true)
@Getter @Setter @EqualsAndHashCode(callSuper = true, of = {})
public class RangeFacetQuery extends AbstractNestableFacetQuery {

	private Object start;
	private Object end;
	private Object gap;

	private Boolean hardEnd = false;
	private String other;

	private RangeType rangeType;

	/**
	 * Creates a range facet query.
	 *
	 * @param id Facet identifier; produces a corresponding entry in the response.
	 * @param field Index field to partition into ranges.
	 * @param facets Nested facet queries to compute per range. May be null; an empty list is applied.
	 * @param start Inclusive lower bound of the first range. May be null if derived implicitly.
	 * @param end Exclusive upper bound of the last range. May be null if derived implicitly.
	 * @param gap Step size between consecutive ranges (numeric or date duration depending on {@link RangeType}). May be null if not required.
	 */
	@JsonCreator public RangeFacetQuery(@JsonProperty("id") String id, @JsonProperty("field") String field,
		@JsonProperty("facets") Collection<? extends AbstractFacetQuery> facets,
		@JsonProperty("start") Object start, @JsonProperty("end") Object end, @JsonProperty("gap") Object gap) {
		super(id, field, facets, FacetType.RANGE);
		this.start = start;
		this.end = end;
		this.gap = gap;
	}

	/**
	 * Type of range values used when computing range facets.
	 */
	public enum RangeType {
		NUMBER,
		DATE
	}
}
