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

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.mgmtp.a12.dataservices.internal.DocumentationDiagram;
import com.mgmtp.a12.dataservices.query.ConstraintAware;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.fields.aggregation.AggregationProjector;

/**
 * Represents the topology of a query structure, serving as a backbone for query design,
 * handling constraints, links, aggregation, and fields. This interface is implemented by different
 * query components and acts as a unifying abstraction.
 *
 * Key features include:
 *
 * - Managing constraints applied to documents.
 * - Handling fields for document selection or projection.
 * - Supporting document aggregation via `AggregationProjector`.
 * - Linking additional documents using query links.
 * - Allowing temporary metadata, such as back references and internal UUIDs, for internal operations.
 * - Enabling exclusion of documents from the visible projection while keeping them as connections.
 *
 * Implementations often form part of hierarchical query structures, such as `QueryRoot`
 * and `QueryLink`, to build complex queries combining selection and projection logic.
 *
 * This interface extends {@link ConstraintAware} to inherit capabilities of applying logic operators
 * as constraints during query execution.
 */
@DocumentationDiagram
public interface QueryTopology extends ConstraintAware {

	/**
	 * Determines whether the current query topology involves aggregation.
	 *
	 * @return true if the query topology is aggregated, false otherwise.
	 */
	 boolean isAggregated();

	/**
	 * Checks whether documents should be excluded from the visible projection of the query at this level.
	 *
	 * @return true if documents are to be excluded from the visible projection while retaining them as connections; false otherwise.
	 */
	boolean isExclude();

	/**
	 * Retrieves the list of field names associated with the query topology.
	 * The fields represent the elements to be selected or projected in a query execution.
	 *
	 * @return a list of field names, or an empty list if no fields are defined.
	 */
	List<String> getFields();

	/**
	 * Retrieves the list of link document field names associated with the query topology.
	 * The fields represent the elements to be selected or projected in a query execution.
	 *
	 * @return a list of field names, or an empty list if no fields are defined.
	 */
	List<String> getLinkDocumentFields();

	/**
	 * Retrieves the aggregation projector associated with the query topology.
	 * The aggregation projector contains details of the aggregation structure, such as group-by fields
	 * and aggregation functions, and is mutually exclusive with the list of fields for projection.
	 *
	 * @return an instance of `AggregationProjector` if the query topology is configured for aggregation,
	 *         or `null` if the query does not involve aggregation.
	 */
	AggregationProjector getAggregation();

	/**
	 * Retrieves the collection of query links associated with the query topology.
	 * Query links represent connections to additional documents that are part of the overall query structure.
	 *
	 * @return a collection of `QueryLink` instances, or an empty collection if no links are defined.
	 */
	Collection<QueryLink> getLinks();

	/**
	 * Retrieves the logical constraint associated with the query topology.
	 * The constraint represents the filtering or conditional logic applied during query execution.
	 *
	 * @return an instance of `ILogicOperator` representing the logical constraint,
	 *         or `null` if no constraint is defined.
	 */
	ILogicOperator getConstraint();

	/**
	 * Retrieves the back-reference of the query topology.
	 * The back-reference represents a client's reference to a related part of the query.
	 *
	 * @return a string representing the back-reference, or null if no back-reference is defined.
	 */
	String getBackReference();

	/**
	 * Retrieves the internal unique identifier associated with the query topology.
	 * This is for internal use only to connect the query with IQueryProjection#postprocess.
	 *
	 * @return a `UUID` representing the internal identifier of the query topology.
	 */
	UUID getInternalId();

	/**
	 * Determines whether documents should be excluded from the visible projection of the query at this level.
	 *
	 * @return true if documents are to be excluded from the visible projection while retaining them as connections; false otherwise.
	 */
	Boolean getExclude();

	/**
	 * Sets the collection of query links associated with the query topology.
	 * Query links represent connections to additional documents that form
	 * part of the broader query structure.
	 *
	 * @param links the collection of `QueryLink` instances to be associated
	 *              with the query topology.
	 */
	void setLinks(Collection<QueryLink> links);

	/**
	 * Sets the logical constraint associated with the query topology.
	 * The constraint defines the filtering or conditional logic
	 * to be applied during query execution.
	 *
	 * @param constraint an instance of `ILogicOperator` representing
	 *                   the logical constraint to be set.
	 */
	void setConstraint(ILogicOperator constraint);

	/**
	 * Sets the back-reference for the query topology.
	 * The back-reference is a string representation used as a client's reference
	 * to a related part of the query.
	 *
	 * @param backReference the back-reference to set; can be `null` if no back-reference is defined
	 */
	void setBackReference(String backReference);

	/**
	 * Sets whether documents should be excluded from the visible projection of the query at this level.
	 *
	 * @param exclude a `Boolean` value indicating whether documents are to be excluded
	 *                from the visible projection. If `true`, documents are excluded from
	 *                visible projection while retaining them as connections;
	 *                if `false`, documents are included in the visible projection.
	 */
	void setExclude(Boolean exclude);

	/**
	 * Sets the list of field names associated with the query topology.
	 * These fields represent the elements to be selected or projected in a query execution.
	 *
	 * @param fields a list of field names to be associated with the query topology;
	 *               can be empty or null if no fields are provided.
	 */
	void setFields(List<String> fields);

	/**
	 * Sets the aggregation projector for the query topology.
	 * The aggregation projector defines the structure and configuration for data aggregation,
	 * including grouping fields and aggregation functions.
	 *
	 * @param aggregation the aggregation projector to be associated with the query topology.
	 *                    This parameter is mutually exclusive with the list of projection fields.
	 */
	void setAggregation(AggregationProjector aggregation);
}
