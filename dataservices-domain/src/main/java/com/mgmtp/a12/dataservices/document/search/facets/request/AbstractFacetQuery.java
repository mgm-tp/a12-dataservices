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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.document.search.facets.request.AbstractFacetQuery.FacetType.AVG_CONSTANT;
import static com.mgmtp.a12.dataservices.document.search.facets.request.AbstractFacetQuery.FacetType.MAX_CONSTANT;
import static com.mgmtp.a12.dataservices.document.search.facets.request.AbstractFacetQuery.FacetType.MIN_CONSTANT;
import static com.mgmtp.a12.dataservices.document.search.facets.request.AbstractFacetQuery.FacetType.RANGE_CONSTANT;
import static com.mgmtp.a12.dataservices.document.search.facets.request.AbstractFacetQuery.FacetType.SUM_CONSTANT;
import static com.mgmtp.a12.dataservices.document.search.facets.request.AbstractFacetQuery.FacetType.TERM_CONSTANT;

/**
 * Base type for facet queries in the document search domain.
 *
 * @deprecated The functionality was only used in Solr-based implementations and is no longer supported.
 */
@SuppressWarnings({"removal"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = UnknownFacetQuery.class)
@JsonSubTypes({
	@JsonSubTypes.Type(value = SumFacetQuery.class, name = SUM_CONSTANT),
	@JsonSubTypes.Type(value = AvgFacetQuery.class, name = AVG_CONSTANT),
	@JsonSubTypes.Type(value = MinFacetQuery.class, name = MIN_CONSTANT),
	@JsonSubTypes.Type(value = MaxFacetQuery.class, name = MAX_CONSTANT),
	@JsonSubTypes.Type(value = TermFacetQuery.class, name = TERM_CONSTANT),
	@JsonSubTypes.Type(value = RangeFacetQuery.class, name = RANGE_CONSTANT),
})
@Data
@AllArgsConstructor
@Deprecated(since = "38.1.0", forRemoval = true)
public abstract class AbstractFacetQuery {

	/**
	 * Id of the facet which will produce corresponding entry in the response.
	 */
	private final String id;

	private final FacetType type;

	private final String field;

	/**
	 * Enumerates supported facet types used in the deprecated Solr-based facet API.
	 */
	@RequiredArgsConstructor
	public enum FacetType {
		SUM(FacetType.SUM_CONSTANT),
		MIN(FacetType.MIN_CONSTANT),
		MAX(FacetType.MAX_CONSTANT),
		AVG(FacetType.AVG_CONSTANT),
		TERM(FacetType.TERM_CONSTANT),
		RANGE(FacetType.RANGE_CONSTANT);

		@Getter
		private final String value;

		/** Constant type name for sum facets. */
		public static final String SUM_CONSTANT = "sum";
		/** Constant type name for min facets. */
		public static final String MIN_CONSTANT = "min";
		/** Constant type name for max facets. */
		public static final String MAX_CONSTANT = "max";
		/** Constant type name for average facets. */
		public static final String AVG_CONSTANT = "avg";
		/** Constant type name for term facets. */
		public static final String TERM_CONSTANT = "term";
		/** Constant type name for range facets. */
		public static final String RANGE_CONSTANT = "range";
	}
}
