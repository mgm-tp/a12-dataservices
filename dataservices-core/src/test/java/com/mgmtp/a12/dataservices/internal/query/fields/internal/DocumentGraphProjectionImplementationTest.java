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
package com.mgmtp.a12.dataservices.internal.query.fields.internal;

import java.util.List;
import java.util.Optional;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.TestHeader;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.cdd.internal.CddSkeletonFactory;
import com.mgmtp.a12.dataservices.cdd.jms.internal.ComposeDocumentModel;
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.exception.query.QueryNotFoundException;
import com.mgmtp.a12.dataservices.model.document.persistence.DocumentModelReadRepository;
import com.mgmtp.a12.dataservices.model.document.persistence.internal.DefaultDocumentModelLoader;
import com.mgmtp.a12.dataservices.model.internal.IndexedModelFieldCache;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelLoader;
import com.mgmtp.a12.dataservices.query.internal.DefaultQueryContext;
import com.mgmtp.a12.dataservices.query.projection.internal.DocumentGraphProjectionImplementation;
import com.mgmtp.a12.dataservices.query.projection.internal.DocumentProjectionImplementation;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.query.internal.QueryContextHelper;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelContent;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelInfo;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelWrapper;

import static org.mockito.Mockito.mock;

@Listeners(MockitoTestNGListener.class)
public class DocumentGraphProjectionImplementationTest extends AbstractDataServicesCoreTest {

	private static final String NON_CDM_MODEL_NAME = "NON_CDM_MODEL";
	private static final ComposeDocumentModel NON_CDM_MODEL = new ComposeDocumentModel(
		new DocumentModelWrapper(
			DocumentModel.builder()
				.header(new TestHeader())
				.content(DocumentModelContent.builder()
					.modelInfo(DocumentModelInfo.builder()
						.name(NON_CDM_MODEL_NAME)
						.build())
					.build())
				.build()));

	@Mock private DefaultQueryContext.InternalQueryAction queryMethod;
	@Mock private ModelPermissionEvaluator<IDocumentModel> documentModelPermissionEvaluator;
	@Mock private DocumentProjectionImplementation documentProjection;
	@Mock private QueryContextHelper queryContextHelper;
	@Mock private IModelLoader<ComposeDocumentModel> composeDocumentModelLoader;
	@Mock private CddSkeletonFactory cddSkeletonFactory;
	@Mock private IModelLoader<RelationshipModel> relationshipModelLoader;
	@Mock private DocumentModelReadRepository documentModelReadRepository;
	@Mock private IndexedModelFieldCache indexedModelFieldCache;
	private IModelLoader<IDocumentModel> documentModelLoader;
	private final DocumentModelServiceFactory documentModelServiceFactory = new DocumentModelServiceFactory();
	private DefaultQueryContext queryContext;

	private DocumentGraphProjectionImplementation documentGraphProjection;

	@BeforeMethod
	void setup() {
		documentModelLoader =
			new DefaultDocumentModelLoader(documentModelPermissionEvaluator, eventPublisher, documentModelReadRepository);
		queryContext = new DefaultQueryContext(documentModelLoader, mock(RelationshipModelLoader.class),
			queryMethod, documentModelServiceFactory, queryContextHelper, indexedModelFieldCache, null, null);
		documentGraphProjection = new DocumentGraphProjectionImplementation(
			composeDocumentModelLoader, relationshipModelLoader, documentProjection, documentModelServiceFactory, Optional.of(cddSkeletonFactory)
		);
	}

	@Test
	public void testPreProcess_shouldThrowQueryInvalidInputException_whenQueryWithMultiFields() {
		QueryInvalidInputException exception = Assert.expectThrows(QueryInvalidInputException.class, () ->
			documentGraphProjection.preprocess(QueryRoot.builder()
					.fields(List.of("/ContractRoot", "/ContractRoot/ContractBusinessPartner"))
					.build(),
				queryContext
			));
		Assert.assertNotNull(exception);
		Assert.assertTrue(exception.getMessage().contains(DocumentGraphProjectionImplementation.INVALID_MULTI_VALUES_INPUT_FIELDS_EXCEPTION));
	}

	@Test
	public void testPreProcess_shouldThrowQueryNotFoundException_whenHandleNonCdmModel() {
		Mockito.when(composeDocumentModelLoader.loadModel(NON_CDM_MODEL_NAME)).thenReturn(NON_CDM_MODEL);
		QueryNotFoundException exception = Assert.expectThrows(QueryNotFoundException.class, () ->
			documentGraphProjection.preprocess(QueryRoot.builder()
					.targetDocumentModel(NON_CDM_MODEL_NAME)
					.build(),
				queryContext
			));
		Assert.assertNotNull(exception);
		Assert.assertTrue(exception.getMessage().contains(DocumentGraphProjectionImplementation.NO_MODEL_AVAILABLE_FOR_CDDS.formatted(NON_CDM_MODEL_NAME)));
	}
}
