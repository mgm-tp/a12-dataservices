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
package com.mgmtp.a12.dataservices.document.persistence.internal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.mgmtp.a12.dataservices.TestHeader;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterDeleteEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterLoadEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeDeleteEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentAfterRepositoryDeleteEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentsAfterDeleteEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentsBeforeDeleteEvent;
import com.mgmtp.a12.dataservices.document.internal.DefaultDataServicesDocument;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.jsonb.DocumentSearchJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.searchtable.DocumentFieldsJpaRepository;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.model.header.Header;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

public class OtherDefaultDocumentServiceTest extends AbstractDefaultDocumentServiceTest {

	@Mock DocumentFieldsJpaRepository documentFieldsJpaRepository;
	@Mock DocumentSearchJpaRepository documentSearchJpaRepository;

	@Test
	public void testDocumentLoad_success() {
		DocumentReference docRef = new DocumentReference(testModelName, "1");
		DocumentV2 kernelDocument = metadataUtils.createDocumentMetadata(DocumentV2.empty(testModelName), docRef, "admin", Instant.now(), null);
		DefaultDataServicesDocument dataServicesDocument = createDataServicesDocument(docRef, kernelDocument);
		DocumentTreeResult documentTreeResult = new DocumentTreeResult();
		JsonNode node = JsonNodeFactory.instance.objectNode();
		documentTreeResult.setDocument(node);
		QueryPage<Object> queryResult = QueryPage.of(List.of(documentTreeResult), 1, 0, 10, null);

		doReturn(queryResult).when(queryService).query(any(), any());
		doReturn(kernelDocument).when(documentSupport).convertJSONToDocument(any(), any());
		doReturn(dataServicesDocument).when(dataServicesDocumentFactory).newDataServicesDocument(kernelDocument);

		defaultDocumentService.load(docRef);

		verify(queryService, times(1)).query(any(), any());
		verify(eventPublisher, times(1)).publishEvent(any(DocumentAfterLoadEvent.class));
	}

	@Test
	public void testDocumentDelete_success() {
		DocumentReference docRef = new DocumentReference(testModelName, "1");
		DocumentV2 kernelDocument = DocumentV2.empty(testModelName);
		kernelDocument = metadataUtils.createDocumentMetadata(kernelDocument, docRef, "admin", Instant.now(), null);
		DataServicesDocument dataServicesDocument = createDataServicesDocument(docRef, kernelDocument);
		when(documentRepository.supports(any())).thenReturn(true);
		when(documentRepository.findByDocumentReference(any())).thenReturn(
			Optional.of(dataServicesDocument)
		);

		defaultDocumentService.delete(docRef);

		verify(documentRepository, times(1)).delete(docRef);
		verify(documentPermissionEvaluator, times(1)).checkDocumentDeletePermission(dataServicesDocument);
		verify(modelPermissionEvaluator, times(1)).checkModelReadPermission(testModelName);
		verify(attachmentHandler, times(1)).deleteAttachmentsForDocument(
			ArgumentMatchers.argThat(doc -> doc.fieldValue(DocumentMetadataConstants.DOCREF_METADATA_PATH).equals(docRef.toString())), eq(docRef));

		ArgumentCaptor<DocumentBeforeDeleteEvent> documentBeforeDeleteEventCaptor = ArgumentCaptor.forClass(DocumentBeforeDeleteEvent.class);
		verify(eventPublisher, times(1)).publishEvent(documentBeforeDeleteEventCaptor.capture());
		compareDocumentV2(documentBeforeDeleteEventCaptor.getValue().getPersistedDocument(), kernelDocument);

		ArgumentCaptor<DocumentAfterRepositoryDeleteEvent> documentAfterRepositoryDeleteEventCaptor =
			ArgumentCaptor.forClass(DocumentAfterRepositoryDeleteEvent.class);
		verify(eventPublisher, times(1)).publishEvent(documentAfterRepositoryDeleteEventCaptor.capture());
		compareDocumentV2(documentAfterRepositoryDeleteEventCaptor.getValue().getOldDocument().getKernelDocument(), dataServicesDocument.getKernelDocument());

		ArgumentCaptor<DocumentAfterDeleteEvent> documentAfterDeleteEventCaptor = ArgumentCaptor.forClass(DocumentAfterDeleteEvent.class);
		verify(eventPublisher, times(1)).publishEvent(documentAfterDeleteEventCaptor.capture());
		compareDocumentV2(documentAfterDeleteEventCaptor.getValue().getDataServicesDocument().getKernelDocument(), dataServicesDocument.getKernelDocument());
	}

