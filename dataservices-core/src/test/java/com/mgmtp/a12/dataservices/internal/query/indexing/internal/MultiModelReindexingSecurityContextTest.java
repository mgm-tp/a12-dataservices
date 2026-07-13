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
package com.mgmtp.a12.dataservices.internal.query.indexing.internal;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.apache.commons.lang3.tuple.Pair;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.postgresql.copy.CopyIn;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.Encoding;
import org.postgresql.core.QueryExecutor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties.Query.Reindexing;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DocumentMapper;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterRepositoryLoadEvent;
import com.mgmtp.a12.dataservices.document.internal.entity.DocumentEntity;
import com.mgmtp.a12.dataservices.document.persistence.internal.AggregatedDocumentRepository;
import com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentRepository;
import com.mgmtp.a12.dataservices.document.persistence.internal.DocumentJpaRepository;
import com.mgmtp.a12.dataservices.initialization.internal.BusinessModelInitializer;
import com.mgmtp.a12.dataservices.initialization.events.DataServicesCustomInitializationEvent;
import com.mgmtp.a12.dataservices.initialization.internal.DataServicesInitializationService;
import com.mgmtp.a12.dataservices.initialization.internal.JsonRpcInitializer;
import com.mgmtp.a12.dataservices.migration.internal.MigrationRunner;
import com.mgmtp.a12.dataservices.internal.TransactionHandler;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.DefaultQueryIndexManager;
import com.mgmtp.a12.dataservices.query.indexing.internal.DocumentSearchIndexBehaviour;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.DocumentModelFieldsIndexer;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.ModelFieldsJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.jsonb.DocumentSearchJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.searchtable.DocumentFieldsJpaRepository;
import com.mgmtp.a12.dataservices.rpc.internal.RequestIdService;
import com.mgmtp.a12.dataservices.search.customizer.internal.SearchCustomizerRegistry;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.uaa.authentication.backend.AuthenticatedUserLoader;
import com.mgmtp.a12.uaa.authentication.backend.BackendAuthenticationService;

import jakarta.persistence.EntityManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.ADDRESS_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.CONTRACT_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;
import static com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_NAME;
import static com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.DOC_REF_METADATA_NAME;
import static com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.MODEL_REFERENCE_METADATA_NAME;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Authentication missing in SecurityContext during re-indexing phase with multiple models.
 */
@Slf4j
@Test public class MultiModelReindexingSecurityContextTest extends AbstractDataServicesCoreTest {

	@Mock private AuthenticatedUserLoader backendUserLoader;
	@Mock private ModelHeaderJpaRepository modelHeaderJpaRepository;
	@Mock private MockableDocumentSearchIndexBehaviour indexBehavior;
	@Mock private ApplicationEventPublisher eventPublisher;
	@Mock private ModelFieldsJpaRepository modelFieldsJpaRepository;
	@Mock private BusinessModelInitializer businessModelInitializer;
	@Mock private MigrationRunner migrationRunner;
	@Mock private RequestIdService requestIdService;
	@Mock private DocumentSearchJpaRepository documentSearchJpaRepository;
	@Mock private DocumentModelFieldsIndexer documentModelFieldsIndexer;
	@Mock private EntityManager entityManager;
	@Mock private DocumentFieldsJpaRepository documentFieldsJpaRepository;
	@Mock private DocumentJpaRepository documentJpaRepository;
	@Mock private SearchCustomizerRegistry searchCustomizerRegistry;
	@MockitoSpyBean("dsDataSource")
	@Mock private DataSource dataSource;

	@Spy private TransactionHandler transactionHandler;

	@InjectMocks
	@Spy private BackendAuthenticationService backendAuthenticationService = new BackendAuthenticationService(true);

	@InjectMocks
	@Spy private DocumentMapper documentMapper = Mappers.getMapper(DocumentMapper.class);

	private Optional<JsonRpcInitializer> jsonRpcInitializer = Optional.empty();

	private DefaultDocumentRepository defaultDocumentDepository;

	private AggregatedDocumentRepository aggregatedDocumentRepository;

