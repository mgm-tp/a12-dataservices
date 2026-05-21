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
package com.mgmtp.a12.dataservices.internal.query.mapping;

import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.dataservices.query.internal.DocumentTreeMapper;

import static org.testng.Assert.assertEquals;

public class DocumentTreeMapperTest {

	@Test
	public void testContentMapping() throws JsonProcessingException {

		String input = """
			[
				["/ContractRoot/ContractName", [1, 1], "Test Contract"],
				["/ContractRoot/LengthOfContract", [1, 1], "123"],
				["/ContractRoot/ContractValue", [1, 1], "123"],
				["/ContractRoot/Liability", [1, 1], "123"],
				["/ContractRoot/CostToCustomer", [1, 1], "123"],
				["/ContractRoot/CostPerCoInsured", [1, 1], "123"],
				["/ContractRoot/MaxDiscount", [1, 1], "10%"],
				["/ContractRoot/NoOfCoInsuredCustomers", [1, 1], "123"],
				["/ContractRoot/Valid", [1, 1], "true"],
				["/__meta/creator", [1, 1], "admin"],
				["/__meta/modifier", [1, 1], "admin"],
				["/__meta/createdAt", [1, 1], "2024-10-02T13:33:22.305"],
				["/__meta/modifiedAt", [1, 1], "2024-10-02T13:33:22.305"],
				["/__meta/modelReference", [1, 1], "Contract"],
				["/__meta/docRef", [1, 1], "Contract/77922f51-1a11-4c35-b22b-9e1999005b8d"],
				["/__meta/modelVersion", [1, 1], null]
			]
			""";
		JsonNode result = DocumentTreeMapper.stringToJsonNode(input);

		assertEquals(result.toString(), """
			{"ContractRoot":{"MaxDiscount":"10%","ContractValue":"123","Liability":"123","Valid":"true","LengthOfContract":"123","CostPerCoInsured":"123","NoOfCoInsuredCustomers":"123","CostToCustomer":"123","ContractName":"Test Contract"},"__meta":{"createdAt":"2024-10-02T13:33:22.305","creator":"admin","docRef":"Contract/77922f51-1a11-4c35-b22b-9e1999005b8d","modelReference":"Contract","modelVersion":null,"modifiedAt":"2024-10-02T13:33:22.305","modifier":"admin"}}""");
	}
}
