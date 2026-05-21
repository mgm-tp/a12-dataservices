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

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.contentstore.utils.internal.UrlUtils;
import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.ThumbnailType;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.contentstore.ThumbnailUrlGeneratorImpl;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@SpringBootTest()
public class ThumbnailUrlGeneratorImplTest extends AbstractSpringContextIT {

	@Autowired private ThumbnailUrlGeneratorImpl thumbnailUrlGenerator;
	@MockitoSpyBean private DataServicesCoreProperties dataServicesCoreProperties;

	private static final String GENERATED_BIG_URL = "http://localhost/generatedBigUrl";
	private static final String GENERATED_SMALL_URL = "http://localhost/generatedSmallUrl";
	private static final String BASE_URL = "http://localhost";
	private static final String ATTACHMENT_ID = "attachmentId";
	private static final String BIG_THUMBNAIL_ID = "thumbnailBigId";
	private static final String SMALL_THUMBNAIL_ID = "thumbnailSmallId";

	@Test
	public void testBuildThumbnailUrl_returnNull_WhenNoHaveThumbnail() {
		AttachmentHeader attachmentHeader = AttachmentHeader.builder()
			.attachmentId(ATTACHMENT_ID)
			.build();

		Assert.assertTrue(thumbnailUrlGenerator.generateThumbnailUrl(attachmentHeader, ThumbnailType.SMALL).isEmpty());
		Assert.assertTrue(thumbnailUrlGenerator.generateThumbnailUrl(attachmentHeader, ThumbnailType.BIG).isEmpty());

	}

	@Test
	public void testBuildThumbnailUrl_withDisableAutoBuildUrl() {
		AttachmentHeader attachmentHeader = AttachmentHeader.builder()
			.attachmentId(ATTACHMENT_ID)
			.thumbnailBigId(BIG_THUMBNAIL_ID)
			.thumbnailSmallId(SMALL_THUMBNAIL_ID)
			.build();

		Assert.assertTrue(thumbnailUrlGenerator.generateThumbnailUrl(attachmentHeader, ThumbnailType.BIG).isEmpty());
		Assert.assertTrue(thumbnailUrlGenerator.generateThumbnailUrl(attachmentHeader, ThumbnailType.SMALL).isEmpty());
	}

	@Test
	public void testBuildThumbnailUrl_withEnableAutoBuildUrl() {
		AttachmentHeader attachmentHeader = AttachmentHeader.builder()
			.attachmentId(ATTACHMENT_ID)
			.thumbnailBigId(BIG_THUMBNAIL_ID)
			.thumbnailSmallId(SMALL_THUMBNAIL_ID)
			.build();

		DataServicesCoreProperties.Attachments attachments = Mockito.mock(DataServicesCoreProperties.Attachments.class, Mockito.RETURNS_DEEP_STUBS);
		DataServicesCoreProperties.Attachments.Thumbnail thumbnail =
			Mockito.mock(DataServicesCoreProperties.Attachments.Thumbnail.class, Mockito.RETURNS_DEEP_STUBS);
		DataServicesCoreProperties.Attachments.Thumbnail.Optimization optimization =
			Mockito.mock(DataServicesCoreProperties.Attachments.Thumbnail.Optimization.class, Mockito.RETURNS_DEEP_STUBS);
		DataServicesCoreProperties.Attachments.Thumbnail.Optimization.Url url =
			Mockito.mock(DataServicesCoreProperties.Attachments.Thumbnail.Optimization.Url.class, Mockito.RETURNS_DEEP_STUBS);

		// mock value for enable auto generate thumbnail url.
		doReturn(attachments).when(dataServicesCoreProperties).getAttachments();
		doReturn(thumbnail).when(attachments).getThumbnail();
		doReturn(optimization).when(thumbnail).getOptimization();
		doReturn(url).when(optimization).getUrl();
		doReturn(true).when(url).isEnabled();
		doReturn(BASE_URL).when(optimization).getBaseUrl();

		try (MockedStatic<UrlUtils> ignored = Mockito.mockStatic(UrlUtils.class)) {
			when(UrlUtils.buildContentUrl(BASE_URL, "", BIG_THUMBNAIL_ID)).thenReturn(GENERATED_BIG_URL);
			when(UrlUtils.buildContentUrl(BASE_URL, "", SMALL_THUMBNAIL_ID)).thenReturn(GENERATED_SMALL_URL);
			Assert.assertEquals(thumbnailUrlGenerator.generateThumbnailUrl(attachmentHeader, ThumbnailType.BIG).get(), GENERATED_BIG_URL);
			Assert.assertEquals(thumbnailUrlGenerator.generateThumbnailUrl(attachmentHeader, ThumbnailType.SMALL).get(), GENERATED_SMALL_URL);
		}
	}
}
