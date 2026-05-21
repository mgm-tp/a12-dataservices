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

import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.rpc.query.PagedResultSet;

public class RpcAddLinkOrderIT extends RpcOrderedLinkIT {

	@Test
	@Transactional
	public void insertWithOrder1() throws IOException {
		String template = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "add/add_link_head.json");
		// no predecessor init request
		String request = String.format(template, RoleConstants.PARTNER_ROLE, partner1DocRef, RoleConstants.CONTRACT_ROLE, contract1DocRef, "");
		sendRpcRequest(request);

		QueryRoot queryLink = constructQueryLink(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, partner1DocRef, RoleConstants.CONTRACT_ROLE);
		PagedResultSet<DocumentTreeResult> result = queryOperation.rpc(queryLink);
		Assert.assertEquals(result.getFullSize(), 1);
		String firstLink = result.getLinks().stream()
			.filter(link -> link.getType() == DocumentTreeNodeType.LINK)
			.findFirst()
			.map(DocumentTreeResult::getLinkId)
			.orElse(null);

		request = String.format(template, RoleConstants.PARTNER_ROLE, partner1DocRef, RoleConstants.CONTRACT_ROLE, contract2DocRef, firstLink);
		sendRpcRequest(request);

		// order: first, second
		queryLink = constructQueryLink(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, partner1DocRef, RoleConstants.CONTRACT_ROLE);
		result = queryOperation.rpc(queryLink);
		Assert.assertEquals(result.getFullSize(), 2);
		List<DocumentTreeResult> links = result.getLinks().stream()
			.filter(link -> link.getType() == DocumentTreeNodeType.LINK)
			.toList();
		Assert.assertEquals(links.get(0).getLinkId(), firstLink);
		String secondLink = links.get(1).getLinkId();

		request = String.format(template, RoleConstants.PARTNER_ROLE, partner1DocRef, RoleConstants.CONTRACT_ROLE, contract3DocRef, secondLink);
		sendRpcRequest(request);

		// order: first, second, third
		queryLink = constructQueryLink(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, partner1DocRef, RoleConstants.CONTRACT_ROLE);
		result = queryOperation.rpc(queryLink);
		Assert.assertEquals(result.getFullSize(), 3);
		links = result.getLinks().stream()
			.filter(link -> link.getType() == DocumentTreeNodeType.LINK)
			.toList();
		Assert.assertEquals(links.get(0).getLinkId(), firstLink);
		Assert.assertEquals(links.get(1).getLinkId(), secondLink);
		String thirdLink = links.get(2).getLinkId();

		request = String.format(template, RoleConstants.PARTNER_ROLE, partner1DocRef, RoleConstants.CONTRACT_ROLE, contract4DocRef, firstLink);
		sendRpcRequest(request);

		// order: first, fourth, second, third
		queryLink = constructQueryLink(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, partner1DocRef, RoleConstants.CONTRACT_ROLE);
		result = queryOperation.rpc(queryLink);
		Assert.assertEquals(result.getFullSize(), 4);
		links = result.getLinks().stream()
			.filter(link -> link.getType() == DocumentTreeNodeType.LINK)
			.toList();
		Assert.assertEquals(links.get(0).getLinkId(), firstLink);
		String fourthLink = links.get(1).getLinkId();
		Assert.assertEquals(links.get(2).getLinkId(), secondLink);
		Assert.assertEquals(links.get(3).getLinkId(), thirdLink);

		request = String.format(template, RoleConstants.PARTNER_ROLE, partner1DocRef, RoleConstants.CONTRACT_ROLE, contract5DocRef, "");
		sendRpcRequest(request);

