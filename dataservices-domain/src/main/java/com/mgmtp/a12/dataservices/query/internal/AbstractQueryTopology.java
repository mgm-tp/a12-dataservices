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
package com.mgmtp.a12.dataservices.query.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.mgmtp.a12.dataservices.internal.DocumentationDiagram;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.fields.aggregation.AggregationProjector;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.query.topology.QueryTopology;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Abstract parent common for {@link QueryRoot} and {@link QueryLink}.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY) @DocumentationDiagram
@Data @AllArgsConstructor(access = AccessLevel.PROTECTED) @NoArgsConstructor(access = AccessLevel.PROTECTED) @SuperBuilder(toBuilder = true)
public abstract class AbstractQueryTopology implements QueryTopology {

	/**
	 * Fields of the document displayed in the projection. It's mutually exclusive with {@link #aggregation}.
	 */
	@JsonPropertyDescription("Fields of the document displayed in the projection. It's mutually exclusive with aggregation.")
	@Setter private List<String> fields;

	/**
	 * Fields of the link document displayed in the projection. It's mutually exclusive with {@link #aggregation}.
	 */
	@JsonPropertyDescription("Fields of the link document displayed in the projection. It's mutually exclusive with aggregation.")
	@Setter private List<String> linkDocumentFields;

	/**
	 * Aggregations applied in the projection. It's mutually exclusive with {@link #fields}.
	 */
	@JsonPropertyDescription("Aggregations applied in the projection. It's mutually exclusive with fields, linkDocumentFields, and links.")
	@Setter private AggregationProjector aggregation;

	/**
	 * Additional documents linked to the current one.
	 */
	@JsonPropertyDescription("Additional documents linked to the current one.")
	private Collection<QueryLink> links;

	@JsonPropertyDescription("The constraint applied to the query. This is a logical operator that defines the conditions for the query.")
	private ILogicOperator constraint;

	/**
	 * Temporary ID used to pair query and response.
	 *
	 * @see DocumentTreeResult#getBackReference()
	 */
	@JsonPropertyDescription("Temporary ID used to pair query and response. See DocumentTreeResult.getBackReference()")
	private String backReference;

	/**
	 * For internal use only, to pair query part to the result. See {@link DocumentTreeResult#internalId}.
	 */
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	private final UUID internalId = UUID.randomUUID();

	/**
	 * Exclude selected documents from the projection. Use this document just as a hidden link to other documents.
	 */
	@JsonPropertyDescription("Exclude documents from the projection. In effect, this document is used just as a hidden link to other documents.")
	private Boolean exclude;

	public abstract static class AbstractQueryTopologyBuilder<C extends AbstractQueryTopology, B extends AbstractQueryTopologyBuilder<C, B>> {

		public B aggregation(AggregationProjector aggregation) {
			this.aggregation = aggregation;
			return self();
		}

		public B field(String field) {
			if (this.fields == null) {
				this.fields = new ArrayList<>(List.of(field));
			} else {
				this.fields.add(field);
			}
			return self();
		}

		public B link(QueryLink link) {
			if (links == null) {
				links = new ArrayList<>();
			}
			links.add(link);
			return self();
		}
	}

	@JsonIgnore
	@Override public boolean isAggregated() {
		return getAggregation() != null;
	}

	@Override public boolean isExclude() {
		return getExclude() != null && getExclude();
	}
}
