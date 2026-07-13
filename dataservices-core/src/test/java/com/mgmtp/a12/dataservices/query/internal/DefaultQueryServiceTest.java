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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractQueryContextAwareTest;
import com.mgmtp.a12.dataservices.authorization.DocumentPermissionEvaluator;
import com.mgmtp.a12.dataservices.authorization.internal.DefaultDocumentModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.document.internal.kernel.KernelDocumentService;
import com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository;
import com.mgmtp.a12.dataservices.document.persistence.internal.AggregatedDocumentRepository;
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.model.internal.DefaultModelTypeService;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder;
import com.mgmtp.a12.dataservices.query.Order;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.QueryContextFactory;
import com.mgmtp.a12.dataservices.query.enrichment.QueryEnricher;
import com.mgmtp.a12.dataservices.query.generator.sql.internal.DefaultQueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.internal.marshalling.QuerySubtypeProvider;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.DocumentModelFieldsIndexer;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.ModelFieldsJpaRepository;
import com.mgmtp.a12.dataservices.query.projection.IQueryProjection;
import com.mgmtp.a12.dataservices.query.projection.internal.CddProjectionImplementation;
import com.mgmtp.a12.dataservices.query.projection.internal.DocumentProjectionImplementation;
import com.mgmtp.a12.dataservices.query.security.IQueryResultAuthorization;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.query.validation.internal.FieldsValidator;
import com.mgmtp.a12.dataservices.query.validation.internal.LinkAwareValidator;
import com.mgmtp.a12.dataservices.query.validation.internal.QueryValidator;
import com.mgmtp.a12.dataservices.query.validation.internal.ValidationResult;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipUtils;
import com.mgmtp.a12.dataservices.request.internal.QueryPagingHelper;
import com.mgmtp.a12.dataservices.rpc.query.PagedResultSet;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.model.header.Header;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.CONTRACT_DOCUMENT_MODEL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@Listeners(MockitoTestNGListener.class)
public class DefaultQueryServiceTest extends AbstractQueryContextAwareTest {

	@Spy private ProjectionProvider projectionProvider = mock(ProjectionProvider.class);
	@Spy private KernelDocumentService kernelDocumentService = mock(KernelDocumentService.class);
	@Spy private ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
	@Spy private IQueryResultAuthorization queryResultAuthorization = mock(IQueryResultAuthorization.class);
	@Spy private DefaultQueryRepository defaultQueryRepository = mock(DefaultQueryRepository.class);
	@Spy private IDocumentRepository documentRepository = mock(IDocumentRepository.class);
	@Spy private List<IDocumentRepository> documentRepositories = spy(List.of(documentRepository));
	@Spy private AggregatedDocumentRepository aggregatedDocumentRepository = spy(new AggregatedDocumentRepository(documentRepositories));
	@Spy private QueryEnricher queryEnricher = mock(QueryEnricher.class);
	@Spy private DocumentPermissionEvaluator documentPermissionEvaluator = mock(DocumentPermissionEvaluator.class);
	@Spy private DataServicesCoreProperties.Query queryProperties = mock(DataServicesCoreProperties.Query.class);
	private final String language = null;
	@Spy private IDocumentV2Serializer documentV2Serializer = mock(IDocumentV2Serializer.class);
	@Spy private DocumentSerializationConfig documentJsonSerializationConfig = mock(DocumentSerializationConfig.class);
	@Spy private DataServicesCoreProperties.Query.PageRequest pageRequestProperties = mock(DataServicesCoreProperties.Query.PageRequest.class);
	@Spy private DocumentModelUtils documentModelUtils = new DocumentModelUtils(documentModelServiceFactory, documentModelSerializer, headerParser);
	@Spy private Collection<IQueryProjection<?>> documentGraphProjections;
	@Spy private final DocumentTreeHelper documentTreeHelper = new DocumentTreeHelper(objectMapper);
	@Captor ArgumentCaptor<QueryRoot> queryRootCaptor;
	@Mock private final DefaultModelTypeService modelTypeService = mock(DefaultModelTypeService.class);
	@Spy private RelationshipUtils relationshipUtils = new RelationshipUtils(modelTypeService);
	@Spy private final LinkAwareValidator linkAwareValidator = new LinkAwareValidator(modelTypeService, dataServicesCoreProperties, relationshipUtils);
	@Mock private final DefaultDocumentModelPermissionEvaluator documentModelPermissionEvaluator = mock(DefaultDocumentModelPermissionEvaluator.class);
	@Mock private final ModelFieldsJpaRepository modelFieldsJpaRepository = mock(ModelFieldsJpaRepository.class);
	@Spy private QueryContextFactory queryContextFactory = mock(DefaultQueryContextFactory.class);
	@Spy DocumentModelFieldsIndexer documentModelFieldsIndexer =
		new DocumentModelFieldsIndexer(documentModelUtils, iDocumentModelService, modelFieldsJpaRepository, objectMapper);
	@Spy private final QueryValidator queryValidator = spy(new QueryValidator(dataServicesCoreProperties, documentModelPermissionEvaluator, linkAwareValidator,
		relationshipModelLoader, mock(FieldsValidator.class), mock(com.mgmtp.a12.dataservices.query.validation.internal.OrderValidator.class)));
	private final DefaultQueryService queryService =
		new DefaultQueryService(documentModelResolver, relationshipModelLoader, documentModelServiceFactory, documentPermissionEvaluator,
			projectionProvider, queryValidator, dataServicesCoreProperties, defaultQueryRepository, queryEnricher, Optional.of(queryResultAuthorization),
			applicationEventPublisher, queryContextHelper, indexedModelFieldCache);

