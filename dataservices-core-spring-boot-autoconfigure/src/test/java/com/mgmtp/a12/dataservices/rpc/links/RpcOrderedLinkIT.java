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

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;

import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.repository.RelationshipLinkJpaRepository;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;
import com.mgmtp.a12.dataservices.rpc.query.PagedResultSet;

public class RpcOrderedLinkIT extends AbstractLinkIT {

	@Autowired protected RelationshipLinkJpaRepository relationshipLinkJpaRepository;

	protected void assertQueryIsOrdered(String... expectedOrder) {
		QueryRoot queryLink = constructQueryLink(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, partner1DocRef, RoleConstants.CONTRACT_ROLE);
		PagedResultSet<DocumentTreeResult> result = queryOperation.rpc(queryLink);
		Assert.assertEquals(result.getFullSize(), expectedOrder.length);
		List<DocumentTreeResult> links = result.getLinks().stream()
			.filter(link -> link.getType() == DocumentTreeNodeType.LINK)
			.toList();

		for (int i = 0; i < expectedOrder.length; i++) {
			String expected = expectedOrder[i];
			String actual = links.get(i).getLinkId();
			Assert.assertEquals(actual, expected, "On position #%d there is".formatted(i));
		}
	}

	protected String addLinkWithPositionAndPredecessor(DocumentReference secondDocRef, String position, String predecessorLink) throws IOException {
		String TEMPLATE = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "add/add_link_with_position.json");
		String request =
			TEMPLATE.formatted(RoleConstants.PARTNER_ROLE, partner1DocRef, RoleConstants.CONTRACT_ROLE, secondDocRef, predecessorLink,
				position);
		JsonRpc2Response rpcResponse = sendRpcRequest(request).getFirst();
		return (convertResponse(rpcResponse.getResult().toString(), RelationshipLinkSpec.class)).getId();
	}

	protected String relinkWithPositionAndPredecessor(DocumentReference campaignDocRef, String position, String predecessorLink, String linkRef)
		throws IOException {
		String template = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "templates/relink_document_template_with_position.json");
		String request =
			template.formatted(RoleConstants.PARTNER_ROLE, partner1DocRef, RoleConstants.CONTRACT_ROLE, campaignDocRef, predecessorLink, position,
				linkRef);
		JsonRpc2Response rpcResponse = sendRpcRequest(request).getFirst();
		return (convertResponse(rpcResponse.getResult().toString(), RelationshipLinkSpec.class)).getId();
	}

	protected String addLinkWithoutPositionNorPredecessor(DocumentReference secondDocRef) throws IOException {
		String TEMPLATE = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "add/add_link_head.json");
		String request = TEMPLATE.formatted(RoleConstants.PARTNER_ROLE, partner1DocRef, RoleConstants.CONTRACT_ROLE, secondDocRef, "");
		JsonRpc2Response rpcResponse = sendRpcRequest(request).getFirst();
		return (convertResponse(rpcResponse.getResult().toString(), RelationshipLinkSpec.class)).getId();
	}

	protected String relinkWithoutPositionNorPredecessor(DocumentReference secondDocRef, String linkRef) throws IOException {
		String TEMPLATE = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "templates/relink_document_template.json");
		String request =
			TEMPLATE.formatted(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, RelationshipModelConstants.RoleConstants.PARTNER_ROLE,
				partner1DocRef,
				RelationshipModelConstants.RoleConstants.CONTRACT_ROLE, secondDocRef, "", linkRef);
		JsonRpc2Response rpcResponse = sendRpcRequest(request).getFirst();
		return (convertResponse(rpcResponse.getResult().toString(), RelationshipLinkSpec.class)).getId();
	}
}
