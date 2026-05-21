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
package com.mgmtp.a12.dataservices.query.fields.aggregation;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.mgmtp.a12.dataservices.internal.DocumentationDiagram;
import com.mgmtp.a12.dataservices.query.topology.QueryTopology;
import com.mgmtp.a12.dataservices.query.fields.ProjectionField;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A query can contain either list of fields or this `AggregationProjector`. Both are mutually exclusive.
 *
 * @see QueryTopology#getAggregation()
 */
@DocumentationDiagram
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonClassDescription("This is the wrapper property to contain all aggregations.")
@Getter @Builder @AllArgsConstructor @NoArgsConstructor @EqualsAndHashCode
public class AggregationProjector {

	/**
	 * A fake docRef which represents the not-persisted aggregated data structure, and therefore is not supposed to exist in the database.
	 */
	private String docRef;

	/**
	 * List of aggregations to be presented.
	 */
	@JsonPropertyDescription("The list of aggregation functions.")
	@Builder.Default
	private List<IAggregationFunction> aggregations = new ArrayList<>();

	/**
	 * List of fields to group by.
	 */
	@JsonPropertyDescription("The list of fields that make up the group the aggregation functions should be applied on.")
	@Builder.Default
	private List<ProjectionField> group = new ArrayList<>();

	public static class AggregationProjectorBuilder {

		private List<IAggregationFunction> aggregations$value = new ArrayList<>();
		private boolean aggregations$set;

		private List<ProjectionField> group$value = new ArrayList<>();
		private boolean group$set;

		/**
		 * Adds an aggregation function to this builder.
		 *
		 * @param filter the aggregation function to add; must not be null.
		 * @return this builder for method chaining.
		 */
		public AggregationProjectorBuilder aggregation(IAggregationFunction filter) {
			if (aggregations$value == null) {
				aggregations$value = new ArrayList<>();
			}
			aggregations$value.add(filter);
			aggregations$set = true;
			return this;
		}

		/**
		 * Adds a grouping field that defines the grouping key.
		 *
		 * @param groupingField the field to use for grouping; must not be null.
		 * @return this builder for method chaining.
		 */
		public AggregationProjectorBuilder groupingField(ProjectionField groupingField) {
			if (group$value == null) {
				group$value = new ArrayList<>();
			}
			group$value.add(groupingField);
			group$set = true;
			return this;
		}
	}
}
