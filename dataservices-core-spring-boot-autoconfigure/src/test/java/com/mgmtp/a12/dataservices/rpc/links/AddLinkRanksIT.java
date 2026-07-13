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
package com.mgmtp.a12.dataservices.rpc.links;

import java.io.IOException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;

public class AddLinkRanksIT extends AbstractLinkIT {

	@Test(description = "Should include sourceRank and targetRank in ADD_LINK JSON-RPC response")
	public void shouldIncludeRankFieldsInAddLinkResponse() throws IOException {
		// Given: a ContractCoInsuredPartner link between partner1 and contract1
		String request = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "add/add_links_request.json");
		request = request.formatted(
			partner1DocRef, contract1DocRef,
			partner2DocRef, contract1DocRef,
			partner3DocRef, contract2DocRef,
			partner4DocRef, contract2DocRef,
			partner5DocRef, contract1DocRef,
			partner5DocRef, contract2DocRef,
			partner1DocRef, contract3DocRef
		);

		// When: ADD_LINK is called via JSON-RPC
		List<RelationshipLinkSpec> results = sendRpcRequest(request).stream()
			.map(e -> convertResponse(e.getResult().toString(), RelationshipLinkSpec.class))
			.toList();

		// Then: response contains non-null sourceRank and targetRank
		Assert.assertFalse(results.isEmpty());
		for (RelationshipLinkSpec result : results) {
			Assert.assertNotNull(result.getSourceRank(), "sourceRank must not be null");
			Assert.assertNotNull(result.getTargetRank(), "targetRank must not be null");
		}
	}
}
