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
package com.mgmtp.a12.examples.document.external.enumeration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.enumeration.external.ExternalEnumeration;
import com.mgmtp.a12.dataservices.enumeration.external.ExternalEnumerationLoader;
import com.mgmtp.a12.examples.document.storage.ram.InMemoryDocumentRepository;

import lombok.NonNull;

/**
 * External enumeration loader for the DomainProduct model.
 * Validates supported models and converts {@link com.mgmtp.a12.dataservices.document.DocumentReference} to {@link com.mgmtp.a12.dataservices.enumeration.external.ExternalEnumeration}.
 */
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.documents.external-enumeration", name = "enabled", havingValue = "true")
@Component public class BusinessPartnerExternalEnumeration implements ExternalEnumerationLoader {

	/**
	 * Indicates whether the given model is supported by this loader.
	 *
	 * @param modelName the model name to check; must not be null.
	 * @return `true` if the model equals {@link com.mgmtp.a12.examples.document.store.ram.InMemoryDocumentRepository#MODEL_NAME}, otherwise `false`.
	 */
	@Override
	public Boolean isModelSupported(@NonNull String modelName) {
		return InMemoryDocumentRepository.MODEL_NAME.equals(modelName);
	}

	/**
	 * Converts a document reference to an external enumeration entry.
	 * The enumeration uses the `toString()` representation as ID and the model name as label.
	 *
	 * @param documentReference the reference of the document to convert; must not be null.
	 * @return a new {@link com.mgmtp.a12.dataservices.enumeration.external.ExternalEnumeration} representing the document.
	 */
	@Override
	public ExternalEnumeration convertToEnumeration(@NonNull DocumentReference documentReference) {
		return new ExternalEnumeration(documentReference.toString(), documentReference.getDocumentModelName());
	}
}

