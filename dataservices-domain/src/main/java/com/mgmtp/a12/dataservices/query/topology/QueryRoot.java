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
package com.mgmtp.a12.dataservices.query.topology;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.exception.query.QueryJsonParsingException;
import com.mgmtp.a12.dataservices.internal.DocumentationDiagram;
import com.mgmtp.a12.dataservices.query.Order;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.TargetDocumentModelAware;
import com.mgmtp.a12.dataservices.query.internal.AbstractQueryTopology;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_GENERAL;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.QUERY_INVALID_INPUT_ERROR_KEY;

/**
 * The QueryRoot class is the primary structure for defining queries in the system.
 * It extends {@link AbstractQueryTopology} and implements {@link QueryTopology} as well
 * as {@link TargetDocumentModelAware}. This class is utilized to represent
 * and manage query-related configurations and behaviors.
 *
 * Key functionalities include:
 *
 * - Definition of the target paging behavior using the `paging` property.
 * - Specification of the sort order via the `sort` property, which is a list of {@link Order}.
 * - Handling projection details through the `projectionName`.
 * - Associating the query with a specific target document model via `targetDocumentModel`.
 *
 * Additional features such as linking with other document models, setting
 * constraints, and managing field visibility are inherited from the parent class.
 *
 * This class supports serialization and deserialization of query structures
 * and can include non-empty elements when serialized to JSON.
 */

@DocumentationDiagram
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonClassDescription("This is the root of a query topology. It defines the structure and behavior of a query, including paging, sorting, projection, and target document model.")
@Data @NoArgsConstructor(access = AccessLevel.PRIVATE) @SuperBuilder(toBuilder = true) @EqualsAndHashCode(callSuper = true)
public class QueryRoot extends AbstractQueryTopology implements QueryTopology, TargetDocumentModelAware {

	@JsonProperty(required = true)
	@JsonPropertyDescription("Paging information for the query, defining how many results to return and from which offset.")
	private Paging paging;

	@JsonPropertyDescription("Sort order for the query results. This is a list of Order objects that define the sorting criteria.")
	private List<Order> sort;

	@JsonProperty(required = true)
	@JsonPropertyDescription("The name of the projection to be applied to the query results. This defines how the results will be structured.")
	private String projectionName;

	@JsonProperty(required = true)
	@JsonPropertyDescription("The name of the target document model for this query. This specifies which document model the query is targeting.")
	private String targetDocumentModel;

	/**
	 * Returns a structured String representation of this query root, including projection, fields,
	 * aggregation, constraint, links, sorting, and paging details. JSON parts are pretty-printed.
	 *
	 * @return a human-readable representation of the query configuration.
	 * @throws QueryJsonParsingException if JSON serialization of parts fails during logging.
	 */
	@Override public String toString() {
		try {
			return "[documentModelName=%s, projection=%s, fields=%s, aggregation=%s, filter=%s, links=%s, sort=%s, page=%s]".formatted(
				getTargetDocumentModel(),
				getProjectionName(),
				getFields() != null ? new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(getFields()) : "",
				getAggregation() != null ? new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(getAggregation()) : "",
				getConstraint() != null ? new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(getConstraint()) : "",
				getLinks() != null ? new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(getLinks()) : "",
				sort != null ? sort.toString() : "",
				paging != null ? paging.toString() : "");
		} catch (JsonProcessingException e) {
			throw new QueryJsonParsingException(QUERY_GENERAL, QUERY_INVALID_INPUT_ERROR_KEY, null, e)
				.withAnonymityMessage("Could not process JSON when logging query");
		}
	}
}

