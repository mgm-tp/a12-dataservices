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
package com.mgmtp.a12.dataservices.initialization;

import java.io.IOException;

import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextRefreshedEvent;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.initialization.events.DataServicesInitializationFinishedEvent;
import com.mgmtp.a12.dataservices.initialization.internal.DataServicesInitializationService;

public class DataServicesInitializationListenerTest extends AbstractDataServicesCoreTest {
	@Mock private ApplicationContext applicationContext;
	@Mock private ApplicationEventPublisher applicationEventPublisher;
	@Mock private DataServicesInitializationService initializationService;
	private DataServicesInitializationListener dataServicesInitializationListener;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.openMocks(this);
		dataServicesInitializationListener =
			new DataServicesInitializationListener(applicationContext, applicationEventPublisher, initializationService);
	}
	@Test void testOnApplicationInitialization() throws IOException {
		dataServicesInitializationListener.onApplicationInitialization(new ContextRefreshedEvent(applicationContext));

		Mockito.verify(initializationService, Mockito.times(1)).runInitialization();
		Mockito.verify(applicationEventPublisher, Mockito.times(1)).publishEvent(ArgumentMatchers.any(DataServicesInitializationFinishedEvent.class));

	}
}
