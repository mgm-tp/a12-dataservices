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
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentPart;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterUpdateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeIndexEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeUpdateEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentAfterRepositoryUpdateEvent;
import com.mgmtp.a12.dataservices.document.internal.DefaultDataServicesDocument;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.model.header.Header;

public class DefaultDocumentServiceUpdatingTest extends AbstractDefaultDocumentServiceTest {
	@Test
	public void testDocumentUpdate_success() {
		Locale locale = Locale.GERMANY;
		testModelName = DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
		DocumentReference docRef = new DocumentReference(testModelName, "testDocumentId");
		DocumentV2 oldKernelDocument =
			loadDocumentV2(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL + ".json");
		oldKernelDocument = metadataUtils.createDocumentMetadata(oldKernelDocument, docRef, "admin", Instant.now(), null);
		DocumentV2 updateKernelDocument = oldKernelDocument.withFieldValue(DOCUMENT_FIELD_NAME, "testUserName");
		List<String> uploadAttachmentIds = List.of(RandomStringUtils.randomAlphabetic(10));
		List<String> existedAttachmentIds = List.of(RandomStringUtils.randomAlphabetic(10));
		DefaultDataServicesDocument oldDoc = createDataServicesDocument(docRef, oldKernelDocument);
		ArgumentCaptor<DocumentBeforeIndexEvent> documentBeforeIndexEventCaptor = ArgumentCaptor.forClass(DocumentBeforeIndexEvent.class);
		ArgumentCaptor.forClass(DocumentBeforeIndexEvent.class);
		Mockito.when(documentRepository.supports(Mockito.any())).thenReturn(true);
		Mockito.when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		Mockito.when(documentRepository.findByDocumentReference(Mockito.any())).thenReturn(Optional.of((oldDoc)));
		Mockito.when(attachmentSupport.collectAttachmentIDs(Mockito.any())).thenReturn(uploadAttachmentIds, existedAttachmentIds);
		Mockito.when(kernelDocumentService.computeDocument(Mockito.any(DocumentV2.class), Mockito.eq(locale))).then(AdditionalAnswers.returnsFirstArg());

		DataServicesDocument result = defaultDocumentService.update(docRef, updateKernelDocument, locale);

		Mockito.verify(documentRepository, Mockito.times(1)).update(Mockito.any());
		Mockito.verify(documentPermissionEvaluator, Mockito.times(1)).checkDocumentUpdatePermission(oldDoc.getKernelDocument(), updateKernelDocument, docRef);
		Mockito.verify(documentRepository, Mockito.times(1)).supports(updateKernelDocument);
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(documentBeforeIndexEventCaptor.capture());
		assertUpdateDocument(oldDoc, result, uploadAttachmentIds, existedAttachmentIds, locale);

	}

	@Test
	public void testDocumentUpdate_documentNotFound() {

		Mockito.when(documentRepository.supports(Mockito.any())).thenReturn(true);
		Mockito.when(documentRepository.findByDocumentReference(Mockito.any())).thenReturn(Optional.empty());

		Assert.assertThrows(NotFoundException.class,
			() -> defaultDocumentService.update(new DocumentReference(testModelName, "documentId"), DocumentV2.empty(testModelName), null));
		Mockito.verify(documentRepository, Mockito.times(0)).update(Mockito.any());
		Mockito.verifyNoInteractions(eventPublisher, modelPermissionEvaluator, documentPermissionEvaluator, kernelDocumentService,
			attachmentHandler, attachmentSupport);
	}

