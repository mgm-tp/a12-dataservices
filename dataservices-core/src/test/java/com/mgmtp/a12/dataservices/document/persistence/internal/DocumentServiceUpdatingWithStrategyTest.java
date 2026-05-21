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
import com.mgmtp.a12.dataservices.document.persistence.DocumentComputationStrategy;
import com.mgmtp.a12.dataservices.document.persistence.DocumentValidationStrategy;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.model.header.Header;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class DocumentServiceUpdatingWithStrategyTest extends AbstractDefaultDocumentServiceTest {

	@Test public void testDocumentUpdate_defaultConfig_success() {
		Locale locale = Locale.GERMANY;
		DocumentReference docRef = new DocumentReference(testModelName, "testDocumentId");
		DocumentV2 oldKernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);
		oldKernelDocument = metadataUtils.createDocumentMetadata(oldKernelDocument, docRef, "admin", Instant.now(), null);
		DefaultDataServicesDocument oldDoc = createDataServicesDocument(docRef, oldKernelDocument);
		DocumentV2 updatedKernelDocument = oldKernelDocument.withFieldValue(DOCUMENT_FIELD_NAME, "testUserName");
		List<String> existedAttachmentIds = List.of(RandomStringUtils.secure().nextAlphabetic(10));
		List<String> uploadAttachmentIds = List.of(RandomStringUtils.secure().nextAlphabetic(10));

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentRepository.findByDocumentReference(any())).thenReturn(Optional.of((oldDoc)));
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		when(kernelDocumentService.computeDocument(any(DocumentV2.class), Mockito.eq(locale))).then(AdditionalAnswers.returnsFirstArg());
		when(attachmentSupport.collectAttachmentIDs(any())).thenReturn(uploadAttachmentIds, existedAttachmentIds);

		DataServicesDocument result = defaultDocumentService.update(docRef, updatedKernelDocument, locale,
			DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION);

		verify(documentRepository).supports(updatedKernelDocument);
		verify(documentPermissionEvaluator).checkDocumentUpdatePermission(oldDoc.getKernelDocument(), updatedKernelDocument, docRef);
		verify(kernelDocumentService).computeDocument(any(DocumentV2.class), Mockito.eq(locale));
		verify(kernelDocumentService, never()).compute(any(DocumentV2.class), Mockito.eq(locale));
		verify(kernelDocumentService).validateDocument(any(DocumentV2.class), Mockito.eq(locale));
		verify(kernelDocumentService, never()).validateFull(any(DocumentV2.class), Mockito.eq(locale));
		verify(kernelDocumentService, never()).validatePartially(any(DocumentV2.class), Mockito.eq(locale));
		verify(documentRepository).update(any());
		verify(eventPublisher).publishEvent(any(DocumentBeforeIndexEvent.class));
		assertUpdateDocument(oldDoc, result, uploadAttachmentIds, existedAttachmentIds);
	}

	@Test public void testDocumentUpdate_nullValidationStrategy() {
		Locale locale = Locale.GERMANY;
		DocumentReference docRef = new DocumentReference(testModelName, "testDocumentId");
		DocumentV2 oldKernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);
		oldKernelDocument = metadataUtils.createDocumentMetadata(oldKernelDocument, docRef, "admin", Instant.now(), null);
		DocumentV2 updatedKernelDocument = oldKernelDocument.withFieldValue(DOCUMENT_FIELD_NAME, "testUserName");

		Assert.assertThrows(NullPointerException.class, () ->
			defaultDocumentService.update(docRef, updatedKernelDocument, locale, null, DocumentComputationStrategy.DEFAULT_CONFIGURATION));
	}

	@Test public void testDocumentUpdate_nullComputationStrategy() {
		Locale locale = Locale.GERMANY;
		DocumentReference docRef = new DocumentReference(testModelName, "testDocumentId");
		DocumentV2 oldKernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);
		oldKernelDocument = metadataUtils.createDocumentMetadata(oldKernelDocument, docRef, "admin", Instant.now(), null);
		DocumentV2 updatedKernelDocument = oldKernelDocument.withFieldValue(DOCUMENT_FIELD_NAME, "testUserName");

		Assert.assertThrows(NullPointerException.class, () ->
			defaultDocumentService.update(docRef, updatedKernelDocument, locale, DocumentValidationStrategy.DEFAULT_CONFIGURATION, null));
	}

	@Test public void testDocumentUpdate_fullComputation_success() {
		Locale locale = Locale.GERMANY;
		DocumentReference docRef = new DocumentReference(testModelName, "testDocumentId");
		DocumentV2 oldKernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);
		oldKernelDocument = metadataUtils.createDocumentMetadata(oldKernelDocument, docRef, "admin", Instant.now(), null);
		DefaultDataServicesDocument oldDoc = createDataServicesDocument(docRef, oldKernelDocument);
		DocumentV2 updatedKernelDocument = oldKernelDocument.withFieldValue(DOCUMENT_FIELD_NAME, "testUserName");
		List<String> existedAttachmentIds = List.of(RandomStringUtils.secure().nextAlphabetic(10));
		List<String> uploadAttachmentIds = List.of(RandomStringUtils.secure().nextAlphabetic(10));

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentRepository.findByDocumentReference(any())).thenReturn(Optional.of((oldDoc)));
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		when(kernelDocumentService.compute(any(DocumentV2.class), Mockito.eq(locale))).then(AdditionalAnswers.returnsFirstArg());
		when(attachmentSupport.collectAttachmentIDs(any())).thenReturn(uploadAttachmentIds, existedAttachmentIds);

		DataServicesDocument result = defaultDocumentService.update(docRef, updatedKernelDocument, locale,
			DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.FULL_COMPUTATION);

		verify(documentRepository).supports(updatedKernelDocument);
		verify(documentPermissionEvaluator).checkDocumentUpdatePermission(oldDoc.getKernelDocument(), updatedKernelDocument, docRef);
		verify(kernelDocumentService).compute(any(DocumentV2.class), Mockito.eq(locale));
		verify(kernelDocumentService, never()).computeDocument(any(DocumentV2.class), Mockito.eq(locale));
		verify(documentRepository).update(any());
		verify(eventPublisher).publishEvent(any(DocumentBeforeIndexEvent.class));
		assertUpdateDocument(oldDoc, result, uploadAttachmentIds, existedAttachmentIds);
	}

	@Test public void testDocumentUpdate_noComputation_success() {
		Locale locale = Locale.GERMANY;
		DocumentReference docRef = new DocumentReference(testModelName, "testDocumentId");
		DocumentV2 oldKernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);
		oldKernelDocument = metadataUtils.createDocumentMetadata(oldKernelDocument, docRef, "admin", Instant.now(), null);
		DefaultDataServicesDocument oldDoc = createDataServicesDocument(docRef, oldKernelDocument);
		DocumentV2 updatedKernelDocument = oldKernelDocument.withFieldValue(DOCUMENT_FIELD_NAME, "testUserName");
		List<String> existedAttachmentIds = List.of(RandomStringUtils.secure().nextAlphabetic(10));
		List<String> uploadAttachmentIds = List.of(RandomStringUtils.secure().nextAlphabetic(10));

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentRepository.findByDocumentReference(any())).thenReturn(Optional.of((oldDoc)));
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		when(attachmentSupport.collectAttachmentIDs(any())).thenReturn(uploadAttachmentIds, existedAttachmentIds);

		DataServicesDocument result = defaultDocumentService.update(docRef, updatedKernelDocument, locale,
			DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.NO_COMPUTATION);

		verify(documentRepository).supports(updatedKernelDocument);
		verify(documentPermissionEvaluator).checkDocumentUpdatePermission(oldDoc.getKernelDocument(), updatedKernelDocument, docRef);
		verify(kernelDocumentService, never()).compute(any(DocumentV2.class), Mockito.eq(locale));
		verify(kernelDocumentService, never()).computeDocument(any(DocumentV2.class), Mockito.eq(locale));
		verify(documentRepository).update(any());
		verify(eventPublisher).publishEvent(any(DocumentBeforeIndexEvent.class));
		assertUpdateDocument(oldDoc, result, uploadAttachmentIds, existedAttachmentIds);
	}

	@Test public void testDocumentUpdate_fullValidation_success() {
		Locale locale = Locale.GERMANY;
		DocumentReference docRef = new DocumentReference(testModelName, "testDocumentId");
		DocumentV2 oldKernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);
		oldKernelDocument = metadataUtils.createDocumentMetadata(oldKernelDocument, docRef, "admin", Instant.now(), null);
		DefaultDataServicesDocument oldDoc = createDataServicesDocument(docRef, oldKernelDocument);
		DocumentV2 updatedKernelDocument = oldKernelDocument.withFieldValue(DOCUMENT_FIELD_NAME, "testUserName");
		List<String> existedAttachmentIds = List.of(RandomStringUtils.secure().nextAlphabetic(10));
		List<String> uploadAttachmentIds = List.of(RandomStringUtils.secure().nextAlphabetic(10));

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentRepository.findByDocumentReference(any())).thenReturn(Optional.of((oldDoc)));
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		when(kernelDocumentService.computeDocument(any(DocumentV2.class), Mockito.eq(locale))).then(AdditionalAnswers.returnsFirstArg());
		when(attachmentSupport.collectAttachmentIDs(any())).thenReturn(uploadAttachmentIds, existedAttachmentIds);

		DataServicesDocument result = defaultDocumentService.update(docRef, updatedKernelDocument, locale,
			DocumentValidationStrategy.FULL_VALIDATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION);

		verify(documentRepository).supports(updatedKernelDocument);
		verify(documentPermissionEvaluator).checkDocumentUpdatePermission(oldDoc.getKernelDocument(), updatedKernelDocument, docRef);
		verify(kernelDocumentService).validateFull(any(DocumentV2.class), Mockito.eq(locale));
		verify(kernelDocumentService, never()).validateDocument(any(DocumentV2.class), Mockito.eq(locale));
		verify(kernelDocumentService, never()).validatePartially(any(DocumentV2.class), Mockito.eq(locale));
		verify(documentRepository).update(any());
		verify(eventPublisher).publishEvent(any(DocumentBeforeIndexEvent.class));
		assertUpdateDocument(oldDoc, result, uploadAttachmentIds, existedAttachmentIds);
	}

	@Test public void testDocumentUpdate_partialValidation_success() {
		Locale locale = Locale.GERMANY;
		DocumentReference docRef = new DocumentReference(testModelName, "testDocumentId");
		DocumentV2 oldKernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);
		oldKernelDocument = metadataUtils.createDocumentMetadata(oldKernelDocument, docRef, "admin", Instant.now(), null);
		DefaultDataServicesDocument oldDoc = createDataServicesDocument(docRef, oldKernelDocument);
		DocumentV2 updatedKernelDocument = oldKernelDocument.withFieldValue(DOCUMENT_FIELD_NAME, "testUserName");
		List<String> existedAttachmentIds = List.of(RandomStringUtils.secure().nextAlphabetic(10));
		List<String> uploadAttachmentIds = List.of(RandomStringUtils.secure().nextAlphabetic(10));

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentRepository.findByDocumentReference(any())).thenReturn(Optional.of((oldDoc)));
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		when(kernelDocumentService.computeDocument(any(DocumentV2.class), Mockito.eq(locale))).then(AdditionalAnswers.returnsFirstArg());
		when(attachmentSupport.collectAttachmentIDs(any())).thenReturn(uploadAttachmentIds, existedAttachmentIds);

		DataServicesDocument result = defaultDocumentService.update(docRef, updatedKernelDocument, locale,
			DocumentValidationStrategy.PARTIAL_VALIDATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION);

		verify(documentRepository).supports(updatedKernelDocument);
		verify(documentPermissionEvaluator).checkDocumentUpdatePermission(oldDoc.getKernelDocument(), updatedKernelDocument, docRef);
		verify(kernelDocumentService).validatePartially(any(DocumentV2.class), Mockito.eq(locale));
		verify(kernelDocumentService, never()).validateDocument(any(DocumentV2.class), Mockito.eq(locale));
		verify(kernelDocumentService, never()).validateFull(any(DocumentV2.class), Mockito.eq(locale));
		verify(documentRepository).update(any());
		verify(eventPublisher).publishEvent(any(DocumentBeforeIndexEvent.class));
		assertUpdateDocument(oldDoc, result, uploadAttachmentIds, existedAttachmentIds);
	}

	@Test public void testDocumentUpdate_noValidation_success() {
		Locale locale = Locale.GERMANY;
		DocumentReference docRef = new DocumentReference(testModelName, "testDocumentId");
		DocumentV2 oldKernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);
		oldKernelDocument = metadataUtils.createDocumentMetadata(oldKernelDocument, docRef, "admin", Instant.now(), null);
		DefaultDataServicesDocument oldDoc = createDataServicesDocument(docRef, oldKernelDocument);
		DocumentV2 updatedKernelDocument = oldKernelDocument.withFieldValue(DOCUMENT_FIELD_NAME, "testUserName");
		List<String> existedAttachmentIds = List.of(RandomStringUtils.secure().nextAlphabetic(10));
		List<String> uploadAttachmentIds = List.of(RandomStringUtils.secure().nextAlphabetic(10));

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentRepository.findByDocumentReference(any())).thenReturn(Optional.of((oldDoc)));
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		when(kernelDocumentService.computeDocument(any(DocumentV2.class), Mockito.eq(locale))).then(AdditionalAnswers.returnsFirstArg());
		when(attachmentSupport.collectAttachmentIDs(any())).thenReturn(uploadAttachmentIds, existedAttachmentIds);

		DataServicesDocument result = defaultDocumentService.update(docRef, updatedKernelDocument, locale,
			DocumentValidationStrategy.NO_VALIDATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION);

		verify(documentRepository).supports(updatedKernelDocument);
		verify(documentPermissionEvaluator).checkDocumentUpdatePermission(oldDoc.getKernelDocument(), updatedKernelDocument, docRef);
		verify(kernelDocumentService, never()).validateDocument(any(DocumentV2.class), Mockito.eq(locale));
		verify(kernelDocumentService, never()).validateFull(any(DocumentV2.class), Mockito.eq(locale));
		verify(kernelDocumentService, never()).validatePartially(any(DocumentV2.class), Mockito.eq(locale));
		verify(documentRepository).update(any());
		verify(eventPublisher).publishEvent(any(DocumentBeforeIndexEvent.class));
		assertUpdateDocument(oldDoc, result, uploadAttachmentIds, existedAttachmentIds);
	}

	@Test public void testDocumentUpdate_documentNotFound() {
		when(documentRepository.supports(any())).thenReturn(true);
		when(documentRepository.findByDocumentReference(any())).thenReturn(Optional.empty());

		Assert.assertThrows(NotFoundException.class, () ->
			defaultDocumentService.update(new DocumentReference(testModelName, "documentId"), DocumentV2.empty(testModelName), null,
				DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION));

		verify(documentRepository, never()).update(any());
		verifyNoInteractions(eventPublisher, modelPermissionEvaluator, documentPermissionEvaluator, kernelDocumentService,
			attachmentHandler, attachmentSupport);
	}

	@Test
	public void testDocumentUpdate_noDocumentUpdatePermission() {
		DocumentReference docRef = new DocumentReference(testModelName, "testDocumentId");
		DocumentV2 oldKernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);
		final DocumentV2 updatedDocV2 = metadataUtils.createDocumentMetadata(oldKernelDocument, docRef, "admin", Instant.now(), null);

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentRepository.findByDocumentReference(any())).thenReturn(Optional.of((createDataServicesDocument(docRef, oldKernelDocument))));
		doThrow(AccessDeniedException.class)
			.when(documentPermissionEvaluator)
			.checkDocumentUpdatePermission(any(), any(), any());

		Assert.assertThrows(AccessDeniedException.class, () ->
			defaultDocumentService.update(docRef, updatedDocV2, null,
				DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION));

		verify(documentPermissionEvaluator).checkDocumentUpdatePermission(any(), any(), any());
		verify(documentRepository, never()).update(any());
		verifyNoInteractions(eventPublisher, modelPermissionEvaluator, kernelDocumentService, attachmentHandler, attachmentSupport);
	}

	@Test public void testDocumentUpdate_noModelReadPermission() {
		DocumentReference docRef = new DocumentReference(testModelName, "testDocumentId");
		DocumentV2 oldKernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);
		final DocumentV2 updatedKernelDocument = metadataUtils.createDocumentMetadata(oldKernelDocument, docRef, "admin", Instant.now(), null);

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentRepository.findByDocumentReference(any())).thenReturn(Optional.of(createDataServicesDocument(docRef, null)));
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));

		doThrow(AccessDeniedException.class)
			.when(modelPermissionEvaluator)
			.checkModelReadPermission(any(Header.class));
		Assert.assertThrows(AccessDeniedException.class, () ->
			defaultDocumentService.update(docRef, updatedKernelDocument, null,
				DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION));

		verify(documentPermissionEvaluator).checkDocumentUpdatePermission(any(), any(), any());
		verify(documentRepository).supports(updatedKernelDocument);
		verify(eventPublisher).publishEvent(any(DocumentBeforeUpdateEvent.class));
		verify(modelPermissionEvaluator).checkModelReadPermission(headerEntity);
		verify(documentRepository, never()).update(any());
		verifyNoMoreInteractions(documentPermissionEvaluator, documentRepository, eventPublisher, modelPermissionEvaluator);
	}

	@Test public void testDocumentUpdate_modelNotFound() {
		DocumentReference docRef = new DocumentReference(testModelName, "testDocumentId");
		DocumentV2 oldKernelDocument = DocumentV2.empty(testModelName);
		final DocumentV2 updatedKernelDocument = metadataUtils.createDocumentMetadata(oldKernelDocument, docRef, "admin", Instant.now(), null);

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentRepository.findByDocumentReference(any())).thenReturn(Optional.of(createDataServicesDocument(docRef, null)));
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.empty());

		Assert.assertThrows(NotFoundException.class, () ->
			defaultDocumentService.update(docRef, updatedKernelDocument, null,
				DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION));

		verify(documentPermissionEvaluator).checkDocumentUpdatePermission(any(), any(), any());
		verify(eventPublisher).publishEvent(any(DocumentBeforeUpdateEvent.class));
		verifyNoMoreInteractions(eventPublisher);
		verify(documentRepository, never()).update(any());
		verifyNoInteractions(kernelDocumentService, attachmentHandler, attachmentSupport, modelPermissionEvaluator);
	}

	@Test public void testDocumentUpdate_modelIsAbstract() {
		DocumentReference docRef = new DocumentReference(testModelName, "testDocumentId");
		DocumentV2 oldKernelDocument = DocumentV2.empty(testModelName);
		final DocumentV2 updatedKernelDocument = metadataUtils.createDocumentMetadata(oldKernelDocument, docRef, "admin", Instant.now(), null);

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentRepository.findByDocumentReference(any())).thenReturn(Optional.of(createDataServicesDocument(docRef, null)));
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		when(documentModelUtils.isAbstract(any())).thenReturn(true);

		Assert.assertThrows(InvalidInputException.class, () ->
			defaultDocumentService.update(docRef, updatedKernelDocument, null,
				DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION));

		verify(documentPermissionEvaluator).checkDocumentUpdatePermission(any(), any(), any());
		verify(documentRepository).supports(updatedKernelDocument);
		verify(eventPublisher).publishEvent(any(DocumentBeforeUpdateEvent.class));
		verify(modelPermissionEvaluator).checkModelReadPermission(headerEntity);
		verify(documentModelUtils).isAbstract(any());
		verify(documentRepository, never()).update(any());
		verifyNoMoreInteractions(documentPermissionEvaluator, documentRepository, eventPublisher, modelPermissionEvaluator);
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
		List<String> uploadAttachmentIds = List.of(RandomStringUtils.secure().nextAlphabetic(10));
		List<String> existedAttachmentIds = List.of(RandomStringUtils.secure().nextAlphabetic(10));
		DataServicesDocument oldDataServiceDocument = createDataServicesDocument(docRef, oldKernelDocument);

		when(documentRepository.supports(any())).thenReturn(true);
		when(modelHeaderRepository.findById(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL)).thenReturn(Optional.of(headerEntity));
		when(documentRepository.findByDocumentReference(any())).thenReturn(Optional.of(oldDataServiceDocument));
		when(attachmentSupport.collectAttachmentIDs(any())).thenReturn(uploadAttachmentIds, existedAttachmentIds);
		when(kernelDocumentService.computeDocument(any(DocumentV2.class), Mockito.eq(locale))).then(AdditionalAnswers.returnsFirstArg());

		DataServicesDocument result = defaultDocumentService.update(docRef, List.of(documentPart), locale,
			DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION);

		verify(documentRepository).update(any());
		verify(documentRepository).findByDocumentReference(docRef);
		verify(documentPermissionEvaluator).checkDocumentPartialUpdatePermission(Mockito.eq(oldKernelDocument), any(), Mockito.eq(docRef));

		assertUpdateDocument(oldDataServiceDocument, result, uploadAttachmentIds, existedAttachmentIds);
	}

	@Test
	public void testDocumentPartialUpdate_documentNotFound() {
		when(documentRepository.findByDocumentReference(any())).thenReturn(Optional.empty());

		Assert.assertThrows(NotFoundException.class, () -> defaultDocumentService.update(new DocumentReference(testModelName, "documentId"), List.of(), null,
			DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION));

		verify(documentRepository, times(0)).update(any());
		verifyNoInteractions(documentPermissionEvaluator, modelPermissionEvaluator, eventPublisher, modelHeaderRepository,
			documentServiceFactory, documentFactory, documentUtils);
	}

	@Test
	public void testDocumentPartialUpdate_noDocumentPartialUpdatePermission() {
		DocumentReference docRef = new DocumentReference(testModelName, "documentId");
		when(documentRepository.findByDocumentReference(any())).thenReturn(Optional.of(createDataServicesDocument(docRef, null)));

		doThrow(AccessDeniedException.class)
			.when(documentPermissionEvaluator)
			.checkDocumentPartialUpdatePermission(any(), any(), any());

		Assert.assertThrows(AccessDeniedException.class,
			() -> defaultDocumentService.update(new DocumentReference(testModelName, "documentId"), List.of(), null,
				DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION));

		verify(documentPermissionEvaluator).checkDocumentPartialUpdatePermission(any(), any(), any());
		verifyNoInteractions(modelPermissionEvaluator, eventPublisher, modelHeaderRepository, documentFactory, documentUtils);
	}

	@Test(expectedExceptions = InvalidInputException.class, expectedExceptionsMessageRegExp = "Invalid documentPart for partial modify document")
	public void testDocumentPartialUpdate_modifyDocument_throwException() {
		DocumentReference docRef = new DocumentReference(testModelName, "documentId");
		DocumentPart documentPart = new DocumentPart(RandomStringUtils.secure().nextAlphabetic(10), RandomStringUtils.secure().nextAlphabetic(12), new int[] { 1 });

		when(documentRepository.findByDocumentReference(any())).thenReturn(Optional.of(createDataServicesDocument(docRef, null)));
		when(documentRepository.supports(any())).thenReturn(true);
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));

		defaultDocumentService.update(new DocumentReference(testModelName, "documentId"), List.of(documentPart), null,
			DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION);
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

		when(documentRepository.findByDocumentReference(any())).thenReturn(Optional.of(createDataServicesDocument(docRef, oldKernelDocument)));
		when(documentRepository.supports(any())).thenReturn(true);
		when(modelHeaderRepository.findById(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL)).thenReturn(Optional.of(new ModelHeaderEntity()));
		when(documentModelUtils.isAbstract(any())).thenReturn(true);

		Assert.assertThrows(InvalidInputException.class, () -> defaultDocumentService.update(docRef, List.of(), null,
			DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION));

		verify(documentRepository, times(0)).update(any());
		verify(documentPermissionEvaluator).checkDocumentPartialUpdatePermission(any(), any(), any());
		verify(modelPermissionEvaluator).checkModelReadPermission(any(Header.class));
		verify(eventPublisher).publishEvent(any(DocumentBeforeUpdateEvent.class));
		verifyNoMoreInteractions(eventPublisher);
	}

	private void assertUpdateDocument(@NotNull DataServicesDocument oldDataServicesDocument, DataServicesDocument updatedDataServicesDocument,
		List<String> uploadAttachmentIds, List<String> existedAttachmentIds) {
		DocumentV2 oldDocumentV2 = oldDataServicesDocument.getKernelDocument();
		DocumentV2 updatedDocumentV2 = updatedDataServicesDocument.getKernelDocument();

		Assert.assertNotEquals(updatedDocumentV2.fieldValue(DOCUMENT_FIELD_NAME), oldDocumentV2.fieldValue(DOCUMENT_FIELD_NAME));
		Assert.assertNotEquals(updatedDocumentV2.fieldValue(DocumentMetadataConstants.MODIFIED_AT_PATH),
			oldDocumentV2.fieldValue(DocumentMetadataConstants.MODIFIED_AT_PATH));
		Assert.assertNotEquals(updatedDocumentV2.fieldValue(DocumentMetadataConstants.MODIFIER_PATH),
			oldDocumentV2.fieldValue(DocumentMetadataConstants.MODIFIER_PATH));

		ArgumentCaptor<DocumentV2> collectAttachmentIDsCaptor = ArgumentCaptor.forClass(DocumentV2.class);
		ArgumentCaptor<DocumentBeforeUpdateEvent> documentBeforeUpdateEventCaptor = ArgumentCaptor.forClass(DocumentBeforeUpdateEvent.class);
		ArgumentCaptor<DocumentAfterUpdateEvent> documentAfterUpdateEventCaptor = ArgumentCaptor.forClass(DocumentAfterUpdateEvent.class);
		ArgumentCaptor<DocumentAfterRepositoryUpdateEvent> documentAfterRepositoryUpdateEventCaptor =
			ArgumentCaptor.forClass(DocumentAfterRepositoryUpdateEvent.class);

		verify(attachmentSupport, times(2)).collectAttachmentIDs(collectAttachmentIDsCaptor.capture());
		Assert.assertEquals(collectAttachmentIDsCaptor.getAllValues().size(), 2);
		verify(attachmentHandler).synchronizeAttachments(
			Mockito.eq(uploadAttachmentIds),
			Mockito.eq(existedAttachmentIds),
			ArgumentMatchers.argThat(m ->
				m.equals(updatedDataServicesDocument.getMetadata().getDocRef())
			)
		);
		verify(modelPermissionEvaluator).checkModelReadPermission(headerEntity);
		verify(eventPublisher).publishEvent(documentBeforeUpdateEventCaptor.capture());
		verify(eventPublisher).publishEvent(documentAfterUpdateEventCaptor.capture());
		verify(eventPublisher).publishEvent(documentAfterRepositoryUpdateEventCaptor.capture());

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
