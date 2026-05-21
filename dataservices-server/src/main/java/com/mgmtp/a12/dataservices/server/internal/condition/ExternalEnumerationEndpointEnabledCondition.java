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
package com.mgmtp.a12.dataservices.server.internal.condition;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.configuration.validation.internal.ConfigurationMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExternalEnumerationEndpointEnabledCondition extends AbstractDataServicesCondition {

	private static final String ENUMERATION_ENDPOINT_ENABLED = "mgmtp.a12.dataservices.enumeration.rest-endpoint.enabled";

	@Override
	protected String getStringRepresentation(DataServicesCoreProperties dataServicesCoreProperties) {
		return evaluateCondition(dataServicesCoreProperties) ? enabledMessage().getMessage() : disabledMessage().getMessage();
	}

	@Override protected boolean evaluateCondition(DataServicesCoreProperties dataServicesCoreProperties) {
		return dataServicesCoreProperties.getEnumeration().getRestEndpoint().isEnabled();
	}

	@Override
	protected ConfigurationMessage validate(DataServicesCoreProperties dataServicesCoreProperties) {
		return evaluateCondition(dataServicesCoreProperties) ? enabledMessage() : disabledMessage();
	}

	private ConfigurationMessage enabledMessage() {
		return makeValidMessage("External Enumeration REST endpoint enabled", ENUMERATION_ENDPOINT_ENABLED);
	}

	private ConfigurationMessage disabledMessage() {
		return makeValidMessage("External Enumeration REST endpoint is disabled", ENUMERATION_ENDPOINT_ENABLED);
	}
}
