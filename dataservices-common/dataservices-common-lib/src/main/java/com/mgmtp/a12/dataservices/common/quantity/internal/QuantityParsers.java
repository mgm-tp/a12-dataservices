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
package com.mgmtp.a12.dataservices.common.quantity.internal;

import java.util.Arrays;
import java.util.stream.Stream;

import javax.measure.BinaryPrefix;
import javax.measure.MetricPrefix;
import javax.measure.Unit;
import javax.measure.quantity.Time;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tech.units.indriya.format.SimpleQuantityFormat;
import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.unit.Units;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuantityParsers {

	public static final SimpleQuantityFormat QUANTITY_FORMAT = SimpleQuantityFormat.getInstance();
	public static final SimpleUnitFormat UNIT_FORMAT = SimpleUnitFormat.getInstance();

	static {
		UNIT_FORMAT.label(DataQuantity.BYTE, "B");
		Stream.of(MetricPrefix.values(), BinaryPrefix.values())
			.flatMap(Arrays::stream)
			.forEach(m -> {
				Unit<DataQuantity> prefix = DataQuantity.BYTE.prefix(m);
				UNIT_FORMAT.label(prefix, prefix.toString());
			});
	}

	/**
	 * Method is used for transfer String size to number of bytes
	 *
	 * @param size input string for transferring to long, input case is insensitive
	 * @return size of input String to byte
	 * @example `1 MiB` = `1024 KiB` =>  1_048_576L; `1 MB` = `1000 kB` => 1_000_000L
	 */
	public static long parseDataQuantity(String size) {

		return QUANTITY_FORMAT.parse(size)
			.asType(DataQuantity.class)
			.to(DataQuantity.BYTE)
			.getValue()
			.longValue();
	}

	/**
	 * Method is used for transfer String duration to number of seconds
	 *
	 * @param duration input string for transferring to long number, representing duration in seconds. Input case is insensitive.
	 * @return time in seconds transferred from input
	 * @example `"1 wk"` = `"7 d"` = `"168 h"` = `"10080 min"` = `"604800 s"` => 604_800L
	 */
	public static long parseTimeQuantity(String duration) {
		return QUANTITY_FORMAT.parse(duration)
			.asType(Time.class)
			.to(Units.SECOND)
			.getValue()
			.longValue();
	}
}
