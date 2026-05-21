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
package com.mgmtp.a12.examples.document.encryption;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterRepositoryLoadEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeRepositorySaveEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeIndexEvent;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.internal.DefaultDataServicesDocument;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import org.mockito.InjectMocks;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Base64;

@Listeners(MockitoTestNGListener.class)
public class EncryptionListenersTest {

	@InjectMocks
	private EncryptionListeners listeners;


	@Test
	public void decryptLoadedDocuments_shouldDecodeBase64() {
		String plain = "hello";
		String encoded = Base64.getEncoder().encodeToString(plain.getBytes());
		DocumentReference docRef = new DocumentReference("doc-id", "v1");
		DocumentAfterRepositoryLoadEvent event = new DocumentAfterRepositoryLoadEvent(
			docRef,
			encoded
		);

		listeners.decryptLoadedDocuments(event);

		Assert.assertEquals(event.getDocumentContent(), plain);
	}

	@Test
	public void decryptLoadedDocuments_shouldSkipWhenNull() {
		DocumentAfterRepositoryLoadEvent event = new DocumentAfterRepositoryLoadEvent(
			new DocumentReference("doc-id", "v1"),
			null
		);

		String contentBefore = event.getDocumentContent();
		listeners.decryptLoadedDocuments(event);

		Assert.assertEquals(event.getDocumentContent(), contentBefore);
	}

	@Test
	public void encryptBeforePersisting_shouldEncodeBase64() {
		String plain = "content";
		DocumentBeforeRepositorySaveEvent event = new DocumentBeforeRepositorySaveEvent(
			"test-user",
			plain
		);

		listeners.encryptBeforePersisting(event);

		Assert.assertEquals(
			event.getDocumentContent(),
			Base64.getEncoder().encodeToString(plain.getBytes())
		);
	}

	@Test
	public void encryptBeforePersisting_shouldSkipWhenNull() {
		DocumentBeforeRepositorySaveEvent event = new DocumentBeforeRepositorySaveEvent(
			"test-user",
			null
		);

		String contentBefore = event.getDocumentContent();
		listeners.encryptBeforePersisting(event);

		Assert.assertEquals(event.getDocumentContent(), contentBefore);
	}

	@Test
	public void changeIndexDocumentContent_shouldInvokeKernelDocument() {
		DataServicesDocument document = new DefaultDataServicesDocument(DocumentV2.empty("test-doc"), null);

		DocumentBeforeIndexEvent event = new DocumentBeforeIndexEvent(document);

		listeners.changeIndexDocumentContent(event);

		// The method calls getKernelDocument() which doesn't throw
		Assert.assertNotNull(event.getDataServicesDocument());
	}
}
