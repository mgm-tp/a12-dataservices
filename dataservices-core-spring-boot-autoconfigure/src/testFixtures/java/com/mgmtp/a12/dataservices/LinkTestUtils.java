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
package com.mgmtp.a12.dataservices;

import org.testng.Assert;

import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LinkTestUtils {

	public static void assertProductCampaignLinkRef(String relationshipName, RelationshipLinkSpec link) {
		Assert.assertEquals(link.getLinkDescriptor().getRelationshipModel(), relationshipName);
		Assert.assertNotNull(link.getId());
		Assert.assertNotNull(link.getLinkDescriptor());
		Assert.assertEquals(link.getLinkDescriptor().getEntities().size(), 2);
		Assert.assertNotNull(link.getLinkDescriptor().getEntities().get(0).getDocRef());
		Assert.assertEquals(link.getLinkDescriptor().getEntities().get(0).getRole(), "ProductRole");
		Assert.assertNotNull(link.getLinkDescriptor().getEntities().get(0).getDocRef());
		Assert.assertEquals(link.getLinkDescriptor().getEntities().get(1).getRole(), "CampaignRole");
	}

	public static void assertContractBusinessPartnerLinkRef(String relationshipName, RelationshipLinkSpec link) {
		Assert.assertEquals(link.getLinkDescriptor().getRelationshipModel(), relationshipName);
		Assert.assertNotNull(link.getId());
		Assert.assertNotNull(link.getLinkDescriptor());
		Assert.assertEquals(link.getLinkDescriptor().getEntities().size(), 2);
		Assert.assertNotNull(link.getLinkDescriptor().getEntities().get(0).getDocRef());
		Assert.assertEquals(link.getLinkDescriptor().getEntities().get(0).getRole(), "Partner");
		Assert.assertNotNull(link.getLinkDescriptor().getEntities().get(0).getDocRef());
		Assert.assertEquals(link.getLinkDescriptor().getEntities().get(1).getRole(), "Contract");
	}

	public static void assertPartnerAddressLinkRef(String relationshipName, RelationshipLinkSpec link) {
		Assert.assertEquals(link.getLinkDescriptor().getRelationshipModel(), relationshipName);
		Assert.assertNotNull(link.getId());
		Assert.assertNotNull(link.getLinkDescriptor());
		Assert.assertEquals(link.getLinkDescriptor().getEntities().size(), 2);
		Assert.assertNotNull(link.getLinkDescriptor().getEntities().get(0).getDocRef());
		Assert.assertEquals(link.getLinkDescriptor().getEntities().get(0).getRole(), "Partner");
		Assert.assertNotNull(link.getLinkDescriptor().getEntities().get(0).getDocRef());
		Assert.assertEquals(link.getLinkDescriptor().getEntities().get(1).getRole(), "Address");
	}
}
