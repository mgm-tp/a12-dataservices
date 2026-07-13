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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeIndexEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentAfterRepositoryCreateEvent;
import com.mgmtp.a12.dataservices.document.exception.DocumentValidationException;
import com.mgmtp.a12.dataservices.document.internal.DefaultDataServicesDocument;
import com.mgmtp.a12.dataservices.document.persistence.DocumentComputationStrategy;
import com.mgmtp.a12.dataservices.document.persistence.DocumentValidationStrategy;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;
import com.mgmtp.a12.model.header.Header;

import static com.mgmtp.a12.dataservices.common.exception.InvalidInputException.INVALID_INPUT_EXCEPTION_CODE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class DocumentServiceCreatingWithStrategyTest extends AbstractDefaultDocumentServiceTest {

	@Test public void testDocumentCreate_defaultConfig_success() {
		DocumentReference documentReference = DocumentReference.builder()
			.documentModelName(testModelName)
			.documentId(UUID.randomUUID().toString())
			.build();
		DocumentV2 kernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentUtils.generateDocRef(kernelDocument)).thenReturn(documentReference);
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		when(documentModelUtils.isAbstract(headerEntity)).thenReturn(false);
		when(kernelDocumentService.computeDocument(any(DocumentV2.class), Mockito.eq(null))).then(AdditionalAnswers.returnsFirstArg());

		DataServicesDocument result = defaultDocumentService.create(kernelDocument, null, DocumentValidationStrategy.DEFAULT_CONFIGURATION,
			DocumentComputationStrategy.DEFAULT_CONFIGURATION);

		verify(documentPermissionEvaluator).checkDocumentCreatePermission(kernelDocument);
		verify(documentRepository).supports(kernelDocument);
		verify(documentUtils).generateDocRef(argThat(documentV2 -> {
			Assert.assertTrue(documentV2.getId().isEmpty());
			return documentV2.getDocumentModelId().equals(testModelName);
		}));
		verify(modelHeaderRepository).findById(testModelName);
		verify(modelPermissionEvaluator).checkModelReadPermission(headerEntity);
		verify(documentModelUtils).isAbstract(headerEntity);
		verify(kernelDocumentService).computeDocument(any(DocumentV2.class), Mockito.eq(null));
		verify(kernelDocumentService, never()).compute(any(DocumentV2.class), Mockito.eq(null));
		verify(kernelDocumentService).validateDocument(any(DocumentV2.class), Mockito.eq(null));
		verify(kernelDocumentService, never()).validateFull(any(DocumentV2.class), Mockito.eq(null));
		verify(kernelDocumentService, never()).validatePartially(any(DocumentV2.class), Mockito.eq(null));
		verify(documentRepository).create(argThat(dataServicesDocument -> {
			assertCreatedDocument(dataServicesDocument.getKernelDocument());
			return true;
		}));

		ArgumentCaptor<DocumentBeforeCreateEvent> documentBeforeCreateEventArgumentCaptor = ArgumentCaptor.forClass(DocumentBeforeCreateEvent.class);
		ArgumentCaptor<DocumentAfterCreateEvent> documentAfterCreateEventArgumentCaptor = ArgumentCaptor.forClass(DocumentAfterCreateEvent.class);
		ArgumentCaptor<DocumentAfterRepositoryCreateEvent> documentAfterRepositoryCreateEventArgumentCaptor =
			ArgumentCaptor.forClass(DocumentAfterRepositoryCreateEvent.class);
		ArgumentCaptor<DocumentBeforeIndexEvent> documentBeforeIndexEventArgumentCaptor = ArgumentCaptor.forClass(DocumentBeforeIndexEvent.class);

		verify(eventPublisher).publishEvent(documentBeforeCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentAfterCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentAfterRepositoryCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentBeforeIndexEventArgumentCaptor.capture());

		Assert.assertEquals(documentBeforeCreateEventArgumentCaptor.getValue().getCreatedDocument().getDocumentModelId(), kernelDocument.getDocumentModelId());
		compareDocumentV2(
			documentAfterRepositoryCreateEventArgumentCaptor.getValue().getNewDocument().getKernelDocument(),
			result.getKernelDocument()
		);
		compareDocumentV2(
			documentAfterCreateEventArgumentCaptor.getValue().getDataServicesDocument().getKernelDocument(),
			result.getKernelDocument()
		);
	}

	@Test public void testDocumentCreate_nullValidationStrategy() {
		DocumentV2 kernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);

		Assert.assertThrows(NullPointerException.class, () ->
			defaultDocumentService.create(kernelDocument, null, null, DocumentComputationStrategy.DEFAULT_CONFIGURATION));
	}

	@Test public void testDocumentCreate_nullComputationStrategy() {
		DocumentV2 kernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);

		Assert.assertThrows(NullPointerException.class, () ->
			defaultDocumentService.create(kernelDocument, null, DocumentValidationStrategy.DEFAULT_CONFIGURATION, null));
	}

	@Test public void testDocumentCreate_fullComputation_success() {
		DocumentReference documentReference = DocumentReference.builder()
			.documentModelName(testModelName)
			.documentId(UUID.randomUUID().toString())
			.build();
		DocumentV2 kernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentUtils.generateDocRef(kernelDocument)).thenReturn(documentReference);
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		when(documentModelUtils.isAbstract(headerEntity)).thenReturn(false);
		when(kernelDocumentService.compute(any(DocumentV2.class), Mockito.eq(null))).then(AdditionalAnswers.returnsFirstArg());

		DataServicesDocument result = defaultDocumentService.create(kernelDocument, null, DocumentValidationStrategy.DEFAULT_CONFIGURATION,
			DocumentComputationStrategy.FULL_COMPUTATION);

		verify(documentPermissionEvaluator).checkDocumentCreatePermission(kernelDocument);
		verify(documentRepository).supports(kernelDocument);
		verify(documentUtils).generateDocRef(argThat(documentV2 -> {
			Assert.assertTrue(documentV2.getId().isEmpty());
			return documentV2.getDocumentModelId().equals(testModelName);
		}));
		verify(modelHeaderRepository).findById(testModelName);
		verify(modelPermissionEvaluator).checkModelReadPermission(headerEntity);
		verify(documentModelUtils).isAbstract(headerEntity);
		verify(kernelDocumentService).compute(any(DocumentV2.class), Mockito.eq(null));
		verify(kernelDocumentService, never()).computeDocument(any(DocumentV2.class), Mockito.eq(null));
		verify(documentRepository).create(argThat(dataServicesDocument -> {
			assertCreatedDocument(dataServicesDocument.getKernelDocument());
			return true;
		}));

		ArgumentCaptor<DocumentBeforeCreateEvent> documentBeforeCreateEventArgumentCaptor = ArgumentCaptor.forClass(DocumentBeforeCreateEvent.class);
		ArgumentCaptor<DocumentAfterCreateEvent> documentAfterCreateEventArgumentCaptor = ArgumentCaptor.forClass(DocumentAfterCreateEvent.class);
		ArgumentCaptor<DocumentAfterRepositoryCreateEvent> documentAfterRepositoryCreateEventArgumentCaptor =
			ArgumentCaptor.forClass(DocumentAfterRepositoryCreateEvent.class);
		ArgumentCaptor<DocumentBeforeIndexEvent> documentBeforeIndexEventArgumentCaptor = ArgumentCaptor.forClass(DocumentBeforeIndexEvent.class);

		verify(eventPublisher).publishEvent(documentBeforeCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentAfterCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentAfterRepositoryCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentBeforeIndexEventArgumentCaptor.capture());

		Assert.assertEquals(documentBeforeCreateEventArgumentCaptor.getValue().getCreatedDocument().getDocumentModelId(), kernelDocument.getDocumentModelId());
		compareDocumentV2(
			documentAfterRepositoryCreateEventArgumentCaptor.getValue().getNewDocument().getKernelDocument(),
			result.getKernelDocument()
		);
		compareDocumentV2(
			documentAfterCreateEventArgumentCaptor.getValue().getDataServicesDocument().getKernelDocument(),
			result.getKernelDocument()
		);
	}

	@Test public void testDocumentCreate_noComputation_success() {
		DocumentReference documentReference = DocumentReference.builder()
			.documentModelName(testModelName)
			.documentId(UUID.randomUUID().toString())
			.build();
		DocumentV2 kernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentUtils.generateDocRef(kernelDocument)).thenReturn(documentReference);
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		when(documentModelUtils.isAbstract(headerEntity)).thenReturn(false);

		DataServicesDocument result = defaultDocumentService.create(kernelDocument, null, DocumentValidationStrategy.DEFAULT_CONFIGURATION,
			DocumentComputationStrategy.NO_COMPUTATION);

		verify(documentPermissionEvaluator).checkDocumentCreatePermission(kernelDocument);
		verify(documentRepository).supports(kernelDocument);
		verify(documentUtils).generateDocRef(argThat(documentV2 -> {
			Assert.assertTrue(documentV2.getId().isEmpty());
			return documentV2.getDocumentModelId().equals(testModelName);
		}));
		verify(modelHeaderRepository).findById(testModelName);
		verify(modelPermissionEvaluator).checkModelReadPermission(headerEntity);
		verify(documentModelUtils).isAbstract(headerEntity);
		verify(kernelDocumentService, never()).computeDocument(any(DocumentV2.class), Mockito.eq(null));
		verify(kernelDocumentService, never()).compute(any(DocumentV2.class), Mockito.eq(null));
		verify(documentRepository).create(argThat(dataServicesDocument -> {
			assertCreatedDocument(dataServicesDocument.getKernelDocument());
			return true;
		}));

		ArgumentCaptor<DocumentBeforeCreateEvent> documentBeforeCreateEventArgumentCaptor = ArgumentCaptor.forClass(DocumentBeforeCreateEvent.class);
		ArgumentCaptor<DocumentAfterCreateEvent> documentAfterCreateEventArgumentCaptor = ArgumentCaptor.forClass(DocumentAfterCreateEvent.class);
		ArgumentCaptor<DocumentAfterRepositoryCreateEvent> documentAfterRepositoryCreateEventArgumentCaptor =
			ArgumentCaptor.forClass(DocumentAfterRepositoryCreateEvent.class);
		ArgumentCaptor<DocumentBeforeIndexEvent> documentBeforeIndexEventArgumentCaptor = ArgumentCaptor.forClass(DocumentBeforeIndexEvent.class);

		verify(eventPublisher).publishEvent(documentBeforeCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentAfterCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentAfterRepositoryCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentBeforeIndexEventArgumentCaptor.capture());

		Assert.assertEquals(documentBeforeCreateEventArgumentCaptor.getValue().getCreatedDocument().getDocumentModelId(), kernelDocument.getDocumentModelId());
		compareDocumentV2(
			documentAfterRepositoryCreateEventArgumentCaptor.getValue().getNewDocument().getKernelDocument(),
			result.getKernelDocument()
		);
		compareDocumentV2(
			documentAfterCreateEventArgumentCaptor.getValue().getDataServicesDocument().getKernelDocument(),
			result.getKernelDocument()
		);
	}

	@Test public void testDocumentCreate_fullValidation_success() {
		DocumentReference documentReference = DocumentReference.builder()
			.documentModelName(testModelName)
			.documentId(UUID.randomUUID().toString())
			.build();
		DocumentV2 kernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentUtils.generateDocRef(kernelDocument)).thenReturn(documentReference);
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		when(documentModelUtils.isAbstract(headerEntity)).thenReturn(false);
		when(kernelDocumentService.computeDocument(any(DocumentV2.class), Mockito.eq(null))).then(AdditionalAnswers.returnsFirstArg());

		DataServicesDocument result = defaultDocumentService.create(kernelDocument, null, DocumentValidationStrategy.FULL_VALIDATION,
			DocumentComputationStrategy.DEFAULT_CONFIGURATION);

		verify(documentPermissionEvaluator).checkDocumentCreatePermission(kernelDocument);
		verify(documentRepository).supports(kernelDocument);
		verify(documentUtils).generateDocRef(argThat(documentV2 -> {
			Assert.assertTrue(documentV2.getId().isEmpty());
			return documentV2.getDocumentModelId().equals(testModelName);
		}));
		verify(modelHeaderRepository).findById(testModelName);
		verify(modelPermissionEvaluator).checkModelReadPermission(headerEntity);
		verify(documentModelUtils).isAbstract(headerEntity);
		verify(kernelDocumentService).validateFull(any(DocumentV2.class), Mockito.eq(null));
		verify(kernelDocumentService, never()).validateDocument(any(DocumentV2.class), Mockito.eq(null));
		verify(kernelDocumentService, never()).validatePartially(any(DocumentV2.class), Mockito.eq(null));
		verify(documentRepository).create(argThat(dataServicesDocument -> {
			assertCreatedDocument(dataServicesDocument.getKernelDocument());
			return true;
		}));

		ArgumentCaptor<DocumentBeforeCreateEvent> documentBeforeCreateEventArgumentCaptor = ArgumentCaptor.forClass(DocumentBeforeCreateEvent.class);
		ArgumentCaptor<DocumentAfterCreateEvent> documentAfterCreateEventArgumentCaptor = ArgumentCaptor.forClass(DocumentAfterCreateEvent.class);
		ArgumentCaptor<DocumentAfterRepositoryCreateEvent> documentAfterRepositoryCreateEventArgumentCaptor =
			ArgumentCaptor.forClass(DocumentAfterRepositoryCreateEvent.class);
		ArgumentCaptor<DocumentBeforeIndexEvent> documentBeforeIndexEventArgumentCaptor = ArgumentCaptor.forClass(DocumentBeforeIndexEvent.class);

		verify(eventPublisher).publishEvent(documentBeforeCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentAfterCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentAfterRepositoryCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentBeforeIndexEventArgumentCaptor.capture());

		Assert.assertEquals(documentBeforeCreateEventArgumentCaptor.getValue().getCreatedDocument().getDocumentModelId(), kernelDocument.getDocumentModelId());
		compareDocumentV2(
			documentAfterRepositoryCreateEventArgumentCaptor.getValue().getNewDocument().getKernelDocument(),
			result.getKernelDocument()
		);
		compareDocumentV2(
			documentAfterCreateEventArgumentCaptor.getValue().getDataServicesDocument().getKernelDocument(),
			result.getKernelDocument()
		);
	}

	@Test public void testDocumentCreate_partialValidation_success() {
		DocumentReference documentReference = DocumentReference.builder()
			.documentModelName(testModelName)
			.documentId(UUID.randomUUID().toString())
			.build();
		DocumentV2 kernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentUtils.generateDocRef(kernelDocument)).thenReturn(documentReference);
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		when(documentModelUtils.isAbstract(headerEntity)).thenReturn(false);
		when(kernelDocumentService.computeDocument(any(DocumentV2.class), Mockito.eq(null))).then(AdditionalAnswers.returnsFirstArg());

		DataServicesDocument result = defaultDocumentService.create(kernelDocument, null, DocumentValidationStrategy.PARTIAL_VALIDATION,
			DocumentComputationStrategy.DEFAULT_CONFIGURATION);

		verify(documentPermissionEvaluator).checkDocumentCreatePermission(kernelDocument);
		verify(documentRepository).supports(kernelDocument);
		verify(documentUtils).generateDocRef(argThat(documentV2 -> {
			Assert.assertTrue(documentV2.getId().isEmpty());
			return documentV2.getDocumentModelId().equals(testModelName);
		}));
		verify(modelHeaderRepository).findById(testModelName);
		verify(modelPermissionEvaluator).checkModelReadPermission(headerEntity);
		verify(documentModelUtils).isAbstract(headerEntity);
		verify(kernelDocumentService).validatePartially(any(DocumentV2.class), Mockito.eq(null));
		verify(kernelDocumentService, never()).validateDocument(any(DocumentV2.class), Mockito.eq(null));
		verify(kernelDocumentService, never()).validateFull(any(DocumentV2.class), Mockito.eq(null));
		verify(documentRepository).create(argThat(dataServicesDocument -> {
			assertCreatedDocument(dataServicesDocument.getKernelDocument());
			return true;
		}));

		ArgumentCaptor<DocumentBeforeCreateEvent> documentBeforeCreateEventArgumentCaptor = ArgumentCaptor.forClass(DocumentBeforeCreateEvent.class);
		ArgumentCaptor<DocumentAfterCreateEvent> documentAfterCreateEventArgumentCaptor = ArgumentCaptor.forClass(DocumentAfterCreateEvent.class);
		ArgumentCaptor<DocumentAfterRepositoryCreateEvent> documentAfterRepositoryCreateEventArgumentCaptor =
			ArgumentCaptor.forClass(DocumentAfterRepositoryCreateEvent.class);
		ArgumentCaptor<DocumentBeforeIndexEvent> documentBeforeIndexEventArgumentCaptor = ArgumentCaptor.forClass(DocumentBeforeIndexEvent.class);

		verify(eventPublisher).publishEvent(documentBeforeCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentAfterCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentAfterRepositoryCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentBeforeIndexEventArgumentCaptor.capture());

		Assert.assertEquals(documentBeforeCreateEventArgumentCaptor.getValue().getCreatedDocument().getDocumentModelId(), kernelDocument.getDocumentModelId());
		compareDocumentV2(
			documentAfterRepositoryCreateEventArgumentCaptor.getValue().getNewDocument().getKernelDocument(),
			result.getKernelDocument()
		);
		compareDocumentV2(
			documentAfterCreateEventArgumentCaptor.getValue().getDataServicesDocument().getKernelDocument(),
			result.getKernelDocument()
		);
	}

	@Test public void testDocumentCreate_noValidation_success() {
		DocumentReference documentReference = DocumentReference.builder()
			.documentModelName(testModelName)
			.documentId(UUID.randomUUID().toString())
			.build();
		DocumentV2 kernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentUtils.generateDocRef(kernelDocument)).thenReturn(documentReference);
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		when(documentModelUtils.isAbstract(headerEntity)).thenReturn(false);
		when(kernelDocumentService.computeDocument(any(DocumentV2.class), Mockito.eq(null))).then(AdditionalAnswers.returnsFirstArg());

		DataServicesDocument result = defaultDocumentService.create(kernelDocument, null, DocumentValidationStrategy.NO_VALIDATION,
			DocumentComputationStrategy.DEFAULT_CONFIGURATION);

		verify(documentPermissionEvaluator).checkDocumentCreatePermission(kernelDocument);
		verify(documentRepository).supports(kernelDocument);
		verify(documentUtils).generateDocRef(argThat(documentV2 -> {
			Assert.assertTrue(documentV2.getId().isEmpty());
			return documentV2.getDocumentModelId().equals(testModelName);
		}));
		verify(modelHeaderRepository).findById(testModelName);
		verify(modelPermissionEvaluator).checkModelReadPermission(headerEntity);
		verify(documentModelUtils).isAbstract(headerEntity);
		verify(kernelDocumentService, never()).validateDocument(any(DocumentV2.class), Mockito.eq(null));
		verify(kernelDocumentService, never()).validateFull(any(DocumentV2.class), Mockito.eq(null));
		verify(kernelDocumentService, never()).validatePartially(any(DocumentV2.class), Mockito.eq(null));
		verify(documentRepository).create(argThat(dataServicesDocument -> {
			assertCreatedDocument(dataServicesDocument.getKernelDocument());
			return true;
		}));

		ArgumentCaptor<DocumentBeforeCreateEvent> documentBeforeCreateEventArgumentCaptor = ArgumentCaptor.forClass(DocumentBeforeCreateEvent.class);
		ArgumentCaptor<DocumentAfterCreateEvent> documentAfterCreateEventArgumentCaptor = ArgumentCaptor.forClass(DocumentAfterCreateEvent.class);
		ArgumentCaptor<DocumentAfterRepositoryCreateEvent> documentAfterRepositoryCreateEventArgumentCaptor =
			ArgumentCaptor.forClass(DocumentAfterRepositoryCreateEvent.class);
		ArgumentCaptor<DocumentBeforeIndexEvent> documentBeforeIndexEventArgumentCaptor = ArgumentCaptor.forClass(DocumentBeforeIndexEvent.class);

		verify(eventPublisher).publishEvent(documentBeforeCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentAfterCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentAfterRepositoryCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentBeforeIndexEventArgumentCaptor.capture());

		Assert.assertEquals(documentBeforeCreateEventArgumentCaptor.getValue().getCreatedDocument().getDocumentModelId(), kernelDocument.getDocumentModelId());
		compareDocumentV2(
			documentAfterRepositoryCreateEventArgumentCaptor.getValue().getNewDocument().getKernelDocument(),
			result.getKernelDocument()
		);
		compareDocumentV2(
			documentAfterCreateEventArgumentCaptor.getValue().getDataServicesDocument().getKernelDocument(),
			result.getKernelDocument()
		);
	}

	@Test public void testDocumentCreateWithExistedDocument_success() {
		String documentId = UUID.randomUUID().toString();
		DocumentReference documentReference = DocumentReference.builder()
			.documentModelName(testModelName)
			.documentId(documentId)
			.build();
		DocumentV2 kernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);
		kernelDocument = kernelDocument.withId(documentId);

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentUtils.generateDocRef(kernelDocument)).thenReturn(documentReference);
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		when(documentModelUtils.isAbstract(headerEntity)).thenReturn(false);
		when(kernelDocumentService.computeDocument(any(DocumentV2.class), Mockito.eq(null))).then(AdditionalAnswers.returnsFirstArg());

		DataServicesDocument result = defaultDocumentService.create(kernelDocument, null, DocumentValidationStrategy.DEFAULT_CONFIGURATION,
			DocumentComputationStrategy.DEFAULT_CONFIGURATION);

		verify(documentPermissionEvaluator).checkDocumentCreatePermission(kernelDocument);
		verify(documentRepository).supports(kernelDocument);
		verify(documentUtils).generateDocRef(argThat(documentV2 -> {
			Assert.assertTrue(documentV2.getId().isPresent());
			return documentV2.getDocumentModelId().equals(testModelName);
		}));
		verify(modelHeaderRepository).findById(testModelName);
		verify(modelPermissionEvaluator).checkModelReadPermission(headerEntity);
		verify(documentModelUtils).isAbstract(headerEntity);
		verify(documentRepository).create(argThat(dataServicesDocument -> {
			assertCreatedDocument(dataServicesDocument.getKernelDocument());
			return true;
		}));

		ArgumentCaptor<DocumentBeforeCreateEvent> documentBeforeCreateEventArgumentCaptor = ArgumentCaptor.forClass(DocumentBeforeCreateEvent.class);
		ArgumentCaptor<DocumentAfterCreateEvent> documentAfterCreateEventArgumentCaptor = ArgumentCaptor.forClass(DocumentAfterCreateEvent.class);
		ArgumentCaptor<DocumentAfterRepositoryCreateEvent> documentAfterRepositoryCreateEventArgumentCaptor =
			ArgumentCaptor.forClass(DocumentAfterRepositoryCreateEvent.class);
		ArgumentCaptor<DocumentBeforeIndexEvent> documentBeforeIndexEventArgumentCaptor = ArgumentCaptor.forClass(DocumentBeforeIndexEvent.class);

		verify(eventPublisher).publishEvent(documentBeforeCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentAfterCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentAfterRepositoryCreateEventArgumentCaptor.capture());
		verify(eventPublisher).publishEvent(documentBeforeIndexEventArgumentCaptor.capture());

		Assert.assertEquals(documentBeforeCreateEventArgumentCaptor.getValue().getCreatedDocument().getDocumentModelId(), kernelDocument.getDocumentModelId());
		compareDocumentV2(
			documentAfterRepositoryCreateEventArgumentCaptor.getValue().getNewDocument().getKernelDocument(),
			result.getKernelDocument()
		);
		compareDocumentV2(
			documentAfterCreateEventArgumentCaptor.getValue().getDataServicesDocument().getKernelDocument(),
			result.getKernelDocument()
		);
		Assert.assertEquals(result.getKernelDocument().getId().get(), documentId);
	}

	@Test public void testDocumentCreate_noDocumentCreatePermission() {
		DocumentV2 kernelDocument = DocumentV2.empty(testModelName);

		doThrow(AccessDeniedException.class)
			.when(documentPermissionEvaluator)
			.checkDocumentCreatePermission(any());
		Assert.assertThrows(AccessDeniedException.class, () ->
			defaultDocumentService.create(kernelDocument, null,
				DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION));

		verify(documentPermissionEvaluator).checkDocumentCreatePermission(any());
		verifyNoInteractions(documentRepository, eventPublisher, modelPermissionEvaluator);
	}

	@Test public void testDocumentCreate_noModelReadPermission() {
		DocumentReference documentReference = DocumentReference.builder()
			.documentModelName(testModelName)
			.documentId(UUID.randomUUID().toString())
			.build();
		DefaultDataServicesDocument dataServicesDocument = createDataServicesDocument(documentReference, null);
		DocumentV2 kernelDocument = dataServicesDocument.getKernelDocument();

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentUtils.generateDocRef(kernelDocument)).thenReturn(documentReference);
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));

		doThrow(AccessDeniedException.class)
			.when(modelPermissionEvaluator)
			.checkModelReadPermission(any(Header.class));
		Assert.assertThrows(AccessDeniedException.class, () ->
			defaultDocumentService.create(kernelDocument, null,
				DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION));

		verify(documentPermissionEvaluator).checkDocumentCreatePermission(kernelDocument);
		verify(documentRepository).supports(kernelDocument);
		verify(eventPublisher).publishEvent(any(DocumentBeforeCreateEvent.class));
		verify(modelPermissionEvaluator).checkModelReadPermission(headerEntity);
		verifyNoMoreInteractions(documentPermissionEvaluator, documentRepository, eventPublisher, modelPermissionEvaluator);
	}

	@Test public void testDocumentCreate_supportingRepositoryNotFound() {
		DocumentV2 kernelDocument = DocumentV2.empty(testModelName);

		when(documentRepository.supports(any())).thenReturn(false);
		Assert.assertThrows(NotFoundException.class, () ->
			defaultDocumentService.create(kernelDocument, null,
				DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION));

		verify(documentPermissionEvaluator).checkDocumentCreatePermission(any());
		verify(documentRepository).supports(ArgumentMatchers.argThat(documentV2 -> documentV2.getDocumentModelId().equals(testModelName)));
		verifyNoInteractions(eventPublisher, modelPermissionEvaluator);
	}

	@Test public void testDocumentCreate_modelIsAbstract() {
		DocumentReference documentReference = new DocumentReference(testModelName, "1");
		DefaultDataServicesDocument dataServicesDocument = createDataServicesDocument(documentReference, null);
		DocumentV2 kernelDocument = dataServicesDocument.getKernelDocument();

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentUtils.generateDocRef(kernelDocument)).thenReturn(documentReference);
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		when(documentModelUtils.isAbstract(headerEntity)).thenReturn(true);

		Assert.assertThrows(InvalidInputException.class, () ->
			defaultDocumentService.create(kernelDocument, null,
				DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION));

		verify(documentPermissionEvaluator).checkDocumentCreatePermission(any());
		verify(eventPublisher).publishEvent(any(DocumentBeforeCreateEvent.class));
		verify(modelPermissionEvaluator).checkModelReadPermission(any(Header.class));
		verify(documentRepository, never()).create(any());
		verifyNoMoreInteractions(eventPublisher);
	}

	@Test public void testDocumentCreate_modelNotFound() {
		DocumentReference documentReference = new DocumentReference(testModelName, "1");
		DefaultDataServicesDocument dataServicesDocument = createDataServicesDocument(documentReference, null);
		DocumentV2 kernelDocument = dataServicesDocument.getKernelDocument();

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentUtils.generateDocRef(kernelDocument)).thenReturn(documentReference);
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.empty());

		Assert.assertThrows(NotFoundException.class, () ->
			defaultDocumentService.create(kernelDocument, null,
				DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION));

		verify(documentPermissionEvaluator).checkDocumentCreatePermission(any());
		verify(eventPublisher).publishEvent(any(DocumentBeforeCreateEvent.class));
		verify(modelPermissionEvaluator, never()).checkModelReadPermission(any(Header.class));
		verify(documentRepository, never()).create(any());
		verify(eventPublisher, never()).publishEvent(any(DocumentAfterCreateEvent.class));
		verify(eventPublisher, never()).publishEvent(any(DocumentAfterRepositoryCreateEvent.class));
	}

	@Test public void testDocumentCreate_testDocumentValidationException() {
		DocumentReference documentReference = DocumentReference.builder()
			.documentModelName(testModelName)
			.documentId(UUID.randomUUID().toString())
			.build();
		DefaultDataServicesDocument dataServicesDocument = createDataServicesDocument(documentReference, null);
		DocumentV2 kernelDocument = dataServicesDocument.getKernelDocument();

		IDocumentValidationResult mockedDocumentValidationResult = Mockito.mock(IDocumentValidationResult.class);
		IMessage mockedMessage = Mockito.mock(IMessage.class);

		when(documentRepository.supports(any())).thenReturn(true);
		when(documentUtils.generateDocRef(kernelDocument)).thenReturn(documentReference);
		when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		when(mockedDocumentValidationResult.noErrorOccurred()).thenReturn(false);
		when(mockedDocumentValidationResult.getMessages()).thenReturn(List.of(mockedMessage));
		when(kernelDocumentService.validateDocument(Mockito.any(), Mockito.any())).thenReturn(Optional.of(mockedDocumentValidationResult));

		Assert.assertThrows(DocumentValidationException.class, () ->
			defaultDocumentService.create(kernelDocument, null,
				DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION));

		try {
			defaultDocumentService.create(kernelDocument, null);
		} catch (DocumentValidationException e) {
			Assert.assertNotNull(e.getErrorDetail());
			Assert.assertEquals(e.getCode(), INVALID_INPUT_EXCEPTION_CODE);
			Assert.assertEquals(e.getMessage(), "Document is not valid:%n[%s]".formatted(mockedMessage));
		}
	}

	private void assertCreatedDocument(DocumentV2 documentV2) {
		Assert.assertEquals(documentV2.getDocumentModelId(), DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);
		Assert.assertEquals(documentV2.fieldValue(DocumentMetadataConstants.CREATOR_PATH), userName);
		Assert.assertNotNull(documentV2.fieldValue(DocumentMetadataConstants.CREATED_AT_PATH));
		Assert.assertEquals(documentV2.fieldValue(DOCUMENT_FIELD_NAME), "Malcolm");
	}
}
