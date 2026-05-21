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

import java.util.Collection;

import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

/**
 * The query service is used to execute a query secure way. Validations, enrichments, and projections are applied to the input query
 */
public interface QueryService {

	/**
	 * Execute the query with all validations, projections, permission checks, and context enrichments.
	 * After preprocessing of the query it calls {@link QueryRepository#query(QueryRoot, Collection, QueryContext)} to execute the processed query.
	 *
	 * IMPORTANT: When using `QueryService` and the `locale` parameter is not provided or is `null`,
	 * enumeration fields in the query will be processed by their value, not by their localized text representation.
	 *
	 * @param queryRoot the query root
	 * @param locale the locale used in localized parts of the query execution
	 * @param <T> projected item type selected by the projection
	 * @return the page of document tree results
	 *
	 * @authorizationScope Query
	 * @authorizationResource Documentmodel-Id
	 */
	<T> QueryPage<T> query(QueryRoot queryRoot, String locale);
}

