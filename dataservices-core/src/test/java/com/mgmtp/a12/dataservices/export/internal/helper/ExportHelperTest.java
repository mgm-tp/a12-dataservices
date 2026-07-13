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
package com.mgmtp.a12.dataservices.export.internal.helper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.InjectMocks;
import org.testng.Assert;
import org.testng.annotations.Test;

import tools.jackson.databind.JsonNode;
import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateTimeType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.INumberType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IStringType;

import static org.mockito.Mockito.mock;

public class ExportHelperTest extends AbstractDataServicesCoreTest {

	@InjectMocks private ExportHelper exportHelper;

	@Test void testCollectHeaders_shouldSuccess() {
		IDocumentModel documentModel = documentModelResolver.getDocumentModelById(DocumentModelConstants.ANONYMIZED_BUSINESS_PARTNER_CDM_MODEL);
		List<Header> headers = new ExportHelper(kernelTestSupport.getIDocumentModelService())
			.collectHeaders(documentModel);
		Assert.assertEquals(headers.size(), 10);
		Assert.assertEquals(headers.getFirst().getName(), "/BusinessPartnerRoot/Industry");
		Assert.assertEquals(headers.get(1).getName(), "/BusinessPartnerRoot/StartOfRelationship");
		Assert.assertEquals(headers.get(2).getName(), "/BusinessPartnerRoot/CustomerDiscount");
		Assert.assertEquals(headers.get(3).getName(), "/__meta/docRef");
		Assert.assertEquals(headers.get(4).getName(), "/__meta/modelReference");
		Assert.assertEquals(headers.get(5).getName(), "/__meta/modelVersion");
		Assert.assertEquals(headers.get(6).getName(), "/__meta/creator");
		Assert.assertEquals(headers.get(7).getName(), "/__meta/createdAt");
		Assert.assertEquals(headers.get(8).getName(), "/__meta/modifier");
		Assert.assertEquals(headers.get(9).getName(), "/__meta/modifiedAt");

	}

	@Test void testIsNumeric_shouldSuccess() {
		Assert.assertFalse(exportHelper.isNumeric(mockFieldType()));
		Assert.assertTrue(exportHelper.isNumeric(mock(INumberType.class)));
		Assert.assertFalse(exportHelper.isNumeric(mock(IStringType.class)));
		Assert.assertFalse(exportHelper.isNumeric(mock(IDateTimeType.class)));
	}

	@Test void getRows_shouldSuccess() throws IOException {
		JsonNode jsonTree = objectMapper.readTree(loadDocumentAsString(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL + ".json"));
		IDocumentModel documentModel = documentModelResolver.getDocumentModelById(DocumentModelConstants.ANONYMIZED_BUSINESS_PARTNER_CDM_MODEL);
		List<Header> headers = new ExportHelper(kernelTestSupport.getIDocumentModelService())
			.collectHeaders(documentModel);

		List<List<Object>> results = exportHelper.getRows(List.of(jsonTree), documentModel, headers);
		assertExportResult(results, headers);
	}

	private static void assertExportResult(List<List<Object>> results, List<Header> headers) {
		Assert.assertFalse(results.isEmpty(), null);

		Assert.assertEquals(results.size(), 1);
		Assert.assertEquals(results.getFirst().size(), headers.size());
		Map<String, String> resultMap = new HashMap<>();
		for (int i = 0; i < headers.size(); i++) {
			resultMap.put(headers.get(i).getName(), String.valueOf(results.getFirst().get(i)));
		}

		Assert.assertTrue(resultMap.containsKey("/BusinessPartnerRoot/Industry"));
		Assert.assertEquals(resultMap.get("/BusinessPartnerRoot/Industry"), "IT");
		Assert.assertEquals(resultMap.get("/BusinessPartnerRoot/StartOfRelationship"), "2022-07-05");
	}

	private IFieldType mockFieldType() {
		return mock(IFieldType.class);
	}
}
