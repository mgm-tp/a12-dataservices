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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateTimeType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.ITimeType;
import com.mgmtp.a12.model.header.Annotation;

import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.model.internal.TimeFormatUtils.BASE_YEAR;
import static org.testng.Assert.assertEquals;

@Slf4j
public class TimeFormatUtilsTest {

	@DataProvider
	public static Object[][] completeDateTimeProvider() {
		return new Object[][] {
			{ "2021-02-14T16:54:45", "2021-02-14T16:54:45", dateTimeType() },
			{ "2021-02-14", "2021-02-14", dateType() },
			{ "16:54:45", "16:54:45", timeType() },
		};
	}

	@DataProvider
	public static Object[][] partiallyKnownDateProvider() {
		return new Object[][] {
			{ "2021-02-00", "2021-02-01", dateType() },
			{ "2021-00-00", "2021-01-01", dateType() },
			{ "0000-00-00", "1970-01-01", dateType() },
		};
	}

	@Test(dataProvider = "completeDateTimeProvider")
	public void testFormat(String actual, String expected, IFieldType type) {
		assertEquals(
			TimeFormatUtils.format(
				DateTimeFormatter.ofPattern(TimeFormatUtils.getDateTimeFormat(type)),
				parse(type, actual)),
			expected
		);
	}

	@Test(dataProvider = "completeDateTimeProvider")
	public void testParseComplete(String actual, String expected, IFieldType type) {
		assertEquals(TimeFormatUtils.parse(actual, type, BASE_YEAR, false, TimeZone.getTimeZone(ZoneId.systemDefault())).toEpochMilli(),
			parse(type, expected).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
	}

	@Test(dataProvider = "completeDateTimeProvider")
	public void testParseCompleteOptional(String actual, String expected, IFieldType type) {
		assertEquals(TimeFormatUtils.parse(actual, type, BASE_YEAR, true, TimeZone.getTimeZone(ZoneId.systemDefault())).toEpochMilli(),
			parse(type, expected).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
	}

	@Test(dataProvider = "partiallyKnownDateProvider", expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "java.text.ParseException: Unparseable date: \"[0-9]{4}-[0-9]{2}-00\"")
	public void testParseIncompleteFail(String actual, String expected, IFieldType type) {
		assertEquals(TimeFormatUtils.parse(actual, type, BASE_YEAR, false, TimeZone.getTimeZone(ZoneId.systemDefault())).toEpochMilli(),
			parse(type, expected).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
	}

	@Test(dataProvider = "partiallyKnownDateProvider")
	public void testParsePartiallyKnown(String actual, String expected, IFieldType type) {
		assertEquals(TimeFormatUtils.parse(actual, type, BASE_YEAR, true, TimeZone.getTimeZone(ZoneId.systemDefault())).toEpochMilli(),
			parse(type, expected).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
	}

	private LocalDateTime parse(IFieldType type, String expected) {
		if (type instanceof IDateTimeType) {
			return LocalDateTime.parse(expected);
		} else if (type instanceof ITimeType) {
			return LocalTime.parse(expected).atDate(LocalDate.ofEpochDay(0));
		} else {
			return LocalDate.parse(expected).atStartOfDay();
		}
	}

	private static IFieldType dateType() {
		return new IDateType() {

			@Override
			public List<Annotation> getAnnotations() {
				return null;
			}

			// needed only to satisfy interface, not used in tests
			@Override
			public void setAnnotations(List<Annotation> annotations) {
			}

			@Override
			public String getFormat() {
				return "yyyy-MM-dd";
			}

			@Override
			public boolean isYoungerThan1900Check() {
				return false;
			}

			public DatePrecision getDatePrecision() {
				return IDateType.DatePrecision.YEAR_OPTIONAL;
			}

			@Override
			public Optional<String> getNotInDCustomFormat() {
				return Optional.empty();
			}
		};
	}

	private static IFieldType dateTimeType() {
		return new IDateTimeType() {
			@Override
			public List<Annotation> getAnnotations() {
				return null;
			}

			// needed only to satisfy interface, not used in tests
			@Override
			public void setAnnotations(List<Annotation> annotations) {

			}

			@Override
			public String getFormat() {
				return "yyyy-MM-dd'T'HH:mm:ss";
			}

			@Override
			public boolean isYoungerThan1900Check() {
				return false;
			}

			@Override
			public DatePrecision getDatePrecision() {
				return null;
			}

			@Override
			public boolean arePartiallyKnownDatesAllowed() {
				return IDateTimeType.super.arePartiallyKnownDatesAllowed();
			}

			@Override
			public Optional<String> getNotInDCustomFormat() {
				return Optional.empty();
			}
		};
	}

	private static IFieldType timeType() {
		return new ITimeType() {
			@Override
			public String getFormat() {
				return "HH:mm:ss";
			}

			@Override
			public boolean isYoungerThan1900Check() {
				return false;
			}

			@Override
			public DatePrecision getDatePrecision() {
				return null;
			}

			@Override
			public Optional<String> getNotInDCustomFormat() {
				return Optional.empty();
			}

			@Override
			public List<Annotation> getAnnotations() {
				return null;
			}

			// needed only to satisfy interface, not used in tests
			@Override
			public void setAnnotations(List<Annotation> annotations) {

			}
		};
	}
}
