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
package com.mgmtp.a12.dataservices.utils.internal;

import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.mgmtp.a12.dataservices.model.persistence.GenericModelReadRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.initialization.events.DataServicesInitializationFinishedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The Document and generic models have to be preloaded on the application start-up otherwise first request to load them is really slow.
 * First requests to the server is usually for retrieving all models. Without this request no other can be sent. Models
 * are not being often changed on the server-side and they are served frequently. Therefore, it is best to load them on
 * the start-up to avoid any exceptionally lengthy request.
 */
@EnableAsync
@Slf4j
@RequiredArgsConstructor
@Component public class CachePreloader {

	private final ModelHeaderJpaRepository modelHeaderRepository;
	private final GenericModelReadRepository genericModelReadRepository;
	/**
	 * After server is started all models will get loaded into the eh cache.
	 */
	@Async
	@CommonDataServicesEventListener public void onApplicationEvent(DataServicesInitializationFinishedEvent contextRefreshedEvent) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		Long modelsCount = modelHeaderRepository.findAll().stream()
			.map(header -> genericModelReadRepository.readModel(header.getId()))
			.count();

		stopWatch.stop();
		log.debug("Pre-loading of models finished in {} ms, pre-loaded {} Models.", stopWatch.getTotalTimeMillis(), modelsCount);
	}
}
