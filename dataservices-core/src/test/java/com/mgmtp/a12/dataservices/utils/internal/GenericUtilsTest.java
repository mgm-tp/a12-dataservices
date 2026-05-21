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

import java.util.List;

import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class GenericUtilsTest {

	@Test public void testIsSingleAsteriskInList() {
		assertTrue(GenericUtils.isSingleAsterisk(List.of(DataServicesCoreProperties.MATCH_ALL)));
	}

	@Test public void testAsteriskWithAdditionalString() {
		assertFalse(GenericUtils.isSingleAsterisk(List.of(DataServicesCoreProperties.MATCH_ALL, "Hello")));
	}

	@Test public void testEmptyListForAsterisk() {
		assertFalse(GenericUtils.isSingleAsterisk(List.of()));
	}

	@Test public void testNoAsteriskInList() {
		assertFalse(GenericUtils.isSingleAsterisk(List.of("Hello", "Hello2")));
	}

	@Test public void testMatchOrAllEmptyListReturnsFalse() {
		assertFalse(GenericUtils.matchOrAll("anything", List.of()));
	}

	@Test public void testMatchOrAllSingleAsteriskReturnsTrueForAnyValue() {
		assertTrue(GenericUtils.matchOrAll("anything", List.of(DataServicesCoreProperties.MATCH_ALL)));
	}

	@Test public void testMatchOrAllAsteriskWithAdditionalValuesBehavesAsExact() {
		// "*" je považována za přesnou hodnotu, ne wildcard, když je v seznamu více hodnot
		assertTrue(GenericUtils.matchOrAll(DataServicesCoreProperties.MATCH_ALL, List.of(DataServicesCoreProperties.MATCH_ALL, "Hello")));
		assertTrue(GenericUtils.matchOrAll("Hello", List.of(DataServicesCoreProperties.MATCH_ALL, "Hello")));
		assertFalse(GenericUtils.matchOrAll("World", List.of(DataServicesCoreProperties.MATCH_ALL, "Hello")));
	}

	@Test public void testMatchOrAllExactMatch() {
		assertTrue(GenericUtils.matchOrAll("A", List.of("A", "B")));
	}

	@Test public void testMatchOrAllNoMatch() {
		assertFalse(GenericUtils.matchOrAll("C", List.of("A", "B")));
	}
}
