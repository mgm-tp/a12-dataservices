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
package com.mgmtp.a12.dataservices.server.util.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.User;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.initialization.events.DataServicesDocumentModelCachesPreloadedEvent;
import com.mgmtp.a12.dataservices.initialization.events.DataServicesInitializationFinishedEvent;
import com.mgmtp.a12.dataservices.model.document.IValidationCodeProvider;
import com.mgmtp.a12.dataservices.model.document.persistence.DocumentModelReadRepository;
import com.mgmtp.a12.dataservices.model.internal.IndexedModelFieldCache;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.server.AbstractDataServiceServerTest;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelWrapper;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentRtService;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.uaa.authentication.backend.AuthenticatedUserLoader;
import com.mgmtp.a12.uaa.authentication.backend.BackendAuthenticationService;

import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KernelCachesPreloaderTest extends AbstractDataServiceServerTest {

	@Mock private ApplicationEventPublisher eventPublisher;
	@Mock private IDocumentRtService rtService;
	@Mock private IValidationCodeProvider validationCodeProvider;
	@Mock private ModelHeaderJpaRepository modelHeaderRepository;
	@Mock private DocumentModelReadRepository documentModelReadRepository;
	@Spy private DataServicesCoreProperties dataServicesCoreProperties = new DataServicesCoreProperties();
	@Mock private AuthenticatedUserLoader backendUserLoader;
	@Mock private IndexedModelFieldCache indexedModelFieldCache;
	@Mock private DocumentModelServiceFactory documentModelServiceFactory;
	@InjectMocks private BackendAuthenticationService backendAuthenticationService = spy(new BackendAuthenticationService(true));
	@InjectMocks private KernelCachesPreloader kernelCachesPreloader;

	private String username;

	@BeforeMethod
	public void setUp() {
		reset(rtService, indexedModelFieldCache, documentModelServiceFactory, eventPublisher, modelHeaderRepository, documentModelReadRepository);

		username = RandomStringUtils.randomAlphabetic(10);
		dataServicesCoreProperties.getAuthorization().getBackendJob().getPrincipal().setUsername(username);
		dataServicesCoreProperties.getInitialization().getPreCompile().setEnabledForModels(List.of("*"));
		when(backendUserLoader.loadUser(anyString())).thenReturn(User.builder().username(username).password("").build());
		doNothing().when(rtService).precompileDocumentModel(any());
		when(documentModelServiceFactory.createDocumentModelSearchService(any())).thenReturn(null);
	}

	@Test(description = "Should preload all caches when initialization finishes")
	void shouldPreloadAllCachesWhenInitializationFinishes() {
		Header header = mockModelHeader();
		DocumentModelWrapper documentModelWrapper = createDocumentModelWrapper(header);

		when(modelHeaderRepository.findByModelType(DOCUMENT_MODEL_TYPE)).thenReturn(Arrays.asList(header));
		when(documentModelReadRepository.readModel(header.getId())).thenReturn(documentModelWrapper);
		when(indexedModelFieldCache.getIndexedFields(header.getId())).thenReturn(new HashSet<>());

		kernelCachesPreloader.listenOnServicesInitializationFinished(new DataServicesInitializationFinishedEvent());

		verify(indexedModelFieldCache, times(1)).getIndexedFields(header.getId());
		verify(documentModelServiceFactory, times(1)).createDocumentModelSearchService(documentModelWrapper);
		verify(eventPublisher, times(1)).publishEvent(any(DataServicesDocumentModelCachesPreloadedEvent.class));
	}

	@Test(description = "Should continue preloading when indexed field cache fails for one model")
	void shouldContinuePreloadingWhenIndexedFieldCacheFailsForOneModel() {
		Header header1 = mockModelHeader();
		Header header2 = mockModelHeader();
		DocumentModelWrapper wrapper1 = createDocumentModelWrapper(header1);
		DocumentModelWrapper wrapper2 = createDocumentModelWrapper(header2);

		when(modelHeaderRepository.findByModelType(DOCUMENT_MODEL_TYPE)).thenReturn(Arrays.asList(header1, header2));
		when(documentModelReadRepository.readModel(header1.getId())).thenReturn(wrapper1);
		when(documentModelReadRepository.readModel(header2.getId())).thenReturn(wrapper2);
		when(indexedModelFieldCache.getIndexedFields(header1.getId())).thenThrow(new RuntimeException("Cache error"));
		when(indexedModelFieldCache.getIndexedFields(header2.getId())).thenReturn(new HashSet<>());

		kernelCachesPreloader.listenOnServicesInitializationFinished(new DataServicesInitializationFinishedEvent());

		verify(indexedModelFieldCache, times(1)).getIndexedFields(header1.getId());
		verify(indexedModelFieldCache, times(1)).getIndexedFields(header2.getId());
		verify(documentModelServiceFactory, times(1)).createDocumentModelSearchService(wrapper1);
		verify(documentModelServiceFactory, times(1)).createDocumentModelSearchService(wrapper2);
		verify(eventPublisher, times(1)).publishEvent(any(DataServicesDocumentModelCachesPreloadedEvent.class));
	}

	@Test(description = "Should preload all caches for multiple models")
	void shouldPreloadAllCachesForMultipleModels() {
		Header header1 = mockModelHeader();
		Header header2 = mockModelHeader();
		Header header3 = mockModelHeader();
		DocumentModelWrapper wrapper1 = createDocumentModelWrapper(header1);
		DocumentModelWrapper wrapper2 = createDocumentModelWrapper(header2);
		DocumentModelWrapper wrapper3 = createDocumentModelWrapper(header3);

		when(modelHeaderRepository.findByModelType(DOCUMENT_MODEL_TYPE)).thenReturn(Arrays.asList(header1, header2, header3));
		when(documentModelReadRepository.readModel(header1.getId())).thenReturn(wrapper1);
		when(documentModelReadRepository.readModel(header2.getId())).thenReturn(wrapper2);
		when(documentModelReadRepository.readModel(header3.getId())).thenReturn(wrapper3);
		when(indexedModelFieldCache.getIndexedFields(any())).thenReturn(new HashSet<>());

		kernelCachesPreloader.listenOnServicesInitializationFinished(new DataServicesInitializationFinishedEvent());

		verify(indexedModelFieldCache, times(1)).getIndexedFields(header1.getId());
		verify(indexedModelFieldCache, times(1)).getIndexedFields(header2.getId());
		verify(indexedModelFieldCache, times(1)).getIndexedFields(header3.getId());
		verify(documentModelServiceFactory, times(1)).createDocumentModelSearchService(wrapper1);
		verify(documentModelServiceFactory, times(1)).createDocumentModelSearchService(wrapper2);
		verify(documentModelServiceFactory, times(1)).createDocumentModelSearchService(wrapper3);
	}

	@Test(description = "Should respect enabledForModels configuration for preloading")
	void shouldRespectEnabledForModelsConfigurationForPreloading() {
		Header header1 = mockModelHeader();
		Header header2 = mockModelHeader();
		DocumentModelWrapper wrapper1 = createDocumentModelWrapper(header1);
		DocumentModelWrapper wrapper2 = createDocumentModelWrapper(header2);

		dataServicesCoreProperties.getInitialization().getPreCompile().setEnabledForModels(List.of(header1.getId()));

		when(modelHeaderRepository.findByModelType(DOCUMENT_MODEL_TYPE)).thenReturn(Arrays.asList(header1, header2));
		when(documentModelReadRepository.readModel(header1.getId())).thenReturn(wrapper1);
		when(documentModelReadRepository.readModel(header2.getId())).thenReturn(wrapper2);
		when(indexedModelFieldCache.getIndexedFields(header1.getId())).thenReturn(new HashSet<>());

		kernelCachesPreloader.listenOnServicesInitializationFinished(new DataServicesInitializationFinishedEvent());

		verify(indexedModelFieldCache, times(1)).getIndexedFields(header1.getId());
		verify(indexedModelFieldCache, never()).getIndexedFields(header2.getId());
		verify(documentModelServiceFactory, times(1)).createDocumentModelSearchService(wrapper1);
		verify(documentModelServiceFactory, never()).createDocumentModelSearchService(wrapper2);
	}

	@Test(description = "Should not preload when no models are enabled")
	void shouldNotPreloadWhenNoModelsAreEnabled() {
		Header header = mockModelHeader();
		DocumentModelWrapper wrapper = createDocumentModelWrapper(header);

		dataServicesCoreProperties.getInitialization().getPreCompile().setEnabledForModels(List.of("non-existent-model"));

		when(modelHeaderRepository.findByModelType(DOCUMENT_MODEL_TYPE)).thenReturn(Arrays.asList(header));
		when(documentModelReadRepository.readModel(header.getId())).thenReturn(wrapper);

		kernelCachesPreloader.listenOnServicesInitializationFinished(new DataServicesInitializationFinishedEvent());

		verify(indexedModelFieldCache, never()).getIndexedFields(any());
		verify(documentModelServiceFactory, never()).createDocumentModelSearchService(any());
		verify(eventPublisher, times(1)).publishEvent(any(DataServicesDocumentModelCachesPreloadedEvent.class));
	}

	@Test(description = "Should not call caches when pre-compilation fails")
	void shouldNotCallCachesWhenPreCompilationFails() {
		Header header = mockModelHeader();
		DocumentModelWrapper wrapper = createDocumentModelWrapper(header);

		when(modelHeaderRepository.findByModelType(DOCUMENT_MODEL_TYPE)).thenReturn(Arrays.asList(header));
		when(documentModelReadRepository.readModel(header.getId())).thenReturn(wrapper);
		doThrow(new RuntimeException("Precompile error")).when(rtService).precompileDocumentModel(any());

		kernelCachesPreloader.listenOnServicesInitializationFinished(new DataServicesInitializationFinishedEvent());

		verify(indexedModelFieldCache, never()).getIndexedFields(any());
		verify(documentModelServiceFactory, never()).createDocumentModelSearchService(any());
	}

	@Test(description = "Should continue preloading when DocumentModelSearchService cache fails for one model")
	void shouldContinuePreloadingWhenDocumentModelSearchServiceCacheFailsForOneModel() {
		Header header1 = mockModelHeader();
		Header header2 = mockModelHeader("another-model");
		DocumentModelWrapper wrapper1 = createDocumentModelWrapper(header1);
		DocumentModelWrapper wrapper2 = createDocumentModelWrapper(header2);

		when(modelHeaderRepository.findByModelType(DOCUMENT_MODEL_TYPE)).thenReturn(Arrays.asList(header1, header2));
		when(documentModelReadRepository.readModel(header1.getId())).thenReturn(wrapper1);
		when(documentModelReadRepository.readModel(header2.getId())).thenReturn(wrapper2);
		when(indexedModelFieldCache.getIndexedFields(any())).thenReturn(Collections.emptySet());
		when(documentModelServiceFactory.createDocumentModelSearchService(wrapper1))
			.thenThrow(new RuntimeException("DocumentModelSearchService cache error"));
		when(documentModelServiceFactory.createDocumentModelSearchService(wrapper2))
			.thenReturn(mock(IDocumentModelSearchService.class));

		kernelCachesPreloader.listenOnServicesInitializationFinished(new DataServicesInitializationFinishedEvent());

		verify(indexedModelFieldCache, times(2)).getIndexedFields(any());
		verify(documentModelServiceFactory).createDocumentModelSearchService(wrapper1);
		verify(documentModelServiceFactory).createDocumentModelSearchService(wrapper2);
	}

	private DocumentModelWrapper createDocumentModelWrapper(Header header) {
		DocumentModel model = new DocumentModel();
		model.setHeader(header);
		return new DocumentModelWrapper(model);
	}
}
