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
package com.mgmtp.a12.dataservices.server.internal.rest;

import java.util.Iterator;
import java.util.Optional;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.QueryService;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.server.uaa.SecuredController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_VALIDATION;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.QUERY_INVALID_INPUT_ERROR_KEY;
import static com.mgmtp.a12.dataservices.query.projection.internal.DocumentProjectionImplementation.PROJECTION_NAME;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Endpoint to load aggregations.
 *
 * @title Query Aggregations REST API
 * @topic Query API
 */
@RequestMapping("#{@dataServicesCoreProperties.server.contextPath}/aggregation")
@RequiredArgsConstructor
@SecuredController
@RestController public class AggregationController {

	private final QueryService queryService;
	private final DataServicesCoreProperties dataServicesCoreProperties;

	/**
	 * Returns aggregated values as a 2-dim object array with the values of the group by columns first, and the aggregated values behind them.
	 * The number of returned rows is controlled by configuration.
	 *
	 * Example:
	 *
	 * [source,json]
	 * ----
	 * [
	 *   ["Household", 1, 50000.0],
	 *   ["Liability", 1, 1000000.0],
	 *   ["Travel", 3, 1350000.0]
	 * ]
	 * ----
	 *
	 * @title Load Aggregations
	 * @param queryRoot A query that contains aggregations. No links and no paging are allowed.
	 * @return The values of the group by fields, and the aggregated values as a 2-dimensional array.
	 */
	@PostMapping(consumes = APPLICATION_JSON_VALUE)
	public Object[][] loadAggregations(@RequestBody QueryRoot queryRoot) {

		validate(queryRoot);
		HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		QueryPage<DocumentTreeResult> queryResult = queryService.query(queryRoot, httpRequest.getHeader(ACCEPT_LANGUAGE));

		return mapQueryResult(queryResult, queryRoot.getAggregation().getGroup().size(), queryRoot.getAggregation().getAggregations().size());
	}

	private void validate(@NonNull QueryRoot queryRoot) {
		if (Optional.of(queryRoot)
			.map(QueryRoot::getLinks)
			.isPresent()) {
			throw new QueryInvalidInputException(QUERY_VALIDATION, QUERY_INVALID_INPUT_ERROR_KEY, null)
				.withAnonymityMessage("Links are not allowed for aggregation queries");
		}
		if (Optional.of(queryRoot)
			.map(QueryRoot::getExclude)
			.filter(ex -> ex.equals(Boolean.TRUE))
			.isPresent()) {
			throw new QueryInvalidInputException(QUERY_VALIDATION, QUERY_INVALID_INPUT_ERROR_KEY, null)
				.withAnonymityMessage("Aggregation queries must not have 'exclude=true'");
		}
		if (Optional.of(queryRoot)
			.map(QueryRoot::getBackReference)
			.isPresent()) {
			throw new QueryInvalidInputException(QUERY_VALIDATION, QUERY_INVALID_INPUT_ERROR_KEY, null)
				.withAnonymityMessage("BackReference is not allowed for aggregation queries");
		}
		if (Optional.of(queryRoot)
			.map(QueryRoot::getAggregation)
			.isEmpty()) {
			throw new QueryInvalidInputException(QUERY_VALIDATION, QUERY_INVALID_INPUT_ERROR_KEY, null)
				.withAnonymityMessage("Aggregation must not be null");
		}

		queryRoot.setProjectionName(PROJECTION_NAME);
		queryRoot.setPaging(Paging.builder()
				.pageNumber(0)
				.pageSize(dataServicesCoreProperties.getQuery().getAggregation().getListSize())
				.build());
	}

	private static Object[][] mapQueryResult(QueryPage<?> queryResult, int groupSize, int aggregationSize) {
		Object[][] resultArray = new Object[queryResult.getContent().size()][groupSize + aggregationSize];
		int row = 0;
		for (Object aggregationResult : queryResult.getContent().stream().toList()) {
			if (aggregationResult instanceof DocumentTreeResult docTreeResult) {
				JsonNode document = docTreeResult.getDocument();
				if (document instanceof ArrayNode docArray) {
					Iterator<JsonNode> docIterator = docArray.iterator();
					int col = 0;
					while (docIterator.hasNext()) {
						resultArray[row][col++] = docIterator.next();
					}
				}
			}
			row++;
		}
		return resultArray;
	}
}
