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
package com.mgmtp.a12.dataservices.query.validation.internal.range;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.annotation.QueryOperatorValidator;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.NumericRangeOperator;
import com.mgmtp.a12.dataservices.query.validation.IQueryOperatorValidator;
import com.mgmtp.a12.dataservices.query.validation.ValidationItem;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@QueryOperatorValidator(NumericRangeOperator.class)
@Component public class NumericRangeOperatorValidator implements IQueryOperatorValidator {

	@Override public @NonNull Collection<ValidationItem> validate(ILogicOperator operator, String parentDocumentModel, String[] path, QueryContext context, boolean validationEnabled) {
		if (validationEnabled && operator instanceof NumericRangeOperator<?> numericRangeOperator) {
			// Skip validation if parentDocumentModel is null (e.g., due to invalid role in link constraint)
			// The LinkAwareValidator will report the specific role validation error
			if (parentDocumentModel == null) {
				return Collections.emptyList();
			}
			List<ValidationItem> issues = new ArrayList<>();
			if (numericRangeOperator.getFrom() == null && numericRangeOperator.getTo() == null) {
				issues.add(ValidationItem.invalid(path, "Please provide `from` or `to` or both in %s operator.".formatted(context.getOperatorName(operator))));
			}
			if (numericRangeOperator.getFrom() != null && numericRangeOperator.getTo() != null && isFromAfterTo(numericRangeOperator)) {
				issues.add(ValidationItem.invalid(path, "In %s operator `from` cannot be bigger than `to`.".formatted(context.getOperatorName(operator))));
			}
			return issues.isEmpty() ? List.of(ValidationItem.valid(path, "Validation passed for operator %s".formatted(context.getOperatorName(operator)))) : issues;

		}
		return Collections.emptyList();
	}

	private static boolean isFromAfterTo(NumericRangeOperator<?> numericRangeOperator) {
		return new BigDecimal(String.valueOf(numericRangeOperator.getFrom())).compareTo(new BigDecimal(String.valueOf(numericRangeOperator.getTo()))) > 0;
	}
}