	@Test
	public void testDocumentUpdate_noDocumentUpdatePermission() {
		DocumentReference docRef = new DocumentReference(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, "documentId");
		DocumentV2 kernelDocument =
			loadDocumentV2(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL + ".json");
		final DocumentV2 updateDocV2 = metadataUtils.createDocumentMetadata(kernelDocument, docRef, "admin", Instant.now(), null);
		Mockito.when(documentRepository.supports(Mockito.any())).thenReturn(true);
		Mockito.when(documentRepository.findByDocumentReference(Mockito.any())).thenReturn(Optional.of((createDataServicesDocument(docRef, kernelDocument))));
		Mockito.doThrow(AccessDeniedException.class)
			.when(documentPermissionEvaluator)
			.checkDocumentUpdatePermission(Mockito.any(), Mockito.any(), Mockito.any());

		Assert.assertThrows(AccessDeniedException.class, () -> defaultDocumentService.update(docRef, updateDocV2, null));

		Mockito.verify(documentPermissionEvaluator, Mockito.times(1)).checkDocumentUpdatePermission(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(documentRepository, Mockito.times(0)).update(Mockito.any());
		Mockito.verifyNoInteractions(eventPublisher, modelPermissionEvaluator, kernelDocumentService, attachmentHandler, attachmentSupport);
	}

	@Test
	public void testDocumentUpdate_noModelReadPermission() {
		DocumentReference docRef = new DocumentReference(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, "documentId");
		DocumentV2 kernelDocument =
			loadDocumentV2(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL + ".json");

		Mockito.when(documentRepository.supports(Mockito.any())).thenReturn(true);
		Mockito.when(modelHeaderRepository.findById(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL)).thenReturn(Optional.of(new ModelHeaderEntity()));

		Mockito.when(documentRepository.findByDocumentReference(Mockito.any())).thenReturn(Optional.of(createDataServicesDocument(docRef, null)));
		Mockito.doThrow(AccessDeniedException.class)
			.when(modelPermissionEvaluator)
			.checkModelReadPermission(Mockito.any(Header.class));

		Assert.assertThrows(AccessDeniedException.class, () -> defaultDocumentService.update(docRef, kernelDocument, null));

		Mockito.verify(documentRepository, Mockito.times(0)).update(Mockito.any());

		Mockito.verifyNoInteractions(kernelDocumentService, attachmentHandler, attachmentSupport);
		Mockito.verify(documentPermissionEvaluator, Mockito.times(1)).checkDocumentUpdatePermission(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(Mockito.any(Header.class));
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(Mockito.any(DocumentBeforeUpdateEvent.class));
		Mockito.verifyNoMoreInteractions(eventPublisher);
	}

	@Test
	public void testDocumentUpdate_modelNotFound() {

		DocumentReference docRef = new DocumentReference(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, "documentId");
		DocumentV2 kernelDocument =
			loadDocumentV2(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL + ".json");

		Mockito.when(documentRepository.supports(Mockito.any())).thenReturn(true);
		Mockito.when(modelHeaderRepository.findById(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL)).thenReturn(Optional.empty());
		Mockito.when(documentRepository.findByDocumentReference(Mockito.any())).thenReturn(Optional.of(createDataServicesDocument(docRef, null)));

		Assert.assertThrows(NotFoundException.class, () -> defaultDocumentService.update(docRef, kernelDocument, null));

		Mockito.verifyNoInteractions(kernelDocumentService, attachmentHandler, attachmentSupport, modelPermissionEvaluator);
		Mockito.verify(documentPermissionEvaluator, Mockito.times(1)).checkDocumentUpdatePermission(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(Mockito.any(DocumentBeforeUpdateEvent.class));
		Mockito.verifyNoMoreInteractions(eventPublisher);
	}

	@Test
	public void testDocumentUpdate_modelIsAbstract() {
		DocumentReference docRef = new DocumentReference(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, "documentId");
		DocumentV2 kernelDocument = DocumentV2.empty(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);

		Mockito.when(documentRepository.supports(Mockito.any())).thenReturn(true);
		Mockito.when(modelHeaderRepository.findById(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL)).thenReturn(Optional.of(headerEntity));
		Mockito.when(documentRepository.findByDocumentReference(Mockito.any())).thenReturn(Optional.of(createDataServicesDocument(docRef, null)));
		Mockito.when(documentModelUtils.isAbstract(Mockito.any())).thenReturn(true);

		Assert.assertThrows(InvalidInputException.class, () -> defaultDocumentService.update(docRef, kernelDocument, null));

		Mockito.verify(documentRepository, Mockito.times(0)).update(Mockito.any());
		Mockito.verifyNoInteractions(kernelDocumentService, attachmentHandler, attachmentSupport);

		Mockito.verify(documentModelUtils, Mockito.times(1)).isAbstract(Mockito.any());
		Mockito.verify(documentPermissionEvaluator, Mockito.times(1)).checkDocumentUpdatePermission(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(Mockito.any(Header.class));
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(Mockito.any(DocumentBeforeUpdateEvent.class));
		Mockito.verifyNoMoreInteractions(eventPublisher);
	}

	@Test
	public void testDocumentPartialUpdate_success() {
		Locale locale = Locale.GERMANY;
		DocumentReference docRef = new DocumentReference(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, "documentId");
		DocumentV2 oldKernelDocument =
			metadataUtils.createDocumentMetadata(
				loadDocumentV2(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL + ".json"),
				docRef,
				"admin",
				Instant.now(),
				null
			);

		DocumentPart documentPart = new DocumentPart(DOCUMENT_FIELD_NAME, "updatedName", new int[] { 1, 1 });
		List<String> uploadAttachmentIds = List.of(RandomStringUtils.randomAlphabetic(10));
		List<String> existedAttachmentIds = List.of(RandomStringUtils.randomAlphabetic(10));
		DataServicesDocument oldDataServiceDocument = createDataServicesDocument(docRef, oldKernelDocument);

		Mockito.when(documentRepository.supports(Mockito.any())).thenReturn(true);
		Mockito.when(modelHeaderRepository.findById(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL)).thenReturn(Optional.of(headerEntity));
		Mockito.when(documentRepository.findByDocumentReference(Mockito.any())).thenReturn(Optional.of(oldDataServiceDocument));
		Mockito.when(attachmentSupport.collectAttachmentIDs(Mockito.any())).thenReturn(uploadAttachmentIds, existedAttachmentIds);

		Mockito.when(kernelDocumentService.computeDocument(Mockito.any(DocumentV2.class), Mockito.eq(locale))).then(AdditionalAnswers.returnsFirstArg());

		DataServicesDocument result = defaultDocumentService.update(docRef, List.of(documentPart), locale);

		Mockito.verify(documentRepository, Mockito.times(1)).update(Mockito.any());

		Mockito.verify(documentRepository, Mockito.times(1)).findByDocumentReference(docRef);

		Mockito.verify(documentPermissionEvaluator, Mockito.times(1)).checkDocumentPartialUpdatePermission(Mockito.eq(oldKernelDocument), Mockito.any(), Mockito.eq(docRef));

		assertUpdateDocument(oldDataServiceDocument, result, uploadAttachmentIds, existedAttachmentIds,
			locale);
	}

	@Test
	public void testDocumentPartialUpdate_documentNotFound() {

		Mockito.when(documentRepository.findByDocumentReference(Mockito.any())).thenReturn(Optional.empty());

		Assert.assertThrows(NotFoundException.class, () -> defaultDocumentService.update(new DocumentReference(testModelName, "documentId"), List.of(), null));

		Mockito.verify(documentRepository, Mockito.times(0)).update(Mockito.any());
		Mockito.verifyNoInteractions(documentPermissionEvaluator, modelPermissionEvaluator, eventPublisher, modelHeaderRepository,
			documentServiceFactory, documentFactory, documentUtils);
	}

	@Test
	public void testDocumentPartialUpdate_noDocumentPartialUpdatePermission() {

		DocumentReference docRef = new DocumentReference(testModelName, "documentId");
		Mockito.when(documentRepository.findByDocumentReference(Mockito.any())).thenReturn(Optional.of(createDataServicesDocument(docRef, null)));
		Mockito.doThrow(AccessDeniedException.class)
			.when(documentPermissionEvaluator)
			.checkDocumentPartialUpdatePermission(Mockito.any(), Mockito.any(), Mockito.any());

		Assert.assertThrows(AccessDeniedException.class,
			() -> defaultDocumentService.update(new DocumentReference(testModelName, "documentId"), List.of(), null));

		Mockito.verify(documentPermissionEvaluator, Mockito.times(1)).checkDocumentPartialUpdatePermission(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verifyNoInteractions(modelPermissionEvaluator, eventPublisher, modelHeaderRepository, documentFactory, documentUtils);
	}

	@Test(expectedExceptions = InvalidInputException.class, expectedExceptionsMessageRegExp = "Invalid documentPart for partial modify document")
	public void testDocumentPartialUpdate_modifyDocument_throwException() {
		DocumentReference docRef = new DocumentReference(testModelName, "documentId");
		DocumentPart documentPart = new DocumentPart(RandomStringUtils.randomAlphabetic(10), RandomStringUtils.randomAlphabetic(12), new int[] { 1 });

		Mockito.when(documentRepository.findByDocumentReference(Mockito.any())).thenReturn(Optional.of(createDataServicesDocument(docRef, null)));

		Mockito.when(documentRepository.supports(Mockito.any())).thenReturn(true);
		Mockito.when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));

		defaultDocumentService.update(new DocumentReference(testModelName, "documentId"), List.of(documentPart), null);
	}

	@Test
	public void testDocumentPartialUpdate_modelIsAbstract() {

		DocumentReference docRef = new DocumentReference(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, "testDocumentId");
		DocumentV2 oldKernelDocument =
			metadataUtils.createDocumentMetadata(
				loadDocumentV2(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL + ".json"),
				docRef,
				"admin",
				Instant.now(),
				null
			);
		Mockito.when(documentRepository.findByDocumentReference(Mockito.any())).thenReturn(Optional.of(createDataServicesDocument(docRef, oldKernelDocument)));
		Mockito.when(documentRepository.supports(Mockito.any())).thenReturn(true);
		Mockito.when(modelHeaderRepository.findById(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL)).thenReturn(Optional.of(new ModelHeaderEntity()));
		Mockito.when(documentModelUtils.isAbstract(Mockito.any())).thenReturn(true);

		Assert.assertThrows(InvalidInputException.class,
			() -> defaultDocumentService.update(docRef, List.of(), null));

		Mockito.verify(documentRepository, Mockito.times(0)).update(Mockito.any());
		Mockito.verify(documentPermissionEvaluator, Mockito.times(1)).checkDocumentPartialUpdatePermission(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(Mockito.any(Header.class));
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(Mockito.any(DocumentBeforeUpdateEvent.class));
		Mockito.verifyNoMoreInteractions(eventPublisher);
	}

	private void assertUpdateDocument(@NotNull DataServicesDocument oldDataServiceDocument, DataServicesDocument updatedDataServiceDocument,
		List<String> uploadAttachmentIds, List<String> existedAttachmentIds, Locale locale) {

		DocumentV2 updatedDocumentV2 = updatedDataServiceDocument.getKernelDocument();
		DocumentV2 oldDocumentV2 = oldDataServiceDocument.getKernelDocument();

		Assert.assertNotEquals(updatedDocumentV2.fieldValue(DOCUMENT_FIELD_NAME), oldDocumentV2.fieldValue(DOCUMENT_FIELD_NAME));
		Assert.assertNotEquals(updatedDocumentV2.fieldValue(DocumentMetadataConstants.MODIFIED_AT_PATH),
			oldDocumentV2.fieldValue(DocumentMetadataConstants.MODIFIED_AT_PATH));
		Assert.assertNotEquals(updatedDocumentV2.fieldValue(DocumentMetadataConstants.MODIFIER_PATH),
			oldDocumentV2.fieldValue(DocumentMetadataConstants.MODIFIER_PATH));

		ArgumentCaptor<DocumentV2> collectAttachmentIDsCaptor = ArgumentCaptor.forClass(DocumentV2.class);
		Mockito.verify(attachmentSupport, Mockito.times(2)).collectAttachmentIDs(collectAttachmentIDsCaptor.capture());
		Assert.assertEquals(collectAttachmentIDsCaptor.getAllValues().size(), 2);

		Mockito.verify(attachmentHandler, Mockito.times(1)).synchronizeAttachments(
			Mockito.eq(uploadAttachmentIds),
			Mockito.eq(existedAttachmentIds),
			ArgumentMatchers.argThat(m ->
				m.equals(updatedDataServiceDocument.getMetadata().getDocRef())
			)
		);
		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(headerEntity);
		Mockito.verify(kernelDocumentService, Mockito.times(1)).computeDocument(Mockito.any(), Mockito.eq(locale));
		Mockito.verify(kernelDocumentService, Mockito.times(1)).validateDocument(Mockito.any(), Mockito.eq(locale));

		ArgumentCaptor<DocumentBeforeUpdateEvent> documentBeforeUpdateEventCaptor = ArgumentCaptor.forClass(DocumentBeforeUpdateEvent.class);
		ArgumentCaptor<DocumentAfterUpdateEvent> documentAfterUpdateEventCaptor = ArgumentCaptor.forClass(DocumentAfterUpdateEvent.class);
		ArgumentCaptor<DocumentAfterRepositoryUpdateEvent> documentAfterRepositoryUpdateEventCaptor =
			ArgumentCaptor.forClass(DocumentAfterRepositoryUpdateEvent.class);

		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(documentBeforeUpdateEventCaptor.capture());
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(documentAfterUpdateEventCaptor.capture());
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(documentAfterRepositoryUpdateEventCaptor.capture());

		compareDocumentV2(
			documentBeforeUpdateEventCaptor.getValue().getUpdatedDocument(),
			updatedDocumentV2,
			List.of(DocumentMetadataConstants.DOCREF_METADATA_PATH, DocumentMetadataConstants.MODIFIER_PATH, DocumentMetadataConstants.MODIFIED_AT_PATH,
				DOCUMENT_FIELD_NAME)
		);

		compareDocumentV2(
			documentAfterUpdateEventCaptor.getValue().getNewDocument().getKernelDocument(),
			updatedDocumentV2,
			List.of(DocumentMetadataConstants.DOCREF_METADATA_PATH, DocumentMetadataConstants.MODIFIER_PATH, DocumentMetadataConstants.MODIFIED_AT_PATH,
				DOCUMENT_FIELD_NAME)
		);

		compareDocumentV2(
			documentAfterUpdateEventCaptor.getValue().getOldDocument().getKernelDocument(),
			oldDocumentV2,
			List.of(DocumentMetadataConstants.DOCREF_METADATA_PATH, DocumentMetadataConstants.MODIFIER_PATH, DocumentMetadataConstants.MODIFIED_AT_PATH,
				DOCUMENT_FIELD_NAME)
		);

		compareDocumentV2(
			documentAfterRepositoryUpdateEventCaptor.getValue().getNewDocument().getKernelDocument(),
			updatedDocumentV2,
			List.of(DocumentMetadataConstants.DOCREF_METADATA_PATH, DocumentMetadataConstants.MODIFIER_PATH, DocumentMetadataConstants.MODIFIED_AT_PATH,
				DOCUMENT_FIELD_NAME)
		);

		compareDocumentV2(
			documentAfterRepositoryUpdateEventCaptor.getValue().getOldDocument().getKernelDocument(),
			oldDocumentV2,
			List.of(DocumentMetadataConstants.DOCREF_METADATA_PATH, DocumentMetadataConstants.MODIFIER_PATH, DocumentMetadataConstants.MODIFIED_AT_PATH,
				DOCUMENT_FIELD_NAME)
		);
	}
}
