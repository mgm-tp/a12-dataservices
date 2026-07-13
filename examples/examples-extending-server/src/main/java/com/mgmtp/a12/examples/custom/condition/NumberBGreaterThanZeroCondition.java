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
package com.mgmtp.a12.examples.custom.condition;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.IntegrityException;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.PartiallyKnownDocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.utils.DocumentV2Utils;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.kernel.md.rt.api.ICustomCondition;
import com.mgmtp.a12.kernel.md.rt.api.ICustomConditionFactory;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Example of custom implementation of ICustomCondition.
 *
 */
@Component @RequiredArgsConstructor
public class NumberBGreaterThanZeroCondition implements ICustomConditionFactory {

	private final DocumentServiceFactory documentServiceFactory;

	@Override
	public Optional<ICustomCondition> createCustomConditionV2(String s) {
		if (!"NumberBGreaterThanZero".equals(s)) {
			return Optional.empty();
		}

		return Optional.of(new ICustomCondition() {
			@Override
			public boolean check(@NonNull DocumentV2 document, Set<? extends DocumentMultiPointer> relevantEntities,
				@NonNull Set<DocumentPointer> formallyIncorrectEntities, @NonNull PartiallyKnownDocumentMultiPointer errorEntityInstance) {
				try {

					return DocumentV2Utils.getFieldInstances(document, "/top/numberB").stream()
						.reduce((a, b) -> {
							throw new IntegrityException(ExceptionKeys.DOCUMENT_FIELD_ERROR_KEY, "Ambiguous field %s".formatted("/top/numberB"));
						})
						.map(Map.Entry::getValue)
						.map(FieldInstanceV2::value)
						.map(BigDecimal.class::cast)
						.map(BigDecimal::intValue)
						.map(v -> v <= 0)
						.orElse(true);
				} catch (final Exception e) {
					return true;
				}
			}
		});
	}
}
