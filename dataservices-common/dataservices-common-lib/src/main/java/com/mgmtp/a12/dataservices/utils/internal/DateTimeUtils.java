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
package com.mgmtp.a12.dataservices.utils.internal;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.kernel.md.facade.a12internal.KernelUtils;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateFragmentType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateRangeType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeUtils {

	public static final String DATE_RANGE_SEPARATOR = "/";
	public static final String STANDARD_TIME_ZONE = "UTC";
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
			throw new InvalidInputException(ExceptionKeys.TIME_FORMAT_ERROR_KEY, String.format("%s is not supported date value", v.getClass().getName()));
		}
		return ta;
	}

	public static String format(DateTimeFormatter formatter, TemporalAccessor v) {
		try {
			return formatter.format(v);
		} catch (Exception e) {
			throw new InvalidInputException(ExceptionKeys.TIME_FORMAT_ERROR_KEY, String.format("Date [%s] cannot be converted to string with format [%s].", v, formatter), e);
		}
	}

	public static String formatPartialDate(DateTimeFormatter dateTimeFormatter, String partialDateString, TimeZone timeZone) {
		Date approximatedDate = KernelUtils.getApproximatedDate(partialDateString, timeZone);
		return format(dateTimeFormatter, approximatedDate.toInstant().atZone(timeZone.toZoneId()));
	}

	public static String transformToIsoFormat(String format, String value, String timeZone) {
		Object o = KernelUtils.parseDate(value, format, BASE_YEAR, false, TimeZone.getTimeZone(timeZone));
		TemporalAccessor temporalAccessor = DateTimeUtils.getTemporalAccessor(o, TimeZone.getTimeZone(STANDARD_TIME_ZONE));
		return format(DateTimeFormatter.ISO_LOCAL_DATE_TIME, temporalAccessor);
	}

	public static String transformToIsoFormat(LocalDate localDate) {
		return format(DateTimeFormatter.ISO_LOCAL_DATE, localDate);
	}

	public static String transformPartialDateToIsoFormat(String value, String timeZone) {
		return formatPartialDate(DateTimeFormatter.ISO_LOCAL_DATE_TIME, value, TimeZone.getTimeZone(timeZone));
	}

	/**
	 * Primary intention of this method is to parse partial dates for search queries.
	 *
	 * @param arePartiallyKnownDatesAllowed If partially specified dates are allowed in target field.
	 * @param timeZone The time zone used.
	 * @param value The date value.
	 * @param effectiveType The field type.
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

	public static  String getDateFormat(IDateType type) {
		return type.getFormat();
	}

	public static  String getDateRangeFormat(IDateRangeType type) {
		return type.getFormat();
	}

	public static  String getDateFragmentFormat(IDateFragmentType type) {
		return type.getFormatOfFragment();
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
			throw new InvalidInputException(String.format("Date parsing is not supported for %s type.", date.getClass().getName()));
		}
	}

	public static Optional<String> getFieldFormat(IFieldType fieldType) {
		try {
			return Optional.of(getDateTimeFormat(fieldType));
		} catch (UnexpectedException e) {
			return Optional.empty();
		}
	}

	private static String getDateTimeFormat(IFieldType effectiveType) {
		if (effectiveType instanceof IDateType iDateType) {
			return getDateFormat(iDateType);
		} else if (effectiveType instanceof IDateRangeType iDateRangeType) {
			return getDateRangeFormat(iDateRangeType);
		} else if (effectiveType instanceof IDateFragmentType iDateFragmentType) {
			return getDateFragmentFormat(iDateFragmentType);
		} else {
			throw new UnexpectedException(String.format("Unsupported field type for date/time parsing: %s", effectiveType.getClass().getName()));
		}
	}
}
