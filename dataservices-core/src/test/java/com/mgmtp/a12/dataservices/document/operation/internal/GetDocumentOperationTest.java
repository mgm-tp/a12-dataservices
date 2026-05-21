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

import java.util.Optional;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.DocumentSpec;
import com.mgmtp.a12.dataservices.document.internal.DefaultDataServicesDocument;
import com.mgmtp.a12.dataservices.document.internal.DefaultDocumentMetadata;
import com.mgmtp.a12.dataservices.document.operation.events.GetDocumentAfterEvent;
import com.mgmtp.a12.dataservices.document.operation.events.GetDocumentBeforeEvent;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.utils.internal.LoadedDocumentReferencesContextHolder;

@Listeners(MockitoTestNGListener.class)
public class GetDocumentOperationTest {

	@InjectMocks private GetDocumentOperation getDocumentOperation;

	@Mock DocumentService documentService;
	@Mock ApplicationEventPublisher applicationEventPublisher;
	@Mock DocumentSupport documentSupport;
	@Mock Anonymizer anonymizer;
	@Mock DefaultDocumentMetadata documentMetadata;

	private AutoCloseable openMocks;
	private DataServicesDocument dataServicesDocument;
	private final DocumentReference documentReference = new DocumentReference("BusinessPartner", "1");

	@BeforeMethod
	public void setUp() {
		openMocks = MockitoAnnotations.openMocks(documentMetadata);
		dataServicesDocument = new DefaultDataServicesDocument( null, documentMetadata);
	}

	@AfterMethod
	public void tearDown() throws Exception {
		openMocks.close();
	}

	@Test public void testGetDocument_success() {
		try (MockedStatic<LoadedDocumentReferencesContextHolder> mockedStatic = Mockito.mockStatic(LoadedDocumentReferencesContextHolder.class)) {
			Mockito.when(documentService.load(documentReference)).thenReturn(Optional.of(dataServicesDocument));
			Mockito.when(documentSupport.convertToDocumentSpec(Mockito.any())).thenReturn(new DocumentSpec());

			getDocumentOperation.rpc(documentReference);

			Mockito.verify(applicationEventPublisher, Mockito.times(1)).publishEvent(Mockito.any(GetDocumentBeforeEvent.class));
			Mockito.verify(applicationEventPublisher, Mockito.times(1)).publishEvent(Mockito.any(GetDocumentAfterEvent.class));
			mockedStatic.verify(() -> LoadedDocumentReferencesContextHolder.addDocumentReference(Mockito.any(DocumentSpec.class)), Mockito.times(1));
		}
	}

	@Test(expectedExceptions = NotFoundException.class)
	public void testGetDocument_documentNotFound() {
		try (MockedStatic<LoadedDocumentReferencesContextHolder> mockedStatic = Mockito.mockStatic(LoadedDocumentReferencesContextHolder.class)) {
			Mockito.when(documentService.load(documentReference)).thenReturn(Optional.empty());

			getDocumentOperation.rpc(documentReference);

			Mockito.verify(applicationEventPublisher, Mockito.times(1)).publishEvent(Mockito.any(GetDocumentBeforeEvent.class));
			Mockito.verify(applicationEventPublisher, Mockito.times(0)).publishEvent(Mockito.any(GetDocumentAfterEvent.class));
			mockedStatic.verify(() -> LoadedDocumentReferencesContextHolder.addDocumentReference(Mockito.any(DocumentSpec.class)), Mockito.times(0));
		}
	}
}
