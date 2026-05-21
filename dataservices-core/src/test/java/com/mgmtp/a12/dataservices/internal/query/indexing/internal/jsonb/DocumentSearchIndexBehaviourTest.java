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
package com.mgmtp.a12.dataservices.internal.query.indexing.internal.jsonb;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.persistence.internal.AggregatedDocumentRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.DocumentSearchIndexBehaviour;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.DocumentModelFieldsIndexer;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.jsonb.DocumentSearchEntity;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.LocalizedFieldsJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.ModelFieldsJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.jsonb.DocumentSearchJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.searchtable.DocumentFieldsJpaRepository;
import com.mgmtp.a12.dataservices.search.customizer.internal.SearchCustomizerRegistry;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertListContainsObject;

@Slf4j public class DocumentSearchIndexBehaviourTest extends AbstractDataServicesCoreTest {

	public static final String ENUMS_IN_REPEATABLE_GROUPS_MODEL_NAME = "Contract";
	private final AggregatedDocumentRepository aggregatedDocumentRepository = mock(AggregatedDocumentRepository.class);
	private final DocumentFieldsJpaRepository documentFieldsJpaRepository = mock(DocumentFieldsJpaRepository.class);
	private final DataSource dataSource = mock(DataSource.class);
	private final DocumentSearchJpaRepository documentSearchJpaRepository = mock(DocumentSearchJpaRepository.class);

	private final DocumentModelUtils documentModelUtils = mock(DocumentModelUtils.class);
	private final ModelFieldsJpaRepository modelFieldsJpaRepository = mock(ModelFieldsJpaRepository.class);
	private final LocalizedFieldsJpaRepository localizedFieldsJpaRepository = mock(LocalizedFieldsJpaRepository.class);
	private final EntityManager entityManager = mock(EntityManager.class);
	private final SearchCustomizerRegistry searchCustomizerRegistry = mock(SearchCustomizerRegistry.class);

	DocumentModelFieldsIndexer documentModelFieldsIndexer = spy(
		new DocumentModelFieldsIndexer(documentModelUtils, iDocumentModelService, modelFieldsJpaRepository, localizedFieldsJpaRepository,
			searchCustomizerRegistry, jsonMapper));
	DocumentSearchIndexBehaviour indexBehavior = spy(new DocumentSearchIndexBehaviour(aggregatedDocumentRepository, documentFieldsJpaRepository,
		documentModelServiceFactory, dataSource, documentModelFieldsIndexer, documentServiceFactory, documentSearchJpaRepository, entityManager,
		documentV2Serializer, searchCustomizerRegistry));

	private final DocumentDeserializationConfig documentDeserializationConfig = DocumentDeserializationConfig.builder()
		.format(DocumentSerializationConfig.Format.JSON)
		.addTransientFields(dataServicesCoreProperties.getDocuments().getPersistTransientFields().isEnabled())
		.build();

	@Test
	public void testJsonB() throws IOException {

		IDocumentModelSearchService documentModelSearchService = documentModelServiceFactory.createDocumentModelSearchService(
			documentModelResolver.getDocumentModelById(ENUMS_IN_REPEATABLE_GROUPS_MODEL_NAME));

		DocumentV2 doc = documentV2Serializer.deserializeV2(
			new InputStreamReader(new ClassPathResource("/document/Contract_document.json").getInputStream()),
			"Contract",
			documentDeserializationConfig, n -> log.warn("{} [{}] - {}: {}", Instant.now(), n.getSeverity(), n.getSource(), n.getMessage()));

		doc = metadataUtils.createDocumentMetadata(doc, documentUtils.generateDocRef(doc),
			UserConstants.ADMIN_USER, Instant.now(), null);

		Session mockedSession = mock(Session.class);
		when(entityManager.unwrap(Session.class)).thenReturn(mockedSession);
		NativeQuery nativeQuery = mock(NativeQuery.class);
		when(nativeQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(nativeQuery);
		when(nativeQuery.addSynchronizedEntityClass(DocumentSearchEntity.class)).thenReturn(nativeQuery);

		when(mockedSession.createNativeQuery(Mockito.anyString(), eq(Integer.class))).thenReturn(nativeQuery);

		DataServicesDocument dataServicesDocument = dataServicesDocumentFactory.newDataServicesDocument(doc);

		indexBehavior.saveDocumentFields(dataServicesDocument,
			documentModelSearchService,
			(a, b) -> 10001L
		);

		verify(documentFieldsJpaRepository, times(1)).saveAll(assertArg(fieldEntities -> {
			AtomicInteger i = new AtomicInteger();
			fieldEntities.forEach(fieldEntity -> {
				i.getAndIncrement();
				assertEquals(fieldEntity.getModelName(), ENUMS_IN_REPEATABLE_GROUPS_MODEL_NAME);
				assertListContainsObject(List.of("INumberType", "IDateType", "IDateFragmentType", "IDateRangeType"), fieldEntity.getFieldType(),
					"Type must be numeric or date/time. Not <%s>".formatted(fieldEntity.getFieldType()));
				log.debug("{} {} [{}]: {} / {}", fieldEntity.getFieldName(), fieldEntity.getRepetitions(), fieldEntity.getFieldType(),
					fieldEntity.getValue(), fieldEntity.getTypedValue());
			});

			assertEquals(i.get(), 24);
		}));
	}
}
