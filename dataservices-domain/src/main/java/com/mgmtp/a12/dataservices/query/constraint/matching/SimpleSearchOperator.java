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
package com.mgmtp.a12.dataservices.query.constraint.matching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.mgmtp.a12.dataservices.query.annotation.QueryOperator;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.ValueAware;
import com.mgmtp.a12.dataservices.query.constraint.ValuesAware;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Simple multi-field search operator that evaluates a single value or a list of values
 * across the provided fields. If no fields are specified, all indexed document fields are considered.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonClassDescription("This operator performs a simple search across multiple fields. " +
	"It allows for searching a single value or multiple values across specified fields.")
@Data @NoArgsConstructor @EqualsAndHashCode @ToString(callSuper = true) @SuperBuilder
@QueryOperator("simple_search") public class SimpleSearchOperator implements ValueAware<String>, ValuesAware<String>, ILogicOperator {

	@JsonPropertyDescription("The value to be searched for in the specified fields. Either this or 'values' must be provided.")
	private String value;
	@JsonPropertyDescription("The list of values to be searched for in the specified fields. Either this or 'value' must be provided.")
	private List<String> values;

	@JsonPropertyDescription("A list of fields in which the search will be performed. " +
		"If not provided, the search will be performed in all indexed fields of the document.")
	@Builder.Default
	private List<String> fields = new ArrayList<>();
	@Builder.Default
	@JsonIgnore Map<String, String> fieldsTypes = new HashMap<>();

	// TODO: A12S-5373: We can remove this field once we enable the new search configuration based on ts_vector
	/**
	 * Specifies how search terms are combined in the query.
	 * AND requires all terms to match; OR matches if any term is present.
	 */
	@Getter @RequiredArgsConstructor public enum TermJoinType {
		AND(" AND "),
		OR(" OR ");

		private final String value;
	}
}
