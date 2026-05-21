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
package com.mgmtp.a12.dataservices.query.events;

import org.springframework.data.domain.Page;

import com.mgmtp.a12.dataservices.common.events.internal.EventDocumentation;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import lombok.Data;

/**
 * The {@link com.mgmtp.a12.dataservices.query.QueryService} publishes this event to allow bypassing a call to the
 * {@link com.mgmtp.a12.dataservices.query.QueryRepository}. This enables listeners to provide alternative means to resolve the query.
 *
 * Event listeners must provide results in their implementation because {@link com.mgmtp.a12.dataservices.query.QueryRepository} is never called.
 */
@Data
@EventDocumentation
public class QueryBeforeExecutionPhaseEvent {

	private QueryRoot query;
	private QueryContext queryContext;
	private Page<DocumentTreeResult> results;

	/**
	 * Creates a new event for a query before its execution phase.
	 *
	 * @param query the query to be executed; never null.
	 * @param queryContext the context holding request-scoped data for execution; never null.
	 */
	public QueryBeforeExecutionPhaseEvent(QueryRoot query, QueryContext queryContext) {
		this.query = query;
		this.queryContext = queryContext;
	}
}

