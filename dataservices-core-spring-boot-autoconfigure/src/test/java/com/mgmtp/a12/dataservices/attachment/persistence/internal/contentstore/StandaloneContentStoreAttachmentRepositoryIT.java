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
package com.mgmtp.a12.dataservices.attachment.persistence.internal.contentstore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.ResourceAccessException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.contentstore.ContentPersistenceResult;
import com.mgmtp.a12.contentstore.DownloadUrlResponse;
import com.mgmtp.a12.contentstore.client.content.ContentStorePrivateClient;
import com.mgmtp.a12.contentstore.client.content.ContentStoreTicketClient;
import com.mgmtp.a12.contentstore.client.exception.BadRequestException;
import com.mgmtp.a12.contentstore.client.exception.RestErrorDetail;
import com.mgmtp.a12.contentstore.client.localization.LocalizedEntry;
import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.ResourceFunctions;
import com.mgmtp.a12.dataservices.attachment.AttachmentUrl;
import com.mgmtp.a12.dataservices.attachment.TypeOfTheContent;
import com.mgmtp.a12.dataservices.attachment.persitence.AttachmentPersistenceResult;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.ThumbnailUtil;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.contentstore.ContentStoreMapper;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.contentstore.StandaloneContentStoreAttachmentRepository;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.exception.ContentStoreClientException;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.utils.AttachmentConstants;

import lombok.SneakyThrows;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.expectThrows;

@Test
@ActiveProfiles({ "dataservices-uaa", "dataservices-local_auth", "dataservices-embedded_postgres", "contentstore-embedded_postgres",
	"dataservices-standalone_contentstore", "dataservices-actuators", "dataservices-cdd_sync",
	"dataservices-core-test-test_logging", "dataservices-rpc", "dataservices-embedded_jms", "dataservices-core-test-qa" })
public class StandaloneContentStoreAttachmentRepositoryIT extends AbstractSpringContextIT {

	private static final String CS_DOWNLOAD_URL = "http://localhost:8080/cs/download/";
	private static final String FULL_IMAGE_PATH = PathConstants.ATTACHMENT_PATH + "image-attachment.png";
	private static final String IMAGE_FILE_NAME = "tempImage.png";

	private StandaloneContentStoreAttachmentRepository standaloneContentStoreAttachmentRepository;
	@Autowired private ResourceFunctions resourceFunctions;
	@Autowired private ContentStoreMapper contentStoreMapper;

	@Mock private ContentStoreTicketClient ticketClient;
	@Mock private ContentStorePrivateClient privateClient;

	private AutoCloseable mocks;
	private String contentId;
	private String thumbnailBigId;
	private String thumbnailSmallId;

	@BeforeClass public void init() {
		mocks = MockitoAnnotations.openMocks(this);

	}

	@BeforeMethod void initData() {
		contentId = UUID.randomUUID().toString();
		thumbnailBigId = UUID.randomUUID().toString();
		thumbnailSmallId = UUID.randomUUID().toString();
		reset(privateClient);
		standaloneContentStoreAttachmentRepository = new StandaloneContentStoreAttachmentRepository(
			ticketClient, privateClient, contentStoreMapper
		);
	}

	@SneakyThrows
	@AfterClass public void tearDown() {
		mocks.close();
	}

	@Test public void testCreateWithoutFilename_shouldSuccess_whenGivenInputStreamAsAttachmentPublic() throws IOException {
		ContentPersistenceResult response = new ContentPersistenceResult();
		response.setContentId(contentId);
		response.setContentType(MediaType.IMAGE_PNG_VALUE);
		response.setSize(new Random().nextInt());
		response.setUrl(Optional.of(CS_DOWNLOAD_URL + contentId));
		when(privateClient.uploadContent(ArgumentMatchers.any(), eq(contentId), eq(AttachmentConstants.PUBLIC_TYPE), eq(IMAGE_FILE_NAME),
			ArgumentMatchers.any()))
			.thenReturn(response);
		AttachmentPersistenceResult attachmentPersistenceResult = standaloneContentStoreAttachmentRepository.create(contentId,
			resourceFunctions.loadResourceAsStream(FULL_IMAGE_PATH), IMAGE_FILE_NAME, TypeOfTheContent.ATTACHMENT_PUBLIC, null);
		Assert.assertEquals(attachmentPersistenceResult.getAttachmentId(), contentId);
		Assert.assertEquals(attachmentPersistenceResult.getMimeType(), MediaType.IMAGE_PNG_VALUE);
		Assert.assertEquals(attachmentPersistenceResult.getSize(), response.getSize());
		Assert.assertTrue(attachmentPersistenceResult.getUrl().isPresent());
		Assert.assertTrue(PUBLIC_URL_PATTERN.test(attachmentPersistenceResult.getUrl().get()));
	}

