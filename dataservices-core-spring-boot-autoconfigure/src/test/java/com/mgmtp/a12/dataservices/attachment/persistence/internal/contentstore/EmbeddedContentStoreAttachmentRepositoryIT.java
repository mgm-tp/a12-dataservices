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
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.EmbeddedPostgresInitializer;
import com.mgmtp.a12.dataservices.ResourceFunctions;
import com.mgmtp.a12.dataservices.attachment.AttachmentUrl;
import com.mgmtp.a12.dataservices.attachment.TypeOfTheContent;
import com.mgmtp.a12.dataservices.attachment.persitence.AttachmentPersistenceResult;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.contentstore.EmbeddedContentStoreAttachmentRepository;
import com.mgmtp.a12.dataservices.constants.PathConstants;

import static org.testng.Assert.assertTrue;

@ContextConfiguration(initializers = EmbeddedPostgresInitializer.class)
@TestPropertySource(properties = {
	"spring.datasources.dataservices.embedded-postgres.enabled=true",
	"spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate",
	"spring.datasources.contentstore.embedded-postgres.enabled=true"
})
@SpringBootTest(properties = { "mgmtp.a12.dataservices.attachments.ext.contentstore.embedded.enabled=true" })
public class EmbeddedContentStoreAttachmentRepositoryIT extends AbstractSpringContextIT {

	private static final String IMAGE_ATTACHMENT_PNG_FILE = "image-attachment.png";

	@Autowired private EmbeddedContentStoreAttachmentRepository attachmentRepository;
	@Autowired private ResourceFunctions resourceFunctions;

	private String thumbnailBigId;

	@Test
	public void testCreate_shouldSuccess_whenGivenPngInputStreamAsAttachmentSecured() throws IOException {
		AttachmentPersistenceResult actualResult = persistSecuredAttachment();
		Assert.assertNotNull(actualResult);
		Assert.assertEquals(actualResult.getMimeType(), MediaType.IMAGE_PNG_VALUE);
		Assert.assertEquals(actualResult.getAttachmentId(), SECURED_ATTACHMENT_ID);
		Assert.assertTrue(actualResult.getSize() > 0);
		Assert.assertTrue(actualResult.getUrl().isEmpty());
	}

	@Test
	public void testCreate_shouldSuccess_whenGivenTextInputStreamAsAttachmentPublic() throws IOException {
		AttachmentPersistenceResult actualResult = persistPublicAttachment();
		Assert.assertNotNull(actualResult);
		Assert.assertEquals(actualResult.getMimeType(), MediaType.IMAGE_PNG_VALUE);
		Assert.assertEquals(actualResult.getAttachmentId(), PUBLIC_ATTACHMENT_ID);
		Assert.assertTrue(actualResult.getSize() > 0);
		Assert.assertTrue(actualResult.getUrl().isPresent());
		Assert.assertTrue(PUBLIC_URL_PATTERN.test(actualResult.getUrl().get()));
	}

	@Test
	public void testSaveThumbnail_shouldSuccess_whenGivenTextInputStreamAsPublic() {
		AttachmentPersistenceResult actualResult = persistThumbnail();
		Assert.assertNotNull(actualResult);
		Assert.assertNotNull(actualResult.getAttachmentId());
		Assert.assertEquals(actualResult.getMimeType(), MediaType.TEXT_PLAIN_VALUE);
		Assert.assertTrue(actualResult.getSize() > 0);
		Assert.assertTrue(actualResult.getUrl().isPresent());
		assertTrue(PUBLIC_URL_PATTERN.test(actualResult.getUrl().get()));
	}

	@Test(dependsOnMethods = { "testCreate_shouldSuccess_whenGivenPngInputStreamAsAttachmentSecured" })
	public void testFindUrl_shouldReturnAttachmentUrl_whenGivenExistingID() {
		Optional<AttachmentUrl> actualAttachmentUrl = attachmentRepository.findUrl(SECURED_ATTACHMENT_ID, FILENAME, TypeOfTheContent.ATTACHMENT_SECURED);
		Assert.assertTrue(actualAttachmentUrl.isPresent());
		Assert.assertTrue(StringUtils.isNotBlank(actualAttachmentUrl.get().getLocation()));
	}

	@Test(dependsOnMethods = { "testSaveThumbnail_shouldSuccess_whenGivenTextInputStreamAsPublic" })
	public void testFindUrl_shouldReturnThumbnailUrl_whenGivenExistingID() {
		Optional<AttachmentUrl> actualThumbnailUrl = attachmentRepository.findUrl(thumbnailBigId, FILENAME,
			TypeOfTheContent.ATTACHMENT_THUMBNAIL);
		assertTrue(actualThumbnailUrl.isPresent());
		Assert.assertTrue(StringUtils.isNotBlank(actualThumbnailUrl.get().getLocation()));
		Assert.assertTrue(PUBLIC_URL_PATTERN.test(actualThumbnailUrl.get().getLocation()));
	}

	@Test(dependsOnMethods = { "testCreate_shouldSuccess_whenGivenPngInputStreamAsAttachmentSecured",
		"testFindUrl_shouldReturnAttachmentUrl_whenGivenExistingID", "testDeleteThumbnail_shouldSuccess_whenGivenExistingID" })
	public void testDelete_shouldSuccess_whenGivenExistingID() {
		attachmentRepository.delete(SECURED_ATTACHMENT_ID);
		Optional<AttachmentUrl> actualContentUrl = attachmentRepository.findUrl(SECURED_ATTACHMENT_ID, FILENAME, TypeOfTheContent.ATTACHMENT_SECURED);
		Assert.assertTrue(actualContentUrl.isEmpty());
	}

	@Test(dependsOnMethods = { "testSaveThumbnail_shouldSuccess_whenGivenTextInputStreamAsPublic",
		"testFindUrl_shouldReturnThumbnailUrl_whenGivenExistingID" })
	public void testDeleteThumbnail_shouldSuccess_whenGivenExistingID() {
		attachmentRepository.delete(thumbnailBigId);
		Optional<AttachmentUrl> actualThumbnailUrl = attachmentRepository.findUrl(thumbnailBigId, FILENAME, TypeOfTheContent.ATTACHMENT_THUMBNAIL);
		assertTrue(actualThumbnailUrl.isEmpty());
	}

	private AttachmentPersistenceResult persistSecuredAttachment() throws IOException {
		return attachmentRepository.create(SECURED_ATTACHMENT_ID,
			resourceFunctions.loadResourceAsStream(PathConstants.ATTACHMENT_PATH + IMAGE_ATTACHMENT_PNG_FILE),
			IMAGE_ATTACHMENT_PNG_FILE, TypeOfTheContent.ATTACHMENT_SECURED, null);
	}

	private AttachmentPersistenceResult persistPublicAttachment() throws IOException {
		return attachmentRepository.create(PUBLIC_ATTACHMENT_ID,
			resourceFunctions.loadResourceAsStream(PathConstants.ATTACHMENT_PATH + IMAGE_ATTACHMENT_PNG_FILE),
			IMAGE_ATTACHMENT_PNG_FILE, TypeOfTheContent.ATTACHMENT_PUBLIC, null);
	}

	private AttachmentPersistenceResult persistThumbnail() {
		String contenttId = UUID.randomUUID().toString();
		AttachmentPersistenceResult actualThumbnailResult =
			attachmentRepository.create(contenttId, new ByteArrayInputStream(CONTENT.getBytes()), contenttId,
				TypeOfTheContent.ATTACHMENT_THUMBNAIL, null);
		thumbnailBigId = contenttId;
		return actualThumbnailResult;
	}
}
