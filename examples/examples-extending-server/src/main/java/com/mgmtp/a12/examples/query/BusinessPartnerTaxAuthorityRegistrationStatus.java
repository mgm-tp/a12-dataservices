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
package com.mgmtp.a12.examples.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.annotation.QueryProjection;
import com.mgmtp.a12.dataservices.query.projection.IQueryProjection;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import lombok.NonNull;

import static com.mgmtp.a12.examples.query.BusinessPartnerTaxAuthorityRegistrationStatus.PROJECTION_NAME;

/**
 * In this example, we are trying to load as many documents as it is allowed by the system by setting max page size to the limit.
 * 1. Validating the query target document mode (must be BusinessPartner)
 * 2. Validating the query if it contains links. It shouldn't
 * 3. Returned results contain a links section that has been loaded from 3rd party server (tax Authority servers)
 */
@Order(100)
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples", name = "business-partner-tax-authority-registration-status-projection.enabled", havingValue = "true")
@QueryProjection(PROJECTION_NAME)
@Component public class BusinessPartnerTaxAuthorityRegistrationStatus implements IQueryProjection<DocumentTreeResult> {

	/**
	 * Name of the target document model for this projection.
	 */
	public static final String BUSINESS_PARTNER_DOCUMENT_MODEL_NAME = "BusinessPartner";
	/**
	 * Projection registration name.
	 */
	public static final String PROJECTION_NAME = "businessPartnerTaxAuthorityRegistrationStatus";
	/**
	 * Document model name of the mapped tax authority registration status.
	 */
	public static final String MAPPED_DOCUMENTS_DOCUMENT_MODEL_NAME = "TaxAuthorityRegistrationStatus";
	/**
	 * Relationship model name linking business partners to tax authority registration status.
	 */
	public static final String MAPPED_DOCUMENTS_RELATIONSHIP_MODEL_NAME = "BusinessPartnerTaxAuthorityRegistrationStatus";

	/**
	 * Validates the original query for target model and link usage.
	 * Rejects queries not targeting {@link #BUSINESS_PARTNER_DOCUMENT_MODEL_NAME} or specifying links.
	 *
	 * @param originalQuery the incoming query to validate; must not be null.
	 * @param context the query execution context; may be null.
	 * @return the validated query unchanged.
	 * @throws com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException if validation fails.
	 */
	@Override public @NonNull QueryRoot preprocess(@NonNull QueryRoot originalQuery, QueryContext context) {
		if (!BUSINESS_PARTNER_DOCUMENT_MODEL_NAME.equals(originalQuery.getTargetDocumentModel())) {
			throw new QueryInvalidInputException(
				ExceptionKeys.ExecutionPhase.QUERY_VALIDATION,
				"query.validation.projection.targetDocumentModel.invalid",
				String.format("%s projection does only allow documents of model %s", PROJECTION_NAME, BUSINESS_PARTNER_DOCUMENT_MODEL_NAME));
		}

		if (originalQuery.getLinks() != null && !originalQuery.getLinks().isEmpty()) {
			throw new QueryInvalidInputException(
				ExceptionKeys.ExecutionPhase.QUERY_VALIDATION,
				"query.validation.projection.links.not.allowed",
				String.format("%s projection does not allow for links to be specified because they are loaded automatically from 3rd party servers",
					PROJECTION_NAME));
		}

		return originalQuery;
	}

	/**
	 * Enriches the query result by calling a 3rd-party service and adding link results to the response.
	 *
	 * @param originalQuery the original query root; must not be null.
	 * @param queryResult the page returned by the base query execution; must not be null.
	 * @param context the query execution context; may be null.
	 * @return a {@link QueryPage} containing the original results and the added link entries.
	 */
	@Override
	public @NonNull QueryPage<DocumentTreeResult> postprocess(@NonNull QueryRoot originalQuery, @NonNull Page<DocumentTreeResult> queryResult,
		QueryContext context) {
		List<DocumentTreeResult> enrichedResultSet = getLinkedDocumentsFrom3rdPartyServer(queryResult.getContent());

		return QueryPage.of(enrichedResultSet, queryResult.getTotalElements(),
			queryResult.getSize(), queryResult.getNumber(), Collections.emptyMap());
	}

	private List<DocumentTreeResult> getLinkedDocumentsFrom3rdPartyServer(List<DocumentTreeResult> documents) {
		ArrayList<DocumentTreeResult> links = new ArrayList<>();
		documents.forEach(document -> {
			DocumentReference documentReference = new DocumentReference(MAPPED_DOCUMENTS_DOCUMENT_MODEL_NAME, UUID.randomUUID().toString());
			links.add(
				DocumentTreeResult.builder()
					.sourceDocRef(document.getDocRef())
					.targetDocRef(documentReference)
					.docRef(documentReference)
					.document(mock3rdPartyServerResponse())
					.linkId(UUID.randomUUID().toString())
					.type(DocumentTreeNodeType.CHILD)
					.relationshipModel(MAPPED_DOCUMENTS_RELATIONSHIP_MODEL_NAME)
					.build()
			);
		});

		links.addAll(documents);
		return links;
	}

	private JsonNode mock3rdPartyServerResponse() {
		ObjectMapper objectMapper = new ObjectMapper();

		// Create the root object node
		ObjectNode rootNode = objectMapper.createObjectNode();
		String processId = UUID.randomUUID().toString();

		// Add fields to the JSON object
		rootNode.put("status", "active");
		rootNode.put("processId", processId);

		return rootNode;
	}
}
