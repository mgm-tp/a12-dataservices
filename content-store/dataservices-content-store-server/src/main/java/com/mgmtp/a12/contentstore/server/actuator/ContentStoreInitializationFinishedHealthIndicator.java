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
package com.mgmtp.a12.contentstore.server.actuator;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.contentstore.initialization.events.ContentStoreInitializationFinishedEvent;
import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;

/**
 * Health indicator for initialization process of Content Store.
 */
@Component("contentstoreInitializationFinished") public class ContentStoreInitializationFinishedHealthIndicator implements HealthIndicator {
	private static final String DETAIL_KEY = "dataServicesInitialization";
	private static final String FINISHED_DETAIL = "Finished";
	private static final String NOT_FINISHED_DETAIL = "Not Finished";
	private boolean finished = false;

	/**
	 * Reports the initialization status of the Content Store.
	 *
	 * @return {@link Health} with {@link Status#UP} when initialization finished; {@link Status#DOWN} otherwise.
	 */
	@Override public Health health() {
		return Health.status(finished ? Status.UP : Status.DOWN)
			.withDetail(DETAIL_KEY, finished ? FINISHED_DETAIL : NOT_FINISHED_DETAIL)
			.build();
	}

	/**
	 * Event listener for {@link ContentStoreInitializationFinishedEvent}.
	 *
	 * @param event the event
	 */
	@Order
	@CommonDataServicesEventListener public void onApplicationEvent(ContentStoreInitializationFinishedEvent event) {
		finished = true;
	}
}

