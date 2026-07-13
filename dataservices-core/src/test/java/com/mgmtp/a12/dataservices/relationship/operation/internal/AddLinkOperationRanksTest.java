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
package com.mgmtp.a12.dataservices.relationship.operation.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.RelationshipRole;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class AddLinkOperationRanksTest {

	private static final String RELATIONSHIP_MODEL = "ContractCoInsuredPartner";
	private static final String SOURCE_ROLE_NAME = "Partner";
	private static final String TARGET_ROLE_NAME = "Contract";

	private RelationshipLinkService relationshipLinkService;
	private Anonymizer anonymizer;
	private AddLinkOperation addLinkOperation;

	@BeforeMethod
	public void setUp() {
		relationshipLinkService = mock(RelationshipLinkService.class);
		anonymizer = mock(Anonymizer.class);
		when(anonymizer.apply(any())).thenAnswer(invocation -> invocation.getArgument(0));
		addLinkOperation = new AddLinkOperation(relationshipLinkService, anonymizer);
	}

	private LinkDescriptor buildLinkDescriptor() {
		DocumentReference sourceDocRef = DocumentReference.builder()
			.documentModelName("BusinessPartner")
			.documentId("bp-1")
			.build();
		DocumentReference targetDocRef = DocumentReference.builder()
			.documentModelName("Contract")
			.documentId("contract-1")
			.build();
		return new LinkDescriptor(
			RELATIONSHIP_MODEL,
			List.of(
				new RelationshipRoleSpec(SOURCE_ROLE_NAME, sourceDocRef),
				new RelationshipRoleSpec(TARGET_ROLE_NAME, targetDocRef)
			)
		);
	}

	@SuppressWarnings("unchecked")
	private RelationshipLink buildMockLink(String sourceOrder, String targetOrder) {
		RelationshipLink link = mock(RelationshipLink.class);
		RelationshipRole sourceRole = mock(RelationshipRole.class);
		RelationshipRole targetRole = mock(RelationshipRole.class);

		when(sourceRole.getOrder()).thenReturn(sourceOrder);
		when(targetRole.getOrder()).thenReturn(targetOrder);

		Map<String, RelationshipRole> roles = new HashMap<>();
		roles.put(SOURCE_ROLE_NAME, sourceRole);
		roles.put(TARGET_ROLE_NAME, targetRole);
		when(link.getRoles()).thenReturn((Map) roles);
		when(link.getId()).thenReturn("link-1");
		when(link.getLinkDocumentDocRef()).thenReturn(null);
		return link;
	}

	@Test(description = "Should return sourceRank and targetRank when link is created successfully")
	public void shouldReturnBothRanksInResponseWhenLinkIsCreated() {
		// Given: mock RelationshipLinkService.create() returns a RelationshipLink
		//        whose roles[sourceRoleName].getOrder() == "a"
		//        and   roles[targetRoleName].getOrder() == "b"
		LinkDescriptor linkDescriptor = buildLinkDescriptor();
		RelationshipLink createdLink = buildMockLink("a", "b");
		when(relationshipLinkService.create(any(LinkDescriptor.class))).thenReturn(createdLink);

		// When
		RelationshipLinkSpec result = addLinkOperation.rpc(linkDescriptor, null);

		// Then
		assertEquals(result.getSourceRank(), "a");
		assertEquals(result.getTargetRank(), "b");
	}

	@Test(description = "Should assign sourceRank to source role order and targetRank to target role order without swapping")
	public void shouldNotSwapSourceRankAndTargetRank() {
		// Given: mock RelationshipLink where source role order differs from target role order
		LinkDescriptor linkDescriptor = buildLinkDescriptor();
		RelationshipLink createdLink = buildMockLink("x", "y");
		when(relationshipLinkService.create(any(LinkDescriptor.class))).thenReturn(createdLink);

		// When
		RelationshipLinkSpec result = addLinkOperation.rpc(linkDescriptor, null);

		// Then: sourceRank maps to "x" (source role) and targetRank maps to "y" (target role)
		assertEquals(result.getSourceRank(), "x");
		assertEquals(result.getTargetRank(), "y");
	}

	@Test(description = "Should handle null order gracefully when role order is not set")
	public void shouldHandleNullRoleOrderWithoutNullPointerException() {
		// Given: mock RelationshipLink whose roles[source].getOrder() returns null
		LinkDescriptor linkDescriptor = buildLinkDescriptor();
		RelationshipLink createdLink = buildMockLink(null, null);
		when(relationshipLinkService.create(any(LinkDescriptor.class))).thenReturn(createdLink);

		// When: no NullPointerException is thrown
		RelationshipLinkSpec result = addLinkOperation.rpc(linkDescriptor, null);

		// Then
		assertNull(result.getSourceRank());
		assertNull(result.getTargetRank());
	}
}
