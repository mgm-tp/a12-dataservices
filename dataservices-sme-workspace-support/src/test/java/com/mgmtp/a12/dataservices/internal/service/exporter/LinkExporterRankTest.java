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
package com.mgmtp.a12.dataservices.internal.service.exporter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.internal.model.SmeWorkspaceLinkDescriptor;
import com.mgmtp.a12.dataservices.internal.model.SmeWorkspaceRoleSpec;
import com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelLoader;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.entity.RelationshipLinkEntity;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.entity.RelationshipRoleEntity;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.repository.RelationshipLinkJpaRepository;

import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNotNull;

public class LinkExporterRankTest {

	private static final String PARTNER_ROLE = "Partner";
	private static final String CONTRACT_ROLE = "Contract";

	private RelationshipModelLoader relationshipModelLoader;
	private RelationshipLinkJpaRepository relationshipLinkJpaRepository;
	private ObjectMapper objectMapper;
	private LinkExporter linkExporter;

	@BeforeMethod
	public void setUp() {
		relationshipModelLoader = mock(RelationshipModelLoader.class);
		relationshipLinkJpaRepository = mock(RelationshipLinkJpaRepository.class);
		objectMapper = mock(ObjectMapper.class);
		linkExporter = new LinkExporter(relationshipModelLoader, relationshipLinkJpaRepository, objectMapper);
	}

	private RelationshipLinkEntity buildEntityWithOrders(String partnerOrder, String contractOrder) {
		RelationshipLinkEntity entity = new RelationshipLinkEntity();
		entity.setId("link-1");
		entity.setRelationshipModel("ContractCoInsuredPartner");

		RelationshipRoleEntity partnerRoleEntity = new RelationshipRoleEntity();
		partnerRoleEntity.setName(PARTNER_ROLE);
		partnerRoleEntity.setDocRef(DocumentReference.builder()
			.documentModelName("BusinessPartner")
			.documentId("bp-1")
			.build());
		partnerRoleEntity.setOrder(partnerOrder);

		RelationshipRoleEntity contractRoleEntity = new RelationshipRoleEntity();
		contractRoleEntity.setName(CONTRACT_ROLE);
		contractRoleEntity.setDocRef(DocumentReference.builder()
			.documentModelName("Contract")
			.documentId("contract-1")
			.build());
		contractRoleEntity.setOrder(contractOrder);

		entity.getRoles().put(PARTNER_ROLE, partnerRoleEntity);
		entity.getRoles().put(CONTRACT_ROLE, contractRoleEntity);
		return entity;
	}

	private SmeWorkspaceLinkDescriptor invokeCreateLinkDescriptor(RelationshipLinkEntity entity)
		throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Method method = LinkExporter.class.getDeclaredMethod("createLinkDescriptor", RelationshipLinkEntity.class);
		method.setAccessible(true);
		return (SmeWorkspaceLinkDescriptor) method.invoke(null, entity);
	}

	@Test(description = "Should export roleOrder for both roles of every link")
	public void shouldExportRoleOrderForBothRoles() throws Exception {
		// Given: a RelationshipLinkEntity with two roles, each having a non-null order field
		RelationshipLinkEntity entity = buildEntityWithOrders("order-partner-1", "order-contract-1");

		// When
		SmeWorkspaceLinkDescriptor descriptor = invokeCreateLinkDescriptor(entity);

		// Then: both role specs have non-null roleOrder
		descriptor.getEntities().forEach(roleSpec -> {
			SmeWorkspaceRoleSpec spec = (SmeWorkspaceRoleSpec) roleSpec;
			assertNotNull(spec.getRoleOrder(), "roleOrder must not be null for role: " + spec.getRole());
		});
	}

	@Test(description = "Should export roleOrder regardless of whether relationship model is ordered")
	public void shouldExportRoleOrderRegardlessOfOrderedFlag() throws Exception {
		// Given: a RelationshipLinkEntity for a non-ordered relationship, roles have non-null order
		RelationshipLinkEntity entity = buildEntityWithOrders("order-a", "order-b");

		// When
		SmeWorkspaceLinkDescriptor descriptor = invokeCreateLinkDescriptor(entity);

		// Then: all role specs still have non-null roleOrder
		descriptor.getEntities().forEach(roleSpec -> {
			SmeWorkspaceRoleSpec spec = (SmeWorkspaceRoleSpec) roleSpec;
			assertNotNull(spec.getRoleOrder(), "roleOrder must not be null for role: " + spec.getRole());
		});
	}
}
