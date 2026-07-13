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
package com.mgmtp.a12.dataservices.query.internal;

import java.time.LocalDate;
import java.util.Date;
import java.util.TimeZone;

import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.DateRangeOperator;
import com.mgmtp.a12.dataservices.query.enrichement.Enrichments;
import com.mgmtp.a12.dataservices.utils.internal.DateTimeUtils;
import com.mgmtp.a12.kernel.md.facade.a12internal.KernelUtils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateFragmentType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateRangeType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.QUERY_INVALID_INPUT_ERROR_KEY;
import static com.mgmtp.a12.dataservices.query.internal.QueryTopologyHelper.getEffectiveFieldType;
import static com.mgmtp.a12.dataservices.utils.internal.DateTimeUtils.transformToIsoFormat;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EnrichmentHelper {

	public static void enrichDateRangeOperator(DateRangeOperator dateRangeOperator, IField field, IDocumentModel documentModel, QueryContext context, ExceptionKeys.ExecutionPhase executionPhase) {
		IFieldType effectiveFieldType = getEffectiveFieldType(field, executionPhase);
		if (effectiveFieldType instanceof IDateFragmentType dateFragmentType) {
			enrichDateRangeOperator(documentModel, dateRangeOperator, field, dateFragmentType.getFormatOfFragment(), false, true, false, context, executionPhase);
			dateRangeOperator.setRangeType(false);
		} else if (effectiveFieldType instanceof IDateRangeType dateRangeType) {
			enrichDateRangeOperator(documentModel, dateRangeOperator, field, dateRangeType.getFormat(), false, false, true, context, executionPhase);
			dateRangeOperator.setRangeType(true);
		} else if (effectiveFieldType instanceof IDateType dateType) {
			if (dateType.arePartiallyKnownDatesAllowed()) {
				enrichDateRangeOperator(documentModel, dateRangeOperator, null, dateType.getFormat(), true, false, false, context, executionPhase);
			} else {
				enrichDateRangeOperator(documentModel, dateRangeOperator, field, dateType.getFormat(), false, false, false, context, executionPhase);
			}
			dateRangeOperator.setRangeType(false);
		} else {
			String msg = "%s not allowed on field of type %s.".formatted(dateRangeOperator.getClass().getSimpleName(),
				effectiveFieldType.getClass().getSimpleName());
			log.error(msg);
			throw new QueryInvalidInputException(executionPhase, QUERY_INVALID_INPUT_ERROR_KEY, msg);
		}
	}

	private static void enrichDateRangeOperator(IDocumentModel documentModel, DateRangeOperator rangeOperator, IField field, String format,
		boolean isPartialDate, boolean isDateFragment, boolean isDateRangeType, QueryContext context, ExceptionKeys.ExecutionPhase executionPhase) {
		String timeZone = documentModel.getContent().getDocumentModelConfig().getTimeZone();

		String value = rangeOperator.getValue();
		// Handle ISO 8601 interval format (e.g., "2015-01-01/2020-12-31") only for IDateRangeType fields
		if (isDateRangeType && value != null && value.contains(DateTimeUtils.DATE_RANGE_SEPARATOR)) {
			// Split into from/to components. Using limit=2 guarantees exactly 2 parts when separator is present.
			String[] parts = value.split(DateTimeUtils.DATE_RANGE_SEPARATOR, 2);
			enrichDate(Enrichments.FROM_PROPERTY, documentModel, rangeOperator, field,
				parts[0].isEmpty() ? null : parts[0], format, isPartialDate, isDateFragment, timeZone, context, executionPhase);
			enrichDate(Enrichments.TO_PROPERTY, documentModel, rangeOperator, field,
				parts[1].isEmpty() ? null : parts[1], format, isPartialDate, isDateFragment, timeZone, context, executionPhase);
		} else {
			// Original behavior for non-interval values or non-DateRangeType fields
			enrichDate(Enrichments.VALUE_PROPERTY, documentModel, rangeOperator, field, value, format,
				isPartialDate, isDateFragment, timeZone, context, executionPhase);
		}

		// Enrich from/to from explicit properties if not already set from interval parsing above.
		// The enrichDate method uses computeIfAbsent, so interval-derived values take precedence.
		enrichDate(Enrichments.FROM_PROPERTY, documentModel, rangeOperator, field, rangeOperator.getFrom(), format,
			isPartialDate, isDateFragment, timeZone, context, executionPhase);
		enrichDate(Enrichments.TO_PROPERTY, documentModel, rangeOperator, field, rangeOperator.getTo(), format,
			isPartialDate, isDateFragment, timeZone, context, executionPhase);
	}

	public static void enrichDate(String propertyName, IDocumentModel documentModel, ILogicOperator rangeOperator, IField field, String value, String format,
		boolean isPartialDate,
		boolean isDateFragment, String timeZone, QueryContext context, ExceptionKeys.ExecutionPhase executionPhase) {

		if (value != null) {
			context.getEnrichments().getOperatorEnrichment(rangeOperator)
				.computeIfAbsent(propertyName, k -> computeEnrichedDateValue(documentModel, value, field, format, timeZone, isPartialDate, isDateFragment, executionPhase));
		}
	}

	private static String computeEnrichedDateValue(IDocumentModel documentModel, String value, IField field, String format, String timeZone,
		boolean isPartialDate, boolean isDateFragment, ExceptionKeys.ExecutionPhase executionPhase) {
		if (timeZone == null) {
			throw new QueryInvalidInputException(executionPhase, QUERY_INVALID_INPUT_ERROR_KEY, "Cannot parsed date without timezone");
		}
		try {
			if (isPartialDate) {
				return DateTimeUtils.transformPartialDateToIsoFormat(value, timeZone);
			} else if (isDateFragment) {
				Date date = KernelUtils.parseDate(value, field, documentModel);
				LocalDate localDate = LocalDate.ofInstant(date.toInstant(), TimeZone.getTimeZone(timeZone).toZoneId());
				return transformToIsoFormat(localDate);
			} else {
				return transformToIsoFormat(format, value, timeZone);
			}
		} catch (IllegalArgumentException e) {
			throw new QueryInvalidInputException(executionPhase, QUERY_INVALID_INPUT_ERROR_KEY, "Could not parse date from [%s]".formatted(value), e);
		}
	}
}
