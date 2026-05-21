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
package com.mgmtp.a12.dataservices.model.document;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithUserDetails;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.authorization.AuthConstants;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.model.exception.SerializationException;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelEntity;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.UserConstants.ADMIN_USER;
import static com.mgmtp.a12.dataservices.constants.UserConstants.GUEST_USER;

public class CreateDocumentModelIT extends AbstractSpringContextIT {

	@DataProvider public static Object[][] invalidModelNameProvider() {
		return new Object[][] {
			new Object[] { "0BusinessPartner" },
			new Object[] { "BusinessPartnerBusinessPartnerBusinessPartnerBusinessPartnerBusinessPartnerBusinessPartnerBusinessPar" },
			new Object[] { "_BusinessPartner-BusinessPartner+BusinessPartner" }
		};
	}

	@Test(expectedExceptions = AccessDeniedException.class, expectedExceptionsMessageRegExp = AuthConstants.ACCESS_DENIED)
	@WithUserDetails(value = GUEST_USER)
	public void createModelWithBadRole() {
		List<ModelEntity> allModels = modelRepository.findAll();
		Assert.assertEquals(allModels.size(), 0);
		String modelWithBadRole = loadResourceFromClasspathAsString(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH).replace(ADMIN_USER, "adminX");
		try {
			modelService.create(modelWithBadRole);
		} catch (final Exception ex) {
			allModels = modelRepository.findAll();
			Assert.assertEquals(allModels.size(), 0);
			Assert.assertNotNull(ex);
			throw ex;
		}
		Assert.fail();

	}

	@Test(expectedExceptions = InvalidInputException.class, expectedExceptionsMessageRegExp = "Model cannot be created without id")
	public void createModelWithEmptyName() {
		modelService.create(loadResourceFromClasspathAsString(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH).replace(BUSINESS_PARTNER_DOCUMENT_MODEL, ""));
	}

	@Test(dataProvider = "invalidModelNameProvider",
		expectedExceptions = InvalidInputException.class, expectedExceptionsMessageRegExp = "Model name .* is not valid")
	public void createModelWithInvalidName(String invalidModelName) {
		modelService.create(loadResourceFromClasspathAsString(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH).replace(BUSINESS_PARTNER_DOCUMENT_MODEL, invalidModelName));
	}

	@Test
	public void createModelWithProperRole() {
		List<ModelEntity> allModels = modelRepository.findAll();
		Assert.assertEquals(allModels.size(), 0);
		modelService.create(loadResourceFromClasspathAsString(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH));
		allModels = modelRepository.findAll();
		Assert.assertEquals(allModels.size(), 1);
	}

	@Test(expectedExceptions = SerializationException.class, expectedExceptionsMessageRegExp = "Model validation failed. Model is not acceptable:.*")
	public void tryCreateBrokenModelWithRolesAndResolveIncludes() {
		final String brokenDocumentModelString = loadResourceFromClasspathAsString(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH).replace("\"Name\",", "\"Name\"");
		modelService.create(brokenDocumentModelString);
	}

}
