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
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Base type for facet queries that can contain nested sub-facets and pagination hints.
 *
 * @deprecated The functionality was only used in Solr-based implementations and is no longer supported.
 */
@SuppressWarnings({"removal"})
@Getter @Setter @EqualsAndHashCode(callSuper = true)
@Deprecated(since = "38.1.0", forRemoval = true)
public abstract class AbstractNestableFacetQuery extends AbstractFacetQuery {

	/** Default page offset used when none is provided. */
	protected static final Integer DEFAULT_OFFSET = 0;
	/** Sentinel value indicating that offset pagination is unsupported for the facet type (-1). */
	public static final Integer OFFSET_NOT_SUPPORTED = -1;
	/** Default maximum number of buckets returned when limit is not provided. */
	protected static final Integer DEFAULT_LIMIT = 10;
	/** Sentinel value indicating that limit is not supported. */
	public static final Integer LIMIT_NOT_SUPPORTED = 10;
	/** Minimum count threshold used to include a bucket in results. */
	public static final Integer DEFAULT_MIN_COUNT = 1;

	protected Integer offset;
	protected Integer limit;
	protected Collection<? extends AbstractFacetQuery> facets;

	/**
	 * Creates a nestable facet query with explicit pagination settings.
	 *
	 * @param id Facet identifier; produces a corresponding entry in the response. May be null only if the consumer tolerates unnamed facets.
	 * @param field Index field to aggregate on. Must reference a valid document field; may be null depending on facet type.
	 * @param facets Nested facet queries to compute per bucket. May be null; an empty list is applied.
	 * @param offset Zero-based starting offset of buckets to return. May be null; defaults to {@link #DEFAULT_OFFSET}.
	 * @param limit Maximum number of buckets to return. May be null; defaults to {@link #DEFAULT_LIMIT}.
	 * @param type Facet type discriminator used for polymorphic serialization.
	 */
	@JsonCreator protected AbstractNestableFacetQuery(@JsonProperty("id") String id, @JsonProperty("field") String field, @JsonProperty("facets") Collection<? extends AbstractFacetQuery> facets,
		@JsonProperty("offset") Integer offset, @JsonProperty("limit") Integer limit, @JsonProperty("type") FacetType type) {
		super(id, type, field);
		this.offset = offset == null ? DEFAULT_OFFSET : offset;
		this.limit = limit == null ? DEFAULT_LIMIT : limit;
		this.facets = facets == null ? List.of() : facets;
	}

	/**
	 * Creates a nestable facet query without pagination support.
	 *
	 * @param id Facet identifier; produces a corresponding entry in the response. May be null only if the consumer tolerates unnamed facets.
	 * @param field Index field to aggregate on. Must reference a valid document field; may be null depending on facet type.
	 * @param facets Nested facet queries to compute per bucket. May be null; an empty list is applied.
	 * @param type Facet type discriminator used for polymorphic serialization.
	 */
	@JsonCreator protected AbstractNestableFacetQuery(@JsonProperty("id") String id, @JsonProperty("field") String field, @JsonProperty("facets") Collection<? extends AbstractFacetQuery> facets,
		@JsonProperty("type") FacetType type) {
		super(id, type, field);
		this.offset = OFFSET_NOT_SUPPORTED;
		this.limit = LIMIT_NOT_SUPPORTED;
		this.facets = facets == null ? List.of() : facets;
	}

}
