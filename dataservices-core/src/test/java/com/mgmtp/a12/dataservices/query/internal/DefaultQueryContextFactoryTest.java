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

import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractQueryContextAwareTest;
import com.mgmtp.a12.dataservices.model.internal.IndexedModelFieldCache;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.model.relationship.persistence.internal.DefaultRelationshipModelLoader;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.QueryContextFactory;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;

import static org.mockito.Mockito.mock;

@Listeners(MockitoTestNGListener.class)
public class DefaultQueryContextFactoryTest extends AbstractQueryContextAwareTest {

	private final IModelLoader<RelationshipModel> relationshipModelLoader = mock(DefaultRelationshipModelLoader.class);
	private final DefaultQueryService queryService = mock(DefaultQueryService.class);
	private final IndexedModelFieldCache indexedModelFieldCache = mock(IndexedModelFieldCache.class);

	private QueryContextFactory queryContextFactory;

	@BeforeClass
	public void init() {
		queryContextFactory = new DefaultQueryContextFactory(kernelTestSupport.getDocumentModelResolver(), relationshipModelLoader,
			queryService, kernelTestSupport.getDocumentModelServiceFactory(), queryContextHelper, indexedModelFieldCache);
	}

	@Test
	public void testCreateContext_shouldInitializeNewEnrichmentsInstance() {
		QueryContext queryContext1 = queryContextFactory.createContext(null);
		QueryContext queryContext2 = queryContextFactory.createContext(null);
		Assert.assertNotEquals(queryContext1.getEnrichments(), queryContext2.getEnrichments());
		Assert.assertNull(queryContext1.getOriginalQuery());
	}

	@Test
	public void testCreateContext_shouldContainOriginalQuery() {
		QueryRoot originalQuery = QueryRoot.builder().build();
		QueryContext queryContext = queryContextFactory.createContext(originalQuery, "de");
		Assert.assertNotNull(queryContext.getOriginalQuery());
		Assert.assertEquals(queryContext.getOriginalQuery(), originalQuery);
		Assert.assertEquals(queryContext.getLocale(), "de");
	}
}
