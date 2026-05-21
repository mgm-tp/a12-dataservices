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
package com.mgmtp.a12.dataservices.document.internal;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.authorization.internal.UaaConnector;
import com.mgmtp.a12.dataservices.document.DataServicesDocumentFactory;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.IDataServicesDocumentMetadataExtractor;
import com.mgmtp.a12.dataservices.utils.internal.DocumentUtils;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_NAME;

@RequiredArgsConstructor
@Component public class DefaultDataServicesDocumentFactory implements DataServicesDocumentFactory {

	private final IDataServicesDocumentMetadataExtractor dataServicesDocumentMetadataExtractor;
	private final MetadataUtils metadataUtils;
	private final DocumentUtils documentUtils;

	@Override public DefaultDataServicesDocument newDataServicesDocument(DocumentV2 document) {
		if (document.group(DOCUMENT_METADATA_GROUP_NAME) == null) {
			DocumentReference documentReference = documentUtils.generateDocRef(document);
			document = metadataUtils.createDocumentMetadata(
					document,
					documentReference,
					UaaConnector.getCurrentUserName(),
					Instant.now(),
					null)
				.withId(documentReference.getDocumentId());
		}
		return new DefaultDataServicesDocument(document, dataServicesDocumentMetadataExtractor.getMetadata(document));
	}

}
