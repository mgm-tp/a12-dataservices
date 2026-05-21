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
package com.mgmtp.a12.dataservices.server.actuator.internal;

import com.mgmtp.a12.dataservices.initialization.events.DataServicesInitializationFinishedEvent;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component public class InitializationFinishedHealthIndicator implements HealthIndicator {
	private static final String DETAIL_KEY = "dataServicesInitialization";
	private static final String FINISHED_DETAIL = "Finished";
	private static final String NOT_FINISHED_DETAIL = "Not Finished";
	private Boolean finished = false;

	@Override public Health health() {
		return Health.status(finished ? Status.UP : Status.DOWN)
				.withDetail(DETAIL_KEY, finished ? FINISHED_DETAIL : NOT_FINISHED_DETAIL)
				.build();
	}

	/**
	 * This listener has `LOWEST_PRECEDENCE` because `finished` state should be set to true, only if all other initialization listeners are finished.
	 *
	 * @param event {@link DataServicesInitializationFinishedEvent}
	 */
	@Order
	@CommonDataServicesEventListener public void onApplicationEvent(DataServicesInitializationFinishedEvent event) {
		finished = true;
	}
}
