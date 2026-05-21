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
package com.mgmtp.a12.dataservices.search.customizer.internal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.searchtable.DocumentFieldEntity;
import com.mgmtp.a12.dataservices.search.customizer.DocumentFieldContext;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import io.hypersistence.utils.hibernate.type.range.Range;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter
public class DocumentFieldContextImpl implements DocumentFieldContext {

	private final IDocumentModelSearchService documentModelSearchService;
	private final DocumentReference documentReference;
	private final DocumentPointer pointerRelativeToBase;
	private final FieldInstanceV2 fieldInstance;
	private final IField field;
	private final BiFunction<String, String, Long> fieldTypeIdProvider;
	private final List<DocumentFieldEntity> additionalFields = new ArrayList<>();
	private boolean skipCoreIndexing = false;

	@Override public String getModelName() {
		return documentReference.getDocumentModelName();
	}

	@Override public String getFieldPath() {
		return pointerRelativeToBase.fullName();
	}

	@Override
	public DocumentFieldContext addField(@NonNull String fieldPath, @NonNull String value, String typedValue, BigDecimal numberValue,
		LocalDateTime timestampValue, Object tsRangeValue, String type, @NonNull String source) {

		if (source.isEmpty()) {
			throw new IllegalArgumentException("Source parameter is mandatory and must not be null or empty");
		}

		Range<LocalDateTime> rangeValue = tsRangeValue != null ? (Range<LocalDateTime>) tsRangeValue : null;

		DocumentFieldEntity entity = DocumentFieldEntity.builder()
			.docRef(documentReference.toString())
			.modelName(documentReference.getDocumentModelName())
			.fieldName(fieldPath)
			.repetitions(pointerRelativeToBase.repetitionIndexes().stream().mapToInt(Integer::intValue).toArray())
			.value(value)
			.fieldType(type)
			.fieldTypeId(fieldTypeIdProvider.apply(documentReference.getDocumentModelName(), fieldPath))
			.typedValue(typedValue)
			.numberValue(numberValue)
			.timestampValue(timestampValue)
			.tsRangeValue(rangeValue)
			.source(source)
			.build();

		additionalFields.add(entity);
		return this;
	}

	@Override public DocumentFieldContext skipCoreFieldIndexing() {
		this.skipCoreIndexing = true;
		return this;
	}

	@Override public boolean isCoreFieldIndexingSkipped() {
		return skipCoreIndexing;
	}
}