	@Test public void testCreate_shouldSuccess_whenGivenInputStreamAsAttachmentSecured() throws IOException {
		ContentPersistenceResult response = new ContentPersistenceResult();
		response.setContentId(contentId);
		response.setContentType(MediaType.IMAGE_PNG_VALUE);
		response.setSize(new Random().nextInt());
		when(privateClient.uploadContent(ArgumentMatchers.any(), eq(contentId), eq(AttachmentConstants.PRIVATE_TYPE), eq(IMAGE_FILE_NAME),
			ArgumentMatchers.any()))
			.thenReturn(response);
		AttachmentPersistenceResult attachmentPersistenceResult = standaloneContentStoreAttachmentRepository.create(contentId,
			resourceFunctions.loadResourceAsStream(FULL_IMAGE_PATH), IMAGE_FILE_NAME, TypeOfTheContent.ATTACHMENT_SECURED, null);
		Assert.assertEquals(attachmentPersistenceResult.getAttachmentId(), contentId);
		Assert.assertEquals(attachmentPersistenceResult.getMimeType(), MediaType.IMAGE_PNG_VALUE);
		Assert.assertEquals(attachmentPersistenceResult.getSize(), response.getSize());
		Assert.assertTrue(attachmentPersistenceResult.getUrl().isEmpty());
	}

	@Test(expectedExceptions = ContentStoreClientException.class, expectedExceptionsMessageRegExp = "Cannot connect to content store server")
	public void testCreate_throwServerError_whenHasProblem() throws IOException {
		when(
			privateClient.uploadContent(
				ArgumentMatchers.any(),
				eq(contentId),
				ArgumentMatchers.any(),
				ArgumentMatchers.any(),
				ArgumentMatchers.any())
		).thenThrow(new ResourceAccessException("Cannot connect"));
		standaloneContentStoreAttachmentRepository.create(contentId,
			resourceFunctions.loadResourceAsStream(FULL_IMAGE_PATH), IMAGE_FILE_NAME, TypeOfTheContent.ATTACHMENT_SECURED, null);
	}

	@Test(expectedExceptions = ContentStoreClientException.class, expectedExceptionsMessageRegExp = "Unable to reach content store server")
	public void testCreate_throwNormalMessage_whenHasProblem() throws IOException {
		when(
			privateClient.uploadContent(
				ArgumentMatchers.any(),
				eq(contentId),
				ArgumentMatchers.any(),
				ArgumentMatchers.any(),
				ArgumentMatchers.any()
			)
		).thenThrow(new BadRequestException("bad request", null, null, null, null));
		standaloneContentStoreAttachmentRepository.create(contentId,
			resourceFunctions.loadResourceAsStream(FULL_IMAGE_PATH), IMAGE_FILE_NAME, TypeOfTheContent.ATTACHMENT_SECURED, null);
	}

	@Test public void testCreate_throwExactMessage_whenHasLocalizedMessage() throws IOException {
		String errorKey = "error.content-store.content.invalidSize";
		String longMessage = "Long localization message";
		String shortMessage = "Short localization message";
		when(
			privateClient.uploadContent(
				ArgumentMatchers.any(),
				eq(contentId),
				ArgumentMatchers.any(),
				ArgumentMatchers.any(),
				ArgumentMatchers.any())
		).thenThrow(new BadRequestException(
			"Bad Request",
			new RestErrorDetail(HttpStatus.BAD_REQUEST.value(), "Content size cannot exceed 10 Mb"),
			new LocalizedEntry(errorKey, longMessage),
			new LocalizedEntry(errorKey, shortMessage),
			null
		));
		ContentStoreClientException exception = null;
		try {
			standaloneContentStoreAttachmentRepository.create(contentId,
				resourceFunctions.loadResourceAsStream(FULL_IMAGE_PATH), IMAGE_FILE_NAME, TypeOfTheContent.ATTACHMENT_SECURED, null);
		} catch (ContentStoreClientException t) {
			exception = t;
		}

		Assert.assertNotNull(exception);
		Assert.assertEquals(exception.getStatusCode(), 400);
		Assert.assertEquals(exception.getCode(), ExceptionCodes.CONTENT_STORE_CLIENT_EXCEPTION_CODE);
		Assert.assertEquals(exception.getLongMessage().getDefaultMessage(), longMessage);
		Assert.assertEquals(exception.getLongMessage().getKey(), errorKey);
		Assert.assertEquals(exception.getShortMessage().getDefaultMessage(), shortMessage);
		Assert.assertEquals(exception.getShortMessage().getKey(), errorKey);
		Assert.assertEquals(exception.getMessage(), "Unable to reach content store server");

	}

