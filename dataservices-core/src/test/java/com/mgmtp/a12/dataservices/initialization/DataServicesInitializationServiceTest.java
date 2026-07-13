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

import java.util.Optional;

import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.security.core.userdetails.User;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.initialization.events.DataServicesCustomInitializationEvent;
import com.mgmtp.a12.dataservices.initialization.internal.BusinessModelInitializer;
import com.mgmtp.a12.dataservices.initialization.internal.DataServicesInitializationService;
import com.mgmtp.a12.dataservices.initialization.internal.JsonRpcInitializer;
import com.mgmtp.a12.dataservices.query.indexing.QueryIndexManager;
import com.mgmtp.a12.dataservices.migration.internal.MigrationRunner;
import com.mgmtp.a12.dataservices.rpc.internal.RequestIdService;
import com.mgmtp.a12.uaa.authentication.backend.AuthenticatedUserLoader;
import com.mgmtp.a12.uaa.authentication.backend.BackendAuthenticationService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataServicesInitializationServiceTest extends AbstractDataServicesCoreTest {

	@Mock private MigrationRunner migrationRunner;
	@Mock private BusinessModelInitializer businessModelInitializer;
	@Mock private AuthenticatedUserLoader backendUserLoader;
	@Mock private RequestIdService requestIdService;
	@Mock private QueryIndexManager queryIndexManager;
	@Spy Optional<JsonRpcInitializer> jsonRpcInitializer = Optional.empty();

	@InjectMocks
	private BackendAuthenticationService backendAuthenticationService = spy(new BackendAuthenticationService(true));

	@InjectMocks
	@Spy private DataServicesInitializationService dataServicesInitializationService;

	@BeforeMethod
	public void setUp() {
		when(backendUserLoader.loadUser(anyString())).thenAnswer(a -> User.builder().username(a.getArgument(0)).password("").build());

		String userName = UserConstants.ADMIN_USER;
		dataServicesCoreProperties.getAuthorization().getBackendJob().getPrincipal().setUsername(userName);
		dataServicesCoreProperties.getInitialization().getImport().getModels().setEnabled(true);
		dataServicesCoreProperties.getInitialization().getCleanUpRequestId().setEnabled(true);
		setCurrentUser(userName);
	}

	@Test void testRunInitialization() throws Exception {

		dataServicesInitializationService.runInitialization();

		verify(requestIdService, times(1)).cleanRequestIds();
		verify(businessModelInitializer, times(1)).importBusinessModels();
		verify(migrationRunner, times(1)).migrate();

		verify(eventPublisher, times(1)).publishEvent(ArgumentMatchers.any(DataServicesCustomInitializationEvent.class));
	}
}
