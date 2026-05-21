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
package com.mgmtp.a12.dataservices.internal.query.indexing.internal.jsonb;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.query.indexing.internal.SearchDataBuildHelper;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

public class SearchDataBuildHelperTest extends AbstractDataServicesCoreTest {

	private final DocumentModelUtils documentModelUtils = new DocumentModelUtils(documentModelServiceFactory, documentModelSerializer, headerParser);

	@Test public void testBuildSearchData() throws IOException {

		String modelJson = IOUtils.resourceToString("/models/document/Contract.json", StandardCharsets.UTF_8);
		String documentJson = IOUtils.resourceToString("/document/SearchDataBuilder_Test.json", StandardCharsets.UTF_8);

		IDocumentModel documentModel = documentModelUtils.deserializeDocumentModel(modelJson);
		DocumentV2 docV2 = documentSupport.convertJSONToDocument(documentModel.getContent().getDocumentModelInfo().getName(), new StringReader(documentJson));
		IDocumentModelSearchService searchService = documentModelServiceFactory.createDocumentModelSearchService(documentModel);
		String data = SearchDataBuildHelper.buildSearchData(searchService, docV2);
		System.out.println(data.replaceAll("~/", "~\n/"));

		Assert.assertTrue(data.contains("/ContractRoot/ContractName~Intelligent Marble Shirt~"));
		Assert.assertTrue(data.contains("/ContractRoot/ContractDescription~This is a really important contract~"));
		Assert.assertTrue(data.contains("/ContractRoot/LengthOfContract~5~"));
		Assert.assertTrue(data.contains("/ContractRoot/ContractValue~42~"));
		Assert.assertTrue(data.contains("/ContractRoot/Liability~12.34~"));
		Assert.assertTrue(data.contains("/ContractRoot/CostToCustomer~45.67~"));
		Assert.assertTrue(data.contains("/ContractRoot/NoOfCoInsuredCustomers~4~"));
		Assert.assertTrue(data.contains("/ContractRoot/MaxDiscount~Nothing~"));
		Assert.assertTrue(data.contains("/ContractRoot/CostPerCoInsured~1~"));
		Assert.assertTrue(data.contains("/ContractRoot/Valid~true~"));
		Assert.assertTrue(data.contains("/ContractRoot/Type~Travel~"));
		Assert.assertTrue(data.contains("/ContractRoot/Type[en]~Travel Insurance~"));
		Assert.assertTrue(data.contains("/ContractRoot/Type[en_US]~Travel Coverage~"));
		Assert.assertTrue(data.contains("/ContractRoot/Type[de]~Reiseversicherung~"));
		Assert.assertTrue(data.contains("/ContractRoot/ContractDates/CoveragePeriod~2024-01-01/2028-07-05~"));
		Assert.assertTrue(data.contains("/ContractRoot/ContractDates/ContractStartDate~2025-02-12T14:15:16~"));
		Assert.assertTrue(data.contains("/ContractRoot/ContractDates/ContractEndDate~2028-02-12~"));
		Assert.assertTrue(data.contains("/ContractRoot/ContractDates/ContractEndTime~17:18:19~"));
		Assert.assertTrue(data.contains("/ContractRoot/ContractDates/ContractReviewDate~2026~"));
		Assert.assertTrue(data.contains("/ContractRoot/ContractDates/LastPremiumPaidDate~2024-07-00~"));
		Assert.assertTrue(data.contains("/ContractRoot/ChangeLog/Number~1~"));
		Assert.assertTrue(data.contains("/ContractRoot/ChangeLog/ChangeTimestamp~2025-01-01~"));
		Assert.assertTrue(data.contains("/ContractRoot/ChangeLog/Description~First description~"));
		Assert.assertTrue(data.contains("/ContractRoot/ChangeLog/Description~Second description~"));
		Assert.assertTrue(data.contains("/ContractRoot/ChangeLog/Description~Third description~"));
		Assert.assertTrue(data.contains("/ContractRoot/ChangeLog/User~Zaphod~"));
		Assert.assertTrue(data.contains("/ContractRoot/ChangeLog/User~Tricia~"));
	}

}
