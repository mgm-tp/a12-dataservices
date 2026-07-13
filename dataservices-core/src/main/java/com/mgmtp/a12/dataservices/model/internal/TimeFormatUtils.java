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
package com.mgmtp.a12.dataservices.model.internal;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.TimeZone;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.kernel.md.facade.a12internal.KernelUtils;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateFragmentType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateRangeType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateTimeType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.ITimeType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeFormatUtils {

	public static final int BASE_YEAR = 2000;

	public static TemporalAccessor getTemporalAccessor(Object v, TimeZone timeZone) {
		TemporalAccessor ta;
		if (v instanceof Date date) {
			ta = date.toInstant().atZone(timeZone.toZoneId());
		} else if (v instanceof Instant instant) {
			ta = instant.atZone(timeZone.toZoneId());
		} else if (v instanceof TemporalAccessor temporalAccessor) {
			ta = temporalAccessor;
		} else {
			throw new InvalidInputException(ExceptionKeys.TIME_FORMAT_ERROR_KEY, "%s is not supported date value".formatted(v.getClass().getName()));
		}
		return ta;
	}

	public static String format(DateTimeFormatter formatter, TemporalAccessor v) {
		try {
			return formatter.format(v);
		} catch (Exception e) {
			throw new InvalidInputException(ExceptionKeys.TIME_FORMAT_ERROR_KEY, "Date [%s] cannot be converted to string with format [%s].".formatted(v, formatter), e);
		}
	}

	/**
	 * Primary intention of this method is to parse partial dates for search queries.
	 *
	 * @param arePartiallyKnownDatesAllowed
	 * @param timeZone
	 * @param value date value
	 * @param effectiveType
	 * @return millis since epoch
	 * @see Instant#toEpochMilli()
	 */
	public static Instant parse(String value, IFieldType effectiveType, int baseYear, boolean arePartiallyKnownDatesAllowed, TimeZone timeZone) {
		if (effectiveType instanceof IDateType iDateType) {
			arePartiallyKnownDatesAllowed = iDateType.arePartiallyKnownDatesAllowed() && arePartiallyKnownDatesAllowed;
		}
		return handlePartiallyKnownDates(
			KernelUtils.parseDate(value, getDateTimeFormat(effectiveType), baseYear, arePartiallyKnownDatesAllowed, timeZone), timeZone);
	}

	public static String getDateTimeFormat(IFieldType effectiveType) {
		String dateFormat;
		if (effectiveType instanceof IDateType iDateType) {
			dateFormat = iDateType.getFormat();
		} else if (effectiveType instanceof IDateRangeType iDateRangeType) {
			dateFormat = iDateRangeType.getFormat();
		} else if (effectiveType instanceof IDateFragmentType iDateFragmentType) {
			dateFormat = iDateFragmentType.getFormatOfFragment();
		} else {
			throw new UnexpectedException("Unsupported field type for date/time parsing: %s".formatted(effectiveType.getClass().getName()));
		}
		return dateFormat;
	}

	public static Instant handlePartiallyKnownDates(Object date, TimeZone timeZone) {
		if (date instanceof String str) {
			date = KernelUtils.getApproximatedDate(str, timeZone);
		}
		if (date instanceof Date d) {
			return d.toInstant();
		} else if (date instanceof Instant instant) {
			return instant;
		} else {
			throw new InvalidInputException(ExceptionKeys.TIME_FORMAT_ERROR_KEY, "Date parsing is not supported for %s type.".formatted(date.getClass().getName()));
		}
	}

	/**
	 * CAUTION: This method is used just for creating exception or error message content.
	 */
	public static String getDateTypeForLogging(String fieldName, IFieldType dataType) {
		String dataTypeString;
		if (dataType instanceof ITimeType) {
			dataTypeString = "Time";
		} else if (dataType instanceof IDateTimeType) {
			dataTypeString = "DateTime";
		} else if (dataType instanceof IDateRangeType) {
			dataTypeString = "DateRange";
		} else {
			dataTypeString = "Date";
		}

		return "Filter [%s] was expected to have format [%s] because it is of data type [%s] for value: ".formatted(fieldName, getDateTimeFormat(dataType),
			dataTypeString);
	}

}
