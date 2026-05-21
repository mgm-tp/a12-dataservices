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

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.model.internal.IndexedModelFieldCache;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.QueryContextFactory;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component public class DefaultQueryContextFactory implements QueryContextFactory {

	private final IModelLoader<IDocumentModel> documentModelLoader;
	private final IModelLoader<RelationshipModel> relationshipModelLoader;
	private final DefaultQueryService queryService;
	private final DocumentModelServiceFactory documentModelServiceFactory;
	private final QueryContextHelper queryContextHelper;
	private final IndexedModelFieldCache indexedModelFieldCache;

	@Override public QueryContext createContext(String locale) {
		return new DefaultQueryContext(documentModelLoader, relationshipModelLoader, queryService::queryWithoutProjection, documentModelServiceFactory,
			queryContextHelper, indexedModelFieldCache, locale, null);
	}

	@Override public QueryContext createContext(QueryRoot originalQuery, String locale) {
		return new DefaultQueryContext(documentModelLoader, relationshipModelLoader, queryService::queryWithoutProjection, documentModelServiceFactory,
			queryContextHelper, indexedModelFieldCache, locale, originalQuery);
	}
}
