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
package com.mgmtp.a12.dataservices.configuration.internal.validation.condition;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.configuration.validation.internal.ConfigurationMessage;
import com.mgmtp.a12.dataservices.utils.internal.GenericUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KernelDocumentServiceCondition extends AbstractDataServicesCondition {

	public static final String COMPUTATION_FOR_MODEL = "mgmtp.a12.dataservices.documents.computation.enabledForModels";

	@Override protected String getStringRepresentation(DataServicesCoreProperties dataServicesCoreProperties) {
		return String.valueOf(evaluateCondition(dataServicesCoreProperties));
	}

	@Override protected boolean evaluateCondition(DataServicesCoreProperties dataServicesCoreProperties) {
		return true;
	}

	@Override protected ConfigurationMessage validate(DataServicesCoreProperties dataServicesCoreProperties) {
		DataServicesCoreProperties.Document.Computation computation = dataServicesCoreProperties.getDocuments().getComputation();
		List<String> modelsList = computation.getEnabledForModels();
		if (modelsList != null && GenericUtils.isSingleAsterisk(modelsList)) {
			return makeValidMessage("Computation enabled for all models.", COMPUTATION_FOR_MODEL);
		} else if (CollectionUtils.isNotEmpty(modelsList)) {
			return handleNonEmptyModelsList(modelsList);
		} else {
			return makeValidMessage("Computation disabled for all models", COMPUTATION_FOR_MODEL);
		}
	}

	private ConfigurationMessage handleNonEmptyModelsList(List<String> modelsList) {
		if (modelsList.contains(DataServicesCoreProperties.MATCH_ALL)) {
			return makeWarnMessage(String.format(
				"Computation enabled for these models: %%s%%nBut there is '%s' in the list of the supported models, but it is considered to be exact model name instead of wildcard because there are more model names in the list.".formatted(
					DataServicesCoreProperties.MATCH_ALL),
				modelsList), COMPUTATION_FOR_MODEL);
		} else {
				return makeValidMessage(String.format("Computation enabled for these models: %s", modelsList), COMPUTATION_FOR_MODEL);
		}
	}
}
