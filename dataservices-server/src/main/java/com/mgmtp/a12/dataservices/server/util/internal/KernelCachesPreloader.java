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

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.dataservices.initialization.events.DataServicesDocumentModelCachesPreloadedEvent;
import com.mgmtp.a12.dataservices.initialization.events.DataServicesInitializationFinishedEvent;
import com.mgmtp.a12.dataservices.model.document.IValidationCodeProvider;
import com.mgmtp.a12.dataservices.model.internal.IndexedModelFieldCache;
import com.mgmtp.a12.dataservices.model.document.persistence.DocumentModelReadRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.utils.internal.GenericUtils;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentRtService;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.uaa.authentication.backend.BackendAuthenticationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;

/**
 * Preloads kernel caches and indexed field cache asynchronously on application startup.
 *
 * Generation of validation code, population of the indexed field cache, and preloading of
 * DocumentModelSearchService cache are expensive operations that would happen with the first
 * request to the server per model. To avoid lengthy delays on first queries, this component
 * preloads these caches during application startup.
 */
@RequiredArgsConstructor
@Slf4j
@EnableAsync
@Component public class KernelCachesPreloader {

	private final ApplicationEventPublisher eventPublisher;
	private final IDocumentRtService rtService;
	private final ModelHeaderJpaRepository modelHeaderRepository;
	private final DocumentModelReadRepository documentModelReadRepository;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final IValidationCodeProvider validationCodeProvider;
	private final BackendAuthenticationService backendAuthenticationService;
	private final IndexedModelFieldCache indexedModelFieldCache;
	private final DocumentModelServiceFactory documentModelServiceFactory;

	/**
	 * Preloads kernel caches, indexed field cache, and DocumentModelSearchService cache after services initialization has finished.
	 *
	 * Initiates pre-compilation of document models, generation of validation code,
	 * population of the indexed field cache, and preloading of DocumentModelSearchService cache in an authenticated context.
	 * All document models are processed in parallel, each establishing its own backend authentication context.
	 *
	 * @param dataServicesInitializationFinishedEvent the event signaling that Data Services initialization is complete; never `null`.
	 * @event {@link DataServicesDocumentModelCachesPreloadedEvent}
	 */
	@Async
	@CommonDataServicesEventListener public void listenOnServicesInitializationFinished(
		DataServicesInitializationFinishedEvent dataServicesInitializationFinishedEvent) {
		try {
			modelHeaderRepository.findByModelType(DOCUMENT_MODEL_TYPE).parallelStream()
				.map(Header::getId)
				.map(documentModelReadRepository::readModel)
				.filter(this::isPreCompilationEnabledForModel)
				.forEach(documentModel ->
					backendAuthenticationService.executeWithBackendAuthentication(
						dataServicesCoreProperties.getAuthorization().getBackendJob().getPrincipal().getUsername(),
						() -> {
							StopWatch stopWatch = StopWatch.createStarted();
							try {
								rtService.precompileDocumentModel(documentModel);
							} catch (Exception e) {
								log.error("pre-compilation failed for document model {}", documentModel.getHeader().getId());
								throw e;
							}

							try {
								ListIProblemReporter pr = new ListIProblemReporter();
								validationCodeProvider.getValidationCode(documentModel.getHeader().getId(), pr);
								pr.validate(ExceptionCodes.VALIDATION_CODES_GENERATION_EXCEPTION_CODE, ExceptionKeys.VALIDATION_CODES_GENERATION_ERROR_KEY,
									"Error while validation codes generation");
							} catch (Exception e) {
								log.error("generation of validation code for document model {} failed", documentModel.getHeader().getId());
								throw e;
							}

							try {
								StopWatch cacheStopWatch = StopWatch.createStarted();
								indexedModelFieldCache.getIndexedFields(documentModel.getHeader().getId());
								cacheStopWatch.stop();
								log.info("Indexed field cache preloading for {} finished in {} ms",
									documentModel.getHeader().getId(), cacheStopWatch.getTime());
							} catch (Exception e) {
								log.error("Indexed field cache preloading failed for document model {}",
									documentModel.getHeader().getId(), e);
							}

							try {
								StopWatch searchServiceCacheStopWatch = StopWatch.createStarted();
								documentModelServiceFactory.createDocumentModelSearchService(documentModel);
								searchServiceCacheStopWatch.stop();
								log.info("DocumentModelSearchService cache preloading for {} finished in {} ms",
									documentModel.getHeader().getId(), searchServiceCacheStopWatch.getTime());
							} catch (Exception e) {
								log.error("DocumentModelSearchService cache preloading failed for document model {}",
									documentModel.getHeader().getId(), e);
							}

							stopWatch.stop();
							log.info("Pre-loading of {} finished in {} ms", documentModel.getHeader().getId(), stopWatch.getTime());
							return null;
						}));
			eventPublisher.publishEvent(new DataServicesDocumentModelCachesPreloadedEvent());
		} catch (Exception ex) {
			log.warn("Kernel cache pre-loading failed", ex);
		}
	}

	private boolean isPreCompilationEnabledForModel(IDocumentModel documentModel) {
		return GenericUtils.matchOrAll(documentModel.getHeader().getId(),
			dataServicesCoreProperties.getInitialization().getPreCompile().getEnabledForModels());
	}
}
