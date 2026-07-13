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
package com.mgmtp.a12.contentstore.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.contentstore.AbstractContentStoreTest;
import com.mgmtp.a12.contentstore.ContentPersistenceResult;
import com.mgmtp.a12.contentstore.TestConfiguration;
import com.mgmtp.a12.contentstore.autoconfigure.internal.ContentStoreAutoConfiguration;
import com.mgmtp.a12.contentstore.autoconfigure.internal.ContentStoreRepositoryConfiguration;
import com.mgmtp.a12.contentstore.content.ContentStream;
import com.mgmtp.a12.contentstore.content.internal.jpa.entity.ContentEntity;
import com.mgmtp.a12.contentstore.service.internal.DefaultContentStoreServiceIT;
import com.mgmtp.a12.contentstore.ticket.internal.jpa.entity.TicketInfoEntity;
import com.mgmtp.a12.contentstore.utils.Constants;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;

@SpringBootTest(classes = { TestConfiguration.class,
	ContentStoreAutoConfiguration.class, ContentStoreRepositoryConfiguration.class },
	properties = {
		"mgmtp.a12.dataservices.contentstore.storage.content-storage=DB"
	})
public class DefaultContentStoreServiceInDatabaseModeIT extends AbstractContentStoreTest {

	@BeforeMethod
	public void initData() {
		privateContentId = UUID.randomUUID().toString();
		InputStream privateContentStream = new ByteArrayInputStream(PRIVATE_UPLOAD_CONTENT.getBytes());
		contentStoreService.saveContent(privateContentId, Constants.PERSISTENT_TYPE_PRIVATE, privateContentStream, FILE_NAME);

		publicContentId = UUID.randomUUID().toString();
		InputStream publicContentStream = new ByteArrayInputStream(PUBLIC_UPLOAD_CONTENT.getBytes());
		contentStoreService.saveContent(publicContentId, Constants.PERSISTENT_TYPE_PUBLIC, publicContentStream, FILE_NAME);

	}

	@Test(dataProvider = "mimeTypeContentCases")
	public void testPublicContentMimeTypesPersistence(String fileName, String expectedMimeType) throws IOException {
		Resource resource = resourceLoader.getResource("classpath:files/" + fileName);
		String contentId = UUID.randomUUID().toString();

		ContentPersistenceResult publicContentPersistenceResult =
			persistContent(contentId, Constants.PERSISTENT_TYPE_PUBLIC, resource.getContentAsByteArray(), fileName);

		Assert.assertEquals(publicContentPersistenceResult.getContentId(), contentId);
		Assert.assertEquals(publicContentPersistenceResult.getContentType(), expectedMimeType);
		Assert.assertTrue(publicContentPersistenceResult.getSize() > 0);
		Assert.assertTrue(publicContentPersistenceResult.getUrl().isPresent());
	}

	@Test
	public void testPersistContent_shouldReturnJsonContentType_whenPersistJsonFileWithoutFileName() throws IOException {
		Resource resource = resourceLoader.getResource("classpath:files/AttachmentJson.json");
		String contentId = UUID.randomUUID().toString();

		ContentPersistenceResult publicContentPersistenceResult =
			persistContent(contentId, Constants.PERSISTENT_TYPE_PUBLIC, resource.getContentAsByteArray(), null);

		Assert.assertEquals(publicContentPersistenceResult.getContentId(), contentId);
		Assert.assertEquals(publicContentPersistenceResult.getContentType(), MimeTypeUtils.APPLICATION_JSON_VALUE);
		Assert.assertTrue(publicContentPersistenceResult.getSize() > 0);
		Assert.assertTrue(publicContentPersistenceResult.getUrl().isPresent());
	}

	@Test(dataProvider = "mimeTypeContentCases")
	public void testPrivateContentMimeTypesPersistence(String fileName, String expectedMimeType) throws IOException {
		Resource resource = resourceLoader.getResource("classpath:files/" + fileName);
		String contentId = UUID.randomUUID().toString();

		ContentPersistenceResult publicContentPersistenceResult =
			persistContent(contentId, Constants.PERSISTENT_TYPE_PRIVATE, resource.getContentAsByteArray(), fileName);

		Assert.assertEquals(publicContentPersistenceResult.getContentId(), contentId);
		Assert.assertEquals(publicContentPersistenceResult.getContentType(), expectedMimeType);
		Assert.assertTrue(publicContentPersistenceResult.getSize() > 0);
		Assert.assertFalse(publicContentPersistenceResult.getUrl().isPresent());
	}

