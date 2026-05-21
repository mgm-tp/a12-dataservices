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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.mgmtp.a12.dataservices.internal.DocumentationDiagram;
import com.mgmtp.a12.dataservices.query.LinkAware;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.internal.AbstractQueryTopology;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Represents a query component corresponding to a link within a query hierarchy.
 * A `QueryLink` encapsulates the relationship and constraints between the current
 * query component and its linked entities, enabling flexible and recursive query designs.
 *
 * Key responsibilities:
 *
 * - Supports linking documents through a predefined relationship model.
 * - Facilitates constraints and projections on linked documents.
 * - Manages recursion in self-referencing links through depth control.
 * - Configurable for ordered relationships and link sizes.
 *
 * Inherits capabilities from {@link AbstractQueryTopology}, such as managing fields, constraints,
 * and aggregation for the current query node. Implements {@link QueryTopology} and {@link LinkAware}
 * to expose link-specific operations and topology-related behavior.
 *
 * Core fields:
 *
 * - `relationshipModel`: Specifies the model defining the relationship between documents.
 * - `targetRole`: Represents the role of the linked document in the relationship.
 * - `linkDocumentConstraint`: Defines the logical constraints applied to the linked document.
 * - `linkDocumentFields`: Indicates fields selected or projected from the linked document.
 * - `maxDepth`: Configures the maximum depth for recursive self-referencing links.
 * - `maxLinksSize`: Limits the size of the linked documents to retrieve.
 * - `ordered`: Internally used to indicate whether the relationship is ordered.
 */
@DocumentationDiagram
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonClassDescription("This is a query link topology. It defines the structure and behavior of a link in a query, including the relationship model, " +
	"target role, constraints, and depth control.")
@Data @AllArgsConstructor(access = AccessLevel.PRIVATE) @NoArgsConstructor(access = AccessLevel.PRIVATE) @SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class QueryLink extends AbstractQueryTopology implements QueryTopology, LinkAware {

	@JsonPropertyDescription("Relationship model defining the link between documents.")
	private String relationshipModel;
	@JsonPropertyDescription("Role of the linked document in the relationship.")
	private String targetRole;
	@JsonPropertyDescription("Logical constraints applied to the linked document.")
	private ILogicOperator linkDocumentConstraint;
	@JsonPropertyDescription("Fields selected from the linked document.")
	private List<String> linkDocumentFields;
	@JsonPropertyDescription("Maximum depth for recursive self-referencing links.")
	private Integer maxDepth;
	@JsonPropertyDescription("Maximum number of linked documents to retrieve.")
	private Integer maxLinksSize;

	/**
	 * This is used internally to get hold of the ordered property of the underlying relationship model.
	 */
	@JsonIgnore
	@Builder.Default
	private Boolean ordered = false;
}
