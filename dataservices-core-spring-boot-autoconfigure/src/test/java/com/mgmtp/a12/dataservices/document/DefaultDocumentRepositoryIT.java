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
package com.mgmtp.a12.dataservices.document;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.autoconfigure.DataServicesRepositoryConfiguration;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.document.internal.DefaultDataServicesDocumentFactory;
import com.mgmtp.a12.dataservices.document.internal.DefaultDocumentIdGenerator;
import com.mgmtp.a12.dataservices.document.internal.MetadataUtils;
import com.mgmtp.a12.dataservices.utils.internal.KernelUtils;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

public class DefaultDocumentRepositoryIT extends AbstractSpringContextIT {
	private DataServicesDocument docAddress;
	@Autowired private DefaultDocumentIdGenerator documentIdGenerator;
	@Autowired private DefaultDataServicesDocumentFactory dataServicesDocumentFactory;
	@Autowired private MetadataUtils metadataUtils;

	@BeforeMethod public void init() throws Exception {
		super.cleanUpTestEnvironment();

		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.ADDRESS_DOCUMENT_MODEL_PATH);

		docAddress = createMockDataServicesDocument(documentFunctions.getKernelDocumentFromFile(
				DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "Address.json"),
			DocumentModelConstants.ADDRESS_DOCUMENT_MODEL);
	}

	@Test
	public void repositoryTest() {
		DataServicesRepositoryConfiguration repository = new DataServicesRepositoryConfiguration();
		assertNotNull(repository);
	}

	@Test
	public void createDocumentTest() {
		documentRepository.create(docAddress);
		assertTrue(documentRepository.findByDocumentReference(docAddress.getMetadata().getDocRef()).isPresent());
	}

	@Test
	public void createWrongDocumentTest() {
		assertThrows(NullPointerException.class, () -> createMockDataServicesDocument(null, DocumentModelConstants.ADDRESS_DOCUMENT_MODEL));
	}

	@Test
	public void updateDocumentTest() {
		documentRepository.create(docAddress);
		assertTrue(documentRepository.findByDocumentReference(docAddress.getMetadata().getDocRef()).isPresent());
		DocumentV2 updateDocument = docAddress.getKernelDocument();
		updateDocument = modifyDocument(updateDocument, constructPart("/AddressRoot/Location", new int[] { 1, 1 }, "newLocation"));
		docAddress = dataServicesDocumentFactory.newDataServicesDocument(updateDocument);
		documentRepository.update(docAddress);
		documentRepository.findByDocumentReference(docAddress.getMetadata().getDocRef()).ifPresent(
			dataServicesDocument -> assertFieldExistsAndHasValue(dataServicesDocument.getKernelDocument(), "/AddressRoot/Location", new int[] { 1, 1 },
				"newLocation"));
	}

	@Test
	public void updateWrongDocumentTest() {
		documentRepository.create(docAddress);
		assertTrue(documentRepository.findByDocumentReference(docAddress.getMetadata().getDocRef()).isPresent());
		DocumentV2 updateDocument = docAddress.getKernelDocument();
		updateDocument = modifyDocument(updateDocument, constructPart("/AddressRoot/Location", new int[] { 1, 1 }, "newLocation"));
		docAddress = dataServicesDocumentFactory.newDataServicesDocument(updateDocument);
		DocumentReference docRef = docAddress.getMetadata().getDocRef();

		docRef.setDocumentId("null");
		final DocumentV2 updateDocument2 = metadataUtils.createDocumentMetadata(docAddress.getKernelDocument(), docRef, "tester", Instant.now(), null);
		assertThrows(NotFoundException.class, () -> documentRepository.update(dataServicesDocumentFactory.newDataServicesDocument(updateDocument2)));
	}

	@Test
	public void findDocumentsByDocRefsTest() {
		documentRepository.create(docAddress);
		assertTrue(documentRepository.findByDocumentReference(docAddress.getMetadata().getDocRef()).isPresent());
		List<DataServicesDocument> dataServicesDocumentList = documentRepository.findDocumentsByDocRefs(List.of(docAddress.getMetadata().getDocRef()));
		assertEquals(dataServicesDocumentList.getFirst().getMetadata().getDocRef(), docAddress.getMetadata().getDocRef());
	}

	@Test
	public void supportTest() {
		documentRepository.create(docAddress);
		assertTrue(documentRepository.findByDocumentReference(docAddress.getMetadata().getDocRef()).isPresent());
		assertTrue(documentRepository.supports(docAddress.getKernelDocument()));
	}

	@Transactional
	@Test public void deleteDocumentTest() {
		documentRepository.create(docAddress);
		assertTrue(documentRepository.findByDocumentReference(docAddress.getMetadata().getDocRef()).isPresent());
		documentRepository.delete(docAddress.getMetadata().getDocRef());
		Assert.assertFalse(documentRepository.findByDocumentReference(docAddress.getMetadata().getDocRef()).isPresent());
	}

	private DataServicesDocument createMockDataServicesDocument(DocumentV2 document, String modelName) {
		document = metadataUtils.createDocumentMetadata(document, new DocumentReference(modelName, documentIdGenerator.generateId(null).get()), "tester", Instant.now(), null);
		return dataServicesDocumentFactory.newDataServicesDocument(document);
	}

	private DocumentV2 modifyDocument(DocumentV2 document, DocumentPart part) {
		return document.withFieldValue(KernelUtils.fromDocumentPart(part), part.getValue());
	}

	private DocumentPart constructPart(String path, int[] repetitions, Object value) {
		return DocumentPart.builder().path(path).repetitions(repetitions).value(value).build();
	}

	private void assertFieldExistsAndHasValue(DocumentV2 document, String path, int[] repetitions, Object value) {
		DocumentPointer documentPointer = KernelUtils.fromPathAndRepetitions(
			path, repetitions
		);
		Assert.assertNotNull(document.field(documentPointer));

		if (value == null) {
			assertNull(document.fieldValue(documentPointer));
		} else {
			assertEquals(document.fieldValue(documentPointer).toString(), value.toString());
		}
	}
}
