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
import java.util.UUID;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.internal.ranks.ComputedRank;
import com.mgmtp.a12.dataservices.relationship.internal.ranks.RelationshipRankService;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;

import static org.mockito.Mockito.when;

public class DefaultRelationshipLinkFactoryTest extends AbstractDataServicesCoreTest {

	@Mock private RelationshipRankService relationshipRankService;
	@Mock private RelationshipValidationSupport relationshipValidationSupport;

	@InjectMocks private DefaultRelationshipLinkFactory relationshipLinkFactory;

	@Test void shouldCreateLinkWithNewComputedRankWhenNoneProvided() {
		Mockito.reset(relationshipRankService, relationshipValidationSupport);
		LinkDescriptor mockedLinkDescriptor = mockLinkDescriptor();

		when(relationshipRankService.computeRank(mockedLinkDescriptor)).thenReturn(
			new ComputedRank("11111", "22222")
		);

		RelationshipLink relationshipLink = relationshipLinkFactory.createLink(mockedLinkDescriptor, null, null);

		Mockito.verify(relationshipValidationSupport, Mockito.times(1)).validateLink(mockedLinkDescriptor, null);
		Mockito.verify(relationshipRankService, Mockito.times(1)).computeRank(mockedLinkDescriptor);

		Assert.assertEquals(relationshipLink.getRoles().size(), 2);
		Assert.assertEquals(relationshipLink.getRoles().get(RelationshipModelConstants.RoleConstants.CONTRACT_ROLE).getOrder(), "11111");
		Assert.assertEquals(relationshipLink.getRoles().get(RelationshipModelConstants.RoleConstants.PARTNER_ROLE).getOrder(), "22222");
	}

	@Test void shouldCreateLinkWithExistingComputedRankWhenProvided() {
		Mockito.reset(relationshipRankService, relationshipValidationSupport);
		LinkDescriptor mockedLinkDescriptor = mockLinkDescriptor();

		RelationshipLink relationshipLink = relationshipLinkFactory.createLink(mockedLinkDescriptor, null, new ComputedRank("11111", "22222"));

		Mockito.verify(relationshipValidationSupport, Mockito.times(1)).validateLink(mockedLinkDescriptor, null);
		Mockito.verifyNoInteractions(relationshipRankService);

		Assert.assertEquals(relationshipLink.getRoles().size(), 2);
		Assert.assertEquals(relationshipLink.getRoles().get(RelationshipModelConstants.RoleConstants.CONTRACT_ROLE).getOrder(), "11111");
		Assert.assertEquals(relationshipLink.getRoles().get(RelationshipModelConstants.RoleConstants.CONTRACT_ROLE).getDocRef(), mockedLinkDescriptor.getSourceRole().getDocRef());
		Assert.assertEquals(relationshipLink.getRoles().get(RelationshipModelConstants.RoleConstants.PARTNER_ROLE).getOrder(), "22222");
		Assert.assertEquals(relationshipLink.getRoles().get(RelationshipModelConstants.RoleConstants.PARTNER_ROLE).getDocRef(), mockedLinkDescriptor.getTargetRole().getDocRef());
	}

	private LinkDescriptor mockLinkDescriptor() {
		LinkDescriptor linkDescriptor = new LinkDescriptor();
		linkDescriptor.setRelationshipModel(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL);
		linkDescriptor.setEntities(
			List.of(
				new RelationshipRoleSpec(RelationshipModelConstants.RoleConstants.CONTRACT_ROLE, DocumentReference.builder().documentModelName(
					DocumentModelConstants.CONTRACT_DOCUMENT_MODEL).documentId(UUID.randomUUID().toString()).build()),
				new RelationshipRoleSpec(RelationshipModelConstants.RoleConstants.PARTNER_ROLE, DocumentReference.builder().documentModelName(
					DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL).documentId(UUID.randomUUID().toString()).build())
			)
		);
		return linkDescriptor;
	}

}