	@Test
	public void testDocumentDelete_documentNotFound_success() {

		when(documentRepository.findByDocumentReference(any())).thenReturn(Optional.empty());

		defaultDocumentService.delete(new DocumentReference("TestModel/1"));

		verify(modelPermissionEvaluator, times(1)).checkModelReadPermission(any(String.class));
		verifyNoInteractions(documentPermissionEvaluator, eventPublisher, attachmentHandler);
	}

	@Test
	public void testDocumentDelete_noModelReadPermission() {
		doThrow(AccessDeniedException.class)
			.when(modelPermissionEvaluator)
			.checkModelReadPermission(any(String.class));

		assertThrows(AccessDeniedException.class, () -> defaultDocumentService.delete(new DocumentReference("TestModel/1")));

		verify(modelPermissionEvaluator, times(1)).checkModelReadPermission(any(String.class));
		verifyNoInteractions(documentRepository, documentPermissionEvaluator, eventPublisher, attachmentHandler);
	}

	@Test
	public void testDocumentDelete_noDocumentDeletePermission() {
		DocumentReference docRef = new DocumentReference("TestModel", "TestModel/1");
		when(documentRepository.findByDocumentReference(any())).thenReturn(
			Optional.of(createDataServicesDocument(docRef, null))
		);
		doThrow(AccessDeniedException.class)
			.when(documentPermissionEvaluator)
			.checkDocumentDeletePermission(any());

		assertThrows(AccessDeniedException.class, () -> defaultDocumentService.delete(new DocumentReference("TestModel/1")));

		verify(documentRepository, times(0)).delete(any());
		verify(documentPermissionEvaluator, times(1)).checkDocumentDeletePermission(any());
		verify(modelPermissionEvaluator, times(1)).checkModelReadPermission(any(String.class));

		verifyNoInteractions(eventPublisher, attachmentHandler);
	}

