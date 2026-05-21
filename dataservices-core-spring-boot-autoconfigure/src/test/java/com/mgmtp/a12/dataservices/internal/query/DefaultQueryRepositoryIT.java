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
package com.mgmtp.a12.dataservices.internal.query;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.model.internal.IndexedModelFieldCache;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.QueryRepository;
import com.mgmtp.a12.dataservices.query.enrichment.QueryEnricher;
import com.mgmtp.a12.dataservices.query.internal.DefaultQueryContext;
import com.mgmtp.a12.dataservices.query.internal.DefaultQueryService;
import com.mgmtp.a12.dataservices.query.internal.QueryContextHelper;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.spec.LinkPosition;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class DefaultQueryRepositoryIT extends AbstractSpringContextIT {

	@Autowired QueryRepository queryRepository;
	@Autowired IModelLoader<RelationshipModel> relationshipModelLoader;
	@Autowired QueryContextHelper queryContextHelper;
	@Autowired DefaultQueryService defaultQueryService;
	@Autowired QueryEnricher queryEnricher;
	@Autowired IndexedModelFieldCache indexedModelFieldCache;

	DocumentReference docRefContract;
	DocumentReference docRefPartner;
	DocumentReference docRefCoInsured;
	RelationshipLinkSpec relationshipLinkSpecPartner;
	RelationshipLinkSpec relationshipLinkSpecCoInsured;

	@BeforeMethod
	public void init() throws Exception {
		super.cleanUpTestEnvironment();
		modelsFunctions.createModel(PathConstants.CONTRACT_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.COINSURED_ADDITIONAL_PARTNER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.CONTRACT_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH);

		docRefContract = documentFunctions.createDocumentFromFileAndGetDocRef("Contract", "document/Contract-1.json");
		docRefPartner = documentFunctions.createDocumentFromFileAndGetDocRef("BusinessPartner", "document/BusinessPartner-1.json");
		docRefCoInsured = documentFunctions.createDocumentFromFileAndGetDocRef("BusinessPartner", "document/BusinessPartner-2.json");
		JsonNode linkDocJsonNode = objectMapper.readTree(new ClassPathResource("document/CoInsuredAdditionalFields-1.json").getInputStream());

		relationshipLinkSpecPartner = linksFunctions.addLink("ContractBusinessPartner", "Contract", docRefContract, "Partner", docRefPartner);
		relationshipLinkSpecCoInsured =
			linksFunctions.addLink("ContractCoInsuredPartner", "Contract", docRefContract, "Partner", docRefCoInsured, LinkPosition.TOP, linkDocJsonNode);
	}

	@Test
	public void testEmptyContentWhenNoFieldsSpecified() {
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName("document")
			.targetDocumentModel("Contract")
			.links(
				List.of(
					QueryLink.builder().relationshipModel("ContractBusinessPartner").targetRole("Partner").build(),
					QueryLink.builder().relationshipModel("ContractCoInsuredPartner").targetRole("Partner").build()
				)
			)
			.paging(Paging.builder().pageNumber(0).pageSize(100).build())
			.build();
		List<DocumentTreeNodeType> types = List.of(DocumentTreeNodeType.CHILD, DocumentTreeNodeType.LINK, DocumentTreeNodeType.ROOT);
		QueryContext context =
			new DefaultQueryContext(documentModelLoader, relationshipModelLoader, defaultQueryService::queryWithoutProjection, documentModelServiceFactory,
				queryContextHelper, indexedModelFieldCache,
				null, queryRoot);

		queryEnricher.enrichQuery(queryRoot, context);

		Page<DocumentTreeResult> queryResult = queryRepository.query(queryRoot, types, context);

		queryResult.stream().forEach(r -> {
			JsonNode document = r.getDocument();
			assertTrue("Without fields specification an empty document should be returned", document.isEmpty());
		});
	}

	@Test
	public void testNonEmptyContentWhenFieldsSpecified() {
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName("document")
			.targetDocumentModel("Contract")
			.fields(List.of("/Contract/ContractName"))
			.links(
				List.of(
					QueryLink.builder().relationshipModel("ContractBusinessPartner").targetRole("Partner")
						.fields(List.of("/BusinessPartnerRoot/Name")).build(),
					QueryLink.builder().relationshipModel("ContractCoInsuredPartner").targetRole("Partner")
						.fields(List.of("/BusinessPartnerRoot/Name"))
						.linkDocumentFields(List.of("/CoInsuredRoot/Name")).build()
				)
			)
			.paging(Paging.builder().pageNumber(0).pageSize(100).build())
			.build();
		List<DocumentTreeNodeType> types = List.of(DocumentTreeNodeType.CHILD, DocumentTreeNodeType.LINK, DocumentTreeNodeType.ROOT);
		QueryContext context =
			new DefaultQueryContext(documentModelLoader, relationshipModelLoader, defaultQueryService::queryWithoutProjection, documentModelServiceFactory,
				queryContextHelper, indexedModelFieldCache,
				null,
				queryRoot);

		queryEnricher.enrichQuery(queryRoot, context);

		Page<DocumentTreeResult> queryResult = queryRepository.query(queryRoot, types, context);

		queryResult.stream().forEach(r -> {
			JsonNode document = r.getDocument();
			assertFalse("With fields specification the document should not be empty", document.isEmpty());
		});
	}
}
