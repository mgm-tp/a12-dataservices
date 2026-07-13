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
package com.mgmtp.a12.dataservices.model.internal;

import java.util.Optional;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.kernel.md.rt.api.IDocumentModelIdProvider;
import com.mgmtp.a12.kernel.md.rt.api.IModelCodeCache;

@Listeners(MockitoTestNGListener.class)
public class DataServicesDocumentDynamicServiceConfigTest {

	@Mock private IModelCodeCache modelCodeCache;

	private DataServicesDocumentDynamicServiceConfig config;

	@BeforeMethod
	public void setUp() {
		config = new DataServicesDocumentDynamicServiceConfig(modelCodeCache);
	}

	@Test(description = "Should return the injected model code cache")
	public void shouldReturnInjectedModelCodeCache() {
		Assert.assertEquals(config.getCache(), modelCodeCache);
	}

	@Test(description = "Should return empty variant")
	public void shouldReturnEmptyVariant() {
		Optional<String> variant = config.getVariant();

		Assert.assertTrue(variant.isEmpty());
	}

	@Test(description = "Should return empty label provider")
	public void shouldReturnEmptyLabelProvider() {
		Assert.assertTrue(config.getLabelProvider().isEmpty());
	}

	@Test(description = "Should return model ID provider that is present")
	public void shouldReturnModelIdProviderThatIsPresent() {
		Optional<IDocumentModelIdProvider> modelIdProviderOpt = config.getModelIdProvider();

		Assert.assertTrue(modelIdProviderOpt.isPresent());
		Assert.assertNotNull(modelIdProviderOpt.get());
	}
}
