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
package com.mgmtp.a12.dataservices.model.relationship;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.model.header.HeaderParseException;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_INVALID_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_SUPER_INVALID_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.CONTRACT_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_INVALID_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_SUPER_INVALID_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_BUSINESS_PARTNER_RELATIONSHIP_MODEL_INVALID_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_INVALID_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CO_INSURED_ADDITIONAL_FIELDS_INVALID_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.LEGACY_RM_INVALID_PATH;
import static com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL;
import static com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL;
import static com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.CO_INSURED_ADDITIONAL_FIELDS_INVALID_RM;

public class WrongDocRefIT extends AbstractSpringContextIT {

	@Autowired ModelService modelService;

	@BeforeMethod public void setUp() throws HeaderParseException {
		Objects.requireNonNull(getClass().getResourceAsStream(LEGACY_RM_INVALID_PATH));
		createModel(loadResourceFromClasspathAsString(CONTRACT_DOCUMENT_MODEL_PATH));
		createInvalidModel(loadResourceFromClasspathAsString(BUSINESS_PARTNER_SUPER_INVALID_PATH));
		createInvalidModel(loadResourceFromClasspathAsString(BUSINESS_PARTNER_INVALID_PATH));
		createInvalidModel(loadResourceFromClasspathAsString(CO_INSURED_ADDITIONAL_FIELDS_INVALID_PATH));
	}

	@AfterMethod public void tearDown() {
		modelService.delete(CONTRACT_DOCUMENT_MODEL);
		modelService.delete(CO_INSURED_ADDITIONAL_FIELDS_INVALID_RM);
		modelService.delete(BUSINESS_PARTNER_INVALID_MODEL);
		modelService.delete(BUSINESS_PARTNER_SUPER_INVALID_MODEL);
	}

	@DataProvider public static Object[][] invalidRmDataProvider() {
		return new Object[][] {
			new Object[] { CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_INVALID_PATH },
			new Object[] { CONTRACT_BUSINESS_PARTNER_MODEL, CONTRACT_BUSINESS_PARTNER_RELATIONSHIP_MODEL_INVALID_PATH },
		};
	}

	@Test(dataProvider = "invalidRmDataProvider", expectedExceptions = InvalidInputException.class, expectedExceptionsMessageRegExp = ".*Type: VALUE_ERROR Message: model name must be string of length between 1 and 100 characters and matching the pattern /\\[_a-zA-Z]\\[-_.a-zA-Z0-9]\\*/ ErrorCode: stringFalschesMuster.*")
	public void testValidationCoInsurer(String description, String path) {
		modelService.create(loadResourceFromClasspathAsString(path));
	}
}
