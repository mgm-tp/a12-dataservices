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
package com.mgmtp.a12.dataservices.relationship.internal.ranks;

import java.util.List;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.DefaultRelationshipLinkRepository;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;
import com.mgmtp.a12.dataservices.rpc.RpcException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Listeners(MockitoTestNGListener.class)
public class RelationshipRankServiceTest {

	@Mock private DefaultRelationshipLinkRepository relationshipLinkRepository;

	private RelationshipRankService relationshipRankService;

	@BeforeMethod
	public void setUp() {
		relationshipRankService = new RelationshipRankService(relationshipLinkRepository);
	}

	/**
	 * Tests that the exception message includes the relationship model name, source role, and document reference
	 * when the predecessor link cannot be found in the list of existing links.
	 */
	@Test(description = "Should include model name, role, and document reference in exception message when predecessor link is not found")
	public void shouldIncludeModelNameRoleAndDocRefInMessageWhenPredecessorLinkNotFound() {
		// Given: a ranked relationship "myLinkModel" with role "insured", document reference "Policy/abc123"
		//        a list of existing links that does NOT contain predecessorRef "missing-id"
		String relationshipModelName = "myLinkModel";
		String sourceRole = "insured";
		DocumentReference docRef = new DocumentReference("Policy", "abc123");
		String missingPredecessorRef = "missing-id";

		RelationshipRoleSpec sourceRoleSpec = mock(RelationshipRoleSpec.class);
		when(sourceRoleSpec.getRole()).thenReturn(sourceRole);
		when(sourceRoleSpec.getDocRef()).thenReturn(docRef);

		LinkDescriptor linkDescriptor = mock(LinkDescriptor.class);
		when(linkDescriptor.getRelationshipModel()).thenReturn(relationshipModelName);
		when(linkDescriptor.getSourceRole()).thenReturn(sourceRoleSpec);
		when(linkDescriptor.getPredecessorLinkRef()).thenReturn(missingPredecessorRef);

		// Existing links list is non-empty but does not contain the predecessor link
		RelationshipLink existingLink = mock(RelationshipLink.class);
		when(existingLink.getId()).thenReturn("other-id");

		// countByRole returns > 0 so the "init set" branch is not taken
		when(relationshipLinkRepository.countByRole(eq(relationshipModelName), eq(sourceRole), eq(docRef))).thenReturn(1L);

		// findByRelationshipModelNameAndSource returns a page with one link (not the predecessor)
		// Use doReturn to avoid type-checking issues with wildcard return type Page<? extends RelationshipLink>
		org.mockito.Mockito.doReturn(new PageImpl<>(List.of(existingLink)))
			.when(relationshipLinkRepository)
			.findByRelationshipModelNameAndSource(
				eq(relationshipModelName), eq(sourceRole), eq(docRef), any(Pageable.class)
			);

		// When / Then
		try {
			relationshipRankService.computeRank(linkDescriptor);
			fail("Expected RpcException to be thrown");
		} catch (RpcException e) {
			String message = e.getOperationError().getLongMessage().getDefaultMessage();
			assertTrue(message.contains(relationshipModelName), "Message should contain relationship model name: " + message);
			assertTrue(message.contains(sourceRole), "Message should contain source role: " + message);
			assertTrue(message.contains(docRef.toString()), "Message should contain document reference: " + message);
			assertTrue(message.contains(missingPredecessorRef), "Message should contain missing predecessor ref: " + message);
		}
	}
}
