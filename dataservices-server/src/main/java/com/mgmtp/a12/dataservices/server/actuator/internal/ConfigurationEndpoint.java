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

import java.util.Map;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import com.mgmtp.a12.dataservices.configuration.internal.ConfigurationPropertiesData;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Endpoint(id = ConfigurationEndpoint.CONFIGURATION_ENDPOINT) public class ConfigurationEndpoint {

	public static final String CONFIGURATION_ENDPOINT = "configuration";
	private final ConfigurationPropertiesData configurationPropertiesData;

	public ConfigurationEndpoint(ConfigurationPropertiesData configurationPropertiesData) {
		this.configurationPropertiesData = configurationPropertiesData;
	}

	@ReadOperation public Map<String, Object> all() {
		return configurationPropertiesData.getAll();
	}

	@ReadOperation public Object sub(@Selector String name) {
		return configurationPropertiesData.getByKey(name);
	}
}
