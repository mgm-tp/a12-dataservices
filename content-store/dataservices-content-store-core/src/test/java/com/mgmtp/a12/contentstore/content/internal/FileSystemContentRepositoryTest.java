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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mgmtp.a12.contentstore.AbstractContentStoreTest;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;

import static com.mgmtp.a12.contentstore.constants.Constants.CONTENT;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

public class FileSystemContentRepositoryTest extends AbstractContentStoreTest {

	private FileSystemContentRepository fileSystemContentRepository;
	private File contentLocation;

	@BeforeClass public void initData() throws IOException {
		contentLocation = Files.createTempDirectory("tmpDirPrefix").toFile();
		fileSystemContentRepository = new FileSystemContentRepository(contentLocation);
	}

	@AfterClass public void clearData() {
		contentLocation.delete();
	}

	@Test
	public void testDelete_callChildrenMethodWithRightData() {
		String contentId = UUID.randomUUID().toString();

		try (MockedStatic<Files> files = Mockito.mockStatic(Files.class)) {

			fileSystemContentRepository.delete(contentId);

			files.verify(() -> Files.deleteIfExists(argThat(path -> path.equals(FileSystemContentUtil.getContentPath(contentLocation, contentId)))));
		}
	}

	@Test()
	public void testDelete_noThrowUnexpectedExceptionWhenHavingError() {
		String contentId = UUID.randomUUID().toString();

		try (MockedStatic<Files> files = Mockito.mockStatic(Files.class)) {
			files.when(() -> Files.delete(FileSystemContentUtil.getContentPath(contentLocation, contentId)))
					.thenThrow(new IOException("Delete message error"));

			fileSystemContentRepository.delete(contentId);
		}
	}

	@Test
	public void testSave_callChildrenMethodWithRightData() {
		String contentId = UUID.randomUUID().toString();
		InputStream inputStream = new ByteArrayInputStream(CONTENT.getBytes());

		try (MockedStatic<Files> files = Mockito.mockStatic(Files.class)) {
			files.when(() -> Files.copy(
					eq(inputStream),
					argThat(path -> path.equals(FileSystemContentUtil.getContentPath(contentLocation, contentId))),
					eq(REPLACE_EXISTING))
			).thenReturn(1000L);

			long size = fileSystemContentRepository.save(contentId, inputStream);

			Assert.assertEquals(size, 1000L);
		}
	}

	@Test(expectedExceptions = UnexpectedException.class, expectedExceptionsMessageRegExp = "Error occurs while trying to persist content with id .* to File System")
	public void testSave_throwUnexpectedExceptionWhenHavingError() {
		String contentId = UUID.randomUUID().toString();
		InputStream inputStream = new ByteArrayInputStream(CONTENT.getBytes());

		try (MockedStatic<Files> files = Mockito.mockStatic(Files.class)) {

			files.when(() -> Files.copy(
							eq(inputStream),
							argThat(path -> path.equals(FileSystemContentUtil.getContentPath(contentLocation, contentId))),
							eq(REPLACE_EXISTING)
					)).thenThrow(new IOException("Save error"))
					.thenThrow(new IOException("Delete message error"));

			fileSystemContentRepository.save(contentId, inputStream);
		}
	}
}
