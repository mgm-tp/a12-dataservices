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
package com.mgmtp.a12.dataservices.attachment.internal;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.MimeTypeUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractKernelAwareTest;
import com.mgmtp.a12.dataservices.attachment.AttachmentAnnotation;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentReference;
import com.mgmtp.a12.dataservices.attachment.AttachmentReferenceType;
import com.mgmtp.a12.dataservices.attachment.AttachmentUrl;
import com.mgmtp.a12.dataservices.attachment.ThumbnailType;
import com.mgmtp.a12.dataservices.attachment.TypeOfTheContent;
import com.mgmtp.a12.dataservices.attachment.events.AttachmentAfterCreateEvent;
import com.mgmtp.a12.dataservices.attachment.events.AttachmentAfterDeleteEvent;
import com.mgmtp.a12.dataservices.attachment.events.AttachmentBeforeCreateEvent;
import com.mgmtp.a12.dataservices.attachment.events.AttachmentBeforeDeleteEvent;
import com.mgmtp.a12.dataservices.attachment.events.AttachmentThumbnailAfterSaveEvent;
import com.mgmtp.a12.dataservices.attachment.events.AttachmentThumbnailBeforeSaveEvent;
import com.mgmtp.a12.dataservices.attachment.header.AttachmentHeaderService;
import com.mgmtp.a12.dataservices.attachment.persitence.AttachmentPersistenceResult;
import com.mgmtp.a12.dataservices.attachment.persitence.IAttachmentRepository;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.ThumbnailUtil;
import com.mgmtp.a12.dataservices.authorization.AttachmentPermissionEvaluator;
import com.mgmtp.a12.dataservices.common.content.ContentTypeDetector;
import com.mgmtp.a12.dataservices.authorization.AuthConstants;
import com.mgmtp.a12.dataservices.authorization.DocumentPermissionEvaluator;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentRepository;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.QueryService;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.reference.GenericReference;
import com.mgmtp.a12.dataservices.uaa.UaaTestHelper;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.SneakyThrows;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class DefaultAttachmentServiceTest extends AbstractKernelAwareTest {

	private static final String PNG_ATTACHMENT_PATH = "/attachment/image-attachment.png";
	private String contentId;

	private String modelName;
	private String fileName;
	private String pathToField;
	private String errorMessage;
	private String name;
	private List<AttachmentAnnotation> annotations;

	@Mock private IAttachmentRepository attachmentRepository;
	@Mock private AttachmentHeaderService attachmentHeaderService;
	@Spy private DataServicesCoreProperties dataServicesCoreProperties = Mockito.spy(new DataServicesCoreProperties());
	@Mock private ApplicationEventPublisher eventPublisher;
	@Mock private AttachmentPermissionEvaluator attachmentPermissionEvaluator;
	@Mock private DocumentPermissionEvaluator documentPermissionEvaluator;
	@Mock private ModelPermissionEvaluator<IDocumentModel> modelPermissionEvaluator;
	@Mock private DefaultDocumentRepository defaultDocumentRepository;
	@Mock private ThumbnailUrlGenerator thumbnailUrlGenerator;
	@Mock private QueryService queryService;
	@Mock private ContentTypeDetector contentTypeDetector;

	@Spy private RetryRegistry retryRegistry = Mockito.spy(RetryRegistry.of(RetryConfig.custom()
		.maxAttempts(3)
		.waitDuration(Duration.ofMillis(1))
		.failAfterMaxAttempts(true)
		.build()));

	private DefaultAttachmentService defaultAttachmentService;

	private AutoCloseable mocks;

	@BeforeClass public void init() {
		mocks = MockitoAnnotations.openMocks(this);
		DefaultAttachmentService realService =
			new DefaultAttachmentService(attachmentRepository, attachmentHeaderService, dataServicesCoreProperties, eventPublisher,
				List.of(defaultDocumentRepository), attachmentPermissionEvaluator, modelPermissionEvaluator, retryRegistry,
				thumbnailUrlGenerator, queryService, contentTypeDetector);
		defaultAttachmentService = Mockito.spy(realService);
	}

	@BeforeMethod public void clearAndSetup() {
		Mockito.reset(attachmentRepository, attachmentHeaderService, defaultAttachmentService, thumbnailUrlGenerator, attachmentPermissionEvaluator,
			modelPermissionEvaluator, documentPermissionEvaluator, eventPublisher, contentTypeDetector);
		name = RandomStringUtils.randomAlphabetic(10);
		contentId = RandomStringUtils.insecure().nextAlphabetic(10);
		modelName = RandomStringUtils.insecure().nextAlphabetic(10);
		fileName = RandomStringUtils.insecure().nextAlphabetic(10);
		pathToField = RandomStringUtils.insecure().nextAlphabetic(10);
		errorMessage = RandomStringUtils.insecure().nextAlphabetic(15);
		annotations = List.of(AttachmentAnnotation.builder().name(RandomStringUtils.insecure().nextAlphabetic(5)).value(RandomStringUtils.insecure().nextAlphabetic(10)).build());

		dataServicesCoreProperties.getAttachments().getThumbnail().getPreview().setEnabled(true);
		UaaTestHelper.setCurrentUserName(User.builder().username(name).password("").build());
	}

	@SneakyThrows
	@AfterClass public void tearDown() {
		mocks.close();
	}

	/**
	 * This test is about creating correct ATTACHMENT_PUBLIC type attachment when model is defined in configuration property
	 */
	@Test
	public void testCreateAttachment_shouldSuccess_whenPersistPublicAttachment() {
		mockSuccessSaveContents();
		dataServicesCoreProperties.getAttachments().getType().getPublicType().getModels().add(modelName);
		mockAttachmentHeader();

		defaultAttachmentService.createAttachment(
			Objects.requireNonNull(this.getClass().getResourceAsStream(PNG_ATTACHMENT_PATH)),
			fileName,
			modelName,
			pathToField,
			annotations);

		Mockito.verify(attachmentRepository, Mockito.times(1))
			.create(ArgumentMatchers.anyString(), any(InputStream.class), any(),
				ArgumentMatchers.eq(TypeOfTheContent.ATTACHMENT_PUBLIC), ArgumentMatchers.nullable(String.class));
		Mockito.verify(attachmentRepository, Mockito.times(2))
			.create(ArgumentMatchers.anyString(), any(InputStream.class), any(),
				ArgumentMatchers.eq(TypeOfTheContent.ATTACHMENT_THUMBNAIL), ArgumentMatchers.nullable(String.class));
		verifyCreateAttachmentHeaderByType(TypeOfTheContent.ATTACHMENT_PUBLIC);
		dataServicesCoreProperties.getAttachments().getType().getPublicType().getModels().clear();
	}

	@Test
	public void testCreateAttachmentWithAttachmentId_shouldSuccess_whenPersistAttachment() {
		mockSuccessSaveContents();
		dataServicesCoreProperties.getAttachments().getType().getPublicType().getModels().add(modelName);
		String attachmentId = UUID.randomUUID().toString();
		AttachmentHeader attachmentHeader = mockAttachmentHeader(attachmentId);
		try (MockedStatic<AttachmentHelper> attachmentHelperMockedStatic = Mockito.mockStatic(AttachmentHelper.class)) {
			attachmentHelperMockedStatic.when(() -> AttachmentHelper.prepareAttachmentHeader(any(), any(), any())).thenReturn(
				attachmentHeader
			);

			defaultAttachmentService.createSecuredAttachment(
				attachmentId,
				Objects.requireNonNull(this.getClass().getResourceAsStream(PNG_ATTACHMENT_PATH)),
				fileName,
				annotations
			);

			Mockito.verify(attachmentRepository, Mockito.times(1))
				.create(ArgumentMatchers.anyString(), any(InputStream.class), any(),
					ArgumentMatchers.eq(TypeOfTheContent.ATTACHMENT_SECURED), ArgumentMatchers.nullable(String.class));
			Mockito.verify(attachmentRepository, Mockito.times(2))
				.create(ArgumentMatchers.anyString(), any(InputStream.class), any(),
					ArgumentMatchers.eq(TypeOfTheContent.ATTACHMENT_THUMBNAIL), ArgumentMatchers.nullable(String.class));
			verifyCreateAttachmentHeaderByType(TypeOfTheContent.ATTACHMENT_SECURED);
			attachmentHelperMockedStatic.verify(
				() -> AttachmentHelper.prepareAttachmentHeader(eq(attachmentId)
					, eq(fileName), eq(annotations))
			);
			dataServicesCoreProperties.getAttachments().getType().getPublicType().getModels().clear();

		}

	}

	/**
	 * This test is about creating correct ATTACHMENT_SECURED type attachment when model is defined in configuration property
	 */
	@Test
	public void testCreateAttachment_shouldSuccess_whenPersistSecuredAttachment() {
		mockSuccessSaveContents();
		mockAttachmentHeader();

		defaultAttachmentService.createAttachment(
			Objects.requireNonNull(this.getClass().getResourceAsStream("/attachment/image-attachment.png")),
			fileName,
			modelName,
			pathToField,
			annotations);

		Mockito.verify(attachmentRepository, Mockito.times(1))
			.create(ArgumentMatchers.anyString(), any(InputStream.class), any(),
				ArgumentMatchers.eq(TypeOfTheContent.ATTACHMENT_SECURED), ArgumentMatchers.nullable(String.class));
		Mockito.verify(attachmentRepository, Mockito.times(2))
			.create(ArgumentMatchers.anyString(), any(InputStream.class), any(),
				ArgumentMatchers.eq(TypeOfTheContent.ATTACHMENT_THUMBNAIL), ArgumentMatchers.nullable(String.class));
		verifyCreateAttachmentHeaderByType(TypeOfTheContent.ATTACHMENT_SECURED);
	}

	@Test
	public void testCreateAttachment_shouldDeleteAttachmentFromRepository_whenExceptionIsThrownByAttachmentHeaderService() {
		mockSuccessSaveContents();
		setupRollbackSaveAttachment();
		UnexpectedException e = Assert.expectThrows(UnexpectedException.class, () ->
			defaultAttachmentService.createAttachment(
				Objects.requireNonNull(this.getClass().getResourceAsStream("/attachment/image-attachment.png")),
				fileName,
				modelName,
				pathToField,
				annotations));

		Assert.assertNotNull(e);
		Assert.assertEquals(e.getMessage(), errorMessage);
		Mockito.verify(attachmentRepository, Mockito.times(3)).delete(ArgumentMatchers.anyString());
	}

	@Test
	public void testCreateAttachment_shouldDeleteAttachmentFromRepository_whenExceptionIsThrown_ByAttachmentRepository() {
		mockSuccessSaveContents();
		Mockito.when(attachmentRepository.create(
			ArgumentMatchers.anyString(),
			any(InputStream.class),
			ArgumentMatchers.anyString(),
			any(TypeOfTheContent.class),
			ArgumentMatchers.nullable(String.class)
		)).thenThrow(new UnexpectedException(errorMessage));
		UnexpectedException e = Assert.expectThrows(UnexpectedException.class, () ->
			defaultAttachmentService.createAttachment(
				this.getClass().getResourceAsStream("/attachment/image-attachment.png"),
				fileName,
				modelName,
				pathToField,
				annotations));

		Assert.assertNotNull(e);
		Assert.assertEquals(e.getMessage(), errorMessage);
		Mockito.verify(
			attachmentRepository,
			Mockito.times(1).description("AttachmentRepository#delete should be called only 1 time since there is no thumbnail yet.")
		).delete(ArgumentMatchers.anyString());
	}

	@Test
	public void testCreateAttachment_shouldRetryDeleteAttachmentWithMaxAttempts_whenExceptionIsThrown() {
		mockSuccessSaveContents();
		setupRollbackSaveAttachment();
		Mockito.doThrow(new UnexpectedException(errorMessage)).when(attachmentRepository).delete(ArgumentMatchers.anyString());

		UnexpectedException e = Assert.expectThrows(UnexpectedException.class, () ->
			defaultAttachmentService.createAttachment(
				Objects.requireNonNull(this.getClass().getResourceAsStream("/attachment/image-attachment.png")),
				fileName,
				modelName,
				RandomStringUtils.insecure().nextAlphabetic(10),
				Collections.emptyList())
		);

		Assert.assertNotNull(e);
		Assert.assertEquals(e.getMessage(), errorMessage);
		Mockito.verify(attachmentRepository, Mockito.times(9)).delete(ArgumentMatchers.anyString());
	}

	@Test public void testCreateAttachment_saveSuccess_thenReturnAttachmentHeader() {
		long size = 100;
		InputStream inputStream = this.getClass().getResourceAsStream("/attachment/image-attachment.png");
		dataServicesCoreProperties.getAttachments().getThumbnail().getPreview().setEnabled(true);
		AttachmentPersistenceResult result = AttachmentPersistenceResult
			.builder()
			.size(size)
			.mimeType(MimeTypeUtils.IMAGE_PNG_VALUE)
			.attachmentId(contentId)
			.build();
		AttachmentHeader attachmentHeader = mockAttachmentHeader();

		Mockito.when(
				attachmentRepository.create(ArgumentMatchers.anyString(), any(InputStream.class), Mockito.eq(fileName),
					Mockito.eq(TypeOfTheContent.ATTACHMENT_SECURED), ArgumentMatchers.nullable(String.class)))
			.thenReturn(result);

		Mockito.when(attachmentRepository.create(ArgumentMatchers.anyString(), any(InputStream.class), ArgumentMatchers.anyString(),
				Mockito.eq(TypeOfTheContent.ATTACHMENT_THUMBNAIL), ArgumentMatchers.nullable(String.class)))
			.thenReturn(result);

		Mockito.when(attachmentHeaderService.create(any(AttachmentHeader.class)))
			.thenReturn(attachmentHeader);

		ArgumentCaptor<AttachmentBeforeCreateEvent> attachmentBeforeCaptor = ArgumentCaptor.forClass(AttachmentBeforeCreateEvent.class);
		ArgumentCaptor<AttachmentAfterCreateEvent> attachmentAfterCaptor = ArgumentCaptor.forClass(AttachmentAfterCreateEvent.class);
		ArgumentCaptor<AttachmentThumbnailBeforeSaveEvent> thumbnailBeforeSaveCaptor = ArgumentCaptor.forClass(AttachmentThumbnailBeforeSaveEvent.class);
		ArgumentCaptor<AttachmentThumbnailAfterSaveEvent> thumbnailAfterSaveCaptor = ArgumentCaptor.forClass(AttachmentThumbnailAfterSaveEvent.class);

		defaultAttachmentService.createAttachment(Objects.requireNonNull(inputStream), fileName, modelName, pathToField, annotations);

		Mockito.verify(modelPermissionEvaluator).checkModelReadPermission(modelName);
		Mockito.verify(attachmentPermissionEvaluator).checkUploadPermission(Mockito.argThat(header -> {
			Assert.assertEquals(header.getFilename(), fileName);
			Assert.assertEquals(header.getAnnotations(), annotations);
			return true;
		}));

		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(attachmentBeforeCaptor.capture());
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(attachmentAfterCaptor.capture());
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(thumbnailBeforeSaveCaptor.capture());
		Mockito.verify(eventPublisher, Mockito.times(2)).publishEvent(thumbnailAfterSaveCaptor.capture());

		Assert.assertEquals(attachmentBeforeCaptor.getValue().getAttachment().getHeader(), attachmentHeader);
		Assert.assertEquals(attachmentAfterCaptor.getValue().getDataServicesAttachment().getHeader(), attachmentHeader);
		Assert.assertEquals(attachmentAfterCaptor.getValue().getDataServicesAttachment().getHeader().getSize(), size);
		Assert.assertEquals(attachmentAfterCaptor.getValue().getDataServicesAttachment().getHeader().getMimeType(), MimeTypeUtils.IMAGE_PNG_VALUE);

	}

	@Test
	public void testCreateAttachment_shouldUseThumbnailator_whenUsingDefaultConfig() {
		mockSuccessSaveContents();
		AttachmentHeader attachmentHeader = mockAttachmentHeader();
		Mockito.when(attachmentHeaderService.create(any(AttachmentHeader.class))).thenReturn(attachmentHeader);
		try (MockedStatic<ThumbnailUtil> files = Mockito.mockStatic(ThumbnailUtil.class, Mockito.CALLS_REAL_METHODS)) {
			defaultAttachmentService.createAttachment(
				this.getClass().getResourceAsStream("/attachment/image-attachment.png"),
				fileName,
				modelName,
				pathToField,
				annotations);
			files.verify(() -> ThumbnailUtil.convertToDSThumbnailByThumbnailator(any(),
				ArgumentMatchers.eq(ThumbnailType.BIG), ArgumentMatchers.anyInt()), Mockito.times(1));
			files.verify(() -> ThumbnailUtil.convertToDSThumbnailByThumbnailator(any(),
				ArgumentMatchers.eq(ThumbnailType.SMALL), ArgumentMatchers.anyInt()), Mockito.times(1));
		}
	}

	@Test
	public void testCreateAttachment_throwEx_whenHaveNoModelPermission() throws IOException {
		try (InputStream inputStream = this.getClass().getResourceAsStream("/attachment/image-attachment.png")) {
			Mockito.doThrow(new AccessDeniedException(AuthConstants.ACCESS_DENIED)).when(modelPermissionEvaluator).checkModelReadPermission(modelName);

			AccessDeniedException e = Assert.expectThrows(AccessDeniedException.class,
				() -> defaultAttachmentService.createAttachment(inputStream, fileName, modelName,
					pathToField, annotations)
			);

			Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(modelName);
			Mockito.verifyNoInteractions(attachmentPermissionEvaluator, attachmentRepository, attachmentHeaderService);
			Assert.assertEquals(e.getMessage(), AuthConstants.ACCESS_DENIED);
		}
	}

	@Test
	public void testCreateAttachment_throwEx_whenHaveNoUploadPermission() throws IOException {
		try (InputStream inputStream = this.getClass().getResourceAsStream("/attachment/image-attachment.png")) {

			Mockito.doThrow(new AccessDeniedException(AuthConstants.ACCESS_DENIED)).when(attachmentPermissionEvaluator).checkUploadPermission(any());

			AccessDeniedException e = Assert.expectThrows(AccessDeniedException.class,
				() -> defaultAttachmentService.createAttachment(inputStream, fileName, modelName, pathToField, annotations)
			);

			Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(modelName);
			Mockito.verify(attachmentPermissionEvaluator, Mockito.times(1)).checkUploadPermission(
				ArgumentMatchers.argThat(argument -> {
					Assert.assertEquals(argument.getFilename(), fileName);
					Assert.assertEquals(argument.getCreatedBy(), name);
					Assert.assertEquals(argument.getModifiedBy(), name);
					return true;
				})
			);

			Assert.assertEquals(e.getMessage(), AuthConstants.ACCESS_DENIED);
			Mockito.verifyNoInteractions(attachmentRepository, attachmentHeaderService);
		}
	}

	@Test
	public void testDeleteAttachmentFromRepository() {
		AttachmentHeader header = mockAttachmentHeader();

		ArgumentCaptor<String> deleteCaptor = ArgumentCaptor.forClass(String.class);

		defaultAttachmentService.delete(header);

		Mockito.verify(attachmentRepository, Mockito.times(3)).delete(deleteCaptor.capture());

		Assert.assertEquals(deleteCaptor.getAllValues().size(), 3);
		Assert.assertEquals(deleteCaptor.getAllValues().getFirst(), header.getAttachmentId());
		Assert.assertEquals(deleteCaptor.getAllValues().get(1), header.getThumbnailBigId());
		Assert.assertEquals(deleteCaptor.getAllValues().get(2), header.getThumbnailSmallId());

		Mockito.verify(attachmentHeaderService, Mockito.times(1)).delete(header.getAttachmentId());

		ArgumentCaptor<AttachmentBeforeDeleteEvent> attachmentBeforeDeleteCaptor = ArgumentCaptor.forClass(AttachmentBeforeDeleteEvent.class);
		ArgumentCaptor<AttachmentAfterDeleteEvent> attachmentAfterDeleteCaptor = ArgumentCaptor.forClass(AttachmentAfterDeleteEvent.class);

		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(attachmentBeforeDeleteCaptor.capture());
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(attachmentAfterDeleteCaptor.capture());

		Assert.assertEquals(attachmentAfterDeleteCaptor.getValue().getAttachmentHeader(), header);
		Assert.assertEquals(attachmentAfterDeleteCaptor.getValue().getAttachmentHeader(), header);
	}

	@Test
	public void testFindAttachmentUrl_shouldReturnRightResult() {
		String attachmentId = UUID.randomUUID().toString();
		DocumentReference documentReference = new DocumentReference(BUSINESS_PARTNER_DOCUMENT_MODEL, RandomStringUtils.insecure().nextAlphabetic(20));
		DocumentV2 emptyDoc = DocumentV2.empty(BUSINESS_PARTNER_DOCUMENT_MODEL);
		DocumentV2 doc = metadataUtils.createDocumentMetadata(
			emptyDoc,
			documentUtils.generateDocRef(emptyDoc),
			UserConstants.ADMIN_USER, Instant.now(), null
		);
		DataServicesDocument dataServicesDocument = dataServicesDocumentFactory.newDataServicesDocument(doc);
		AttachmentReference<GenericReference> attachmentReference =
			AttachmentReference.parse(AttachmentReferenceType.DOCUMENT, documentReference.toString());
		AttachmentHeader attachmentHeader = AttachmentHeader.builder()
			.attachmentId(attachmentId)
			.filename(fileName)
			.references(List.of(attachmentReference))
			.typeOfTheContent(TypeOfTheContent.ATTACHMENT_SECURED)
			.build();
		AttachmentUrl attachmentUrl = new AttachmentUrl(RandomStringUtils.insecure().nextAlphabetic(50));
		QueryPage<String> queryResult = QueryPage.of(List.of("content"), 1, 0, 10, null);

		Mockito.doReturn(Optional.of(dataServicesDocument)).when(defaultDocumentRepository).findByDocumentReference(documentReference);
		Mockito.doReturn(Optional.of(attachmentHeader)).when(attachmentHeaderService).load(attachmentId);
		Mockito.doReturn(Optional.of(attachmentUrl)).when(attachmentRepository).findUrl(attachmentId, fileName, TypeOfTheContent.ATTACHMENT_SECURED);
		Mockito.doReturn(queryResult).when(queryService).query(Mockito.any(QueryRoot.class), Mockito.nullable(String.class));

		Optional<AttachmentUrl> result = defaultAttachmentService.findAttachmentUrl(attachmentId, documentReference);

		Mockito.verify(modelPermissionEvaluator).checkModelReadPermission(documentReference.getDocumentModelName());

		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get().getLocation(), attachmentUrl.getLocation());
	}

	@Test
	public void testFindAttachmentUrl_shouldReturnRightResult_whenAttachHeaderHasNullTypeOfTheContent() {
		String attachmentId = UUID.randomUUID().toString();
		DocumentReference documentReference = new DocumentReference(BUSINESS_PARTNER_DOCUMENT_MODEL, RandomStringUtils.insecure().nextAlphabetic(20));
		DocumentV2 emptyDoc = DocumentV2.empty(BUSINESS_PARTNER_DOCUMENT_MODEL);
		DocumentV2 doc = metadataUtils.createDocumentMetadata(
			emptyDoc,
			documentUtils.generateDocRef(emptyDoc),
			UserConstants.ADMIN_USER, Instant.now(),
			null
		);
		DataServicesDocument dataServicesDocument = dataServicesDocumentFactory.newDataServicesDocument(doc);
		AttachmentReference<GenericReference> attachmentReference =
			AttachmentReference.parse(AttachmentReferenceType.DOCUMENT, documentReference.toString());
		AttachmentHeader attachmentHeader = AttachmentHeader.builder()
			.attachmentId(attachmentId)
			.filename(fileName)
			.references(List.of(attachmentReference))
			.typeOfTheContent(TypeOfTheContent.ATTACHMENT_SECURED)
			.build();
		AttachmentUrl attachmentUrl = new AttachmentUrl(RandomStringUtils.insecure().nextAlphabetic(50));
		QueryPage<String> queryResult = QueryPage.of(List.of("content"), 1, 0, 10, null);

		Mockito.doReturn(Optional.of(dataServicesDocument)).when(defaultDocumentRepository).findByDocumentReference(documentReference);
		Mockito.doReturn(Optional.of(attachmentHeader)).when(attachmentHeaderService).load(attachmentId);
		Mockito.doReturn(Optional.of(attachmentUrl)).when(attachmentRepository).findUrl(attachmentId, fileName, TypeOfTheContent.ATTACHMENT_SECURED);
		Mockito.doReturn(queryResult).when(queryService).query(Mockito.any(QueryRoot.class), Mockito.nullable(String.class));

		Optional<AttachmentUrl> result = defaultAttachmentService.findAttachmentUrl(attachmentId, documentReference);

		Mockito.verify(modelPermissionEvaluator).checkModelReadPermission(documentReference.getDocumentModelName());

		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get().getLocation(), attachmentUrl.getLocation());
	}

	@Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "No URL from attachmentId .* could be found.")
	public void testFindAttachmentUrl_throwExactExceptionWhenDocumentNotFound() {
		String attachmentId = UUID.randomUUID().toString();
		DocumentReference documentReference = new DocumentReference(RandomStringUtils.insecure().nextAlphabetic(10), RandomStringUtils.insecure().nextAlphabetic(20));

		QueryPage<String> queryResult = QueryPage.of(new ArrayList<>(), 0, 0, 10, null);
		Mockito.doReturn(queryResult).when(queryService).query(Mockito.any(), Mockito.any());

		defaultAttachmentService.findAttachmentUrl(attachmentId, documentReference);
	}

	@Test
	public void testFindAttachmentUrl_throwExactException_whenNoHaveModelReadPermission() {
		String attachmentId = UUID.randomUUID().toString();
		DocumentReference documentReference = new DocumentReference(RandomStringUtils.insecure().nextAlphabetic(10), RandomStringUtils.insecure().nextAlphabetic(20));
		DocumentV2 emptyDoc = DocumentV2.empty(BUSINESS_PARTNER_DOCUMENT_MODEL);
		DocumentV2 doc = metadataUtils.createDocumentMetadata(
			emptyDoc,
			documentUtils.generateDocRef(emptyDoc),
			UserConstants.ADMIN_USER, Instant.now(),
			null
		);
		DataServicesDocument dataServicesDocument = dataServicesDocumentFactory.newDataServicesDocument(doc);
		Mockito.doReturn(Optional.of(dataServicesDocument)).when(defaultDocumentRepository).findByDocumentReference(documentReference);
		Mockito.doThrow(new AccessDeniedException(AuthConstants.ACCESS_DENIED)).when(modelPermissionEvaluator).checkModelReadPermission(Mockito.anyString());

		AccessDeniedException e = Assert.expectThrows(AccessDeniedException.class,
			() -> defaultAttachmentService.findAttachmentUrl(attachmentId, documentReference)
		);

		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(BUSINESS_PARTNER_DOCUMENT_MODEL);
		Mockito.verifyNoInteractions(documentPermissionEvaluator, attachmentHeaderService, attachmentRepository);
		Assert.assertEquals(e.getMessage(), AuthConstants.ACCESS_DENIED);
	}

	@Test
	public void testFindAttachmentUrl_throwExactException_whenNoHaveQueryPermission() {
		String attachmentId = UUID.randomUUID().toString();
		DocumentReference documentReference = new DocumentReference(RandomStringUtils.insecure().nextAlphabetic(10), RandomStringUtils.insecure().nextAlphabetic(20));
		DocumentV2 emptyDoc = DocumentV2.empty(BUSINESS_PARTNER_DOCUMENT_MODEL);
		DocumentV2 doc = metadataUtils.createDocumentMetadata(
			emptyDoc,
			documentUtils.generateDocRef(emptyDoc),
			UserConstants.ADMIN_USER, Instant.now(),
			null
		);
		DataServicesDocument dataServicesDocument = dataServicesDocumentFactory.newDataServicesDocument(doc);
		QueryPage<String> queryResult = QueryPage.of(new ArrayList<>(), 0, 0, 10, null);

		Mockito.doReturn(Optional.of(dataServicesDocument)).when(defaultDocumentRepository).findByDocumentReference(documentReference);
		Mockito.doReturn(queryResult).when(queryService).query(Mockito.any(), Mockito.any());

		AccessDeniedException e = Assert.expectThrows(AccessDeniedException.class,
			() -> defaultAttachmentService.findAttachmentUrl(attachmentId, documentReference)
		);

		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(BUSINESS_PARTNER_DOCUMENT_MODEL);
		Mockito.verifyNoInteractions(attachmentHeaderService, attachmentRepository);
		Assert.assertEquals(e.getMessage(), AuthConstants.ACCESS_DENIED);
	}

	@Test public void testFindThumbnailUrlById() {
		Mockito.when(attachmentHeaderService.load("attachment-id")).thenReturn(Optional.of(AttachmentHeader.builder().build()));

		defaultAttachmentService.findThumbnailUrl("attachment-id", ThumbnailType.SMALL);

		Mockito.verify(attachmentHeaderService, Mockito.times(1)).load("attachment-id");
		Mockito.verify(defaultAttachmentService, Mockito.times(1)).findThumbnailUrl(any(AttachmentHeader.class), any());
	}

	@Test public void testFindThumbnailUrlByHeader() {
		AttachmentHeader attachmentHeader = AttachmentHeader.builder().thumbnailSmallId("id").build();
		Mockito.when(thumbnailUrlGenerator.generateThumbnailUrl(attachmentHeader, ThumbnailType.SMALL)).thenReturn(Optional.empty());

		defaultAttachmentService.findThumbnailUrl(attachmentHeader, ThumbnailType.SMALL);

		Mockito.verify(thumbnailUrlGenerator, Mockito.times(1)).generateThumbnailUrl(attachmentHeader, ThumbnailType.SMALL);
		Mockito.verify(attachmentRepository, Mockito.times(1))
			.findUrl(attachmentHeader.getThumbnailSmallId(), attachmentHeader.getThumbnailSmallId(), TypeOfTheContent.ATTACHMENT_THUMBNAIL);
	}

	@Test public void testFindThumbnailUrlByHeader_thumbnailIdOptimization() {
		AttachmentHeader attachmentHeader = AttachmentHeader.builder().thumbnailSmallId("id").build();

		defaultAttachmentService.findThumbnailUrl(attachmentHeader, ThumbnailType.SMALL);

		Mockito.verify(thumbnailUrlGenerator, Mockito.times(1)).generateThumbnailUrl(attachmentHeader, ThumbnailType.SMALL);
	}

	@Test public void testFindThumbnailUrlByHeader_noRequiredThumbnail() {
		AttachmentHeader attachmentHeader = AttachmentHeader.builder().build();

		defaultAttachmentService.findThumbnailUrl(attachmentHeader, ThumbnailType.SMALL);

		Mockito.verify(thumbnailUrlGenerator, Mockito.times(0)).generateThumbnailUrl(attachmentHeader, ThumbnailType.SMALL);
	}

	@SneakyThrows
	@Test
	public void testCreateAttachment_shouldPassNullMimeType_whenProbeMimeTypeDisabled() {
		dataServicesCoreProperties.getAttachments().getMimeType().getProbeMimeType().setEnabled(false);
		mockSuccessSaveContents();
		mockAttachmentHeader();

		defaultAttachmentService.createAttachment(
			Objects.requireNonNull(this.getClass().getResourceAsStream(PNG_ATTACHMENT_PATH)),
			fileName, modelName, pathToField, annotations);

		Mockito.verify(attachmentRepository, Mockito.times(1))
			.create(ArgumentMatchers.anyString(), any(InputStream.class), ArgumentMatchers.anyString(),
				ArgumentMatchers.eq(TypeOfTheContent.ATTACHMENT_SECURED), ArgumentMatchers.isNull());
		Mockito.verify(contentTypeDetector, Mockito.never()).probeContentType(any(), any());
	}

	@SneakyThrows
	@Test
	public void testCreateAttachment_shouldPassProbedMimeType_whenProbeMimeTypeEnabled() {
		dataServicesCoreProperties.getAttachments().getMimeType().getProbeMimeType().setEnabled(true);
		String probedMimeType = "image/png";
		Mockito.when(contentTypeDetector.probeContentType(any(InputStream.class), ArgumentMatchers.anyString()))
			.thenReturn(probedMimeType);
		mockSuccessSaveContents();
		mockAttachmentHeader();

		defaultAttachmentService.createAttachment(
			Objects.requireNonNull(this.getClass().getResourceAsStream(PNG_ATTACHMENT_PATH)),
			fileName, modelName, pathToField, annotations);

		Mockito.verify(attachmentRepository, Mockito.times(1))
			.create(ArgumentMatchers.anyString(), any(InputStream.class), ArgumentMatchers.anyString(),
				ArgumentMatchers.eq(TypeOfTheContent.ATTACHMENT_SECURED), ArgumentMatchers.eq(probedMimeType));
		Mockito.verify(contentTypeDetector, Mockito.times(1)).probeContentType(any(InputStream.class), ArgumentMatchers.eq(fileName));

		dataServicesCoreProperties.getAttachments().getMimeType().getProbeMimeType().setEnabled(false);
	}

	private void setupRollbackSaveAttachment() {
		Mockito.when(attachmentHeaderService.create(any(AttachmentHeader.class))).thenThrow(new UnexpectedException(errorMessage));
	}

	private void mockSuccessSaveContents() {
		AttachmentPersistenceResult result = AttachmentPersistenceResult
			.builder()
			.size(1)
			.mimeType(MimeTypeUtils.APPLICATION_JSON_VALUE)
			.attachmentId(contentId)
			.build();
		Mockito.when(
				attachmentRepository.create(any(), any(), any(), any(TypeOfTheContent.class), ArgumentMatchers.nullable(String.class)))
			.thenReturn(result);
	}

	private AttachmentHeader mockAttachmentHeader() {
		return mockAttachmentHeader(UUID.randomUUID().toString());
	}

	private AttachmentHeader mockAttachmentHeader(String uuid) {
		Instant now = Instant.now();
		AttachmentHeader attachmentHeader = AttachmentHeader.builder()
			.attachmentId(uuid)
			.createdAt(now)
			.createdBy(name)
			.size(100L)
			.modifiedAt(now)
			.modifiedBy(name)
			.filename(fileName)
			.mimeType(MimeTypeUtils.IMAGE_PNG_VALUE)
			.thumbnailBigId(UUID.randomUUID().toString())
			.thumbnailSmallId(UUID.randomUUID().toString())
			.annotations(annotations)
			.build();

		Mockito.when(attachmentHeaderService.create(any(AttachmentHeader.class))).thenReturn(attachmentHeader);
		return attachmentHeader;
	}

	private void verifyCreateAttachmentHeaderByType(TypeOfTheContent expectedTypeOfTheContent) {
		Mockito.verify(attachmentHeaderService, Mockito.times(1))
			.create(ArgumentMatchers.argThat(header -> {
				Assert.assertEquals(header.getCreatedBy(), name);
				Assert.assertEquals(header.getSize(), 1);
				Assert.assertEquals(header.getFilename(), fileName);
				Assert.assertEquals(header.getMimeType(), MimeTypeUtils.APPLICATION_JSON_VALUE);
				Assert.assertEquals(header.getAnnotations(), annotations);
				Assert.assertEquals(header.getTypeOfTheContent(), expectedTypeOfTheContent);
				Assert.assertEquals(header.getModifiedBy(), name);
				return true;
			}));
	}
}
