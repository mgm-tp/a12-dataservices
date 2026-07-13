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
package com.mgmtp.a12.dataservices.query.enrichment.internal;

import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;

/**
 * Extension point for enriching individual query operators during the enrichment phase.
 *
 * Implementations are called by `QueryAPIOperatorWalker` for every operator in the constraint tree.
 * An enricher inspects the operator type and populates the `QueryContext` enrichments accordingly.
 * Returning `true` signals that the enricher handled this operator; returning `false` signals
 * that the operator was not applicable. The walker always calls all registered enrichers
 * regardless of the return value.
 *
 * Register a custom implementation as a Spring bean to extend the enrichment phase.
 */
public interface IQueryAPIOperatorEnricher {

	/**
	 * Enriches the given operator using data from the provided context.
	 *
	 * @param operator the operator to enrich; never `null`.
	 * @param context the query context providing models, enrichments, and caches; never `null`.
	 * @return `true` if this enricher handled the operator, `false` if not applicable.
	 */
	boolean enrich(ILogicOperator operator, QueryContext context);
}
