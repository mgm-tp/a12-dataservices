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

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;

import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.enrichement.Enrichments;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.query.validation.IQueryOperatorValidator;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import lombok.NonNull;

/**
 * The QueryContext interface provides methods to facilitate querying and retrieval of data models
 * and their relationships within a localized request context. It includes mechanisms for caching
 * models and services to enhance performance and ensures access to only the models permitted
 * for the current user.
 */
public interface QueryContext {

	/**
	 * Get the original query root that was used to create this context. preprocess of the projection might change the query.
	 * To know during later phases which query was originally used, this method provides access to the original query.
	 *
	 * @return the original query root
	 */
	QueryRoot getOriginalQuery();

	/**
	 * Get the document model for the given document ID from context's cache.
	 * If a particular model was already used in the context, this method speeds up retrieving of the model.
	 * Context instance is considered to be local for a particular request,
	 * so there are only models the current user has a right to read it.
	 *
	 * @param documentModelId document model ID (name)
	 * @return the document model
	 */
	IDocumentModel getDocumentModel(String documentModelId);

	/**
	 * Get the relationship model for the given relationship ID from context's cache.
	 * If a particular model was already used in the context, this method speeds up retrieving of the model.
	 * Context instance is considered to be local for a particular request,
	 * so there are only models the current user has a right to read it.
	 *
	 * @param relationshipModelId relationship model ID (name)
	 * @return the relationship model
	 */
	RelationshipModel getRelationshipModel(String relationshipModelId);

	/**
	 * Execute the query with the current context.
	 *
	 * @param types types of nodes to be queried; see {@link DocumentTreeNodeType}.
	 * @param queryRoot The query root.
	 * @return the page of the document tree results; see {@link DocumentTreeResult}.
	 */
	@NonNull Page<DocumentTreeResult> query(List<DocumentTreeNodeType> types, QueryRoot queryRoot);

	/**
	 * Get the locale of this request.
	 *
	 * @return the locale of this request
	 */
	String getLocale();

	/**
	 * Get the document model search service for the given document model name using context cache.
	 * If a particular model search service was already used in the context, this method speeds up retrieving of the service.
	 * Context instance is considered to be local for a particular request,
	 * so there are only model services the current user has a right to read its models.
	 *
	 * @param modelId the document model ID (name)
	 * @return the document model search service for this model ID
	 */
	IDocumentModelSearchService getDocumentModelSearchService(String modelId);

	/**
	 * Get enrichments of this context.
	 * Enrichments are used to cache computed information during the query execution, so such information can be reused.
	 * These enrichments are passed to the generator context during query execution.
	 *
	 * @return enrichments for this context
	 */
	Enrichments getEnrichments();

	/**
	 * Returns a set of validators applicable to the given operator type.
	 *
	 * @param operator the operator class for which validators are requested; never null.
	 * @return a set of validators applicable to the operator type; may be empty if none are registered.
	 */
	@NonNull Set<IQueryOperatorValidator> getValidators(Class<? extends ILogicOperator> operator);

	/**
	 * Returns a human-readable operator name used in validation and error reporting.
	 *
	 * @param operator the operator instance; may be null.
	 * @return the operator name, typically derived from the operator type or metadata.
	 */
	String getOperatorName(ILogicOperator operator);

	/**
	 * Increments the internal counter for AND operators and returns the updated count.
	 *
	 * @return the number of AND operators encountered so far.
	 */
	int addAndGetNumberOfAndOperators();
	/**
	 * Increments the internal counter for OR operators and returns the updated count.
	 *
	 * @return the number of OR operators encountered so far.
	 */
	int addAndGetNumberOfOrOperators();

	/**
	 * Checks if the given field path is indexed in any of the given models.
	 * Uses a cache to speed up subsequent calls.
	 *
	 * @param models the set of all document model IDs (names) to check
	 * @param fieldPath the field path to check
	 * @return true if the field is indexed in any of the given models, false otherwise.
	 */
	boolean isIndexedField(Set<String> models, String fieldPath);
}

