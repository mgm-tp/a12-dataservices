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
import java.util.Locale;
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
import com.mgmtp.a12.dataservices.exception.UniqueConstraintViolationException;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeIndexEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentAfterRepositoryCreateEvent;
import com.mgmtp.a12.dataservices.document.exception.DocumentValidationException;
import com.mgmtp.a12.dataservices.document.internal.DefaultDataServicesDocument;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;
import com.mgmtp.a12.model.header.Header;

import static com.mgmtp.a12.dataservices.common.exception.InvalidInputException.INVALID_INPUT_EXCEPTION_CODE;
import static org.mockito.ArgumentMatchers.any;

public class DefaultDocumentServiceCreatingTest extends AbstractDefaultDocumentServiceTest {

	@Test public void testDocumentCreate_success() {

		DocumentReference documentReference = DocumentReference.builder()
			.documentModelName(testModelName)
			.documentId(UUID.randomUUID().toString())
			.build();

		DocumentV2 kernelDocument = loadDocumentV2(
			testModelName, DOCUMENT_FILENAME);

		Mockito.when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		Mockito.when(documentRepository.supports(any())).thenReturn(true);
		Mockito.when(documentUtils.generateDocRef(kernelDocument)).thenReturn(documentReference);
		Mockito.when(documentModelUtils.isAbstract(headerEntity)).thenReturn(false);
		Mockito.when(kernelDocumentService.computeDocument(Mockito.any(DocumentV2.class), Mockito.eq(null))).then(AdditionalAnswers.returnsFirstArg());

		DataServicesDocument result = defaultDocumentService.create(kernelDocument, null);

		Mockito.verify(documentRepository, Mockito.times(1)).create(ArgumentMatchers.argThat(a -> {
			assertCreatedDocument(a.getKernelDocument());
			return true;
		}));
		Mockito.verify(documentRepository, Mockito.times(1)).supports(kernelDocument);
		Mockito.verify(modelHeaderRepository, Mockito.times(1)).findById(testModelName);
		Mockito.verify(documentModelUtils, Mockito.times(1)).isAbstract(headerEntity);
		Mockito.verify(documentUtils, Mockito.times(1)).generateDocRef(Mockito.argThat(
			document -> {
				Assert.assertTrue(document.getId().isEmpty());
				return document.getDocumentModelId().equals(testModelName);
			}
		));		Mockito.verify(documentPermissionEvaluator, Mockito.times(1)).checkDocumentCreatePermission(kernelDocument);
		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(headerEntity);

		ArgumentCaptor<DocumentBeforeCreateEvent> documentBeforeCreateEventCaptor = ArgumentCaptor.forClass(DocumentBeforeCreateEvent.class);
		ArgumentCaptor<DocumentAfterCreateEvent> documentAfterCreateEventCaptor = ArgumentCaptor.forClass(DocumentAfterCreateEvent.class);
		ArgumentCaptor<DocumentAfterRepositoryCreateEvent> documentAfterRepositoryCreateEventCaptor =
			ArgumentCaptor.forClass(DocumentAfterRepositoryCreateEvent.class);
		ArgumentCaptor<DocumentBeforeIndexEvent> documentBeforeIndexEventCaptor = ArgumentCaptor.forClass(DocumentBeforeIndexEvent.class);
			ArgumentCaptor.forClass(DocumentBeforeIndexEvent.class);

		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(documentBeforeCreateEventCaptor.capture());
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(documentAfterCreateEventCaptor.capture());
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(documentAfterRepositoryCreateEventCaptor.capture());
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(documentBeforeIndexEventCaptor.capture());

		Assert.assertEquals(documentBeforeCreateEventCaptor.getValue().getCreatedDocument().getDocumentModelId(), kernelDocument.getDocumentModelId());
		compareDocumentV2(
			documentAfterCreateEventCaptor.getValue().getDataServicesDocument().getKernelDocument(),
			result.getKernelDocument()
		);
		compareDocumentV2(
			documentAfterRepositoryCreateEventCaptor.getValue().getNewDocument().getKernelDocument(),
			result.getKernelDocument()
		);

	}

