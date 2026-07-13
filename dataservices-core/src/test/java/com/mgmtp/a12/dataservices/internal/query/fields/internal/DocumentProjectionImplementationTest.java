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

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.mockito.Spy;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.rpc.internal.marshalling.DataServicesJacksonModule;
import com.mgmtp.a12.dataservices.authorization.internal.DefaultDocumentModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DataServicesDocumentMetadata;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.internal.DefaultDocumentMetadata;
import com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository;
import com.mgmtp.a12.dataservices.document.persistence.internal.AggregatedDocumentRepository;
import com.mgmtp.a12.dataservices.internal.query.fields.AbstractProjectionTest;
import com.mgmtp.a12.dataservices.model.document.persistence.DocumentModelReadRepository;
import com.mgmtp.a12.dataservices.model.document.persistence.internal.DefaultDocumentModelLoader;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.internal.DefaultQueryContext;
import com.mgmtp.a12.dataservices.query.projection.internal.DocumentProjectionImplementation;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

import lombok.NonNull;
import lombok.SneakyThrows;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static com.mgmtp.a12.dataservices.query.DocumentTreeNodeType.ROOT;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class DocumentProjectionImplementationTest extends AbstractProjectionTest {

	public static final String SINGLE_BUSINESS_PARTNER_DOCUMENT_TREE_RESULT = "BusinessPartner_DocumentTreeResult";
	public static final DocumentReference CONTRACT_1_DOCUMENT_REFERENCE = new DocumentReference("Contract/1");
	public static final DocumentV2 CONTRACT_DOCUMENT = DocumentV2.empty(CONTRACT_1_DOCUMENT_REFERENCE.getDocumentModelName())
		.withFieldValue("/ContractRoot/ContractName", "Contract 1")
		.withFieldValue("/ContractRoot/ChangeLog[1]/ChangeTimestamp", "2025-02-13")
		.withFieldValue("/ContractRoot/ChangeLog[1]/User", "me")
		.withFieldValue("/ContractRoot/ChangeLog[1]/Number", "1")
		.withFieldValue("/ContractRoot/ChangeLog[1]/Description", "xxxxxxx")
		.withFieldValue("/ContractRoot/ChangeLog[1]/Changes[1]/Title", "title 1")
		.withFieldValue("/ContractRoot/ChangeLog[1]/Changes[1]/Status", "status 1")
		.withFieldValue("/ContractRoot/ChangeLog[1]/Changes[1]/Details", "detail 1");

	private final DocumentModelReadRepository documentModelReadRepository = mock(DocumentModelReadRepository.class);

	private final ModelPermissionEvaluator<IDocumentModel> documentModelPermissionEvaluator = mock(DefaultDocumentModelPermissionEvaluator.class);
	private final DefaultDocumentModelLoader defaultDocumentModelLoader =
		new DefaultDocumentModelLoader(documentModelPermissionEvaluator, eventPublisher, documentModelReadRepository);
	private final QueryContext queryContext =
		new DefaultQueryContext(defaultDocumentModelLoader, relationshipModelLoader, queryMethod, documentModelServiceFactory, queryContextHelper,
			indexedModelFieldCache, null, null);
	@Spy private IDocumentRepository documentRepository = mock(IDocumentRepository.class);
	@Spy private List<IDocumentRepository> documentRepositories = spy(List.of(documentRepository));
	@Spy private AggregatedDocumentRepository aggregatedDocumentRepository = spy(new AggregatedDocumentRepository(documentRepositories));
	@Spy private ObjectMapper customObjectMapper = objectMapper.rebuild().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build();

	private final DocumentProjectionImplementation documentProjection =
		new DocumentProjectionImplementation(customObjectMapper, documentTreeHelper, aggregatedDocumentRepository, documentSupport);

	@BeforeMethod
	public void setUp() {
		doAnswer(invocation -> loadDocumentModel(invocation.getArgument(0, String.class))).when(documentModelReadRepository).readModel(anyString());
	}

	@Test public void testPostProcess_shouldReturnOriginalResult_whenNoFieldsAreSpecified() {
		@NonNull Page<DocumentTreeResult> results = new PageImpl<>(List.of(DocumentTreeResult.builder()
			.docRef(CONTRACT_1_DOCUMENT_REFERENCE)
			.document(objectMapper.createObjectNode())
			.type(ROOT)
			.build()));

		doReturn(Optional.of(new DataServicesDocument() {
			@Override public DocumentV2 getKernelDocument() {
				return CONTRACT_DOCUMENT
					;
			}

			@Override public DataServicesDocumentMetadata getMetadata() {
				return new DefaultDocumentMetadata(getKernelDocument());
			}
		})).when(aggregatedDocumentRepository)
			.getByDocumentReference(eq(CONTRACT_1_DOCUMENT_REFERENCE));

		QueryPage<DocumentTreeResult> actualResults = documentProjection.postprocess(QueryRoot.builder().build(), results, queryContext);
		assertResults(actualResults, results);
	}

	@Test public void testPostProcess_shouldReturnOriginalResult_whenGivenProjectFieldWithAllFields() {
		@NonNull Page<DocumentTreeResult> results = new PageImpl<>(List.of());
		QueryPage<DocumentTreeResult> actualResults = documentProjection.postprocess(QueryRoot.builder()
				.fields(null)
				.build(),
			results, queryContext);
		assertResults(actualResults, results);
	}

	@Test public void testPostProcess_shouldProcessResultContainsOnlyProjectFields_whenGivenValidProjectFields() {
		@NonNull Page<DocumentTreeResult> results = new PageImpl<>(List.of(loadDocumentTreeResult(SINGLE_BUSINESS_PARTNER_DOCUMENT_TREE_RESULT)));
		QueryRoot query = constructQueryRootWithFields(List.of(
				"/BusinessPartnerRoot/Name",
				"/BusinessPartnerRoot/Industry",
				"/BusinessPartnerRoot/SubtypeGroup/Company"
			), ExactMatchOperator.builder()
				.field("/BusinessPartnerRoot/Name")
				.value("Konstantin")
				.build()
		);
		results.getContent().getFirst().setInternalId(query.getInternalId());
		QueryPage<DocumentTreeResult> actualResults = documentProjection.postprocess(query, results, queryContext);
		assertEquals(actualResults.getTotalElements(), 1);
		List<DocumentTreeResult> documentTreeResults = actualResults.getContent();
		assertTrue(CollectionUtils.isNotEmpty(documentTreeResults));
		DocumentTreeResult firstResult = documentTreeResults.getFirst();
		assertNotNull(firstResult);
		JsonNode documentJsonRoot = firstResult.getDocument();
		assertNotNull(documentJsonRoot);
		assertFalse(documentJsonRoot.isNull());
		assertFalse(documentJsonRoot.isEmpty());
		assertFalse(documentJsonRoot.get("BusinessPartnerRoot").isNull());
		JSONAssert.assertEquals("{"
			+ "\"Name\" : \"Konstantin\","
			+ "\"Industry\" : \"IT\","
			+ "\"SubtypeGroup\" : {"
			+ "\"Company\" : \"None\""
			+ "}"
			+ "}", documentJsonRoot.get("BusinessPartnerRoot").toString(), JSONCompareMode.NON_EXTENSIBLE);
	}

	@Override
	@SneakyThrows
	public DocumentTreeResult loadDocumentTreeResult(String documentName) {
		String path = PathConstants.DOCUMENT_TREE_RESULT_PATH + documentName + PathConstants.JSON_EXT;
		try (InputStream is = getClass().getResourceAsStream(path)) {
			return jsonMapper.rebuild()
				.addModule(new DataServicesJacksonModule(Collections.emptyList()))
				.build()
				.readValue(is, DocumentTreeResult.class);
		}
	}

	private static void assertResults(QueryPage<DocumentTreeResult> actualResults, @NotNull Page<DocumentTreeResult> results) {
		assertEquals(actualResults.getContent(), results.getContent());
		assertEquals(actualResults.getSize(), results.getSize());
		assertEquals(actualResults.getNumber(), results.getNumber());
		assertEquals(actualResults.getTotalElements(), results.getTotalElements());
	}
}
