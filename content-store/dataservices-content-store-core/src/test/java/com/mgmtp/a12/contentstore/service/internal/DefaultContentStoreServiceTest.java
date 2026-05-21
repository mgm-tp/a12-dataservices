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
package com.mgmtp.a12.contentstore.service.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MediaType;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.MimeTypeUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.contentstore.AbstractContentStoreTest;
import com.mgmtp.a12.contentstore.ContentPersistenceResult;
import com.mgmtp.a12.contentstore.content.ContentStream;
import com.mgmtp.a12.contentstore.content.internal.ContentValidator;
import com.mgmtp.a12.contentstore.content.internal.jpa.entity.ContentHeaderEntity;
import com.mgmtp.a12.contentstore.events.ContentAfterCreateEvent;
import com.mgmtp.a12.contentstore.events.ContentAfterDownloadEvent;
import com.mgmtp.a12.contentstore.events.ContentAfterRequestEvent;
import com.mgmtp.a12.contentstore.events.ContentBeforeCreateEvent;
import com.mgmtp.a12.contentstore.events.ContentBeforeDownloadEvent;
import com.mgmtp.a12.contentstore.exception.ExceptionKeys;
import com.mgmtp.a12.contentstore.exception.InvalidTypeException;
import com.mgmtp.a12.contentstore.ticket.internal.jpa.entity.TicketInfoEntity;
import com.mgmtp.a12.contentstore.utils.Constants;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.common.exception.InvalidSizeException;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;

