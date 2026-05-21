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
package com.mgmtp.a12.examples.document.staticcode;

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import java.util.Optional;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

@Listeners(MockitoTestNGListener.class)
public class ExampleDocumentStaticServiceConfigTest {

	@Test
	public void getVariant_noVariant_empty() {
		ExampleDocumentStaticServiceConfig config = new ExampleDocumentStaticServiceConfig();
		assertFalse(config.getVariant().isPresent(), "Variant should be empty");
	}

	@Test
	public void getLabelProvider_noProvider_empty() {
		ExampleDocumentStaticServiceConfig config = new ExampleDocumentStaticServiceConfig();
		assertFalse(config.getLabelProvider().isPresent(), "LabelProvider should be empty");
	}

	@Test
	public void getModelPackage_validModel_returnsPackage() {
		ExampleDocumentStaticServiceConfig config = new ExampleDocumentStaticServiceConfig();
		String documentModelId = "testModel";
		Optional<String> result = config.getModelPackage(documentModelId);
		assertTrue(result.isPresent(), "Model package should be present");
		assertEquals(result.get(), "com.mgmtp.a12.kernel.generated.testModel");
	}

	@Test
	public void getCache_noCache_empty() {
		ExampleDocumentStaticServiceConfig config = new ExampleDocumentStaticServiceConfig();
		assertFalse(config.getCache().isPresent(), "Cache should be empty");
	}

	@Test
	public void getModelCodeClassLoader_noLoader_empty() {
		ExampleDocumentStaticServiceConfig config = new ExampleDocumentStaticServiceConfig();
		assertFalse(config.getModelCodeClassLoader("anyId").isPresent(), "ModelCodeClassLoader should be empty");
	}
}