	@Test(dataProvider = "mimeTypeContentCases")
	public void testPublicContentMimeTypesWithFilenamePersistence(String fileName, String expectedMimeType) throws IOException {
		Resource resource = resourceLoader.getResource("classpath:files/" + fileName);
		String contentId = UUID.randomUUID().toString();
		InputStream contentInputStream = new ByteArrayInputStream(resource.getContentAsByteArray());

		ContentPersistenceResult publicContentPersistenceResult =
			contentStoreService.saveContent(contentId, Constants.PERSISTENT_TYPE_PUBLIC, contentInputStream, fileName);

		Assert.assertEquals(publicContentPersistenceResult.getContentId(), contentId);
		Assert.assertEquals(publicContentPersistenceResult.getContentType(), expectedMimeType);
		Assert.assertTrue(publicContentPersistenceResult.getSize() > 0);
		Assert.assertTrue(publicContentPersistenceResult.getUrl().isPresent());
	}

	@Test
	public void testSaveSuccessfully_dataIsSaved() throws IOException {
		String contentId = UUID.randomUUID().toString();

		persistContent(contentId, Constants.PERSISTENT_TYPE_PUBLIC, UPLOAD_CONTENT.getBytes(), FILE_NAME);
		ContentStream contentStream = contentStoreService.getContent(contentId);

		Assert.assertEquals(contentStream.getContentSupplier().get().readAllBytes(), UPLOAD_CONTENT.getBytes());
		verifyContentColumnIsNotNull(contentId);
	}

	private void verifyContentColumnIsNotNull(String contentId) {
		// Verify that no content is persisted to content table since this is FS storage
		Optional<ContentEntity> contentEntity = contentJpaRepository.findById(contentId);
		Assert.assertTrue(contentEntity.isPresent());
		Assert.assertNotNull(contentEntity.get().getContent());
	}

	@Test
	public void testSaveContent_hasError_rollbackTicketSaved() {
		DefaultContentStoreServiceIT.TestEventListener testEventListener = applicationContext.getBean(DefaultContentStoreServiceIT.TestEventListener.class);
		String contentId = testEventListener.getContentIdForSavingTest();
		InputStream contentInputStream = new ByteArrayInputStream(UPLOAD_CONTENT.getBytes());
		// save content and listener has an error.
		RuntimeException exception =
			Assert.expectThrows(RuntimeException.class,
				() -> contentStoreService.saveContent(contentId, Constants.PERSISTENT_TYPE_PUBLIC, contentInputStream, FILE_NAME));
		Assert.assertEquals(exception.getMessage(), "Event after create has error");

		// verify rollback is running, and content with contentId above is not existed.
		NotFoundException notFoundException = Assert.expectThrows(
			NotFoundException.class,
			() -> contentStoreService.getContent(contentId)
		);

		Assert.assertEquals(notFoundException.getShortMessage().getKey(), "error.content-store.content.notFound");
	}

	@Test
	public void getPrivateContent_updateTicketSuccessfully() throws IOException {

		String contentId = UUID.randomUUID().toString();

		persistContent(contentId, Constants.PERSISTENT_TYPE_PRIVATE, UPLOAD_CONTENT.getBytes(), FILE_NAME);

		TicketInfoEntity ticketInfoEntity = ticketService.createTicket(contentId, 3000);

		Assert.assertNotNull(ticketInfoEntity);

		// download content with private content.
		ContentStream contentStream = contentStoreService.getContent(ticketInfoEntity.getTicketId());

		Assert.assertEquals(contentStream.getContentSupplier().get().readAllBytes(), UPLOAD_CONTENT.getBytes());

		// verify ticket is updated.
		Optional<TicketInfoEntity> ticketOpt = ticketService.findTicket(ticketInfoEntity.getTicketId());
		Assert.assertTrue(ticketOpt.isPresent());
		Assert.assertFalse(ticketInfoEntity.isDownloaded());
		Assert.assertTrue(ticketOpt.get().isDownloaded());
	}

