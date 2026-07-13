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
package com.mgmtp.a12.dataservices.utils;

import java.util.List;

import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.utils.internal.GenericUtils;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for `ConfigurationParsingUtils`.
 *
 * The tests delegate through `GenericUtils` which forwards calls to `ConfigurationParsingUtils`,
 * so these tests exercise the parsing logic indirectly.
 */
public class ConfigurationParsingUtilsTest {

	@Test(enabled = true, description = "Should return true when list contains only asterisk")
	public void shouldReturnTrueWhenListContainsSingleAsterisk() {
		assertTrue(GenericUtils.isSingleAsterisk(List.of(DataServicesCoreProperties.MATCH_ALL)));
	}

	@Test(enabled = true, description = "Should return false when list contains asterisk among other values")
	public void shouldReturnFalseWhenAsteriskIsNotAlone() {
		assertFalse(GenericUtils.isSingleAsterisk(List.of(DataServicesCoreProperties.MATCH_ALL, "otherValue")));
	}

	@Test(enabled = true, description = "Should return false when list is empty")
	public void shouldReturnFalseWhenListIsEmpty() {
		assertFalse(GenericUtils.isSingleAsterisk(List.of()));
	}

	@Test(enabled = true, description = "Should return false when list is null")
	public void shouldReturnFalseWhenListIsNull() {
		assertFalse(GenericUtils.isSingleAsterisk(null));
	}

	@Test(enabled = true, description = "Should match when value is in list")
	public void shouldMatchWhenValueIsInList() {
		assertTrue(GenericUtils.matchOrAll("alpha", List.of("alpha", "beta")));
	}

	@Test(enabled = true, description = "Should match all when list is single asterisk")
	public void shouldMatchAllWhenListIsSingleAsterisk() {
		assertTrue(GenericUtils.matchOrAll("anything", List.of(DataServicesCoreProperties.MATCH_ALL)));
	}

	@Test(enabled = true, description = "Should not match when value is not in list and list has no asterisk")
	public void shouldNotMatchWhenValueAbsentAndNoWildcard() {
		assertFalse(GenericUtils.matchOrAll("gamma", List.of("alpha", "beta")));
	}

	@Test(enabled = true, description = "Should warn and treat asterisk as literal when mixed with other values")
	public void shouldTreatAsteriskAsLiteralWhenMixedWithOtherValues() {
		// When "*" appears alongside other entries it is treated as a literal value, not a wildcard.
		// A log warning is emitted; the lookup falls back to exact matching.
		assertTrue(GenericUtils.matchOrAll(DataServicesCoreProperties.MATCH_ALL, List.of(DataServicesCoreProperties.MATCH_ALL, "alpha")));
		assertTrue(GenericUtils.matchOrAll("alpha", List.of(DataServicesCoreProperties.MATCH_ALL, "alpha")));
		assertFalse(GenericUtils.matchOrAll("beta", List.of(DataServicesCoreProperties.MATCH_ALL, "alpha")));
	}
}