import static com.mgmtp.a12.contentstore.constants.Constants.BASE_URL;
import static com.mgmtp.a12.contentstore.constants.Constants.CONTENT;
import static com.mgmtp.a12.contentstore.constants.Constants.CONTENT_INPUT_STREAM;
import static com.mgmtp.a12.contentstore.constants.Constants.FILE_NAME;
import static com.mgmtp.a12.contentstore.constants.Constants.TICKET_DURATION;
import static com.mgmtp.a12.contentstore.utils.Constants.PERSISTENT_TYPE_PRIVATE;
import static com.mgmtp.a12.contentstore.utils.Constants.PERSISTENT_TYPE_PUBLIC;
import static com.mgmtp.a12.contentstore.utils.internal.UrlUtils.CONTENT_STORE_DOWNLOAD_URL_PATTERN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class DefaultContentStoreServiceTest extends AbstractContentStoreTest {
	private static final String CONTENT_MIME_TYPE_IS_MANDATORY = "Content mime type is mandatory";

	private ContentValidator contentValidator;
	@Mock private ApplicationEventPublisher eventPublisher;
	private DefaultContentStoreService contentStoreService;

	private String contentId;
	private String ticketId;

	@Override
	@BeforeMethod public void init() throws IOException {
		super.init();
		contentValidator =
			spy(new ContentValidator(contentTypeDetector, contentStoreProperties));
		contentStoreService = new DefaultContentStoreService(ticketService, contentService, ticketValidator, contentValidator,
			contentStoreProperties, eventPublisher, contentTypeDetector);
		contentId = UUID.randomUUID().toString();
		ticketId = UUID.randomUUID().toString();
	}

	@Test public void testRequestContentUrl_shouldReturnValidUrl_whenGivenValidPrivateContent() {
		String thisContentId = UUID.randomUUID().toString();
		String thisTicketId = UUID.randomUUID().toString();

		ContentHeaderEntity contentHeaderEntity = new ContentHeaderEntity(thisContentId, PERSISTENT_TYPE_PRIVATE, MediaType.TEXT_PLAIN.toString());

		Mockito.when(contentService.findHeaderById(thisContentId)).thenReturn(Optional.of(contentHeaderEntity));
		Mockito.when(contentService.findBinaryContentById(thisContentId)).thenReturn(Optional.of(CONTENT.getBytes()));
		TicketInfoEntity ticketInfoEntity = mockValidTicket(thisContentId, thisTicketId, TICKET_DURATION);
		mockValidContentEntity(thisContentId, PERSISTENT_TYPE_PRIVATE);

		String actualUrl = contentStoreService.requestContentUrl(thisContentId, TICKET_DURATION);

		Assert.assertTrue(StringUtils.isNotBlank(actualUrl));
		Assert.assertEquals(actualUrl,
			String.format(CONTENT_STORE_DOWNLOAD_URL_PATTERN, BASE_URL, contentStoreProperties.getServer().getContextPath(), thisTicketId));

		ArgumentCaptor<ContentAfterRequestEvent> contentAfterRequestEventArgumentCaptor = ArgumentCaptor.forClass(ContentAfterRequestEvent.class);

		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(contentAfterRequestEventArgumentCaptor.capture());

		Assert.assertEquals(contentAfterRequestEventArgumentCaptor.getValue().getTicketInfoEntity(), ticketInfoEntity);
		Assert.assertNotNull(contentAfterRequestEventArgumentCaptor.getValue().getContentStream().getContentType());
		Assert.assertEquals(contentAfterRequestEventArgumentCaptor.getValue().getContentStream().getContentType(), PERSISTENT_TYPE_PRIVATE);
	}

	@Test public void testRequestContentUrl_shouldThrowException_whenGivenPublicContent() {
		mockValidContentEntity(contentId, Constants.PERSISTENT_TYPE_PUBLIC);
		NotFoundException exception =
			Assert.expectThrows(NotFoundException.class, () -> contentStoreService.requestContentUrl(contentId, TICKET_DURATION));

		Assert.assertNotNull(exception);
		Assert.assertEquals(exception.getMessage(), String.format(Constants.CANNOT_FIND_CONTENT_BY_ID_PATTERN, contentId));
	}

	@Test public void testFindPublicContentUrl_shouldReturnValidUrl_whenGivenValidPublicContent() {

		mockValidContentEntity(contentId, Constants.PERSISTENT_TYPE_PUBLIC);
		when(contentService.exists(contentId, Constants.PERSISTENT_TYPE_PUBLIC)).thenReturn(true);
		Optional<String> actualUrl = contentStoreService.findPublicContentUrl(contentId);

		Assert.assertTrue(actualUrl.isPresent());
		Assert.assertTrue(StringUtils.isNotBlank(actualUrl.get()));
		Assert.assertEquals(actualUrl.get(),
			String.format(CONTENT_STORE_DOWNLOAD_URL_PATTERN, BASE_URL, contentStoreProperties.getServer().getContextPath(), contentId));
	}

	@Test public void testFindPublicContentUrl_shouldReturnEmpty_whenGivenValidPrivateContent() {
		mockValidContentEntity(contentId, PERSISTENT_TYPE_PRIVATE);
		Mockito.when(contentHeaderJpaRepository.existsByIdAndPersistentType(contentId, Constants.PERSISTENT_TYPE_PUBLIC)).thenReturn(false);
		Optional<String> actualUrl = contentStoreService.findPublicContentUrl(contentId);

		Assert.assertFalse(actualUrl.isPresent());
	}

	@Test public void testGetContent_shouldReturnValidContentStream_whenGivenValidPrivateContent() {
		mockValidContentEntity(contentId, PERSISTENT_TYPE_PRIVATE);
		mockValidTicket(contentId, ticketId, TICKET_DURATION);
		Mockito.when(ticketValidator.isAvailableTicket(any(TicketInfoEntity.class))).thenReturn(true);
		mockReadyContentStream();

		ContentStream actualContentStream = contentStoreService.getContent(ticketId);

		Assert.assertNotNull(actualContentStream);
		Assert.assertEquals(actualContentStream.getContentType(), MediaType.TEXT_PLAIN.toString());

		ArgumentCaptor<ContentBeforeDownloadEvent> contentBeforeDownloadEventArgumentCaptor = ArgumentCaptor.forClass(ContentBeforeDownloadEvent.class);
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(contentBeforeDownloadEventArgumentCaptor.capture());
		Assert.assertEquals(contentBeforeDownloadEventArgumentCaptor.getValue().getContentId(), contentId);
		Assert.assertEquals(contentBeforeDownloadEventArgumentCaptor.getValue().getPersistentType(), PERSISTENT_TYPE_PRIVATE);

		ArgumentCaptor<ContentAfterDownloadEvent> contentAfterDownloadEventArgumentCaptor = ArgumentCaptor.forClass(ContentAfterDownloadEvent.class);
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(contentAfterDownloadEventArgumentCaptor.capture());
		Assert.assertEquals(contentAfterDownloadEventArgumentCaptor.getValue().getContentId(), contentId);
	}

	@Test public void testGetContent_shouldReturnValidContentStream_whenGivenValidPublicContent() {
		mockValidContentEntity(contentId, Constants.PERSISTENT_TYPE_PUBLIC);
		mockReadyContentStream();

		ContentStream actualContentStream = contentStoreService.getContent(contentId);

		Assert.assertNotNull(actualContentStream);
		Assert.assertEquals(actualContentStream.getContentType(), MediaType.TEXT_PLAIN.toString());

		ArgumentCaptor<ContentBeforeDownloadEvent> contentBeforeDownloadEventArgumentCaptor = ArgumentCaptor.forClass(ContentBeforeDownloadEvent.class);

		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(contentBeforeDownloadEventArgumentCaptor.capture());
		Mockito.verify(eventPublisher, Mockito.times(0)).publishEvent(Mockito.any());

		Assert.assertEquals(contentBeforeDownloadEventArgumentCaptor.getValue().getContentId(), contentId);
		Assert.assertEquals(contentBeforeDownloadEventArgumentCaptor.getValue().getPersistentType(), Constants.PERSISTENT_TYPE_PUBLIC);
	}

	@DataProvider public Object[][] persistentTypeData() {
		return new Object[][] {
			new Object[] { Constants.PERSISTENT_TYPE_PUBLIC },
			new Object[] { PERSISTENT_TYPE_PRIVATE }
		};
	}

	@Test(dataProvider = "persistentTypeData")
	public void testSaveContent_shouldSuccess_whenGivenValidContent(String persistentType) throws IOException {
		ContentPersistenceResult expectedResult = ContentPersistenceResult.builder()
			.contentId(contentId)
			.contentType(MediaType.TEXT_PLAIN.toString())
			.size(0)
			.url(BASE_URL.describeConstable())
			.build();
		Mockito.when(contentService.save(any(), any(), any(), any(InputStream.class))).thenReturn(expectedResult);

		Mockito.doReturn(2000L).when(contentValidator).getSizeAndValidate(any(byte[].class));
		Mockito.doReturn("text/plain").when(contentTypeDetector).probeContentType(any(InputStream.class), eq(FILE_NAME));



		ContentPersistenceResult actualResult = contentStoreService.saveContent(contentId, persistentType, CONTENT_INPUT_STREAM, FILE_NAME);

		Assert.assertEquals(actualResult, expectedResult);
		verify(contentTypeDetector, times(1)).probeContentType(any(InputStream.class), eq(FILE_NAME));

		ArgumentCaptor<ContentBeforeCreateEvent> contentBeforeCreateCaptor = ArgumentCaptor.forClass(ContentBeforeCreateEvent.class);
		ArgumentCaptor<ContentAfterCreateEvent> contentAfterCreateCaptor = ArgumentCaptor.forClass(ContentAfterCreateEvent.class);

		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(contentBeforeCreateCaptor.capture());
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(contentAfterCreateCaptor.capture());

		Assert.assertEquals(contentBeforeCreateCaptor.getValue().getContentId(), contentId);
		Assert.assertEquals(contentBeforeCreateCaptor.getValue().getPersistentType(), persistentType);
		Assert.assertEquals(contentBeforeCreateCaptor.getValue().getContentType(), "text/plain");
		Assert.assertTrue(contentBeforeCreateCaptor.getValue().getInputStream().isPresent());

		Assert.assertEquals(contentAfterCreateCaptor.getValue().getContentId(), contentId);
		Assert.assertEquals(contentAfterCreateCaptor.getValue().getPersistentType(), persistentType);
		Assert.assertEquals(contentAfterCreateCaptor.getValue().getContentType(), "text/plain");
		Assert.assertEquals(contentAfterCreateCaptor.getValue().getSize(), 2000);
	}

	@Test(dataProvider = "persistentTypeData")
	public void testSaveContentTrustExternalMimeType_shouldSuccess_whenGivenValidContent(String persistentType) {
		contentStoreProperties.getServer().getApi().getMimeType().getTrustExternalMimeType().setEnabled(true);
		ContentPersistenceResult expectedResult = ContentPersistenceResult.builder()
			.contentId(contentId)
			.contentType(MediaType.TEXT_PLAIN.toString())
			.size(0)
			.url(BASE_URL.describeConstable())
			.build();
		Mockito.when(contentService.save(any(), any(), any(), any(InputStream.class))).thenReturn(expectedResult);
		Mockito.doReturn(2000L).when(contentValidator).getSizeAndValidate(any(byte[].class));

		ContentPersistenceResult actualResult =
			contentStoreService.saveContent(contentId, persistentType, CONTENT_INPUT_STREAM, FILE_NAME, MimeTypeUtils.IMAGE_PNG_VALUE);

		Assert.assertEquals(actualResult, expectedResult);
		verifyNoInteractions(contentTypeDetector);
		contentStoreProperties.getServer().getApi().getMimeType().getTrustExternalMimeType().setEnabled(false);
	}

	@Test(dataProvider = "persistentTypeData")
	public void testSaveContent_shouldThrowInvalidInputException_whenMissingExternalMimeType(String persistentType) {
		contentStoreProperties.getServer().getApi().getMimeType().getTrustExternalMimeType().setEnabled(true);

		InvalidInputException invalidInputException = Assert.expectThrows(InvalidInputException.class, () ->
			contentStoreService.saveContent(contentId, persistentType, CONTENT_INPUT_STREAM, FILE_NAME));

		Assert.assertNotNull(invalidInputException);
		Assert.assertEquals(invalidInputException.getMessage(), CONTENT_MIME_TYPE_IS_MANDATORY);
		contentStoreProperties.getServer().getApi().getMimeType().getTrustExternalMimeType().setEnabled(false);
	}

	@Test(expectedExceptions = {InvalidTypeException.class}, expectedExceptionsMessageRegExp = "Invalid persistent type \\[.*]")
	public void testSaveContent_shouldThrowInvalidTypeException_whenPersistentTypeIsNull() {
		contentStoreService.saveContent(contentId, null, CONTENT_INPUT_STREAM, FILE_NAME);
	}
	@Test(expectedExceptions = {NullPointerException.class}, expectedExceptionsMessageRegExp = "inputStream is marked non-null but is null")
	public void testSaveContent_shouldThrowException_whenContentStreamNull() {
		contentStoreService.saveContent(contentId, PERSISTENT_TYPE_PUBLIC, null, FILE_NAME);
	}

	@Test
	public void testSaveContent_shouldNotBeSaved_whenFileSizeExceedLimit() {
		Mockito.when(contentValidator.getSizeAndValidate(any(byte[].class))).thenThrow(new InvalidSizeException(ExceptionKeys.INVALID_CONTENT_SIZE_ERROR_KEY,
			Constants.CONTENT_SIZE_CANNOT_EXCEED_LIMIT_PATTERN.formatted("10MB")));

		Assert.expectThrows(InvalidSizeException.class,
			() -> contentStoreService.saveContent(contentId, PERSISTENT_TYPE_PUBLIC, CONTENT_INPUT_STREAM, FILE_NAME));

		Mockito.verify(
			contentService,
			Mockito.never().description("contentService#save should not be called.")
			// The 3rd parameter contentType may be null, so do not use anyString() for parameter check for it
		).save(anyString(), anyString(), any(), any(InputStream.class));
	}

	@DataProvider public Object[][] saveContentError() {
		return new Object[][] {
			new Object[] { (Runnable) () -> Mockito.when(contentValidator.getSizeAndValidate(any(byte[].class)))
				.thenThrow(new InvalidSizeException(ExceptionKeys.INVALID_CONTENT_SIZE_ERROR_KEY,
					Constants.CONTENT_SIZE_CANNOT_EXCEED_LIMIT_PATTERN.formatted("10MB"))) },
			new Object[] {
				(Runnable) () -> Mockito.when(contentService.save(anyString(), anyString(), anyString(), any(InputStream.class))).thenThrow(new RuntimeException()) },
			new Object[] { (Runnable) () -> Mockito.doThrow(new RuntimeException()).when(eventPublisher).publishEvent(any(ContentBeforeCreateEvent.class)) },
			new Object[] { (Runnable) () -> Mockito.doThrow(new RuntimeException()).when(eventPublisher).publishEvent(any(ContentAfterCreateEvent.class)) }
		};
	}

	@Test(dataProvider = "saveContentError")
	public void testSaveContent_shouldCleanUpSavedFile_ifThereIsError(Runnable errorSetup) {
		errorSetup.run();

		Assert.expectThrows(Exception.class, () -> contentStoreService.saveContent(contentId, PERSISTENT_TYPE_PUBLIC, CONTENT_INPUT_STREAM, FILE_NAME));

		Mockito.verify(
			contentService,
			Mockito.times(1).description("File need to be cleaned up if there is error")
		).deleteContentById(anyString());
	}

	@Test(dataProvider = "persistentTypeData")
	public void testExists_shouldReturnRightValue(String persistentType) {
		String id = UUID.randomUUID().toString();

		Mockito.doReturn(true).when(contentService).exists(id, persistentType);
		Assert.assertTrue(contentStoreService.exists(id, persistentType));
	}

	@Test
	public void testDeleteById_shouldCallRightMethod() {
		String id = UUID.randomUUID().toString();
		contentStoreService.deleteById(id);
		Mockito.verify(contentService).deleteContentById(id);
	}

	private void mockReadyContentStream() {
		Mockito.doAnswer(invocation -> {
			Object[] args = invocation.getArguments();
			((ContentBeforeDownloadEvent) args[0])
				.getContentStream()
				.setReady();
			return null; // void method, so return null
		}).when(eventPublisher).publishEvent(any(ContentBeforeDownloadEvent.class));
	}
}