	@Test
	public void getPrivateContent_hasError_rollbackTicketUpdate() {

		DefaultContentStoreServiceIT.TestEventListener testEventListener = applicationContext.getBean(DefaultContentStoreServiceIT.TestEventListener.class);
		String contentId = testEventListener.getContentIdForDownloadPrivateTest();
		InputStream contentInputStream = new ByteArrayInputStream(UPLOAD_CONTENT.getBytes());

		// save content and listener has an error.
		contentStoreService.saveContent(contentId, Constants.PERSISTENT_TYPE_PRIVATE, contentInputStream, FILE_NAME);

		TicketInfoEntity ticketInfoEntity = ticketService.createTicket(contentId, 3000);

		Assert.assertNotNull(ticketInfoEntity);

		// validate rollback is running, ticket is not updated.
		RuntimeException mockedException = Assert.expectThrows(
			RuntimeException.class,
			() -> contentStoreService.getContent(ticketInfoEntity.getTicketId())
		);
		Assert.assertEquals(mockedException.getMessage(), "Event after download has an error");

		Optional<TicketInfoEntity> ticketOpt = ticketService.findTicket(ticketInfoEntity.getTicketId());
		Assert.assertTrue(ticketOpt.isPresent());
		Assert.assertFalse(ticketInfoEntity.isDownloaded());
		Assert.assertFalse(ticketOpt.get().isDownloaded());
	}

	@Test
	public void requestPrivateContentUrl_success_returnUrl() {
		String url = contentStoreService.requestContentUrl(privateContentId, 3000);
		String ticketId = url.substring(url.lastIndexOf("/") + 1);
		ContentStream contentStream = contentStoreService.getContent(ticketId);
		Assert.assertNotNull(contentStream);
	}

	@Test
	public void requestNotFoundContentUrl_throwException() {
		// content is not found throw error
		String contentId = UUID.randomUUID().toString();
		NotFoundException notFoundException1 = Assert.expectThrows(
			NotFoundException.class,
			() -> contentStoreService.requestContentUrl(contentId, 3000));
		Assert.assertEquals(notFoundException1.getMessage(), "Cannot find content by id " + contentId);

		// with public content still throw NotFoundException
		NotFoundException notFoundException2 = Assert.expectThrows(
			NotFoundException.class,
			() -> contentStoreService.requestContentUrl(publicContentId, 3000));
		Assert.assertEquals(notFoundException2.getMessage(), "Cannot find content by id " + publicContentId);
	}

	@Test
	public void getPublicContentUrl_testAllCase() {
		// with public content we can get public url
		Assert.assertTrue(contentStoreService.findPublicContentUrl(publicContentId).isPresent());

		// with private or not-existed content we can not get  url
		Assert.assertFalse(contentStoreService.findPublicContentUrl(privateContentId).isPresent());
		Assert.assertFalse(contentStoreService.findPublicContentUrl("randomId").isPresent());
	}

	@Test
	public void exists_testAllCase() {
		// assert true with exact id and persistent type.
		Assert.assertTrue(contentStoreService.exists(publicContentId, Constants.PERSISTENT_TYPE_PUBLIC));
		Assert.assertTrue(contentStoreService.exists(privateContentId, Constants.PERSISTENT_TYPE_PRIVATE));

		// assert false with the other cases.
		Assert.assertFalse(contentStoreService.exists(publicContentId, Constants.PERSISTENT_TYPE_PRIVATE));
		Assert.assertFalse(contentStoreService.exists(privateContentId, Constants.PERSISTENT_TYPE_PUBLIC));
		Assert.assertFalse(contentStoreService.exists("publicContentId", Constants.PERSISTENT_TYPE_PUBLIC));
		Assert.assertFalse(contentStoreService.exists("privateContentId", Constants.PERSISTENT_TYPE_PRIVATE));
	}

	@Test
	public void deleteById_testAllCase() {
		Assert.assertTrue(contentService.findByContentIdAndPersistentType(privateContentId, Constants.PERSISTENT_TYPE_PRIVATE).isPresent());
		Assert.assertTrue(contentService.findByContentIdAndPersistentType(publicContentId, Constants.PERSISTENT_TYPE_PUBLIC).isPresent());

		contentStoreService.deleteById(privateContentId);
		contentStoreService.deleteById(publicContentId);

		Assert.assertFalse(contentService.findByContentIdAndPersistentType(privateContentId, Constants.PERSISTENT_TYPE_PRIVATE).isPresent());
		Assert.assertFalse(contentService.findByContentIdAndPersistentType(publicContentId, Constants.PERSISTENT_TYPE_PUBLIC).isPresent());
	}
}
