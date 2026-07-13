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

import java.io.Closeable;
import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.Lifecycle;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;

import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;
import com.mgmtp.a12.dataservices.initialization.events.DataServicesInitializationFinishedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Data Services Initialization Listener class. Listens to the {@link ContextRefreshedEvent} and triggers the data services initialization.
 *
 */
@RequiredArgsConstructor
@Slf4j
public class DataServicesInitializationListener {

	private final ApplicationContext applicationContext;
	private final ApplicationEventPublisher applicationEventPublisher;
	private final InitializationService initializationService;

	/**
	 * Handles application initialization on {@link ContextRefreshedEvent}.
	 * Ensures this listener runs before UAA re-enables security bypass (ordered with {@link Order} 100).
	 *
	 * @param event the Spring context refresh event; never `null`.
	 * @throws IOException if stopping or closing the {@link ApplicationContext} fails.
	 */
	@Order(100)
	@CommonDataServicesEventListener public void onApplicationInitialization(ContextRefreshedEvent event) throws IOException {
		ApplicationContext context = event.getApplicationContext();
		if (applicationContext.equals(context)) {
			try {
				initializationService.runInitialization();
				// Fire event announcing that system is initialized and also preload of caches is requested.
				applicationEventPublisher.publishEvent(new DataServicesInitializationFinishedEvent());

			} catch (Throwable t) {
				log.error("System initialization failed: {}. Exiting.", t.getMessage(), t);
				if (context instanceof Lifecycle lifecycle) {
					lifecycle.stop();
				}
				if (context instanceof Closeable closeable) {
					closeable.close();
				}
				throw t;
			}
		}
	}
}
