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

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Base type for statistical facet queries (sum, min, max, avg) that may specify precision.
 *
 * @deprecated The functionality was only used in Solr-based implementations and is no longer supported.
 */
@SuppressWarnings({"removal"})
@Getter @EqualsAndHashCode(callSuper = true)
@Deprecated(since = "38.1.0", forRemoval = true)
public abstract class AbstractStatsFacetQuery extends AbstractFacetQuery {

	private final Integer precision;

	/**
	 * Constructs a statistical facet query.
	 *
	 * @param id Facet identifier; produces a corresponding entry in the response. May be null only if the consumer tolerates unnamed facets.
	 * @param type Specific statistical facet type (e.g., {@link FacetType#SUM}, {@link FacetType#AVG}).
	 * @param field Index field to aggregate on. Must reference a valid document field; may be null depending on facet type.
	 * @param precision Decimal precision to apply when formatting numeric results. May be null to use default precision.
	 */
	protected AbstractStatsFacetQuery(String id, FacetType type, String field, Integer precision) {
		super(id, type, field);
		this.precision = precision;
	}
}
