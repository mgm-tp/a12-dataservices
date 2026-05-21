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
package com.mgmtp.a12.examples.custom.type;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeConversionResult;

public class CustomTypeConversionIT {

	@Test
	public void convertDisplayInvalid() {
		TaxIDCustomFieldType type = new TaxIDCustomFieldType();
		ICustomFieldTypeConversionResult result = type.convertDisplay2Internal("INVALIDDATA", Map.of());
		Assert.assertTrue(result.getErrorMessage().isPresent());
		Assert.assertEquals(result.getErrorMessage().get(), TaxIDCustomFieldType.ERROR_MSG_DISPLAY_VALUE);
	}

	@Test
	public void convertDisplayValid() {
		TaxIDCustomFieldType type = new TaxIDCustomFieldType();
		ICustomFieldTypeConversionResult result = type.convertDisplay2Internal("BR-12345678", Map.of());
		Assert.assertFalse(result.getErrorMessage().isPresent());
		Assert.assertEquals(result.getConvertedValue(), "BR12345678");
	}

	@Test
	public void convertInternalInvalid() {
		TaxIDCustomFieldType type = new TaxIDCustomFieldType();
		ICustomFieldTypeConversionResult result = type.convertInternal2Display("INVALIDDATA", Map.of());
		Assert.assertTrue(result.getErrorMessage().isPresent());
		Assert.assertEquals(result.getErrorMessage().get(), TaxIDCustomFieldType.ERROR_MSG_INTERNAL_VALUE);
	}

	@Test
	public void convertInternalValid() {
		TaxIDCustomFieldType type = new TaxIDCustomFieldType();
		ICustomFieldTypeConversionResult result = type.convertInternal2Display("BR12345678", Map.of());
		Assert.assertFalse(result.getErrorMessage().isPresent());
		Assert.assertEquals(result.getConvertedValue(), "BR-12345678");
	}
}