	private DefaultQueryIndexManager queryIndexManager;

	private DataServicesInitializationService dataServicesInitializationService;

	private ConcurrentMap<String, String> modelsOfDocuments = new ConcurrentHashMap<>();

	@BeforeMethod public void setUp() {
		setCurrentUser(UserConstants.GUEST_USER);
		dataServicesCoreProperties.getQuery().getReindexing().setMode(Reindexing.Mode.REBUILD_INDEX);
		dataServicesCoreProperties.getQuery().getReindexing().getVacuum().setEnabled(false);

		defaultDocumentDepository = spy(new DefaultDocumentRepository(documentJpaRepository, documentSupport, eventPublisher, documentMapper,
			dataServicesDocumentFactory));

		aggregatedDocumentRepository = new AggregatedDocumentRepository(
			List.of(defaultDocumentDepository));

		indexBehavior = spy(new MockableDocumentSearchIndexBehaviour(aggregatedDocumentRepository, documentFieldsJpaRepository, documentModelServiceFactory,
			dataSource, documentModelFieldsIndexer, documentServiceFactory, documentSearchJpaRepository, entityManager, documentV2Serializer,
			searchCustomizerRegistry));

		queryIndexManager = new DefaultQueryIndexManager(documentFieldsJpaRepository, modelHeaderJpaRepository,
			dataServicesCoreProperties, aggregatedDocumentRepository, indexBehavior, documentModelLoader, documentModelServiceFactory, modelFieldsJpaRepository,
			transactionHandler, documentModelFieldsIndexer, documentSearchJpaRepository, dataSource, backendAuthenticationService);

		dataServicesInitializationService = new DataServicesInitializationService(dataServicesCoreProperties,
			migrationRunner, businessModelInitializer, eventPublisher, backendAuthenticationService, requestIdService, jsonRpcInitializer,
			Optional.of(queryIndexManager));

	}

	@Transactional
	@Test void testInitializationCredentials() {
		Queue<Pair<Class<?>, String>> expectedEvents = new LinkedBlockingQueue<>();

		expectedEvents.add(Pair.of(DocumentAfterRepositoryLoadEvent.class, "superUser"));
		expectedEvents.add(Pair.of(DocumentAfterRepositoryLoadEvent.class, "superUser"));
		expectedEvents.add(Pair.of(DocumentAfterRepositoryLoadEvent.class, "superUser"));

		expectedEvents.add(Pair.of(DocumentAfterRepositoryLoadEvent.class, "superUser"));
		expectedEvents.add(Pair.of(DocumentAfterRepositoryLoadEvent.class, "superUser"));
		expectedEvents.add(Pair.of(DocumentAfterRepositoryLoadEvent.class, "superUser"));

		expectedEvents.add(Pair.of(DocumentAfterRepositoryLoadEvent.class, "superUser"));
		expectedEvents.add(Pair.of(DocumentAfterRepositoryLoadEvent.class, "superUser"));
		expectedEvents.add(Pair.of(DocumentAfterRepositoryLoadEvent.class, "superUser"));

		expectedEvents.add(Pair.of(DataServicesCustomInitializationEvent.class, "superUser"));

		mockInitialization(expectedEvents);
		dataServicesInitializationService.runInitialization();
		assertTrue(expectedEvents.isEmpty(), "Not all events were triggered. Remaining: %s".formatted(expectedEvents));
	}

