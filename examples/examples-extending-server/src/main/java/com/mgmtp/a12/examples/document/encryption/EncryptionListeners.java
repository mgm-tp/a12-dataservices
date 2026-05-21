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

import java.util.Base64;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;

import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterRepositoryLoadEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeIndexEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeRepositorySaveEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Event listeners that transparently encrypt/decrypt document content on repository boundaries, and adjust content before indexing.
 */
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.documents.encryption", name = "enabled", havingValue = "true")
@Slf4j
@Component public class EncryptionListeners {

	/**
	 * Decodes Base64-encoded document content after loading from the repository.
	 *
	 * @param documentAfterRepositoryLoadEvent the event with the content loaded from the repository; content may be null.
	 */
	@CommonDataServicesEventListener public void decryptLoadedDocuments(DocumentAfterRepositoryLoadEvent documentAfterRepositoryLoadEvent) {
		String documentContent = documentAfterRepositoryLoadEvent.getDocumentContent();
		if (documentContent != null) {
			byte[] decoded = Base64.getDecoder().decode(documentContent);
			documentAfterRepositoryLoadEvent.setDocumentContent(new String(decoded));
		}
	}

	/**
	 * Encodes document content as Base64 before persisting to the repository.
	 *
	 * @param documentBeforeRepositorySaveEvent the event providing the content to persist; content may be null.
	 */
	@CommonDataServicesEventListener public void encryptBeforePersisting(DocumentBeforeRepositorySaveEvent documentBeforeRepositorySaveEvent) {
		String documentContent = documentBeforeRepositorySaveEvent.getDocumentContent();
		if (documentContent != null) {
			String encoded = Base64.getEncoder().encodeToString(documentContent.getBytes());
			documentBeforeRepositorySaveEvent.setDocumentContent(encoded);
		}
	}

	/**
	 * Allows modification of the document content just before indexing.
	 * TODO: Clarify contract (uncertain behavior) for expected modifications and invariants.
	 *
	 * @param documentBeforeIndexEvent the event carrying the document about to be indexed; never null.
	 */
	@CommonDataServicesEventListener public void changeIndexDocumentContent(DocumentBeforeIndexEvent documentBeforeIndexEvent) {
		modifyDocumentContent(documentBeforeIndexEvent.getDataServicesDocument());
	}

	private void modifyDocumentContent(DataServicesDocument dataServicesDocument) {
		dataServicesDocument.getKernelDocument();
		// Please use this method to modify the document content before indexing.
	}
}

