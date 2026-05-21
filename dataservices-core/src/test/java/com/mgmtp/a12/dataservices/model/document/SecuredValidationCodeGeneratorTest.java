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

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.authorization.AuthConstants;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.model.document.internal.ValidationCodeGenerator;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

public class SecuredValidationCodeGeneratorTest extends AbstractDataServicesCoreTest {
	@Mock private ValidationCodeGenerator validationCodeGenerator;
	@Mock private ModelPermissionEvaluator<IDocumentModel> modelPermissionEvaluator;
	@InjectMocks private SecuredValidationCodeGenerator securedValidationCodeGenerator;

	@Test
	void testLoadValidationCode_successful() {
		String modelId = RandomStringUtils.randomAlphabetic(15);
		String code = RandomStringUtils.randomAlphabetic(15);
		ListIProblemReporter problemReporter = new ListIProblemReporter();
		Mockito.when(validationCodeGenerator.getValidationCode(ArgumentMatchers.any(String.class), ArgumentMatchers.any())).thenReturn(code);
		Assert.assertEquals(code, securedValidationCodeGenerator.generateValidationCode(modelId, problemReporter));

		Mockito.verify(validationCodeGenerator, Mockito.times(1)).getValidationCode(modelId, problemReporter);
		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(modelId);
	}

	@Test
	void testLoadValidationCode_hasNoPermission() {
		String modelId = RandomStringUtils.randomAlphabetic(15);
		ListIProblemReporter problemReporter = new ListIProblemReporter();
		Mockito.doThrow(new AccessDeniedException(AuthConstants.ACCESS_DENIED)).
			when(modelPermissionEvaluator).checkModelReadPermission(modelId);

		AccessDeniedException e = Assert.expectThrows(AccessDeniedException.class, () ->
			securedValidationCodeGenerator.generateValidationCode(modelId, problemReporter));

		Mockito.verify(validationCodeGenerator, Mockito.times(0)).getValidationCode(modelId, problemReporter);
		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(modelId);
		Assert.assertEquals(e.getMessage(), AuthConstants.ACCESS_DENIED);


	}
}