	@Test
	public void testDeleteAll_success() {
		Set<String> models = documentReferences.stream().map(DocumentReference::getDocumentModelName).collect(Collectors.toSet());
		List<Header> headers = new ArrayList<>();
		models.forEach(m -> {
			TestHeader testHeader = new TestHeader();
			testHeader.setId(m);
			headers.add(testHeader);
		});
		when(documentRepository.supports(any(), any())).thenReturn(true);
		dataServicesCoreProperties.getDocuments().getMultiDelete().setLimit(100);

		when(modelHeaderRepository.findAllByIdIn(any())).thenReturn(headers);

		defaultDocumentService.deleteAll(documentReferences);

		ArgumentCaptor<Header> modelPermissionEvaluatorCaptor = ArgumentCaptor.captor();
		ArgumentCaptor<String> deleteAllModelNameCaptor = ArgumentCaptor.captor();
		ArgumentCaptor<Collection<DocumentReference>> deleteAllDocRefCaptor = ArgumentCaptor.captor();

		verify(modelHeaderRepository, times(1)).findAllByIdIn(ArgumentMatchers.argThat(modelNames -> modelNames.containsAll(models)));
		verify(modelPermissionEvaluator, times(2)).checkModelReadPermission(modelPermissionEvaluatorCaptor.capture());
		assertTrue(modelPermissionEvaluatorCaptor.getAllValues().containsAll(headers));
		verify(documentPermissionEvaluator, times(1)).checkDocumentMultiDeletePermission(headers);

		verify(documentRepository, times(2)).deleteAll(deleteAllModelNameCaptor.capture(), deleteAllDocRefCaptor.capture());
		assertTrue(deleteAllModelNameCaptor.getAllValues().containsAll(models));
		assertTrue(deleteAllDocRefCaptor.getAllValues().stream()
			.flatMap(Collection::stream)
			.toList().containsAll(documentReferences)
		);
		assertTrue(deleteAllModelNameCaptor.getAllValues().containsAll(models));

		verify(attachmentHandler, times(1)).deleteAttachmentsForDocuments(documentReferences);

		ArgumentCaptor<DocumentsBeforeDeleteEvent> documentsBeforeDeleteEventArgumentCaptor =
			ArgumentCaptor.forClass(DocumentsBeforeDeleteEvent.class);

		verify(eventPublisher, times(1))
			.publishEvent(documentsBeforeDeleteEventArgumentCaptor.capture());

		assertTrue(documentsBeforeDeleteEventArgumentCaptor.getValue().getDocumentReferences().containsAll(documentReferences));

		ArgumentCaptor<DocumentsAfterDeleteEvent> documentsAfterDeleteEventArgumentCaptor =
			ArgumentCaptor.forClass(DocumentsAfterDeleteEvent.class);

		verify(eventPublisher, times(1))
			.publishEvent(documentsAfterDeleteEventArgumentCaptor.capture());

		assertTrue(documentsAfterDeleteEventArgumentCaptor.getValue().getDocumentReferences().containsAll(documentReferences));
	}

	@Test
	public void testDeleteAll_maxNumberExceeded() {
		dataServicesCoreProperties.getDocuments().getMultiDelete().setLimit(1);

		assertThrows(InvalidInputException.class, () -> defaultDocumentService.deleteAll(documentReferences));

		verifyNoInteractions(documentRepository, documentPermissionEvaluator, modelPermissionEvaluator, defaultRelationshipLinkService,
			documentRepository);
	}

	@Test
	public void testDeleteAll_noDocumentMultiDeletePermission() {
		dataServicesCoreProperties.getDocuments().getMultiDelete().setLimit(100);
		doThrow(AccessDeniedException.class)
			.when(documentPermissionEvaluator)
			.checkDocumentMultiDeletePermission(Mockito.anyCollection());

		assertThrows(AccessDeniedException.class, () -> defaultDocumentService.deleteAll(documentReferences));

		verify(documentPermissionEvaluator, times(1)).checkDocumentMultiDeletePermission(any());
		verifyNoInteractions(documentRepository, modelPermissionEvaluator, defaultRelationshipLinkService, documentRepository);
	}

	@Test
	public void testDeleteAll_noModelReadPermission() {
		when(modelHeaderRepository.findAllByIdIn(Mockito.anyCollection())).thenReturn(List.of(new ModelHeaderEntity()));

		doThrow(AccessDeniedException.class)
			.when(modelPermissionEvaluator)
			.checkModelReadPermission(any(Header.class));

		assertThrows(AccessDeniedException.class, () -> defaultDocumentService.deleteAll(documentReferences));

		verifyNoInteractions(documentRepository, defaultRelationshipLinkService, documentRepository);

		verify(documentRepository, times(0)).deleteAll(any(), Mockito.anyCollection());
	}

	@Test void testLoadForModel_success() {
		String modelId = RandomStringUtils.randomAlphabetic(20);
		PageRequest pageable = PageRequest.of(1, 50);

		when(documentRepository.findAllDocRefsForModel(any(), any())).thenReturn(documentReferences);

		List<DocumentReference> references = defaultDocumentService.loadForModel(modelId, pageable);
		Assert.assertEquals(references, documentReferences);
		verify(documentRepository, times(1)).findAllDocRefsForModel(modelId, pageable);
	}

}
