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
package com.mgmtp.a12.dataservices.server.internal.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mgmtp.a12.dataservices.configuration.internal.MonitorPropertiesData;

import lombok.RequiredArgsConstructor;

/**
* API to retrieve monitored configuration properties.
*
* @title Monitor Configuration Properties REST API
* @topic Monitoring
*
* */
@RequiredArgsConstructor
@RequestMapping("#{@dataServicesCoreProperties.server.contextPath}")
@RestController public class MonitorConfigurationControllerImpl {

	private final MonitorPropertiesData monitorPropertiesData;

	/**
	 * Retrieves monitored configuration properties and their current values.
	 * This endpoint exposes specific configuration properties that clients may need to know,
	 * returning the currently effective value for each property (either from configuration
	 * sources or the default value if not overridden).
	 * Please note this endpoint is public and does not require authentication.
	 *
	 * @return A map where keys are property names and values are the current property values.
	 * @title Get Monitored Configuration Properties
	 * @responseSuccess 200 OK:: The response contains a body with the map of monitoring properties.
	 *
	 */
	@GetMapping(path = { "/monitored-properties" })
	public Object getMonitorProperties() {
		return monitorPropertiesData.getMonitorProperties();
	}
}
