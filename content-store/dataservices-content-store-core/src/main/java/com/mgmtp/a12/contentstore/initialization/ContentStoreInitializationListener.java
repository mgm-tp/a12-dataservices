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
package com.mgmtp.a12.contentstore.initialization;

import java.io.Closeable;
import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.Lifecycle;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;

import com.mgmtp.a12.contentstore.initialization.events.ContentStoreInitializationFinishedEvent;
import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;

import lombok.extern.slf4j.Slf4j;

/**
 * Listener which listens to the {@link ContextRefreshedEvent} and triggers the content store initialization.
 */
@Slf4j
public class ContentStoreInitializationListener {

	private final ApplicationContext applicationContext;
	private final ApplicationEventPublisher applicationEventPublisher;

	/**
	 * Creates a new listener bound to a specific application context and event publisher.
	 *
	 * @param applicationContext the Spring {@link ApplicationContext} to monitor; must not be null.
	 * @param applicationEventPublisher publisher used to emit initialization events; must not be null.
	 */
	public ContentStoreInitializationListener(ApplicationContext applicationContext, ApplicationEventPublisher applicationEventPublisher) {
		this.applicationContext = applicationContext;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	/**
	 * UAA also listens to the ContextRefreshedEvent and disables security bypass in the listener with order HIGHEST_PRECEDENCE.
	 * Therefore, we need to make sure that our listeners will be executed before UAA disables security bypass -> @Order(101)
	 * And if someone wants their ContextRefreshedEvent listener to be executed before this method, for example,
	 * they need to set an Order lower than 100. Be aware that the listener DataServicesInitializationListener#onApplicationInitialization,
	 * which listens also to the ContextRefreshedEvent, is executed with @Order(100).
	 *
	 * @param event the {@link ContextRefreshedEvent} emitted when the application context is refreshed; never null.
	 * @throws IOException if closing the application context fails after an initialization error.
	 */
	@Order(101)
	@CommonDataServicesEventListener public void onApplicationInitialization(ContextRefreshedEvent event) throws IOException {
		if (applicationContext.equals(event.getApplicationContext())) {
			ApplicationContext context = event.getApplicationContext();
			try {
				// Fire event announcing that system is initialized and also preload of caches is requested.
				applicationEventPublisher.publishEvent(new ContentStoreInitializationFinishedEvent());

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
