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
package com.mgmtp.a12.examples.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MimeTypeUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.contentstore.service.ContentStoreService;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentService;
import com.mgmtp.a12.dataservices.attachment.ThumbnailType;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.ThumbnailUtil;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.examples.AbstractITBase;
import com.mgmtp.a12.examples.attachment.thumbnails.CustomThumbnailListener;
import com.mgmtp.a12.examples.extra.ExtraEntity;
import com.mgmtp.a12.examples.extra.ExtraEntityRepository;
import com.mgmtp.a12.examples.util.ResourceUtil;

@ActiveProfiles({ "dataservices-example-common", "dataservices-example-attachments-audit", "dataservices-example-attachments-thumbnail-custom" })
public class CustomThumbnailListenerIT extends AbstractITBase {

	public static final String ATTACHMENT_PDF = "attachment.pdf";
	public static final String ATTACHMENT_PNG = "attachment.png";
	public static final String ATTACHMENT_JPG = "attachment.jpg";
	public static final String APPLICATION_PDF_MIMETYPE = "application/pdf";
	public static final String BIG_THUMB_PNG = "big_thumb.png";
	public static final String SMALL_THUMB_PNG = "small_thumb.png";

	@Autowired private AttachmentService attachmentService;
	@Autowired private ContentStoreService contentStoreService;
	@Autowired private DataServicesCoreProperties properties;

	@Autowired private ResourceUtil resourceUtil;

	@Autowired private ExtraEntityRepository repository;

	@DataProvider
	public Object[][] customUploadThumbnails() {
		return new Object[][] {
			{ ATTACHMENT_PDF, APPLICATION_PDF_MIMETYPE, BIG_THUMB_PNG, ThumbnailType.BIG },
			{ ATTACHMENT_PDF, APPLICATION_PDF_MIMETYPE, SMALL_THUMB_PNG, ThumbnailType.SMALL },
			{ ATTACHMENT_PNG, MimeTypeUtils.IMAGE_PNG_VALUE, BIG_THUMB_PNG, ThumbnailType.BIG },
			{ ATTACHMENT_PNG, MimeTypeUtils.IMAGE_PNG_VALUE, SMALL_THUMB_PNG, ThumbnailType.SMALL }
		};
	}

	@Test(dataProvider = "customUploadThumbnails")
	public void testCustomUploadAttachmentThumbnail(String attachmentName, String attachmentMimeType, String expectedThumbnailName,
		ThumbnailType thumbnailType) {
		AttachmentHeader header = createAttachment(attachmentName, ATTACHMENT_UPLOAD_PATH + attachmentName);
		Assert.assertTrue(attachmentService.findThumbnailUrl(header.getAttachmentId(), thumbnailType).isPresent());
	}

	@DataProvider
	public Object[][] normalAttachmentThumbnails() {
		return new Object[][] {
			{ ATTACHMENT_JPG, ThumbnailType.BIG, properties.getAttachments().getThumbnail().getSizeBig() },
			{ ATTACHMENT_JPG, ThumbnailType.SMALL, properties.getAttachments().getThumbnail().getSizeSmall() }
		};
	}

	@Test(dataProvider = "normalAttachmentThumbnails")
	public void testNormalUploadAttachmentThumbnail(String attachmentName, ThumbnailType thumbnailType, int expectedSize) {
		AttachmentHeader attachmentHeader = createAttachment(attachmentName, ATTACHMENT_UPLOAD_PATH + attachmentName);

		ThumbnailUtil.convertToDSThumbnail(
				resourceUtil.getInputStream(ATTACHMENT_UPLOAD_PATH + attachmentName),
				thumbnailType,
				expectedSize,
				properties.getAttachments().getThumbnail())
			.ifPresentOrElse(expectedDSThumbnail -> {
				InputStream thumbnailInputStream = null;
				try {
					if (thumbnailType.equals(ThumbnailType.BIG)) {
						thumbnailInputStream = contentStoreService.getContent(attachmentHeader.getThumbnailBigId()).getContentSupplier().get();
					} else if (thumbnailType.equals(ThumbnailType.SMALL)) {
						thumbnailInputStream = contentStoreService.getContent(attachmentHeader.getThumbnailSmallId()).getContentSupplier().get();
					}
					Assert.assertTrue(IOUtils.contentEquals(thumbnailInputStream, expectedDSThumbnail.getContent().get()));
				} catch (IOException e) {
					Assert.fail("Expected thumbnail file does not exist");
				}
			}, () -> Assert.fail("Expected thumbnail does not exist"));
	}

	@Test
	public void testUploadAttachment_shouldCreateExtraEntity_whenHandleAttachmentBeforeCreateEvent() {
		AttachmentHeader attachmentHeader = createAttachment(ATTACHMENT_JPG, ATTACHMENT_UPLOAD_PATH + ATTACHMENT_JPG);

		Optional<ExtraEntity> extraEntity = repository.findById(attachmentHeader.getAttachmentId());
		Assert.assertTrue(extraEntity.isPresent());
		Assert.assertEquals(extraEntity.get().getId(), attachmentHeader.getAttachmentId());
		Assert.assertEquals(extraEntity.get().getText(), ATTACHMENT_JPG);
	}

	@Test
	public void testDefaultAttachmentService_shouldNotSetWorkaroundProperty_whenRunDefaultConfiguration() {
		Assert.assertFalse(Boolean.getBoolean("thumbnailator.conserveMemoryWorkaround"));
		Assert.assertFalse(ImageIO.getUseCache());
	}

	@Test(expectedExceptions = UnexpectedException.class, expectedExceptionsMessageRegExp = "Throwing exception for rolling back persisted attachment from content store")
	public void testForbiddenAttachmentThrowsAnException() {
		attachmentService.createAttachment(
			resourceUtil.getInputStream(ATTACHMENT_UPLOAD_PATH + ATTACHMENT_JPG),
			CustomThumbnailListener.ROLLBACK_ATTACHMENT_FILE_NAME,
			AbstractITBase.BUSINESS_PARTNER,
			"/BusinessPartnerRoot/Attachment",
			Collections.emptyList());
	}

	private AttachmentHeader createAttachment(String attachmentName, String location) {
		return attachmentService.createAttachment(
			resourceUtil.getInputStream(location),
			attachmentName,
			AbstractITBase.BUSINESS_PARTNER,
			RandomStringUtils.randomAlphabetic(10),
			Collections.emptyList());
	}
}