	@BeforeClass public void setUpClass() {
		new DefaultQueryGeneratorContext.QueryGeneratorContextFactory(objectMapper, mock(ApplicationContext.class), mock(QuerySubtypeProvider.class)).init();
		when(documentModelPermissionEvaluator.hasModelReadPermission(any(String.class))).thenReturn(true);
		when(documentModelPermissionEvaluator.hasModelReadPermission(any(Header.class))).thenReturn(true);
		when(documentModelPermissionEvaluator.hasModelReadPermission(any(IDocumentModel.class))).thenReturn(true);
	}

	@BeforeMethod public void setUp() throws IllegalAccessException {
		Mockito.reset(defaultQueryRepository);
		when(queryContextFactory.createContext(Mockito.anyString())).thenReturn(newQueryContext());

		IQueryProjection jsonbCddProjectionImplementation =
			new CddProjectionImplementation(iDocumentModelService, documentModelServiceFactory, dataServicesCoreProperties, objectMapper,
				documentTreeHelper, Optional.of(kernelDocumentService), documentSupport, cdmHelper);

		IQueryProjection documentProjectionImplementation =
			new DocumentProjectionImplementation(objectMapper, documentTreeHelper, aggregatedDocumentRepository, documentSupport);

		when(projectionProvider.getMatchingProjection("cdd"))
			.thenReturn(jsonbCddProjectionImplementation);
		when(projectionProvider.getMatchingProjection("document"))
			.thenReturn(documentProjectionImplementation);

	}

	@DataProvider public Object[][] pagingData() {
		return new Object[][] {
			{ 0, 10 },
			{ 5, 20 },
		};
	}

	@DataProvider public Object[][] invalidPagingData() {
		return new Object[][] {
			{ null, 10, "pageNumber must not be null" },
			{ -1, 10, "is invalid, it must be greater or equal to" },
			{ 0, -1, "is invalid, it must be greater or equal to" },
			{ 0, 0, "is invalid, it must be greater or equal to" },
			{ 0, 200, "is bigger than the allowed limit" },
			{ 200, 100, "is bigger than the allowed limit" },
		};
	}

	@DataProvider public Object[][] sortingData() {
		return new Object[][] {
			{ List.<Order>of(new DirectFieldOrder("sortField", DirectFieldOrder.Direction.ASC, DirectFieldOrder.NullHandling.NATIVE)) },
			{ List.<Order>of(new DirectFieldOrder("sortField", DirectFieldOrder.Direction.DESC, DirectFieldOrder.NullHandling.NULLS_FIRST)) },
			{ List.<Order>of(new DirectFieldOrder("sortField", DirectFieldOrder.Direction.DESC)) },
			{ List.<Order>of(new DirectFieldOrder("sortField")) },
			{ List.<Order>of(new DirectFieldOrder("sortField1", DirectFieldOrder.Direction.ASC, DirectFieldOrder.NullHandling.NATIVE),
				new DirectFieldOrder("sortField2", DirectFieldOrder.Direction.DESC, DirectFieldOrder.NullHandling.NULLS_LAST)) },
		};
	}

	@Test(dataProvider = "pagingData")
	public void testQuery_paging(Integer pageNumber, int pageSize) {
		Paging paging = Paging.builder()
			.pageNumber(pageNumber)
			.pageSize(pageSize)
			.build();

		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
			.paging(paging)
			.build();
		basicMocks();
		doReturn(Page.empty())
			.when(defaultQueryRepository).query(any(QueryRoot.class), anyCollection(), any());

		queryService.query(queryRoot, language);

		// Verify that correct Paging is passed to DefaultQueryRepository
		Mockito.verify(defaultQueryRepository, Mockito.times(1)).
			query(eq(queryRoot), eq(Arrays.asList(DocumentTreeNodeType.values())), any(QueryContext.class));
	}

