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

import lombok.Data;

import java.util.List;
/**
 * Facet result consisting of multiple buckets including pagination metadata.
 *
 * @deprecated The functionality was only used in Solr-based implementations and is no longer supported.
 */
@SuppressWarnings({"removal"})
@Deprecated(since = "38.1.0", forRemoval = true)
@Data public class BucketedFacetResult implements IFacetResult {

	private final List<BucketContent> buckets;
	private final long offset;
	private final long limit;
	private final long fullSize;

	/**
	 * Constructs a bucketed facet result.
	 *
	 * @param buckets Bucket entries. May be null; an empty list is applied.
	 * @param offset Zero-based starting offset of returned buckets.
	 * @param limit Maximum number of buckets returned.
	 * @param fullSize Total number of buckets available server-side (unpaged).
	 */
	public BucketedFacetResult(List<BucketContent> buckets, long offset, long limit, long fullSize) {
		this.buckets = buckets == null ? List.of() : buckets;
		this.offset = offset;
		this.limit = limit;
		this.fullSize = fullSize;
	}

	/**
	 * Creates an empty bucketed facet result.
	 *
	 * @param offset Zero-based starting offset to report.
	 * @param limit Maximum number of buckets to report.
	 * @return An empty result with `fullSize = 0`.
	 */
	public static BucketedFacetResult empty(int offset, int limit) {
		return new BucketedFacetResult(List.of(), offset, limit, 0);
	}
}
