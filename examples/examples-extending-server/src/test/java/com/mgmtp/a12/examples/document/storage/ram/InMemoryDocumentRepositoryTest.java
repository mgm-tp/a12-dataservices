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
package com.mgmtp.a12.examples.document.storage.ram;

import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DataServicesDocumentMetadata;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterDeleteEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterUpdateEvent;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Listeners(MockitoTestNGListener.class)
public class InMemoryDocumentRepositoryTest {

	private InMemoryDocumentRepository repository;

	@Mock
	private DataServicesDocument document;
	@Mock
	private DataServicesDocument oldDocument;
	@Mock
	private DataServicesDocumentMetadata metadata;
	@Mock
	private DataServicesDocumentMetadata oldMetadata;
	@Mock
	private DocumentReference docRef;
	@Mock
	private DocumentV2 docV2;
	@Mock
	private DocumentAfterCreateEvent afterCreateEvent;
	@Mock
	private DocumentAfterUpdateEvent afterUpdateEvent;
	@Mock
	private DocumentAfterDeleteEvent afterDeleteEvent;

	@BeforeMethod
	public void setUp() {
		repository = new InMemoryDocumentRepository();

		// Common stubs used in most tests
		lenient().when(document.getMetadata()).thenReturn(metadata);
		lenient().when(metadata.getDocRef()).thenReturn(docRef);
		lenient().when(docRef.getDocumentModelName()).thenReturn(InMemoryDocumentRepository.MODEL_NAME);

		// Lenient stubs used only in rollback update tests
		lenient().when(oldDocument.getMetadata()).thenReturn(oldMetadata);
		lenient().when(oldMetadata.getDocRef()).thenReturn(docRef);
		lenient().when(oldMetadata.getDocRef().getDocumentModelName()).thenReturn(InMemoryDocumentRepository.MODEL_NAME);
	}

	@Test
	public void createAndFind_document_savedAndFound() {
		repository.create(document);
		Optional<DataServicesDocument> found = repository.findByDocumentReference(docRef);
		assertTrue(found.isPresent());
		assertEquals(found.get(), document);
	}

	@Test
	public void update_document_updated() {
		repository.create(document);
		repository.update(document);
		Optional<DataServicesDocument> found = repository.findByDocumentReference(docRef);
		assertTrue(found.isPresent());
		assertEquals(found.get(), document);
	}

	@Test
	public void delete_document_removed() {
		repository.create(document);
		repository.delete(docRef);
		assertFalse(repository.findByDocumentReference(docRef).isPresent());
	}

	@Test
	public void deleteAll_modelRefs_removed() {
		repository.create(document);
		repository.deleteAll(InMemoryDocumentRepository.MODEL_NAME, List.of(docRef));
		assertFalse(repository.findByDocumentReference(docRef).isPresent());
	}

	@Test
	public void findAllDocRefsForModel_model_returnsDocRefs() {
		repository.create(document);
		List<DocumentReference> refs = repository.findAllDocRefsForModel(InMemoryDocumentRepository.MODEL_NAME);
		assertEquals(refs.size(), 1);
		assertEquals(refs.get(0), docRef);
	}

	@Test
	public void findDocumentsByDocRefs_list_returnsDocuments() {
		repository.create(document);
		List<DataServicesDocument> docs = repository.findDocumentsByDocRefs(List.of(docRef));
		assertEquals(docs.size(), 1);
		assertEquals(docs.get(0), document);
	}

	@Test
	public void supports_documentV2_correct() {
		when(docV2.getDocumentModelId()).thenReturn(InMemoryDocumentRepository.MODEL_NAME);
		assertTrue(repository.supports(docV2));
		when(docV2.getDocumentModelId()).thenReturn("OtherModel");
		assertFalse(repository.supports(docV2));
	}

	@Test
	public void supports_modelName_correct() {
		assertTrue(repository.supports(InMemoryDocumentRepository.MODEL_NAME, Optional.empty()));
		assertFalse(repository.supports("OtherModel", Optional.empty()));
	}

	@Test
	public void rollBackOrder_afterCreate_removesDocument() {
		when(afterCreateEvent.getDataServicesDocument()).thenReturn(document);
		when(document.getMetadata().getDocRef().getDocumentModelName()).thenReturn(InMemoryDocumentRepository.MODEL_NAME);

		repository.create(document);
		repository.rollBackOrder(afterCreateEvent);

		assertFalse(repository.findByDocumentReference(docRef).isPresent());
	}

	@Test
	public void rollBackOrder_afterUpdate_restoresOldDocument() {
		when(afterUpdateEvent.getOldDocument()).thenReturn(oldDocument);

		repository.create(oldDocument);
		repository.rollBackOrder(afterUpdateEvent);

		Optional<DataServicesDocument> found = repository.findByDocumentReference(docRef);
		assertTrue(found.isPresent());
		assertEquals(found.get(), oldDocument);
	}

	@Test
	public void rollBackOrder_afterDelete_restoresDocument() {
		when(afterDeleteEvent.getDataServicesDocument()).thenReturn(document);
		when(document.getMetadata().getDocRef().getDocumentModelName()).thenReturn(InMemoryDocumentRepository.MODEL_NAME);

		repository.create(document);
		repository.rollBackOrder(afterDeleteEvent);

		Optional<DataServicesDocument> found = repository.findByDocumentReference(docRef);
		assertTrue(found.isPresent());
		assertEquals(found.get(), document);
	}
}