	@Test public void testDocumentCreateWithExistedDocument_success() {
		String documentId = UUID.randomUUID().toString();
		DocumentReference documentReference = DocumentReference.builder()
			.documentModelName(testModelName)
			.documentId(documentId)
			.build();

		DocumentV2 kernelDocument = loadDocumentV2(
			testModelName, DOCUMENT_FILENAME);
		kernelDocument = kernelDocument.withId(documentId);

		Mockito.when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		Mockito.when(documentRepository.supports(any())).thenReturn(true);
		Mockito.when(documentUtils.generateDocRef(kernelDocument)).thenReturn(documentReference);
		Mockito.when(documentModelUtils.isAbstract(headerEntity)).thenReturn(false);
		Mockito.when(kernelDocumentService.computeDocument(Mockito.any(DocumentV2.class), Mockito.eq(null))).then(AdditionalAnswers.returnsFirstArg());

		DataServicesDocument result = defaultDocumentService.create(kernelDocument, null);

		Mockito.verify(documentRepository, Mockito.times(1)).create(ArgumentMatchers.argThat(a -> {
			assertCreatedDocument(a.getKernelDocument());
			return true;
		}));
		Mockito.verify(documentRepository, Mockito.times(1)).supports(kernelDocument);
		Mockito.verify(modelHeaderRepository, Mockito.times(1)).findById(testModelName);
		Mockito.verify(documentModelUtils, Mockito.times(1)).isAbstract(headerEntity);
		Mockito.verify(documentUtils, Mockito.times(1)).generateDocRef(Mockito.argThat(
			document -> {
				Assert.assertTrue(document.getId().isPresent());
				return document.getDocumentModelId().equals(testModelName);
			}
		));
		Mockito.verify(documentPermissionEvaluator, Mockito.times(1)).checkDocumentCreatePermission(kernelDocument);
		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(headerEntity);

		ArgumentCaptor<DocumentBeforeCreateEvent> documentBeforeCreateEventCaptor = ArgumentCaptor.forClass(DocumentBeforeCreateEvent.class);
		ArgumentCaptor<DocumentAfterCreateEvent> documentAfterCreateEventCaptor = ArgumentCaptor.forClass(DocumentAfterCreateEvent.class);
		ArgumentCaptor<DocumentAfterRepositoryCreateEvent> documentAfterRepositoryCreateEventCaptor =
			ArgumentCaptor.forClass(DocumentAfterRepositoryCreateEvent.class);

		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(documentBeforeCreateEventCaptor.capture());
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(documentAfterCreateEventCaptor.capture());
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(documentAfterRepositoryCreateEventCaptor.capture());

		Assert.assertEquals(documentBeforeCreateEventCaptor.getValue().getCreatedDocument().getDocumentModelId(), kernelDocument.getDocumentModelId());
		compareDocumentV2(
			documentAfterCreateEventCaptor.getValue().getDataServicesDocument().getKernelDocument(),
			result.getKernelDocument()
		);
		compareDocumentV2(
			documentAfterRepositoryCreateEventCaptor.getValue().getNewDocument().getKernelDocument(),
			result.getKernelDocument()
		);

		Assert.assertEquals(result.getKernelDocument().getId().get(), documentId);

	}

