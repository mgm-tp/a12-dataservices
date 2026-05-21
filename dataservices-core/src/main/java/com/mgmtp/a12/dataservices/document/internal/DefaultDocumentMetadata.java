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
import java.util.List;
import java.util.Optional;

import javax.annotation.concurrent.Immutable;

import com.mgmtp.a12.dataservices.document.DataServicesDocumentMetadata;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.dataservices.utils.internal.KernelUtils;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.RequiredArgsConstructor;

@Immutable
@RequiredArgsConstructor
public class DefaultDocumentMetadata implements DataServicesDocumentMetadata {

	private final DocumentV2 document;

	@Override public DocumentReference getDocRef() {
		return getStringValue(DocumentMetadataConstants.DOC_REF_METADATA_NAME)
			.map(DocumentReference::new)
			.orElse(null);
	}

	public String getDocumentModelReference() {
		return getStringValue(DocumentMetadataConstants.MODEL_REFERENCE_METADATA_NAME).orElse(null);
	}

	public String getDocumentModelVersion() {
		return getStringValue(DocumentMetadataConstants.MODEL_VERSION_METADATA_NAME).orElse(null);
	}

	public String getCreator() {
		return getStringValue(DocumentMetadataConstants.CREATOR_METADATA_NAME).orElse(null);
	}

	public String getModifier() {
		return getStringValue(DocumentMetadataConstants.MODIFIER_METADATA_NAME).orElse(null);
	}

	public Instant getCreatedAt() {
		return getInstantValue(DocumentMetadataConstants.CREATED_AT_METADATA_NAME).orElse(null);
	}

	public Instant getModifiedAt() {
		return getInstantValue(DocumentMetadataConstants.MODIFIED_AT_METADATA_NAME).orElse(null);
	}

	private Optional<String> getStringValue(String fieldName) {
		return getTypedValue(fieldName, List.of(1, 1), String.class);
	}

	private Optional<Instant> getInstantValue(String fieldName) {
		return getTypedValue(fieldName, List.of(1, 1), Instant.class);
	}

	private <T> Optional<T> getTypedValue(String fieldName, List<Integer> repetitions, Class<T> type) {
		return Optional.of(
			type.cast(
				document.fieldValue(KernelUtils.of(List.of(DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_NAME, fieldName), repetitions))
			)
		);
	}
}
