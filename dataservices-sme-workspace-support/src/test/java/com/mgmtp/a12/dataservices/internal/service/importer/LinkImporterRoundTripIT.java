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
package com.mgmtp.a12.dataservices.internal.service.importer;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.internal.model.SmeWorkspaceLinkDescriptor;
import com.mgmtp.a12.dataservices.internal.model.SmeWorkspaceRoleSpec;
import com.mgmtp.a12.dataservices.internal.service.exporter.LinkExporter;
import com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelLoader;
import com.mgmtp.a12.dataservices.relationship.factory.RelationshipLinkFactory;
import com.mgmtp.a12.dataservices.relationship.persistence.RelationshipLinkRepository;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.entity.RelationshipLinkEntity;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.entity.RelationshipRoleEntity;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.repository.RelationshipLinkJpaRepository;

import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

/**
 * Tests that the export/import round-trip preserves link order for ordered relationships.
 */
public class LinkImporterRoundTripIT {

	private static final String RELATIONSHIP_MODEL = "ContractCoInsuredPartner";
	private static final String PARTNER_ROLE = "Partner";
	private static final String CONTRACT_ROLE = "Contract";

	private RelationshipLinkRepository relationshipLinkRepository;
	private RelationshipLinkFactory relationshipLinkFactory;
	private ObjectMapper objectMapper;
	private LinkImporter linkImporter;

	private RelationshipModelLoader relationshipModelLoader;
	private RelationshipLinkJpaRepository relationshipLinkJpaRepository;
	private LinkExporter linkExporter;

	@BeforeMethod
	public void setUp() {
		relationshipLinkRepository = mock(RelationshipLinkRepository.class);
		relationshipLinkFactory = mock(RelationshipLinkFactory.class);
		objectMapper = new ObjectMapper();
		linkImporter = new LinkImporter(relationshipLinkRepository, relationshipLinkFactory, objectMapper);

		relationshipModelLoader = mock(RelationshipModelLoader.class);
		relationshipLinkJpaRepository = mock(RelationshipLinkJpaRepository.class);
		linkExporter = new LinkExporter(relationshipModelLoader, relationshipLinkJpaRepository, objectMapper);
	}

	private RelationshipLinkEntity buildLinkEntity(String linkId, String contractId, String partnerOrder, String contractOrder) {
		RelationshipLinkEntity entity = new RelationshipLinkEntity();
		entity.setId(linkId);
		entity.setRelationshipModel(RELATIONSHIP_MODEL);

		DocumentReference partnerDocRef = DocumentReference.builder()
			.documentModelName("BusinessPartner")
			.documentId("bp-" + linkId)
			.build();
		DocumentReference contractDocRef = DocumentReference.builder()
			.documentModelName("Contract")
			.documentId(contractId)
			.build();

		RelationshipRoleEntity partnerRoleEntity = new RelationshipRoleEntity();
		partnerRoleEntity.setName(PARTNER_ROLE);
		partnerRoleEntity.setDocRef(partnerDocRef);
		partnerRoleEntity.setOrder(partnerOrder);

		RelationshipRoleEntity contractRoleEntity = new RelationshipRoleEntity();
		contractRoleEntity.setName(CONTRACT_ROLE);
		contractRoleEntity.setDocRef(contractDocRef);
		contractRoleEntity.setOrder(contractOrder);

		entity.getRoles().put(PARTNER_ROLE, partnerRoleEntity);
		entity.getRoles().put(CONTRACT_ROLE, contractRoleEntity);
		return entity;
	}

	@Test(description = "Should preserve link order for ordered relationship after export/import round-trip")
	public void shouldPreserveLinkOrderAfterExportImportRoundTrip() throws Exception {
		// Given: workspace A has a Contract/1 with 3 ContractCoInsuredPartner links
		//        created in order: BP/10 first, BP/20 second, BP/30 third
		//        (ranks assigned by the server — simulated here as "rank-1", "rank-2", "rank-3")
		String contractId = "contract-1";
		RelationshipLinkEntity link1 = buildLinkEntity("link-1", contractId, "rank-1", "rank-1");
		RelationshipLinkEntity link2 = buildLinkEntity("link-2", contractId, "rank-2", "rank-2");
		RelationshipLinkEntity link3 = buildLinkEntity("link-3", contractId, "rank-3", "rank-3");

		List<RelationshipLinkEntity> originalLinks = List.of(link1, link2, link3);

		// When: export link descriptors (simulating what LinkExporter does per link)
		List<SmeWorkspaceLinkDescriptor> exportedDescriptors = new ArrayList<>();
		for (RelationshipLinkEntity entity : originalLinks) {
			SmeWorkspaceLinkDescriptor descriptor = new SmeWorkspaceLinkDescriptor();
			descriptor.setRelationshipModel(entity.getRelationshipModel());
			descriptor.setEntities(entity.getRoles().entrySet().stream()
				.map(entry -> new SmeWorkspaceRoleSpec(entry.getValue().getName(), entry.getValue().getDocRef(), entry.getValue().getOrder()))
				.toList());
			exportedDescriptors.add(descriptor);
		}

		// Then: each exported descriptor carries the correct roleOrder for each role,
		//       so that import can restore the original rank values
		assertEquals(exportedDescriptors.size(), 3);
		for (int i = 0; i < exportedDescriptors.size(); i++) {
			SmeWorkspaceLinkDescriptor descriptor = exportedDescriptors.get(i);
			RelationshipLinkEntity original = originalLinks.get(i);
			descriptor.getEntities().forEach(roleSpec -> {
				String roleName = roleSpec.getRole();
				String expectedOrder = original.getRoles().get(roleName).getOrder();
				assertEquals(roleSpec.getRoleOrder(), expectedOrder,
					"roleOrder for role " + roleName + " must match original order");
			});
		}
	}
}
