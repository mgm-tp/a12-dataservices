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
package com.mgmtp.a12.examples.document.extension.document;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.examples.custom.exception.InvalidAddressException;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeUpdateEvent;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;

@Listeners(MockitoTestNGListener.class)
public class ContactModelValidationExtensionTest {

	@Mock
	private AddressValidator addressValidator;

	private ContactModelValidationExtension createExtension() {
		return new ContactModelValidationExtension(addressValidator);
	}

	private DocumentV2 createDocument() {
		return DocumentV2.empty("ContactModel")
			.withId("doc-" + System.currentTimeMillis())
			.withFieldValue("/Contact/Address/ZipCode", "123456")
			.withFieldValue("/Contact/Address/Street", "Oak Avenue")
			.withFieldValue("/Contact/Address/Country", "Poland")
			.withFieldValue("/Contact/Address/City", "London");
	}

	@Test
	public void beforeCreateListener_delegates_success() {
		ContactModelValidationExtension extension = createExtension();
		DocumentV2 document = createDocument();
		DocumentBeforeCreateEvent event = new DocumentBeforeCreateEvent(document);

		extension.beforeCreateListener(event);

		verify(addressValidator, times(1)).validatedAddress(document);
		verifyNoMoreInteractions(addressValidator);
	}

	@Test
	public void beforeUpdateListener_delegates_success() {
		ContactModelValidationExtension extension = createExtension();
		DocumentV2 document = createDocument();
		DocumentBeforeUpdateEvent event = new DocumentBeforeUpdateEvent(
			DocumentReference.builder()
				.documentModelName(document.getDocumentModelId())
				.documentId(document.getId().get())
				.build(),
			document,
			document.withFieldValue("/Contact/Address/ZipCode", "123456")
		);

		extension.beforeUpdateListener(event);

		verify(addressValidator, times(1)).validatedAddress(document);
		verifyNoMoreInteractions(addressValidator);
	}

	@Test(expectedExceptions = InvalidAddressException.class,
		expectedExceptionsMessageRegExp = "invalid address")
	public void beforeCreateListener_propagatesException_throwsInvalidAddress() {
		ContactModelValidationExtension extension = createExtension();
		DocumentV2 document = createDocument();
		DocumentBeforeCreateEvent event = new DocumentBeforeCreateEvent(document);
		doThrow(new InvalidAddressException(ExceptionKeys.DOCUMENT_VALIDATION_ERROR_KEY, "invalid address"))
			.when(addressValidator).validatedAddress(document);

		extension.beforeCreateListener(event);
	}

	@Test(expectedExceptions = InvalidAddressException.class,
		expectedExceptionsMessageRegExp = "invalid address")
	public void beforeUpdateListener_propagatesException_throwsInvalidAddress() {
		ContactModelValidationExtension extension = createExtension();
		DocumentV2 document = createDocument();
		DocumentBeforeUpdateEvent event = new DocumentBeforeUpdateEvent(
			DocumentReference.builder()
				.documentModelName(document.getDocumentModelId())
				.documentId(document.getId().get())
				.build(),
			document,
			document.withFieldValue("/Contact/Address/ZipCode", "123456")
		);
		doThrow(new InvalidAddressException(ExceptionKeys.DOCUMENT_VALIDATION_ERROR_KEY, "invalid address"))
			.when(addressValidator).validatedAddress(document);

		extension.beforeUpdateListener(event);
	}
}
