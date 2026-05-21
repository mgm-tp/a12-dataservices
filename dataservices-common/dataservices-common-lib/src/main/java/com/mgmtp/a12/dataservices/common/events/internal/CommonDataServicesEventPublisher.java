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
package com.mgmtp.a12.dataservices.common.events.internal;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.PayloadApplicationEvent;

import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;

/**
 * The event publisher that delegates the publishing of event to the current context {@link ApplicationContext}
 */
public class CommonDataServicesEventPublisher implements ApplicationContextAware, ApplicationEventPublisher {
	private ApplicationContext delegate;

	@Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.delegate = applicationContext;
	}

	/**
	 * Notify all *matching* listeners registered with this application of an event.
	 *
	 * If the specified `event` is not an {@link ApplicationEvent},
	 * it is wrapped in a {@link PayloadApplicationEvent} and enforced its source to the current context.
	 * This is for avoiding Spring automatically reassigns the source of `event` and propagates it to
	 * parent context.
	 *
	 * If the specified `event` is an {@link ApplicationEvent}, the source of `event` would still remain,
	 * those who publishes it must be aware of the fact that the event listener generated from
	 * {@link CommonDataServicesEventListener} annotation only listens to events which have the same context.
	 * @param event the event to published
	 */
	@Override public void publishEvent(Object event) {
		if (event instanceof ApplicationEvent) {
			delegate.publishEvent(event);
		} else {
			delegate.publishEvent(new PayloadApplicationEvent<>(delegate, event));
		}
	}
}
