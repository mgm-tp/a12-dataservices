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

import java.util.Objects;

public class SomeClass {
	private String anotherString = "anotherStringValue";
	private int anotherInt = 77;
	private SomeMoreClass anotherObject = new SomeMoreClass();

	public String getAnotherString() {
		return anotherString;
	}

	public int getAnotherInt() {
		return anotherInt;
	}

	public SomeMoreClass getAnotherObject() {
		return anotherObject;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SomeClass someClass = (SomeClass) o;
		return anotherInt == someClass.anotherInt &&
			Objects.equals(anotherString, someClass.anotherString) &&
			Objects.equals(anotherObject, someClass.anotherObject);
	}

	@Override
	public int hashCode() {
		return Objects.hash(anotherString, anotherInt, anotherObject);
	}

	@Override
	public String toString() {
		return "SomeClass{" +
			"anotherString='" + anotherString + '\'' +
			", anotherInt=" + anotherInt +
			", anotherObject=" + anotherObject +
			'}';
	}
}
