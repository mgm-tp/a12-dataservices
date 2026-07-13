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
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.rpc.query.PagedResultSet;

public class RpcRelinkDocumentOperationIT extends AbstractLinkIT {

	private String firstLink;

	@BeforeMethod
	@Transactional
	@Override public void setUp() throws Exception {
		super.setUp();
		String template = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "add/add_link_head.json");
		// no predecessor init request
		String request =
			template.formatted(RoleConstants.PARTNER_ROLE, partner1DocRef, RoleConstants.CONTRACT_ROLE, contract1DocRef, "");
		handleErrors(sendRpcRequest(request));

		QueryRoot queryLink = constructQueryLink(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, partner1DocRef, RoleConstants.CONTRACT_ROLE);
		PagedResultSet<DocumentTreeResult> result = queryOperation.rpc(queryLink);
		Assert.assertEquals(result.getFullSize(), 1);
		firstLink = result.getLinks().stream()
			.filter(link -> link.getType() == DocumentTreeNodeType.LINK)
			.findFirst()
			.map(DocumentTreeResult::getLinkId)
			.orElse(null);
	}

	@Test
	public void relinkDocument() throws IOException {
		String template = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "templates/relink_document_template.json");

		String request =
			template.formatted(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, RelationshipModelConstants.RoleConstants.PARTNER_ROLE,
				partner1DocRef,
				RelationshipModelConstants.RoleConstants.CONTRACT_ROLE, contract2DocRef, "", firstLink);
		sendRpcRequest(request);

		Optional<? extends RelationshipLink> loadedLink = relationshipLinkRepository.findById(firstLink);
		Assert.assertFalse(loadedLink.isPresent());

		QueryRoot queryLink = constructQueryLink(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, partner1DocRef, RoleConstants.CONTRACT_ROLE);
		PagedResultSet<DocumentTreeResult> result = queryOperation.rpc(queryLink);
		Assert.assertEquals(result.getFullSize(), 1);
		DocumentTreeResult link = result.getLinks().stream()
			.filter(l -> l.getType() == DocumentTreeNodeType.LINK)
			.findFirst()
			.orElse(null);
		Assert.assertNotNull(link);
		Assert.assertNotNull(link.getLinkId());
		Assert.assertNotNull(link.getSourceDocRef());
		Assert.assertNotNull(link.getTargetDocRef());
	}

	@Test
	public void relinkNonExisting() throws IOException {
		String template = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "templates/relink_document_template.json");
		String request =
			template.formatted(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, RelationshipModelConstants.RoleConstants.PARTNER_ROLE,
				partner1DocRef,
				RelationshipModelConstants.RoleConstants.CONTRACT_ROLE, contract2DocRef, "", "999999999");
		Assert.assertFalse(sendRpcRequest(request).getFirst().isSuccess());
	}
}
