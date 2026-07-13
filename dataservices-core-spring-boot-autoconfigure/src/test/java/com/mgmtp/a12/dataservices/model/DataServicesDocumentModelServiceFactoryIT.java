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
package com.mgmtp.a12.dataservices.model;

import java.io.IOException;

import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.CONTRACT_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_DOCUMENT_MODEL_PATH;
import static org.mockito.ArgumentMatchers.any;

public class DataServicesDocumentModelServiceFactoryIT extends AbstractSpringContextIT {

	@MockitoSpyBean private DocumentModelServiceFactory documentModelServiceFactory;
	@MockitoSpyBean private IDocumentModelResolver modelResolverSpy;

	@BeforeMethod
	public void setup() throws IOException {
		cleanUpTestEnvironment();
		modelsFunctions.createModel(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(CONTRACT_DOCUMENT_MODEL_PATH);
		Mockito.reset(modelResolverSpy);
		Mockito.reset(documentModelServiceFactory);
		modelCacheManager.invalidateDocumentModelSearchServiceCache();
	}

	@Test
	public void secondCallHitsCache() {
		modelResolverSpy.getDocumentModelSearchService(BUSINESS_PARTNER_DOCUMENT_MODEL);
		modelResolverSpy.getDocumentModelSearchService(BUSINESS_PARTNER_DOCUMENT_MODEL);
		Mockito.verify(documentModelServiceFactory, Mockito.times(1)).createDocumentModelSearchService(any());
	}

	@Test
	public void differentCallsInvokeMethod() {
		modelResolverSpy.getDocumentModelSearchService(BUSINESS_PARTNER_DOCUMENT_MODEL);
		modelResolverSpy.getDocumentModelSearchService(CONTRACT_DOCUMENT_MODEL);
		Mockito.verify(documentModelServiceFactory, Mockito.times(2)).createDocumentModelSearchService(any());
	}

}
