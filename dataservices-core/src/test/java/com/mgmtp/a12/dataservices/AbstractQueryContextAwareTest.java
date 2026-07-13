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
package com.mgmtp.a12.dataservices;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.model.internal.IndexedModelFieldCache;
import com.mgmtp.a12.dataservices.query.annotation.QueryOperator;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.enrichment.internal.AggregationEnricher;
import com.mgmtp.a12.dataservices.query.enrichment.internal.ConstraintEnricher;
import com.mgmtp.a12.dataservices.query.enrichment.internal.DefaultQueryEnricher;
import com.mgmtp.a12.dataservices.query.enrichment.internal.HasOperatorEnricher;
import com.mgmtp.a12.dataservices.query.enrichment.internal.LinkEnricher;
import com.mgmtp.a12.dataservices.query.enrichment.internal.QueryAPIOperatorWalker;
import com.mgmtp.a12.dataservices.query.enrichment.internal.SortEnricher;
import com.mgmtp.a12.dataservices.query.internal.DefaultQueryContext;
import com.mgmtp.a12.dataservices.query.internal.QueryContextHelper;
import com.mgmtp.a12.dataservices.query.internal.marshalling.QuerySubtypeProvider;
import com.mgmtp.a12.dataservices.query.projection.internal.CdmHelper;
import com.mgmtp.a12.dataservices.query.security.internal.DefaultQueryAuthorizationService;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;

import lombok.NonNull;
import tools.jackson.databind.jsontype.NamedType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

public abstract class AbstractQueryContextAwareTest extends AbstractDataServicesCoreTest {

	protected final IDocumentModelService documentModelService = documentModelServiceFactory.createDocumentModelService();
	protected CdmHelper cdmHelper = new CdmHelper(documentModelService);

	@Mock protected final @NonNull DefaultQueryContext.InternalQueryAction queryMethod = mock(DefaultQueryContext.InternalQueryAction.class);
	@Mock protected final QueryContextHelper queryContextHelper = mock(QueryContextHelper.class);
	@Mock protected final ModelTypeService modelTypeService = mock(ModelTypeService.class);
	@Mock protected final IndexedModelFieldCache indexedModelFieldCache = mock(IndexedModelFieldCache.class);

	@NotNull protected DefaultQueryContext newQueryContext() {
		return new DefaultQueryContext(documentModelResolver, relationshipModelLoader, queryMethod, documentModelServiceFactory, queryContextHelper,
			indexedModelFieldCache, null, null);
	}

	@NotNull protected DefaultQueryContext newQueryContextWithLocale(String locale) {
		return new DefaultQueryContext(documentModelResolver, relationshipModelLoader, queryMethod,
			documentModelServiceFactory, queryContextHelper, indexedModelFieldCache, locale, null);
	}

	@Mock protected DefaultQueryAuthorizationService queryAuthorizationService;
	//	@Mock protected static DataServicesCoreProperties dataServicesCoreProperties = mock(DataServicesCoreProperties.class);
	@Mock protected static DataServicesCoreProperties.Query queryProperties = mock(DataServicesCoreProperties.Query.class);

	@InjectMocks
	@Spy protected DocumentModelUtils documentModelUtils;

	protected HasOperatorEnricher hasOperatorEnricher = new HasOperatorEnricher(modelTypeService, documentModelUtils, documentModelServiceFactory,
		queryAuthorizationService);
	protected QueryAPIOperatorWalker queryAPIOperatorWalker = new QueryAPIOperatorWalker(List.of(hasOperatorEnricher), documentModelUtils, modelTypeService);
	protected SortEnricher sortEnricher = new SortEnricher(modelTypeService, documentModelUtils, documentModelServiceFactory);
	protected ConstraintEnricher constraintEnricher = new ConstraintEnricher(queryAuthorizationService, queryAPIOperatorWalker);
	protected AggregationEnricher aggregationEnricher = new AggregationEnricher();
	protected LinkEnricher linkEnricher = new LinkEnricher(modelTypeService, documentModelUtils, documentModelServiceFactory, dataServicesCoreProperties,
		queryAPIOperatorWalker, constraintEnricher);

	@InjectMocks
	@Spy protected DefaultQueryEnricher queryEnricher = new DefaultQueryEnricher(modelTypeService, documentModelUtils, documentModelServiceFactory,
		sortEnricher, constraintEnricher, aggregationEnricher, linkEnricher);

	@BeforeMethod
	public void mockEnricher() {
		reset(modelTypeService);
		hasOperatorEnricher = new HasOperatorEnricher(modelTypeService, documentModelUtils, documentModelServiceFactory, queryAuthorizationService);
		queryAPIOperatorWalker = new QueryAPIOperatorWalker(List.of(hasOperatorEnricher), documentModelUtils, modelTypeService);
		sortEnricher = new SortEnricher(modelTypeService, documentModelUtils, documentModelServiceFactory);
		constraintEnricher = new ConstraintEnricher(queryAuthorizationService, queryAPIOperatorWalker);
		aggregationEnricher = new AggregationEnricher();
		linkEnricher = new LinkEnricher(modelTypeService, documentModelUtils, documentModelServiceFactory, dataServicesCoreProperties, queryAPIOperatorWalker,
			constraintEnricher);
		queryEnricher = new DefaultQueryEnricher(modelTypeService, documentModelUtils, documentModelServiceFactory, sortEnricher, constraintEnricher,
			aggregationEnricher, linkEnricher);
	}

	@BeforeMethod
	public void mockAbac() {
		Mockito.lenient().doAnswer(a -> a.getArgument(0)).when(queryAuthorizationService).addAbacRules(any(ILogicOperator.class), anyString());
	}

	@BeforeClass
	public void beforeClass() {
		QuerySubtypeProvider subtypeProvider = new QuerySubtypeProvider(DataServicesCoreProperties.DS_PACKAGE_PREFIX);

		Map<Class<? extends ILogicOperator>, String> operators = new HashMap<>();
		for (NamedType namedType : subtypeProvider.getSubtypes()) {
			objectMapper.deserializationConfig().getSubtypeResolver().registerSubtypes(namedType);
			objectMapper.serializationConfig().getSubtypeResolver().registerSubtypes(namedType);
			if (namedType.getType().isAnnotationPresent(QueryOperator.class)) {
				operators.put((Class<? extends ILogicOperator>) namedType.getType(), namedType.getName());
			}
		}

		doReturn(operators).when(queryContextHelper).getOperators();
	}
}
