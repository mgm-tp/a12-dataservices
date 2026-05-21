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
package com.mgmtp.a12.dataservices.server.util;

import java.util.Arrays;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.User;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.initialization.events.DataServicesDocumentModelCachesPreloadedEvent;
import com.mgmtp.a12.dataservices.initialization.events.DataServicesInitializationFinishedEvent;
import com.mgmtp.a12.dataservices.model.document.internal.ValidationCodeGenerator;
import com.mgmtp.a12.dataservices.model.document.persistence.DocumentModelReadRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.server.AbstractDataServiceServerTest;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelWrapper;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentRtService;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.uaa.authentication.backend.AuthenticatedUserLoader;
import com.mgmtp.a12.uaa.authentication.backend.BackendAuthenticationService;

import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class KernelCachesPreloaderTest extends AbstractDataServiceServerTest {

	@Mock private ApplicationEventPublisher eventPublisher;
	@Mock private IDocumentRtService rtService;
	@Mock private ValidationCodeGenerator validationCodeGenerator;
	@Mock private ModelHeaderJpaRepository modelHeaderRepository;
	@Mock private DocumentModelReadRepository documentModelReadRepository;
	@Spy private DataServicesCoreProperties dataServicesCoreProperties = new DataServicesCoreProperties();
	@Mock private AuthenticatedUserLoader backendUserLoader;
	@InjectMocks private BackendAuthenticationService backendAuthenticationService = spy(new BackendAuthenticationService(true));
	@InjectMocks private KernelCachesPreloader kernelCachesPreloader;

	@Test void testListenOnServicesInitializationFinished() {
		String username = RandomStringUtils.randomAlphabetic(10);
		dataServicesCoreProperties.getAuthorization().getBackendJob().getPrincipal().setUsername(username);

		Header header = mockModelHeader();
		DocumentModel model = new DocumentModel();
		model.setHeader(header);
		DocumentModelWrapper documentModelWrapper = new DocumentModelWrapper(model);

		when(modelHeaderRepository.findByModelType(DOCUMENT_MODEL_TYPE)).thenReturn(Arrays.asList(header));
		when(backendUserLoader.loadUser(username)).thenReturn(User.builder().username(username).password("").build());
		when(documentModelReadRepository.readModel(header.getId())).thenReturn(documentModelWrapper);

		kernelCachesPreloader.listenOnServicesInitializationFinished(new DataServicesInitializationFinishedEvent());

		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(ArgumentMatchers.any(DataServicesDocumentModelCachesPreloadedEvent.class));
	}
}
