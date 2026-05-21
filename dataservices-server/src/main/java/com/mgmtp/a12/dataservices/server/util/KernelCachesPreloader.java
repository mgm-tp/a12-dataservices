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


import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.task.DelegatingSecurityContextTaskExecutor;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.initialization.events.DataServicesDocumentModelCachesPreloadedEvent;
import com.mgmtp.a12.dataservices.initialization.events.DataServicesInitializationFinishedEvent;
import com.mgmtp.a12.dataservices.model.document.IValidationCodeProvider;
import com.mgmtp.a12.dataservices.model.document.persistence.DocumentModelReadRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.utils.internal.GenericUtils;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentRtService;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.uaa.authentication.backend.BackendAuthenticationService;

import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;

/**
 * Generation of the validation code is an expensive operation which would happen with the first request to the server per model. To avoid lengthy code generation
 * we will preload kernel caches asynchronously on the application start-up.
 */
@Slf4j
@EnableAsync
@Component public class KernelCachesPreloader {

	@Autowired private ApplicationEventPublisher eventPublisher;
	@Autowired private IDocumentRtService rtService;
	@Autowired private ModelHeaderJpaRepository modelHeaderRepository;
	@Autowired private DocumentModelReadRepository documentModelReadRepository;
	@Autowired private DataServicesCoreProperties dataServicesCoreProperties;
	@Autowired private IValidationCodeProvider validationCodeProvider;
	@Autowired private BackendAuthenticationService backendAuthenticationService;

	/**
	 * Preloads kernel caches after services initialization has finished.
	 *
	 * Initiates pre-compilation of document models and generation of validation code in an authenticated context.
	 *
	 * @param dataServicesInitializationFinishedEvent the event signaling that DS initialization is complete; never null.
	 * @event {@link DataServicesDocumentModelCachesPreloadedEvent}
	 */
	@Async
	@CommonDataServicesEventListener public void listenOnServicesInitializationFinished(DataServicesInitializationFinishedEvent dataServicesInitializationFinishedEvent) {
		DelegatingSecurityContextTaskExecutor executor =
			new DelegatingSecurityContextTaskExecutor(new SyncTaskExecutor(), SecurityContextHolder.createEmptyContext());
		Runnable originalRunnable = new Runnable() {
			public void run() {
				backendAuthenticationService.executeWithBackendAuthentication(
					dataServicesCoreProperties.getAuthorization().getBackendJob().getPrincipal().getUsername(),
					() -> {
						try {
							modelHeaderRepository.findByModelType(DOCUMENT_MODEL_TYPE).stream()
								.map(Header::getId)
								.map(documentModelReadRepository::readModel)
								.filter(this::isPreCompilationEnabledForModel)
								.forEach(documentModel -> {
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
										pr.validate(ExceptionCodes.VALIDATION_CODES_GENERATION_EXCEPTION_CODE, ExceptionKeys.VALIDATION_CODES_GENERATION_ERROR_KEY, "Error while validation codes generation");
									} catch (Exception e) {
										log.error("generation of validation code for document model {} failed", documentModel.getHeader().getId());
										throw e;
									}

									stopWatch.stop();
									log.info("Pre-loading of {} finished in {} ms", documentModel.getHeader().getId(), stopWatch.getTime());
								});
							eventPublisher.publishEvent(new DataServicesDocumentModelCachesPreloadedEvent());
						} catch (Exception ex) {
							log.warn("Kernel cache pre-loading failed", ex);
						}

						return null;
					});
			}

			private boolean isPreCompilationEnabledForModel(IDocumentModel documentModel) {
				return GenericUtils.matchOrAll(documentModel.getHeader().getId(),
					dataServicesCoreProperties.getInitialization().getPreCompile().getEnabledForModels());
			}
		};
		executor.execute(originalRunnable);
	}
}