	@Test public void testCreate_shouldSuccess_whenGivenTextInputStreamAsAttachmentThumbnail() {
		String url = RandomStringUtils.random(20, true, true);
		ContentPersistenceResult persistenceResponse = new ContentPersistenceResult();
		persistenceResponse.setContentId(thumbnailBigId);
		persistenceResponse.setContentType(MediaType.TEXT_PLAIN_VALUE);
		persistenceResponse.setSize(new Random().nextInt());
		persistenceResponse.setUrl(Optional.of(url));

		when(privateClient.uploadContent(ArgumentMatchers.any(), eq(thumbnailBigId), eq(AttachmentConstants.PUBLIC_TYPE),
			eq(thumbnailBigId), eq(ThumbnailUtil.getImageMimeType())))
			.thenReturn(persistenceResponse);

		AttachmentPersistenceResult actualResult =
			standaloneContentStoreAttachmentRepository.create(thumbnailBigId, new ByteArrayInputStream(CONTENT.getBytes()),
				IMAGE_FILE_NAME, TypeOfTheContent.ATTACHMENT_THUMBNAIL, null);

		Assert.assertNotNull(actualResult);
		Assert.assertEquals(actualResult.getAttachmentId(), thumbnailBigId);
		Assert.assertEquals(actualResult.getMimeType(), MediaType.TEXT_PLAIN_VALUE);
		Assert.assertEquals(actualResult.getSize(), persistenceResponse.getSize());
		Assert.assertTrue(actualResult.getUrl().isPresent());
		Assert.assertEquals(actualResult.getUrl().get(), url);
	}

	@Test public void testFindUrl_shouldReturnAttachmentUrl_whenGivenExistingID() {
		String imageName = RandomStringUtils.random(10, true, true);
		String url = CS_DOWNLOAD_URL + contentId;
		DownloadUrlResponse downloadUrlResponse = new DownloadUrlResponse();
		downloadUrlResponse.setUrl(url);
		when(ticketClient.requestTicket(contentId)).thenReturn(downloadUrlResponse);
		Optional<AttachmentUrl> result = standaloneContentStoreAttachmentRepository.findUrl(contentId, imageName, TypeOfTheContent.ATTACHMENT_SECURED);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get().getLocation(), String.format(CS_DOWNLOAD_URL + "%s?filename=%s", contentId, imageName));
	}

	@Test(expectedExceptions = ContentStoreClientException.class, expectedExceptionsMessageRegExp = "Cannot connect to content store server")
	public void testFindUrl_throwServerError_whenCanNotConnectToCS() {
		String imageName = RandomStringUtils.random(10, true, true);
		when(ticketClient.requestTicket(contentId)).thenThrow(new ResourceAccessException("Cannot connect"));
		standaloneContentStoreAttachmentRepository.findUrl(contentId, imageName, TypeOfTheContent.ATTACHMENT_SECURED);
	}

	@Test public void testFindUrl_returnEmpty_whenCanNotGetUrl() {
		String imageName = "downloadImage.png";
		when(ticketClient.requestTicket(contentId)).thenThrow(new BadRequestException("bad request", null, null, null, null));
		Optional<AttachmentUrl> result = standaloneContentStoreAttachmentRepository.findUrl(contentId,
			imageName, TypeOfTheContent.ATTACHMENT_SECURED);
		Assert.assertTrue(result.isEmpty());
	}

	@Test public void testFindUrl_shouldReturnBigThumbnail_whenGivenExistingID() {
		String url = CS_DOWNLOAD_URL + thumbnailBigId;
		DownloadUrlResponse downloadUrlResponse = new DownloadUrlResponse();
		downloadUrlResponse.setUrl(url);

		when(privateClient.getDownloadUrl(thumbnailBigId)).thenReturn(downloadUrlResponse);
		Optional<AttachmentUrl> result = standaloneContentStoreAttachmentRepository.findUrl(thumbnailBigId,
			IMAGE_FILE_NAME, TypeOfTheContent.ATTACHMENT_THUMBNAIL);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get().getLocation(), url);
	}

	@Test public void testFindUrl_shouldReturnSmallThumbnail_whenGivenExistingID() {
		String url = CS_DOWNLOAD_URL + thumbnailSmallId;
		DownloadUrlResponse downloadUrlResponse = new DownloadUrlResponse();
		downloadUrlResponse.setUrl(url);

		when(privateClient.getDownloadUrl(thumbnailSmallId)).thenReturn(downloadUrlResponse);
		Optional<AttachmentUrl> result = standaloneContentStoreAttachmentRepository.findUrl(thumbnailSmallId,
			IMAGE_FILE_NAME, TypeOfTheContent.ATTACHMENT_THUMBNAIL);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get().getLocation(), url);
	}

	@Test public void testFindThumbnailUrl_shouldThrowException_whenAttachmentDontHaveThumbnail() {
		NullPointerException ex = expectThrows(NullPointerException.class, () ->
			standaloneContentStoreAttachmentRepository.findUrl(null,
				SECURED_ATTACHMENT_ID, TypeOfTheContent.ATTACHMENT_THUMBNAIL)
		);
		assertNotNull(ex);
		assertEquals(ex.getMessage(), "id is marked non-null but is null");
	}

	@Test public void testDelete_success_whenCallToCS() {
		doNothing().when(privateClient).deleteContent(contentId);
		standaloneContentStoreAttachmentRepository.delete(contentId);
	}

}
