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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;

public class KernelUtilsTest {

	@Test
	public void splitFieldPath_success() {
		Assert.assertEquals(KernelUtils.splitFieldPath("/B/A"), List.of("B", "A"));
		Assert.assertEquals(KernelUtils.splitFieldPath("B/A"), List.of("B", "A"));
	}

	@Test(enabled = true, description = "lastRepetitionIsWildcard returns true when the last repetition index is 0")
	public void shouldReturnTrueWhenLastRepetitionIsZero() {
		Assert.assertTrue(KernelUtils.isLastRepetitionWildcard(new int[] { 1, 0 }));
		Assert.assertTrue(KernelUtils.isLastRepetitionWildcard(new int[] { 0 }));
	}

	@Test(enabled = true, description = "lastRepetitionIsWildcard returns false when the last repetition index is non-zero")
	public void shouldReturnFalseWhenLastRepetitionIsNonZero() {
		Assert.assertFalse(KernelUtils.isLastRepetitionWildcard(new int[] { 1, 2 }));
		Assert.assertFalse(KernelUtils.isLastRepetitionWildcard(new int[] { 1 }));
	}

	@Test(enabled = true, description = "lastRepetitionIsWildcard returns false for null or empty repetitions arrays")
	public void shouldReturnFalseForWildcardCheckWhenRepetitionsNullOrEmpty() {
		Assert.assertFalse(KernelUtils.isLastRepetitionWildcard(null));
		Assert.assertFalse(KernelUtils.isLastRepetitionWildcard(new int[0]));
	}

	@Test(enabled = true, description = "hasIntermediateWildcard returns true when a non-last repetition index is 0")
	public void shouldDetectIntermediateWildcardWhenNonLastIndexIsZero() {
		Assert.assertTrue(KernelUtils.hasIntermediateWildcard(new int[] { 0, 2 }));
		Assert.assertTrue(KernelUtils.hasIntermediateWildcard(new int[] { 1, 0, 3 }));
	}

	@Test(enabled = true, description = "hasIntermediateWildcard returns false when only the last repetition index is 0")
	public void shouldReportNoIntermediateWildcardWhenOnlyLastIsZero() {
		Assert.assertFalse(KernelUtils.hasIntermediateWildcard(new int[] { 1, 0 }));
		Assert.assertFalse(KernelUtils.hasIntermediateWildcard(new int[] { 1, 2, 0 }));
		Assert.assertFalse(KernelUtils.hasIntermediateWildcard(new int[] { 1, 2 }));
	}

	@Test(enabled = true, description = "hasAnyWildcard returns true when any repetition index (intermediate or last) is 0")
	public void shouldDetectAnyWildcardWhenAnyIndexIsZero() {
		Assert.assertTrue(KernelUtils.hasAnyWildcard(new int[] { 1, 0 }));
		Assert.assertTrue(KernelUtils.hasAnyWildcard(new int[] { 0, 2 }));
		Assert.assertTrue(KernelUtils.hasAnyWildcard(new int[] { 1, 0, 3 }));
		Assert.assertTrue(KernelUtils.hasAnyWildcard(new int[] { 0 }));
	}

	@Test(enabled = true, description = "hasAnyWildcard returns false when no repetition index is 0 or the array is null/empty")
	public void shouldReportNoWildcardWhenNoIndexIsZero() {
		Assert.assertFalse(KernelUtils.hasAnyWildcard(new int[] { 1, 2 }));
		Assert.assertFalse(KernelUtils.hasAnyWildcard(new int[] { 1 }));
		Assert.assertFalse(KernelUtils.hasAnyWildcard(null));
		Assert.assertFalse(KernelUtils.hasAnyWildcard(new int[0]));
	}

	@Test(enabled = true, description = "pointerPreservingWildcard builds a DocumentPointer that retains a trailing wildcard 0 as the last repetition index")
	public void shouldBuildPointerPreservingWildcardLastIndex() {
		DocumentPointer pointer = KernelUtils.pointerPreservingWildcard("BusinessPartnerRoot/Attachment", new int[] { 1, 0 });
		Assert.assertNotNull(pointer);
		List<Integer> repetitions = pointer.repetitionIndexes();
		Assert.assertFalse(repetitions.isEmpty());
		Assert.assertEquals(repetitions.getLast().intValue(), 0);
	}
}
