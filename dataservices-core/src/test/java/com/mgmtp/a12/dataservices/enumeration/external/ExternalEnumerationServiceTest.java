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
package com.mgmtp.a12.dataservices.enumeration.external;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.authorization.AuthConstants;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

public class ExternalEnumerationServiceTest extends AbstractDataServicesCoreTest {

	@Mock private ModelPermissionEvaluator<IDocumentModel> modelPermissionEvaluator;
	@Mock private DocumentService documentService;
	@Mock private ExternalEnumerationLoader externalEnumerationLoader;
	private final DataServicesCoreProperties dataServicesCoreProperties = Mockito.spy(DataServicesCoreProperties.class);
	@Mock private DefaultDocumentService defaultDocumentService;

	private ExternalEnumerationService externalEnumerationService;

	@BeforeMethod public void before() {
		externalEnumerationService = new ExternalEnumerationService(modelPermissionEvaluator,  documentService, Optional.of(List.of(externalEnumerationLoader)),
			dataServicesCoreProperties, defaultDocumentService);
	}

	@Test
	void testLoadExternalEnumerationForModel_successful() {
		String modelId = RandomStringUtils.randomAlphabetic(15);
		DataServicesDocument document = makeTestDsDocument();
		DocumentReference documentReference = document.getMetadata().getDocRef();
		dataServicesCoreProperties.getEnumeration().setPageLimit(200);
		Mockito.when(defaultDocumentService.loadForModel(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(List.of(documentReference));
		Mockito.when(documentService.load(documentReference)).thenReturn(Optional.of(document));
		Mockito.when(externalEnumerationLoader.isModelSupported(modelId)).thenReturn(true);

		externalEnumerationService.loadExternalEnumerationForModel(modelId);

		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(modelId);
		Mockito.verify(defaultDocumentService, Mockito.times(1)).loadForModel(
			ArgumentMatchers.eq(modelId),
			ArgumentMatchers.argThat(pageable -> {
				Assert.assertEquals(pageable.getOffset(), 0);
				Assert.assertEquals(pageable.getPageSize(), 200);
				Assert.assertEquals(pageable.getSort(), Sort.unsorted());
				return true;
			})
		);

		Mockito.verify(documentService, Mockito.times(1)).load(documentReference);
		Mockito.verify(externalEnumerationLoader, Mockito.times(1)).isModelSupported(modelId);
	}

	@Test
	void testLoadExternalEnumerationForModel_whenHasNoPermission() {
		String modelId = RandomStringUtils.randomAlphabetic(15);

		Mockito.doThrow(new AccessDeniedException(AuthConstants.ACCESS_DENIED)).when(modelPermissionEvaluator).checkModelReadPermission(modelId);

		AccessDeniedException e = Assert.expectThrows(AccessDeniedException.class, () -> externalEnumerationService.loadExternalEnumerationForModel(modelId));

		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(modelId);
		Mockito.verifyNoInteractions(documentService, defaultDocumentService, externalEnumerationLoader);
		Assert.assertEquals(e.getMessage(), AuthConstants.ACCESS_DENIED);
	}

}
