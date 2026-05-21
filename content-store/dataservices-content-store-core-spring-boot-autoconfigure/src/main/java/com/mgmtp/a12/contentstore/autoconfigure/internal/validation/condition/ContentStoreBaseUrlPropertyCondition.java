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
package com.mgmtp.a12.contentstore.autoconfigure.internal.validation.condition;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.contentstore.configuration.ContentStoreProperties;
import com.mgmtp.a12.contentstore.configuration.internal.validation.ConfigurationMessage;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;

public class ContentStoreBaseUrlPropertyCondition extends AbstractContentStoreCondition {
	@Override protected String getStringRepresentation(ContentStoreProperties contentStoreProperties) {
		return (evaluateCondition(contentStoreProperties) ?
			makeValidMessage("Valid content store base url", CONTENT_STORE_BASE_URL) :
			makeWarnMessage("Content store base url is a mandatory property", CONTENT_STORE_BASE_URL)).getMessage();
	}

	@Override protected boolean evaluateCondition(ContentStoreProperties contentStoreProperties) {
		return StringUtils.isNotBlank(contentStoreProperties.getBaseUrl());
	}

	@Override protected ConfigurationMessage validate(ContentStoreProperties contentStoreProperties) {
		if (evaluateCondition(contentStoreProperties)) {
			return makeValidMessage("Valid content store base url", CONTENT_STORE_BASE_URL);
		} else {
			throw new UnexpectedException(String.format("%s is required and cannot be blank for starting content store application", CONTENT_STORE_BASE_URL))
				.withAnonymityMessage("Base URL is missing.");
		}
	}
}
