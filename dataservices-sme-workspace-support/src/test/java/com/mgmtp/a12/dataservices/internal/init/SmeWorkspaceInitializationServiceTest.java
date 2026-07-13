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
package com.mgmtp.a12.dataservices.internal.init;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.internal.configuration.SmeWorkspaceProperties;
import com.mgmtp.a12.dataservices.internal.service.SmeWorkspaceImportService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;

@Listeners(MockitoTestNGListener.class)
public class SmeWorkspaceInitializationServiceTest {

	@Spy private SmeWorkspaceProperties workspaceProperties = Mockito.spy(SmeWorkspaceProperties.class);
	@Mock private SmeWorkspaceImportService workspaceImportService;

	@InjectMocks SmeWorkspaceInitializationService smeWorkspaceInitializationService;

	@Test
	void runInitialization_success() throws IOException {
		Path tempFile = Files.createTempFile("data", ".tgz");

		workspaceProperties.getInitialization().getSmeWorkspaceFile().setPath(tempFile.toString());
		try (MockedStatic<Files> mockedStaticFiles = Mockito.mockStatic(Files.class)) {
			InputStream inputStream = Mockito.mock(InputStream.class);
			mockedStaticFiles.when(() -> Files.newInputStream(any()))
				.thenReturn(inputStream);

			smeWorkspaceInitializationService.runInitialization();

			mockedStaticFiles.verify(
				() -> Files.newInputStream(argThat(path -> path.toString().equals(tempFile.toString()))),
				Mockito.times(1)
			);
			Mockito.verify(workspaceImportService, Mockito.times(1)).deleteAllData();
			Mockito.verify(workspaceImportService, Mockito.times(1)).importData(inputStream);
		}

		FileUtils.deleteQuietly(tempFile.toFile());
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "java.nio.file.NoSuchFileException: /path/var/data.tgz")
	void runInitialization_fileNotFound() {
		workspaceProperties.getInitialization().getSmeWorkspaceFile().setPath("/path/var/data.tgz");

		smeWorkspaceInitializationService.runInitialization();
	}

	@Test(expectedExceptions = UnexpectedException.class, expectedExceptionsMessageRegExp = "Unexpected error")
	void runInitialization_deleteHasError() throws IOException {
		Path tempFile = Files.createTempFile("data", ".tgz");

		workspaceProperties.getInitialization().getSmeWorkspaceFile().setPath(tempFile.toString());
		Mockito.doThrow(new UnexpectedException("Unexpected error")).when(workspaceImportService).deleteAllData();
		smeWorkspaceInitializationService.runInitialization();
	}

	@Test(expectedExceptions = UnexpectedException.class, expectedExceptionsMessageRegExp = "Unexpected error")
	void runInitialization_importHasError() throws IOException {
		Path tempFile = Files.createTempFile("data", ".tgz");

		workspaceProperties.getInitialization().getSmeWorkspaceFile().setPath(tempFile.toString());
		Mockito.doThrow(new UnexpectedException("Unexpected error")).when(workspaceImportService).importData(any());
		smeWorkspaceInitializationService.runInitialization();
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "java.io.IOException: Unexpected error")
	void runInitialization_importHasIOError() throws IOException {
		Path tempFile = Files.createTempFile("data", ".tgz");

		workspaceProperties.getInitialization().getSmeWorkspaceFile().setPath(tempFile.toString());
		Mockito.doThrow(new IOException("Unexpected error")).when(workspaceImportService).importData(any());
		smeWorkspaceInitializationService.runInitialization();
	}
}