	@Test
	public void testQuery_shouldExecuteValidate_whenGivenDefaultCoreProperties() {
		Paging paging = Paging.builder()
			.pageNumber(0)
			.pageSize(0)
			.build();
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
			.paging(paging)
			.build();
		basicMocks();
		doReturn(Page.empty())
			.when(defaultQueryRepository).query(any(QueryRoot.class), anyCollection(), any());
		when(queryProperties.getValidation()).thenReturn(new DataServicesCoreProperties.Query.Validation());
		doReturn(new ValidationResult())
			.when(queryValidator).validate(any(QueryRoot.class), any(QueryContext.class), anyBoolean());

		queryService.query(queryRoot, language);

		// Verify that correct Paging is passed to DefaultQueryRepository
		Mockito.verify(queryValidator, Mockito.times(1)).
			validate(eq(queryRoot), any(QueryContext.class), anyBoolean());
	}

	@Test(dataProvider = "invalidPagingData")
	public void testQuery_invalidPaging(Integer pageNumber, int pageSize, String expectedErrorMessage) {
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.paging(Paging.builder()
				.pageNumber(pageNumber)
				.pageSize(pageSize)
				.build())
			.build();
		basicMocks();
		QueryInvalidInputException invalidInputException =
			Assert.expectThrows(QueryInvalidInputException.class, () -> queryService.query(queryRoot, language));
		Assert.assertTrue(invalidInputException.getMessage().contains(expectedErrorMessage));
	}

	@Test(dataProvider = "sortingData")
	public void testQuery_sorting(List<Order> sort) {
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
			.paging(Paging.builder()
				.pageNumber(0)
				.pageSize(1)
				.build())
			.sort(sort)
			.build();
		basicMocks();

		when(defaultQueryRepository.query(any(QueryRoot.class), anyList(), any()))
			.thenReturn(Page.empty());
		queryService.query(queryRoot, language);

		// Verify that correct sorting is passed to DefaultQueryRepository
		Mockito.verify(defaultQueryRepository).query(queryRootCaptor.capture(), any(), any());
		QueryRoot passedQueryRoot = queryRootCaptor.getValue();
		for (int i = 0; i < passedQueryRoot.getSort().size(); i++) {
			DirectFieldOrder order = (DirectFieldOrder) passedQueryRoot.getSort().get(i);
			DirectFieldOrder expected = (DirectFieldOrder) (sort.get(i));
			Assert.assertEquals(expected.field(), order.field());
			Assert.assertEquals(expected.direction() != null ? expected.direction() : DirectFieldOrder.Direction.ASC, order.direction());
			Assert.assertEquals(expected.nullHandling() != null ? expected.nullHandling() : DirectFieldOrder.NullHandling.NATIVE, order.nullHandling());
		}
	}

	@Test public void testQuery_shouldExecuteQueryResultAuthorization() {
		Paging paging = Paging.builder()
			.pageNumber(0)
			.pageSize(1)
			.build();

		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
			.paging(paging)
			.build();
		basicMocks();

		List<DocumentTreeResult> result = List.of(new DocumentTreeResult());

		when(queryResultAuthorization.authorizeQueryResult(result)).thenReturn(result);
		when(defaultQueryRepository.query(any(QueryRoot.class), anyCollection(), any())).thenReturn(
			new PageImpl<>(result));

		PagedResultSet<?> pageResult = QueryPagingHelper.pageToResultSet(queryService.query(queryRoot, language), queryRoot.isExclude());

		Mockito.verify(queryResultAuthorization, Mockito.times(1))
			.authorizeQueryResult(result);
		Assert.assertEquals(pageResult.getEntries().size(), 1);
	}

	@Test public void testQuery_shouldReturnEmptyForNonLoadableResult_whenExecuteQueryResultAuthorization() {
		Paging paging = Paging.builder()
			.pageNumber(0)
			.pageSize(1)
			.build();

		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
			.paging(paging)
			.build();
		basicMocks();

		List<DocumentTreeResult> result = List.of(new DocumentTreeResult());

		when(queryResultAuthorization.authorizeQueryResult(result)).thenReturn(new ArrayList<>());
		when(defaultQueryRepository.query(any(QueryRoot.class), anyCollection(), any())).thenReturn(
			new PageImpl<>(result));

		PagedResultSet<?> pageResult = QueryPagingHelper.pageToResultSet(queryService.query(queryRoot, language), queryRoot.isExclude());

		Mockito.verify(queryResultAuthorization, Mockito.times(1))
			.authorizeQueryResult(result);
		Assert.assertEquals(pageResult.getEntries().size(), 0);
	}

	private void basicMocks() {
		doNothing().when(documentPermissionEvaluator).checkDocumentQueryPermission(any());
		doNothing().when(queryEnricher).enrichQuery(any(QueryRoot.class), any(QueryContext.class));

		when(dataServicesCoreProperties.getQuery()).thenReturn(queryProperties);
		when(queryProperties.getPageRequest()).thenReturn(pageRequestProperties);
		when(queryProperties.getValidation()).thenReturn(new DataServicesCoreProperties.Query.Validation());
		when(pageRequestProperties.getPageSizeLimit()).thenReturn(100);
		when(pageRequestProperties.getPageNumberLimit()).thenReturn(100);
	}
}
