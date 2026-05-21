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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.annotation.QueryOperatorValidator;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.DateRangeOperator;
import com.mgmtp.a12.dataservices.query.enrichement.FieldDescriptor;
import com.mgmtp.a12.dataservices.query.internal.EnrichmentHelper;
import com.mgmtp.a12.dataservices.query.validation.IQueryOperatorValidator;
import com.mgmtp.a12.dataservices.query.validation.ValidationItem;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_VALIDATION;
import static com.mgmtp.a12.dataservices.query.internal.QueryTopologyHelper.fieldTypeAsString;
import static com.mgmtp.a12.dataservices.query.internal.QueryTopologyHelper.getEffectiveFieldType;

@RequiredArgsConstructor
@QueryOperatorValidator(DateRangeOperator.class)
@Component public class DateRangeOperatorValidator implements IQueryOperatorValidator {

	private final DocumentModelUtils documentModelUtils;

	@Override
	public @NonNull Collection<ValidationItem> validate(ILogicOperator operator, String parentDocumentModel, String[] path, QueryContext context,
		boolean validationEnabled) {
		if (validationEnabled && operator instanceof DateRangeOperator dateRangeOperator) {
			if (dateRangeOperator.getFrom() == null && dateRangeOperator.getTo() == null && dateRangeOperator.getValue() == null) {
				return List.of(ValidationItem.invalid(path,
					"Please provide `value` or `from` or `to` or both `from` and `to` in %s operator.".formatted(context.getOperatorName(operator))));
			} else {
				IDocumentModel documentModel = context.getDocumentModel(parentDocumentModel);
				Optional<IField> fieldOptional = documentModelUtils.findField(documentModel, dateRangeOperator.getField());
				if (fieldOptional.isPresent()) {
					IField field = fieldOptional.get();
					FieldDescriptor fieldDescriptor = context.getEnrichments().getFieldDescriptor(dateRangeOperator.getField());
					if (fieldDescriptor.getFieldType() == null) {
						fieldDescriptor.setFieldType(fieldTypeAsString(getEffectiveFieldType(field, QUERY_VALIDATION)));
					}
					try {
						EnrichmentHelper.enrichDateRangeOperator(dateRangeOperator, field, documentModel, context, QUERY_VALIDATION);
					} catch (QueryInvalidInputException e) {
						return List.of(ValidationItem.invalid(path, e.getMessage()));
					} catch (Exception e) {
						return List.of(ValidationItem.invalid(path, "Validation failed for operator %s".formatted(context.getOperatorName(operator))));
					}
				}
			}
			return List.of(ValidationItem.valid(path, "Validation passed for operator %s".formatted(context.getOperatorName(operator))));
		}
		return Collections.emptyList();
	}
}
