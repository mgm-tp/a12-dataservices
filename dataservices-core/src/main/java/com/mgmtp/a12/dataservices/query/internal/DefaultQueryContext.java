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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;

import com.mgmtp.a12.dataservices.model.internal.IndexedModelFieldCache;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.enrichement.Enrichments;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.query.validation.IQueryOperatorValidator;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultQueryContext implements QueryContext {

	@NonNull private final IModelLoader<IDocumentModel> documentModelLoader;
	@NonNull private final IModelLoader<RelationshipModel> relationshipModelLoader;
	@NonNull private final InternalQueryAction queryMethod;
	@NonNull private final DocumentModelServiceFactory documentModelServiceFactory;
	@NonNull private final QueryContextHelper queryContextHelper;
	@NonNull private final IndexedModelFieldCache indexedModelFieldCache;
	@Getter private final String locale;

	@Getter private final Enrichments enrichments = new Enrichments();
	private final Map<String, IDocumentModel> documentModels = new HashMap<>();
	private final Map<String, RelationshipModel> relationshipModels = new HashMap<>();
	private final Map<IDocumentModel, IDocumentModelSearchService> documentModelSearchServices = new HashMap<>();
	private final QueryRoot originalQuery;

	private int numberOfAndOperators = 0;
	private int numberOfOrOperators = 0;

	@Override public QueryRoot getOriginalQuery() {
		return originalQuery;
	}

	@Override public IDocumentModel getDocumentModel(final String documentModelId) {
		return documentModels.computeIfAbsent(documentModelId, documentModelLoader::loadModel);
	}

	@Override public RelationshipModel getRelationshipModel(String relationshipModelId) {
		return relationshipModels.computeIfAbsent(relationshipModelId, relationshipModelLoader::loadModel);
	}

	@Override public @NonNull Page<DocumentTreeResult> query(List<DocumentTreeNodeType> types, QueryRoot queryRoot) {
		return queryMethod.apply(types, queryRoot, this);
	}

	@Override public IDocumentModelSearchService getDocumentModelSearchService(String modelId) {
		return documentModelSearchServices.computeIfAbsent(getDocumentModel(modelId), documentModelServiceFactory::createDocumentModelSearchService);
	}

	@Override public @NonNull Set<IQueryOperatorValidator> getValidators(Class<? extends ILogicOperator> operator) {
		return queryContextHelper.getValidatorMappings().getOrDefault(operator, Set.of());
	}

	@Override public String getOperatorName(ILogicOperator operator) {
		return queryContextHelper.getOperators().get(operator.getClass());
	}

	@Override public int addAndGetNumberOfAndOperators() {
		return ++numberOfAndOperators;
	}

	@Override public int addAndGetNumberOfOrOperators() {
		return ++numberOfOrOperators;
	}

	@FunctionalInterface public interface InternalQueryAction {
		Page<DocumentTreeResult> apply(List<DocumentTreeNodeType> types, QueryRoot queryRoot, QueryContext context);
	}

	@Override public boolean isIndexedField(Set<String> models, String fieldPath) {
		return models.stream()
			.map(indexedModelFieldCache::getIndexedFields)
			.anyMatch(fields -> fields != null && fields.contains(fieldPath));
	}

}
