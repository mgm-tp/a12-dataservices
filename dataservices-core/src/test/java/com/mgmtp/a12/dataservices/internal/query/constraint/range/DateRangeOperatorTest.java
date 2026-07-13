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
package com.mgmtp.a12.dataservices.internal.query.constraint.range;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.internal.query.constraint.AbstractILogicOperatorTest;
import com.mgmtp.a12.dataservices.query.constraint.range.DateRangeOperator;

@Test public class DateRangeOperatorTest extends AbstractILogicOperatorTest {

	// test methods are inherited from parent AbstractILogicOperatorTest.
	@DataProvider public static Object[][] operatorProvider() {
		return new Object[][] {
			/* TIMESTAMP RANGE ****************************************************************************************/
			new Object[] { "Timestamp range", """
	{
	    "operator": "date_range",
	    "field": "/RootGroup/SomeField",
	    "from": "2023-11-26T14:54:45.000",
	    "to": "2023-12-24T17:00:00.000"
	}
	""", DateRangeOperator
				.builder()
				.field("/RootGroup/SomeField")
				.from("2023-11-26T14:54:45.000")
				.to("2023-12-24T17:00:00.000")
				.build() },

			/* TIME RANGE ****************************************************************************************/
			new Object[] { "Time range", """
	{
	    "operator": "date_range",
	    "field": "/RootGroup/SomeField",
	    "from": "14:54:45.000",
	    "to": "17:00:00.000"
	}
	""", DateRangeOperator
				.builder()
				.field("/RootGroup/SomeField")
				.from("14:54:45.000")
				.to("17:00:00.000")
				.build() },

			/* DATE RANGE ****************************************************************************************/
			new Object[] { "Date range", """
	{
	    "operator": "date_range",
	    "field": "/RootGroup/SomeField",
	    "from": "2023-11-26T14:54:45.000",
	    "to": "2023-12-24T17:00:00.000"
	}
	""", DateRangeOperator
				.builder()
				.field("/RootGroup/SomeField")
				.from("2023-11-26T14:54:45.000")
				.to("2023-12-24T17:00:00.000")
				.build() },

			/* DATE RANGE ****************************************************************************************/
			new Object[] { "Date range", """
	{
	    "operator": "date_range",
	    "field": "/RootGroup/SomeField",
	    "from": "2023-11-26",
	    "to": "2023-12-24"
	}
	""", DateRangeOperator
				.builder()
				.field("/RootGroup/SomeField")
				.from("2023-11-26")
				.to("2023-12-24")
				.build() },

			/* TIMESTAMP RANGE ****************************************************************************************/
			new Object[] { "Timestamp range", """
	{
	    "operator": "date_range",
	    "field": "/RootGroup/SomeField",
	    "value": "2023-11-26T14:54:45.000",
	    "reverse": true
	}
	""", DateRangeOperator
				.builder()
				.field("/RootGroup/SomeField")
				.value("2023-11-26T14:54:45.000")
				.reverse(true)
				.build() },

			/* TIME RANGE ****************************************************************************************/
			new Object[] { "Time range", """
	{
	    "operator": "date_range",
	    "field": "/RootGroup/SomeField",
	    "value": "14:54:45.000",
	    "reverse": true
	}

	""", DateRangeOperator
				.builder()
				.field("/RootGroup/SomeField")
				.value("14:54:45.000")
				.reverse(true)
				.build() },

			/* DATE RANGE ****************************************************************************************/
			new Object[] { "Date range", """
	{
	    "operator": "date_range",
	    "field": "/RootGroup/SomeField",
	    "value": "2023-11-26T14:54:45.000",
	    "reverse": true
	}
	""", DateRangeOperator
				.builder()
				.field("/RootGroup/SomeField")
				.value("2023-11-26T14:54:45.000")
				.reverse(true)
				.build() },

			/* DATE RANGE ****************************************************************************************/
			new Object[] { "Date range", """
	{
	    "operator": "date_range",
	    "field": "/RootGroup/SomeField",
	    "value": "2023-11-26",
	    "reverse": true
	}
	""", DateRangeOperator
				.builder()
				.field("/RootGroup/SomeField")
				.value("2023-11-26")
				.reverse(true)
				.build() },
			/* DATE RANGE - ISO 8601 INTERVAL FORMAT *********************************************************/
			new Object[] { "Date range with ISO 8601 interval value", """
	{
	    "operator": "date_range",
	    "field": "/RootGroup/SomeField",
	    "value": "2015-01-01/2020-12-31"
	}
	""", DateRangeOperator
				.builder()
				.field("/RootGroup/SomeField")
				.value("2015-01-01/2020-12-31")
				.build() },

			/* DATE RANGE - Open interval (from only) *******************************************************/
			new Object[] { "Date range with open interval (from only)", """
	{
	    "operator": "date_range",
	    "field": "/RootGroup/SomeField",
	    "value": "2015-01-01/"
	}
	""", DateRangeOperator
				.builder()
				.field("/RootGroup/SomeField")
				.value("2015-01-01/")
				.build() },

			/* DATE RANGE - Open interval (to only) *********************************************************/
			new Object[] { "Date range with open interval (to only)", """
	{
	    "operator": "date_range",
	    "field": "/RootGroup/SomeField",
	    "value": "/2020-12-31"
	}
	""", DateRangeOperator
				.builder()
				.field("/RootGroup/SomeField")
				.value("/2020-12-31")
				.build() }
		};
	}

	@DataProvider public static Object[][] reverseTestProvider() {
		return new Object[][] {
			new Object[] {
				"reverse is null", false,
				DateRangeOperator.builder().build()
			},
			new Object[] {
				"reverse is false", false,
				DateRangeOperator.builder().reverse(false).build()
			},
			new Object[] {
				"reverse is true", true,
				DateRangeOperator.builder().reverse(true).build()
			},
		};
	}

	@Test(dataProvider = "reverseTestProvider")
	public void isReverseShouldReturnCorrectBoolean(String description, boolean expected, DateRangeOperator dateRangeOperator) {
		Assert.assertEquals(dateRangeOperator.isReverse(), expected);
	}
}