		// order: fifth, first, fourth, second, third
		queryLink = constructQueryLink(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, partner1DocRef, RoleConstants.CONTRACT_ROLE);
		result = queryOperation.rpc(queryLink);
		Assert.assertEquals(result.getFullSize(), 5);
		links = result.getLinks().stream()
			.filter(link -> link.getType() == DocumentTreeNodeType.LINK)
			.toList();
		String fifthLink = links.get(0).getLinkId();
		Assert.assertNotNull(fifthLink);
		Assert.assertEquals(links.get(1).getLinkId(), firstLink);
		Assert.assertEquals(links.get(2).getLinkId(), fourthLink);
		Assert.assertEquals(links.get(3).getLinkId(), secondLink);
		Assert.assertEquals(links.get(4).getLinkId(), thirdLink);
	}

	@Test
	public void insertWithOrder2() throws IOException {
		String template = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "add/add_link_head.json");
		// no predecessor init request
		String request = String.format(template, RoleConstants.CONTRACT_ROLE, contract1DocRef, RoleConstants.PARTNER_ROLE, partner1DocRef, "");
		sendRpcRequest(request);

		QueryRoot queryLink = constructQueryLink(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, contract1DocRef, RoleConstants.PARTNER_ROLE);
		PagedResultSet<DocumentTreeResult> result = queryOperation.rpc(queryLink);
		Assert.assertEquals(result.getFullSize(), 1);
		String firstLink = result.getLinks().stream()
			.filter(link -> link.getType() == DocumentTreeNodeType.LINK)
			.findFirst()
			.map(DocumentTreeResult::getLinkId)
			.orElse(null);

		request = String.format(template, RoleConstants.CONTRACT_ROLE, contract1DocRef, RoleConstants.PARTNER_ROLE, partner2DocRef, firstLink);
		sendRpcRequest(request);

		// order: first, second
		queryLink = constructQueryLink(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, contract1DocRef, RoleConstants.PARTNER_ROLE);
		result = queryOperation.rpc(queryLink);
		List<DocumentTreeResult> links = result.getLinks().stream()
			.filter(link -> link.getType() == DocumentTreeNodeType.LINK)
			.toList();
		Assert.assertEquals(result.getFullSize(), 2);
		Assert.assertEquals(links.get(0).getLinkId(), firstLink);
		String secondLink = links.get(1).getLinkId();

		request = String.format(template, RoleConstants.CONTRACT_ROLE, contract1DocRef, RoleConstants.PARTNER_ROLE, partner3DocRef, secondLink);
		sendRpcRequest(request);

		// order: first, second, third
		queryLink = constructQueryLink(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, contract1DocRef, RoleConstants.PARTNER_ROLE);
		result = queryOperation.rpc(queryLink);
		links = result.getLinks().stream()
			.filter(link -> link.getType() == DocumentTreeNodeType.LINK)
			.toList();
		Assert.assertEquals(result.getFullSize(), 3);
		Assert.assertEquals(links.get(0).getLinkId(), firstLink);
		Assert.assertEquals(links.get(1).getLinkId(), secondLink);
		String thirdLink = links.get(2).getLinkId();

		request = String.format(template, RoleConstants.CONTRACT_ROLE, contract1DocRef, RoleConstants.PARTNER_ROLE, partner4DocRef, firstLink);
		sendRpcRequest(request);

		// order: first, fourth, second, third
		queryLink = constructQueryLink(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, contract1DocRef, RoleConstants.PARTNER_ROLE);
		result = queryOperation.rpc(queryLink);
		links = result.getLinks().stream()
			.filter(link -> link.getType() == DocumentTreeNodeType.LINK)
			.toList();
		Assert.assertEquals(result.getFullSize(), 4);
		Assert.assertEquals(links.get(0).getLinkId(), firstLink);
		String fourthLink = links.get(1).getLinkId();
		Assert.assertEquals(links.get(2).getLinkId(), secondLink);
		Assert.assertEquals(links.get(3).getLinkId(), thirdLink);

		request = String.format(template, RoleConstants.CONTRACT_ROLE, contract1DocRef, RoleConstants.PARTNER_ROLE, partner5DocRef, "");
		sendRpcRequest(request);

		// order: fifth, first, fourth, second, third
		queryLink = constructQueryLink(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, contract1DocRef, RoleConstants.PARTNER_ROLE);
		result = queryOperation.rpc(queryLink);
		links = result.getLinks().stream()
			.filter(link -> link.getType() == DocumentTreeNodeType.LINK)
			.toList();
		Assert.assertEquals(result.getFullSize(), 5);
		String fifthLink = links.get(0).getLinkId();
		Assert.assertNotNull(fifthLink);
		Assert.assertEquals(links.get(1).getLinkId(), firstLink);
		Assert.assertEquals(links.get(2).getLinkId(), fourthLink);
		Assert.assertEquals(links.get(3).getLinkId(), secondLink);
		Assert.assertEquals(links.get(4).getLinkId(), thirdLink);
	}

	@Test
	@Transactional
	public void testWithSourceRoleOrderOnly() throws IOException {
		String template = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "add/add_link_head.json");
		// no predecessor init request
		sendRpcRequest(String.format(template, RoleConstants.PARTNER_ROLE, partner1DocRef, RoleConstants.CONTRACT_ROLE, contract3DocRef, ""));
		sendRpcRequest(String.format(template, RoleConstants.PARTNER_ROLE, partner1DocRef, RoleConstants.CONTRACT_ROLE, contract2DocRef, ""));
		sendRpcRequest(String.format(template, RoleConstants.PARTNER_ROLE, partner1DocRef, RoleConstants.CONTRACT_ROLE, contract1DocRef, ""));

		QueryRoot queryLink = constructQueryLink(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, partner1DocRef, RoleConstants.CONTRACT_ROLE);
		PagedResultSet<DocumentTreeResult> sourceRoleOrderResult = queryOperation.rpc(queryLink);
		Assert.assertEquals(sourceRoleOrderResult.getFullSize(), 3);
		// Relationship model have ordered:true in both source and target role, so they have order as expectation.
		Assert.assertEquals(sourceRoleOrderResult.getLinks().get(0).getTargetDocRef(), contract1DocRef);
		Assert.assertEquals(sourceRoleOrderResult.getLinks().get(1).getTargetDocRef(), contract2DocRef);
		Assert.assertEquals(sourceRoleOrderResult.getLinks().get(2).getTargetDocRef(), contract3DocRef);

		// modify source role order to false the links should be sorted by link id
		JsonNode node = objectMapper.readTree(resourceFunctions.loadResource(PathConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH));
		ObjectNode contractRole = (ObjectNode) node.path("content").path("entityCharacteristics").get(0);
		ObjectNode partnerRole = (ObjectNode) node.path("content").path("entityCharacteristics").get(1);
		partnerRole.put("ordered", BooleanNode.FALSE);
		modelsFunctions.updateModelContent(node.toString());

		PagedResultSet<DocumentTreeResult> orderedByLinkIdResult = queryOperation.rpc(queryLink);
		List<DocumentTreeResult> orderedByLinkIdList = new java.util.ArrayList<>(List.of(
			sourceRoleOrderResult.getLinks().get(0),
			sourceRoleOrderResult.getLinks().get(1),
			sourceRoleOrderResult.getLinks().get(2)
		));
		orderedByLinkIdList.sort((d1, d2) -> d1.getLinkId().compareToIgnoreCase(d2.getLinkId()));

		Assert.assertEquals(orderedByLinkIdResult.getFullSize(), 3);
		Assert.assertEquals(orderedByLinkIdResult.getLinks().get(0).getTargetDocRef(), orderedByLinkIdList.get(0).getTargetDocRef());
		Assert.assertEquals(orderedByLinkIdResult.getLinks().get(1).getTargetDocRef(), orderedByLinkIdList.get(1).getTargetDocRef());
		Assert.assertEquals(orderedByLinkIdResult.getLinks().get(2).getTargetDocRef(), orderedByLinkIdList.get(2).getTargetDocRef());

		// modify source role order to true, target role order to false, it will keep order by source role order.
		partnerRole.put("ordered", BooleanNode.TRUE);
		contractRole.put("ordered", BooleanNode.FALSE);
		modelsFunctions.updateModelContent(node.toString());

		PagedResultSet<DocumentTreeResult> orderedResult = queryOperation.rpc(queryLink);
		Assert.assertEquals(orderedResult.getFullSize(), 3);
		Assert.assertEquals(orderedResult.getLinks().get(0).getTargetDocRef(), contract1DocRef);
		Assert.assertEquals(orderedResult.getLinks().get(1).getTargetDocRef(), contract2DocRef);
		Assert.assertEquals(orderedResult.getLinks().get(2).getTargetDocRef(), contract3DocRef);
	}

	@Test
	public void vwrongPredecessor() throws IOException {
		String template = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "add/add_link_head.json");
		// no predecessor init request
		String request = String.format(template, RoleConstants.PARTNER_ROLE, partner2DocRef, RoleConstants.CONTRACT_ROLE, contract1DocRef, "9999999999");
		sendRpcRequest(request);

		request = String.format(template, RoleConstants.PARTNER_ROLE, partner2DocRef, RoleConstants.CONTRACT_ROLE, contract2DocRef, "9999999999");
		Assert.assertFalse(sendRpcRequest(request).get(0).isSuccess());
	}

	@Test
	public void insertWithPositionBottom_shouldPutNewAtTheEnd() throws IOException {
		String firstLink = addLinkWithPositionAndPredecessor(contract1DocRef, "BOTTOM", "");
		assertQueryIsOrdered(firstLink);

		String secondLink = addLinkWithPositionAndPredecessor(contract2DocRef, "BOTTOM", "");
		assertQueryIsOrdered(firstLink, secondLink);

		String thirdLink = addLinkWithPositionAndPredecessor(contract3DocRef, "BOTTOM", "");
		assertQueryIsOrdered(firstLink, secondLink, thirdLink);

		String fourthLink = addLinkWithPositionAndPredecessor(contract4DocRef, "BOTTOM", "");
		assertQueryIsOrdered(firstLink, secondLink, thirdLink, fourthLink);

		String fifthLink = addLinkWithPositionAndPredecessor(contract5DocRef, "BOTTOM", "");
		assertQueryIsOrdered(firstLink, secondLink, thirdLink, fourthLink, fifthLink);
	}

	@Test
	public void insertWithPositionTop_shouldPutNewAtTheBeginning() throws IOException {
		String firstLink = addLinkWithPositionAndPredecessor(contract1DocRef, "TOP", "");
		assertQueryIsOrdered(firstLink);

		String secondLink = addLinkWithPositionAndPredecessor(contract2DocRef, "TOP", "");
		assertQueryIsOrdered(secondLink, firstLink);

		String thirdLink = addLinkWithPositionAndPredecessor(contract3DocRef, "TOP", "");
		assertQueryIsOrdered(thirdLink, secondLink, firstLink);

		String fourthLink = addLinkWithPositionAndPredecessor(contract4DocRef, "TOP", "");
		assertQueryIsOrdered(fourthLink, thirdLink, secondLink, firstLink);

		String fifthLink = addLinkWithPositionAndPredecessor(contract5DocRef, "TOP", "");
		assertQueryIsOrdered(fifthLink, fourthLink, thirdLink, secondLink, firstLink);
	}

	@Test
	public void insertWithoutPositionNorPredecessor_shouldPutNewAtTheBeginning() throws IOException {
		String firstLink = addLinkWithoutPositionNorPredecessor(contract1DocRef);
		assertQueryIsOrdered(firstLink);

		String secondLink = addLinkWithoutPositionNorPredecessor(contract2DocRef);
		assertQueryIsOrdered(secondLink, firstLink);

		String thirdLink = addLinkWithoutPositionNorPredecessor(contract3DocRef);
		assertQueryIsOrdered(thirdLink, secondLink, firstLink);

		String fourthLink = addLinkWithoutPositionNorPredecessor(contract4DocRef);
		assertQueryIsOrdered(fourthLink, thirdLink, secondLink, firstLink);

		String fifthLink = addLinkWithoutPositionNorPredecessor(contract5DocRef);
		assertQueryIsOrdered(fifthLink, fourthLink, thirdLink, secondLink, firstLink);
	}

	@Test
	public void insertWithPredecessor_shouldIgnorePosition() throws IOException {
		String firstLink = addLinkWithPositionAndPredecessor(contract1DocRef, "TOP", "");
		assertQueryIsOrdered(firstLink);

		String secondLink = addLinkWithPositionAndPredecessor(contract2DocRef, "TOP", firstLink);
		assertQueryIsOrdered(firstLink, secondLink);

		String thirdLink = addLinkWithPositionAndPredecessor(contract3DocRef, "TOP", secondLink);
		assertQueryIsOrdered(firstLink, secondLink, thirdLink);

		String fourthLink = addLinkWithPositionAndPredecessor(contract4DocRef, "TOP", firstLink);
		assertQueryIsOrdered(firstLink, fourthLink, secondLink, thirdLink);

		String fifthLink = addLinkWithPositionAndPredecessor(contract5DocRef, "TOP", "");
		assertQueryIsOrdered(fifthLink, firstLink, fourthLink, secondLink, thirdLink);
	}

	@Test
	public void mixPositionsAndPredecessor_shouldOrderAccordingly() throws IOException {
		String firstLink = addLinkWithPositionAndPredecessor(contract1DocRef, "TOP", "");
		assertQueryIsOrdered(firstLink);

		String secondLink = addLinkWithPositionAndPredecessor(contract2DocRef, "BOTTOM", "");
		assertQueryIsOrdered(firstLink, secondLink);

		String thirdLink = addLinkWithPositionAndPredecessor(contract3DocRef, "TOP", "");
		assertQueryIsOrdered(thirdLink, firstLink, secondLink);

		String fourthLink = addLinkWithPositionAndPredecessor(contract4DocRef, "BOTTOM", "");
		assertQueryIsOrdered(thirdLink, firstLink, secondLink, fourthLink);

		String fifthLink = addLinkWithPositionAndPredecessor(contract5DocRef, "BOTTOM", firstLink);
		assertQueryIsOrdered(thirdLink, firstLink, fifthLink, secondLink, fourthLink);
	}
}