	@Test public void testDocumentCreate_noModelReadPermission() {
		DocumentReference documentReference = DocumentReference.builder()
			.documentModelName(testModelName)
			.documentId(UUID.randomUUID().toString())
			.build();

		Mockito.when(documentRepository.supports(any())).thenReturn(true);
		Mockito.when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));

		Mockito.doThrow(AccessDeniedException.class)
			.when(modelPermissionEvaluator)
			.checkModelReadPermission(any(Header.class));

		DefaultDataServicesDocument dataServicesDocument = createDataServicesDocument(documentReference, null);
		DocumentV2 kernelDocument = dataServicesDocument.getKernelDocument();
		Mockito.when(documentUtils.generateDocRef(kernelDocument)).thenReturn(documentReference);

		Assert.assertThrows(AccessDeniedException.class, () -> defaultDocumentService.create(kernelDocument, null));

		Mockito.verify(documentRepository, Mockito.times(1)).supports(kernelDocument);
		Mockito.verifyNoMoreInteractions(documentRepository);
		Mockito.verify(documentPermissionEvaluator, Mockito.times(1)).checkDocumentCreatePermission(kernelDocument);
		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(headerEntity);
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(any(DocumentBeforeCreateEvent.class));
		Mockito.verifyNoMoreInteractions(eventPublisher);
	}

	@Test
	public void testDocumentCreate_noDocumentCreatePermission() {

		Mockito.doThrow(AccessDeniedException.class)
			.when(documentPermissionEvaluator)
			.checkDocumentCreatePermission(any());

		DocumentV2 kernelDocument = DocumentV2.empty(testModelName);
		Assert.assertThrows(AccessDeniedException.class, () -> defaultDocumentService.create(kernelDocument, null));

		Mockito.verify(documentPermissionEvaluator, Mockito.times(1)).checkDocumentCreatePermission(any());
		Mockito.verifyNoInteractions(documentRepository, eventPublisher, modelPermissionEvaluator);
	}

	@Test
	public void testDocumentCreate_supportingRepositoryNotFound() {
		Mockito.when(documentRepository.supports(any())).thenReturn(false);
		Assert.assertThrows(NotFoundException.class, () -> defaultDocumentService.create(DocumentV2.empty(testModelName), null));
		Mockito.verify(documentPermissionEvaluator, Mockito.times(1)).checkDocumentCreatePermission(any());

		Mockito.verify(documentRepository, Mockito.times(1)).supports(ArgumentMatchers.argThat(m -> m.getDocumentModelId().equals(testModelName)));
		Mockito.verifyNoInteractions(eventPublisher, modelPermissionEvaluator);
	}

	@Test
	public void testDocumentCreate_modelIsAbstract() {
		Mockito.when(documentRepository.supports(any())).thenReturn(true);
		Mockito.when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		Mockito.when(documentModelUtils.isAbstract(headerEntity)).thenReturn(true);
		DocumentReference documentReference = new DocumentReference(testModelName, "1");
		DefaultDataServicesDocument dataServicesDocument = createDataServicesDocument(documentReference, null);
		DocumentV2 kernelDocument = dataServicesDocument.getKernelDocument();
		Mockito.when(documentUtils.generateDocRef(kernelDocument)).thenReturn(documentReference);

		Assert.assertThrows(InvalidInputException.class, () -> defaultDocumentService.create(kernelDocument, null));

		Mockito.verify(documentRepository, Mockito.times(0)).create(any());
		Mockito.verify(documentPermissionEvaluator, Mockito.times(1)).checkDocumentCreatePermission(any());
		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(any(Header.class));
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(any(DocumentBeforeCreateEvent.class));
		Mockito.verifyNoMoreInteractions(eventPublisher);
	}

	@Test
	public void testDocumentCreate_modelNotFound() {
		DocumentReference documentReference = new DocumentReference(testModelName, "1");

		Mockito.when(documentRepository.supports(any())).thenReturn(true);
		Mockito.when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.empty());

		DefaultDataServicesDocument dataServicesDocument = createDataServicesDocument(documentReference, null);
		DocumentV2 kernelDocument = dataServicesDocument.getKernelDocument();
		Mockito.when(documentUtils.generateDocRef(kernelDocument)).thenReturn(documentReference);

		Assert.assertThrows(NotFoundException.class, () -> defaultDocumentService.create(kernelDocument, null));

		Mockito.verify(documentRepository, Mockito.times(0)).create(any());
		Mockito.verify(documentPermissionEvaluator, Mockito.times(1)).checkDocumentCreatePermission(any());
		Mockito.verify(modelPermissionEvaluator, Mockito.times(0)).checkModelReadPermission(any(Header.class));
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(any(DocumentBeforeCreateEvent.class));
		Mockito.verify(eventPublisher, Mockito.times(0)).publishEvent(any(DocumentAfterCreateEvent.class));
		Mockito.verify(eventPublisher, Mockito.times(0)).publishEvent(any(DocumentAfterRepositoryCreateEvent.class));
	}

	@Test public void testDocumentCreate_testDocumentValidationException() {
		Mockito.when(documentRepository.supports(any())).thenReturn(true);
		Mockito.when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));

		DocumentReference documentReference = DocumentReference.builder()
			.documentModelName(testModelName)
			.documentId(UUID.randomUUID().toString())
			.build();
		IDocumentValidationResult mockedDocumentValidationResult = Mockito.mock(IDocumentValidationResult.class);
		IMessage mockedMessage = Mockito.mock(IMessage.class);

		DefaultDataServicesDocument dataServicesDocument = createDataServicesDocument(documentReference, null);
		DocumentV2 kernelDocument = dataServicesDocument.getKernelDocument();
		Mockito.when(documentUtils.generateDocRef(kernelDocument)).thenReturn(documentReference);
		Mockito.when(mockedDocumentValidationResult.noErrorOccurred()).thenReturn(false);
		Mockito.when(mockedDocumentValidationResult.getMessages()).thenReturn(List.of(mockedMessage));
		Mockito.when(kernelDocumentService.validateDocument(Mockito.any(), Mockito.any())).thenReturn(Optional.of(mockedDocumentValidationResult));
		Assert.assertThrows(DocumentValidationException.class, () -> defaultDocumentService.create(kernelDocument, null));

		try {
			defaultDocumentService.create(kernelDocument, null);
		} catch (DocumentValidationException e) {
			Assert.assertNotNull(e.getErrorDetail());
			Assert.assertEquals(e.getCode(), INVALID_INPUT_EXCEPTION_CODE);
			Assert.assertEquals(e.getMessage(), "Document is not valid:%n[%s]".formatted(mockedMessage));
		}
	}

	@Test(description = "Should invoke uniqueConstraintValidator.insert with the correct docRef after a successful create")
	public void shouldInvokeUniqueConstraintInsertAfterSuccessfulCreate() {
		DocumentReference documentReference = DocumentReference.builder()
			.documentModelName(testModelName)
			.documentId(UUID.randomUUID().toString())
			.build();

		DocumentV2 kernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);

		Mockito.when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		Mockito.when(documentRepository.supports(any())).thenReturn(true);
		Mockito.when(documentUtils.generateDocRef(kernelDocument)).thenReturn(documentReference);
		Mockito.when(documentModelUtils.isAbstract(headerEntity)).thenReturn(false);
		Mockito.when(kernelDocumentService.computeDocument(Mockito.any(DocumentV2.class), Mockito.eq(null)))
			.then(AdditionalAnswers.returnsFirstArg());

		defaultDocumentService.create(kernelDocument, null);

		ArgumentCaptor<DocumentReference> docRefCaptor = ArgumentCaptor.forClass(DocumentReference.class);
		Mockito.verify(uniqueConstraintValidator, Mockito.times(1))
			.insert(Mockito.any(DocumentV2.class), docRefCaptor.capture(), Mockito.nullable(Locale.class));
		Assert.assertEquals(docRefCaptor.getValue(), documentReference);
	}

	@Test(description = "Should propagate UniqueConstraintViolationException when a unique constraint is violated during create")
	public void shouldPropagateUniqueConstraintViolationExceptionOnCreate() {
		DocumentReference documentReference = DocumentReference.builder()
			.documentModelName(testModelName)
			.documentId(UUID.randomUUID().toString())
			.build();

		DocumentV2 kernelDocument = loadDocumentV2(testModelName, DOCUMENT_FILENAME);

		Mockito.when(modelHeaderRepository.findById(testModelName)).thenReturn(Optional.of(headerEntity));
		Mockito.when(documentRepository.supports(any())).thenReturn(true);
		Mockito.when(documentUtils.generateDocRef(kernelDocument)).thenReturn(documentReference);
		Mockito.when(documentModelUtils.isAbstract(headerEntity)).thenReturn(false);
		Mockito.when(kernelDocumentService.computeDocument(Mockito.any(DocumentV2.class), Mockito.eq(null)))
			.then(AdditionalAnswers.returnsFirstArg());
		Mockito.doThrow(new UniqueConstraintViolationException("c1", testModelName))
			.when(uniqueConstraintValidator).insert(Mockito.any(DocumentV2.class), Mockito.any(DocumentReference.class), Mockito.any());

		Assert.assertThrows(UniqueConstraintViolationException.class, () -> defaultDocumentService.create(kernelDocument, null));

		Mockito.verify(uniqueConstraintValidator, Mockito.times(1)).insert(any(), any(), any());
		Mockito.verify(documentRepository, Mockito.never()).create(any());
	}

	void assertCreatedDocument(DocumentV2 documentV2) {
		Assert.assertEquals(documentV2.getDocumentModelId(), DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);
		Assert.assertEquals(documentV2.fieldValue(DocumentMetadataConstants.CREATOR_PATH), userName);
		Assert.assertNotNull(documentV2.fieldValue(DocumentMetadataConstants.CREATED_AT_PATH));
		Assert.assertEquals(documentV2.fieldValue(DOCUMENT_FIELD_NAME), "Malcolm");
	}
}
