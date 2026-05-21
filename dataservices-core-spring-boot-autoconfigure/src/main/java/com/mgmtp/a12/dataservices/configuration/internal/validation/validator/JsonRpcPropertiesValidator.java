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
package com.mgmtp.a12.dataservices.configuration.internal.validation.validator;

import java.util.List;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;

import lombok.extern.slf4j.Slf4j;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

@Slf4j
public class JsonRpcPropertiesValidator implements Validator {

	private static final String INITIALIZATION_JSON_RPC_ENABLED = "mgmtp.a12.dataservices.initialization.scripts.jsonRpc.enabled";
	private static final String INITIALIZATION_JSON_RPC_PATHS = "mgmtp.a12.dataservices.initialization.scripts.jsonRpc.paths";

	@Override public boolean supports(Class<?> clazz) {
		return DataServicesCoreProperties.Initialization.Script.JsonRpc.class.isAssignableFrom(clazz);
	}

	@Override public void validate(Object target, Errors errors) {
		DataServicesCoreProperties.Initialization.Script.JsonRpc jsonRpcProperties = (DataServicesCoreProperties.Initialization.Script.JsonRpc) target;

		boolean jsonRpcInitializationEnabled = jsonRpcProperties.isEnabled();
		List<String> jsonRpcInitializationPaths = jsonRpcProperties.getPaths();
		if (jsonRpcInitializationEnabled && (isEmpty(jsonRpcInitializationPaths))) {
			log.warn("Configuration problem: " + INITIALIZATION_JSON_RPC_ENABLED + " is set to true but no path is provided via " +INITIALIZATION_JSON_RPC_PATHS);
		}
	}
}
