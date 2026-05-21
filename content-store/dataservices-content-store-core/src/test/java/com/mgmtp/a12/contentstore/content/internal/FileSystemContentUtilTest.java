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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.logging.log4j.util.PropertiesUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FileSystemContentUtilTest {
	private File contentLocation;

	@BeforeClass public void initData() throws IOException {
		contentLocation = Files.createTempDirectory("tempDir").toFile();
	}

	@AfterClass public void clearData() {
		contentLocation.delete();
	}

	@DataProvider
	public static Object[][] testBuildContentUrlData() {
		return new Object[][] {
			new Object[] { "4a3d26dd-e7db-4e03-b9cc-c0c6cf437d8f", "/4a/3d/26/4a3d26dd-e7db-4e03-b9cc-c0c6cf437d8f" },
			new Object[] { "c0c6c", "/c0c6c/c0c6c" },
			new Object[] { "aaa", "/aaa/aaa" },
		};
	}

	@Test(dataProvider = "testBuildContentUrlData")
	public void testBuildContentUrl(String id, String fullUrl) {
		String fullPath = FileSystemContentUtil.getContentPath(contentLocation, id).toString();

		Assert.assertEquals(fullPath.replace(contentLocation.getAbsolutePath(), ""),
			PropertiesUtil.getProperties().isOsWindows()? fullUrl.replace("/", "\\") : fullUrl);
	}

}
