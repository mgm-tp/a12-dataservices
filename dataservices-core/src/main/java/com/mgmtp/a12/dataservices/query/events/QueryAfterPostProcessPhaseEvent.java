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

import com.mgmtp.a12.dataservices.common.events.internal.EventDocumentation;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.operation.events.QueryAfterOperationEvent;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * The QueryAfterPostProcessPhaseEvent is published after the query has been executed and post-processed. Results are available in the {@link QueryPage} and can be modified by the event listeners.
 * This event should be used if the query results need to be modified in all system not just in the JSON-RPC operation like {@link QueryAfterOperationEvent} which is only used in the JSON-RPC operation.
 *
 * @param <T> type for the results is dependent on the projection selected.
 */
@Data
@EventDocumentation
@AllArgsConstructor
public class QueryAfterPostProcessPhaseEvent<T> {

	private QueryPage<T> results;
	private QueryRoot queryRoot;
	private String locale;
}
