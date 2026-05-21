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

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ApplicationListenerMethodAdapter;
import org.springframework.context.event.EventListener;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.util.Objects;

import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;

/**
 * {@link GenericApplicationListener} adapter that delegates the processing of
 * an event to a {@link CommonDataServicesEventListener} annotated method. Supports
 * the exact same features as any regular {@link EventListener} annotated method
 * but only listens to events that come from the same application context.
 *
 * @see CommonDataServicesEventListener
 */
public class CommonDataServicesListenerMethodAdapter extends ApplicationListenerMethodAdapter {
	private final ApplicationContext applicationContext;

	public CommonDataServicesListenerMethodAdapter(
			String beanName,
			Class<?> targetClass,
			Method method,
			ApplicationContext applicationContext
	) {
		super(beanName, targetClass, method);
		this.applicationContext = applicationContext;
	}

	@Override public void onApplicationEvent(@NonNull ApplicationEvent event) {
		if (Objects.nonNull(applicationContext) && applicationContext.equals(event.getSource())) {
			super.onApplicationEvent(event);
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("The event is not from the same context - skipping " + event);
			}
		}
	}
}
