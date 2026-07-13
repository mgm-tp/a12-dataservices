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
package com.mgmtp.a12.dataservices.query;

import com.mgmtp.a12.dataservices.internal.DocumentationDiagram;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * The LinkAware interface defines the contract for objects that reference or operate on linked entities
 * within a relationship model. It provides methods to retrieve metadata and configure behavior associated
 * with such links.
 *
 * Responsibilities of implementations:
 *
 * - Define relationships between entities using a specified relationship model.
 * - Specify logical constraints on linked documents or their relationships.
 * - Configure recursion depth for self-referencing links.
 * - Indicate whether the relationship is aggregated or ordered.
 *
 * Core concepts:
 *
 * - The relationship model serves as the basis for defining links between entities.
 * - Target role identifies the specific role of the linked entity within the relationship.
 * - Constraints can be applied to the linked documents themselves or the links connecting them.
 * - Aggregation and ordering determine how linked entities are processed and structured.
 */
@DocumentationDiagram
@OnlyForUsage public interface LinkAware {

	/**
	 * Retrieves the name of the relationship model defining the link between entities.
	 *
	 * @return the name of the relationship model as a String
	 */
	String getRelationshipModel();

	/**
	 * Retrieves the role of the linked entity within the defined relationship model.
	 *
	 * @return the target role as a String
	 */
	String getTargetRole();

	/**
	 * Retrieves the logical constraint applied to the linked document.
	 * The constraint represents a logic operator that defines how the linked entities
	 * are filtered or conditioned within the relationship model. This can be used to
	 * specify detailed query conditions on the links or linked documents.
	 *
	 * @return an instance of `ILogicOperator` representing the logical constraint
	 *         for the linked document
	 */
	ILogicOperator getLinkDocumentConstraint();

	/**
	 * Retrieves the maximum recursion depth for self-referencing links within the relationship model.
	 *
	 * @return the maximum depth as an Integer, or null if there is no constraint on recursion depth
	 */
	Integer getMaxDepth();

	/**
	 * Indicates whether the current relationship or link is aggregated.
	 *
	 * @return true if the relationship or link is aggregated, false otherwise
	 */
	boolean isAggregated();

	/**
	 * Retrieves whether the relationship or link is ordered within the underlying model.
	 *
	 * @return true if the relationship is ordered; false if it is unordered; null if it is undefined
	 */
	Boolean getOrdered();
}
