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
package com.mgmtp.a12.dataservices.query.validation.internal.matching;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.annotation.QueryOperatorValidator;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.validation.IQueryOperatorValidator;
import com.mgmtp.a12.dataservices.query.validation.ValidationItem;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@QueryOperatorValidator(ExactMatchOperator.class)
@Component public class ExactMatchOperatorValidator implements IQueryOperatorValidator {

	private final DataServicesCoreProperties dataServicesCoreProperties;

	@Override public @NonNull Collection<ValidationItem> validate(ILogicOperator operator, String parentDocumentModel, String[] path, QueryContext context, boolean validationEnabled) {
		if (validationEnabled && operator instanceof ExactMatchOperator<?> exactMatchOperator) {
			String operatorName = context.getOperatorName(operator);
			boolean hasValue = exactMatchOperator.getValue() != null;
			boolean hasValues = !CollectionUtils.isEmpty(exactMatchOperator.getValues());

			if (!hasValue && !hasValues) {
				return List.of(ValidationItem.invalid(path, "Please provide value or values for %s operator.".formatted(operatorName)));
			}
			if (hasValue && hasValues) {
				return List.of(ValidationItem.invalid(path, "Please provide either value or values, not both, for %s operator.".formatted(operatorName)));
			}
			if (hasValues) {
				if (exactMatchOperator.getValues().stream().anyMatch(Objects::isNull)) {
					return List.of(ValidationItem.invalid(path, "Please ensure none of the values are null for %s operator.".formatted(operatorName)));
				}
				int maxValuesCount = dataServicesCoreProperties.getQuery().getExactMatch().getMaxValuesCount();
				if (exactMatchOperator.getValues().size() > maxValuesCount) {
					return List.of(ValidationItem.invalid(path, "Please reduce the number of values to a value lower than or equal to %s for the %s operator.".formatted(maxValuesCount, operatorName)));
				}
			}
			return List.of(ValidationItem.valid(path, "Validation passed for operator %s".formatted(operatorName)));
		}
		return Collections.emptyList();
	}
}
