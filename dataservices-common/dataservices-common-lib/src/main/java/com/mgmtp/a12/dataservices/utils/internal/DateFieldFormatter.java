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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

import org.apache.commons.lang3.ObjectUtils;

import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType;

public class DateFieldFormatter implements FieldFormatter {

	private final IDateType fieldType;

	public DateFieldFormatter(IDateType fieldType) {
		this.fieldType = fieldType;
	}

	@Override public String format(Object value, String format, TimeZone timeZone) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		DateTimeFormatter isoLocalDateTimeFormatter = format != null ? DateTimeFormatter.ofPattern(format) : DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		if (fieldType.arePartiallyKnownDatesAllowed() && value instanceof String partialDateString) {
			return DateTimeUtils.formatPartialDate(isoLocalDateTimeFormatter, partialDateString, timeZone);
		} else {
			LocalDateTime localDateTime = LocalDateTime.from(DateTimeUtils.getTemporalAccessor(value, timeZone)).truncatedTo(ChronoUnit.SECONDS);
			return DateTimeUtils.format(isoLocalDateTimeFormatter, localDateTime);
		}
	}
}
