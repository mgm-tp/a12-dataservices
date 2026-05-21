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

import java.util.TimeZone;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.fieldtypes.DateTypeWrapper;

import static com.mgmtp.a12.dataservices.document.persistence.internal.AbstractDefaultDocumentServiceTest.DOCUMENT_FILENAME;

public class DocumentUtilsTest extends AbstractDataServicesCoreTest {

	@Test
	public void format_should_with_empty_string() {
		IDateType dateType = new DateTypeWrapper(new DateType());
		Assert.assertEquals(DocumentUtils.format(dateType, "", TimeZone.getDefault()), "");
	}

	@Test
	public void format_should_return_partial_date_as_string() {
		IDateType dateType = new DateTypeWrapper(new DateType());
		Assert.assertEquals(DocumentUtils.format(dateType, "2024-00-00", TimeZone.getDefault()), "2024-00-00");
	}

	@Test void testFindSingleValue() {
		DocumentV2 documentV2 = loadDocumentV2(
			DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, DOCUMENT_FILENAME);
		Assert.assertEquals(DocumentUtils.findSingleValue(documentV2, "/BusinessPartnerRoot/Name").get(), "Malcolm");
		Assert.assertTrue(DocumentUtils.findSingleValue(documentV2, "/BusinessPartnerRoot/name2").isEmpty());
	}
}
