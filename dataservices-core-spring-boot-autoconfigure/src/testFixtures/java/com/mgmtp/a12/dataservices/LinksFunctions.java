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

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import com.mgmtp.a12.dataservices.configuration.internal.validation.condition.OnEnabledRpcCondition;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.operation.internal.AddLinkOperation;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.LinkPosition;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;

@Conditional(OnEnabledRpcCondition.class)
@Component
public class LinksFunctions {

	@Autowired AddLinkOperation addLinkOperation;

	public RelationshipLinkSpec addLink(LinkDescriptor linkDescriptor, JsonNode linkDocument) {
		return addLinkOperation.rpc(linkDescriptor, linkDocument);
	}

	public RelationshipLinkSpec addLink(String relationshipModel, String role1, DocumentReference docRef1, String role2, DocumentReference docRef2,
		LinkPosition position, JsonNode linkDocument) {
		LinkDescriptor linkDescriptor = createLinkDescriptor(relationshipModel, role1, docRef1, role2, docRef2, position);
		return addLink(linkDescriptor, linkDocument);
	}

	public RelationshipLinkSpec addLink(String relationshipModel, String role1, DocumentReference docRef1, String role2, DocumentReference docRef2,
		LinkPosition position) {
		return addLink(relationshipModel, role1, docRef1, role2, docRef2, position, null);
	}

	public RelationshipLinkSpec addLink(String relationshipModel, String role1, DocumentReference docRef1, String role2, DocumentReference docRef2) {
		return addLink(relationshipModel, role1, docRef1, role2, docRef2, LinkPosition.TOP, null);
	}

	public LinkDescriptor createLinkDescriptor(String relationshipModel, String role1, DocumentReference docRef1, String role2, DocumentReference docRef2,
		LinkPosition position) {
		LinkDescriptor linkDescriptor = new LinkDescriptor();
		linkDescriptor.setRelationshipModel(relationshipModel);
		RelationshipRoleSpec relationshipRoleSpec1 = new RelationshipRoleSpec(role1, docRef1);
		RelationshipRoleSpec relationshipRoleSpec2 = new RelationshipRoleSpec(role2, docRef2);
		linkDescriptor.setEntities(Arrays.asList(relationshipRoleSpec1, relationshipRoleSpec2));
		linkDescriptor.setPosition(position == null ? LinkPosition.TOP : position);
		return linkDescriptor;
	}

}
