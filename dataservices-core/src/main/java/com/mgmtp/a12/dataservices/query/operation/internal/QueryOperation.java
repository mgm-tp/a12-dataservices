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
package com.mgmtp.a12.dataservices.query.operation.internal;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.query.QueryService;
import com.mgmtp.a12.dataservices.query.operation.events.QueryAfterOperationEvent;
import com.mgmtp.a12.dataservices.query.operation.events.QueryBeforeOperationEvent;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.request.internal.QueryPagingHelper;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.rpc.query.PagedResultSet;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * RPC operation for all requests that use the Data Services Query API.
 *
 */
@Slf4j
@RemoteOperation(isMutation = false, name = CoreOperationConstants.QUERY_OPERATION, group = CoreOperationConstants.A12_INTERNAL_OPERATIONS_GROUP)
@RequiredArgsConstructor
@Component public class QueryOperation {

	private final Anonymizer anonymizer;
	private final QueryService queryService;
	private final ApplicationEventPublisher applicationEventPublisher;

	/**
	 * Executes the query operation to fetch document tree results based on the provided query parameters.
	 *
	 * @param query The query parameters for fetching document results.
	 * @return The result set of document tree results.
	 */
	@Transactional(readOnly = true)
	public <T> PagedResultSet<T> rpc(@NonNull @JsonRpcParam("query") QueryRoot query) {
		log.debug("{} called with parameters [{}]",
			CoreOperationConstants.QUERY_OPERATION,
			anonymizer.apply(query.toString()));

		StopWatch stopWatch = StopWatch.createStarted();

		QueryRoot modifiedQuery = publishBeforeEvent(query);
		HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		PagedResultSet<T> pagedResultSet =
			QueryPagingHelper.pageToResultSet(queryService.query(modifiedQuery, httpRequest.getHeader(HttpHeaders.ACCEPT_LANGUAGE)),
				modifiedQuery.isExclude());

		log.debug("{} documents have been loaded in [{} ms]. Total number of matched documents {}", pagedResultSet.getEntries().size(),
			stopWatch.getTime(TimeUnit.MILLISECONDS), pagedResultSet.getFullSize());
		return publishAfterEvent(modifiedQuery, pagedResultSet);
	}

	private QueryRoot publishBeforeEvent(QueryRoot query) {
		QueryBeforeOperationEvent beforeEvent = new QueryBeforeOperationEvent(query);
		applicationEventPublisher.publishEvent(beforeEvent);
		return beforeEvent.getQuery();
	}

	private <T> PagedResultSet<T> publishAfterEvent(QueryRoot query, PagedResultSet<T> pagedResultSet) {
		QueryAfterOperationEvent<T> afterEvent = new QueryAfterOperationEvent<>(query, pagedResultSet);
		applicationEventPublisher.publishEvent(afterEvent);
		return afterEvent.getPagedResultSet();
	}
}
