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
package com.mgmtp.a12.contentstore.content.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tika.mime.MediaType;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.contentstore.AbstractContentStoreTest;
import com.mgmtp.a12.contentstore.ContentPersistenceResult;
import com.mgmtp.a12.contentstore.configuration.ContentStoreProperties;
import com.mgmtp.a12.contentstore.content.internal.jpa.entity.ContentHeaderEntity;

import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.contentstore.constants.Constants.BASE_URL;
import static com.mgmtp.a12.contentstore.utils.Constants.PERSISTENT_TYPE_PRIVATE;
import static com.mgmtp.a12.contentstore.utils.Constants.PERSISTENT_TYPE_PUBLIC;
import static com.mgmtp.a12.contentstore.utils.internal.UrlUtils.CONTENT_STORE_DOWNLOAD_URL_PATTERN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@Slf4j
public class DefaultContentServiceTest extends AbstractContentStoreTest {

	private String contentId;

	@InjectMocks
	private DefaultContentService contentService;

	@BeforeMethod public void initTestData() {
		contentId = UUID.randomUUID().toString();
	}

	@DataProvider
	public Object[][] persistentTypeData() {
		return new Object[][] {
			new Object[] { PERSISTENT_TYPE_PUBLIC },
			new Object[] { PERSISTENT_TYPE_PRIVATE }
		};
	}

	@DataProvider
	public Object[][] contentSaveData() {
		return new Object[][] {
			new Object[] { PERSISTENT_TYPE_PUBLIC, true },
			new Object[] { PERSISTENT_TYPE_PRIVATE, false }
		};
	}
	@Test(dataProvider = "contentSaveData")
	public void testSave_shouldBeSuccessful_whenGivenDuplicatedContentId(String persistentType, boolean expectedHasUrl) {
		mockValidContentEntity(contentId, persistentType);

		mockValidContentEntity(contentId, persistentType);

		ContentPersistenceResult result = contentService.save(contentId, persistentType, MediaType.TEXT_PLAIN.toString(), null);

		Assert.assertEquals(result.getContentId(), contentId);
		Assert.assertEquals(result.getContentType(), MediaType.TEXT_PLAIN.toString());
		Assert.assertEquals(expectedHasUrl, result.getUrl().isPresent());
		Assert.assertEquals(result.getSize(), 0);
	}

	@Test(dataProvider = "persistentTypeData")
	public void testSavePrivateContent_shouldPersistContentSuccessfully(String persistentType) {
		String thisContentId = UUID.randomUUID().toString();
		Long size = 100L;
		byte[] content = RandomStringUtils.randomAlphabetic(10).getBytes();
		InputStream inputStream = new ByteArrayInputStream(content);

		Mockito.when(contentRepository.save(any(), eq(inputStream))).thenReturn(size);
		Mockito.when(contentStoreProperties.getBaseUrl()).thenReturn(BASE_URL);
		Mockito.when(contentStoreProperties.getServer()).thenReturn(new ContentStoreProperties.Server());

		ContentPersistenceResult actualResult = contentService.save(thisContentId, persistentType, MediaType.EMPTY.getType(), inputStream);

		Assert.assertNotNull(actualResult);
		Assert.assertEquals(actualResult.getSize(), size);

		if (PERSISTENT_TYPE_PUBLIC.equals(persistentType)) {
			String expectedUrl = String.format(CONTENT_STORE_DOWNLOAD_URL_PATTERN, BASE_URL, contentStoreProperties.getServer().getContextPath(), thisContentId);
			Assert.assertTrue(actualResult.getUrl().isPresent());
			Assert.assertEquals(actualResult.getUrl().get(), expectedUrl);
		} else {
			Assert.assertFalse(actualResult.getUrl().isPresent());
		}
	}

	@DataProvider
	public Object[][] testGetByContentIdAndPersistentTypeData() {
		return new Object[][] {
			new Object[] { PERSISTENT_TYPE_PUBLIC, PERSISTENT_TYPE_PUBLIC, true },
			new Object[] { PERSISTENT_TYPE_PUBLIC, PERSISTENT_TYPE_PRIVATE, false },
			new Object[] { PERSISTENT_TYPE_PRIVATE, PERSISTENT_TYPE_PRIVATE, true },
			new Object[] { PERSISTENT_TYPE_PRIVATE, PERSISTENT_TYPE_PUBLIC, false }
		};
	}

	@Test(dataProvider = "testGetByContentIdAndPersistentTypeData")
	public void testGetByContentIdAndPersistentType_shouldReturnStream_whenGivenCorrectIdAndType(String persistentType, String requestPersistentType,
		boolean expectedResult) {
		mockValidContentEntity(contentId, persistentType);

		Optional<ContentHeaderEntity> contentHeaderEntityOptional = contentService.findByContentIdAndPersistentType(contentId, requestPersistentType);

		Assert.assertEquals(contentHeaderEntityOptional.isPresent(), expectedResult);
	}

	@Test(dataProvider = "persistentTypeData")
	public void testDeleteContentById_shouldSuccess_whenGivenExistingContentForDelete(String persistentType) {
		mockValidContentEntity(contentId, persistentType);
		contentService.deleteContentById(contentId);
	}

	@Test
	public void testExist_callChildrenMethodWithRightData() {
		String persistentType = RandomStringUtils.randomAlphabetic(6);
		Mockito.doReturn(true).when(contentHeaderJpaRepository).existsByIdAndPersistentType(contentId, persistentType);

		boolean result = contentService.exists(contentId, persistentType);

		Assert.assertTrue(result);
	}

	@Test
	public void testFindByIdAndPersistentTypeIgnoreCase_returnExactValue() {
		String persistentType = RandomStringUtils.randomAlphabetic(10);
		ContentHeaderEntity header = new ContentHeaderEntity(contentId, persistentType, RandomStringUtils.randomAlphabetic(10));
		Mockito.doReturn(Optional.of(header)).when(contentHeaderJpaRepository).findByIdAndPersistentTypeIgnoreCase(contentId, persistentType);

		Optional<ContentHeaderEntity> result = contentService.findByContentIdAndPersistentType(contentId, persistentType);

		assertContentHeader(result, header);
	}

	@Test
	public void testFindHeaderById_returnExactValue() {
		String persistentType = RandomStringUtils.randomAlphabetic(10);
		ContentHeaderEntity header = new ContentHeaderEntity(contentId, persistentType, RandomStringUtils.randomAlphabetic(10));
		Mockito.doReturn(Optional.of(header)).when(contentHeaderJpaRepository).findById(contentId);

		Optional<ContentHeaderEntity> result = contentService.findHeaderById(contentId);

		assertContentHeader(result, header);
	}

	private void assertContentHeader(Optional<ContentHeaderEntity> result, ContentHeaderEntity header) {
		Assert.assertTrue(result.isPresent());
		ContentHeaderEntity actualHeader = result.get();
		Assert.assertEquals(actualHeader, header);
		Assert.assertEquals(actualHeader.getId(), header.getId());
		Assert.assertEquals(actualHeader.getPersistentType(), header.getPersistentType());
		Assert.assertEquals(actualHeader.getContentType(), header.getContentType());
	}
}
