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
package com.mgmtp.a12.dataservices.relationship.internal;

import java.util.List;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.RelationshipRole;
import com.mgmtp.a12.dataservices.relationship.events.RelationshipLinkAfterCreateEvent;
import com.mgmtp.a12.dataservices.relationship.model.EntityCharacteristics;
import com.mgmtp.a12.dataservices.relationship.model.LinkConstraints;
import com.mgmtp.a12.dataservices.relationship.model.Multiplicity;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModelContent;
import com.mgmtp.a12.dataservices.rpc.RpcException;
import com.mgmtp.a12.model.header.Header;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Listeners(MockitoTestNGListener.class)
public class RelationshipLinkValidationListenerTest {

	@Mock private DefaultRelationshipLinkService relationshipLinkService;

	private RelationshipLinkValidationListener listener;

	@BeforeMethod
	public void setUp() {
		listener = new RelationshipLinkValidationListener(relationshipLinkService);
	}

	/**
	 * Tests that the exception message contains the role name, relationship model name, upper limit,
	 * current link count, and document reference when the upper limit is exceeded.
	 */
	@Test(description = "Should include limit, count, and document reference in exception message when upper limit is exceeded")
	public void shouldIncludeLimitCountAndDocRefInMessageWhenUpperLimitExceeded() {
		// Given
		// The source role is the role whose document is adding the link.
		// findTargetEntityCharacteristicsByRole returns the OTHER role's EntityCharacteristics,
		// which is placed in the exception message as the constrained role name.
		String sourceRoleName = "policyholder";
		String constrainedRoleName = "insured";
		String linkModelName = "myLinkModel";
		String relationshipModelId = "myLinkModel";
		DocumentReference docRef = new DocumentReference("Policy", "abc123");
		int upperLimit = 5;

		Header header = mock(Header.class);
		when(header.getId()).thenReturn(relationshipModelId);

		Multiplicity multiplicity = new Multiplicity();
		multiplicity.setUnbounded(false);
		multiplicity.setUpperLimit(upperLimit);

		LinkConstraints linkConstraints = new LinkConstraints();
		linkConstraints.setMultiplicity(multiplicity);

		// Source characteristic — the role whose doc is creating the link
		EntityCharacteristics sourceChar = new EntityCharacteristics();
		sourceChar.setRole(sourceRoleName);

		// Target characteristic — constrained by upper limit; this role name appears in the message
		EntityCharacteristics targetChar = new EntityCharacteristics();
		targetChar.setRole(constrainedRoleName);
		targetChar.setLinkConstraints(linkConstraints);

		RelationshipModelContent content = new RelationshipModelContent();
		content.setEntityCharacteristics(List.of(sourceChar, targetChar));

		RelationshipModel relationshipModel = new RelationshipModel(header, content);

		RelationshipRole role = mock(RelationshipRole.class);
		when(role.getName()).thenReturn(sourceRoleName);
		when(role.getDocRef()).thenReturn(docRef);

		com.mgmtp.a12.dataservices.relationship.RelationshipLink link = mock(com.mgmtp.a12.dataservices.relationship.RelationshipLink.class);
		java.util.Map<String, RelationshipRole> rolesMap = new java.util.HashMap<>();
		rolesMap.put(sourceRoleName, role);
		// Use doReturn to bypass type-checking on wildcard return type Map<String, ? extends RelationshipRole>
		org.mockito.Mockito.doReturn(rolesMap).when(link).getRoles();
		when(link.getRelationshipModel()).thenReturn(linkModelName);

		// countByRole returns upperLimit + 1 to trigger the violation
		when(relationshipLinkService.countByRole(relationshipModelId, sourceRoleName, docRef)).thenReturn((long) (upperLimit + 1));

		RelationshipLinkAfterCreateEvent event = mock(RelationshipLinkAfterCreateEvent.class);
		when(event.getRelationshipModel()).thenReturn(relationshipModel);
		when(event.getLink()).thenReturn(link);

		listener.linkAddedEventListener(event);

		// When / Then
		try {
			listener.validateLinks();
			fail("Expected RpcException to be thrown");
		} catch (RpcException e) {
			String message = e.getOperationError().getLongMessage().getDefaultMessage();
			assertTrue(message.contains(constrainedRoleName), "Message should contain constrained role name: " + message);
			assertTrue(message.contains(linkModelName), "Message should contain relationship model name: " + message);
			assertTrue(message.contains(String.valueOf(upperLimit)), "Message should contain upper limit: " + message);
			assertTrue(message.contains(String.valueOf(upperLimit + 1)), "Message should contain current link count: " + message);
			assertTrue(message.contains(docRef.toString()), "Message should contain document reference: " + message);
		}
	}
}
