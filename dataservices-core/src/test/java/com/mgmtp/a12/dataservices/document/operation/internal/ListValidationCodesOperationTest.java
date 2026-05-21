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
package com.mgmtp.a12.dataservices.document.operation.internal;

import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.model.document.SecuredValidationCodeGenerator;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;

@Listeners(MockitoTestNGListener.class)
public class ListValidationCodesOperationTest {

	@InjectMocks ListValidationCodesOperation listValidationCodesOperation;

	@Mock private SecuredValidationCodeGenerator securedValidationCodeGenerator;
	@Mock private DataServicesCoreProperties dataServicesCoreProperties;
	@Mock private DataServicesCoreProperties.Document documentProperties;
	@Mock private DataServicesCoreProperties.Document.Validation validationProperties;
	@Mock private DataServicesCoreProperties.Document.Validation.ValidationCodeList validationCodeListProperties;
	@Mock private ModelHeaderJpaRepository modelHeaderJpaRepository;

	@Test public void testListValidationCode_success() {
		Mockito.when(dataServicesCoreProperties.getDocuments()).thenReturn(documentProperties);
		Mockito.when(securedValidationCodeGenerator.generateValidationCode(Mockito.any(), Mockito.any())).thenReturn("he");
		Mockito.when(documentProperties.getValidation()).thenReturn(validationProperties);
		Mockito.when(validationProperties.getList()).thenReturn(validationCodeListProperties);
		Mockito.when(validationCodeListProperties.getHardLimit()).thenReturn(10);
		Mockito.when(modelHeaderJpaRepository.existsById(Mockito.any())).thenReturn(true);

		listValidationCodesOperation.rpc(List.of("name1", "name2"));

		Mockito.verify(securedValidationCodeGenerator, Mockito.times(2)).generateValidationCode(Mockito.any(), Mockito.any());
	}

	@Test(expectedExceptions = InvalidInputException.class)
	public void testListValidationCode_hardLimitExceeded() {
		Mockito.when(dataServicesCoreProperties.getDocuments()).thenReturn(documentProperties);
		Mockito.when(securedValidationCodeGenerator.generateValidationCode(Mockito.any(), Mockito.any())).thenReturn("he");
		Mockito.when(documentProperties.getValidation()).thenReturn(validationProperties);
		Mockito.when(validationProperties.getList()).thenReturn(validationCodeListProperties);
		Mockito.when(validationCodeListProperties.getHardLimit()).thenReturn(0);

		listValidationCodesOperation.rpc(List.of("name1", "name2"));

		Mockito.verify(securedValidationCodeGenerator, Mockito.times(0)).generateValidationCode(Mockito.any(), Mockito.any());
	}
}