	@SneakyThrows private void mockInitialization(Queue<Pair<Class<?>, String>> expectedEvents) {

		mockSqlConnection();

		when(backendUserLoader.loadUser(anyString()))
			.thenAnswer(a -> User.builder().username(a.getArgument(0)).password("").build());

		when(documentJpaRepository.findIdByModelName(anyString()))
			.thenAnswer(invocation -> {
				String model = invocation.getArgument(0);
				return IntStream.range(0, 3)
					.mapToObj(i -> UUID.randomUUID())
					.map(UUID::toString)
					.peek(id -> modelsOfDocuments.put(id, model))
					.toList();
			});

		when(documentJpaRepository.findByIdInOrderById(anyList()))
			.thenAnswer(invocation -> mockDocumentEntities(invocation.getArgument(0)));

		when(modelHeaderJpaRepository.findIdsByModelType(DOCUMENT_MODEL_TYPE))
			.thenReturn(List.of(BUSINESS_PARTNER_DOCUMENT_MODEL, CONTRACT_DOCUMENT_MODEL, ADDRESS_DOCUMENT_MODEL));

		doAnswer(a -> {
			try {
				Object event = a.getArgument(0);
				Class<?> eventClass = event.getClass();

				Pair<Class<?>, String> expectation = expectedEvents.poll();
				assertNotNull(expectation, "Event %s is not expected.".formatted(eventClass.getSimpleName()));
				assertEquals(eventClass, expectation.getLeft(),
					"Expected event is %s, but got %s".formatted(expectation.getLeft().getSimpleName(), eventClass.getSimpleName()));
				var auth = SecurityContextHolder.getContext().getAuthentication();
				assertNotNull(auth);
				Object principal = auth.getPrincipal();
				assertTrue(principal instanceof User);
				assertEquals(((User) principal).getUsername(), expectation.getRight());
				return null;
			} catch (AssertionError e) {
				log.error("Events left:\n%s".formatted(expectedEvents.stream()
					.map(Pair::getLeft)
					.map(Class::getSimpleName)
					.collect(Collectors.joining("\n"))));
				throw e;
			}
		})
			.when(eventPublisher)
			.publishEvent(nullable(Object.class));

	}

	private void mockSqlConnection() throws SQLException {
		doReturn("")
			.when(indexBehavior)
			.getCopySql(anyString());

		BaseConnection connection = mock(BaseConnection.class);
		when(dataSource.getConnection()).thenReturn(connection);
		when(connection.getEncoding()).thenReturn(Encoding.defaultEncoding());
		when(connection.unwrap(eq(BaseConnection.class))).thenReturn(connection);
		QueryExecutor queryExecutor = mock(QueryExecutor.class);
		when(queryExecutor.startCopy(anyString(), anyBoolean())).thenReturn(mock(CopyIn.class));
		when(connection.getQueryExecutor()).thenReturn(queryExecutor);
	}

	private List<DocumentEntity> mockDocumentEntities(List<String> ids) {
		return ids.stream()
			.map(this::mockDocumentEntity)
			.toList();
	}

	private DocumentEntity mockDocumentEntity(String id) {
		String modelName = modelsOfDocuments.get(id);
		return DocumentEntity.builder()
			.id(id)
			.modelName(modelName)
			.content("""
				{
					"%s": {
						"%s": "%s/%s",
						"%s: "%s"
					}
				}
				""".formatted(DOCUMENT_METADATA_GROUP_NAME,
				DOC_REF_METADATA_NAME, modelName, id,
				MODEL_REFERENCE_METADATA_NAME, modelName))
			.build();
	}

	static class MockableDocumentSearchIndexBehaviour extends DocumentSearchIndexBehaviour {

		public MockableDocumentSearchIndexBehaviour(AggregatedDocumentRepository aggregatedDocumentRepository, DocumentFieldsJpaRepository documentFieldsJpaRepository,
			DocumentModelServiceFactory documentModelServiceFactory, DataSource dataSource, DocumentModelFieldsIndexer documentModelFieldsIndexer,
			DocumentServiceFactory documentServiceFactory, DocumentSearchJpaRepository documentSearchJpaRepository, EntityManager entityManager,
			IDocumentV2Serializer documentV2Serializer, SearchCustomizerRegistry searchCustomizerRegistry) {

			super(aggregatedDocumentRepository, documentFieldsJpaRepository, documentModelServiceFactory, dataSource,
				documentModelFieldsIndexer, documentServiceFactory, documentSearchJpaRepository, entityManager, documentV2Serializer,
				searchCustomizerRegistry);
		}

		@Override protected String getCopySql(String target) {
			return super.getCopySql(target);
		}
	}
}
